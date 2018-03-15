/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JProgressBar;

/** static functionality */
/* package */ enum Colors {
    ;
    /** JToggleButton background when selected is 184 207 229
     * selection color subtracts 24 from each RGB value */
    public static final Color SELECTION = new Color(160, 183, 205);

    /** imitates color of {@link JProgressBar} text */
    public static final Color PROGRESSBAR = new Color(99, 130, 191);

    /** background color of java native dialogs, e.g. JFileChooser
     * color can replace gradient paint of {@link JButton}s */
    public static final Color PANEL = new Color(238, 238, 238);

    /** foreground color of JLabel */
    public static final Color LABEL = new Color(51, 51, 51);

    /** background for items in menus that are selected; not Java official */
    public static final Color ACTIVE_ITEM = new Color(243, 239, 124);

    public static Color alpha000(Color myColor) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), 0);
    }

    public static Color alpha064(Color myColor) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), 64);
    }

    public static Color alpha128(Color myColor) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), 128);
    }

    public static Color alpha192(Color myColor) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), 192);
    }

    public static Color withAlpha(Color myColor, int alpha) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), alpha);
    }
}
