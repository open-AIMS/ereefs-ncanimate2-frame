/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels.LinearLegendLabels;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.util.Extents;

import java.awt.*;

public class LinearLegendLabelsTest {

    private static class LinearLegendLabelsTestImpl extends LinearLegendLabels {

        /**
         * @param nameAndRange
         * @param labelPrecision
         * @param labelMultiplier
         * @param labelOffset
         * @param majorTickMarkLength
         * @param minorTickMarkLength
         */
        public LinearLegendLabelsTestImpl(Drawable.NameAndRange nameAndRange, Integer steps, Integer labelPrecision,
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
                    true,
                    1);
        }
    }

    @Test
    public void testCalculateLabelValues() {
        Integer labelPrecision = null;
        int majorTickMarkLength = 1;
        int minorTickMarkLength = 1;
        float labelMultiplier = 1;
        float labelOffset = 0;

        {
            float lowVal = 0F, highVal = 3F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "1", "2", "3"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                    lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            float lowVal = -1F, highVal = 2F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"-1", "0", "1", "2"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            float lowVal = 100F, highVal = 400F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"100", "200", "300", "400"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            float lowVal = 0F, highVal = 1F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "0.33", "0.67", "1"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            float lowVal = -1F, highVal = 1F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"-1", "-0.33", "0.33", "1"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            float lowVal = 100F, highVal = 200F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"100", "133", "167", "200"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            // Very low to low
            // ereefs/bgc/detr_n
            float lowVal = 6.6723715E-7F, highVal = 0.7288777F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "0.243", "0.486", "0.729"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            // Very high to very high
            // ereefs/bgc/dic
            float lowVal = 22362.748F, highVal = 23601.023F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"22363", "22776", "23188", "23601"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            // Very low to very low
            // ereefs/bgc/ma_n
            float lowVal = 9.994936E-13F, highVal = 1.2253172E-4F;
            int steps = 4;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "4.1E-5", "8.2E-5", "1.23E-4"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 8;
            float lowVal = 0F, highVal = 7F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "1", "2", "3", "4", "5", "6", "7"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 8;
            float lowVal = 0F, highVal = 0.007F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "0.001", "0.002", "0.003", "0.004", "0.005", "0.006", "0.007"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 3;
            float lowVal = -2F, highVal = 2F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"-2", "0", "2"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 3;
            float lowVal = -0.0000000002F, highVal = 0.0000000002F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"-2.0E-10", "0", "2.0E-10"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 8;
            // Very low to low
            // ereefs/bgc/detr_n
            float lowVal = 6.6723715E-7F, highVal = 0.7288777F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "0.104", "0.208", "0.312", "0.417", "0.521", "0.625", "0.729"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 8;
            // Very high to very high
            // ereefs/bgc/dic
            float lowVal = 22362.748F, highVal = 23601.023F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"22363", "22540", "22717", "22893", "23070", "23247", "23424", "23601"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }

        {
            int steps = 8;
            // Very low to very low
            // ereefs/bgc/ma_n
            float lowVal = 9.994936E-13F, highVal = 1.2253172E-4F;
            Drawable.NameAndRange nameAndRange = new Drawable.NameAndRange("temp",
                    Extents.newExtent(lowVal, highVal));
            LinearLegendLabelsTestImpl linearLegendLabels = new LinearLegendLabelsTestImpl(nameAndRange,
                    steps, labelPrecision, labelMultiplier, labelOffset, majorTickMarkLength, minorTickMarkLength);

            String[] labelValues = linearLegendLabels.calculateLabelStrings();
            String[] expectedLabelValues = new String[]{"0", "1.8E-5", "3.5E-5", "5.3E-5", "7.0E-5", "8.8E-5", "1.05E-4", "1.23E-4"};

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]",
                            lowVal, highVal), expectedLabelValues, labelValues);
        }
    }

}
