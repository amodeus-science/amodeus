/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class CsvReader {
    private File file;
    private final String delim;
    protected List<String> headers = new ArrayList<>();

    public CsvReader(String delim) {
        this.delim = delim;
    }

    public void read(File file) {
        this.file = file;
        readHeaders();
    }

    private void readHeaders() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            if ((line = bufferedReader.readLine()) != null)
                headers = Arrays.asList(line.split(delim));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String[] row, String key) {
        GlobalAssert.that(headers.contains(key));
        return row[headers.indexOf(key)];
    }

    public Stream<String[]> lines() throws IOException {
        GlobalAssert.that(file.isFile());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return bufferedReader.lines().skip(1).map(line -> line.split(delim));
        }
    }
}
