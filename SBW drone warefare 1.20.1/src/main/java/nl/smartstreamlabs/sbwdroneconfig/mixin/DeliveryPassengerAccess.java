package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;import com.google.common.collect.ImmutableList;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.gen.Accessor;
/** Access to vanilla cargo passengers; SBW operator-seat lists remain unchanged. */
@Mixin(Entity.class)public interface DeliveryPassengerAccess{@Accessor("passengers")void sbwdw$cargoPassengers(ImmutableList<Entity> passengers);}
