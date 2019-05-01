/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/* package */ class Filename implements Serializable {
    public final File file;
    public final String title;
    private final String extension;

    public Filename(File file) {
        this.file = file;
        String string = file.getName();
        int index = string.lastIndexOf('.');
        if (0 <= index) {
            title = string.substring(0, index);
            extension = string.substring(index + 1);
        } else {
            title = string;
            extension = "";
        }
    }

    public String extension() {
        return extension;
    }

    public boolean hasExtension(String string) {
        return extension.equalsIgnoreCase(string);
    }

    /** @param string if null, produces file with title only, for instance to use as a directory
     * @return */
    public File withExtension(String string) {
        return new File(file.getParentFile(), title + (Objects.isNull(string) ? "" : "." + string));
    }
}
