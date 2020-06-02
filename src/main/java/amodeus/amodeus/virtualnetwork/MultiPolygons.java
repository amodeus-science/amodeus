/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.MultiPolygon;

import amodeus.amodeus.util.math.GlobalAssert;

public class MultiPolygons {

    private final Set<MultiPolygon> polygons;

    public MultiPolygons(File shapeFile) throws IOException {
        GlobalAssert.that(isConsistent(shapeFile));
        this.polygons = initializeFrom(shapeFile);
        GlobalAssert.that(isConsistent());

    }

    private static Set<MultiPolygon> initializeFrom(File shapeFile) throws IOException {
        URL shapeFileURL = shapeFile.toURI().toURL();
        Map<String, URL> inputMap = new HashMap<>();
        inputMap.put("url", shapeFileURL);

        DataStore dataStore = DataStoreFinder.getDataStore(inputMap);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        SimpleFeatureCollection collection = DataUtilities.collection(featureSource.getFeatures());
        dataStore.dispose();

        Set<MultiPolygon> polygons = new HashSet<>();
        SimpleFeatureIterator iterator = collection.features();
        while (iterator.hasNext())
            polygons.add((MultiPolygon) iterator.next().getDefaultGeometry());
        return polygons;
    }

    private boolean isConsistent() {
        GlobalAssert.that(!polygons.isEmpty());
        return true;
    }

    public Set<MultiPolygon> getPolygons() {
        return polygons;
    }

    private static boolean isConsistent(File shapeFile) {
        GlobalAssert.that(getExtensionOf(shapeFile).equals("shp"));
        return true;
    }

    private static String getExtensionOf(File file) {
        String title = file.getName();
        int index = title.lastIndexOf('.');
        return index > 0 //
                ? title.substring(index + 1)
                : "";
    }
}
