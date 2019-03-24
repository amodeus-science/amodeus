package ch.ethz.idsc.amodeus.scenario.chicago;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public abstract class ChicagoTripsReaderBasic extends AbstractTripsReader {

    private final double milesToM = 1609.34;

    public ChicagoTripsReaderBasic(String delim) {
        super(delim);
    }

    @Override
    public final String getTaxiCode(String[] line) {
        return get(line, "Taxi ID");
    }

    @Override
    public final Scalar getDistance(String[] line) {
        return Quantity.of(Double.valueOf(get(line, "trip_miles")) * milesToM, SI.METER); // miles to meters
    }

    @Override
    public final Scalar getWaitingTime(String[] line) {
        return null;
    }

}
