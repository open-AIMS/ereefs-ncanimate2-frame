/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class ThresholdLegendLabels extends LegendLabels {

    private ArrayList<Float> thresholds;

    /**
     *
     * @param thresholds
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
     */
    public ThresholdLegendLabels(
            ArrayList<Float> thresholds, int componentHeight,
            String legendTitle, Font titleFont, Color titleTextColour,
            Font labelFont, Color labelTextColour, int labelTextPadding,
            Integer labelPrecision, Float labelMultiplier, Float labelOffset,
            Integer majorTickMarkLength, float scale) {
        super(0, 0, componentHeight, legendTitle,
                titleFont, titleTextColour, labelFont, labelTextColour, labelTextPadding, labelPrecision,
                labelMultiplier, labelOffset, majorTickMarkLength, 0, true,
                true, scale);

        this.thresholds = thresholds;
        Collections.sort(this.thresholds);
    }

    /**
     * Create labels from values, either between a range or from predefined thresholds.
     *
     * @return An array of formatted strings representing the numbers in ascending order.
     */
    protected String[] calculateLabelStrings() {
        // Add a value at the bottom (first) and top (last) of the range, it won't be displayed but is needed to
        // position the labels correctly
        float[] values = new float[this.thresholds.size() + 2];

        for (int i=0; i<this.thresholds.size(); i++) {
            values[i + 1] = this.applyMultiplierAndOffset(this.thresholds.get(i));
        }

        return this.stringifyLabelValues(values);
    }
}
