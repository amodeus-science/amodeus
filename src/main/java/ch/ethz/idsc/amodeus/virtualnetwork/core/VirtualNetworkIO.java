/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.zip.DataFormatException;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;

public enum VirtualNetworkIO {
    ;

    /** @param file to save at
     * @param virtualNetwork to save
     * @throws IOException */
    public static <T> void toByte(File file, VirtualNetwork<T> virtualNetwork) throws IOException {
        virtualNetwork.checkConsistency();
        Export.object(file, virtualNetwork);
    }

    /** @param map map with serialization info that s a @param T element to a unique String name
     * @param file where virtualNetwork is stored
     * @return
     * @throws ClassNotFoundException
     * @throws DataFormatException
     * @throws IOException */
    public static <T> VirtualNetwork<T> fromByte(Map<String, T> map, File file)//
            throws ClassNotFoundException, DataFormatException, IOException {
        VirtualNetwork<T> virtualNetwork = Import.object(file);
        GlobalAssert.that(Objects.nonNull(virtualNetwork));
        virtualNetwork.fillSerializationInfo(map);
        virtualNetwork.checkConsistency();
        return virtualNetwork;
    }

}
