/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

public enum XmlDispatchPeriodChanger {
    ;

    public static void of(File simFolder, int dispatch) throws Exception {
        XmlCustomIntDataChanger.of(simFolder, "dispatcher", "dispatchPeriod", dispatch);
    }
}
