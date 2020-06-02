/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer;

import java.awt.Graphics;

import amodeus.amodeus.view.gheat.gui.ColorSchemes;

public interface AmodeusHeatMap {
    void render(Graphics graphics, Tile tile, int zoom, int posx, int posy);

    void clear();

    /** Example use:
     * addPoint(coord.getX(), coord.getY()); */
    void addPoint(double x, double y);

    void setShow(boolean b);

    ColorSchemes getColorSchemes();

    void setColorSchemes(ColorSchemes cs);

    boolean getShow();

}
