/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import ch.ethz.idsc.tensor.io.ReadLine;

public enum FileLines {
    ;

    public static void sort(File file) throws IOException {
        // Sort options by alphabet for better overview
        if (file.exists()) {
            List<String> lineList;
            // try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // String line;
            // while ((line = bufferedReader.readLine()) != null) {
            // lineList.add(line);
            // }
            // }
            // Collections.sort(lineList, String.CASE_INSENSITIVE_ORDER);
            try (InputStream inputStream = new FileInputStream(file)) {
                lineList = ReadLine.of(inputStream) //
                        .sorted(String.CASE_INSENSITIVE_ORDER) //
                        .collect(Collectors.toList());
            }

            try (PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
                lineList.forEach(printWriter::println);
            }
        } else {
            System.err.println("file not found:\n" + file);
        }
    }
}
