package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.*;

/** Resolves the distribution ABI once; never hides invocation failures or discards payload data. */
final class SbwDroneRuntimeAccess {
    private static final Object[] NO_ARGS = new Object[0];
    private static final class Holder { static final Access API = resolve(); }
    private record Access(Method controller, Method item, Method setter, Field field, Method fireGetter, Method fireSetter, Field fireField) {}
    static void initialize() { Holder.API.toString(); }
    private static Access resolve() {
        try {
            Class<?> type = Class.forName("com.atsuishio.superbwarfare.entity.vehicle.DroneEntity");
            Method controller = type.getMethod("getController");
            if (!Entity.class.isAssignableFrom(controller.getReturnType())) throw new NoSuchMethodException("getController must return Entity or Player");
            Method item = null, setter = null; Field field = null;
            try {
                item = type.getMethod("getCurrentItem");
                setter = type.getMethod("setCurrentItem", ItemStack.class);
                if (item.getReturnType() != ItemStack.class || setter.getReturnType() != void.class) throw new IllegalStateException("Unsupported SBW payload descriptors");
            } catch (NoSuchMethodException legacy) {
                field = type.getField("currentItem");
                if (field.getType() != ItemStack.class || Modifier.isFinal(field.getModifiers())) throw new IllegalStateException("Unsupported SBW currentItem field");
                item = null; setter = null;
            }
            Method fireGetter = null, fireSetter = null; Field fireField = null;
            try { fireGetter = type.getMethod("getFire"); fireSetter = type.getMethod("setFire", boolean.class);
                if (fireGetter.getReturnType() != boolean.class || fireSetter.getReturnType() != void.class) throw new IllegalStateException("Unsupported SBW fire descriptors");
            } catch (NoSuchMethodException legacy) { fireField = type.getField("fire");
                if (fireField.getType() != boolean.class || Modifier.isFinal(fireField.getModifiers())) throw new IllegalStateException("Unsupported SBW fire field");
                fireGetter = null; fireSetter = null;
            }
            return new Access(controller, item, setter, field, fireGetter, fireSetter, fireField);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("[SBW Drone Warfare] Unsupported Superb Warfare distribution: requires the 0.8.9 Final or 0.8.9.1 controller/payload ABI. World loading is prevented.", e);
        }
    }
    static Entity controller(Entity drone) {
        return (Entity) invoke(Holder.API.controller, drone, NO_ARGS);
    }
    static boolean fire(Entity drone) {
        Access a = Holder.API;
        try { return a.fireGetter != null ? (Boolean) invoke(a.fireGetter, drone, NO_ARGS) : a.fireField.getBoolean(drone); }
        catch (IllegalAccessException e) { throw new IllegalStateException("SBW fire access failed", e); }
    }
    static void fire(Entity drone, boolean value) {
        Access a = Holder.API;
        try { if (a.fireSetter != null) invoke(a.fireSetter, drone, new Object[]{value}); else a.fireField.setBoolean(drone, value); }
        catch (IllegalAccessException e) { throw new IllegalStateException("SBW fire write failed", e); }
    }
    static ItemStack item(Entity drone) {
        Access a = Holder.API;
        try {
            Object value = a.item != null ? invoke(a.item, drone, NO_ARGS) : a.field.get(drone);
            if (!(value instanceof ItemStack stack)) throw new IllegalStateException("SBW returned a null/invalid currentItem");
            return stack;
        } catch (IllegalAccessException e) { throw new IllegalStateException("SBW payload access failed", e); }
    }
    static void item(Entity drone, ItemStack stack) {
        java.util.Objects.requireNonNull(stack, "payload"); Access a = Holder.API;
        try {
            if (a.setter != null) invoke(a.setter, drone, new Object[]{stack});
            else a.field.set(drone, stack);
        } catch (IllegalAccessException e) { throw new IllegalStateException("SBW payload write failed", e); }
    }
    private static Object invoke(Method method, Object receiver, Object[] args) {
        try { return method.invoke(receiver, args); }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException failure) throw failure;
            if (e.getCause() instanceof Error failure) throw failure;
            throw new IllegalStateException("SBW method failed: " + method, e.getCause());
        } catch (IllegalAccessException e) { throw new IllegalStateException("SBW method inaccessible: " + method, e); }
    }
}
