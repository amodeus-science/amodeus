/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.util.LinkTimePair;

/* package */ enum StaticRoboTaxiCreator {
    ;

    private static final int seats = 100; // just a large number as we are not testing capacity with that

    /* package */ static final double TASK_END = 10.0;
    private static final String STAYINGVEHICLEID = "stayingRoboTaxi";

    /** @param divertableLink
     * @param vehicleLinkin if null the link from Divertable link is taken
     * @return */
    /* package */ static RoboTaxi createStayingRoboTaxi(Link divertableLink, Link vehicleLinkin) {
        Link vehicleLink = vehicleLinkin == null ? divertableLink : vehicleLinkin;
        RoboTaxi roboTaxi = createRoboTaxi(divertableLink, vehicleLink);
        setFirstTaskStay(roboTaxi, vehicleLink);
        return roboTaxi;
    }

    private static void setFirstTaskStay(RoboTaxi roboTaxi, Link link) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new DrtStayTask(0.0, Double.POSITIVE_INFINITY, link));
        schedule.nextTask();
    }

    /* package */ static RoboTaxi createPickUpRoboTaxi(Link pickup) {
        RoboTaxi roboTaxi = createRoboTaxi(pickup, pickup);
        setFirstPickupTask(roboTaxi);
        return roboTaxi;
    }

    private static void setFirstPickupTask(RoboTaxi roboTaxi) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AmodeusStopTask(0.0, TASK_END, roboTaxi.getDivertableLocation(), StopType.Pickup));
        schedule.addTask(new DrtStayTask(TASK_END, Double.POSITIVE_INFINITY, roboTaxi.getDivertableLocation()));
        schedule.nextTask();
    }

    public static RoboTaxi createDropoffRoboTaxi(Link dropoff) {
        RoboTaxi roboTaxi = createRoboTaxi(dropoff, dropoff);
        setFirstDropoffTask(roboTaxi);
        return roboTaxi;
    }

    private static void setFirstDropoffTask(RoboTaxi roboTaxi) {
        Schedule schedule = roboTaxi.getSchedule();
        schedule.addTask(new AmodeusStopTask(0.0, TASK_END, roboTaxi.getDivertableLocation(), StopType.Dropoff));
        schedule.addTask(new DrtStayTask(TASK_END, Double.POSITIVE_INFINITY, roboTaxi.getDivertableLocation()));
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
        schedule.addTask(new DrtDriveTask(vrpPathWithTravelData, DrtDriveTask.TYPE));
        schedule.addTask(new DrtStayTask(vrpPathWithTravelData.getArrivalTime(), Double.POSITIVE_INFINITY, vrpPathWithTravelData.getToLink()));
        schedule.nextTask();
    }

    private static RoboTaxi createRoboTaxi(Link divertableLink, Link vehicleLink) {
        LinkTimePair divertableLinkTime = new LinkTimePair(divertableLink, 0.0);
        Id<DvrpVehicle> idAv2 = Id.create(STAYINGVEHICLEID, DvrpVehicle.class);
        DvrpVehicle vehicle = new DvrpVehicleImpl(ImmutableDvrpVehicleSpecification.newBuilder() //
                .id(idAv2) //
                .serviceBeginTime(0.0) //
                .serviceEndTime(Double.POSITIVE_INFINITY) //
                .capacity(seats) //
                .startLinkId(vehicleLink.getId()) //
                .build(), vehicleLink);
        return new RoboTaxi(vehicle, divertableLinkTime, divertableLinkTime.link, RoboTaxiUsageType.SHARED, null);
    }

}
