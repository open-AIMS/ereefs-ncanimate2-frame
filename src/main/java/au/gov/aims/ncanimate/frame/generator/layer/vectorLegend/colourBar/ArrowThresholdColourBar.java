/*
 * Copyright (c) Australian Institute of Marine Science, 2022.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.colourBar;

import au.gov.aims.ncanimate.frame.generator.layer.edalLayer.DynamicArrowLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrowThresholdColourBar implements ColourBar {

    private final DynamicArrowLayer dynamicArrowLayer;

    public ArrowThresholdColourBar(DynamicArrowLayer dynamicArrowLayer) {
        this.dynamicArrowLayer = dynamicArrowLayer;
    }

    @Override
    public BufferedImage createImage(MapImage mapImage, int scaledWidth, int scaledHeight) {
        BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

        List<Float> thresholds = new ArrayList<Float>(this.dynamicArrowLayer.getThresholds());
        Collections.sort(thresholds);

        // Add an extra item to draw the arrow for the high values
        float maxValue = this.dynamicArrowLayer.getScaleMax();
        Float extraValue = null;

        int size = thresholds.size();
        if (size > 1) {
            Float lastValue = thresholds.get(size - 1);
            Float secondLastValue = thresholds.get(size - 2);
            float delta = lastValue - secondLastValue;

            extraValue = lastValue + delta;
        }

        if (extraValue == null || extraValue > maxValue) {
            extraValue = maxValue;
        }

        thresholds.add(extraValue);
        size++;

        // 0    = North
        // PI   = South
        // PI/2 = East
        // PI/4 = North-East (45 angle)
        double radianAngle = 0;
        Graphics2D g = image.createGraphics();

        int i = scaledWidth/2;

        // Calculate the height of the box in which the arrow is drawn.
        int boxHeight = scaledHeight / size;

        int yPos = scaledHeight;
        for (Float threshold : thresholds) {

            // Calculate the yOffset, to center the arrow in it's "box"
            float arrowLength = this.dynamicArrowLayer.getScaledArrowLength(threshold);
            int yOffset = Math.round((boxHeight - arrowLength) / 2);

            if (threshold != null) {
                this.dynamicArrowLayer.renderDynamicArrowVector(
                        threshold.doubleValue(), radianAngle, i, yPos - yOffset, g);
            }

            yPos -= boxHeight;
        }

        return image;
    }
}
