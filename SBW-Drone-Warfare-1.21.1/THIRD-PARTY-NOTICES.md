# Third-party notices

Paths below are relative to `src/main/resources/assets/sbwdroneconfig/`
(or `assets/sbwdroneconfig/` inside the jar). Date checked: 2026-11-22.
Historical credits are evidence, not proof of the project owner's lack of rights.
Conversely, removal of a notice does not establish ownership or cancel a license.

| Component / files | Creator | Source | License / status | Attribution and modifications |
| --- | --- | --- | --- | --- |
| `models/item/fpv_goggles.obj`, `.mtl`, `textures/item/fpv_goggles.png` | LINE-O, as recorded in both model headers | https://skfb.ly/ptSTu | CC BY 4.0 recorded in original headers; source page unavailable during audit | Meshy FPV Goggles by LINE-O, https://skfb.ly/ptSTu, https://creativecommons.org/licenses/by/4.0/ . Converted/integrated for the mod. Retain credit, license link and modification indication if this source applies. |
| `models/item/delivery_drone.obj`, `.mtl`, `textures/item/delivery_drone.png` | Phoenix..., as recorded in model headers | https://skfb.ly/ory8t | CC BY 4.0 recorded in original headers; source page unavailable during audit | “uAV” by Phoenix..., https://skfb.ly/ory8t, https://creativecommons.org/licenses/by/4.0/ . Converted as Delivery Drone inventory geometry. New world geometry does not establish replacement of this item asset. |
| `sounds/explosion_audio/drone_close_user.ogg` | SamsterBirdies | https://freesound.org/people/SamsterBirdies/sounds/760509/ ; supplied `760509__samsterbirdies__astronomical-explosion.flac` | CC0 1.0 verified on the individual Freesound page | Astronomical Explosion by SamsterBirdies. Attribution is voluntary under CC0. Audio converted for game playback; not owned exclusively by this project. https://creativecommons.org/publicdomain/zero/1.0/ |
| `sounds/explosion_audio/drone_distant_user.ogg` | Not confirmed; supplied filename labels freesound_community | Supplied `freesound_community-distant-explosion-90743.mp3`; candidate https://pixabay.com/sound-effects/distant-explosion-90743/ unavailable during audit | Individual source/license not independently verified | Do not infer original author or license solely from filename. Obtain asset page or download certificate before resolving attribution. |
| `sounds/explosion_audio/small_*.ogg`, `heavy_*.ogg` | Individual authors unknown | Four user-supplied MP3s; exact inputs/hashes and conversion filters in `sounds/explosion_audio/manifest.json` | Earlier supplier statement: Pixabay Content License; asset-specific proof missing | Preserve `sounds/explosion_audio/license.txt`. No CC0 assumption. Exact author, URL and applicable terms still needed for each input. |
| `sounds/distance_war/distant_war_01.ogg`, `_02.ogg`, `_03.ogg` | Unknown | User-supplied Distance_War_Sounds_Natural_Clips.zip | No license demonstrated by the existing notice | The existing notice documents byte-for-byte integration, not a redistribution grant. Creator/source/license required. |
| `sounds/music/flag_music.ogg` | Recording performer/producer and composition rights holders unknown | Supplied Ant_etkenmen.webm; source SHA-256 `273c47aa8073591f169c24d3f1a7729bb1d94c5dd798732cfe407995c319192b` | Recording and composition rights separately unresolved | Full mono conversion, 157.361646 seconds; output SHA-256 `2d244460573f17d592479cdb98202320058dbaebffc8b4f441d2eadbf85f5b65`. Neither anthem status nor free download establishes a recording license. See `sounds/music/notice.txt`. |

## Code, loaders and dependencies

The unchanged root LICENSE contains GNU Affero General Public License version 3.
Do not replace it with asset terms or remove existing code copyright headers.
No additional third-party source copyright headers were found in the Java
source scan; that scan is not proof that all code is independently authored.
No project git object database is available in these rescued source copies.

