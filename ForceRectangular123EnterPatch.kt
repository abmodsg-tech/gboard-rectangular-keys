package app.gboard.rectangularkeys

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private const val GBOARD_PACKAGE = "com.google.android.inputmethod.latin"

// These are the action-key styles present in Gboard 18.0.3.954559732-release.
// They are the pill-shaped variants used by the ?123 / Enter action-key holders.
private val PILL_ACTION_STYLES = mapOf(
    ".keytop.for-action-key-holder.pill-shaped" to ".keytop.for-action-key-holder",
    ".keytop.for-bottom-key.for-action-key-holder.pill-shaped" to ".keytop.for-bottom-key.for-action-key-holder",
    ".keytop.semi-rounded.for-action-key-holder.pill-shaped" to ".keytop.for-action-key-holder",
)

private fun Element.androidTagItem(): Element? =
    (0 until childNodes.length)
        .asSequence()
        .mapNotNull { childNodes.item(it) as? Element }
        .firstOrNull { it.nodeName == "item" && it.getAttribute("name") == "android:tag" }

@Suppress("unused")
val forceRectangular123EnterPatch = resourcePatch(
    name = "Gboard: rectangular ?123 and Enter keys",
    description = "Forces Gboard's ?123 and Enter action keys to use the regular rectangular shape instead of the pill shape.",
    default = false,
) {
    execute {
        if (packageMetadata.packageName != GBOARD_PACKAGE) {
            throw PatchException("This patch is only for Gboard ($GBOARD_PACKAGE)")
        }

        val valueFiles = listApkEntries("res/values/")
            .filter { it.endsWith(".xml", ignoreCase = true) }

        val found = mutableSetOf<String>()
        var changed = 0

        for (path in valueFiles) {
            document(path).use { doc ->
                val styles = doc.getElementsByTagName("style")
                for (i in 0 until styles.length) {
                    val style = styles.item(i) as? Element ?: continue
                    val styleName = style.getAttribute("name")
                    val replacement = PILL_ACTION_STYLES[styleName] ?: continue
                    found += styleName

                    val tag = style.androidTagItem()
                        ?: throw PatchException("Style $styleName has no android:tag item")

                    if (tag.textContent != replacement) {
                        tag.textContent = replacement
                        changed++
                    }
                }
            }
        }

        val missing = PILL_ACTION_STYLES.keys - found
        if (missing.isNotEmpty()) {
            throw PatchException(
                "Gboard action-key styles not found: ${missing.joinToString()}. " +
                    "This patch targets the resource structure of Gboard 18.0.3.954559732-release."
            )
        }

        if (changed == 0) {
            throw PatchException("The Gboard action-key styles were already rectangular or could not be changed")
        }
    }
}
