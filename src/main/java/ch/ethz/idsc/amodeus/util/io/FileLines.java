/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FileLines {
    ;

    public static void sort(File file) throws IOException {
        // Sort options by alphabet for better overview
        if (file.exists()) {

            List<String> lineList = new ArrayList<>();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lineList.add(line);
                }
            }

            Collections.sort(lineList, String.CASE_INSENSITIVE_ORDER);

            try (PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
                for (String outputLine : lineList) {
                    printWriter.println(outputLine);
                }
            }

        } else {
            System.err.println("file not found:\n" + file);
        }
    }

}
