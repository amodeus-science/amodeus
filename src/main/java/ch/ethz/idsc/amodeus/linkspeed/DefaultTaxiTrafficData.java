/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Map.Entry;
import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.trafficmonitoring.TravelTimeData;
import org.matsim.core.trafficmonitoring.TravelTimeDataArrayFactory;

import com.google.inject.Singleton;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;

@Singleton
public class DefaultTaxiTrafficData implements TaxiTrafficData {

    private TaxiTrafficDataContainer trafficData;
    private LinkSpeedDataContainer lsData = new LinkSpeedDataContainer();
    private final int timeBinSize;
    private final int numSlots;
    private final Network network;
    private static final int DAYLENGTH = 86400; // TODO magic const.

    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData, int timeBinSize, Network network) {
        System.out.println("Loading LinkSpeedData into Simulation");
        GlobalAssert.that(Objects.nonNull(lsData));
        this.lsData = lsData;
        this.timeBinSize = timeBinSize;
        this.numSlots = (DAYLENGTH / timeBinSize);
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

        for (Entry<Integer, LinkSpeedTimeSeries> entry : lsData.getLinkSet().entrySet()) {
            Id<Link> linkID = Id.createLinkId(entry.getKey());
            Link link = network.getLinks().get(linkID);

            TravelTimeData ttData = factory.createTravelTimeData(linkID);

            LinkSpeedTimeSeries lsData = entry.getValue();
            for (Integer time : lsData.getRecordedTimes()) {

                Tensor speedRecordings = lsData.getSpeedsAt(time);
                double speedRecorded = speedRecordings.Get(0).number().doubleValue();
                GlobalAssert.that(speedRecorded > 0.0);
                double travelTime = link.getLength() / speedRecorded;
                ttData.setTravelTime(trafficData.getTimeSlot(time), travelTime);

            }

            trafficData.addData(linkID, ttData);
        }
        System.out.println("LinkSpeedData loaded into TravelTimeDataArray [lsData size: " + lsData.getLinkSet().size() + " links]");
    }
}
