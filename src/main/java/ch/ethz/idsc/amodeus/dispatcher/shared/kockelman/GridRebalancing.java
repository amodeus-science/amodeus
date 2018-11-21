/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class GridRebalancing {
    private final Blocks blocks;
    private final Network network;
    private LeastCostCalculatorDatabaseOneTime timeDb;

    /* package */ GridRebalancing(Network network, double gridDistance, int minNumberRobotaxis) {
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        Coord southWestCoord = new Coord(networkBounds[0], networkBounds[1]);
        Coord southEastCoord = new Coord(networkBounds[2], networkBounds[1]);

        Coord northWestCoord = new Coord(networkBounds[0], networkBounds[3]);
        Coord northEastCoord = new Coord(networkBounds[2], networkBounds[3]);

        double xDistance1 = NetworkUtils.getEuclideanDistance(southEastCoord, southWestCoord);
        double xDistance2 = NetworkUtils.getEuclideanDistance(northEastCoord, northWestCoord);
        double xDistance = 0.5 * (xDistance1 + xDistance2);
        double nX = xDistance / gridDistance;
        double xDistCoord = Math.abs(southWestCoord.getX() - northEastCoord.getX());
        double blockLengthX = xDistCoord / nX;

        double yDistance1 = NetworkUtils.getEuclideanDistance(southEastCoord, northEastCoord);
        double yDistance2 = NetworkUtils.getEuclideanDistance(southWestCoord, northWestCoord);
        double yDistance = 0.5 * (yDistance1 + yDistance2);
        double nY = yDistance / gridDistance;
        double yDistCoord = Math.abs(southWestCoord.getY() - northEastCoord.getY());
        double blockLengthY = yDistCoord / nY;

        this.blocks = new Blocks(network, blockLengthX, blockLengthY, minNumberRobotaxis);
        this.network = network;
    }

    /* package */ void setTimeCalculator(LeastCostCalculatorDatabaseOneTime timeDb) {
        this.timeDb = timeDb;
    }

    /* package */ RebalancingDirectives getRebalancingDirectives(double now, Set<RoboTaxi> allAvailableRobotaxisforRebalance, Set<AVRequest> allUnassignedAVRequests,
            Set<Link> allRequestLinksLastHour) {
        GlobalAssert.that(timeDb.isForNow(Quantity.of(now, SI.SECOND)));
        blocks.setAllRequestCoordsLastHour(allRequestLinksLastHour);
        blocks.setNewUnassignedRequests(allUnassignedAVRequests);
        blocks.setNewRoboTaxis(allAvailableRobotaxisforRebalance);

        return new RebalancingDirectives(blocks.getRebalancingDirectives(network, timeDb, now));

    }

}
