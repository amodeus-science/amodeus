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

package amodeus.amodeus.view.gheat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import amodeus.amodeus.view.gheat.gui.BlendComposite;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/** class was originally named "Tile" */
/* package */ enum TileHelper {
    ;

    public static BufferedImage generate(ColorDataIndexed colorDataIndexed, DotImage dot, //
            int zoom, int tileX, int tileY, DataPoint[] points) throws Exception {
        int expandedWidth;
        int expandedHeight;
        int x1;
        int x2;
        int y1;
        int y2;
        // Translate tile to pixel coords.
        x1 = tileX * HeatMap.SIZE;
        x2 = x1 + 255;
        y1 = tileY * HeatMap.SIZE;
        y2 = y1 + 255;
        int extraPad = dot.getWidth() * 2;
        // Expand bounds by one dot width.
        x1 = x1 - extraPad;
        x2 = x2 + extraPad;
        y1 = y1 - extraPad;
        y2 = y2 + extraPad;
        expandedWidth = x2 - x1;
        expandedHeight = y2 - y1;
        BufferedImage tile;
        if (points.length == 0) {
            tile = getEmptyTile(colorDataIndexed);
        } else {
            tile = getBlankImage(expandedHeight, expandedWidth);
            tile = addPoints(tile, dot, points);
            tile = trim(tile, dot.bufferedImage);
            tile = colorize(tile, colorDataIndexed);
        }
        return tile;
    }

    /// Takes the gray scale and applies the color scheme to it.
    private static BufferedImage colorize(BufferedImage tile, ColorDataIndexed colorDataIndexed) {
        Color tilePixelColor;
        // Color colorSchemePixel;
        for (int x = 0; x < tile.getWidth(); x++) {
            for (int y = 0; y < tile.getHeight(); y++) {
                // Get color for this intensity
                tilePixelColor = new Color(tile.getRGB(x, y));
                // Get the color of the scheme from the intensity on the tile
                // Only need to get one color in the tile, because it is grayscale, so each color will have the same intensity
                int index = tilePixelColor.getRed();
                // colorSchemePixel = new Color();
                // zoomOpacity = (int) ((((double) zoomOpacity / 255.0f) * ((double) colorSchemePixel.getAlpha() / 255.0f)) * 255f);
                Color color = colorDataIndexed.getColor(index);
                tile.setRGB(x, y, color.getRGB());
            }
        }
        return tile;
    }

    // Trim the larger tile to the correct size
    private static BufferedImage trim(BufferedImage tile, BufferedImage dot) {
        BufferedImage croppedTile = new BufferedImage(HeatMap.SIZE, HeatMap.SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = croppedTile.createGraphics();
        int adjPad = dot.getWidth() + (dot.getWidth() / 2);
        g.drawImage(tile, // Source Image
                0, 0, HeatMap.SIZE, HeatMap.SIZE, adjPad, // source x, adjusted for padded amount
                adjPad, // source y, adjusted for padded amount
                HeatMap.SIZE + adjPad, // source width
                HeatMap.SIZE + adjPad, // source height
                null);
        g.dispose();
        return croppedTile;
    }

    // Add all of the points to the tile
    private static BufferedImage addPoints(BufferedImage tile, DotImage dot, DataPoint[] points) {
        Graphics2D g = tile.createGraphics();
        g.setComposite(BlendComposite.Multiply);
        for (int i = 0; i < points.length; i++) {
            // double weight = points[i].getWeight();
            // BufferedImage src = dot; // weight != 0 ? ApplyWeightToImage(dot, weight) : dot;
            g.drawImage(dot.bufferedImageRGB, (int) (points[i].getX() + dot.getWidth()), (int) (points[i].getY() + dot.getWidth()), null);
        }
        g.dispose();
        return tile;
    }

    // Gets a blank image / canvas
    private static BufferedImage getBlankImage(int height, int width) {
        BufferedImage newImage;
        Graphics2D g;
        // Create a blank tile that is 32 bit and has an alpha
        newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = newImage.createGraphics();
        // Background must be white so the dots can blend
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, height, width);
        g.dispose();
        return newImage;
    }

    /* Empty tile with no points on it. */
    private static BufferedImage getEmptyTile(ColorDataIndexed colorDataIndexed) {
        // If we have already created the empty tile then return it
        if (Cache.hasEmptyTile(colorDataIndexed))
            return Cache.getEmptyTile(colorDataIndexed);
        // System.out.println("create empty tile: " + colorScheme);
        // Create a blank tile that is 32 bit and has an alpha
        BufferedImage tile = new BufferedImage(HeatMap.SIZE, HeatMap.SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphic = tile.createGraphics();
        // Get the first pixel of the color scheme, on the dark side
        graphic.setColor(colorDataIndexed.getColor(255));
        graphic.fillRect(0, 0, HeatMap.SIZE, HeatMap.SIZE);
        // graphic.setColor(Color.BLACK);
        // graphic.drawString("[empty]", 10, 10);
        graphic.dispose();
        // Save the newly created empty tile
        // There is a empty tile for each scheme and zoom level
        // Double check it does not already exists
        if (!Cache.hasEmptyTile(colorDataIndexed))
            Cache.putEmptyTile(colorDataIndexed, tile);
        return tile;
    }
}
