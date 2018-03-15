/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.owl;

import ch.ethz.idsc.owl.data.nd.NdCenterInterface;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Serialization;
import junit.framework.TestCase;

public class EuclideanNdCenterTest extends TestCase {
    public void testSerializable() throws Exception {
        NdCenterInterface euc = NdCenterInterface.euclidean(Tensors.vector(1, 2, 3));
        Serialization.copy(euc);
    }
}
