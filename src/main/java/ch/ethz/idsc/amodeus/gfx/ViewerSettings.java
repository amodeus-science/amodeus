package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.GrayMapnikTileSource;
import org.matsim.api.core.v01.Coord;

import java.awt.*;
import java.io.Serializable;
import java.util.BitSet;

public class ViewerSettings implements Serializable {

    public int zoom = 12;
    public Dimension dimensions = new Dimension(900, 900);
    public Coord coord = null;  // gets replaced by db.getCenter() initially

    // VideoLayer
    public int fps = 25;
    public int startTime = 5;
    public int endTime = 24;

    // TilesLayer
    public String tileSourceName = "Mapnik";
    public int mapAlphaCover = 192;
    public int mapGrayCover = 0;

    // VirtualNetworkLayer
    public boolean drawVNodes = true;
    public boolean drawVLinks = false;
    public VirtualNodeShader virtualNodeShader = VirtualNodeShader.None;
    public ColorSchemes colorSchemes = ColorSchemes.Jet;

    // VehiclesLayer
    public BitSet bits = null; // gets replaced by meaningful value during default
    public RoboTaxiStatusColors statusColors = RoboTaxiStatusColors.Pop;
    public boolean showLocation = true;

    // RequestsLayer
    public boolean drawNumber = true;
    public boolean drawRequestDestinations = false;
    public boolean sourceShow = true;
    public ColorSchemes sourceColorSchemes = ColorSchemes.OrangeContour;
    public boolean sinkShow = false;
    public ColorSchemes sinkColorSchemes = ColorSchemes.GreenContour;
}
