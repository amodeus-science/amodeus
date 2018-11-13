/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ */
public enum Unzip {
    ;

    /** @param file to unzip
     * @param output folder
     * @param ignoreFirst applies only to zip files that has a single folder at the root level
     *            if ignoreFirst is true, this base folder can be skipped when restoring the compressed files.
     * @return list of files created in unzip process
     * @throws IOException
     * @throws FileNotFoundException */
    public static List<File> of(File file, File outputFolder, boolean ignoreFirst) //
            throws FileNotFoundException, IOException {

        try (InputStream inputStream = new FileInputStream(file)) {
            return of(inputStream, outputFolder, ignoreFirst);
        }
    }

    /** @param inputStream to unzip
     * @param output folder
     * @param ignoreFirst
     * @return list of files created in unzip process
     * @throws IOException */
    public static List<File> of(InputStream inputStream, File outputFolder, boolean ignoreFirst) throws IOException {
        List<File> list = new LinkedList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            if (!outputFolder.exists())
                outputFolder.mkdir();

            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (Objects.nonNull(zipEntry)) {
                final String name = zipEntry.getName();
                int index = ignoreFirst ? name.indexOf('/') : 0;
                if (index < 0)
                    throw new RuntimeException();
                final File target = new File(outputFolder, name.substring(index));
                list.add(target);
                if (zipEntry.isDirectory())
                    target.mkdirs();
                else {
                    new File(target.getParent()).mkdirs();
                    try (OutputStream outputStream = new FileOutputStream(target)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while (0 < (length = zipInputStream.read(buffer)))
                            outputStream.write(buffer, 0, length);
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
        return list;
    }
}
