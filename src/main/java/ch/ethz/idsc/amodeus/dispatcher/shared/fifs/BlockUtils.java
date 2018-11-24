/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum BlockUtils {
    ;

    /* package */ static double calculateBlockBalance(int savTotal, int savBlock, int demandTotal, int demandBlock) {
        return savTotal * ((double) savBlock / (double) savTotal - (double) demandBlock / (double) demandTotal);
    }

    /* package */ static Rect getOuterBoundsOf(Network network) {
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return new Rect(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
    }

    /* package */ static int calcNumberBlocksInDirection(double min, double max, double blockLength) {
        return (int) Math.ceil((max - min) / blockLength);
    }

    /* package */ static Block getBlockwithHighestBalanceAndAvailableRobotaxi(Set<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        Block highestBalanceBlock = null;
        for (Block block : blocks) {
            if (block.hasAvailableRobotaxisToRebalance()) {
                if (highestBalanceBlock == null) {
                    highestBalanceBlock = block;
                } else {
                    if (highestBalanceBlock.getBlockBalance() < block.getBlockBalance()) {
                        highestBalanceBlock = block;
                    }
                }
            }
        }
        return highestBalanceBlock;
    }

    /* package */ static Block getBlockwithLowestBalance(Set<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        Block lowestBalanceBlock = null;
        for (Block block : blocks) {
            if (lowestBalanceBlock == null) {
                lowestBalanceBlock = block;
            } else {
                if (lowestBalanceBlock.getBlockBalance() > block.getBlockBalance()) {
                    lowestBalanceBlock = block;
                }
            }
        }
        return lowestBalanceBlock;
    }

    /* package */ static boolean lowerBalancesPresentInNeighbourhood(Block block) {
        return (BlockUtils.getBlockwithLowestBalance(block.getAdjacentBlocks()).getBlockBalance() < block.getBlockBalance() - 1);
    }

    /* package */ static boolean higherBalancesPresentInNeighbourhood(Block block) {
        Block adjacentBlock = BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(block.getAdjacentBlocks());
        if (Objects.isNull(adjacentBlock)) {
            return false;
        }
        return (adjacentBlock.getBlockBalance() > block.getBlockBalance() + 1);
    }

}
