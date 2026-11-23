package nl.smartstreamlabs.sbwdroneconfig.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class SbwDroneConfigMixinPlugin implements IMixinConfigPlugin {
    private static final String SBW_MONITOR_088_CLASS = "com.atsuishio.superbwarfare.item.Monitor";
    private static final String SBW_MONITOR_089_CLASS = "com.atsuishio.superbwarfare.item.misc.MonitorItem";
    private static final String SBW_DRONE_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity";
    private static final String SBW_ABSTRACT_DEPLOYER_ITEM_088_CLASS = "com.atsuishio.superbwarfare.item.VehicleDeployerBlockItem";
    private static final String SBW_ABSTRACT_DEPLOYER_ITEM_089_CLASS = "com.atsuishio.superbwarfare.item.misc.AbstractDeployerItem";
    private static final String SBW_DRONE_ITEM_088_CLASS = "com.atsuishio.superbwarfare.item.Drone";
    private static final String SBW_DRONE_ITEM_089_CLASS = "com.atsuishio.superbwarfare.item.misc.DroneItem";
    private static final String SBW_DRONE_HUD_CLASS = "com.atsuishio.superbwarfare.client.overlay.DroneHudOverlay";
    private static final String SBW_COMMON_OVERLAY_089_CLASS = "com.atsuishio.superbwarfare.client.overlay.CommonOverlay";
    private static final String SBW_DRONE_RENDERER_CLASS = "com.atsuishio.superbwarfare.client.renderer.entity.DroneRenderer";
    private static final String SBW_AUTO_AIMABLE_ENTITY_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.base.AutoAimableEntity";
    private static final String SBW_LASER_TOWER_ENTITY_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.LaserTowerEntity";
    private static final String MINECRAFT_CHUNK_MAP_TRACKED_ENTITY_CLASS = "net.minecraft.server.level.ChunkMap$TrackedEntity";
    private static final String XAERO_MINIMAP_RENDERER_CLASS = "xaero.common.minimap.render.MinimapRenderer";
    private static final String XAERO_WORLD_MAP_GUI_MAP_CLASS = "xaero.map.gui.GuiMap";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.endsWith(".ExplosionOverhaulBunkerMixin"))return classExists("com.vinlanx.explosionoverhaul.ExplosionOverhaul");
        // Legacy Sodium derivatives and current Sodium already cache the same uniform lookup.
        // Inspect its resource without loading renderer classes on a dedicated server.
        if(mixinClassName.endsWith(".MonitorShaderLocationsMixin"))return
            !classExists("me.jellysquid.mods.sodium.mixin.features.shader.uniform.ShaderProgramMixin")
            && !classExists("net.caffeinemc.mods.sodium.mixin.features.shader.uniform.ShaderInstanceMixin");

        // Sodium 0.8 supplies the actual camera to setupTerrain and replaces setupRender.
        // Both integrations are redundant there; only inspect resources, never load client classes.
        if(mixinClassName.endsWith(".DroneLevelRendererMixin"))return
            !classExists("net.caffeinemc.mods.sodium.mixin.core.render.world.LevelRendererMixin");

        if (mixinClassName.contains(".DwfRs2")) return classExists("com.refinedmods.refinedstorage.common.api.RefinedStorageApi");
        if (mixinClassName.endsWith(".MonitorItemMixin")) {
            return classExists(SBW_MONITOR_088_CLASS);
        }
        if (mixinClassName.endsWith(".MonitorItem089Mixin")) {
            return classExists(SBW_MONITOR_089_CLASS);
        }
        if (mixinClassName.endsWith(".DroneEnergyMixin")) {
            return classExists(SBW_DRONE_CLASS);
        }
        if (mixinClassName.endsWith(".DroneEntityCrashExplosionMixin")) {
            return classExists(SBW_DRONE_CLASS);
        }
        if (mixinClassName.endsWith(".AbstractDeployerItemOwnerMixin")) {
            return classExists(SBW_ABSTRACT_DEPLOYER_ITEM_088_CLASS);
        }
        if (mixinClassName.endsWith(".AbstractDeployerItem089OwnerMixin")) {
            return classExists(SBW_ABSTRACT_DEPLOYER_ITEM_089_CLASS);
        }
        if (mixinClassName.endsWith(".DroneItemOwnerMixin")) {
            return classExists(SBW_DRONE_ITEM_088_CLASS);
        }
        if (mixinClassName.endsWith(".DroneItem089OwnerMixin")) {
            return classExists(SBW_DRONE_ITEM_089_CLASS);
        }
        if (mixinClassName.endsWith(".DroneHudOverlayMixin")) {
            return classExists(SBW_DRONE_HUD_CLASS) && !classExists(SBW_COMMON_OVERLAY_089_CLASS);
        }
        if (mixinClassName.endsWith(".CommonOverlay089Mixin")) {
            return classExists(SBW_COMMON_OVERLAY_089_CLASS);
        }
        if (mixinClassName.endsWith(".DroneRendererMixin")) {
            return classExists(SBW_DRONE_RENDERER_CLASS);
        }
        if (mixinClassName.endsWith(".DroneEntityCullingMixin")) {
            return classExists(SBW_DRONE_CLASS);
        }
        if (mixinClassName.endsWith(".AutoAimableLaserTowerDroneMixin")) {
            return classExists(SBW_AUTO_AIMABLE_ENTITY_CLASS) || classExists(SBW_LASER_TOWER_ENTITY_CLASS);
        }
        if (mixinClassName.endsWith(".ChunkMapTrackedEntityMixin")) {
            return classExists(MINECRAFT_CHUNK_MAP_TRACKED_ENTITY_CLASS);
        }
        if (mixinClassName.endsWith(".XaeroMinimapRendererMixin")) {
            return classExists(XAERO_MINIMAP_RENDERER_CLASS);
        }
        if (mixinClassName.endsWith(".XaeroWorldMapGuiMapMixin")) {
            return classExists(XAERO_WORLD_MAP_GUI_MAP_CLASS);
        }

        if(mixinClassName.endsWith(".XaeroPilotRadarMixin"))return classExists("xaero.hud.minimap.radar.render.element.RadarElementReader");
        if(mixinClassName.endsWith(".XaeroPilotSingleRadarMixin"))return classExists("xaero.hud.minimap.radar.render.element.RadarRenderer");
        if(mixinClassName.endsWith(".XaeroPilotMinimapTrackerMixin"))return classExists("xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementReader");
        if(mixinClassName.endsWith(".XaeroPilotWorldTrackerMixin"))return classExists("xaero.map.radar.tracker.PlayerTrackerMapElementReader");
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private boolean classExists(String className) {
        String resourcePath = className.replace('.', '/') + ".class";
        return SbwDroneConfigMixinPlugin.class.getClassLoader().getResource(resourcePath) != null;
    }
}
