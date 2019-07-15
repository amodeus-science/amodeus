/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public class CharRemovalDataCorrector implements DataCorrector {
    private final String string;

    public CharRemovalDataCorrector(String string) {
        this.string = string;
    }

    @Override
    public File correctFile(File taxiData, MatsimAmodeusDatabase db) throws Exception {
        File outFile = new File(taxiData.getAbsolutePath().replace(".csv", "_corrected.csv"));
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(taxiData)); BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile))) {
            System.out.println("INFO start data correction");

            bufferedReader.lines().forEachOrdered(line -> {
                try {
                    bufferedWriter.write(line.replace(string, ""));
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            System.out.println("INFO successfully stored corrected data in " + outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }
}
