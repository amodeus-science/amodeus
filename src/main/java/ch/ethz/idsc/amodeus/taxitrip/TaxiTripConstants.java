package ch.ethz.idsc.amodeus.taxitrip;

import java.time.format.DateTimeFormatter;

public enum TaxiTripConstants {
    ;

    public static final DateTimeFormatter ldtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static final DateTimeFormatter ldtFormatShort = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

}
