# Drone Warfare 1.0.8-vfx.1

- Nieuwe originele animaties voor vuurbal, opbollende rook en restrook: 48 eigen frames, geen vanilla VFX-textures.
- Korte flits, ongeveer 1–2 seconden vuur en een geleidelijke overgang naar opstijgende rook.
- Zwaardere daadwerkelijke payloads geven meer rooklobben, groter rookvolume en een langere nasleep, tot ongeveer 30 seconden bij zware explosies.
- Een lege FPV, transportdrone of Bayraktar krijgt een bescheiden crash-effect; ingebouwde LUCAS-, Geranium- en glijbomladingen blijven zwaarder.
- Een gedeelde, gesorteerde renderbatch met zachte alpha-overgangen, wereldbelichting voor rook en helder vuur. Rook wordt na wolken weergegeven om rechthoekige uitsparingen te voorkomen.
- Afstandsdetail, deeltjesinstellingen, maximaal 12 effecten en 256 particles, plus een extra budget voor transparante overdraw. Bij drukte verdwijnt oude rook geleidelijk eerder.
- Servergestuurde effectberichten, zonder pakket per particle; correcte herkomst voor afgeworpen eigen payloads. Geen chunkforcing door VFX.
- Oude explosieparticles worden alleen voor de bedoelde addon-explosies vervangen. Terugslag, blokverwerking en losstaande explosies blijven behouden; geannuleerde explosies laten geen vervangingsmarker achter.
- Bestaande schade, explosieradii, blokvernietiging, brandstof en wapenbalans blijven behouden.
- De recente API-, chunk-, camera- en performancereparaties blijven behouden. Xaero-code is niet aangepast.
