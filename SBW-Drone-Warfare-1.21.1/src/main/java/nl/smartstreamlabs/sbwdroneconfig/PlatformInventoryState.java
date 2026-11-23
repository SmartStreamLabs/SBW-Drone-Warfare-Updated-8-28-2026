package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.inventory.ContainerData;
public interface PlatformInventoryState {
    ContainerData inventoryState();
    default boolean inventoryAvailable(){return inventoryState().get(0)==1;}
    default int droneEnergy(){return (inventoryState().get(1)&65535)|((inventoryState().get(2)&65535)<<16);}
    default int droneCapacity(){return (inventoryState().get(3)&65535)|((inventoryState().get(4)&65535)<<16);}
    default String inventoryHint(){return switch(inventoryState().get(0)){case 0->"Load a drone to edit its inventory.";case 2->"Drone inventory in use, or platform busy.";default->"Edit the loaded drone before launch.";};}
}
