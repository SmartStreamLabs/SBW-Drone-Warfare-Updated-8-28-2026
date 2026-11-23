package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

final class DeliveryDroneExternalSoundInstance extends DroneExternalSoundInstance {
    DeliveryDroneExternalSoundInstance(VehicleEntity drone, DroneAudioProfile.Layer layer) {
        super(drone, DroneAudioProfile.DELIVERY, layer);
    }
}
