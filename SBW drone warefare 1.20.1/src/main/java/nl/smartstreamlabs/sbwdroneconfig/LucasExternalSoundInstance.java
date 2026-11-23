package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

final class LucasExternalSoundInstance extends DroneExternalSoundInstance {
    LucasExternalSoundInstance(VehicleEntity drone, DroneAudioProfile.Layer layer) {
        super(drone, DroneAudioProfile.LUCAS, layer);
    }
}
