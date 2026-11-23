package nl.smartstreamlabs.sbwdroneconfig;
/** Per-explosion policy; no entity, world mutation or global config change. */
public interface Lm01ExplosionPolicy {
 void lm01$denyTerrain();
 boolean lm01$terrainDenied();
}
