/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.glpk;

import org.gnu.glpk.GLPK;

import ch.ethz.idsc.amodeus.util.math.UserHome;
import junit.framework.TestCase;

public class GlpkTest extends TestCase {
    public void testSimple() {
        // home directory=/home/travis
        // GLPK version is: 4.65
        System.out.println("home directory=" + UserHome.file(""));

        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());
    }
}
