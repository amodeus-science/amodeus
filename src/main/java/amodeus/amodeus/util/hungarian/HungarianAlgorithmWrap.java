/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.hungarian;

import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.Put;

public enum HungarianAlgorithmWrap {
    ;

    public static int[] matching(double[][] distancematrix) {
        return matching(distancematrix, StaticHelper.EPS_DEFAULT);
    }

    // since this method is private, eps is always equal to StaticHelper.EPS_DEFAULT
    private static int[] matching(double[][] distancematrix, double eps) {
        try {
            FastHungarianAlgorithm fastHungarianAlgorithm = new FastHungarianAlgorithm(distancematrix, eps);
            return fastHungarianAlgorithm.execute();
        } catch (Exception exception) {
            try {
                System.out.println("cost matrix of hungarian algorithm dumpted to user home folder");
                Put.of( //
                        HomeDirectory.file("hungarian_fail_costs.mathematica"), //
                        Tensors.matrixDouble(distancematrix));
            } catch (Exception e) {
                System.err.println("can't export cost matrix");
            }
            throw new RuntimeException(exception);
        }
    }

}
