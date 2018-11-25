/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class BlockRebalancing {

    /** General Settings */
    private final int minNumberForRebalance;
    private final TravelTimeCalculator timeDb;

    /** in this Set All the operations are made. */
    private final Map<Integer, Block> blocks;
    /** this tree is only used as an lookup to quickly find the corresponding block */
    private final HashMap<Link, Block> linkBlockLookup = new HashMap<>();

    public BlockRebalancing(Network network, TravelTimeCalculator timeDb, int minNumberRobotaxisForRebalance, double historicalDataTime, double predictedTime,
            double gridDistance) {
        this.minNumberForRebalance = minNumberRobotaxisForRebalance;
        this.timeDb = timeDb;

        /** generate the Blocks based on the network and the grid distance. */
        blocks = BlocksGenerator.of(network, historicalDataTime, predictedTime, gridDistance);

        /** Fill the Lookup Map for the Link to Block */
        network.getLinks().values().forEach(l -> linkBlockLookup.put(l, getCorespondingBlock(l.getCoord())));
    }

    private Block getCorespondingBlock(Coord coord) {
        for (Block block : blocks.values())
            if (block.contains(coord))
                return block;
        GlobalAssert.that(false); // every link has to be part of a block otherwise the generation was not concise
        return null;
    }

    public RebalancingDirectives getRebalancingDirectives(double now, Set<Link> allRequestLinksLastHour, Set<AVRequest> allUnassignedAVRequests,
            Set<RoboTaxi> allAvailableRobotaxisforRebalance) {

        /** First we have to update all the blocks with the new values of requests and RoboTaxis */
        blocks.values().forEach(v -> v.clear());

        allAvailableRobotaxisforRebalance.forEach(rt -> blocks.get(linkBlockLookup.get(rt.getDivertableLocation()).getId()).addRoboTaxi(rt));
        allUnassignedAVRequests.forEach(req -> blocks.get(linkBlockLookup.get(req.getFromLink()).getId()).addUnassignedRequest());
        allRequestLinksLastHour.forEach(l -> blocks.get(linkBlockLookup.get(l).getId()).addRequestLastHour(l));

        /** Calculate the initial Block Balances for each block */
        blocks.values().forEach(v -> v.calculateInitialBlockBalance(allAvailableRobotaxisforRebalance.size(), allUnassignedAVRequests.size()));

        /** By using push and pull between the Blocks Lets determine which block sends how many robotaxis to which other block */
        calculateRebalancing();

        /** Calculate for each block which vehicles will move to which link based on the results of the calculated rebalancing numbers above */
        return getDirectivesFromBlocks(now);
    }

    private RebalancingDirectives getDirectivesFromBlocks(double now) {
        GlobalAssert.that(timeDb.isForNow(now));

        Map<RoboTaxi, Link> directives = new HashMap<>();
        for (Block block : blocks.values()) {
            for (Entry<RoboTaxi, Block> entry : block.executeRebalance(timeDb).entrySet()) {
                GlobalAssert.that(!directives.containsKey(entry.getKey()));
                directives.put(entry.getKey(), entry.getValue().getCenterLink());
            }
        }
        return new RebalancingDirectives(directives);

    }

    private void calculateRebalancing() {

        /** Store the Blocks in the Order of their Block Balance */
        TreeMultipleItems<Block> blockBalances = new TreeMultipleItems<>(this::getAbsOfBlockBalance);
        blocks.forEach((k, v) -> blockBalances.add(v));
        // Collection<Block> allBlocks = new HashSet<>(blocks.values());
        Set<Block> calculatedBlocks = new HashSet<>();

        /** Get the block with the largest absolut value of the block Balance */
        // Block block2 = BlockUtils.getBlockWithHighestAbsolutBalance(allBlocks);
        Block block = blockBalances.getLast().iterator().next();

        while (getAbsOfBlockBalance(block) > minNumberForRebalance) {
            // GlobalAssert.that(getAbsOfBlockBalance(block) == getAbsOfBlockBalance(block2));
            /** remove the block and its adjacent blocks from the tree, will be added with the updated balance afterwards */
            block.getAdjacentBlocks().forEach(b -> blockBalances.remove(b));
            blockBalances.remove(block);

            /** If the Block has enough free Robotaxis it pushes to other blocks (block balance > minNumberForRebalancing) */
            if (block.getBlockBalance() > minNumberForRebalance) {
                while (block.getBlockBalance() > minNumberForRebalance && BlockUtils.lowerBalancesPresentInNeighbourhood(block) && block.hasAvailableRobotaxisToRebalance()) {
                    block.pushRobotaxiTo(BlockUtils.getBlockwithLowestBalance(block.getAdjacentBlocks()));
                }
                /** If the Block has not enough free Robotaxis it pulls from other blocks (block balance < -minNumberForRebalancing) */
            } else if (block.getBlockBalance() < 0 - minNumberForRebalance) {
                Optional<Block> blockWithHighestBalance = BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(block.getAdjacentBlocks());
                while (block.getBlockBalance() < 0 - minNumberForRebalance && blockWithHighestBalance.isPresent()
                        && blockWithHighestBalance.get().hasAvailableRobotaxisToRebalance() && BlockUtils.balance1HigherThanBalance2(blockWithHighestBalance.get(), block)) {
                    blockWithHighestBalance.get().pushRobotaxiTo(block);
                    blockWithHighestBalance = BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(block.getAdjacentBlocks());
                }
            } else {
                GlobalAssert.that(false);
            }

            /** add the adjacent blocks back to the block balance tree with the updated balance. Btw The current block is not added Anymore as all possible rebalncings have
             * been carried out. It could well be that this block still has the highest balance but we have to move on to the next block. */
            calculatedBlocks.add(block);
            // allBlocks.remove(block);
            block.getAdjacentBlocks().stream().filter(b -> !calculatedBlocks.contains(b)).forEach(b -> blockBalances.add(b));
            /** update the current block */
            Set<Block> set = blockBalances.getLast();
            if (Objects.isNull(set)) {
                break;
            }
            block = set.iterator().next();
            // block2 = BlockUtils.getBlockWithHighestAbsolutBalance(allBlocks);

        }
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    private double getAbsOfBlockBalance(Block block) {
        return Math.abs(block.getBlockBalance());
    }

}
