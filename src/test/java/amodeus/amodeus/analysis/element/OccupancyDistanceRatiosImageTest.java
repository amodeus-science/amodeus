/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import amodeus.amodeus.analysis.plot.ColorDataAmodeusSpecific;
import amodeus.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class OccupancyDistanceRatiosImageTest {
    @Test
    @Ignore // won't work due to funky vscode SSH X11 forwarding
    public void test() {
        OccupancyDistanceRatiosImage image = OccupancyDistanceRatiosImage.INSTANCE;
        File dir = MultiFileTools.getDefaultWorkingDirectory();
        Tensor ratios = Transpose.of(Tensors.fromString("{{1,1,0.6,0.6,0.7},{0.5,0.5,0.1,1,1}}"));
        Tensor time = Tensors.fromString("{1,2,3,4,5}");
        ColorDataIndexed colorData = ColorDataAmodeusSpecific.COLORFUL.cyclic();

        // compute the image
        image.compute(ratios, time, colorData, dir);

        // remove it again
        new File(dir, image.FILE_PNG).delete();
    }
}
