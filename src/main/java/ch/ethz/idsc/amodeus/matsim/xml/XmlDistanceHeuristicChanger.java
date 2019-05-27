/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

public enum XmlDistanceHeuristicChanger {
    ;

    public static void of(File simFolder, String heuristicName) throws Exception {
        XmlCustomDataChanger.of(simFolder, "dispatcher", "distanceHeuristics", heuristicName);
    }
}
