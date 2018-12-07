/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

/* package */ enum BlocksGenerator {
    ;
    /** Generates a Block grid based on the network and the grid distance.
     * it is equally spaced in each direction.
     * 
     * @param network
     * @param historicalDataTime
     * @param predictedTime
     * @param gridDistance
     * @return */
    static Map<Integer, Block> of(Network network, double historicalDataTime, double predictedTime, double gridDistance) {

        double[] xy = getXYLength(network, gridDistance);
        double blockLengthX = xy[0];
        double BlockLengthY = xy[1];
        Map<Integer, Block> blocks = new HashMap<>();
        Rect outerBoundsRect = BlockUtils.getOuterBoundsOf(network);

        int nX = BlockUtils.calcNumberBlocksInDirection(outerBoundsRect.minX, outerBoundsRect.maxX, blockLengthX);
        int nY = BlockUtils.calcNumberBlocksInDirection(outerBoundsRect.minY, outerBoundsRect.maxY, BlockLengthY);
        double xMinBlock = outerBoundsRect.centerX - nX / 2.0 * blockLengthX;
        double yMinBlock = outerBoundsRect.centerY - nY / 2.0 * BlockLengthY;

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
                blockBounds[i][j] = new Block(rect, network, id, historicalDataTime, predictedTime);
                id++;
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
        return blocks;
    }

    private static double[] getXYLength(Network network, double gridDistance) {
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
        double[] returnValues = { blockLengthX, blockLengthY };
        return returnValues;
    }
}
