/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensors;

public class ImportTaxiTrips {

    public static Stream<TaxiTrip> fromFile(File tripsCSVFile) throws IOException {
        List<TaxiTrip> trips = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tripsCSVFile))) {
            // skip header and parse
            bufferedReader.lines().skip(1).forEach(line -> {
                if (Objects.nonNull(line)) {
                    String[] splits = line.split(";");

                    // TODO more elegant? unfortunately necessary as toString()
                    // of LDT removes last :00 if full hour...
                    LocalDateTime ldt = null;
                    String dateString = splits[6];
                    int digits = dateString.toCharArray().length;
                    if (digits == 16) {
                        ldt = LocalDateTime.parse(splits[6], TaxiTripConstants.ldtFormatShort);
                    }
                    if (digits == 19) {
                        ldt = LocalDateTime.parse(splits[6], TaxiTripConstants.ldtFormat);
                    }
                    Objects.requireNonNull(ldt);
                    TaxiTrip trip = TaxiTrip.of(//
                            Integer.parseInt(splits[0]), //
                            splits[1], //
                            Tensors.fromString(splits[2]), //
                            Tensors.fromString(splits[3]), //
                            Scalars.fromString(splits[4]), //
                            Scalars.fromString(splits[5]), //
                            ldt, Scalars.fromString(splits[8])//
                    );
                    trips.add(trip);
                }
            });
        }
        return trips.stream();
    }
}
