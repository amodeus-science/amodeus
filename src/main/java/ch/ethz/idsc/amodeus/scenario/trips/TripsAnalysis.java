/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ch.ethz.idsc.amodeus.scenario.readers.CsvReader;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.idsc.tensor.red.Norm;

public class TripsAnalysis {
    private final File tripsFile;

    private final Set<Integer> requests = new HashSet<>();
    private final Set<Integer> taxis = new HashSet<>();
    private final Tensor distances = Tensors.empty();
    private final Tensor waitingTimes = Tensors.empty();
    public final Tensor durations = Tensors.empty();

    public TripsAnalysis(File tripsFile) throws IOException {
        GlobalAssert.that(tripsFile.isFile());
        this.tripsFile = tripsFile;
        analyze();
    }

    private void analyze() throws IOException {
        new CsvReader(tripsFile, ",").rows().forEachOrdered(row -> {
            requests.add(Integer.valueOf(row.get("Id")));
            taxis.add(Integer.valueOf(row.get("TaxiId")));
            distances.append(RealScalar.of(Double.valueOf(row.get("Distance")) / 1000)); // m -> km
            String waitingTime = row.get("WaitTime");
            try {
                waitingTimes.append(RealScalar.of(Double.valueOf(waitingTime)));
            } catch (NumberFormatException e) {
                if (!waitingTime.equals("null"))
                    System.err.println("WARN unexpected value encountered: WaitTime = " + waitingTime);
            }
            durations.append(RealScalar.of(Double.valueOf(row.get("Duration")) / 60)); // sec -> min
        });
    }

    public File printSummary() throws IOException {
        File file = new File(tripsFile.getParentFile(), "tripAnalysis.properties");
        try (FileWriter writer = new FileWriter(file)) {
            Properties properties = new Properties();

            properties.setProperty("numberOfRequests", String.valueOf(requests.size()));
            properties.setProperty("fleetSize", String.valueOf(taxis.size()));
            if (distances.length() > 0) {
                properties.setProperty("totalDistance", Norm._1.of(distances).toString() + "[km]");
                properties.setProperty("meanDistance", Mean.of(distances).toString() + "[km]");
                properties.setProperty("medianDistance", Median.of(distances).toString() + "[km]");
            }
            if (waitingTimes.length() > 0) {
                properties.setProperty("meanWaitTime", Mean.of(waitingTimes).toString() + "[min]");
                properties.setProperty("medianWaitTime", Median.of(waitingTimes).toString() + "[min]");
            }
            if (durations.length() > 0) {
                properties.setProperty("totalDuration", Norm._1.of(durations).toString() + "[min]");
                properties.setProperty("meanDuration", Mean.of(durations).toString() + "[min]");
                properties.setProperty("medianDuration", Median.of(durations).toString() + "[min]");
            }

            properties.store(writer, "Analysis summary of " + tripsFile.getAbsolutePath());
        }
        return file;
    }

}
