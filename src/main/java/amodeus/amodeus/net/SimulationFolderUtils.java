/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import java.io.File;

import amodeus.amodeus.util.io.MultiFileTools;

public enum SimulationFolderUtils {
    ;

    /** This recursive function looks for the folderName and returns the maximal folder depth found to this named folder
     * relative to the root. If no folder with this name was found, -1 is returned */
    public static int getMaxDepth(File root, String folderName) {
        if (root.getName().equals(folderName))
            return 0;

        File[] children = MultiFileTools.getAllDirectoriesSortedWithSubfolderName(root, folderName);
        if (children.length == 0)
            return -1;

        // looking for the folder name in max depth over all children
        int maxDepth = 0;
        for (File child : children) {
            int childDepth = getMaxDepth(child, folderName);
            if (childDepth > maxDepth)
                maxDepth = childDepth;
        }

        return maxDepth + 1;
    }

    public static String[] getSubfolderNames(File root) {
        // get list of all outputfolders in the outputDirectory
        System.out.println("getting all output folders from: " + root.getAbsolutePath());
        File[] outputFolders = MultiFileTools.getAllDirectoriesSorted(root);
        String[] outputFolderNames = new String[outputFolders.length];
        for (int i = 0; i < outputFolders.length; ++i)
            outputFolderNames[i] = outputFolders[i].getName();
        return outputFolderNames;
    }
}
