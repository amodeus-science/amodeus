/* Copyright (c) 2014 Varun Pant
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * original code retrieved from
 * https://github.com/varunpant/GHEAT-JAVA
 *
 * The code was modified by the IDSC-Frazzoli team at the
 * Institute for Dynamic Systems and Control of ETH Zurich 
 * for use in the amodeus library, 2017-2018. */

package amodeus.amodeus.view.gheat.gui;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/* package */ final class BlendingContext implements CompositeContext {
    private final BlendInterface blender;
    private final BlendComposite composite;

    BlendingContext(BlendComposite composite) {
        this.composite = composite;
        this.blender = Blender.of(composite);
    }

    @Override
    public void dispose() {
        // ---
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        float alpha = composite.getAlpha();
        int[] result = new int[4];
        int[] srcPixel = new int[4];
        int[] dstPixel = new int[4];
        int[] srcPixels = new int[width];
        int[] dstPixels = new int[width];
        for (int y = 0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstPixels);
            for (int x = 0; x < width; x++) {
                // pixels are stored as INT_ARGB
                // our arrays are [R, G, B, A]
                int pixel = srcPixels[x];
                srcPixel[0] = (pixel >> 16) & 0xFF;
                srcPixel[1] = (pixel >> 8) & 0xFF;
                srcPixel[2] = (pixel) & 0xFF;
                srcPixel[3] = (pixel >> 24) & 0xFF;
                pixel = dstPixels[x];
                dstPixel[0] = (pixel >> 16) & 0xFF;
                dstPixel[1] = (pixel >> 8) & 0xFF;
                dstPixel[2] = (pixel) & 0xFF;
                dstPixel[3] = (pixel >> 24) & 0xFF;
                blender.blend(srcPixel, dstPixel, result);
                // mixes the result with the opacity
                dstPixels[x] = ((int) (dstPixel[3] + (result[3] - dstPixel[3]) * alpha) & 0xFF) << 24 | ((int) (dstPixel[0] + (result[0] - dstPixel[0]) * alpha) & 0xFF) << 16
                        | ((int) (dstPixel[1] + (result[1] - dstPixel[1]) * alpha) & 0xFF) << 8 | (int) (dstPixel[2] + (result[2] - dstPixel[2]) * alpha) & 0xFF;
            }
            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }
}
