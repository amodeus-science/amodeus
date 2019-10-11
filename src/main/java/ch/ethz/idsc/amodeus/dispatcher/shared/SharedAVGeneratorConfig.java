/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;

// TODO CLRUCH this is never used, remove?
/* package */ class SharedAVGeneratorConfig extends AVGeneratorConfig {

    final static String NUMBER_OF_SHARED_VEHICLES = "numberOfSharedVehicles";
    private long numberOfSharedVehicles = 0;

    final static String SHARED_VEHICLE_CAPACITY = "sharedVehicleCapacity";
    private long sharedVehicleCapacity = 4;

    public SharedAVGeneratorConfig(AVOperatorConfig parent, String strategyName) {
        super(parent, strategyName);
    }

    @StringSetter(NUMBER_OF_SHARED_VEHICLES)
    public void setNumberOfSharedVehicles(long numberOfSharedVehicles) {
        this.numberOfSharedVehicles = numberOfSharedVehicles;
    }

    @StringGetter(NUMBER_OF_SHARED_VEHICLES)
    public long getNumberOfSharedVehicles() {
        return numberOfSharedVehicles;
    }

    @StringSetter(NUMBER_OF_SHARED_VEHICLES)
    public void setSharedVehicleCapacity(long sharedVehicleCapacity) {
        this.sharedVehicleCapacity = sharedVehicleCapacity;
    }

    @StringGetter(SHARED_VEHICLE_CAPACITY)
    public long getSharedVehicleCapacity() {
        return sharedVehicleCapacity;
    }
}
