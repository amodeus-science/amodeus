package ch.ethz.idsc.amodeus.analysis.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.matsim.core.utils.geometry.CoordUtils;

public class AVServiceWriter {
	final private BufferedWriter writer;

	public AVServiceWriter(File path) {
		try {
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));

			writer.write(String.join(";",
					new String[] { "person_id", "trip_index", "operator_id", "origin_x", "origin_y", "destination_x",
							"destination_y", "departure_time", "waiting_time", "in_vehicle_time", "crowfly_distance",
							"network_distance", "charged_distance" })
					+ "\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(AVServiceItem item) {
		try {
			writer.write(String.join(";",
					new String[] { item.personId.toString(), String.valueOf(item.tripIndex), item.operatorId.toString(),
							String.valueOf(item.originLink.getCoord().getX()),
							String.valueOf(item.originLink.getCoord().getY()),
							String.valueOf(item.destinationLink.getCoord().getX()),
							String.valueOf(item.destinationLink.getCoord().getY()), String.valueOf(item.departureTime),
							String.valueOf(item.waitingTime), String.valueOf(item.inVehicleTime),
							String.valueOf(CoordUtils.calcEuclideanDistance(item.originLink.getCoord(),
									item.destinationLink.getCoord())),
							String.valueOf(item.distance), String.valueOf(item.chargedDistance) })
					+ "\n");
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
