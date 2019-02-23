package ch.ethz.idsc.amodeus.scenario.readers;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// TODO remove
@Deprecated // surely we have this implemented somewhere...
public class CsvReader {
    private File file;
    private String delim;

    protected List<String> headers = new ArrayList<>();

    protected final DateFormat format;

    public CsvReader(String delim, DateFormat format) {
        this.delim = delim;
        this.format = format;
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

    public String[] getRow(int row) {
        GlobalAssert.that(!headers.isEmpty());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                GlobalAssert.that(index <= row);
                if (index == row)
                    return line.split(delim);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String get(String[] row, String key) {
        GlobalAssert.that(headers.contains(key));
        return row[headers.indexOf(key)];
    }

    public String get(int row, String key) {
        return get(getRow(row), key);
    }

    public Stream<String[]> lines() throws IOException {
        GlobalAssert.that(file.isFile());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        return bufferedReader.lines().skip(1).map(line -> line.split(delim));
    }
}
