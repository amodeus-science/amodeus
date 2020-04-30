package ch.ethz.matsim.av.waiting_time.link_attribute;

import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.waiting_time.WaitingTime;

public class LinkAttributeWaitingTime implements WaitingTime {
	private final double defaultWaitingTime;
	private final LinkWaitingTimeData linkWaitingTimeData;

	public LinkAttributeWaitingTime(double defaultWaitingTime, LinkWaitingTimeData linkWaitingTimeData) {
		this.defaultWaitingTime = defaultWaitingTime;
		this.linkWaitingTimeData = linkWaitingTimeData;
	}

	@Override
	public double getWaitingTime(Facility facility, double time) {
		if (facility.getLinkId() == null) {
			throw new IllegalStateException(
					"Cannot use LinkAttributeWaitingTime if RoutingModule does not provide Link ID");
		}

		return linkWaitingTimeData.getWaitingTime(facility.getLinkId(), defaultWaitingTime);
	}
}
