/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensors;

public enum ImportTaxiTrips {
    ;

    private static final Map<Integer, DateTimeFormatter> formatLookup = new HashMap<Integer, DateTimeFormatter>() {
        {
            put(16, TaxiTripConstants.ldtFormatShort);
            put(19, TaxiTripConstants.ldtFormat);
        }
    };

    /** Converts lines in CSV file to elements in list of taxi trips
     * 
     * @param tripsCSVFile
     * @return
     * @throws IOException */
    public static List<TaxiTrip> fromFile(File tripsCSVFile) throws IOException {
        List<TaxiTrip> trips;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tripsCSVFile))) {
            // skip header and parse
            trips = bufferedReader.lines().skip(1).filter(Objects::nonNull).map(ImportTaxiTrips::parseLine).collect(Collectors.toList());
        }
        return trips;
    }

    private static TaxiTrip parseLine(String line) {
        String[] splits = line.split(";");
        // TODO more elegant? unfortunately necessary as toString()
        // of LDT removes last :00 if full hour...
        String dateString = splits[6];
        LocalDateTime ldt = LocalDateTime.parse(dateString, formatLookup.get(dateString.length()));
        return TaxiTrip.of(//
                splits[0], //
                splits[1], //
                Tensors.fromString(splits[2]), //
                Tensors.fromString(splits[3]), //
                Scalars.fromString(splits[4]), //
                ldt, Scalars.fromString(splits[8]), //
                Scalars.fromString(splits[9])//
        );
    }
}
