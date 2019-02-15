package ch.ethz.idsc.amodeus.analysis.traces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AVTraceWriter {
	final private BufferedWriter writer;

	public AVTraceWriter(File path) {
		try {
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));

			writer.write(String.join(";",
					new String[] { "vehicle_id", "vehicle_type", "origin_x", "origin_y", "destination_x",
							"destination_y", "departure_time", "arrival_time", "distance", "occupancy",
							"following_task_type", "following_task_duration" })
					+ "\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(AVTraceItem item) {
		try {
			writer.write(String.join(";", new String[] { item.vehicleId.toString(), item.vehicleType,
					String.valueOf(item.originLink.getCoord().getX()),
					String.valueOf(item.originLink.getCoord().getY()),
					String.valueOf(item.destinationLink.getCoord().getX()),
					String.valueOf(item.destinationLink.getCoord().getY()), String.valueOf(item.departureTime),
					String.valueOf(item.arrivalTime), String.valueOf(item.distance), String.valueOf(item.occupancy),
					String.valueOf(item.followingTaskType), String.valueOf(item.followingTaskDuration) }) + "\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
