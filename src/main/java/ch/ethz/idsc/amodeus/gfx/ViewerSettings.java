/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.BitSet;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;

/** Class contains all settings for the AMoDeus viewer, used for exporting / importing
 * a user-defined choice of settings. */
public class ViewerSettings implements Serializable {

    public int zoom = 12;
    public Dimension dimensions = new Dimension(900, 900);
    public Coord coord = null; // gets replaced by db.getCenter() initially

    // VideoLayer
    public int fps = 25;
    public int startTime = 5;
    public int endTime = 24;

    // TilesLayer
    public String tileSourceName = MapSource.Wikimedia.name();
    public int mapAlphaCover = 192;
    public int mapGrayCover = 0;

    // VirtualNetworkLayer
    public boolean drawVNodes = true;
    public boolean drawVLinks = false;
    public VirtualNodeShader virtualNodeShader = VirtualNodeShader.None;
    public Rescaling rescaling = Rescaling.REL_02;
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

    // LinkLayer
    public boolean drawLinks = true;
    public boolean drawLabel = true; // is this really needed in the video?
    public boolean drawCoordinates = true;

    // LoadLayer
    public boolean drawLoad = false;
    public int historyLength = 4;
    public int loadScale = 5;

    // HudLayer
    public int infoFontSize = 13;

    // ClockLayer
    public boolean show = true;
    public int alpha = 255;
}
