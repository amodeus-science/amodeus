package ch.ethz.idsc.amodeus.traveldata.iterative;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataItem;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public class TravelDataListener implements PersonDepartureEventHandler, PersonArrivalEventHandler, IterationEndsListener {
    final private Map<Id<Person>, TravelDataItem> ongoing = new HashMap<>();
    final private Collection<TravelDataItem> data = new LinkedList<>();

    private TravelData travelData;

    final private Network network;
    final private VirtualNetwork<Link> virtualNetwork;

    public TravelDataListener(VirtualNetwork<Link> virtualNetwork, Network network, TravelData defaultTravelData) {
        this.network = network;
        this.travelData = defaultTravelData;
        this.virtualNetwork = virtualNetwork;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals("av")) {
            TravelDataItem item = new TravelDataItem();
            item.time = event.getTime();
            item.startLink = network.getLinks().get(event.getLinkId());
            ongoing.put(event.getPersonId(), item);
        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        TravelDataItem item = ongoing.remove(event.getPersonId());

        if (item != null) {
            item.endLink = network.getLinks().get(event.getLinkId());

            if (virtualNetwork.hasVirtualNodeFor(item.startLink) && virtualNetwork.hasVirtualNodeFor(item.endLink)) {
                data.add(item);
            }
        }
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        travelData = new TravelData(virtualNetwork, network, data, travelData.getdt());
        ongoing.clear();
        data.clear();
    }

    public TravelData getTravelData() {
        return travelData;
    }
}