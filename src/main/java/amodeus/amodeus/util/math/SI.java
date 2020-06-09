/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.qty.Unit;

public enum SI {
    ;
    public static final Unit ONE = Unit.ONE;
    // ---
    public static final Unit METER = Unit.of("m");
    public static final Unit SECOND = Unit.of("s");
    // ---
    public static final Unit VELOCITY = Unit.of("m*s^-1");

}
