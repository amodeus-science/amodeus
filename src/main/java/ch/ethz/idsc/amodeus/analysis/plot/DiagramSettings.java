/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;
import java.awt.Font;

@Deprecated
public enum DiagramSettings {
    ;

    // TODO Joel Use ChartTheme instead, which is loaded in Analysis Class and valid for all ChartFactory plots!
    public static final Font FONT_TITLE = new Font("Dialog", Font.BOLD, 24);
    public static final Font FONT_AXIS = new Font("Dialog", Font.BOLD, 18);
    public static final Font FONT_TICK = new Font("Dialog", Font.PLAIN, 14);
    public static final Color COLOR_BACKGROUND_PAINT = Color.WHITE;
    public static final Color COLOR_GRIDLINE_PAINT = Color.LIGHT_GRAY;

    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

}