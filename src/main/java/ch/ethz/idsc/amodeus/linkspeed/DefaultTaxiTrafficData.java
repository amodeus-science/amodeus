/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.List;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.trafficmonitoring.TravelTimeData;
import org.matsim.core.trafficmonitoring.TravelTimeDataArrayFactory;

import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer.LinkSpeedDataSet;

@Singleton
public class DefaultTaxiTrafficData implements TaxiTrafficData {

    private TaxiTrafficDataContainer trafficData;
    private LinkSpeedDataContainer lsData = new LinkSpeedDataContainer();
    private final int timeBinSize;
    private final int numSlots;
    private final Network network;

    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData, int timeBinSize, Network network) {
        System.out.println("Loading LinkSpeedData into Simulation");
        if (lsData != null) {
            this.lsData = lsData;
        }
        this.timeBinSize = timeBinSize;
        this.numSlots = (86400 / timeBinSize); // TODO magic const.
        this.network = network;
        this.createTravelTimeData();
    }

    @Override
    public double getTravelTimeData(Link link, double now) {
        return this.trafficData.getLinkTravelTime(link, now);
    }

    @Override
    public void createTravelTimeData() {
        // DEBUG Message
        System.out.println("Creating TravelTimeDataArray from LinkSpeedData:");
        System.out.println("\tNumSlots:\t" + this.numSlots);
        System.out.println("\tTimeBinSize:\t" + this.timeBinSize);

        // Instantiate new TTDF to create new TTDA objects
        TravelTimeDataArrayFactory factory = new TravelTimeDataArrayFactory(this.network, this.numSlots);
        this.trafficData = new TaxiTrafficDataContainer(this.numSlots);

        for (Entry<Integer, LinkSpeedDataSet> entry : lsData.linkSet.entrySet()) {
            Id<Link> linkID = Id.createLinkId(entry.getKey());
            Link link = this.network.getLinks().get(linkID);
            double linkLength = link.getLength();
            TravelTimeData ttData = factory.createTravelTimeData(linkID);
            for (Entry<Integer, List<Double>> lsDataSet : entry.getValue().data.entrySet()) {
                int timeStamp = lsDataSet.getKey();
                for (double freeSpeed : lsDataSet.getValue()) {
                    ttData.addTravelTime(trafficData.getTimeSlot(timeStamp), (linkLength / freeSpeed));
                }
            }
            trafficData.addData(linkID, ttData);
        }
        System.out.println("LinkSpeedData loaded into TravelTimeDataArray [lsData size: " + lsData.linkSet.size() + " links]");
    }
}
