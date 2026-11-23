package nl.smartstreamlabs.sbwdroneconfig;
public final class Hg01Model extends software.bernie.geckolib.model.GeoModel<Hg01Entity>{
 private static net.minecraft.resources.ResourceLocation id(String p){return new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,p);}
 public net.minecraft.resources.ResourceLocation getModelResource(Hg01Entity e){return id("geo/hand_grenade_hg01.geo.json");}public net.minecraft.resources.ResourceLocation getTextureResource(Hg01Entity e){return id("textures/entity/hand_grenade_hg01.png");}public net.minecraft.resources.ResourceLocation getAnimationResource(Hg01Entity e){return id("animations/hand_grenade_hg01.animation.json");}
 @Override public void setCustomAnimations(Hg01Entity e,long instance,software.bernie.geckolib.core.animation.AnimationState<Hg01Entity> state){super.setCustomAnimations(e,instance,state);getBone("Safety_Lever").ifPresent(b->b.setRotZ(e.armed()?.35f:0));getBone("Ring_Assembly").ifPresent(b->b.setRotX(e.armed()?-.2f:0));}
}
