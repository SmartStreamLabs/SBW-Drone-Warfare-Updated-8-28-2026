# [SBW] Drone Range Config

Addon mod for `Superb Warfare` on `Minecraft 1.20.1 Forge`.

## What this addon changes

- Adds a server config for drone control distance.
- Keeps the original default range at `100` blocks.
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
