/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.chicago;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;
import ch.ethz.idsc.amodeus.scenario.readers.CsvReader.Row;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public abstract class ChicagoTripsReaderBasic extends AbstractTripsReader {

    private final double milesToM = 1609.34;

    public ChicagoTripsReaderBasic(String delim) {
        super(delim);
    }

    @Override
    public final String getTaxiCode(Row row) {
        return row.get("Taxi ID");
    }

    @Override
    public final Scalar getDistance(Row row) {
        return Quantity.of(Double.valueOf(row.get("trip_miles")) * milesToM, SI.METER); // miles to meters
    }

    @Override
    public final Scalar getWaitingTime(Row row) {
        return null;
    }

}
