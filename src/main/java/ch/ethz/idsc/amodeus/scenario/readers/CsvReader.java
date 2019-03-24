/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CsvReader {
    private File file;
    private final String delim;
    private final Map<String, Integer> headers = new HashMap<>();

    public CsvReader(String delim) {
        this.delim = delim;
    }

    public void read(File file) throws FileNotFoundException, IOException {
        this.file = file;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = bufferedReader.readLine();
            if (Objects.nonNull(line)) {
                String[] splits = line.split(delim);
                IntStream.range(0, splits.length).forEach(index -> headers.put(splits[index], index));
            }
        }
    }

    public Stream<Row> lines() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return bufferedReader.lines().skip(1).map(line -> new Row(line.split(delim)));
        }
    }

    public Collection<String> headers() {
        return Collections.unmodifiableCollection(headers.keySet());
    }

    public class Row {
        private final String[] row;

        Row(String[] row) {
            this.row = row;
        }

        public String get(String key) {
            return row[headers.get(key)];
        }

        public String get(int col) {
            return row[col];
        }

    }
}
