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

public enum ChartTheme {
    STANDARD {
        @Override
        public StandardChartTheme getChartTheme(boolean shadow) {
            StandardChartTheme theme = new StandardChartTheme("amodeus");
            theme.setExtraLargeFont(new Font("Dialog", Font.BOLD, 24));
            theme.setLargeFont(new Font("Dialog", Font.PLAIN, 18));
            theme.setRegularFont(new Font("Dialog", Font.PLAIN, 14));
            theme.setSmallFont(new Font("Dialog", Font.PLAIN, 10));
            theme.setTitlePaint(Color.BLACK);
            theme.setSubtitlePaint(Color.BLACK);
            theme.setLegendBackgroundPaint(Color.WHITE);
            theme.setLegendItemPaint(Color.BLACK);
            theme.setChartBackgroundPaint(Color.WHITE);
            theme.setDrawingSupplier(new DefaultDrawingSupplier());
            theme.setPlotBackgroundPaint(Color.WHITE);
            theme.setPlotOutlinePaint(Color.BLACK);
            theme.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
            theme.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
            theme.setDomainGridlinePaint(Color.WHITE);
            theme.setRangeGridlinePaint(Color.WHITE);
            theme.setBaselinePaint(Color.BLACK);
            theme.setCrosshairPaint(Color.BLACK);
            theme.setAxisLabelPaint(Color.DARK_GRAY);
            theme.setTickLabelPaint(Color.DARK_GRAY);
            theme.setBarPainter(new StandardBarPainter());
            theme.setXYBarPainter(new StandardXYBarPainter());
            theme.setShadowVisible(shadow);
            theme.setItemLabelPaint(Color.BLACK);
            theme.setThermometerPaint(Color.WHITE);
            theme.setWallPaint(BarRenderer3D.DEFAULT_WALL_PAINT);
            theme.setErrorIndicatorPaint(Color.RED);
            return theme;
        }
    };

    public abstract StandardChartTheme getChartTheme(boolean shadow);
}
