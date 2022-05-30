/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend;

import org.junit.Assert;
import org.junit.Test;

public class LegendLabelsTest {

    @Test
    public void testRound() {
        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "0", LegendLabels.round(0.0F, 2));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "0.0021", LegendLabels.round(0.002100F, 6));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "1", LegendLabels.round(1.0F, 0));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "2", LegendLabels.round(2.0F, 1));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "3", LegendLabels.round(3.0F, 4));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "0.66667", LegendLabels.round(2 / 3.0F, 5));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "5", LegendLabels.round(5.000004F, 5));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "6.00001", LegendLabels.round(6.000005F, 5));

        Assert.assertEquals(
            "Wrong stringified value returned by LegendLabels.round()",
            "100", LegendLabels.round(100.0F, 3));
    }

    @Test
    public void testCalculateLabelPositions_4steps() {
        int legendHeight = 360;
        float extraAmountOutOfRangeLow = 0.1F;
        float extraAmountOutOfRangeHigh = 0.1F;
        int steps = 4;

        int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh, steps);
        int[] expectedPositions = new int[] { 331, 230, 129, 29 };

        Assert.assertArrayEquals("Legend label positions are wrong.",
                expectedPositions, labelPositions);
    }

    @Test
    public void testCalculateLabelPositions_Xsteps() {
        int legendHeight = 360;
        float extraAmountOutOfRangeLow = 0.1F;
        float extraAmountOutOfRangeHigh = 0.1F;

        {
            int steps = 3;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh, steps);
            int[] expectedPositions = new int[] { 331, 180, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int steps = 6;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh, steps);
            int[] expectedPositions = new int[] { 331, 270, 210, 149, 89, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int steps = 8;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh, steps);
            int[] expectedPositions = new int[] { 331, 287, 244, 201, 158, 115, 72, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }
    }

    /**
     * Special case for "steps = 4" since it's the value used by NcWMS.
     */
    @Test
    public void testCalculateLabelStrings_linearScale_4steps() {
        int steps = 4;
        boolean logarithmic = false;

        {
            float lowVal = 0F, highVal = 3F;
            String[] expectedLabels = new String[] { "0", "1", "2", "3" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            float lowVal = -1F, highVal = 2F;
            String[] expectedLabels = new String[] { "-1", "0", "1", "2" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            float lowVal = 100F, highVal = 400F;
            String[] expectedLabels = new String[] { "100", "200", "300", "400" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            float lowVal = 0F, highVal = 1F;
            String[] expectedLabels = new String[] { "0", "0.33", "0.67", "1" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            float lowVal = -1F, highVal = 1F;
            String[] expectedLabels = new String[] { "-1", "-0.33", "0.33", "1" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            float lowVal = 100F, highVal = 200F;
            String[] expectedLabels = new String[] { "100", "133", "167", "200" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            // Very low to low
            // ereefs/bgc/detr_n
            float lowVal = 6.6723715E-7F, highVal = 0.7288777F;
            String[] expectedLabels = new String[] { "0", "0.243", "0.486", "0.729" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.8f, %.2f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            // Very high to very high
            // ereefs/bgc/dic
            float lowVal = 22362.748F, highVal = 23601.023F;
            String[] expectedLabels = new String[] { "22363", "22776", "23188", "23601" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            // Very low to very low
            // ereefs/bgc/ma_n
            float lowVal = 9.994936E-13F, highVal = 1.2253172E-4F;
            String[] expectedLabels = new String[] { "0", "4.1E-5", "8.2E-5", "1.23E-4" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.16f, %.5f]", lowVal, highVal),
                    expectedLabels, labels);
        }
    }

    @Test
    public void testCalculateLabelStrings_linearScale_Xsteps() {
        boolean logarithmic = false;

        {
            int steps = 8;
            float lowVal = 0F, highVal = 7F;
            String[] expectedLabels = new String[] { "0", "1", "2", "3", "4", "5", "6", "7" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 8;
            float lowVal = 0F, highVal = 0.007F;
            String[] expectedLabels = new String[] { "0", "0.001", "0.002", "0.003", "0.004", "0.005", "0.006", "0.007" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 3;
            float lowVal = -2F, highVal = 2F;
            String[] expectedLabels = new String[] { "-2", "0", "2" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 3;
            float lowVal = -0.0000000002F, highVal = 0.0000000002F;
            String[] expectedLabels = new String[] { "-2.0E-10", "0", "2.0E-10" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 8;
            // Very low to low
            // ereefs/bgc/detr_n
            float lowVal = 6.6723715E-7F, highVal = 0.7288777F;
            String[] expectedLabels = new String[] { "0", "0.104", "0.208", "0.312", "0.417", "0.521", "0.625", "0.729" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.8f, %.2f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 8;
            // Very high to very high
            // ereefs/bgc/dic
            float lowVal = 22362.748F, highVal = 23601.023F;
            String[] expectedLabels = new String[] { "22363", "22540", "22717", "22893", "23070", "23247", "23424", "23601" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }

        {
            int steps = 8;
            // Very low to very low
            // ereefs/bgc/ma_n
            float lowVal = 9.994936E-13F, highVal = 1.2253172E-4F;
            String[] expectedLabels = new String[] { "0", "1.8E-5", "3.5E-5", "5.3E-5", "7.0E-5", "8.8E-5", "1.05E-4", "1.23E-4" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Linear scale legend labels are wrong. Scale values: [%.16f, %.5f]", lowVal, highVal),
                    expectedLabels, labels);
        }
    }

    @Test
    public void testCalculateLabelStrings_logScale_4steps() {
        int steps = 4;
        boolean logarithmic = true;

        {
            float lowVal = 1F, highVal = 1000F;
            String[] expectedLabels = new String[] { "1", "10", "100", "1000" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Logarithmic scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }
    }

    @Test
    public void testCalculateLabelStrings_logScale_Xsteps() {
        int steps = 8;
        boolean logarithmic = true;

        {
            float lowVal = 1F, highVal = 1000F;
            String[] expectedLabels = new String[] { "1", "3", "7", "19", "52", "139", "373", "1000" };

            String[] labels = LegendLabels.calculateLabelStrings(lowVal, highVal, logarithmic, steps, null);

            Assert.assertArrayEquals(String.format("Logarithmic scale legend labels are wrong. Scale values: [%.1f, %.1f]", lowVal, highVal),
                    expectedLabels, labels);
        }
    }
}
