# UF-01 — Flag Music

The user's exact supplied `Ant_etkenmen.webm` is now connected to the existing flag. The user explicitly replaced the previous requirement to find/confirm another anthem. No recording identification is required for playback.

The complete prepared mono Ogg Vorbis is bundled as `assets/sbwdroneconfig/sounds/music/flag_music.ogg`. The built-in streaming event `sbwdroneconfig:uf01_music` references it. Server data `data/sbwdroneconfig/uf01_music/track.json` enables it with its measured full duration and the neutral title **Flag Music**. The obsolete anthem descriptor and missing-recording notice have been removed.

Right-click the existing mast foot or a physical mast part to open the menu. Play, Pause/Resume, Stop, volume 0–100%, progress, Repeat (default off) and operator range controls use the existing UF-01 transport. Closing the menu keeps it playing. Personal Master and Jukebox/Note Blocks volume still apply. Default radius is 64 blocks, independent of volume; operator changes and public controls remain in `world/serverconfig/sbwdroneconfig-uf01-music.toml`.

Owner/team/operator checks, settings persistence, stopped-on-restart behavior, one voice per flag, shared server clock, native streaming/seeking, distance falloff, late entry and cleanup on removal/unload remain intact. No new entities or chunk tickets are introduced. Existing flag geometry, waving, dimensions, placement, recipes and IDs remain unchanged.

All clients and server need this update; network version 64 prevents older clients without the recording from joining this version. No manual audio installation, external player or internet request is required.

## Source record

UF-01 flag music — Flag Music
Source: user-provided Ant_etkenmen.webm, explicitly selected for this feature.
Original SHA-256: 273c47aa8073591f169c24d3f1a7729bb1d94c5dd798732cfe407995c319192b
Bundled mono Vorbis SHA-256: 2d244460573f17d592479cdb98202320058dbaebffc8b4f441d2eadbf85f5b65
Complete decoded duration: 157.361646 seconds.
Conversion: full source decoded/downmixed to mono Ogg Vorbis (quality 5). No cuts, extra echo, pitch changes or additional layers. Prepared OGG reused byte-for-byte.
This asset is neutrally titled Flag Music. No claim is made that it is a particular state anthem or that a recording license has been supplied. No identity/license confirmation gate is used.
