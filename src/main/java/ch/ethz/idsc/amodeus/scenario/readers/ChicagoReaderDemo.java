package ch.ethz.idsc.amodeus.scenario.readers;

import java.io.File;
import java.io.IOException;

public class ChicagoReaderDemo {

    public static void main(String[] args) throws IOException {
        File file = new File("C:/Users/joelg/Documents/Studium/ETH/IDSC/TaxiData/Chicago/Taxi_Trips.csv");

        TripsReaderChicago reader = new TripsReaderChicago();
        reader.getTripStream(file).limit(100000).sorted().forEach(System.out::println);
    }

}
