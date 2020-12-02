/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.config.Config;

import amodeus.amodeus.ArtificialScenarioCreator;

/* package */ class ArtificialSharedScenarioCreator extends ArtificialScenarioCreator {

    public final DvrpVehicle vehicle1;
    public final DvrpVehicle vehicle2;
    public final RoboTaxi roboTaxi1;
    public final RoboTaxi roboTaxi2;

    public ArtificialSharedScenarioCreator() {
        this(null);
    }

    public ArtificialSharedScenarioCreator(Config config) {

        LinkTimePair divertableLinkTime = new LinkTimePair(linkDepotOut, 0.0);

        Id<DvrpVehicle> idAv1 = Id.create("av1", DvrpVehicle.class);
        vehicle1 = new DvrpVehicleImpl(ImmutableDvrpVehicleSpecification.newBuilder() //
                .id(idAv1) //
                .serviceBeginTime(0.0) //
                .serviceEndTime(Double.POSITIVE_INFINITY) //
                .capacity(3) //
                .startLinkId(linkDepotOut.getId()) //
                .build(), linkDepotOut);
        roboTaxi1 = new RoboTaxi(vehicle1, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED, null);
        setFirstStayTask(vehicle1);

        Id<DvrpVehicle> idAv2 = Id.create("av2", DvrpVehicle.class);
        vehicle2 = new DvrpVehicleImpl(ImmutableDvrpVehicleSpecification.newBuilder() //
                .id(idAv2) //
                .serviceBeginTime(0.0) //
                .serviceEndTime(Double.POSITIVE_INFINITY) //
                .capacity(3) //
                .startLinkId(linkDepotOut.getId()) //
                .build(), linkDepotOut);
        roboTaxi2 = new RoboTaxi(vehicle2, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED, null);
        setFirstStayTask(vehicle2);
        System.out.println("ArtificialScenario Created");
    }

    private static void setFirstStayTask(DvrpVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();
        schedule.addTask(new DrtStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
        schedule.nextTask();
    }
}
