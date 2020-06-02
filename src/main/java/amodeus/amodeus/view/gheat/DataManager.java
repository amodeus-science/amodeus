/* Copyright (c) 2014 Varun Pant
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * original code retrieved from
 * https://github.com/varunpant/GHEAT-JAVA
 *
 * The code was modified by the IDSC-Frazzoli team at the
 * Institute for Dynamic Systems and Control of ETH Zurich 
 * for use in the amodeus library, 2017-2018. */

package amodeus.amodeus.view.gheat;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final Projections PROJECTION = MercatorProjection.INSTANCE;
    // ---
    private final HeatMapDataSource dataSource;

    public DataManager(HeatMapDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataPoint[] getPointsForTile(int x, int y, BufferedImage dot, int zoom) {
        List<DataPoint> points = new ArrayList<>();
        Size maxTileSize = new Size(HeatMap.SIZE, HeatMap.SIZE);
        DataPoint adjustedDataPoint;
        DataPoint pixelCoordinate;
        // Top Left Bounds
        DataPoint tlb = PROJECTION.fromTileXYToPixel(new DataPoint(x, y));
        // Lower right bounds
        DataPoint lrb = new DataPoint((tlb.getX() + maxTileSize.getWidth()) + dot.getWidth(), (tlb.getY() + maxTileSize.getHeight()) + dot.getWidth());
        // pad the Top left bounds
        tlb = new DataPoint(tlb.getX() - dot.getWidth(), tlb.getY() - dot.getHeight());
        PointLatLng[] TilePoints = dataSource.getList(tlb, lrb, zoom, PROJECTION);
        // Go throught the list and convert the points to pixel cooridents
        for (PointLatLng llDataPoint : TilePoints) {
            // Now go through the list and turn it into pixel points
            pixelCoordinate = PROJECTION.fromLatLngToPixel(llDataPoint.getLatitude(), llDataPoint.getLongitude(), zoom);
            // Make sure the weight is still pointing after the conversion
            pixelCoordinate.setWeight(llDataPoint.getWeight());
            // Adjust the point to the specific tile
            adjustedDataPoint = adjustMapPixelsToTilePixels(new DataPoint(x, y), pixelCoordinate);
            // Make sure the weight is still pointing after the conversion
            adjustedDataPoint.setWeight(pixelCoordinate.getWeight());
            // Add the point to the list
            points.add(adjustedDataPoint);
        }
        return points.toArray(new DataPoint[points.size()]);
    }

    private static DataPoint adjustMapPixelsToTilePixels(DataPoint tileXYPoint, DataPoint mapPixelPoint) {
        return new DataPoint(mapPixelPoint.getX() - (tileXYPoint.getX() * HeatMap.SIZE), mapPixelPoint.getY() - (tileXYPoint.getY() * HeatMap.SIZE));
    }
}
