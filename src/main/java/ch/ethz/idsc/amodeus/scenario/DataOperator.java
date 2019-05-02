/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import ch.ethz.idsc.amodeus.scenario.dataclean.AbstractDataCleaner;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataCorrector;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.FleetConverter;

public abstract class DataOperator {

    public final FleetConverter fleetConverter;
    public final DataCorrector dataCorrector;
    public final AbstractDataCleaner cleaner;

    public DataOperator(FleetConverter fleetConverter, DataCorrector dataCorrector, AbstractDataCleaner cleaner) {
        this.fleetConverter = fleetConverter;
        this.dataCorrector = dataCorrector;
        this.cleaner = cleaner;
    }

    public abstract void setFilters();
}
