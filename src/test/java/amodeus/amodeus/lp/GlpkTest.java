/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.lp;

import org.gnu.glpk.GLPK;

import ch.ethz.idsc.tensor.ext.HomeDirectory;
import junit.framework.TestCase;

public class GlpkTest extends TestCase {
    public void testSimple() {
        // home directory=/home/travis
        // GLPK version is: 4.65
        System.out.println("home directory=" + HomeDirectory.file());

        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());
    }
}
