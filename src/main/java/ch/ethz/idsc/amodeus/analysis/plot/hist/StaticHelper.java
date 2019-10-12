/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot.hist;

/* package */ enum StaticHelper {
    ;

    public static String binName(double binSize, int i) {
        if (isInteger(binSize)) {
            int binSizeInt = (int) binSize;
            return "[" + i * binSizeInt + " , " + (i + 1) * binSizeInt + ")";
        }
        return "[" + i * binSize + " , " + (i + 1) * binSize + ")";
    }

    private static boolean isInteger(double d) {
        return d == Math.floor(d) && Double.isFinite(d);
    }

}
