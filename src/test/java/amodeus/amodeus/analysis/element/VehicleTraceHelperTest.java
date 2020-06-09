package amodeus.amodeus.analysis.element;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class VehicleTraceHelperTest {

    @Test
    public void test() {

        NavigableMap<Long, Scalar> testMap = new TreeMap<>();
        testMap.put((long) 2100, Quantity.of(7893.067522387856, "ft"));
        testMap.put((long) 6010, Quantity.of(8242.382929956508, "ft"));

        Assert.assertTrue(VehicleTraceHelper.distanceAt((long) 2200, testMap.firstEntry(), testMap.lastEntry()) //
                .equals(Quantity.of(7902.001420279637, "ft")));

    }

}
