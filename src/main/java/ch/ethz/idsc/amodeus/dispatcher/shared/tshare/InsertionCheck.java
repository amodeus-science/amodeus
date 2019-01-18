package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class InsertionCheck {

    private final RoboTaxi roboTaxi;
    private final AVRequest request;
    private List<SharedCourse> optimalMenu;
    private final double optimalLength;
    private final double originalLength;

    public InsertionCheck(NetworkDistanceFunction distance, //
            RoboTaxi roboTaxi, AVRequest request, double latestPickup, double latestArrval) {
        this.roboTaxi = roboTaxi;
        this.request = request;

        /** get robotaxi menu */
        List<SharedCourse> menu = roboTaxi.getUnmodifiableViewOfCourses();
        int num = menu.size();
        /** we should only be here if the {@link RoboTaxi} has a {@link AVRequest} on board */
        GlobalAssert.that(num >= 2);

        /** original length */
        
        System.err.println("length ok? ");
        originalLength = Length.of(roboTaxi, menu, distance);

        /** add new requests to end of menu */
        SharedCourse pickupCourse = SharedCourse.pickupCourse(request);
        SharedCourse drpoffCourse = SharedCourse.dropoffCourse(request);

        /** calculate length of each modification, two indices show how many
         * times the Course was moved forward from the end of the menu */

        System.err.println("now this could be difficult");
        
        NavigableMap<Double, List<SharedCourse>> menuOptions = new TreeMap<>();
        for (int pckInsrtIndex = 0; pckInsrtIndex < num + 1; ++pckInsrtIndex) {
            for (int drpInsrtIndex = 0; drpInsrtIndex < (num + 1) - pckInsrtIndex; drpInsrtIndex++) {
                List<SharedCourse> copy = new ArrayList<>();
                for (int k = 0; k < num; ++k) {
                    if (pckInsrtIndex == k)
                        copy.add(pickupCourse);
                    if (drpInsrtIndex == k) {
                        copy.add(drpoffCourse);
                    }
                    copy.add(menu.get(k));
                    menuOptions.put(Length.of(roboTaxi, copy, distance), copy);
                }
            }
        }

        /** save the optimal menu */
        optimalMenu = menuOptions.firstEntry().getValue();
        optimalLength = menuOptions.firstEntry().getKey();
    }

    /** @return null if the request cannot be reached before maxPickupDelay or
     *         the request cannot be dropped of before reaching maxDrpoffDelay. Otherwise
     *         returns the additional necessary distance to pickup the request. */
    public Double getAddDistance() {
        return optimalLength - originalLength;
    }

    public void insert(BiConsumer<RoboTaxi, AVRequest> addSharedPickup) {
        addSharedPickup.accept(roboTaxi, request);
        roboTaxi.updateMenu(optimalMenu);
    }

    public RoboTaxi getRoboTaxi() {
        return roboTaxi;
    }
}
