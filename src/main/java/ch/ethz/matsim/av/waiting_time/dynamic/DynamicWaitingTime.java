package ch.ethz.matsim.av.waiting_time.dynamic;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.waiting_time.WaitingTime;
import ch.ethz.matsim.av.waiting_time.WaitingTimeCollector;
import ch.ethz.matsim.av.waiting_time.link_attribute.LinkWaitingTimeData;

public class DynamicWaitingTime implements WaitingTimeCollector, WaitingTime {
    private final LinkGroupDefinition linkGroupDefinition;
    private final LinkWaitingTimeData linkWaitingTimeData;
    private final double defaultWaitingTime;

    private final double[][] cumulativeValues;
    private final int[][] observationCounts;
    private final double[][] estimates;
    private final double[][] defaultValues;

    private final double alpha;

    private final double startTime;
    private final double interval;
    private final int numberOfTimeBins;

    public DynamicWaitingTime(LinkGroupDefinition linkGroupDefinition, LinkWaitingTimeData linkWaitingTimeData, double defaultWaitingTime, double startTime, double endTime,
            double interval, double alpha) {
        this.linkGroupDefinition = linkGroupDefinition;
        this.linkWaitingTimeData = linkWaitingTimeData;
        this.defaultWaitingTime = defaultWaitingTime;

        this.startTime = startTime;
        this.interval = interval;
        this.numberOfTimeBins = 1 + (int) Math.floor((endTime - startTime) / interval);

        this.cumulativeValues = new double[linkGroupDefinition.getMaximumIndex() + 1][numberOfTimeBins];
        this.observationCounts = new int[linkGroupDefinition.getMaximumIndex() + 1][numberOfTimeBins];
        this.estimates = new double[linkGroupDefinition.getMaximumIndex() + 1][numberOfTimeBins];
        this.defaultValues = createDefaultValues(linkGroupDefinition.getMaximumIndex() + 1, numberOfTimeBins);

        for (int groupIndex = 0; groupIndex < linkGroupDefinition.getMaximumIndex() + 1; groupIndex++) {
            for (int timeIndex = 0; timeIndex < numberOfTimeBins; timeIndex++) {
                estimates[groupIndex][timeIndex] = defaultValues[groupIndex][timeIndex];
            }
        }

        this.alpha = alpha;
    }

    private double[][] createDefaultValues(int numberOfGroups, int numberOfTimeBins) {
        double[][] values = new double[numberOfGroups][numberOfTimeBins];

        for (int groupIndex = 0; groupIndex < numberOfGroups; groupIndex++) {
            for (int timeIndex = 0; timeIndex < numberOfTimeBins; timeIndex++) {
                Collection<Id<Link>> linkIds = linkGroupDefinition.getLinkIds(groupIndex);

                if (linkIds.size() > 0) {
                    double cumulativeWaitingTime = 0.0;

                    for (Id<Link> linkId : linkIds) {
                        cumulativeWaitingTime += linkWaitingTimeData.getWaitingTime(linkId, defaultWaitingTime);
                    }

                    values[groupIndex][timeIndex] = cumulativeWaitingTime / linkIds.size();
                } else {
                    values[groupIndex][timeIndex] = defaultWaitingTime;
                }
            }
        }

        return values;
    }

    private int getTimeIndex(double time) {
        return Math.min(Math.max((int) Math.floor((time - startTime) / interval), 0), numberOfTimeBins - 1);
    }

    @Override
    public void registerWaitingTime(double time, double waitingTime, Id<Link> linkId) {
        int groupIndex = linkGroupDefinition.getGroup(linkId);
        int timeIndex = getTimeIndex(time);

        if (groupIndex != -1) {
            cumulativeValues[groupIndex][timeIndex] += waitingTime;
            observationCounts[groupIndex][timeIndex] += 1;
        }
    }

    private double getWaitingTime(double time, Id<Link> linkId, double alternativeWaitingTime) {
        int groupIndex = linkGroupDefinition.getGroup(linkId);

        if (groupIndex == -1) {
            return alternativeWaitingTime;
        } else {
            return estimates[groupIndex][getTimeIndex(time)];
        }
    }

    @Override
    public void consolidate() {
        for (int groupIndex = 0; groupIndex < linkGroupDefinition.getMaximumIndex() + 1; groupIndex++) {
            for (int timeIndex = 0; timeIndex < numberOfTimeBins; timeIndex++) {
                double previousValue = estimates[groupIndex][timeIndex];
                double currentValue = defaultValues[groupIndex][timeIndex];

                if (observationCounts[groupIndex][timeIndex] > 0) {
                    currentValue = cumulativeValues[groupIndex][timeIndex] / observationCounts[groupIndex][timeIndex];
                }

                estimates[groupIndex][timeIndex] = (1.0 - alpha) * previousValue + alpha * currentValue;

                cumulativeValues[groupIndex][timeIndex] = 0.0;
                observationCounts[groupIndex][timeIndex] = 0;
            }
        }
    }

    @Override
    public double getWaitingTime(Facility facility, double time) {
        if (facility.getLinkId() == null) {
            throw new IllegalStateException("Cannot use LinkAttributeWaitingTime if RoutingModule does not provide Link ID");
        }

        double waitingTime = linkWaitingTimeData.getWaitingTime(facility.getLinkId(), defaultWaitingTime);
        return getWaitingTime(time, facility.getLinkId(), waitingTime);
    }
}
