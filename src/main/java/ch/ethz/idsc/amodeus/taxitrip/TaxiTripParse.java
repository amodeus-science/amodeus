package ch.ethz.idsc.amodeus.taxitrip;

import java.time.LocalDateTime;

import ch.ethz.idsc.amodeus.util.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public enum TaxiTripParse {
    ;

    public static TaxiTrip fromRow(CsvReader.Row line) {
        // get attributes
        Integer globalId = Integer.parseInt(line.get("localId"));
        String taxiId = line.get("taxiId");
        Tensor pickupLoc = Tensors.fromString(line.get("pickupLoc"));
        Tensor dropoffLoc = Tensors.fromString(line.get("dropoffLoc"));
        Scalar distance = Scalars.fromString(line.get("distance"));
        Scalar waitTime = Scalars.fromString(line.get("waitTime"));
        LocalDateTime pickupDate = LocalDateTime.parse(line.get("pickupDate"));
        Scalar duration = Scalars.fromString(line.get("duration"));
        // compile trip
        return TaxiTrip.of(globalId, taxiId, pickupLoc, dropoffLoc, //
                distance, waitTime, //
                pickupDate, duration);
    }

}
