/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.io.File;

public class IterationFolder {
    private final File itDir;
    private final StorageSupplier storageSupplier;

    /** for instance, itDir can be
     * /media/datahaki/data/ethz/2017_03_09_Sioux_HU/output/simobj/it.02 */
    public IterationFolder(File itDir, StorageUtils storageUtils) {
        this.itDir = itDir;
        storageSupplier = new StorageSupplier(storageUtils.getFrom(itDir));
    }

    public StorageSupplier storageSupplier() {
        return storageSupplier;
    }

    @Override
    public String toString() {
        String string = itDir.getName(); // "it.02"
        String digits = string.substring(string.length() - 2, string.length()); // "02"
        return "" + Integer.parseInt(digits); // "2"
    }
}
