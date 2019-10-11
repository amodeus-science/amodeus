/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleCapacityImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ enum StaticRoboTaxiCreator {
    ;

    private static final int seats = 100; // just a large number as we are not testing capacity with that

    private static final VehicleType vehicleType;

    static {
        vehicleType = new VehicleTypeImpl(Id.create("amodeusType", VehicleType.class));

        VehicleCapacity capacity = new VehicleCapacityImpl();
        capacity.setSeats(seats);
        vehicleType.setCapacity(capacity);
    }

    /* package */ static final double TASK_END = 10.0;
    private static final String STAYINGVEHICLEID = "stayingRoboTaxi";

    /** @param divertableLinkTime
     * @param vehicleLink if null the link from Divertable link is taken
     * @return */
    /* package */ static RoboTaxi createStayingRoboTaxi(Link divertableLink, Link vehicleLinkin) {
        Link vehicleLink = vehicleLinkin == null ? divertableLink : vehicleLinkin;
        RoboTaxi roboTaxi = createRoboTaxi(divertableLink, vehicleLink);
        setFirstTaskStay(roboTaxi, vehicleLink);
        return roboTaxi;
    }

    private static void setFirstTaskStay(RoboTaxi roboTaxi, Link link) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AVStayTask(0.0, Double.POSITIVE_INFINITY, link));
        schedule.nextTask();
    }

    /* package */ static RoboTaxi createPickUpRoboTaxi(Link pickup) {
        RoboTaxi roboTaxi = createRoboTaxi(pickup, pickup);
        setFirstPickupTask(roboTaxi);
        return roboTaxi;
    }

    private static void setFirstPickupTask(RoboTaxi roboTaxi) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AVPickupTask(0.0, TASK_END, roboTaxi.getDivertableLocation(), 0.0));
        schedule.addTask(new AVStayTask(TASK_END, Double.POSITIVE_INFINITY, roboTaxi.getDivertableLocation()));
        schedule.nextTask();
    }

    public static RoboTaxi createDropoffRoboTaxi(Link dropoff) {
        RoboTaxi roboTaxi = createRoboTaxi(dropoff, dropoff);
        setFirstDropoffTask(roboTaxi);
        return roboTaxi;
    }

    private static void setFirstDropoffTask(RoboTaxi roboTaxi) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AVDropoffTask(0.0, TASK_END, roboTaxi.getDivertableLocation()));
        schedule.addTask(new AVStayTask(TASK_END, Double.POSITIVE_INFINITY, roboTaxi.getDivertableLocation()));
        schedule.nextTask();
    }

    public static RoboTaxi createDriveRoboTaxi(VrpPathWithTravelData vrpPathWithTravelData) {
        Link currentLocation = vrpPathWithTravelData.getFromLink();
        RoboTaxi roboTaxi = createRoboTaxi(currentLocation, currentLocation);
        setFirstDriveTask(roboTaxi, vrpPathWithTravelData);
        return roboTaxi;
    }

    private static void setFirstDriveTask(RoboTaxi roboTaxi, VrpPathWithTravelData vrpPathWithTravelData) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AVDriveTask(vrpPathWithTravelData));
        schedule.addTask(new AVStayTask(vrpPathWithTravelData.getArrivalTime(), Double.POSITIVE_INFINITY, vrpPathWithTravelData.getToLink()));
        schedule.nextTask();
    }

    private static RoboTaxi createRoboTaxi(Link divertableLink, Link vehicleLink) {
        LinkTimePair divertableLinkTime = new LinkTimePair(divertableLink, 0.0);
        Id<DvrpVehicle> idAv2 = Id.create(STAYINGVEHICLEID, DvrpVehicle.class);
        AVVehicle vehicle = new AVVehicle(idAv2, vehicleLink, 0.0, Double.POSITIVE_INFINITY, vehicleType);
        return new RoboTaxi(vehicle, divertableLinkTime, divertableLinkTime.link, RoboTaxiUsageType.SHARED);
    }

    /* package */ static void updateRoboTaxiMenuTo(RoboTaxi roboTaxi, List<SharedCourse> courses) {
        cleanRTMenu(roboTaxi);
        Set<AVRequest> pickupRequests = new HashSet<>();
        for (SharedCourse sharedCourse : courses) {
            switch (sharedCourse.getMealType()) {
            case PICKUP:
                pickupRequests.add(sharedCourse.getAvRequest());
                break;
            case DROPOFF:
                addAvRequestInBegining(roboTaxi, sharedCourse.getAvRequest());
                if (!pickupRequests.contains(sharedCourse.getAvRequest())) {
                    Link origialLink = roboTaxi.getDivertableLocation();
                    roboTaxi.setDivertableLinkTime(new LinkTimePair(sharedCourse.getAvRequest().getFromLink(), 0.0));
                    roboTaxi.pickupNewCustomerOnBoard();
                    roboTaxi.setDivertableLinkTime(new LinkTimePair(origialLink, 0.0));

                }
                break;
            case REDIRECT:
                roboTaxi.addRedirectCourseToMenu(sharedCourse);
                break;
            default:
                GlobalAssert.that(false);
                break;
            }
        }
        roboTaxi.updateMenu(courses);
    }

    /* package */ static void addAvRequestInBegining(RoboTaxi roboTaxi, AVRequest avRequest) {
        List<SharedCourse> withoutAVRequest = new ArrayList<>(roboTaxi.getUnmodifiableViewOfCourses());
        roboTaxi.addAVRequestToMenu(avRequest);
        List<SharedCourse> newMenu = Arrays.asList(SharedCourse.pickupCourse(avRequest), SharedCourse.dropoffCourse(avRequest));
        newMenu.addAll(withoutAVRequest);
        roboTaxi.updateMenu(newMenu);
    }

    /* package */ static void cleanRTMenu(RoboTaxi roboTaxi) {
        Link originalDivertableLocLink = roboTaxi.getDivertableLocation();
        while (SharedCourseAccess.hasStarter(roboTaxi)) {
            SharedCourse sharedCourse = SharedCourseAccess.getStarter(roboTaxi).get();
            switch (sharedCourse.getMealType()) {
            case PICKUP:
                roboTaxi.setDivertableLinkTime(new LinkTimePair(sharedCourse.getLink(), 0.0));
                roboTaxi.pickupNewCustomerOnBoard();
                break;
            case DROPOFF:
                roboTaxi.setDivertableLinkTime(new LinkTimePair(sharedCourse.getLink(), 0.0));
                roboTaxi.dropOffCustomer();
                break;
            case REDIRECT:
                roboTaxi.setDivertableLinkTime(new LinkTimePair(sharedCourse.getLink(), 0.0));
                roboTaxi.finishRedirection();
                break;
            default:
                GlobalAssert.that(false);
                break;
            }
        }
        roboTaxi.setDivertableLinkTime(new LinkTimePair(originalDivertableLocLink, 0.0));

    }

}
