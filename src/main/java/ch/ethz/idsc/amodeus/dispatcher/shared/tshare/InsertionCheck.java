package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseListUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class InsertionCheck {

    private final RoboTaxi roboTaxi;
    private final AVRequest request;
    private List<SharedCourse> optimalMenu = null;
    private Scalar optimalLength;
    private Scalar originalLength;

    public InsertionCheck(CashedDistanceCalculator distance, //
            RoboTaxi roboTaxi, AVRequest request) {
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
        NavigableMap<Scalar, List<SharedCourse>> menuOptions = new TreeMap<>();
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
                }

                if (SharedCourseListUtils//
                        .checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(newMenu, roboTaxi.getCapacity())) {
                    /** the line below is computationally expensive and calculates the
                     * path length of the option. */
                    menuOptions.put(Length.of(roboTaxi, newMenu, distance), newMenu);
                }
            }
        }

        /** save the optimal menu */
        if (Objects.nonNull(menuOptions.firstEntry())) {
            optimalMenu = menuOptions.firstEntry().getValue();
            GlobalAssert.that(optimalMenu.size() == originalMenu.size() + 2);
            GlobalAssert.that(SharedCourseListUtils.checkMenuConsistency(optimalMenu, roboTaxi.getCapacity()));
            optimalLength = menuOptions.firstEntry().getKey();
        }
    }

    /** @return null if the request cannot be reached before maxPickupDelay or
     *         the request cannot be dropped of before reaching maxDrpoffDelay. Otherwise
     *         returns the additional necessary distance to pickup the request. */
    public Scalar getAddDistance() {
        if (Objects.nonNull(optimalMenu)) {
            return optimalLength.subtract(originalLength);
        } else {
            return null;
        }
    }

    public void insert(BiConsumer<RoboTaxi, AVRequest> addSharedPickup) {
        if (Objects.nonNull(optimalMenu)) {
            addSharedPickup.accept(roboTaxi, request);
            roboTaxi.updateMenu(optimalMenu);
        }
    }

    public RoboTaxi getRoboTaxi() {
        return roboTaxi;
    }
}
