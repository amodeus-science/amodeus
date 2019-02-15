package ch.ethz.idsc.amodeus.analysis.traces;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;

public class AVTraceListener implements ActivityStartEventHandler, ActivityEndEventHandler, LinkEnterEventHandler,
		PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {
	final private Network network;
	final private AVTraceWriter writer;

	final private Map<Id<Vehicle>, AVTraceItem> active = new HashMap<>();
	final private Map<Id<Vehicle>, AVTraceItem> finished = new HashMap<>();
	final private Map<Id<Vehicle>, Integer> occupancy = new HashMap<>();

	public AVTraceListener(Network network, AVTraceWriter writer) {
		this.network = network;
		this.writer = writer;
	}

	private boolean isRelevantId(String id) {
		return id.startsWith("av_") || id.startsWith("prav_");
	}

	private String getVehicleType(String id) {
		if (id.startsWith("av_")) {
			return "sav";
		} else if (id.startsWith("prav_")) {
			return "prav";
		} else {
			return "unknown";
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent endEvent) {
		AVTraceItem item = finished.remove(Id.createVehicleId(endEvent.getPersonId()));

		if (item != null) {
			item.followingTaskDuration = endEvent.getTime() - item.arrivalTime;
			writer.write(item);
		}

		if (isRelevantId(endEvent.getPersonId().toString())) {
			item = new AVTraceItem();
			item.vehicleId = Id.createVehicleId(endEvent.getPersonId());
			item.vehicleType = getVehicleType(endEvent.getPersonId().toString());
			item.departureTime = endEvent.getTime();
			item.originLink = network.getLinks().get(endEvent.getLinkId());
			active.put(item.vehicleId, item);
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent startEvent) {
		AVTraceItem item = active.remove(Id.createVehicleId(startEvent.getPersonId()));

		if (item != null) {
			item.followingTaskType = startEvent.getActType();
			item.destinationLink = network.getLinks().get(startEvent.getLinkId());
			item.arrivalTime = startEvent.getTime();
			finished.put(item.vehicleId, item);
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent enterEvent) {
		if (isRelevantId(enterEvent.getVehicleId().toString()) && !isRelevantId(enterEvent.getPersonId().toString())) {
			int currentOccupancy = occupancy.getOrDefault(enterEvent.getVehicleId(), 0);
			occupancy.put(enterEvent.getVehicleId(), currentOccupancy + 1);
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent leaveEvent) {
		if (isRelevantId(leaveEvent.getVehicleId().toString()) && !isRelevantId(leaveEvent.getPersonId().toString())) {
			int currentOccupancy = occupancy.getOrDefault(leaveEvent.getVehicleId(), 0);
			occupancy.put(leaveEvent.getVehicleId(), Math.max(0, currentOccupancy - 1));
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent linkEvent) {
		AVTraceItem item = active.get(linkEvent.getVehicleId());

		if (item != null) {
			item.distance += network.getLinks().get(linkEvent.getLinkId()).getLength();
			item.occupancy = occupancy.getOrDefault(linkEvent.getVehicleId(), 0);
		}
	}

	public void finish() {
		for (AVTraceItem item : finished.values()) {
			writer.write(item);
		}
	}
}
