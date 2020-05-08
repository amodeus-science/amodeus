package ch.ethz.matsim.av.analysis.passengers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PassengerAnalysisWriter {
    private final PassengerAnalysisListener listener;

    public PassengerAnalysisWriter(PassengerAnalysisListener listener) {
        this.listener = listener;
    }

    public void writeRides(File path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));

        writer.write(String.join(";", new String[] { //
                "person_id", //
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
                "waiting_time", //

                "distance" //
        }) + "\n");

        for (PassengerRideItem ride : listener.getRides()) {
            writer.write(String.join(";", new String[] { //
                    String.valueOf(ride.personId), //
                    ride.mode == null ? "NaN" : String.valueOf(ride.mode), //
                    ride.vehicleId == null ? "NaN" : String.valueOf(ride.vehicleId), //

                    ride.originLink == null ? "null" : String.valueOf(ride.originLink.getId()), //
                    ride.originLink == null ? "NaN" : String.valueOf(ride.originLink.getCoord().getX()), //
                    ride.originLink == null ? "NaN" : String.valueOf(ride.originLink.getCoord().getY()), //

                    ride.destinationLink == null ? "null" : String.valueOf(ride.destinationLink.getId()), //
                    ride.destinationLink == null ? "NaN" : String.valueOf(ride.destinationLink.getCoord().getX()), //
                    ride.destinationLink == null ? "NaN" : String.valueOf(ride.destinationLink.getCoord().getY()), //

                    String.valueOf(ride.departureTime), //
                    String.valueOf(ride.arrivalTime), //
                    String.valueOf(ride.waitingTime), //

                    String.valueOf(ride.distance) //
            }) + "\n");
        }

        writer.close();
    }
}
