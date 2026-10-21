package app.gboard.rectangularkeys

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private const val ACTION_PILL = "for-action-key-holder"
private const val BOTTOM_ACTION_PILL = "for-bottom-key.for-action-key-holder"
private const val PILL = "pill-shaped"
private const val NUMPAD = "for-numpad"

private fun Element.tagValue(): String? =
    childNodes.let { nodes ->
        (0 until nodes.length)
            .asSequence()
            .mapNotNull { nodes.item(it) as? Element }
            .firstOrNull {
                it.nodeName == "item" &&
                it.getAttribute("name") == "android:tag"
            }
            ?.textContent
    }

@Suppress("unused")
val forceRectangular123EnterPatch = resourcePatch(
    name = "Force rectangular 123 and Enter keys",
    description = "Makes Gboard's ?123 and Enter action-key backgrounds use the regular non-pill shape.",
) {
    execute {
        val valuesDir = get("res/values")
        val xmlFiles = valuesDir.listFiles()
            ?.filter {
                it.isFile && it.extension.equals("xml", ignoreCase = true)
            }
            ?: emptyList()

        val styles = linkedMapOf<String, Element>()
        val documents = mutableListOf<AutoCloseable>()

        try {
            for (file in xmlFiles) {
                val document = document(file.inputStream())
                documents += document

                val nodes = document.getElementsByTagName("style")
                for (i in 0 until nodes.length) {
                    val style = nodes.item(i) as? Element ?: continue
                    val name = style.getAttribute("name")
                    if (name.isNotBlank()) styles[name] = style
                }
            }

            if (styles.isEmpty()) {
                throw PatchException("No decoded Gboard styles were found")
            }

            var changed = 0

            styles.values.forEach { style ->
                val tag = style.tagValue() ?: return@forEach

                if (!tag.contains(PILL) ||
                    !tag.contains(ACTION_PILL) ||
                    tag.contains(NUMPAD)
                ) {
                    return@forEach
                }

                val replacementTag =
                    if (tag.contains(BOTTOM_ACTION_PILL)) {
                        ".keytop.for-bottom-key.for-action-key-holder"
                    } else {
                        ".keytop.for-action-key-holder"
                    }

                val item = (0 until style.childNodes.length)
                    .asSequence()
                    .mapNotNull {
                        style.childNodes.item(it) as? Element
                    }
                    .firstOrNull {
                        it.nodeName == "item" &&
                        it.getAttribute("name") == "android:tag"
                    }
                    ?: throw PatchException(
                        "Missing android:tag in style ${style.getAttribute("name")}"
                    )

                if (item.textContent != replacementTag) {
                    item.textContent = replacementTag
                    changed++
                }
            }

            if (changed == 0) {
                throw PatchException(
                    "No Gboard action-key pill styles matched"
                )
            }
        } finally {
            documents.asReversed().forEach { it.close() }
        }
    }
}
