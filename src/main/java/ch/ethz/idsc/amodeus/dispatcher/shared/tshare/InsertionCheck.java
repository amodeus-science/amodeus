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
        List<SharedCourse> originalMenu = roboTaxi.getUnmodifiableViewOfCourses();
        int length = originalMenu.size();

        /** we should only be here if the {@link RoboTaxi} has a {@link AVRequest} on board */
        if (length < 1) {
            System.err.println("menu size of " + roboTaxi.getId().toString() + " is: " + length);
            System.err.println("this part of the method should only be reached to insert additional requests.");
            System.err.println("aborting.");
            GlobalAssert.that(false);
        }

        /** original length */
        originalLength = Length.of(roboTaxi, originalMenu, distance);

        /** add new requests to end of menu */
        SharedCourse pickupCourse = SharedCourse.pickupCourse(request);
        SharedCourse drpoffCourse = SharedCourse.dropoffCourse(request);

        /** calculate length of each modification, two indices show how many
         * times the Course was moved forward from the end of the menu */
        NavigableMap<Double, List<SharedCourse>> menuOptions = new TreeMap<>();
        for (int i = 0; i <= length; ++i) {
            for (int j = length; j >= i; j--) {
                List<SharedCourse> newMenu = new ArrayList<>();
                for (int k = 0; k <= length; ++k) {
                    if (i == k)
                        newMenu.add(pickupCourse);
                    if (j == k) {
                        newMenu.add(drpoffCourse);
                    }
                    if (k < length)
                        newMenu.add(originalMenu.get(k));
                    /** the line below is computationally expensive and calculates the
                     * path length of the option. */
                    menuOptions.put(Length.of(roboTaxi, newMenu, distance), newMenu);
                }
            }
        }

        /** save the optimal menu */
        optimalMenu = menuOptions.firstEntry().getValue();
        GlobalAssert.that(optimalMenu.size() == originalMenu.size() + 2);
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
