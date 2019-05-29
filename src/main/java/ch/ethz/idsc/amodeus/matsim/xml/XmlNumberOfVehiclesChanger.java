/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

public enum XmlNumberOfVehiclesChanger {
    ;

    public static void of(File simFolder, int vehicleNumber) throws Exception {
        XmlCustomDataChanger.of(simFolder, "generator", "numberOfVehicles", Integer.toString(vehicleNumber));
    }

}
