/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels;

import au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.labels.LegendLabels;
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
        int labelCount = 4;

        int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                extraAmountOutOfRangeHigh, labelCount);
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
            int labelCount = 3;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 180, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int labelCount = 6;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 270, 210, 149, 89, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int labelCount = 8;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 287, 244, 201, 158, 115, 72, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int labelCount = 3;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 180, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int labelCount = 6;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 270, 210, 149, 89, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }

        {
            int labelCount = 8;

            int[] labelPositions = LegendLabels.calculateLabelPositions(legendHeight, extraAmountOutOfRangeLow, 
                    extraAmountOutOfRangeHigh, labelCount);
            int[] expectedPositions = new int[] { 331, 287, 244, 201, 158, 115, 72, 29 };

            Assert.assertArrayEquals("Legend label positions are wrong.",
                    expectedPositions, labelPositions);
        }
    }
}
