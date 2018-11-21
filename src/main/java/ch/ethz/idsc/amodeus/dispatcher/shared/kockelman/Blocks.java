/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ class Blocks {

    /** in this Set All the operations are made. */
    private final Map<Integer, Block> blocks = new HashMap<>();
    private int numberUnassignedRequests = 0;
    private int numberFreeRoboTaxis = 0;
    /** this trees are only used as an lookup to quickly find the corresponding block */
    private final HashMap<Link, Block> linkBlockLookup = new HashMap<>();

    /** General Settings */
    private final int minNumberForRebalance;
    private final Rect outerBoundsRect;

    /*package*/ Blocks(Network network, double blockLengthX, double BlockLengthY, int minNumberRobotaxisForRebalance) {
        this.minNumberForRebalance = minNumberRobotaxisForRebalance;
        outerBoundsRect = BlockUtils.getOuterBoundsOf(network);
        int nX = BlockUtils.calcNumberBlocksInDirection(outerBoundsRect.minX, outerBoundsRect.maxX, blockLengthX);
        int nY = BlockUtils.calcNumberBlocksInDirection(outerBoundsRect.minY, outerBoundsRect.maxY, BlockLengthY);
        double xMinBlock = outerBoundsRect.centerX - nX / 2.0 * blockLengthX;
        double yMinBlock = outerBoundsRect.centerY - nY / 2.0 * BlockLengthY;

        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());

        for (double d : networkBounds) {
            System.out.println("networkBounds: " + d);
        }

        double[] xLimits = new double[nX + 1];
        for (int i = 0; i < nX + 1; i++) {
            xLimits[i] = xMinBlock + i * blockLengthX;
        }

        double[] yLimits = new double[nY + 1];
        for (int j = 0; j < nY + 1; j++) {
            yLimits[j] = yMinBlock + j * BlockLengthY;
        }

        int id = 0;
        Block[][] blockBounds = new Block[nX][nY];
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                Rect rect = new Rect(xLimits[i], yLimits[j], xLimits[i + 1], yLimits[j + 1]);
                blockBounds[i][j] = new Block(rect, network, id);
                id++;
                // printRect(rect);
            }
        }

        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                Block newBlock = blockBounds[i][j];
                if (i != 0)
                    newBlock.addAdjacentBlock(blockBounds[i - 1][j]);
                if (j != 0)
                    newBlock.addAdjacentBlock(blockBounds[i][j - 1]);
                if (i != nX - 1)
                    newBlock.addAdjacentBlock(blockBounds[i + 1][j]);
                if (j != nY - 1)
                    newBlock.addAdjacentBlock(blockBounds[i][j + 1]);
                blocks.put(newBlock.getId(), newBlock);
            }
        }

        /** Fill the Lookup Map for the Link to Block */
        for (Link link : network.getLinks().values()) {
            Block block = getCorespondingBlock(link.getCoord());
            linkBlockLookup.put(link, block);
        }

        /** remove Blocks with No Link */
        // Set<Block> blocksWithLinks = linkBlockLookup.values().stream().collect(Collectors.toSet());
        // Set<Block> blocksToRemove = blocks.values().stream().filter(b-> !blocksWithLinks.contains(b)).collect(Collectors.toSet());
        // for (Block block : blocksToRemove) {
        // removeBlock(block);
        // }
        //
        // Set<Block> toRemoveAgainAsBlocksThereAreNoAdjactntBlocks = new HashSet<>();
        // for (Block block : blocks.values()) {
        // if (block.getAdjacentBlocks().isEmpty()) {
        // toRemoveAgainAsBlocksThereAreNoAdjactntBlocks.add(block);
        // }
        // }
        // toRemoveAgainAsBlocksThereAreNoAdjactntBlocks.forEach(block->removeBlock(block));
        //

    }

    // private void removeBlock(Block block) {
    // GlobalAssert.that(blocks.containsKey(block.getId()));
    // Set<Block> adjBlocks = block.getAdjacentBlocks();
    // adjBlocks.forEach(adjB-> blocks.get(adjB.getId()).removeAdjacentblock(block));
    // blocks.remove(block.getId());
    // }

    private Block getCorespondingBlock(Coord coord) {
        for (Block block : blocks.values()) {
            if (block.contains(coord)) {
                return block;
            }
        }
        if (outerBoundsRect.contains(coord.getX(), coord.getY())) {
            System.out.println("We do not cover the whole bounds with our blocks");
        }
        GlobalAssert.that(false);
        return null;
    }

    /*package*/ void setNewRoboTaxis(Set<RoboTaxi> allAvailableRobotaxisforRebalance) {
        blocks.forEach((k, v) -> v.removeAllRobotaxis());
        numberFreeRoboTaxis = allAvailableRobotaxisforRebalance.size();
        for (RoboTaxi roboTaxi : allAvailableRobotaxisforRebalance) {
            blocks.get(linkBlockLookup.get(roboTaxi.getDivertableLocation()).getId()).addRoboTaxi(roboTaxi);
        }
    }

    /*package*/ void setNewUnassignedRequests(Set<AVRequest> allUnassignedAVRequests) {
        blocks.forEach((k, v) -> v.removeAllUnassignedRequests());
        numberUnassignedRequests = allUnassignedAVRequests.size();
        for (AVRequest avRequest : allUnassignedAVRequests) {
            blocks.get(linkBlockLookup.get(avRequest.getFromLink()).getId()).addUnassignedRequest();
        }
    }

    /*package*/ void setAllRequestCoordsLastHour(Set<Link> allRequestCoordsLastHour) {
        blocks.forEach((k, v) -> v.removeAllRequestCoordsLastHour());
        for (Link requestLinkLastHour : allRequestCoordsLastHour) {
            blocks.get(linkBlockLookup.get(requestLinkLastHour).getId()).addRequestCoordLastHour(requestLinkLastHour.getCoord());
        }
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

    /*package*/ Map<RoboTaxi, Link> getRebalancingDirectives(Network network, LeastCostCalculatorDatabaseOneTime timeDb, double now) {
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
        return directives;
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Coord getBlockCenter(Block block) {
        return new Coord(block.getBounds().centerX, block.getBounds().centerY);
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ double getAbsOfBlockBalance(Block block) {
        return Math.abs(block.getBlockBalance());
    }

}
