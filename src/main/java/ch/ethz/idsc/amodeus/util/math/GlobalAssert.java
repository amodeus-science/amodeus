/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

public enum GlobalAssert {
    ;
    /** throws an exception if valid == false
     * 
     * @param valid */
    public static void that(boolean valid) {
        if (!valid)
            throw new RuntimeException();
    }
}
