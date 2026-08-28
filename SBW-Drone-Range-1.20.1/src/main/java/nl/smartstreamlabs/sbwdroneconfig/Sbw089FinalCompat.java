package nl.smartstreamlabs.sbwdroneconfig;

public final class Sbw089FinalCompat extends Sbw088FinalCompat {
    @Override
    public String adapterName() {
        return "Superb Warfare 0.8.9 final";
    }

    @Override
    public String supportedVersionPrefix() {
        return "0.8.9";
    }

    @Override
    public String[] monitorClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.misc.MonitorItem"};
    }

    @Override
    public String[] droneItemClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.misc.DroneItem"};
    }

    @Override
    public String[] deployerItemClassNames() {
        return new String[]{"com.atsuishio.superbwarfare.item.misc.AbstractDeployerItem"};
    }
}
