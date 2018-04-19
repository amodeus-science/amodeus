package ch.ethz.idsc.amodeus.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.matsim.api.core.v01.network.Link;

import junit.framework.TestCase;

public class VirtualNetworkLoadTest extends TestCase {
    public void testSimple() throws ClassNotFoundException, DataFormatException, IOException {
        Map<String, Link> map = new HashMap<>();
        // VirtualNetworkIO.fromByte(map, new File("resources/testComparisonFiles/virtualNetwork"));
    }
}
