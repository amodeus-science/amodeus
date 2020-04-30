package ch.ethz.matsim.av.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;

import ch.ethz.matsim.av.analysis.FleetDistanceListener.OperatorData;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.generator.AVUtils;

public class FleetDistanceListener
		implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkEnterEventHandler {
	private final LinkFinder linkFinder;
	private final PassengerTracker passengers = new PassengerTracker();
	private final Map<Id<AVOperator>, OperatorData> data = new HashMap<>();

	static public class OperatorData {
		public double occupiedDistance_m;
		public double emptyDistance_m;
		public double passengerDistance_m;
	}

	public FleetDistanceListener(Collection<Id<AVOperator>> operatorIds, LinkFinder linkFinder) {
		this.linkFinder = linkFinder;

		for (Id<AVOperator> operatorId : operatorIds) {
			data.put(operatorId, new OperatorData());
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getVehicleId());

				if (data.containsKey(operatorId)) {
					passengers.addPassenger(event.getVehicleId(), event.getPersonId());
				}
			}
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getVehicleId());

				if (data.containsKey(operatorId)) {
					passengers.removePassenger(event.getVehicleId(), event.getPersonId());
				}
			}
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (event.getVehicleId().toString().startsWith("av:")) {
			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getVehicleId());
			OperatorData operator = data.get(operatorId);

			if (operator != null) {
				double linkLength = linkFinder.getDistance(event.getLinkId());
				int numberOfPassengers = passengers.getNumberOfPassengers(event.getVehicleId());

				if (numberOfPassengers > 0) {
					operator.occupiedDistance_m += linkLength;
				} else {
					operator.emptyDistance_m += linkLength;
				}

				operator.passengerDistance_m += linkLength * numberOfPassengers;
			}
		}
	}

	public Map<Id<AVOperator>, OperatorData> getData() {
		return Collections.unmodifiableMap(data);
	}

	public OperatorData getData(Id<AVOperator> operatorId) {
		return data.get(operatorId);
	}

	@Override
	public void reset(int iteration) {
		passengers.clear();

		Set<Id<AVOperator>> operatorIds = data.keySet();

		for (Id<AVOperator> operatorId : operatorIds) {
			data.put(operatorId, new OperatorData());
		}
	}
}
