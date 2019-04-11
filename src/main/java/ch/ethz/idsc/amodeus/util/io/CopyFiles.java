package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public enum CopyFiles {
    ;

    /** Copy list of {@link File}s @param fileNames from directory @param fromDir to
     * directory @param toDir without replacing existing files.
     * 
     * @throws IOException */
    public static void now(String fromDir, String toDir, List<String> fileNames)//
            throws IOException {
        now(fromDir, toDir, fileNames, false);
    }

    /** Copy list of {@link File}s @param fileNames from directory @param fromDir to
     * directory @param toDir, set @param replaceExisting true to replace existing files.
     * 
     * @throws IOException */
    public static void now(String fromDir, String toDir, List<String> fileNames, //
            boolean replaceExisting) throws IOException {
        CopyOption[] options = replaceExisting ? //
                new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING } : //
                new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES };
        for (String fileName : fileNames) {
            Path source = Paths.get(fromDir, fileName);
            Path target = Paths.get(toDir, fileName);
            Files.copy(source, target, options);
        }
    }
}
