/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.UniversalDispatcher;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.routing.NetworkTimeDistInterface;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Scalar;

/** This class is a generalized implementation of the "Algorithm 2: Insertion feasibility check" supplied in
 * the T-Share publication. The original reference is only for menus with 1 passenger in the taxi and the
 * addition of 1 other passenger. Here, all insertion permutations are explored, then, of all feasible
 * permutations, the permutation with the least additional distance is returned, null if none is found. The following
 * symbols represent the sequence of iterations which are checked:
 * 
 * o: existing schedule
 * +: pickup of new request
 * -: dropoff of new request
 * 
 * checking order:
 * +oooo-
 * +ooo-o
 * +oo-oo
 * ...
 * o+ooo-
 * o+oo-o
 * o+o-oo
 * o+-ooo
 * ... */
/* package */ class InsertionChecker {

    private final RoboTaxi roboTaxi;
    private final PassengerRequest request;
    private List<Directive> optimalMenu = null;
    private Scalar optimalLength;
    private Scalar originalLength;

    public InsertionChecker(CachedNetworkTimeDistance distance, NetworkTimeDistInterface travelTimeCached, //
            RoboTaxi roboTaxi, PassengerRequest request, Scalar pickupDelayMax, Scalar drpoffDelayMax, double timeNow) {
        this.roboTaxi = roboTaxi;
        this.request = request;

        /** get robotaxi menu */
        List<Directive> originalMenu = roboTaxi.getUnmodifiableViewOfCourses();
        int length = originalMenu.size();

        /** we should only be here if the {@link RoboTaxi} has a {@link PassengerRequest} on board */
        if (length < 1) {
            System.err.println("menu size of " + roboTaxi.getId().toString() + " is: " + length);
            System.err.println("this part of the method should only be reached to insert additional requests.");
            System.err.println("aborting.");
            throw new RuntimeException();
        }

        /** original length */
        originalLength = Length.of(roboTaxi.getDivertableLocation(), originalMenu, distance, timeNow);

        /** create new courses to add to existing menu */
        Directive pickupCourse = Directive.pickup(request);
        Directive drpoffCourse = Directive.dropoff(request);

        /** calculate length of each modification, two indices show how many
         * times the Course was moved forward from the end of the menu */
        NavigableMap<Scalar, List<Directive>> menuOptions = new TreeMap<>();
        for (int i = 0; i <= length; ++i) {
            for (int j = length; j >= i; j--) {
                /** creation of new menu */
                List<Directive> newMenu = new ArrayList<>();
                for (int k = 0; k <= length; ++k) {
                    if (i == k)
                        newMenu.add(pickupCourse);
                    if (j == k)
                        newMenu.add(drpoffCourse);
                    if (k < length)
                        newMenu.add(originalMenu.get(k));
                }

                /** check compatibility with {@link RoboTaxi} capacity for newMenu */
                boolean capacityCompatible = Compatibility.of(newMenu).forCapacity(roboTaxi.getScheduleManager(), roboTaxi.getCapacity());
                if (!capacityCompatible)
                    continue;

                /** compute expected arrival times and check compatibility with time windows */
                boolean timeCompatible = //
                        TimeWindowCheck.of(timeNow, newMenu, travelTimeCached, roboTaxi.getLastKnownLocation(), pickupDelayMax, drpoffDelayMax);
                if (!timeCompatible)
                    continue;

                /** the line below is computationally expensive and calculates the
                 * path length of the option. */
                menuOptions.put(Length.of(roboTaxi.getDivertableLocation(), newMenu, distance, timeNow), newMenu);
            }
        }

        /** save the optimal menu */
        if (Objects.nonNull(menuOptions.firstEntry())) {
            optimalMenu = menuOptions.firstEntry().getValue();
            optimalLength = menuOptions.firstEntry().getKey();
            /** if the routine ran correctly, the number of {@link SharedCourse}s should
             * be increased by exactly 2 */
            GlobalAssert.that(optimalMenu.size() == originalMenu.size() + 2);
            GlobalAssert.that(Compatibility.of(optimalMenu).forCapacity(roboTaxi.getScheduleManager(), roboTaxi.getCapacity()));
        }
    }

    /** @return {@link Scalar} additional distance when additional request is inserted in
     *         the optimal configuration, null if no feasible insertion exists */
    public Scalar getAddDistance() {
        if (Objects.nonNull(optimalMenu))
            return optimalLength.subtract(originalLength);
        return null;
    }

    /** Function add the request to the optimal menu with the {@link BiConsumer} @param addSharedPickup
     * supplied by the dispatcher, normally from {@link UniversalDispatcher} */
    public void executeBest(BiConsumer<RoboTaxi, PassengerRequest> addSharedPickup) {
        if (Objects.nonNull(optimalMenu)) {
            addSharedPickup.accept(roboTaxi, request);
            roboTaxi.updateMenu(optimalMenu);
        }
    }

}
