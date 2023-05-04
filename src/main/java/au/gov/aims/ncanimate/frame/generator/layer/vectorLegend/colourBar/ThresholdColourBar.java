/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.colourBar;

import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ThresholdColourBar implements ColourBar {

    private final ArrayList<Float> thresholds;

    public ThresholdColourBar(ArrayList<Float> thresholds) {
        this.thresholds = thresholds;
    }

    @Override
    public BufferedImage createImage(MapImage mapImage, int scaledWidth, int scaledHeight) {
        BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        /*
         * Initialise the array to store colour values
         */
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        ArrayList<Float> thresholds = new ArrayList<>();
        // add new threshold for colours above max threshold
        thresholds.add(0, this.thresholds.get(0) + 1);
        thresholds.addAll(this.thresholds);

        int thresholdSize = thresholds.size();

        // Calculate the height of each threshold box.
        // NOTE: This value is a decimal value. It is used to determine what threshold we are at.
        float rowsPerThreshold = (float)image.getHeight() / thresholdSize;

        // Iterate over the threshold and y and x position to add the colour for each line per threshold
        ColourScheme colourScheme = this.extractColourScheme(mapImage);
        int index = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            // Determine what threshold we are at.
            int thresholdIndex = (int)(y / rowsPerThreshold);
            if (thresholdIndex >= thresholdSize) {
                // This should never happen, but float precision is not perfect (better be safe)
                thresholdIndex = thresholdSize-1;
            }
            float threshold = thresholds.get(thresholdIndex);
            for (int x = 0; x < image.getWidth(); x++) {
                pixels[index] = colourScheme.getColor(threshold).getRGB();
                index++;
            }
        }

        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        return image;
    }

    /**
     * Get the colour scheme from the map image. The map image needs to contain a raster layer.
     *
     * @param mapImage The map image with a raster layer.
     * @return The extracted colour scheme.
     */
    private ColourScheme extractColourScheme(MapImage mapImage) {
        ColourScheme colourScheme = null;

        List<Drawable> drawables = mapImage.getLayers();
        for (Drawable drawable : drawables) {
            if (drawable instanceof RasterLayer) {
                RasterLayer rasterLayer = (RasterLayer) drawable;
                colourScheme = rasterLayer.getColourScheme();
                break;
            }
        }

        if (colourScheme == null) {
            throw new IllegalArgumentException("The map image does not contain a raster layer and therefore cannot " +
                    "be used to create a ThresholdColourBar");
        }

        return colourScheme;
    }
}
