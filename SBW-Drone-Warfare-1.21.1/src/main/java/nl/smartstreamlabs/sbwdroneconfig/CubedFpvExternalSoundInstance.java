package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;

final class CubedFpvExternalSoundInstance extends DroneExternalSoundInstance {
    CubedFpvExternalSoundInstance(VehicleEntity drone, DroneAudioProfile.Layer layer) {
        super(drone, DroneAudioProfile.CUBED_FPV, layer);
    }
}
