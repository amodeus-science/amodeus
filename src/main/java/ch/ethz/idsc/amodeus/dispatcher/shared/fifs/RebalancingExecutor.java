/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ class RebalancingExecutor {
    private final Network network;
    private final TravelTimeCalculatorCached timeDb;
    
    /** in this Set All the operations are made. */
    private final Map<Integer, Block> blocks;
    /** this tree is only used as an lookup to quickly find the corresponding block */
    private final HashMap<Link, Block> linkBlockLookup = new HashMap<>();

    /** General Settings */
    private final int minNumberForRebalance;
    private int numberUnassignedRequests = 0;
    private int numberFreeRoboTaxis = 0;

    /* package */ RebalancingExecutor(Network network,TravelTimeCalculatorCached timeDb, int minNumberRobotaxisForRebalance, double historicalDataTime, double predictedTime, double gridDistance) {
        this.minNumberForRebalance = minNumberRobotaxisForRebalance;
        this.network = network;
        this.timeDb = timeDb;
        
        blocks = BlocksGenerator.of(network, historicalDataTime, predictedTime, gridDistance);

        /** Fill the Lookup Map for the Link to Block */
        for (Link link : network.getLinks().values()) {
            Block block = getCorespondingBlock(link.getCoord());
            linkBlockLookup.put(link, block);
        }

    }

    private Block getCorespondingBlock(Coord coord) {
        for (Block block : blocks.values()) {
            if (block.contains(coord)) {
                return block;
            }
        }
        GlobalAssert.that(false); // every link has to be part of a block otherwise the generation was not concise
        return null;
    }

    /* package */ void setNewRoboTaxis(Set<RoboTaxi> allAvailableRobotaxisforRebalance) {
        blocks.values().forEach(v -> v.removeAllRobotaxis());
        numberFreeRoboTaxis = allAvailableRobotaxisforRebalance.size();
        for (RoboTaxi roboTaxi : allAvailableRobotaxisforRebalance) {
            blocks.get(linkBlockLookup.get(roboTaxi.getDivertableLocation()).getId()).addRoboTaxi(roboTaxi);
        }
    }

    /* package */ void setNewUnassignedRequests(Set<AVRequest> allUnassignedAVRequests) {
        blocks.forEach((k, v) -> v.removeAllUnassignedRequests());
        numberUnassignedRequests = allUnassignedAVRequests.size();
        for (AVRequest avRequest : allUnassignedAVRequests) {
            blocks.get(linkBlockLookup.get(avRequest.getFromLink()).getId()).addUnassignedRequest();
        }
    }

    /* package */ void setAllRequestCoordsLastHour(Set<Link> allRequestCoordsLastHour) {
        blocks.forEach((k, v) -> v.removeAllRequestsLastHour());
        for (Link requestLinkLastHour : allRequestCoordsLastHour) {
            Block block = blocks.get(linkBlockLookup.get(requestLinkLastHour).getId());
            GlobalAssert.that(block.contains(requestLinkLastHour.getCoord()));
            block.addRequestLastHour();
        }
    }

    /* package */ RebalancingDirectives getRebalancingDirectives(double now, Set<Link> allRequestLinksLastHour, Set<AVRequest> allUnassignedAVRequests, Set<RoboTaxi> allAvailableRobotaxisforRebalance) {
        GlobalAssert.that(timeDb.isForNow(now));
        setAllRequestCoordsLastHour(allRequestLinksLastHour);
        setNewUnassignedRequests(allUnassignedAVRequests);
        setNewRoboTaxis(allAvailableRobotaxisforRebalance);
        calculateBlockBalances();
        calculateRebalancing();

        Map<RoboTaxi, Link> directives = new HashMap<>();
        Set<RoboTaxi> checkOfAllreadyAddedRoboTaxis = new HashSet<>();
        for (Block block : blocks.values()) {
            for (Entry<RoboTaxi, Block> entry : block.executeRebalance(network, timeDb, now).entrySet()) {
                directives.put(entry.getKey(), entry.getValue().getCenterLink());
                GlobalAssert.that(!checkOfAllreadyAddedRoboTaxis.contains(entry.getKey()));
                checkOfAllreadyAddedRoboTaxis.add(entry.getKey());
            }
        }
        return new RebalancingDirectives(directives);
    }

    private void calculateBlockBalances() {
        blocks.forEach((k, v) -> v.calculateBlockBalance(numberFreeRoboTaxis, numberUnassignedRequests));
    }

    private void calculateRebalancing() {
        TreeMultipleItems<Block> blockBalances = new TreeMultipleItems<>(this::getAbsOfBlockBalance);
        blocks.forEach((k, v) -> blockBalances.add(v));

        // Calculate the Rabalancing Needs for each block
        for (Block block : blockBalances.getTsInOrderOfValueDescending()) {
            if (block.getBlockBalance() > minNumberForRebalance) {

                while (block.getBlockBalance() > minNumberForRebalance && block.lowerBalancesPresentInNeighbourhood() && block.hasAvailableRobotaxisToRebalance()) {
                    block.pushRobotaxiTo(block.getAdjacentBlockWithLowestBalance());
                }
            } else if (block.getBlockBalance() < minNumberForRebalance) {
                Block blockWithHighestBalance = block.getAdjacentBlockWithHighestBalanceAndAvailableRobotaxi();
                while (block.getBlockBalance() < minNumberForRebalance && block.higherBalancesPresentInNeighbourhood()
                        && blockWithHighestBalance.hasAvailableRobotaxisToRebalance()) {
                    blockWithHighestBalance.pushRobotaxiTo(block);
                    blockWithHighestBalance = block.getAdjacentBlockWithHighestBalanceAndAvailableRobotaxi();
                }
            }
        }
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    private double getAbsOfBlockBalance(Block block) {
        return Math.abs(block.getBlockBalance());
    }

}
