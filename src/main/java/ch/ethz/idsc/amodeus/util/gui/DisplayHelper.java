/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;

/* package */ class DisplayHelper {
    private Rectangle screen = new Rectangle();

    public DisplayHelper() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice graphicsDevice : graphicsEnvironment.getScreenDevices())
            for (GraphicsConfiguration graphicsConfiguration : graphicsDevice.getConfigurations())
                screen = screen.union(graphicsConfiguration.getBounds());
    }

    public Rectangle allVisible(int x, int y, int width, int height) {
        x = Math.max(0, Math.min(x, screen.width - width));
        y = Math.max(0, Math.min(y, screen.height - height));
        return new Rectangle(x, y, width, height);
    }

    public Rectangle allVisible(Rectangle rectangle) {
        return allVisible(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public Rectangle getScreenRectangle() {
        return screen;
    }

    @Override
    public String toString() {
        return "Display point=(" + screen.x + ", " + screen.y + ") dimension=(" + screen.width + ", " + screen.height + ")";
    }

    public static Point getMouseLocation() {
        try {
            // can test with GraphicsEnvironment.isHeadless()
            return MouseInfo.getPointerInfo().getLocation();
        } catch (Exception myException) {
            myException.printStackTrace();
        }
        return new Point();
    }
}
