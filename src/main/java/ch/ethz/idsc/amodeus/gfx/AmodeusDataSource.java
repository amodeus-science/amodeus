/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.amodeus.view.gheat.DataPoint;
import ch.ethz.idsc.amodeus.view.gheat.HeatMapDataSource;
import ch.ethz.idsc.amodeus.view.gheat.PointLatLng;
import ch.ethz.idsc.amodeus.view.gheat.Projections;

/* package */ class AmodeusDataSource implements HeatMapDataSource {
    private final List<PointLatLng> pointList = new ArrayList<>();

    public void addPoint(PointLatLng pointLatLng) {
        pointList.add(pointLatLng);
    }

    @Override
    public PointLatLng[] getList(DataPoint tlb, DataPoint lrb, int zoom, Projections projection) {
        PointLatLng tl = projection.fromPixelToLatLng(tlb, zoom);
        PointLatLng lr = projection.fromPixelToLatLng(lrb, zoom);

        // Find all of the points that belong in the expanded tile
        // Some points may appear in more than one tile depending where they appear
        List<PointLatLng> llList = new ArrayList<>();
        for (PointLatLng point : pointList)
            if (point.getLatitude() <= tl.getLatitude() //
                    && point.getLongitude() >= tl.getLongitude() //
                    && point.getLatitude() >= lr.getLatitude() //
                    && point.getLongitude() <= lr.getLongitude())
                llList.add(point);

        return llList.toArray(new PointLatLng[llList.size()]);
    }

    public void clear() {
        pointList.clear();
    }
}
