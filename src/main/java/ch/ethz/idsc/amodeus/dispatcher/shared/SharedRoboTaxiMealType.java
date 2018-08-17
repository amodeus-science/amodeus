/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

/** Bottom level class in SharedRoboTaxi functionality, a {@link SharedRoboTaxiMenu} is
 * composed of {@link SharedRoboTaxiCourse}s which internally have a {@link SharedRoboTaxiMealType}s */
public enum SharedRoboTaxiMealType {
    PICKUP, //
    DROPOFF, //
    REBALANCE, //
    ;
}
