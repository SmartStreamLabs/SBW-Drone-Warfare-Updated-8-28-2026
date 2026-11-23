# SBW Drone Warfare 1.0.7-chunkfix.3 (NeoForge)

Deze source is de volledige bestaande mod, met de chunktrackingreparatie en regressietests.
De runtime-JAR bevat geen testautomatisering en bundelt Superb Warfare niet.

Minecraft: 1.21.1 / NeoForge 21.1.228 / Java 21.
Getest tegen Superb Warfare 0.8.9.1. Bestaande afhankelijkheden en versiebereiken zijn behouden.

Build: `./gradlew.bat build -Psbw0891Jar=C:/volledig/pad/naar/superbwarfare-0.8.9.1.jar`
Gebruik de passende Superb Warfare-JAR voor deze Minecraft/loader-versie.
NeoForge vereist ook SimpleBedrockModel 2.5.1.1, indien nodig met `-PsimpleBedrockModelJar=C:/pad/naar/de.jar`.
De overige runtime-afhankelijkheden staan in build.gradle. De bronnen onder src zijn de actieve implementatie.
Op dit systeem moesten TEMP en TMP voor Java naar een kort pad zonder 8.3-alias worden gezet; de oorspronkelijke tempmap veroorzaakte een Java-loopbackfout.

Installeer de runtime aan beide kanten van een verbinding. Gebruik één Drone Warfare-versie tegelijk.
Nieuwe optie: drone.enableDroneRemoteChunkTracking=true. Deze volgt de camera en heeft voorrang op de oude spelerankeroptie.
De oude ankerstand blijft beschikbaar als camera-chunktracking wordt uitgezet.
Voor extra autonome-flighttickets blijft enableDroneChunkLoading beschikbaar. Normale view/simulation distance wordt niet verhoogd.
Budgetten: droneExtraTicketsPerTick=4 (1..16), droneExtraPendingChunks=8 (1..64), droneExtraTicketLimit=512 (16..4096).
De bestaande ingestelde droneMaxRange, besturing, energie, inventaris-, fiber- en NBT-indelingen zijn niet gewijzigd.

Deze ronde leest eenvoudige monitorvelden zonder telkens een volledig CustomData-tag te kopiëren. De bestaande kopieergrens voor bewerkbare tags blijft behouden. De anti-dronenetcontrole vraagt ontbrekende buur-chunks niet meer synchroon op; zij worden bij een volgende tick opnieuw bekeken.
Zie TESTVERSLAG.md in de release voor de werkelijk uitgevoerde vluchten en beperkingen. Testhulp staat afzonderlijk bij het bewijs en hoort niet in de mods-map van spelers.
