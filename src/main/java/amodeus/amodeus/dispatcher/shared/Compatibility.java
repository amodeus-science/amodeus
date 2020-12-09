/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared;

import java.util.List;
import java.util.Objects;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.ScheduleManager;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/** Checks if a {@link SharedMenu} for a {@link RoboTaxi} is compatible with its
 * capacity, i.e., if at no point in time there are more passengers on board that
 * the vehicle can host. Usage:
 * 
 * Compatibility.of(courses).forCapacity(c) */
public class Compatibility {

    public static Compatibility of(List<Directive> directives) {
        return new Compatibility(Objects.requireNonNull(directives));
    }

    // ---
    private final List<Directive> directives;

    private Compatibility(List<Directive> directives) {
        this.directives = directives;
    }

    /** @param capacity maximum numbe of seats in the taxi.
     * @return true if the maximum number of seats is never violated with the menu {@link #courses}. */
    public boolean forCapacity(ScheduleManager manager, int capacity) {
        long onBoardPassengers = manager.getNumberOfOnBoardRequests();

        for (Directive directive : directives) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                if (stopDirective.isPickup()) {
                    onBoardPassengers++;
                } else {
                    onBoardPassengers--;
                }
            }

            if (onBoardPassengers > capacity) {
                return false;
            }
        }
        return true;
    }
}
