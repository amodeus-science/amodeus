package ch.ethz.matsim.av.analysis.simulation;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.controler.OutputDirectoryHierarchy;

import ch.ethz.matsim.av.analysis.FleetDistanceListener;
import ch.ethz.matsim.av.analysis.FleetDistanceListener.ModeData;

public class DistanceAnalysisWriter implements Closeable {
    private final Collection<String> modes;

    private Map<String, BufferedWriter> writers;
    private Map<String, File> paths = new HashMap<>();

    public DistanceAnalysisWriter(OutputDirectoryHierarchy outputDirectory, Collection<String> modes) {
        this.modes = modes;

        for (String mode : modes) {
            paths.put(mode, new File(outputDirectory.getOutputFilename("distance_" + mode + ".csv")));
        }
    }

    public void write(FleetDistanceListener listener) throws IOException {
        if (writers == null) {
            writers = new HashMap<>();

            for (String mode : modes) {
                @SuppressWarnings("resource")
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths.get(mode))));

                writer.write(String.join(";", new String[] { "occupied_distance", //
                        "empty_distance", //
                        "passenger_distance" //
                }) + "\n");
                writer.flush();

                writers.put(mode, writer);
            }
        }

        for (String mode : modes) {
            BufferedWriter writer = writers.get(mode);
            ModeData data = listener.getData(mode);

            writer.write(String.join(";", new String[] { String.valueOf(data.occupiedDistance_m), //
                    String.valueOf(data.emptyDistance_m), //
                    String.valueOf(data.passengerDistance_m) //
            }) + "\n");
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (writers != null) {
            for (String mode : modes) {
                writers.get(mode).close();
            }
        }
    }
}
