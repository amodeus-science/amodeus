/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.idsc.tensor.Tensor;

public enum ExportTaxiTrips {
    ;

    /** Exports all {@link TaxiTrip}s in the {@link Stream} @param stream to the
     * {@link File} @param outFile in csv notation, one row per {@link TaxiTrip}.
     * Instead of commas, ; are used as separators to ensure loading works properly
     * with the included {@link Tensor}s.
     * 
     * @throws IOException */
    public static void toFile(Stream<TaxiTrip> stream, File outFile) throws Exception {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile))) {
            String headers = Arrays.stream(TaxiTrip.class.getFields()) //
                    .map(Field::getName) //
                    .collect(Collectors.joining(";"));
            bufferedWriter.write(headers);
            stream.forEachOrdered(trip -> {
                try {
                    bufferedWriter.newLine();
                    /** TODO @clruch similar to {@link TaxiTrip#toString()} despite delimiter, error, and null treatment */
                    AtomicBoolean exception = new AtomicBoolean(false);
                    String line = Arrays.stream(TaxiTrip.class.getFields()).map(field -> {
                        try {
                            Object obj = field.get(trip);
                            return Objects.nonNull(obj) ? obj.toString() : "null";
                        } catch (Exception e) {
                            e.printStackTrace();
                            exception.set(true);
                            return "ERROR";
                        }
                    }).collect(Collectors.joining(";"));
                    if (exception.get())
                        throw new Exception(line);

                    // line += trip.localId;
                    // line += ";" + trip.taxiId;
                    // line += ";" + trip.pickupLoc;
                    // line += ";" + trip.dropoffLoc;
                    // line += ";" + trip.distance;
                    // line += ";" + trip.waitTime;
                    // line += ";" + trip.pickupTimeDate;
                    // line += ";" + trip.dropoffTimeDate;
                    // line += ";" + trip.driveTime;
                    bufferedWriter.write(line);
                } catch (Exception e) {
                    System.err.println("Unable to export taxi trip: ");
                    e.printStackTrace();
                }
            });
        }
    }
}
