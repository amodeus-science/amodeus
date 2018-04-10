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

import ch.ethz.idsc.subare.util.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;

@Singleton
public class DefaultTaxiTrafficData implements TaxiTrafficData {

    private TaxiTrafficDataContainer trafficData;
    private LinkSpeedDataContainer lsData = new LinkSpeedDataContainer();
    private final int timeBinSize;
    private final int numSlots;
    private final Network network;
    private final int DAYLENGTH = 86400; // TODO magic const.

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

        for (Entry<Integer, LinkSpeedTimeSeries> entry : lsData.linkSet.entrySet()) {
            Id<Link> linkID = Id.createLinkId(entry.getKey());
            Link link = this.network.getLinks().get(linkID);
            double linkLength = link.getLength();
            TravelTimeData ttData = factory.createTravelTimeData(linkID);

            LinkSpeedTimeSeries lsData = entry.getValue();
            for (Integer time : lsData.getRecordedTimes()) {

                // IMPLEMENTATION DEBUG
                Tensor speedRecordings = lsData.getSpeedsAt(time);
                double travelTime = speedRecordings.Get(0).number().doubleValue();
                // double travelTime = 1000000 * link.getLength() / link.getFreespeed();
                ttData.setTravelTime(trafficData.getTimeSlot(time), travelTime);
                // System.out.println("haahahaaaaaaaa aha ha!");
                // ttData.addTravelTime(trafficData.getTimeSlot(time), travelTime);

                // IMPLEMENTATION BEFORE
                // Tensor speedRecordings = lsData.getSpeedsAt(time);
                //// System.out.println(speedRecordings);
                // speedRecordings.flatten(-1).forEach(t -> {
                //// System.out.println("t: " + t);
                // double travelTime = (linkLength / ((RealScalar) t).number().doubleValue());
                //// System.out.println("travelTime : " + travelTime);
                //// System.out.println("time: " + time);
                //// System.out.println("trafficData.getTimeSlot(time) " + trafficData.getTimeSlot(time));
                // ttData.addTravelTime(trafficData.getTimeSlot(time),travelTime );
                // });
            }

            trafficData.addData(linkID, ttData);
        }
        System.out.println("LinkSpeedData loaded into TravelTimeDataArray [lsData size: " + lsData.linkSet.size() + " links]");
    }
}
