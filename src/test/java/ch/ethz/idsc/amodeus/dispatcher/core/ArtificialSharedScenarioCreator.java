/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.ArtificialScenarioCreator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ class ArtificialSharedScenarioCreator extends ArtificialScenarioCreator {

    public final AVVehicle vehicle1;
    public final AVVehicle vehicle2;
    public final RoboTaxi roboTaxi1;
    public final RoboTaxi roboTaxi2;
    static final int seats = 3;

    public ArtificialSharedScenarioCreator() {
        this(null);
    }

    public ArtificialSharedScenarioCreator(Config config) {

        LinkTimePair divertableLinkTime = new LinkTimePair(linkDepotOut, 0.0);

        Id<DvrpVehicle> idAv1 = Id.create("av1", DvrpVehicle.class);
        vehicle1 = new AVVehicle(idAv1, linkDepotOut, seats, 0.0, Double.POSITIVE_INFINITY);
        roboTaxi1 = new RoboTaxi(vehicle1, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED);
        setFirstStayTask(vehicle1);

        Id<DvrpVehicle> idAv2 = Id.create("av2", DvrpVehicle.class);
        vehicle2 = new AVVehicle(idAv2, linkDepotOut, seats, 0.0, Double.POSITIVE_INFINITY);
        roboTaxi2 = new RoboTaxi(vehicle2, divertableLinkTime, linkDepotOut, RoboTaxiUsageType.SHARED);
        setFirstStayTask(vehicle2);
        System.out.println("ArtificialScenario Created");
    }

    private static void setFirstStayTask(AVVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();
        schedule.addTask(new AVStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
        schedule.nextTask();
    }
}
