package ch.ethz.idsc.amodeus.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum PropertiesUtils {
	;
	public static void sortPropertiesAlphabetically(File simOptionsFile) throws IOException {
		// Sort options by alphabet for better overview
		if (simOptionsFile.exists()) {

			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(simOptionsFile))) {
				String line;
				List<String> lineList = new ArrayList<>();
				while ((line = bufferedReader.readLine()) != null) {
					lineList.add(line);
				}

				Collections.sort(lineList, String.CASE_INSENSITIVE_ORDER);

				try (PrintWriter printWriter = new PrintWriter(new FileWriter(simOptionsFile))) {
					for (String outputLine : lineList) {
						printWriter.println(outputLine);
					}
				}
			}
		} else {
			System.err.println("file not found:\n" + simOptionsFile);
		}
	}
}
