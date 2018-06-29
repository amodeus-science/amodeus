/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.data.AVVehicle;

/** RoboTaxi is central classs to be used in all dispatchers. Dispatchers control
 * a fleet of RoboTaxis, each is uniquely associated to an AVVehicle object in
 * MATSim.
 * 
 * @author Claudio Ruch */
public class RoboTaxi extends AbstractRoboTaxi {

    RoboTaxi(AVVehicle avVehicle, LinkTimePair divertableLinkTime, Link driveDestination) {
        super(avVehicle, divertableLinkTime, driveDestination);
        GlobalAssert.that(getCapacity() == 1);
    }

}