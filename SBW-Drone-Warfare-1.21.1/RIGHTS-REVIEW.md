# Rights review — open evidence, not a license grant

Audit date: 2026-11-22. All files listed in ASSET-RIGHTS-INVENTORY.csv have
concrete resource paths, SHA-256 hashes, status and the relevant review reference.
This audit does not certify the whole mod as cleared for distribution.

1. **CODE-01:** LICENSE is the full GNU AGPL version 3 text in all four projects.
   Previous build metadata incorrectly said GNU GENERAL PUBLIC LICENSE. Updated
   metadata now names AGPL version 3 and links these documents. No project-level
   “or any later version” election was found; boilerplate examples inside the
   license are not treated as such an election. This audit makes no new election.
   Preserve the original file bytes, existing author Team SmartStreamLabs and all
   existing code notices. Verify earlier release source obligations separately;
   supplying this license alone does not provide corresponding source.
2. **HISTORY-01:** git log was attempted in the final Forge project and no .git
   database was present; the four rescued project roots contain no .git database.
   Deleted notices were recovered from the local SBW-Drone-QA-CREDITS-20261122
   pre-deletion backup, corroborated for goggles/delivery by OBJ/MTL headers.
   No invented git commits or historical ownership conclusions are made. Supply
   the original repository/remote or bundle to investigate actual git history.
3. **MODEL-01:** External-provenance questions remain limited to the FPV goggles
   and Delivery inventory mesh families at the exact paths in THIRD-PARTY-NOTICES.md.
   They do not apply to the current FPV drone or the declared original Geranium
   aircraft files. Owner confirmation OWNER-VISUAL-20261122 resolves the project
   administration for those named files; it is not independent verification.
4. **AUDIO-01:** Close explosion's individual Freesound page verifies CC0; original
   filename identifies the source. Confirm conversion lineage if replacing assets.
   Distant explosion needs its individual asset page/certificate and author.
5. **AUDIO-02:** Each of the four legacy MP3 inputs in explosion_audio/manifest.json
   needs a source URL, creator and applicable license evidence. The generic Pixabay
   statement is not independent proof. Unused but packaged assets still need review.
6. **MUSIC-01:** For Ant_etkenmen.webm / flag_music.ogg, identify the recording,
   performer, producer, source URL, recording license and composition/arrangement
   permission separately. No public-domain or national-anthem exemption assumed.
7. **OTHER-01:** Remaining visual/audio entries marked unresolved in the inventory
   need a source package or authoring record and ownership/license confirmation.
   This includes the three distance-war clips, monitor link sound, older engine and
   explosion sounds, artwork and visual families without explicit original-delivery
   notes. An owner's general assertion is recorded but not used as sole evidence
   for applying the separate asset license to every file.
8. **OWNER-01:** Team SmartStreamLabs is preserved as the project author, without
   inventing a personal/legal name or ownership transfer. Identify the respective
   rights holder and contact route when permissions are requested.

No gameplay, asset bytes, rendering rules or audio playback were changed in this
licensing update. Unresolved files remain present and explicitly excluded from
the original-asset license; documenting uncertainty is not permission to use them.

## OWNER-VISUAL-20261122 — specific owner confirmation

The project owner explicitly confirmed that the current Geranium aircraft model
and current FPV drone model were created by the project team. For administration,
the exact current world/item geometry, textures, GUI image and aircraft platform
reference resources enumerated in THIRD-PARTY-NOTICES.md are `original-visual`
under LICENSE-ASSETS.md. This is the owner's statement, not an independent
copyright investigation. It does not cover the FPV goggles, unrelated launcher
structure, audio or other files by association. No asset bytes were modified.

## LEGACY-VISUAL-01 — exact remaining legacy paths

- `assets/sbwdroneconfig/geo/drone.geo.json`
- `assets/sbwdroneconfig/textures/entity/drone.png`

These files exist separately from the current `cubed_fpv_drone` renderer paths.
No direct Java reference was found in the checked source. Their source/license
remains unknown; retain `unresolved` and obtain provenance or establish replacement
lineage before assigning ownership. Presence alone does not prove external authorship.
No additional old external Geranium file could be identified from the current
resource paths or their model headers. A generic OBJ exporter URL is not an
authorship or license notice. Original asset bytes and historical backups are
preserved; earlier blanket Geranium credits are not distributed as current notices.
