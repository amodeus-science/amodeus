package ch.ethz.idsc.amodeus.scenario.chicago;

import ch.ethz.idsc.amodeus.scenario.dataclean.AbstractDataCleaner;
import ch.ethz.idsc.amodeus.scenario.dataclean.CharRemovalDataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.StandardDataCorrector;
import ch.ethz.idsc.amodeus.scenario.dataclean.TripDataCleaner;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.FleetConverter;
import ch.ethz.idsc.amodeus.scenario.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodeus.scenario.readers.OnlineTripsReaderChicago;
import ch.ethz.idsc.amodeus.scenario.readers.TripsReaderChicago;
import ch.ethz.idsc.amodeus.scenario.trips.TripDistanceFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripDurationFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripEndTimeCorrection;
import ch.ethz.idsc.amodeus.scenario.trips.TripNetworkFilter;
import ch.ethz.idsc.amodeus.scenario.trips.TripStartTimeResampling;


public enum DataOperator {
    CHICAGO(new TripFleetConverter(), new StandardDataCorrector(), new TripDataCleaner(new TripsReaderChicago())) {
        @Override
        public void setFilters() {
            cleaner.addFilter(new TripStartTimeResampling(15)); // start/end times in 15 min resolution
            cleaner.addFilter(new TripEndTimeCorrection());
            cleaner.addFilter(new TripNetworkFilter());
            // cleaner.addFilter(new TripDistanceRatioFilter(4)); // massive slow down
            cleaner.addFilter(new TripDurationFilter(20000));
            cleaner.addFilter(new TripDistanceFilter(500, 50000));
        }
    },
    CHICAGO_ONLINE(new TripFleetConverter(), new CharRemovalDataCorrector("\""), new TripDataCleaner(new OnlineTripsReaderChicago())) {
        @Override
        public void setFilters() {
            cleaner.addFilter(new TripStartTimeResampling(15)); // start/end times in 15 min resolution
            cleaner.addFilter(new TripEndTimeCorrection());
            cleaner.addFilter(new TripNetworkFilter());
            // cleaner.addFilter(new TripDistanceRatioFilter(4)); // massive slow down
            cleaner.addFilter(new TripDurationFilter(20000));
            cleaner.addFilter(new TripDistanceFilter(500, 50000));
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

    abstract void setFilters();
}
