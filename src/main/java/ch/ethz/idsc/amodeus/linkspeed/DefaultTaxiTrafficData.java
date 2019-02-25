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
/* package */ class DefaultTaxiTrafficData implements TaxiTrafficData {

    private final LinkSpeedDataContainer lsData;
    private final int timeBinSize;
    private final int numSlots;
    private final Network network;
    private final TaxiTrafficDataContainer trafficData;

    /** @param lsData non-null
     * @param timeBinSize
     * @param network non-null
     * @throws Exception if any of the input parameters is null */
    public DefaultTaxiTrafficData(LinkSpeedDataContainer lsData, int timeBinSize, Network network) {
        System.out.println("Loading LinkSpeedData into Simulation");
        this.lsData = Objects.requireNonNull(lsData);
        this.timeBinSize = timeBinSize;
        this.numSlots = StaticHelper.DAYLENGTH / timeBinSize;
        this.network = Objects.requireNonNull(network);
        trafficData = createTravelTimeData();
    }

    private TaxiTrafficDataContainer createTravelTimeData() {
        // DEBUG Message
        System.out.println("Creating TravelTimeDataArray from LinkSpeedData:");
        System.out.println("\tNumSlots:\t" + numSlots);
        System.out.println("\tTimeBinSize:\t" + timeBinSize);

        // Instantiate new TTDF to create new TTDA objects
        TravelTimeDataArrayFactory factory = new TravelTimeDataArrayFactory(network, numSlots);
        TaxiTrafficDataContainer trafficData = new TaxiTrafficDataContainer(numSlots);

        for (Entry<Integer, LinkSpeedTimeSeries> entry : lsData.getLinkSet().entrySet()) {
            Id<Link> linkID = Id.createLinkId(entry.getKey());
            Link link = network.getLinks().get(linkID);
            if (Objects.isNull(link)) {
                System.err.println("link with id " + linkID.toString() + " not found.");
                System.err.println("you are possibly using a linkSpeedData file which is not\n " + "made for your scenario");
                System.err.println("stopping execution.");
                GlobalAssert.that(false);
            }

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
        return trafficData;
    }

    @Override
    public double getTravelTimeData(Link link, double now) {
        return trafficData.getLinkTravelTime(link, now);
    }

}
