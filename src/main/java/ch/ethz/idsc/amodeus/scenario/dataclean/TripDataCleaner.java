/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;
import ch.ethz.idsc.amodeus.scenario.trips.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TripDataCleaner extends AbstractDataCleaner<TaxiTrip> {
    private final AbstractTripsReader abstractTripsReader;

    public TripDataCleaner(AbstractTripsReader abstractTripsReader) {
        this.abstractTripsReader = abstractTripsReader;
    }

    @Override // from AbstractDataCleaner
    public Stream<TaxiTrip> readFile(File file) throws IOException {
        System.out.println("Reading: " +  file.getAbsolutePath());
        System.out.println("Using:   " + abstractTripsReader.getClass().getSimpleName());
        return abstractTripsReader.getTripStream(file);
    }

    @Override // from AbstractDataCleaner
    public File writeFile(File inFile, Stream<TaxiTrip> stream) throws IOException {
        String fileName = FilenameUtils.getBaseName(inFile.getPath()) + "_clean." + FilenameUtils.getExtension(inFile.getPath());
        File outFile = new File(inFile.getParentFile(), fileName);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile))) {
            String headers = Arrays.stream(TaxiTrip.class.getFields()).map(Field::getName) //
                    .collect(Collectors.joining(","));
            bufferedWriter.write(headers);            
            System.out.println("now entering second part");                        
            stream.sorted().forEachOrdered(trip -> {
                try {
                    bufferedWriter.newLine();
                    String line = "disabledBecauseUnreadable.";
                    // TODO clean up the things below
                    // String line = Arrays.stream(trip.getClass().getFields()).map(field -> {
                    // try {
                    // if (field.get(trip) instanceof LocalDateTime)
                    // return LocalDateTime.parse(text, formatter) dateFormat . format((Date) field.get(trip));
                    // return String.valueOf(field.get(trip));
                    // } catch (Exception e) {
                    // return "";
                    // }
                    // }).collect(Collectors.joining(","));
                    bufferedWriter.write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return outFile;
    }

}
