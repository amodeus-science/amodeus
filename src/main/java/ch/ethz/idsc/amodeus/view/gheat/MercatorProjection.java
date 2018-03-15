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

package ch.ethz.idsc.amodeus.view.gheat;

/* package */ enum MercatorProjection implements Projections {
    INSTANCE;
    // ---
    private static final double MinLatitude = -85.05112878;
    private static final double MaxLatitude = 85.05112878;
    private static final double MinLongitude = -180;
    private static final double MaxLongitude = 180;

    private static double clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    public Size getTileMatrixSizePixel(int zoom) {
        Size s = getTileMatrixSizeXY(zoom);
        return new Size(s.getWidth() * HeatMap.SIZE, s.getHeight() * HeatMap.SIZE);
    }

    public Size getTileMatrixSizeXY(int zoom) {
        Size sMin = getTileMatrixMinXY(zoom);
        Size sMax = getTileMatrixMaxXY(zoom);
        return new Size(sMax.getWidth() - sMin.getWidth() + 1, sMax.getHeight() - sMin.getHeight() + 1);
    }

    @Override
    public Size getTileMatrixMaxXY(int zoom) {
        long xy = (1 << zoom);
        return new Size(xy - 1, xy - 1);
    }

    @Override
    public Size getTileMatrixMinXY(int zoom) {
        return new Size(0, 0);
    }

    @Override
    public DataPoint fromLatLngToPixel(PointLatLng center, int zoom) {
        DataPoint ret = new DataPoint(0, 0);
        center.setLatitude(clip(center.getLatitude(), MinLatitude, MaxLatitude));
        center.setLongitude(clip(center.getLongitude(), MinLongitude, MaxLongitude));
        double x = (center.getLongitude() + 180) / 360;
        double sinLatitude = Math.sin(center.getLatitude() * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        Size s = getTileMatrixSizePixel(zoom);
        long mapSizeX = (long) s.getWidth();
        long mapSizeY = (long) s.getHeight();
        ret.setX((long) clip(x * mapSizeX + 0.5, 0, mapSizeX - 1));
        ret.setY((long) clip(y * mapSizeY + 0.5, 0, mapSizeY - 1));
        return ret;
    }

    @Override
    public DataPoint fromLatLngToPixel(double latitude, double longitude, int zoom) {
        DataPoint ret = new DataPoint(0, 0);
        latitude = clip(latitude, MinLatitude, MaxLatitude);
        longitude = clip(longitude, MinLongitude, MaxLongitude);
        double x = (longitude + 180) / 360;
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        Size s = getTileMatrixSizePixel(zoom);
        long mapSizeX = (long) s.getWidth();
        long mapSizeY = (long) s.getHeight();
        ret.setX((long) clip(x * mapSizeX + 0.5, 0, mapSizeX - 1));
        ret.setY((long) clip(y * mapSizeY + 0.5, 0, mapSizeY - 1));
        return ret;
    }

    @Override
    public PointLatLng fromPixelToLatLng(DataPoint tlb, int zoom) {
        PointLatLng ret = new PointLatLng(0, 0, 0);
        Size s = getTileMatrixSizePixel(zoom);
        double mapSizeX = s.getWidth();
        double mapSizeY = s.getHeight();
        double xx = (clip(tlb.getX(), 0, mapSizeX - 1) / mapSizeX) - 0.5;
        double yy = 0.5 - (clip(tlb.getY(), 0, mapSizeY - 1) / mapSizeY);
        ret.setLatitude(90 - 360 * Math.atan(Math.exp(-yy * 2 * Math.PI)) / Math.PI);
        ret.setLongitude(360 * xx);
        return ret;
    }

    @Override
    public DataPoint fromPixelToTileXY(DataPoint pixelCoordinate) {
        return new DataPoint((long) (pixelCoordinate.getX() / HeatMap.SIZE), (long) (pixelCoordinate.getY() / HeatMap.SIZE));
    }

    @Override
    public DataPoint fromTileXYToPixel(DataPoint dataPoint) {
        return new DataPoint((dataPoint.getX() * HeatMap.SIZE), (dataPoint.getY() * HeatMap.SIZE));
    }
}
