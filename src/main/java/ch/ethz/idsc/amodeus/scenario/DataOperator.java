/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.AbstractDataCleaner;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataCorrector;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.FleetConverter;

public abstract class DataOperator<T> {

    public final FleetConverter fleetConverter;
    public final DataCorrector dataCorrector;
    public final AbstractDataCleaner<T> cleaner;
    protected final ScenarioOptions scenarioOptions;
    protected final Network network;

    public DataOperator(FleetConverter fleetConverter, DataCorrector dataCorrector, //
            AbstractDataCleaner<T> cleaner, ScenarioOptions scenarioOptions, Network network) {
        this.fleetConverter = fleetConverter;
        this.dataCorrector = dataCorrector;
        this.cleaner = cleaner;
        this.scenarioOptions = scenarioOptions;
        this.network = network;
    }

    public abstract void setFilters();
}
