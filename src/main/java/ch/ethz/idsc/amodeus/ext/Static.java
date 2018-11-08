/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.ext;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import org.gnu.glpk.GLPK;

public enum Static {
    ;

    public static void setup() {
        for (LocationSpec locationSpec : UserLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);

    }

    public static void checkGLPKLib() {
        try {
            System.out.println("Working with GLPK version " + GLPK.glp_version());
        } catch (Exception e) {
            System.err.println("GLPK for java is not installed which is necessary to run the preparer or server. \n "
                    + "In order to install it, follow the instructions provided at\n: " + "http://glpk-java.sourceforge.net/gettingStarted.html \n"
                    + "In order to work properly, either the location of the GLPK library must be  specified in \n" + "the environment variable, using for instance the command"
                    + "export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib/jni \n" + "where /usr/local/lib/jni  is the path where the file libglpk_java.so is located \n"
                    + "in your installation. Alternatively, the path can also be supplied as a JAVA runtime \n" + "argument, e.g., -Djava.library.path=/usr/local/lib/jni");
        }
    }
}
