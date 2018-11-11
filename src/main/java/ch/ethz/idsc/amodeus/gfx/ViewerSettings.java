package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;
import org.matsim.api.core.v01.Coord;

import java.awt.*;
import java.io.Serializable;

public class ViewerSettings implements Serializable {

    public int zoom = 12;
    public Dimension dimensions = new Dimension(900, 900);
    public Coord coord = null;  // gets replaced by db.getCenter() initially

    // VideoLayer
    public int fps = 25;
    public int startTime = 5;
    public int endTime = 24;

    // TilesLayer
    public int mapAlphaCover = 192;
    public int mapGrayCover = 0;

    // VirtualNetworkLayer
    public boolean drawVNodes = true;
    public boolean drawVLinks = false;
    public VirtualNodeShader virtualNodeShader = VirtualNodeShader.None;
    public ColorSchemes colorSchemes = ColorSchemes.Jet;
}
