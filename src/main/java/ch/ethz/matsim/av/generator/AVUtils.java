package ch.ethz.matsim.av.generator;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import ch.ethz.matsim.av.data.AVOperator;

public class AVUtils {
    private AVUtils() {
    }

    public static Id<DvrpVehicle> createId(Id<AVOperator> operatorId, String suffix) {
        return Id.create(String.format("av:%s:%s", operatorId.toString(), suffix), DvrpVehicle.class);
    }

    public static Id<DvrpVehicle> createId(Id<AVOperator> operatorId, int suffix) {
        return createId(operatorId, String.valueOf(suffix));
    }

    public static Id<DvrpVehicle> createId(Id<AVOperator> operatorId, long suffix) {
        return createId(operatorId, String.valueOf(suffix));
    }

    public static Id<AVOperator> getOperatorId(String vehicleId) {
        if (!vehicleId.startsWith("av:")) {
            throw new IllegalStateException("Not a valid AV vehicle: " + vehicleId);
        }

        String[] segments = vehicleId.split(":");

        if (segments.length != 3) {
            throw new IllegalStateException("Not a valid AV vehicle: " + vehicleId);
        }

        return AVOperator.createId(segments[1]);
    }

    public static Id<AVOperator> getOperatorId(Id<?> vehicleId) {
        return getOperatorId(vehicleId.toString());
    }
}
