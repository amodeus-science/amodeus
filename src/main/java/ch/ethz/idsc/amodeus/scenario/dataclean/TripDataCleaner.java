package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import ch.ethz.idsc.amodeus.scenario.readers.AbstractTripsReader;
import ch.ethz.idsc.amodeus.scenario.trips.Trip;

public class TripDataCleaner extends AbstractDataCleaner<Trip> {
    private final AbstractTripsReader reader;

    public TripDataCleaner(AbstractTripsReader reader) {
        this.reader = reader;
    }

    public Stream<Trip> readFile(File file) throws IOException {
        return reader.getTripStream(file);
    }

    public File writeFile(File inFile, Stream<Trip> stream) throws IOException {
        String fileName = FilenameUtils.getBaseName(inFile.getPath()) + "_clean." + FilenameUtils.getExtension(inFile.getPath());
        File outFile = new File(inFile.getParentFile(), fileName);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile))) {
            String headers = Arrays.stream(Trip.class.getFields()).map(Field::getName) //
                    .collect(Collectors.joining(","));
            bufferedWriter.write(headers);
            stream.sorted().forEachOrdered(trip -> {
                try {
                    bufferedWriter.newLine();
                    String line = Arrays.stream(trip.getClass().getFields()).map(field -> {
                        try {
                            if (field.get(trip) instanceof Date)
                                return dateFormat.format((Date) field.get(trip));
                            return String.valueOf(field.get(trip));
                        } catch (Exception e) {
                            return "";
                        }
                    }).collect(Collectors.joining(","));
                    bufferedWriter.write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return outFile;
    }

}
