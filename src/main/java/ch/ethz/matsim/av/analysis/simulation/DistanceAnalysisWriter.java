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

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import ch.ethz.matsim.av.analysis.FleetDistanceListener;
import ch.ethz.matsim.av.analysis.FleetDistanceListener.OperatorData;
import ch.ethz.matsim.av.data.AVOperator;

public class DistanceAnalysisWriter implements Closeable {
    private final Collection<Id<AVOperator>> operatorIds;

    private Map<Id<AVOperator>, BufferedWriter> writers;
    private Map<Id<AVOperator>, File> paths = new HashMap<>();

    public DistanceAnalysisWriter(OutputDirectoryHierarchy outputDirectory, Collection<Id<AVOperator>> operatorIds) {
        this.operatorIds = operatorIds;

        for (Id<AVOperator> operatorId : operatorIds) {
            paths.put(operatorId, new File(outputDirectory.getOutputFilename("distance_" + operatorId + ".csv")));
        }
    }

    public void write(FleetDistanceListener listener) throws IOException {
        if (writers == null) {
            writers = new HashMap<>();

            for (Id<AVOperator> operatorId : operatorIds) {
                @SuppressWarnings("resource")
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths.get(operatorId))));

                writer.write(String.join(";", new String[] { "occupied_distance", //
                        "empty_distance", //
                        "passenger_distance" //
                }) + "\n");
                writer.flush();

                writers.put(operatorId, writer);
            }
        }

        for (Id<AVOperator> operatorId : operatorIds) {
            BufferedWriter writer = writers.get(operatorId);
            OperatorData data = listener.getData(operatorId);

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
            for (Id<AVOperator> operatorId : operatorIds) {
                writers.get(operatorId).close();
            }
        }
    }
}
