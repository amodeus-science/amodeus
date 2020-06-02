/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.beam;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.network.NetworkUtils;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.dispatcher.core.SharedUniversalDispatcher;
import amodeus.amodeus.dispatcher.shared.Compatibility;
import amodeus.amodeus.dispatcher.shared.OnMenuRequests;
import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedCourseAccess;
import amodeus.amodeus.dispatcher.shared.SharedMealType;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.VectorAngle;

// TODO @ChengQi after checking with Jan, move to an application package and separate from core classes.
public class BeamExtensionForSharing {
    // TODO @ChengQi after checking with Jan, code/api style is bad: lastEmptyTaxis
    private Collection<RoboTaxi> lastEmptyTaxis = new HashSet<>();
    // TODO @ChengQi after checking with Jan, code/api style is bad: addedAvRequests
    private final Map<PassengerRequest, RoboTaxi> addedAvRequests = new HashMap<>();
    private Scalar phiMax;
    private double rMax;

    public BeamExtensionForSharing(double rMax, double phiMax) {
        this.phiMax = RealScalar.of(phiMax);
        this.rMax = rMax;
    }

    /** This is the fast way of assigning potential sharing possibilities. It
     * 1. finds potential Assignements
     * 2. assignes them directly to the robotaxis
     * 3. reorders the menu of the RoboTaxis
     * 
     * @return the newly added Requests */
    public Map<PassengerRequest, RoboTaxi> findAssignementAndExecute(Collection<RoboTaxi> roboTaxis, Collection<PassengerRequest> openRideSharingRequests,
            SharedUniversalDispatcher sud) {
        getSharingAssignements(roboTaxis, openRideSharingRequests);
        assignTo(sud);
        reorderMenus();
        return addedAvRequests;
    }

    /** This function finds potential assignments for ride sharing. It compares which RoboTaxis changed their status to Drive with customer
     * compared to the last call of this function. This are Taxis which did just pick up their first customer. For all these taxis it
     * is then checked if one or multiple of the given avRequests are within radius rMax and have a similar direction than the current one.
     * This is checked with the maximum angle between the two request.
     * 
     * @param allRoboTaxis the whole fleet of RoboTaxis
     * @param avRequests all the requests which are still not picked up and should be considered for ride sharing
     * @return */

    public Map<PassengerRequest, RoboTaxi> getSharingAssignements(Collection<RoboTaxi> allRoboTaxis, Collection<PassengerRequest> avRequests) {
        addedAvRequests.clear();
        Set<RoboTaxi> driveWithCustomerRoboTaxis = allRoboTaxis.stream().filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER)).collect(Collectors.toSet());

        for (RoboTaxi roboTaxi : driveWithCustomerRoboTaxis) {
            AtomicInteger numberAdded = new AtomicInteger(0); // TODO @ChengQi why atomic integer?
            GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
            if (lastEmptyTaxis.contains(roboTaxi))
                /** The RoboTaxi just picked up a customer! Lets see if we find close requests with similar direction */
                for (PassengerRequest avRequest : avRequests)
                    if (!addedAvRequests.containsKey(avRequest) && checkIfPossibleSharing(roboTaxi, avRequest, numberAdded)) {
                        addedAvRequests.put(avRequest, roboTaxi);
                        numberAdded.incrementAndGet();
                    }
        }
        lastEmptyTaxis = allRoboTaxis.stream().filter(rt -> !rt.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER)).collect(Collectors.toSet());

        return addedAvRequests;
    }

    /** Assigns the internatly stored assignment from the last call of {@link #getSharingAssignements}
     * to the shared Universal dispatcher
     * 
     * @param sud */
    public void assignTo(SharedUniversalDispatcher sud) {
        addedAvRequests.forEach((avr, rt) -> sud.addSharedRoboTaxiPickup(rt, avr));
    }

    /** Once the reorders the menu of each robotaxi in the last assignment which was produced from the {@link #getSharingAssignements}
     * It creates first a fast pickup tour of all the requests and goies afterwards for a fast dropoff tour. */
    public void reorderMenus() {
        for (RoboTaxi roboTaxi : addedAvRequests.values()) {
            roboTaxi.updateMenu(Reorder.firstAllPickupsThenDropoffs(roboTaxi.getUnmodifiableViewOfCourses()));
            /** lets improve the menu a bit */
            Optional<SharedCourse> nextCourse = SharedCourseAccess.getStarter(roboTaxi);
            if (nextCourse.isPresent())
                if (nextCourse.get().getMealType().equals(SharedMealType.PICKUP)) {
                    roboTaxi.updateMenu(FastPickupTour.fastPickupTour(roboTaxi.getUnmodifiableViewOfCourses(), roboTaxi.getDivertableLocation().getCoord()));
                    roboTaxi.updateMenu(FastDropoffTour.fastDropoffTour(roboTaxi.getUnmodifiableViewOfCourses()));
                    GlobalAssert.that(Compatibility.of(roboTaxi.getUnmodifiableViewOfCourses()).forCapacity(roboTaxi.getCapacity()));
                }
        }
    }

    private boolean checkIfPossibleSharing(RoboTaxi roboTaxi, PassengerRequest request2, AtomicInteger numberAdded) {
        /** Check if the Robotaxi can Pickup a new customer on board or if it is allready full */
        if (!oneMorePickupPossible(roboTaxi, numberAdded))
            return false;

        /** Check if the distance of the Robotaxi to the customer is within a Radius rMax */
        if (NetworkUtils.getEuclideanDistance(roboTaxi.getDivertableLocation().getCoord(), request2.getFromLink().getCoord()) > rMax)
            return false;

        /** check if the direction of the Request is similar */
        return directionAngle(roboTaxi, request2).map(angle -> Scalars.lessThan(angle, phiMax)).orElse(false);
    }

    /** As we plan to make the order of pickups and dropoffs such that first all pickups then all dropoffs it makes sense that not dropoffs are planed than
     * capacity
     * 
     * @param roboTaxi
     * @param numberAdded
     * @return */
    private static boolean oneMorePickupPossible(RoboTaxi roboTaxi, AtomicInteger numberAdded) {
        return OnMenuRequests.getNumberMealTypes(roboTaxi.getUnmodifiableViewOfCourses(), SharedMealType.DROPOFF)//
                + numberAdded.get() < roboTaxi.getCapacity();
    }

    private static Optional<Scalar> directionAngle(RoboTaxi roboTaxi, PassengerRequest request2) {
        return phiof(roboTaxi.getDivertableLocation().getCoord(), getDirectionOfTrip(roboTaxi), request2.getFromLink().getCoord(), request2.getToLink().getCoord());
    }

    private static Optional<Scalar> phiof(Coord po, Coord pd, Coord ro, Coord rd) {
        return VectorAngle.of( //
                Tensors.vector(po.getX(), po.getY()).subtract(Tensors.vector(pd.getX(), pd.getY())), //
                Tensors.vector(ro.getX(), ro.getY()).subtract(Tensors.vector(rd.getX(), rd.getY())));
    }

    private static Coord getDirectionOfTrip(RoboTaxi roboTaxi) {
        List<SharedCourse> menu = roboTaxi.getUnmodifiableViewOfCourses();
        return menu.get(menu.size() - 1).getAvRequest().getToLink().getCoord();
    }
}
