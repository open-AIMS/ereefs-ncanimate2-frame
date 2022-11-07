/*
 * Copyright (c) Australian Institute of Marine Science, 2022.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrowThresholdLegendLabels extends LegendLabels {

    private List<Float> thresholds;

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
     * @param scale
     */
    public ArrowThresholdLegendLabels(
            List<Float> thresholds,
            int componentHeight,
            String legendTitle, Font titleFont, Color titleTextColour,
            Font labelFont, Color labelTextColour, int labelTextPadding,
            Integer labelPrecision, Float labelMultiplier, Float labelOffset,
            Integer majorTickMarkLength, float scale) {

        super(0, 0, componentHeight, legendTitle,
                titleFont, titleTextColour, labelFont, labelTextColour, labelTextPadding, labelPrecision,
                labelMultiplier, labelOffset, majorTickMarkLength, 0, true,
                true, scale);

        this.thresholds = new ArrayList<Float>(thresholds);
        Collections.sort(this.thresholds);

        // Add an artificial lower and higher value,
        // to create room for the lower and higher arrow in the legend.
        // Those values are not displayed since "hideLowerLabel" and "hideHigherLabel" are always true.
        this.thresholds.add(0, 0f);
        this.thresholds.add(0f);
    }

    /**
     * Create labels from values, either between a range or from predefined thresholds.
     *
     * @return An array of formatted strings representing the numbers in ascending order.
     */
    protected String[] calculateLabelStrings() {
        return this.stringifyLabelValues(this.thresholds);
    }
}
