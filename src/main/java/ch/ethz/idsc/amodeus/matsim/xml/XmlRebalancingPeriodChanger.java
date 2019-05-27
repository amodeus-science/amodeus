/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

public enum XmlRebalancingPeriodChanger {
    ;

    public static void of(File simFolder, int rebalancing) throws Exception {
        XmlCustomIntDataChanger.of(simFolder, "dispatcher", "rebalancingPeriod", rebalancing);
    }

}
