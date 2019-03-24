/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
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

/** first line in csv file must consist of header names */
public final class CsvReader {
    private final File file;
    private final String delim;
    private final Map<String, Integer> headers = new HashMap<>();

    public CsvReader(File file, String delim) throws FileNotFoundException, IOException {
        this.file = file;
        this.delim = delim;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = bufferedReader.readLine();
            if (Objects.nonNull(line)) {
                String[] splits = line.split(delim);
                IntStream.range(0, splits.length).forEach(index -> headers.put(splits[index], index));
            }
        }
    }

    public Stream<Row> rows() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return bufferedReader.lines().skip(1).map(line -> new Row(line.split(delim)));
        }
    }

    public Collection<String> headers() {
        return Collections.unmodifiableCollection(headers.keySet());
    }

    public class Row {
        private final String[] row;

        private Row(String[] row) {
            this.row = row;
        }

        /** @param key
         * @return
         * @throws Exception if key is not an element in the header row */
        public String get(String key) {
            return row[headers.get(key)];
        }

        public String get(int col) {
            return row[col];
        }

    }
}
