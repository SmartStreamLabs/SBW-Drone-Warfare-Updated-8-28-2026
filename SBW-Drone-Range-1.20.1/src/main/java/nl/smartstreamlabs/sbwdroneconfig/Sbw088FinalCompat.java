package nl.smartstreamlabs.sbwdroneconfig;

public class Sbw088FinalCompat implements SbwDroneAccess {
    @Override
    public String adapterName() {
        return "Superb Warfare 0.8.8 final";
    }

    @Override
    public String supportedVersionPrefix() {
        return "0.8.8";
    }

    @Override
    public String[] monitorClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.Monitor"};
    }

    @Override
    public String[] droneItemClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.Drone"};
    }

    @Override
    public String[] deployerItemClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.VehicleDeployerBlockItem"};
    }
}
