/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.gfx.AmodeusComponent;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.view.jmapviewer.Coordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;

public enum AmodeusComponentUtil {
    ;

    public static void adjustMapZoom(AmodeusComponent amodeusComponent, Network network, //
            ScenarioOptions simOptions, MatsimStaticDatabase db) {
        // Bounding box -> {minX, minY, maxX, maxY}
        double[] bBox = NetworkUtils.getBoundingBox(network.getNodes().values());
        CoordinateTransformation ct = simOptions.getLocationSpec().referenceFrame().coords_toWGS84();
        Coord northWest = ct.transform(new Coord(bBox[0], bBox[3]));
        Coord southWest = ct.transform(new Coord(bBox[0], bBox[1]));
        Coord southEast = ct.transform(new Coord(bBox[2], bBox[1]));
        Coord northEast = ct.transform(new Coord(bBox[2], bBox[3]));
        Set<Coord> envelop = new HashSet<>();
        envelop.add(northEast);
        envelop.add(southWest);
        envelop.add(northWest);
        envelop.add(southEast);

        Coord center = db.getCenter();
        boolean zoomAdj = false;
        boolean zoomAdjPrev = false;
        int prevZoom = 10;
        int width = amodeusComponent.getWidth();
        int height = amodeusComponent.getHeight();
        Point ccc = new Point(width / 2, height / 2);
        ((JMapViewer) amodeusComponent).setDisplayPosition(ccc, new Coordinate(center.getY(), center.getX()), prevZoom);
        while (!zoomAdj) {
            if (envelop.stream().allMatch(c -> ((JMapViewer) amodeusComponent).getMapPosition(c.getY(), c.getX(), true) != null)) {
                prevZoom = prevZoom + 1;
                zoomAdjPrev = true;
            } else {
                prevZoom = prevZoom - 1;
                zoomAdj = (zoomAdjPrev) ? true : false;
                zoomAdjPrev = (zoomAdj) ? true : false;
            }
            ((JMapViewer) amodeusComponent).setDisplayPosition(ccc, new Coordinate(center.getY(), center.getX()), prevZoom);
        }
    }
}
