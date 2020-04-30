package ch.ethz.matsim.av.analysis.run;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import ch.ethz.matsim.av.analysis.LinkFinder;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisListener;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisWriter;

public class RunVehicleAnalysis {
	static public void main(String[] args) throws ConfigurationException, IOException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("events-path", "network-path", "movements-output-path", "activities-output-path") //
				.build();

		String eventsPath = cmd.getOptionStrict("events-path");
		String networkPath = cmd.getOptionStrict("network-path");
		String movementsOutputPath = cmd.getOptionStrict("movements-output-path");
		String activitiesOutputPath = cmd.getOptionStrict("activities-output-path");

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkPath);

		LinkFinder linkFinder = new LinkFinder(network);
		VehicleAnalysisListener listener = new VehicleAnalysisListener(linkFinder);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(listener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);

		new VehicleAnalysisWriter(listener).writeMovements(new File(movementsOutputPath));
		new VehicleAnalysisWriter(listener).writeActivities(new File(activitiesOutputPath));
	}
}
