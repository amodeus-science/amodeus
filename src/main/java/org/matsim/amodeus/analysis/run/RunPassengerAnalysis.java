package org.matsim.amodeus.analysis.run;

import java.io.File;
import java.io.IOException;

import org.matsim.amodeus.analysis.LinkFinder;
import org.matsim.amodeus.analysis.passengers.PassengerAnalysisListener;
import org.matsim.amodeus.analysis.passengers.PassengerAnalysisWriter;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class RunPassengerAnalysis {
    static public void main(String[] args) throws ConfigurationException, IOException {
        CommandLine cmd = new CommandLine.Builder(args) //
                .requireOptions("events-path", "network-path", "output-path") //
                .build();

        String eventsPath = cmd.getOptionStrict("events-path");
        String networkPath = cmd.getOptionStrict("network-path");
        String outputPath = cmd.getOptionStrict("output-path");

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkPath);

        LinkFinder linkFinder = new LinkFinder(network);
        PassengerAnalysisListener listener = new PassengerAnalysisListener(linkFinder);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(listener);
        new MatsimEventsReader(eventsManager).readFile(eventsPath);

        new PassengerAnalysisWriter(listener).writeRides(new File(outputPath));
    }
}
