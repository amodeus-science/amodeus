/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class RoboTaxiHandler {

    private final TreeMaintainer<RoboTaxi> allRoboTaxis;
    private final Set<RoboTaxi> unassignedRoboTaxis = new HashSet<>();
    private final double maxSpeed;

    public RoboTaxiHandler(Network network) {
        maxSpeed = maxSpeed(network);
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.allRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);
    }

    public void update(Collection<RoboTaxi> newAllRoboTaxis, Collection<RoboTaxi> unassignedRoboTaxis) {
        newAllRoboTaxis.forEach(rt -> allRoboTaxis.add(rt));
        unassignedRoboTaxis.stream().forEach(rt -> this.unassignedRoboTaxis.add(rt));
    }

    public void assign(RoboTaxi roboTaxi) {
        unassignedRoboTaxis.remove(roboTaxi);
    }

    public void clear() {
        unassignedRoboTaxis.clear();
        allRoboTaxis.clear();
    }

    public Set<RoboTaxi> getUnassignedRoboTaxis() {
        return unassignedRoboTaxis;
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    private Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        Coord coord = roboTaxi.getDivertableLocation().getCoord();
        return Tensors.vector(coord.getX(), coord.getY());
    }

    private static double maxSpeed(Network network) {
        return network.getLinks().values().stream() //
                .mapToDouble(Link::getFreespeed) //
                .max() //
                .getAsDouble();
    }

    public Collection<RoboTaxi> getRoboTaxisWithinFreeSpeedDisk(Coord coord, double maxTime) {
        double distance = maxTime * maxSpeed;
        return allRoboTaxis.disk(coord.getX(), coord.getY(), distance);
    }

}
