/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario;

import ch.ethz.idsc.amodeus.scenario.chicago.OnlineTripsReaderChicago;
import ch.ethz.idsc.amodeus.scenario.chicago.TripsReaderChicago;
import ch.ethz.idsc.amodeus.scenario.dataclean.AbstractDataCleaner;
import ch.ethz.idsc.amodeus.scenario.dataclean.CharRemovalDataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.StandardDataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.TripDataCleaner;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.FleetConverter;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodeus.scenario.trips.TripDistanceFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripDurationFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripNetworkFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripStartTimeResampling;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum DataOperator {
    // FIXME design is prohibitive !!! list of filters is going with every call !!!
    CHICAGO(new TripFleetConverter(), new StandardDataCorrector(), new TripDataCleaner(new TripsReaderChicago())) {
        @Override
        public void setFilters() {
            cleaner.addFilter(new TripStartTimeResampling(15)); // start/end times in 15 min resolution
            // cleaner.addFilter(new TripEndTimeCorrection());
            cleaner.addFilter(new TripNetworkFilter());
            // cleaner.addFilter(new TripDistanceRatioFilter(4)); // massive slow down
            cleaner.addFilter(new TripDurationFilter(Quantity.of(20000, SI.SECOND)));
            cleaner.addFilter(new TripDistanceFilter(Quantity.of(500, SI.METER), Quantity.of(50000, SI.METER)));
        }
    },
    CHICAGO_ONLINE(new TripFleetConverter(), new CharRemovalDataCorrector("\""), new TripDataCleaner(new OnlineTripsReaderChicago())) {
        @Override
        public void setFilters() {
            cleaner.addFilter(new TripStartTimeResampling(15)); // start/end times in 15 min resolution
            // cleaner.addFilter(new TripEndTimeCorrection());
            cleaner.addFilter(new TripNetworkFilter());
            // cleaner.addFilter(new TripDistanceRatioFilter(4)); // massive slow down
            cleaner.addFilter(new TripDurationFilter(Quantity.of(20000, SI.SECOND)));
            cleaner.addFilter(new TripDistanceFilter(Quantity.of(500, SI.METER), Quantity.of(50000, SI.METER)));
        }
    };

    public final FleetConverter fleetConverter;
    public final DataCorrector dataCorrector;
    public final AbstractDataCleaner cleaner;

    DataOperator(FleetConverter fleetConverter, DataCorrector dataCorrector, AbstractDataCleaner cleaner) {
        this.fleetConverter = fleetConverter;
        this.dataCorrector = dataCorrector;
        this.cleaner = cleaner;
    }

    public abstract void setFilters();
}
