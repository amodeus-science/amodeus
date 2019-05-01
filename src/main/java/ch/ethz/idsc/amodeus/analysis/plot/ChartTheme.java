/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.ui.RectangleInsets;

public class ChartTheme {

    private static StandardChartTheme getChartTheme(StandardChartTheme standardChartTheme, boolean shadow) {
        standardChartTheme.setExtraLargeFont(new Font(Font.DIALOG, Font.BOLD, 24));
        standardChartTheme.setLargeFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        standardChartTheme.setRegularFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        standardChartTheme.setSmallFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        standardChartTheme.setTitlePaint(Color.BLACK);
        standardChartTheme.setSubtitlePaint(Color.BLACK);
        standardChartTheme.setLegendBackgroundPaint(Color.WHITE);
        standardChartTheme.setLegendItemPaint(Color.BLACK);
        standardChartTheme.setChartBackgroundPaint(Color.WHITE);
        standardChartTheme.setDrawingSupplier(new DefaultDrawingSupplier());
        standardChartTheme.setPlotBackgroundPaint(Color.WHITE);
        standardChartTheme.setPlotOutlinePaint(Color.BLACK);
        standardChartTheme.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        standardChartTheme.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        standardChartTheme.setDomainGridlinePaint(Color.LIGHT_GRAY);
        standardChartTheme.setRangeGridlinePaint(Color.LIGHT_GRAY);
        standardChartTheme.setBaselinePaint(Color.BLACK);
        standardChartTheme.setCrosshairPaint(Color.BLACK);
        standardChartTheme.setAxisLabelPaint(Color.DARK_GRAY);
        standardChartTheme.setTickLabelPaint(Color.DARK_GRAY);
        standardChartTheme.setBarPainter(new StandardBarPainter());
        standardChartTheme.setXYBarPainter(new StandardXYBarPainter());
        standardChartTheme.setShadowVisible(shadow);
        standardChartTheme.setItemLabelPaint(Color.BLACK);
        standardChartTheme.setThermometerPaint(Color.WHITE);
        standardChartTheme.setWallPaint(BarRenderer3D.DEFAULT_WALL_PAINT);
        standardChartTheme.setErrorIndicatorPaint(Color.RED);
        return standardChartTheme;
    }

    public static final StandardChartTheme STANDARD = getChartTheme(new StandardChartTheme("amodeus"), false);
    public static final StandardChartTheme SHADOWS = getChartTheme(new StandardChartTheme("amodeus_shadows"), true);

    public static StandardChartTheme valueOf(String string) {
        switch (string) {
        case "STANDARD":
            return STANDARD;
        case "SHADOWS":
            return SHADOWS;
        }
        throw new IllegalArgumentException(string);
    }

    private ChartTheme() {
        // ---
    }
}
