/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package amodeus.amodeus.util.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public final class RowPanel {
    private GridBagLayout gridBagLayout = new GridBagLayout();
    public JPanel jPanel = new JPanel(gridBagLayout);
    private GridBagConstraints gridBagConstraints = new GridBagConstraints();

    public RowPanel() {
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1;
        jPanel.setOpaque(false);
    }

    public void add(JComponent myJComponent) {
        ++gridBagConstraints.gridy; // initially -1
        gridBagLayout.setConstraints(myJComponent, gridBagConstraints);
        jPanel.add(myJComponent);
        jPanel.repaint();
    }
}
