package ch.ethz.matsim.av.data;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;

import com.google.common.collect.ImmutableMap;

public class AVData implements Fleet {
	private final Map<Id<DvrpVehicle>, AVVehicle> vehicles;

	public AVData(Map<Id<DvrpVehicle>, AVVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	@Override
	public ImmutableMap<Id<DvrpVehicle>, DvrpVehicle> getVehicles() {
		return ImmutableMap.copyOf(vehicles);
	}
}