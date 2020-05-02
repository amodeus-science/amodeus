/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

//TODO @marcalbert improve documentation of class
/** a gz file is a compressed version of another file */
public enum GZHandler {
    ;
    /** number of bytes to process at a time */
    private static final int SIZE = 1024;

    /** Function to take a .gz file and extract the content
     *
     * @param source a .gz file
     * @param destination the corresponding unzipped file */
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
