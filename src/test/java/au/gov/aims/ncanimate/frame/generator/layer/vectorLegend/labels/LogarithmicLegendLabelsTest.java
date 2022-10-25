/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels.LogarithmicLegendLabels;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.util.Extents;

import java.awt.*;

public class LogarithmicLegendLabelsTest {
    
    private static class LogarithmicLegendLabelsTestImpl extends LogarithmicLegendLabels {

        /**
         * @param nameAndRange
         * @param labelPrecision
         * @param labelMultiplier
         * @param labelOffset
         * @param majorTickMarkLength
         * @param minorTickMarkLength
         */
        public LogarithmicLegendLabelsTestImpl(Drawable.NameAndRange nameAndRange, Integer steps, Integer labelPrecision,
                                               Float labelMultiplier, Float labelOffset, Integer majorTickMarkLength,
                                               Integer minorTickMarkLength) {
            super(nameAndRange,
                    steps,
                    0.1f, 
                    0.1f, 
                    120, 
                    "Legend Title", 
                    new Font(Font.SANS_SERIF, Font.BOLD, 8), 
                    Color.BLACK, 
                    new Font(Font.SANS_SERIF, Font.PLAIN, 7), 
                    Color.BLACK, 
                    2,
                    labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength,
                    true, 
                    true);
        }
    }

    @Test
    public void testCalculateLabelValues() {
        int labelPrecision = 0;
        int majorTickMarkLength = 1;
        int minorTickMarkLength = 1;
        float labelMultiplier = 1;
        float labelOffset = 0;

        {
            float lowVal = 1F, highVal = 1000F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LogarithmicLegendLabelsTestImpl logLegendLabels = new LogarithmicLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = logLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"1", "10", "100", "1000"};

            Assert.assertArrayEquals(String.format("Log scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                    lowVal, highVal), expectedLabelValues, labelValues);
        }
        
        {
            float lowVal = 1F, highVal = 1000F;
            int steps = 8;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LogarithmicLegendLabelsTestImpl logLegendLabels = new LogarithmicLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = logLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"1", "3", "7", "19", "52", "139", "373", "1000"};

            Assert.assertArrayEquals(String.format("Log scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                    lowVal, highVal), expectedLabelValues, labelValues);
        }
        
    }
}
