/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ThresholdLegendLabelsTest {

    private static class ThresholdLegendLabelsTestImpl extends ThresholdLegendLabels {

        /**
         * @param thresholds
         * @param labelPrecision
         * @param labelMultiplier
         * @param labelOffset
         * @param majorTickMarkLength
         * @param minorTickMarkLength
         */
        public ThresholdLegendLabelsTestImpl(ArrayList<Float> thresholds, Integer labelPrecision, 
                                              Float labelMultiplier, Float labelOffset, Integer majorTickMarkLength, 
                                              Integer minorTickMarkLength) {
            super(thresholds, 
                    120, 
                    "Legend Title", 
                    new Font(Font.SANS_SERIF, Font.BOLD, 8), 
                    Color.BLACK, 
                    new Font(Font.SANS_SERIF, Font.PLAIN, 7), 
                    Color.BLACK, 
                    2,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
        }
    }

    @Test
    public void testCalculateLabelStrings() {
        int majorTickMarkLength = 1;
        int minorTickMarkLength = 1;
        
        {
            ArrayList<Float> thresholds = new ArrayList<>(Arrays.asList(2f, 10f, 40f, 80f));
            int labelPrecision = 0;
            float labelMultiplier = 1;
            float labelOffset = 0;
            
            ThresholdLegendLabelsTestImpl thresholdLegendLabels = new ThresholdLegendLabelsTestImpl(thresholds,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
            String[] labelStrings = thresholdLegendLabels.calculateLabelStrings();
            String[] expectedLabelStrings = new String[]{"0", "2", "10", "40", "80", "0"};

            Assert.assertArrayEquals("Threshold legend label values are wrong.",
                    expectedLabelStrings, labelStrings);
        }
        
        {
            ArrayList<Float> thresholds = new ArrayList<>(Arrays.asList(0.0005f, 0.02f, 0.4f, 8f));
            int labelPrecision = 4;
            float labelMultiplier = 1;
            float labelOffset = 0;
            
            ThresholdLegendLabelsTestImpl thresholdLegendLabels = new ThresholdLegendLabelsTestImpl(thresholds,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
            String[] labelStrings = thresholdLegendLabels.calculateLabelStrings();
            String[] expectedLabelStrings = new String[]{"0", "5.0E-4", "0.02", "0.4", "8", "0"};

            Assert.assertArrayEquals("Threshold legend label values are wrong.",
                    expectedLabelStrings, labelStrings);
        }
        
        {
            ArrayList<Float> thresholds = new ArrayList<>(Arrays.asList(0.0005f, 0.02f, 0.4f, 0.8f));
            int labelPrecision = 2;
            float labelMultiplier = 100.0f;
            float labelOffset = 0;
            
            ThresholdLegendLabelsTestImpl thresholdLegendLabels = new ThresholdLegendLabelsTestImpl(thresholds,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
            String[] labelStrings = thresholdLegendLabels.calculateLabelStrings();
            String[] expectedLabelStrings = new String[]{"0", "0.05", "2", "40", "80", "0"};

            Assert.assertArrayEquals("Threshold legend label values are wrong.",
                    expectedLabelStrings, labelStrings);
        }

        {
            ArrayList<Float> thresholds = new ArrayList<>(Arrays.asList(0.0005f, 0.02f, 0.4f, 0.8f));
            int labelPrecision = 2;
            float labelMultiplier = 100.0f;
            float labelOffset = 5;
            
            ThresholdLegendLabelsTestImpl thresholdLegendLabels = new ThresholdLegendLabelsTestImpl(thresholds,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
            String[] labelStrings = thresholdLegendLabels.calculateLabelStrings();
            String[] expectedLabelStrings = new String[]{"0", "5.05", "7", "45", "85", "0"};

            Assert.assertArrayEquals("Threshold legend label values are wrong.",
                    expectedLabelStrings, labelStrings);
        }

        {
            ArrayList<Float> thresholds = new ArrayList<>(Arrays.asList(0.0005f, 0.02f, 0.4f, 0.8f));
            int labelPrecision = 2;
            float labelMultiplier = 100.0f;
            float labelOffset = -1;
            
            ThresholdLegendLabelsTestImpl thresholdLegendLabels = new ThresholdLegendLabelsTestImpl(thresholds,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);
            String[] labelStrings = thresholdLegendLabels.calculateLabelStrings();
            String[] expectedLabelStrings = new String[]{"0", "-0.95", "1", "39", "79", "0"};

            Assert.assertArrayEquals("Threshold legend label values are wrong.",
                    expectedLabelStrings, labelStrings);
        }
    }
}
