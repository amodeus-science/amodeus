/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.util.EnumSet;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

/*package */ enum StaticHelper {
    ;

    /** for small scenarios, a filter is necessary to obain smooth waiting times plots */
    public static final int FILTERSIZE = 50;
    public static final boolean FILTER_ON = true;

    public static String[] descriptions() {
        return EnumSet.allOf(RoboTaxiStatus.class).stream() //
                .map(RoboTaxiStatus::description) //
                .toArray(String[]::new);
    }
}
