package ch.ethz.idsc.amodeus.scenario.chicago;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OsmLoader {
    private final double[] bbox;

    public static void main(String[] args) {
        OsmLoader osm = new OsmLoader(-87.6497, 41.8602, -87.6092, 41.9019);
        osm.saveIfNotAlreadyExists(new File("C:/Users/joelg/Downloads/map.osm"));
    }

    public OsmLoader(double minLong, double minLat, double maxLong, double maxLat) {
        bbox = new double[] {minLong, minLat, maxLong, maxLat};
        checkBbox();
    }

    public OsmLoader(double[] bbox) {
        this.bbox = bbox;
        checkBbox();
    }

    private void checkBbox() {
        GlobalAssert.that(bbox.length == 4);
        GlobalAssert.that(bbox[0] < bbox[2] && bbox[1] < bbox[3]);
    }

    public URL getURL() throws MalformedURLException {
        return new URL("https://overpass-api.de/api/map?bbox=" + Arrays.stream(bbox) //
                .mapToObj(String::valueOf).map(Object::toString).collect(Collectors.joining(",")));
    }

    public void saveTo(File file) {
        try {
            URL url = getURL();
            System.out.println("INFO download data from " + url);
            InputStream in = url.openStream();
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveIfNotAlreadyExists(File file) {
        if (file.exists())
            System.err.println("INFO " + file.getAbsolutePath() + " already exists");
        else
            saveTo(file);
    }
}
