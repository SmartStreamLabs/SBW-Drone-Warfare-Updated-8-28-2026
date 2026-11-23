# VFX Source

The runtime JAR in this release was built from this source code. QA helpers are added only through an external Gradle init script and are not included in the runtime JAR.

Use Java 17 and the Gradle Wrapper. Build with `gradlew.bat clean build -Psbw0891Jar=<path>`.
The official Superb Warfare JAR is an existing external dependency. Supply it yourself using `-Psbw0891Jar=<absolute path to the official MC 1.20.1 0.8.9.1 JAR>`.

The three original 4×4 atlases and their exact generation prompts are provided separately in the release's assets folder. `PackVfx.java` performs the technical import: cropping, alpha-correct downscaling to 128×128, and adding eight pixels of transparent padding around the edges. All 48 imported frames are also included in `src/main/resources`.

Each smoke or fire lobe runs its animation independently. The shared particle atlas uses mip level 0; the custom fragment rendering uses alpha-correct filtering without changing other mods' atlas settings.

The client renderer sorts by distance and uses a single batch after clouds and precipitation. On Forge, the world matrix is captured at `AFTER_PARTICLES` because Forge 1.20.1 supplies the projection stack at `AFTER_LEVEL`. Render and fog states are restored after the batch.

See `TESTRESULTATEN.md` and the manifests for the runtime combinations actually tested and their limitations.
