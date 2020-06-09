/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JLabel;

import amodeus.amodeus.view.gheat.DataManager;
import amodeus.amodeus.view.gheat.HeatMap;
import amodeus.amodeus.view.gheat.PointLatLng;
import amodeus.amodeus.view.gheat.gui.ColorSchemes;
import amodeus.amodeus.view.jmapviewer.AmodeusHeatMap;
import amodeus.amodeus.view.jmapviewer.Tile;

/* package */ class AmodeusHeatMapImpl implements AmodeusHeatMap {
    private final AmodeusDataSource matsimDataSource = new AmodeusDataSource();
    private final DataManager dataManager = new DataManager(matsimDataSource);
    private final ImageObserver imageObserver = new JLabel();

    private ColorSchemes colorSchemes;
    private boolean show = true;

    public AmodeusHeatMapImpl(ColorSchemes colorSchemes) {
        this.colorSchemes = colorSchemes;
    }

    @Override
    public void render(Graphics graphics, Tile tile, int zoom, int posx, int posy) {
        if (show)
            try {
                BufferedImage img = HeatMap.getTile( //
                        dataManager, //
                        colorSchemes.colorDataIndexed, //
                        zoom, //
                        tile.getXtile(), //
                        tile.getYtile());
                graphics.drawImage(img, posx, posy, imageObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @Override
    public void clear() {
        matsimDataSource.clear();
    }

    @Override
    public void addPoint(double x, double y) {
        matsimDataSource.addPoint(new PointLatLng(x, y));
    }

    @Override
    public void setShow(boolean b) {
        show = b;
    }

    @Override
    public ColorSchemes getColorSchemes() {
        return colorSchemes;
    }

    @Override
    public void setColorSchemes(ColorSchemes cs) {
        colorSchemes = cs;
    }

    @Override
    public boolean getShow() {
        return show;
    }

}
