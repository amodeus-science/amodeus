/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

public class XmlDispatcherChanger {
    ;

    /** Changes dispatcher in "av.xml" file in @param simFolder to the value @param newDispatcher
     * 
     * @throws Exception */
    public static void of(File simFolder, String newValue) throws Exception {
        XmlCustomOperatorValueChanger.of(simFolder, "dispatcher", newValue);
    }
}
