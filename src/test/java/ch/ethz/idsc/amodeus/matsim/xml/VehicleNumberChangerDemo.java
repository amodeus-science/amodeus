/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

enum VehicleNumberChangerDemo {
    ;
    public static void main(String[] args) throws Exception {
        File simFolder = new File("/media/datahaki/data/ethz/2018_official_SF");
        XmlNumberOfVehiclesChanger.of(simFolder, 500);
    }
}
