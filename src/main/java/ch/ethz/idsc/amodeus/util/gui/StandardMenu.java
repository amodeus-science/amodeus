/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public abstract class StandardMenu {
    public static <Type extends StandardMenu> void bind(JButton jButton, Supplier<Type> supplier) {
        jButton.addActionListener(new ActionListener() {
            long tic = System.nanoTime();

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                long toc = System.nanoTime();
                if (500_000_000L < toc - tic) {
                    StandardMenu standardMenu = supplier.get();
                    standardMenu.jPopupMenu.addPopupMenuListener(new PopupMenuListener() {
                        @Override
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                            // System.out.println("popupMenuWillBecomeVisible");
                        }

                        @Override
                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                            // System.out.println("popupMenuWillBecomeInvisible");
                            tic = System.nanoTime();
                        }

                        @Override
                        public void popupMenuCanceled(PopupMenuEvent e) {
                            // System.out.println("popupMenuCanceled");
                            tic = System.nanoTime();
                        }
                    });
                    standardMenu.south(jButton);
                }
            }
        });
    }

    // ---
    protected abstract void design(JPopupMenu myJPopupMenu);

    private JPopupMenu jPopupMenu = new JPopupMenu();

    protected final JPopupMenu designAndGetJPopupMenu() {
        design(jPopupMenu);
        return jPopupMenu;
    }

    /** non-blocking
     * 
     * @param jComponent */
    public void south(JComponent jComponent) {
        designAndGetJPopupMenu().show(jComponent, 0, jComponent.getSize().height);
    }

    /** placement typically avoids that menu is created over mouse pointer
     * 
     * @param jComponent */
    public void southEast(JComponent jComponent) {
        designAndGetJPopupMenu().show(jComponent, jComponent.getSize().width, jComponent.getSize().height);
    }

    public void showRelative(JComponent jComponent, Rectangle rectangle) {
        designAndGetJPopupMenu().show(jComponent, rectangle.x + rectangle.width, rectangle.y);
    }

    public void atMouse(JComponent jComponent) {
        Point myMouse = DisplayHelper.getMouseLocation();
        Point myPoint = jComponent.getLocationOnScreen();
        designAndGetJPopupMenu().show(jComponent, myMouse.x - myPoint.x, myMouse.y - myPoint.y);
    }

    // does not really work :-(
    @Deprecated
    protected static void quickDemo(StandardMenu myStandardMenu) {
        // myStandardMenu.atMouse();
    }
}
