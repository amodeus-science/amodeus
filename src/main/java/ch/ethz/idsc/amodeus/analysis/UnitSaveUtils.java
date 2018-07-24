/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum UnitSaveUtils {
    ;

    /** saves the matrix in three formats to a folder with the specified name in
     * the directory saveToFolder
     * 
     * @param quantityMatrix
     * @param name
     * @param saveToFolder
     * @throws Exception */
    public static void saveFile(Tensor quantityMatrix, String name, File saveToFolder) throws Exception {
        saveFile(quantityMatrix, name, saveToFolder, SaveFormats.values());
    }

    /** @param quantityMatrix
     * @param name
     * @param saveToFolder
     * @param formats
     * @throws Exception */
    public static void saveFile(Tensor quantityMatrix, String name, File saveToFolder, //
            SaveFormats... formats) throws Exception {

        GlobalAssert.that(saveToFolder.isDirectory());
//        GlobalAssert.that(Dimensions.of(quantityMatrix).size() == 2);
        File folder = createFileDir(name, saveToFolder);
        Set<SaveFormats> saveFormats = EnumSet.copyOf(Arrays.asList(formats));

        /** create new matrix where quantities are removed */
        Tensor columUnits = quantityMatrix.get(0)//
                .map(scalar -> Tensors.fromString((((Quantity) scalar).unit().toString())));
        Tensor bareMatrix = null;
        try {
            bareMatrix = quantityMatrix.map(scalar -> ((Quantity) scalar).value());
        } catch (Exception ex) {
            // quantityMatrix.flatten(-1).forEach(t->{
            // System.out.println("t: " + t);
            // Scalar value = ((Quantity) t).value();
            // System.out.println("value: " + value);
            // });
            System.err.println("likely one of your entries does not have a unit \n" //
                    + "uncomment above to check.");
        }

        for (SaveFormats format : saveFormats) {
            format.save(bareMatrix, folder, name);
            System.out.println(format.toString());
        }
        SaveFormats.CSV.save(Tensors.of(columUnits), folder, "units");
    }

    private static File createFileDir(String name, File saveToFolder) {
        /** rename existing folder if exists */
        File folder = new File(saveToFolder, name);
        if (folder.exists() && folder.isDirectory()) {
            long ts = System.currentTimeMillis();
            folder.renameTo(new File(folder + "_" + ts));
        }
        folder.mkdir();
        return folder;
    }
}