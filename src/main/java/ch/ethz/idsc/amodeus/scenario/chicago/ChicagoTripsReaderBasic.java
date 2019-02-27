package ch.ethz.idsc.amodeus.scenario.chicago;

import java.text.SimpleDateFormat;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public abstract class ChicagoTripsReaderBasic extends AbstractTripsReader {

    protected final double milesToM = 1609.34;

    public ChicagoTripsReaderBasic(String delim, SimpleDateFormat format) {
        super(delim, format);
    }

    public final Scalar getWaitingTime(String[] line) {
        return null;
    }

}
