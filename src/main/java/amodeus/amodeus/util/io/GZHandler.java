/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/** Allows to unzip a .gz file via its extract function */
public enum GZHandler {
    ;
    /** number of bytes to process at a time */
    private static final int SIZE = 1024;

    /** Function to take a .gz file and extract the content
     *
     * @param source a .gz file
     * @param destination file the information in @param source is to be extracted into */
    public static void extract(File source, File destination) throws IOException {
        System.out.println("Opening the gzip file.......................... : " + source.toString());
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(source))) {
            try (OutputStream out = new FileOutputStream(destination)) {
                System.out.println("Transferring bytes from the compressed file to the output file........: Transfer successful");
                byte[] buffer = new byte[SIZE];
                int length;
                while (0 < (length = gzipInputStream.read(buffer)))
                    out.write(buffer, 0, length);
                System.out.println("The file and stream is ......closing.......... : closed");
            }
        }
    }
}
