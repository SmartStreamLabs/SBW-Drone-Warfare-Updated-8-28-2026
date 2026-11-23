package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

final class GlideBombExternalSoundInstance extends DroneExternalSoundInstance {
    GlideBombExternalSoundInstance(VehicleEntity drone, DroneAudioProfile.Layer layer) {
        super(drone, DroneAudioProfile.GLIDE_BOMB, layer);
    }
}
