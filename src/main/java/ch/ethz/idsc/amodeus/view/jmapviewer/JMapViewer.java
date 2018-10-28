// License: GPL. For details, see Readme.txt file.
package ch.ethz.idsc.amodeus.view.jmapviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.ICoordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileCache;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileLoaderListener;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.MapnikTileSource;

/** Provides a simple panel that displays pre-rendered map tiles loaded from the
 * OpenStreetMap project.
 *
 * @author Jan Peter Stotz
 * @author Jason Huntley */
public class JMapViewer extends JComponent implements TileLoaderListener {
    /** Vectors for clock-wise tile painting */
    private static final Point[] MOVE = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };

    /** Maximum zoom level */
    public static final int MAX_ZOOM = 22;
    /** Minimum zoom level */
    public static final int MIN_ZOOM = 0;

    /** whether debug mode is enabled or not */
    public static boolean DEBUG;

    /** option to reverse zoom direction with mouse wheel */
    public static boolean ZOOMREVERSEWHEEL;
    // ---
    protected boolean tileGridVisible = false; // <- for debug purposes
    protected boolean scrollWrapEnabled;

    private transient TileController tileController;

    protected final List<AmodeusHeatMap> matsimHeatmaps = new ArrayList<>();

    /** x- and y-position of the center of this map-panel on the world map
     * denoted in screen pixel regarding the current zoom level. */
    protected Point center;

    /** Current zoom level */
    protected int zoom;

    public int mapAlphaCover = 192;
    public int mapGrayCover = 0;

    private transient TileSource tileSource;

    private transient AttributionSupport attribution = new AttributionSupport();

    /** Creates a standard {@link JMapViewer} instance that can be controlled via
     * mouse: hold right mouse button for moving, double click left mouse button
     * or use mouse wheel for zooming. Loaded tiles are stored in a
     * {@link MemoryTileCache} and the tile loader uses 4 parallel threads for
     * retrieving the tiles. */
    public JMapViewer() {
        this(new MemoryTileCache());
        new DefaultMapController(this);
    }

    /** Creates a new {@link JMapViewer} instance.
     * 
     * @param tileCache
     *            The cache where to store tiles */
    JMapViewer(TileCache tileCache) {
        tileSource = MapnikTileSource.INSTANCE;
        tileController = new TileController(tileSource, tileCache, this);
        setLayout(null);
        setMinimumSize(new Dimension(tileSource.getTileSize(), tileSource.getTileSize()));
        setPreferredSize(new Dimension(400, 400));
        setDisplayPosition(new Coordinate(50, 9), 3);
    }

    /** Changes the map pane so that it is centered on the specified coordinate
     * at the given zoom level.
     *
     * @param to
     *            specified coordinate
     * @param zoom
     *            {@link #MIN_ZOOM} &lt;= zoom level &lt;= {@link #MAX_ZOOM} */
    public void setDisplayPosition(ICoordinate to, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), to, zoom);
    }

    /** Changes the map pane so that the specified coordinate at the given zoom
     * level is displayed on the map at the screen coordinate
     * <code>mapPoint</code>.
     *
     * @param mapPoint
     *            point on the map denoted in pixels where the coordinate should
     *            be set
     * @param to
     *            specified coordinate
     * @param zoom
     *            {@link #MIN_ZOOM} &lt;= zoom level &lt;=
     *            {@link TileSource#getMaxZoom()} */
    public void setDisplayPosition(Point mapPoint, ICoordinate to, int zoom) {
        Point p = tileSource.latLonToXY(to, zoom);
        setDisplayPosition(mapPoint, p.x, p.y, zoom);
    }

    /** Sets the display position.
     * 
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param zoom
     *            zoom level, between {@link #MIN_ZOOM} and {@link #MAX_ZOOM} */
    void setDisplayPosition(int x, int y, int zoom) {
        setDisplayPosition(new Point(getWidth() / 2, getHeight() / 2), x, y, zoom);
    }

    /** Sets the display position.
     * 
     * @param mapPoint
     *            map point
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param zoom
     *            zoom level, between {@link #MIN_ZOOM} and {@link #MAX_ZOOM} */
    void setDisplayPosition(Point mapPoint, int x, int y, int zoom) {
        if (zoom > tileController.getTileSource().getMaxZoom() || zoom < MIN_ZOOM)
            return;

        // Get the plain tile number
        Point p = new Point();
        p.x = x - mapPoint.x + getWidth() / 2;
        p.y = y - mapPoint.y + getHeight() / 2;
        center = p;
        this.zoom = zoom;
        repaint();
    }

    /** Calculates the latitude/longitude coordinate of the center of the
     * currently displayed map area.
     *
     * @return latitude / longitude */
    public ICoordinate getPosition() {
        return tileSource.xyToLatLon(center, zoom);
    }

    /** Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPoint
     *            relative pixel coordinate regarding the top left corner of the
     *            displayed map
     * @return latitude / longitude */
    public ICoordinate getPosition(Point mapPoint) {
        return getPosition(mapPoint.x, mapPoint.y);
    }

    /** Converts the relative pixel coordinate (regarding the top left corner of
     * the displayed map) into a latitude / longitude coordinate
     *
     * @param mapPointX
     *            X coordinate
     * @param mapPointY
     *            Y coordinate
     * @return latitude / longitude */
    private ICoordinate getPosition(int mapPointX, int mapPointY) {
        int x = center.x + mapPointX - getWidth() / 2;
        int y = center.y + mapPointY - getHeight() / 2;
        return tileSource.xyToLatLon(x, y, zoom);
    }

    /** Calculates the position on the map of a given coordinate
     *
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     * @param checkOutside
     *            check if the point is outside the displayed area
     * @return point on the map or <code>null</code> if the point is not visible
     *         and checkOutside set to <code>true</code> */
    public Point getMapPosition(double lat, double lon, boolean checkOutside) {
        Point p = tileSource.latLonToXY(lat, lon, zoom);
        p.translate(-(center.x - getWidth() / 2), -(center.y - getHeight() / 2));

        if (checkOutside && (p.x < 0 || p.y < 0 || p.x > getWidth() || p.y > getHeight())) {
            return null;
        }
        return p;
    }

    /** Calculates the position on the map of a given coordinate
     *
     * @param lat
     *            latitude
     * @param lon
     *            longitude
     * @return point on the map or <code>null</code> if the point is not visible */
    public Point getMapPosition(double lat, double lon) {
        return getMapPosition(lat, lon, true);
    }

    /** Gets the meter per pixel.
     *
     * @return the meter per pixel */
    public double getMeterPerPixel() {
        Point origin = new Point(5, 5);
        Point center = new Point(getWidth() / 2, getHeight() / 2);

        double pDistance = center.distance(origin);

        ICoordinate originCoord = getPosition(origin);
        ICoordinate centerCoord = getPosition(center);

        double mDistance = tileSource.getDistance(originCoord.getLat(), originCoord.getLon(), centerCoord.getLat(), centerCoord.getLon());

        return mDistance / pDistance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // super.paintComponent(g);

        int iMove = 0;

        int tilesize = tileSource.getTileSize();
        int tilex = center.x / tilesize;
        int tiley = center.y / tilesize;
        int offsx = center.x % tilesize;
        int offsy = center.y % tilesize;

        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        int posx = w2 - offsx;
        int posy = h2 - offsy;

        int diffLeft = offsx;
        int diffRight = tilesize - offsx;
        int diffTop = offsy;
        int diffBottom = tilesize - offsy;

        boolean startLeft = diffLeft < diffRight;
        boolean startTop = diffTop < diffBottom;

        if (startTop) {
            if (startLeft) {
                iMove = 2;
            } else {
                iMove = 3;
            }
        } else {
            if (startLeft) {
                iMove = 1;
            } else {
                iMove = 0;
            }
        } // calculate the visibility borders
        int xMin = -tilesize;
        int yMin = -tilesize;
        int xMax = getWidth();
        int yMax = getHeight();

        // calculate the length of the grid (number of squares per edge)
        int gridLength = 1 << zoom;

        // paint the tiles in a spiral, starting from center of the map
        boolean painted = true;
        int x = 0;
        g.setFont(new Font(Font.DIALOG, Font.PLAIN, 10)); // jan added this
        while (painted) {
            painted = false;
            for (int i = 0; i < 4; i++) {
                if (i % 2 == 0) {
                    x++;
                }
                for (int j = 0; j < x; j++) {
                    if (xMin <= posx && posx <= xMax && yMin <= posy && posy <= yMax) {
                        // tile is visible
                        Tile tile;
                        if (isScrollWrapEnabled()) {
                            // in case tilex is out of bounds, grab the tile to use for wrapping
                            int tilexWrap = ((tilex % gridLength) + gridLength) % gridLength;
                            tile = tileController.getTile(tilexWrap, tiley, zoom);
                        } else {
                            tile = tileController.getTile(tilex, tiley, zoom);
                        }
                        if (tile != null) {
                            tile.paint(g, posx, posy); // , tilesize, tilesize

                            {
                                int rgb = mapGrayCover;
                                g.setColor(new Color(rgb, rgb, rgb, mapAlphaCover));
                                g.fillRect(posx, posy, 256, 256);
                            }

                            for (AmodeusHeatMap matsimHeatmap : matsimHeatmaps)
                                matsimHeatmap.render(g, tile, zoom, posx, posy);

                            if (isTileGridVisible()) {
                                g.setColor(new Color(0, 0, 0, 32));
                                g.drawLine(posx + 1, posy, posx + tilesize, posy);
                                g.drawLine(posx, posy + 1, posx, posy + tilesize);
                                g.setColor(new Color(0, 0, 0, 128));
                                g.drawString(String.format("x=%d y=%d", tile.getXtile(), tile.getYtile()), posx, posy + 10);
                            }
                        }
                        painted = true;
                    }
                    Point p = MOVE[iMove];
                    posx += p.x * tilesize;
                    posy += p.y * tilesize;
                    tilex += p.x;
                    tiley += p.y;
                }
                iMove = (iMove + 1) % MOVE.length;
            }
        }
        // outer border of the map
        int mapSize = tilesize << zoom;
        if (scrollWrapEnabled) {
            g.drawLine(0, h2 - center.y, getWidth(), h2 - center.y);
            g.drawLine(0, h2 - center.y + mapSize, getWidth(), h2 - center.y + mapSize);
        } else {
            g.drawRect(w2 - center.x, h2 - center.y, mapSize, mapSize);
        }

        // g.drawString("Tiles in cache: " + tileCache.getTileCount(), 50, 20);

        // keep x-coordinates from growing without bound if scroll-wrap is enabled
        if (scrollWrapEnabled) {
            center.x = center.x % mapSize;
        }

        attribution.paintAttribution(g, getWidth(), getHeight(), getPosition(0, 0), getPosition(getWidth(), getHeight()), zoom, this);

    }

    /** Moves the visible map pane.
     *
     * @param x
     *            horizontal movement in pixel.
     * @param y
     *            vertical movement in pixel */
    public void moveMap(int x, int y) {
        tileController.cancelOutstandingJobs(); // Clear outstanding load
        center.x += x;
        center.y += y;
        repaint();
    }

    /** @return the current zoom level */
    public int getZoom() {
        return zoom;
    }

    /** Increases the current zoom level by one */
    public void zoomIn() {
        setZoom(zoom + 1);
    }

    /** Increases the current zoom level by one
     * 
     * @param mapPoint
     *            point to choose as center for new zoom level */
    public void zoomIn(Point mapPoint) {
        setZoom(zoom + 1, mapPoint);
    }

    /** Decreases the current zoom level by one */
    public void zoomOut() {
        setZoom(zoom - 1);
    }

    /** Decreases the current zoom level by one
     *
     * @param mapPoint
     *            point to choose as center for new zoom level */
    public void zoomOut(Point mapPoint) {
        setZoom(zoom - 1, mapPoint);
    }

    /** Set the zoom level and center point for display
     *
     * @param zoom
     *            new zoom level
     * @param mapPoint
     *            point to choose as center for new zoom level */
    public void setZoom(int zoom, Point mapPoint) {
        if (zoom > tileController.getTileSource().getMaxZoom() || zoom < tileController.getTileSource().getMinZoom() || zoom == this.zoom)
            return;
        ICoordinate zoomPos = getPosition(mapPoint);
        tileController.cancelOutstandingJobs(); // Clearing outstanding load
        // requests
        setDisplayPosition(mapPoint, zoomPos, zoom);
    }

    /** Set the zoom level
     *
     * @param zoom
     *            new zoom level */
    public void setZoom(int zoom) {
        setZoom(zoom, new Point(getWidth() / 2, getHeight() / 2));
    }

    /** Determines whether the tile grid is visible or not.
     * 
     * @return {@code true} if the tile grid is visible, {@code false} otherwise */
    public boolean isTileGridVisible() {
        return tileGridVisible;
    }

    /** Sets whether the tile grid is visible or not.
     * 
     * @param tileGridVisible
     *            {@code true} if the tile grid is visible, {@code false} otherwise */
    public void setTileGridVisible(boolean tileGridVisible) {
        this.tileGridVisible = tileGridVisible;
        repaint();
    }

    /** Sets the tile source.
     * 
     * @param tileSource
     *            tile source */
    public void setTileSource(TileSource tileSource) {
        if (tileSource.getMaxZoom() > MAX_ZOOM)
            throw new RuntimeException("Maximum zoom level too high");
        if (tileSource.getMinZoom() < MIN_ZOOM)
            throw new RuntimeException("Minimum zoom level too low");
        ICoordinate position = getPosition();
        this.tileSource = tileSource;
        tileController.setTileSource(tileSource);
        tileController.cancelOutstandingJobs();
        if (zoom > tileSource.getMaxZoom()) {
            setZoom(tileSource.getMaxZoom());
        }
        attribution.initialize(tileSource);
        setDisplayPosition(position, zoom);
        repaint();
    }

    @Override
    public void tileLoadingFinished(Tile tile, boolean success) {
        tile.setLoaded(success);
        repaint();
    }

    /** Determines whether scroll wrap is enabled or not.
     * 
     * @return {@code true} if scroll wrap is enabled, {@code false} otherwise */
    public boolean isScrollWrapEnabled() {
        return scrollWrapEnabled;
    }

    /** Sets whether scroll wrap is enabled or not.
     * 
     * @param scrollWrapEnabled
     *            {@code true} if scroll wrap is enabled, {@code false} otherwise */
    public void setScrollWrapEnabled(boolean scrollWrapEnabled) {
        this.scrollWrapEnabled = scrollWrapEnabled;
        repaint();
    }

    /** Returns attribution.
     * 
     * @return attribution */
    public AttributionSupport getAttribution() {
        return attribution;
    }
}
