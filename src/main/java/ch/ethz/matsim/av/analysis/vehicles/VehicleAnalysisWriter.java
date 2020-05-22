package ch.ethz.matsim.av.analysis.vehicles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class VehicleAnalysisWriter {
    private final VehicleAnalysisListener listener;

    public VehicleAnalysisWriter(VehicleAnalysisListener listener) {
        this.listener = listener;
    }

    public void writeMovements(File path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));

        writer.write(String.join(";", new String[] { //
                "operator_id", //
                "vehicle_id", //

                "origin_link_id", //
                "origin_x", //
                "origin_y", //

                "destination_link_id", //
                "destination_x", //
                "destination_y", //

                "departure_time", //
                "arrival_time", //

                "distance", //
                "number_of_passengers" //
        }) + "\n");

        for (VehicleMovementItem movement : listener.getMovements()) {
            writer.write(String.join(";", new String[] { //
                    String.valueOf(movement.mode), //
                    String.valueOf(movement.vehicleId), //

                    String.valueOf(movement.originLink.getId()), //
                    String.valueOf(movement.originLink.getCoord().getX()), //
                    String.valueOf(movement.originLink.getCoord().getY()), //

                    movement.destinationLink == null ? "null" : String.valueOf(movement.destinationLink.getId()), //
                    movement.destinationLink == null ? "NaN" : String.valueOf(movement.destinationLink.getCoord().getX()), //
                    movement.destinationLink == null ? "NaN" : String.valueOf(movement.destinationLink.getCoord().getY()), //

                    String.valueOf(movement.departureTime), //
                    String.valueOf(movement.arrivalTime), //

                    String.valueOf(movement.distance), //
                    String.valueOf(movement.numberOfPassengers) //
            }) + "\n");
        }

        writer.close();
    }

    public void writeActivities(File path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));

        writer.write(String.join(";", new String[] { //
                "operator_id", //
                "vehicle_id", //

                "link_id", //
                "x", //
                "y", //

                "start_time", //
                "end_time", //

                "type" //
        }) + "\n");

        for (VehicleActivityItem activity : listener.getActivities()) {
            writer.write(String.join(";", new String[] { //
                    String.valueOf(activity.mode), //
                    String.valueOf(activity.vehicleId), //

                    String.valueOf(activity.link.getId()), //
                    String.valueOf(activity.link.getCoord().getX()), //
                    String.valueOf(activity.link.getCoord().getY()), //

                    String.valueOf(activity.startTime), //
                    String.valueOf(activity.endTime), //

                    activity.type //
            }) + "\n");
        }

        writer.close();
    }
}
