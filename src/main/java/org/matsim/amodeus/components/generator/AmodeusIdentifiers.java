package org.matsim.amodeus.components.generator;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

public class AmodeusIdentifiers {
    private AmodeusIdentifiers() {
    }

    private static Id<DvrpVehicle> createVehicleId(String mode, String suffix) {
        return Id.create(String.format("amodeus:%s:%s", mode, suffix), DvrpVehicle.class);
    }

    public static Id<DvrpVehicle> createVehicleId(String mode, int suffix) {
        return createVehicleId(mode, String.valueOf(suffix));
    }

    public static Id<DvrpVehicle> createVehicleId(String mode, long suffix) {
        return createVehicleId(mode, String.valueOf(suffix));
    }

    public static String getMode(String vehicleId) {
        if (!vehicleId.startsWith("amodeus:")) {
            throw new IllegalStateException("Not a valid AMoDeus vehicle: " + vehicleId);
        }

        String[] segments = vehicleId.split(":");

        if (segments.length != 3) {
            throw new IllegalStateException("Not a valid AMoDeus vehicle: " + vehicleId);
        }

        return segments[1];
    }

    // TODO: Try to factor out these functions ...
    public static String getMode(Id<?> vehicleId) {
        return getMode(vehicleId.toString());
    }

    public static boolean isValid(Id<?> id) {
        return isValid(id.toString());
    }

    public static boolean isValid(String id) {
        return id.startsWith("amodeus:");
    }
}
