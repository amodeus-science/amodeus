/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JLabel;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.GraphicsUtil;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.view.jmapviewer.AmodeusHeatMap;
import ch.ethz.idsc.amodeus.view.jmapviewer.Coordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.ICoordinate;

public class AmodeusComponent extends JMapViewer {

    @Deprecated
    /** Should not be used in amodeus repository anymore */
    public static AmodeusComponent createDefault(MatsimAmodeusDatabase db) {
        try {
            return createDefault(db, MultiFileTools.getDefaultWorkingDirectory());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** @param db
     * @return instance of MatsimMapComponent with default sequence of {@link ViewerLayer}s */
    public static AmodeusComponent createDefault(MatsimAmodeusDatabase db, File workingDirectory) {
        AmodeusComponent amodeusComponent = new AmodeusComponent(db);
        amodeusComponent.addLayer(new TilesLayer(amodeusComponent));
        amodeusComponent.addLayer(new VirtualNetworkLayer(amodeusComponent));
        amodeusComponent.addLayer(new VehiclesLayer(amodeusComponent));
        amodeusComponent.addLayer(new RequestsLayer(amodeusComponent));
        amodeusComponent.addLayer(new LinkLayer(amodeusComponent));
        amodeusComponent.addLayer(new LoadLayer(amodeusComponent));
        amodeusComponent.addLayer(new HudLayer(amodeusComponent));
        amodeusComponent.addLayer(new ClockLayer(amodeusComponent));
        amodeusComponent.addLayer(new VideoLayer(amodeusComponent, workingDirectory));
        return amodeusComponent;
    }

    protected final MatsimAmodeusDatabase db;
    @SuppressWarnings("unused")
    private int repaint_count = 0;
    private SimulationObject simulationObject = null;

    public VirtualNetworkLayer virtualNetworkLayer;

    public final List<ViewerLayer> viewerLayers = new ArrayList<>();
    private final List<InfoString> infoStrings = new LinkedList<>();
    private int infoFontSize;

    public final JLabel jLabel = new JLabel(" ");
    final AmodeusComponentMouse amodeusComponentMouse = new AmodeusComponentMouse(this);

    final ViewerConfig defaultConfig;

    /** constructs an component without any {@link ViewerLayer}s
     * 
     * use the function {@link #addLayer(ViewerLayer)} to append layers
     * 
     * @param db */
    public AmodeusComponent(MatsimAmodeusDatabase db) {
        this.db = db;
        defaultConfig = ViewerConfig.fromDefaults(db);
        infoFontSize = defaultConfig.settings.infoFontSize;
        // ---
        addMouseListener(amodeusComponentMouse);
        addMouseMotionListener(amodeusComponentMouse);
    }

    public void addLayer(ViewerLayer viewerLayer) {
        viewerLayers.add(viewerLayer);
        for (AmodeusHeatMap m : viewerLayer.getHeatmaps())
            matsimHeatmaps.add(m);
    }

    public void reorientMap(ViewerConfig viewerConfig) {
        setDisplayPosition( //
                new Coordinate(viewerConfig.settings.coord.getY(), viewerConfig.settings.coord.getX()), //
                viewerConfig.settings.zoom);
    }

    /** @param coord
     * @return null of coord is not within view */
    final Point getMapPosition(Coord coord) {
        return getMapPosition(coord.getY(), coord.getX());
    }

    final Point getMapPositionAlways(Coord coord) {
        return getMapPosition(coord.getY(), coord.getX(), false);
    }

    final Coord getCoordPositionXY(Point point) {
        ICoordinate ic = getPosition(point);
        // System.out.println("lat=" + ic.getLat() + " lon=" + ic.getLon());
        Coord coord = new Coord(ic.getLon(), ic.getLat());
        Coord xy = db.referenceFrame.coords_fromWGS84().transform(coord);
        // System.out.println(xy);
        return xy;
    }

    @Override
    protected void paintComponent(Graphics g) {
        ++repaint_count;
        final SimulationObject ref = simulationObject; // <- use ref for thread safety

        if (Objects.nonNull(ref))
            viewerLayers.forEach(viewerLayer -> viewerLayer.prepareHeatmaps(ref));

        super.paintComponent(g);

        final Graphics2D graphics = (Graphics2D) g;

        infoStrings.clear();
        

        if (Objects.nonNull(ref)) {
            append("i=%-3s %s", "" + ref.iteration, new SecondsToHMS(ref.now).toDigitalWatch());
            appendSeparator();
        }

        viewerLayers.forEach(viewerLayer -> {
            try {
                viewerLayer.paint(graphics, ref);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        viewerLayers.forEach(viewerLayer -> {
            try {
                viewerLayer.hud(graphics, ref);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        append("%5d zoom", getZoom());
        append("%5d m/pixel", (int) Math.ceil(getMeterPerPixel()));
        appendSeparator();

        if (Objects.nonNull(ref))
            jLabel.setText(ref.infoLine);

        if (0 < infoFontSize)
            drawInfoStrings(graphics);

    }

    private void drawInfoStrings(Graphics2D graphics) {
        int piy = 10;
        final int pix = 5;
        final int height = infoFontSize + 2;
        GraphicsUtil.setQualityHigh(graphics);

        graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, infoFontSize));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        for (InfoString infoString : infoStrings) {
            if (infoString.message.isEmpty()) {
                piy += height * 2 / 3;
            } else {
                graphics.setColor(new Color(255, 255, 255, 128));
                int width = fontMetrics.stringWidth(infoString.message);
                graphics.fillRect(0, piy, pix + width + 1, height);
                graphics.setColor(infoString.color);
                graphics.drawString(infoString.message, pix, piy + height - 2);

                piy += height;
            }
        }
        GraphicsUtil.setQualityDefault(graphics);
    }

    void appendSeparator() {
        append(new InfoString(""));
    }

    void append(String format, Object... args) {
        append(new InfoString(String.format(format, args)));
    }

    void append(InfoString infoString) {
        infoStrings.add(infoString);
    }

    public void setSimulationObject(SimulationObject simulationObject) {
        this.simulationObject = simulationObject;
        repaint();
    }

    public void setMapAlphaCover(int alpha) {
        mapAlphaCover = alpha;
        repaint();
    }

    public void setFontSize(int i) {
        infoFontSize = i;
    }

    public int getFontSize() {
        return infoFontSize;
    }

}