Minecraft, Forge/NeoForge, Superb Warfare, GeckoLib, Mixin, JEI, Curios,
Simple Bedrock Model, Refined Storage, Xaero and other integrations remain the
work of their respective authors. Compatibility does not transfer ownership.
Build declarations distinguish compile/runtime dependencies from included files;
the ordinary jar task does not shade these libraries. Their own distribution
notices remain applicable to their own jars. Do not describe the project's
AGPL as relicensing dependencies. If code is copied or dependencies are bundled
in a future release, add the exact applicable notices and license texts first.

## Verified dependency metadata (separate jars, not bundled)

| Dependency / local path | Authors from its metadata | License from its metadata | Notice handling |
| --- | --- | --- | --- |
| `libs/superbwarfare-*.jar` or workspace `Benodigde libraries/superbwarfare-*.jar`; versions 0.8.9 and 0.8.9.1 | Atsuishio, Roki27, Light_Quanta; 0.8.9.1 additionally says “and other contributors” | GNU General Public License v3.0 | Preserve their names and all applicable source/redistribution notices. This addon does not relicense or embed that jar. Exact version and checksum follow the dependency filename/build setup. |
| `libs/simplebedrockmodel-*.jar` or workspace `Benodigde libraries/simplebedrockmodel-*.jar`; Forge 2.5.1 / NeoForge 2.5.1.1 | TartaricAcid, MaydayMemory, MoePus, Hidomatn, xjqsh | LGPL-3.0 | Preserve the dependency's own license and notices when distributing it. Not embedded in this addon jar. |

Evidence for these entries is each dependency's own `META-INF/mods.toml` or
`META-INF/neoforge.mods.toml`, inspected locally. No upstream source URL was
invented where the inspected metadata did not provide one. Other compile-only
integrations are not bundled and retain their respective upstream licenses.

## Current original Geranium and FPV drone assets

The current Geranium aircraft and FPV drone files below are covered by
LICENSE-ASSETS.md as `original-visual`. Provenance: created by the project team,
confirmed by the project owner on 2026-11-22. This records an owner declaration,
not independent verification. Earlier external attribution for these exact
Geranium files was corrected; it is not applied to the Geranium component as a
whole. No third-party license is asserted for these declared original files.

| Exact resource path | Separately inspected role |
| --- | --- |
| `models/item/geranium_2.obj` | Item geometry; also used by the non-LP02-fit world renderer |
| `models/item/geranium_2.mtl` | Item material / atlas reference |
| `models/item/geranium_2.json` | Item model loader and display transforms |
| `textures/item/geranium_2.png` | Shared item/world/platform aircraft atlas |
| `textures/item/gui/geranium_2.png` | GUI aircraft image |
| `geo/lp02_geranium_reference.geo.json` | LP02-fit world aircraft and loaded-platform reference geometry |
| `animations/lp02_geranium_reference.animation.json` | Platform-reference aircraft animation |
| `geo/cubed_fpv_drone.geo.json` | Current FPV drone world geometry |
| `textures/entity/cubed_fpv_drone.png` | Current FPV drone world texture |
| `animations/fpv_drone.animation.json` | Current FPV drone animation |
| `models/item/cubed_fpv_drone.json` | FPV drone inventory item model |
| `textures/item/cubed_fpv_drone.png` | FPV drone inventory / hotbar / dropped-item image |

The FPV drone is distinct from FPV goggles. The separate notices for
`models/item/fpv_goggles.obj`, `models/item/fpv_goggles.mtl` and
`textures/item/fpv_goggles.png` remain in force as recorded above.
The Delivery Drone item asset notice is also unchanged.

Separate legacy files `geo/drone.geo.json` and `textures/entity/drone.png`
remain packaged. The checked current FPV renderer uses the `cubed_fpv_drone`
resources instead; no direct reference to these two legacy paths was found in
the checked Java source. Their precise author/license remains unresolved; they
are not automatically assigned to the current team declaration, nor attributed
to a guessed external maker. See LEGACY-VISUAL-01 in RIGHTS-REVIEW.md.
No separately identifiable old external Geranium aircraft asset was established
in the current resource tree by this path audit. The `lp02_geranium_reference`
filename describes a current renderer resource, not evidence of external authorship.
The LP-02 launcher structure (`geranium2_launch_platform_lp02` resources) is a
separate visual family; this aircraft declaration does not automatically clear it.
