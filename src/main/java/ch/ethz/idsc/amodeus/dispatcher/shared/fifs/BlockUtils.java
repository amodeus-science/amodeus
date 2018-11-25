/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.Optional;
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

    /* package */ static Optional<Block> getBlockwithHighestBalanceAndAvailableRobotaxi(Set<Block> blocks) {
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
        return Optional.ofNullable(highestBalanceBlock);
    }

    /* package */ static Block getBlockWithHighestAbsolutBalance(Collection<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        Block highestAbsBalanceBlock = null;
        for (Block block : blocks) {
            if (highestAbsBalanceBlock == null) {
                highestAbsBalanceBlock = block;
            } else {
                if (Math.abs(highestAbsBalanceBlock.getBlockBalance()) < Math.abs(block.getBlockBalance())) {
                    highestAbsBalanceBlock = block;
                }
            }
        }
        return highestAbsBalanceBlock;
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
        Optional<Block> adjacentBlock = BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(block.getAdjacentBlocks());
        if (adjacentBlock.isPresent()) {
            return balance1HigherThanBalance2(adjacentBlock.get(), block);
        }
        return false;
    }

    /*package*/ static boolean balance1HigherThanBalance2(Block block1, Block block2) {
        return (block1.getBlockBalance() > block2.getBlockBalance() + 1);
    }
}
