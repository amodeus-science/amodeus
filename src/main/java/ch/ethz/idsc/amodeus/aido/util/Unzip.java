/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ */
public enum Unzip {
    ;

    /** @param file to unzip
     * @param output folder
     * @param ignoreFirst applies only to zip files that has a single folder at the root level
     *            if ignoreFirst is true, this base folder can be skipped when restoring the compressed files.
     * @throws IOException
     * @throws FileNotFoundException */
    public static void of(File file, File outputFolder, boolean ignoreFirst) //
            throws FileNotFoundException, IOException {

        if (!outputFolder.exists())
            outputFolder.mkdir();

        byte[] buffer = new byte[1024];

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {

            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                String name = zipEntry.getName();
                // System.out.println(name);
                int index = ignoreFirst ? name.indexOf('/') : 0;
                if (index < 0)
                    throw new RuntimeException();
                File target = new File(outputFolder, name.substring(index));
                System.out.println(target);
                if (zipEntry.isDirectory())
                    target.mkdirs();
                else {
                    new File(target.getParent()).mkdirs();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
                        int length;
                        while (0 < (length = zipInputStream.read(buffer)))
                            fileOutputStream.write(buffer, 0, length);
                    }
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }

        }
    }
}
