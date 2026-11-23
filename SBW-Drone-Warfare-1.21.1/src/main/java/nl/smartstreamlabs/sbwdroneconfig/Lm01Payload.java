package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.item.ItemStack;

/** GAD-01 keeps its existing self-destruction option with either legacy SBW mines or LM-01 cargo. */
public final class Lm01Payload {
 public static float damage(ItemStack s){return s.is(AddonItems.LM01.get())?Lm01Config.DAMAGE.get().floatValue():(s.getItem()instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item?com.atsuishio.superbwarfare.config.server.ExplosionConfig.TM_62_EXPLOSION_DAMAGE:com.atsuishio.superbwarfare.config.server.ExplosionConfig.BLU_43_EXPLOSION_DAMAGE).get().floatValue();}
 public static float radius(ItemStack s){return s.is(AddonItems.LM01.get())?Lm01Config.RADIUS.get().floatValue():(s.getItem()instanceof com.atsuishio.superbwarfare.item.projectile.Tm62Item?com.atsuishio.superbwarfare.config.server.ExplosionConfig.TM_62_EXPLOSION_RADIUS:com.atsuishio.superbwarfare.config.server.ExplosionConfig.BLU_43_EXPLOSION_RADIUS).get().floatValue();}
 private Lm01Payload(){}
}
