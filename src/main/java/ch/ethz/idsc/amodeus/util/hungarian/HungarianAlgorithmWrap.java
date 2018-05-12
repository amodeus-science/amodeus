/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.hungarian;

import ch.ethz.idsc.amodeus.util.math.UserHome;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Put;

public enum HungarianAlgorithmWrap {
    ;

    public static int[] matching(double[][] distancematrix) {
        return matching(distancematrix, StaticHelper.EPS_DEFAULT);
    }

    public static int[] matching(double[][] distancematrix, double eps) {
        try {
            FastHungarianAlgorithm fastHungarianAlgorithm = new FastHungarianAlgorithm(distancematrix, eps);
            return fastHungarianAlgorithm.execute();
        } catch (Exception exception) {
            try {
                Put.of( //
                        UserHome.file("hungarian_fail_costs.mathematica"), //
                        Tensors.matrixDouble(distancematrix));
            } catch (Exception e) {
                System.err.println("can't export cost matrix");
            }
            throw new RuntimeException(exception);
        }
    }

}
