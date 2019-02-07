/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

/** Bottom level class in SharedRoboTaxi functionality, a {@link SharedMenu} is
 * composed of {@link SharedCourse}s which internally have a {@link SharedMealType}s */
public enum SharedMealType {
    PICKUP, //
    DROPOFF, //
    REDIRECT, //
    WAIT,
    PARK,
    ;
}
