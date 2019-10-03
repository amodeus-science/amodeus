/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.time.format.DateTimeFormatter;

public enum TaxiTripConstants {
    ;

    public static final DateTimeFormatter ldtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter ldtFormatShort = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

}
