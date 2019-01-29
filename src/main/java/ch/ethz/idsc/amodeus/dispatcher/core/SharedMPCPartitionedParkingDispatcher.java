package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityAmodeus;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.mpcsetup.MPCsetup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public abstract class SharedMPCPartitionedParkingDispatcher extends SharedMPCPartitionedDispatcher {
    protected final AVSpatialCapacityAmodeus avSpatialCapacityAmodeus; //

    protected SharedMPCPartitionedParkingDispatcher( //
            Config config, //
            AVDispatcherConfig avconfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            VirtualNetwork<Link> virtualNetwork, //
            MPCsetup mpcSetup, //
            MatsimAmodeusDatabase db, //
            AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
        super(config, avconfig, travelTime, router, eventsManager, virtualNetwork, mpcSetup, db);

        if (avSpatialCapacityAmodeus == null) {
            throw new IllegalStateException(
                    "The AVSpatialCapacityAmodeus is not set.");
        }

        this.avSpatialCapacityAmodeus = Objects.requireNonNull(avSpatialCapacityAmodeus);
        
    }
    
    
    /** {@link RoboTaxi} @param roboTaxi is redirected to the {@link Link} of the {@link SharedCourse}
     * the course can be moved to another position in the {@link SharedMenu} of the {@link} RoboTaxi */
    protected static void addSharedRoboTaxiParking(RoboTaxi roboTaxi, SharedCourse parkingCourse) {
        GlobalAssert.that(parkingCourse.getMealType().equals(SharedMealType.PARKING));
        roboTaxi.addParkingCourseToManu(parkingCourse);
    }

    
}