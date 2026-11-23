# [SBW] Drone Range Config

Addon mod for `Superb Warfare` on `Minecraft 1.20.1 Forge`.

## What this addon changes

- Adds a server config for drone control distance.
- Uses a default manual control range of `10,000` blocks; explicit server settings still take precedence.
- Optionally keeps chunks around the active drone loaded while it is being used.
- Does not modify or redistribute Superb Warfare itself.

## Relevant Superb Warfare source locations

- Monitor state and linked-drone tracking:
  - `src/main/kotlin/com/atsuishio/superbwarfare/item/misc/MonitorItem.kt`
- Drone control packets:
  - `src/main/kotlin/com/atsuishio/superbwarfare/network/message/send/VehicleMovementMessage.kt`
  - `src/main/kotlin/com/atsuishio/superbwarfare/network/message/send/MouseMoveMessage.kt`
  - `src/main/kotlin/com/atsuishio/superbwarfare/network/message/send/InteractMessage.kt`
  - `src/main/kotlin/com/atsuishio/superbwarfare/network/message/send/DroneFireMessage.kt`
- Linked drone lookup:
  - `src/main/kotlin/com/atsuishio/superbwarfare/tools/EntityFindUtil.kt`
- Distance warning HUD:
  - `src/main/kotlin/com/atsuishio/superbwarfare/client/overlay/DroneHudOverlay.kt`

## Range note

In this SBW source snapshot, there is no explicit hardcoded `100` range constant for drone control. The practical limit appears to come from linked monitor handling plus world/chunk availability, while the HUD warning uses simulation distance. This addon therefore adds an explicit configurable server-side range gate with the original default of `100`.

## Config location

Forge writes the config to:

- `config/sbwdroneconfig-common.toml`

## Build

Use Java 17:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

Built jar:

- `build/libs/sbw-drone-range-config-1.0.0.jar`


## Licensing and provenance

Code: GNU Affero General Public License version 3; see [LICENSE](LICENSE).
Project author: Team SmartStreamLabs.
Separate documented original visual assets: [LICENSE-ASSETS.md](LICENSE-ASSETS.md).
External material and attribution: [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
Unresolved rights and exact asset hashes: [RIGHTS-REVIEW.md](RIGHTS-REVIEW.md) and [ASSET-RIGHTS-INVENTORY.csv](ASSET-RIGHTS-INVENTORY.csv).
Asset terms do not restrict AGPL rights in code. These documents are included at the root of the built jar.


## Manual control range

New configurations default to 10,000 blocks. Existing custom range values are preserved; the obsolete 100-block default is migrated once as described below. The active server/world config is authoritative: `<world>/serverconfig/sbwdroneconfig-gameplay.toml`. Changing only a client-side or legacy copy does not override an already configured server/world. Version 1.0.13 upgrades the old 100-block setting once and writes the migrated world config to disk. HX-2 and submarine-specific limits still apply.


### Legacy 100-block range migration (1.0.13)

Worlds with the obsolete 100-block range and no `drone._manualRangeDefaultsVersion` marker are upgraded once to 10,000. Other values are preserved. The original config is backed up by the existing atomic migration writer. After the marker becomes 1, explicitly setting 100 remains supported and survives reload. The migration is restricted to the server gameplay spec, never client preferences.
