/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** @author onicolo 06-2018
 *         Created temporarily for running AmodScenarioVideoMaker -> to load v2 network. Copy from NetworkLoader in Amodidsc */
/* package */ enum StaticHelper {
    ;
    public static void setQualityHigh(Graphics2D graphics) {
        graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
    }
}
