/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import java.awt.*;

public class LinearLegendLabels extends LegendLabels {
    
    private static final int DEFAULT_STEPS = 4;
    private Drawable.NameAndRange nameAndRange;
    
    private int steps;
    
    /**
     *
     * @param nameAndRange
     * @param steps
     * @param extraAmountOutOfRangeLow
     * @param extraAmountOutOfRangeHigh
     * @param componentHeight
     * @param legendTitle
     * @param titleFont
     * @param titleTextColour
     * @param labelFont
     * @param labelTextColour
     * @param labelTextPadding
     * @param labelPrecision
     * @param labelMultiplier
     * @param labelOffset
     * @param majorTickMarkLength
     * @param minorTickMarkLength
     */
    public LinearLegendLabels(
            Drawable.NameAndRange nameAndRange, Integer steps, 
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh, int componentHeight,
            String legendTitle, Font titleFont, Color titleTextColour,
            Font labelFont, Color labelTextColour, int labelTextPadding,
            Integer labelPrecision, Float labelMultiplier, Float labelOffset,
            Integer majorTickMarkLength, Integer minorTickMarkLength,
            Boolean hideLowerLabel, Boolean hideHigherLabel) {
        
        super(extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh, componentHeight, legendTitle, 
                titleFont, titleTextColour, labelFont, labelTextColour, labelTextPadding, labelPrecision, 
                labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength, hideLowerLabel, 
                hideHigherLabel);

        this.nameAndRange = nameAndRange;
        this.steps = steps == null ? DEFAULT_STEPS : steps;
    }

    /**
     * Create labels from values, either between a range or from predefined thresholds.
     *
     * @return An array of formatted strings representing the numbers in ascending order.
     */
    protected String[] calculateLabelStrings() {
        /*
         * Find the values to use for the labels and the minimum difference
         * between adjacent values. The latter and the maximum value are used to
         * calculate the number of significant figures required.
         */
        Float lowVal = this.applyMultiplierAndOffset(this.nameAndRange.getScaleRange().getLow());
        Float highVal = this.applyMultiplierAndOffset(this.nameAndRange.getScaleRange().getHigh());

        // Calculate the "steps" (example: 4) values of the legend
        float[] values = new float[this.steps];
       
        for (int i = 0; i < this.steps; i++) {
            values[i] = lowVal + (float) i * (highVal - lowVal) / (this.steps - 1.0F);
        }

        return this.stringifyLabelValues(values);
    }
}
