/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

// placement of class not final
public enum GlobalAssert {
    ;
    /** throws an exception if valid == false
     * 
     * @param valid */
    // DO NOT MODIFY THIS FUNCTION BUT ADD ANOTHER FUNCTION IF CHANGE IS REQUIRED
    public static void that(boolean valid) {
        if (!valid)
            throw new RuntimeException();
    }
}
