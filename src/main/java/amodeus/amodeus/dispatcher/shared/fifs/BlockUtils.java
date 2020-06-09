/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

import amodeus.amodeus.util.math.GlobalAssert;

/* package */ enum BlockUtils {
    ;

    public static double calculateBlockBalance(int savTotal, int savBlock, int demandTotal, int demandBlock) {
        return savTotal * ((double) savBlock / (double) savTotal - (double) demandBlock / (double) demandTotal);
    }

    public static Rect getOuterBoundsOf(Network network) {
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return new Rect(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
    }

    public static int calcNumberBlocksInDirection(double min, double max, double blockLength) {
        return (int) Math.ceil((max - min) / blockLength);
    }

    public static Optional<Block> getBlockwithHighestBalanceAndAvailableRobotaxi(Set<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        return blocks.stream().filter(Block::hasAvailableRobotaxisToRebalance).max(Comparator.comparingDouble(block -> Math.abs(block.getBlockBalance())));
    }

    public static Block getBlockWithHighestAbsolutBalance(Collection<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        return blocks.stream().max(Comparator.comparingDouble(block -> Math.abs(block.getBlockBalance()))).get();
    }

    public static Block getBlockwithLowestBalance(Set<Block> blocks) {
        GlobalAssert.that(!blocks.isEmpty());
        return blocks.stream().min(Comparator.comparingDouble(block -> Math.abs(block.getBlockBalance()))).get();
    }

    public static boolean lowerBalancesPresentInNeighbourhood(Block block) {
        return BlockUtils.getBlockwithLowestBalance(block.getAdjacentBlocks()).getBlockBalance() < block.getBlockBalance() - 1;
    }

    public static boolean higherBalancesPresentInNeighbourhood(Block block) {
        Optional<Block> adjacentBlock = BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(block.getAdjacentBlocks());
        return adjacentBlock.map(ab -> balance1HigherThanBalance2(ab, block)).orElse(false);
    }

    public static boolean balance1HigherThanBalance2(Block block1, Block block2) {
        return block1.getBlockBalance() > block2.getBlockBalance() + 1;
    }
}
