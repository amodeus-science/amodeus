package amodeus.amodeus.net;

import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;

public enum VehicleContainerUtils {
    ;

    public static RoboTaxiStatus finalStatus(VehicleContainer vc) {
        return vc.statii[vc.statii.length - 1];
    }

    public static boolean isDriving(VehicleContainer vc) {
        return finalStatus(vc).isDriving();
    }
}
