/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.pt.PtConstants;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum TravelDataGet {
    ;

    public static TravelData readDefault(VirtualNetwork<Link> virtualNetwork) throws IOException {
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        ScenarioOptions scenarioOptions = ScenarioOptions.load(MultiFileTools.getWorkingDirectory());
        final File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), //
                scenarioOptions.getTravelDataName());
        System.out.println("loading travelData from " + travelDataFile.getAbsoluteFile());
        try {
            return TravelDataIO.read(travelDataFile, virtualNetwork);
        } catch (Exception e) {
            System.err.println("cannot load default " + travelDataFile);
            e.printStackTrace();
        }
        return null;
    }

    static public Collection<TravelDataItem> readFromPopulation(VirtualNetwork<Link> virtualNetwork, Population population, Network network) {
        StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);
        MainModeIdentifier mainModeIdentifier = new MainModeIdentifierImpl();

        Collection<TravelDataItem> items = new LinkedList<>();

        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();

            for (Trip trip : TripStructureUtils.getTrips(plan, stageActivityTypes)) {
                String mode = mainModeIdentifier.identifyMainMode(trip.getTripElements());

                if (mode.equals("av")) {
                    TravelDataItem item = new TravelDataItem();
                    item.time = trip.getOriginActivity().getEndTime();
                    item.startLink = network.getLinks().get(trip.getOriginActivity().getLinkId());
                    item.endLink = network.getLinks().get(trip.getDestinationActivity().getLinkId());

                    if (TravelDataItems.isContained(virtualNetwork, item)) {
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

}
