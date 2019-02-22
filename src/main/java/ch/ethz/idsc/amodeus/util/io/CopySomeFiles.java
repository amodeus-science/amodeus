package ch.ethz.idsc.amodeus.util.io;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public enum CopySomeFiles {
    ;

    public static void now(String fromDir, String toDir, //
            List<String> fileNames) throws IOException {
        CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
        for (String fileName : fileNames) {
            Path source = Paths.get(fromDir, fileName);
            Path target = Paths.get(toDir, fileName);
            Files.copy(source, target, options);
        }
    }
}
