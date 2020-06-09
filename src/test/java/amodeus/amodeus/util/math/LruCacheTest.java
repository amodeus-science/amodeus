// code by jph
package amodeus.amodeus.util.math;

import java.io.IOException;

import ch.ethz.idsc.tensor.io.Serialization;
import junit.framework.TestCase;

public class LruCacheTest extends TestCase {
    public void testSimple() throws ClassNotFoundException, IOException {
        Serialization.copy(LruCache.create(3));
    }
}
