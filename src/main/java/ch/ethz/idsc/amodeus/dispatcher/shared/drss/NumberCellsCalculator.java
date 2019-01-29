package ch.ethz.idsc.amodeus.dispatcher.shared.drss;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

enum NumberCellsCalculator {
    ;
    static int[] of(Network network, double gridDistance) {
        double[] xy = getXYLength(network, gridDistance);
        double blockLengthX = xy[0];
        double BlockLengthY = xy[1];
        Rect outerBoundsRect = getOuterBoundsOf(network);

        int nX = calcNumberBlocksInDirection(outerBoundsRect.minX, outerBoundsRect.maxX, blockLengthX);
        int nY = calcNumberBlocksInDirection(outerBoundsRect.minY, outerBoundsRect.maxY, BlockLengthY);

        return new int[] { nX, nY };
    }

    private static int calcNumberBlocksInDirection(double min, double max, double blockLength) {
        return (int) Math.ceil((max - min) / blockLength);
    }

    private static Rect getOuterBoundsOf(Network network) {
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return new Rect(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
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
