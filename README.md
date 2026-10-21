# Gboard Rectangular Keys — Morphe patch

Target: Gboard `18.0.3.954559732-release-arm64-v8a`.

This patch changes the pill-shaped action-key styles used by Gboard's `?123` and `Enter` keys to the regular rectangular action-key styles.

It intentionally does not touch numpad styles or ordinary letter-key styles.

## Build

Use JDK 21 and the Morphe patch Gradle plugin. The standard Morphe build task produces an `.mpp` bundle under `patches/build/libs/`.

```bash
./gradlew buildAndroid
```

The generated `.mpp` can then be imported as a local patch bundle in Morphe Manager.
