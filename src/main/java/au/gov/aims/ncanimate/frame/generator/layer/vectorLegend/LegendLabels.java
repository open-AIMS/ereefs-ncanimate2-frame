/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend;

import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import org.apache.log4j.Logger;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class LegendLabels {
    private static final Logger LOGGER = Logger.getLogger(LegendLabels.class);

    private static final int DEFAULT_STEPS = 4;
    private static final int DEFAULT_MAJOR_TICK_MARK_LENGTH = 6;
    private static final int DEFAULT_MINOR_TICK_MARK_LENGTH = 3;

    private int width;
    private int height;

    private Drawable.NameAndRange nameAndRange;
    private boolean logarithmic;
    private float extraAmountOutOfRangeLow;
    private float extraAmountOutOfRangeHigh;
    private String[] legendTitles;

    private int labelTextPadding;

    private int steps; // Number of labels in the legend
    private Integer labelPrecision; // Number of digit to display
    private Float labelMultiplier;
    private Float labelOffset;

    private Boolean hideLowerLabel;
    private Boolean hideHigherLabel;

    private String[] labelStrArray;
    private int[] labelYPosArray;
    private int[] minorTickMarksYPosArray;

    private int titleLineHeight;
    private int labelLineHeight;

    private Font titleFont;
    private Color titleTextColour;

    private Font labelFont;
    private Color labelTextColour;

    private int majorTickMarkLength;
    private int minorTickMarkLength;

    /**
     *
     * @param nameAndRange
     * @param logarithmic
     * @param extraAmountOutOfRangeLow
     * @param extraAmountOutOfRangeHigh
     * @param componentHeight
     * @param legendTitle
     * @param titleFont
     * @param titleTextColour
     * @param labelFont
     * @param labelTextColour
     * @param labelTextPadding
     * @param steps
     * @param labelPrecision
     * @param labelMultiplier
     * @param labelOffset
     * @param majorTickMarkLength
     * @param minorTickMarkLength
     */
    public LegendLabels(
            Drawable.NameAndRange nameAndRange, Boolean logarithmic,
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh, int componentHeight,
            String legendTitle, Font titleFont, Color titleTextColour,
            Font labelFont, Color labelTextColour, int labelTextPadding, Integer steps,
            Integer labelPrecision, Float labelMultiplier, Float labelOffset,
            Integer majorTickMarkLength, Integer minorTickMarkLength,
            Boolean hideLowerLabel, Boolean hideHigherLabel) {

        this.nameAndRange = nameAndRange;
        this.logarithmic = logarithmic == null ? false : logarithmic;
        this.extraAmountOutOfRangeLow = extraAmountOutOfRangeLow;
        this.extraAmountOutOfRangeHigh = extraAmountOutOfRangeHigh;
        this.height = componentHeight;

        this.legendTitles = legendTitle == null ? null : legendTitle.split("\n");

        this.titleFont = titleFont;
        this.titleTextColour = titleTextColour;

        this.labelFont = labelFont;
        this.labelTextColour = labelTextColour;
        this.labelTextPadding = labelTextPadding;

        this.majorTickMarkLength = majorTickMarkLength == null ? DEFAULT_MAJOR_TICK_MARK_LENGTH : majorTickMarkLength;
        this.minorTickMarkLength = minorTickMarkLength == null ? DEFAULT_MINOR_TICK_MARK_LENGTH : minorTickMarkLength;

        this.steps = steps == null ? DEFAULT_STEPS : steps;
        this.labelPrecision = labelPrecision;
        this.labelMultiplier = labelMultiplier;
        this.labelOffset = labelOffset;

        this.hideLowerLabel = hideLowerLabel;
        this.hideHigherLabel = hideHigherLabel;
    }

    public void init() {
        if (this.steps < 2) {
            LOGGER.warn(String.format("Legend steps must be greater or equal to 2. Found: %d", this.steps));
            this.steps = DEFAULT_STEPS;
        }

        /*
         * Find the values to use for the labels and the minimum difference
         * between adjacent values. The latter and the maximum value are used to
         * calculate the number of significant figures required.
         */
        Float lowVal = this.nameAndRange.getScaleRange().getLow();
        Float highVal = this.nameAndRange.getScaleRange().getHigh();

        if (this.labelMultiplier != null) {
            lowVal *= this.labelMultiplier;
            highVal *= this.labelMultiplier;
        }
        if (this.labelOffset != null) {
            lowVal += this.labelOffset;
            highVal += this.labelOffset;
        }

        this.labelStrArray = LegendLabels.calculateLabelStrings(
                lowVal, highVal, this.logarithmic, this.steps, this.labelPrecision);


        /*
         * Create a temporary image so that we can get some metrics about the
         * font. We can use these to determine the size of the final image.
         */
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = tempImage.createGraphics();

        /*
         * Calculate the font size which will fit the font into the given
         * height.
         *
         * A minimum font size of 6 is set which is just about readable
         */

        FontMetrics titleFontMetrics = this.titleFont == null ? null : graphics.getFontMetrics(this.titleFont);
        FontMetrics labelFontMetrics = this.labelFont == null ? null : graphics.getFontMetrics(this.labelFont);

        // The height of a line of text
        this.titleLineHeight = titleFontMetrics == null ? 0 : titleFontMetrics.getHeight();
        this.labelLineHeight = labelFontMetrics == null ? 0 : labelFontMetrics.getHeight();

        this.labelYPosArray = calculateLabelPositions(
                this.height,
                this.extraAmountOutOfRangeLow, this.extraAmountOutOfRangeHigh,
                this.steps);

        this.minorTickMarksYPosArray = calculateMinorTickMarksPositions(this.labelYPosArray);

        int nLines = this.legendTitles == null ? 0 : this.legendTitles.length;

        /*
         * Calculate width needed for labels
         */
        int largerLabelWidth = 0;
        for (String labelStr : this.labelStrArray) {
            largerLabelWidth = Math.max(largerLabelWidth, labelFontMetrics == null ? 0 : labelFontMetrics.stringWidth(labelStr));
        }

        /*
         * Total space needed for all text
         */
        this.width = largerLabelWidth +
                2 * this.labelTextPadding +
                this.titleLineHeight * nLines;

        // Dispose of the unused graphics context.
        graphics.dispose();
    }

    protected static int[] calculateLabelPositions(
            int legendHeight,
            float extraAmountOutOfRangeLow, float extraAmountOutOfRangeHigh,
            int steps) {

        /*
         * This is how much of an offset we need so that the high/low scale
         * labels are in the right place
         */
        int outOfRangeLowOffset = (int) ((legendHeight * extraAmountOutOfRangeLow) / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));
        int outOfRangeHighOffset = (int) ((legendHeight * extraAmountOutOfRangeHigh) / (1 + extraAmountOutOfRangeLow + extraAmountOutOfRangeHigh));

        int[] labelYPosArray = new int[steps];

        // Calculate top and bottom positions
        int lowPos = legendHeight - outOfRangeLowOffset;
        int highPos = outOfRangeHighOffset;

        labelYPosArray[0] = lowPos;
        labelYPosArray[steps-1] = highPos;

        // Calculate intermediate positions
        float spaceBetweenLabels = (lowPos - highPos) / (steps - 1.0F);
        for (int i=1; i<steps-1; i++) {
            int factor = steps-(i+1);
            labelYPosArray[i] = highPos + (int) (factor * spaceBetweenLabels);
        }

        return labelYPosArray;
    }

    protected static int[] calculateMinorTickMarksPositions(int[] labelYPosArray) {
        if (labelYPosArray == null || labelYPosArray.length <= 1) {
            return new int[0];
        }

        int minorSteps = labelYPosArray.length - 1;
        int[] minorTickMarksYPosArray = new int[minorSteps];

        for (int i=0; i<minorSteps; i++) {
            minorTickMarksYPosArray[i] = (labelYPosArray[i] + labelYPosArray[i+1]) / 2;
        }

        return minorTickMarksYPosArray;
    }

    /**
     * The Edal library attempt to round the number in a cleaver way,
     *   but its algorithm doesn't work (throws NumberFormatException).
     * We spent a fair amount of time to find a better algorithm.
     *   The one we found is not perfect but does quite a good job.
     * @param lowVal The lower value in the legend.
     * @param highVal The higher value in the legend.
     * @param logarithmic True to generate a logarithmic scale.
     * @param steps The number of numbers to display on the legend. Example: 4.
     * @return An array of "steps" (example: 4) formatted strings representing the numbers
     *   in ascending order, as they needs to be written in the legend.
     *
     */
    protected static String[] calculateLabelStrings(
            float lowVal, float highVal,
            boolean logarithmic,
            int steps,
            Integer precision) {

        // Calculate the "steps" (example: 4) values of the legend
        float[] vals = new float[steps];

        if (logarithmic) {
            float logLowVal = (float)Math.log10(lowVal);
            float highLowVal = (float)Math.log10(highVal);

            for (int i = 0; i < steps; i++) {
                float logVal = logLowVal + (float) i * (highLowVal - logLowVal) / (steps - 1.0F);
                vals[i] = (float)Math.pow(10, logVal);
            }
        } else {
            for (int i = 0; i < steps; i++) {
                vals[i] = lowVal + (float) i * (highVal - lowVal) / (steps - 1.0F);
            }
        }

        if (precision == null) {
            // Calculate the precision (number of digit to display)

            // rawPrecision calculate the number of digits in the difference between the low and the high.
            // It will be negative in the cases of large number. We only want to format small number.
            int rawPrecision = (int)Math.ceil(-Math.log10(highVal - lowVal) + 2);
            // Ignore large numbers (precision is 0 in cases of large numbers)
            precision = Math.max(rawPrecision, 0);
        }

        // Stringify the labels, to the calculated precision
        String[] stringLabels = new String[steps];
        for (int i=0; i<steps; i++) {
            stringLabels[i] = LegendLabels.round(vals[i], precision);
        }

        return stringLabels;
    }

    /**
     * Round the number to the given precision, and returns a
     * String representing the formatted number.
     * @param number The float number to round.
     * @param precision The number of digits after the decimal point.
     * @return A formatted string representing the rounded number.
     */
    public static String round(float number, int precision) {
        // Math.round(float) can only return number within the bounds of an Integer.
        // Numbers outside that bounds will have no decimal values anyway
        // since the mantissa is too small to store them.
        if (number >= Integer.MAX_VALUE || number <= Integer.MIN_VALUE) {
            // Number too large, can't be round (rounding not needed anyway)
            return trimMeaninglessDigits(String.valueOf(number));
        }

        if (precision <= 0) {
            // Normal rounding
            return trimMeaninglessDigits(String.valueOf(Math.round(number)));
        }

        float shift = new Double(Math.pow(10, precision)).floatValue();
        float shiftedNumber = number * shift;

        // Number is too close to the limit, can't be rounded
        if (shiftedNumber >= Integer.MAX_VALUE || shiftedNumber <= Integer.MIN_VALUE) {
            // NOTE: I don't think this case is possible, the mantissa of the float is too small
            //   to contain that many digits.
            // I.E. This case would occur if we could (for example) store this number in a float:
            //   2147483.649
            // (Integer.MAX_VALUE = 2147483647)
            return trimMeaninglessDigits(String.valueOf(number));
        }

        return trimMeaninglessDigits(String.valueOf(Math.round(shiftedNumber) / shift));
    }

    /**
     * Remove meaningless digits.
     * 5.0 => 5
     * 0.002100 => 0.0021
     * 0.0 => 0
     * 100.00 => 100
     * @param numberStr String representing the number
     * @return numberStr without meaningless digits
     */
    private static String trimMeaninglessDigits(String numberStr) {
        // Return as-is if number string is empty, or doesn't have a decimal part
        if (numberStr == null || numberStr.isEmpty() || !numberStr.contains(".")) {
            return numberStr;
        }

        // Return as-is if it's a scientific notation (do not mess with that)
        // or anything that doesn't look like a decimal number
        //     -?      Optional negative sign
        //     [0-9]+  Integer part of the number
        //     \\.     The decimal point
        //     [0-9]*  The decimal part of the number (optional)
        // NOTE: The decimal part is optional. If the input is like "-10.", it will return "-10"
        if (!numberStr.matches("-?[0-9]+\\.[0-9]*")) {
            return numberStr;
        }

        // Remove trailing zeros (and the decimal point if needed)
        while (numberStr.contains(".") && (numberStr.endsWith("0") || numberStr.endsWith("."))) {
            // Trim the last character
            numberStr = numberStr.substring(0, numberStr.length() - 1);
        }

        return numberStr;
    }

    public void draw(VectorRasterGraphics2D canvas, int offsetX, int offsetY) {
        // Draw the legend labels (text for the scale limits)
        canvas.setColor(this.labelTextColour);
        if (this.labelFont != null) {
            canvas.setFont(this.labelFont);
        }

        /*
         * The offset needed to account for the fact that the position of text
         * refers to the position of the baseline, not the centre
         */
        int labelTextHeightOffset = this.labelLineHeight / 3; // The center is about 1/3 or the way up from the baseline

        int firstLabelIndex = 0;
        if (this.hideLowerLabel != null && this.hideLowerLabel) {
            firstLabelIndex += 1;
        }

        int lastLabelIndex = this.labelStrArray.length;
        if (this.hideHigherLabel != null && this.hideHigherLabel) {
            lastLabelIndex -= 1;
        }

        //for (int i=0; i<this.labelStrArray.length; i++) {
        for (int i=firstLabelIndex; i<lastLabelIndex; i++) {
            if (this.majorTickMarkLength > 0) {
                canvas.drawLine(
                    offsetX, this.labelYPosArray[i] + offsetY,
                    offsetX + this.majorTickMarkLength, this.labelYPosArray[i] + offsetY);
            }
            if (this.labelFont != null) {
                canvas.drawString(this.labelStrArray[i], this.labelTextPadding + offsetX, this.labelYPosArray[i] + offsetY + labelTextHeightOffset);
            }
        }

        if (this.minorTickMarkLength > 0) {
            int firstMinorTickIndex = 0;
            if (this.hideLowerLabel != null && this.hideLowerLabel) {
                firstMinorTickIndex += 1;
            }

            int lastMinorTickIndex = this.minorTickMarksYPosArray.length;
            if (this.hideHigherLabel != null && this.hideHigherLabel) {
                lastMinorTickIndex -= 1;
            }

            //for (int minorTickMarksYPos : this.minorTickMarksYPosArray) {
            for (int i = firstMinorTickIndex; i < lastMinorTickIndex; i++) {
                int minorTickMarksYPos = this.minorTickMarksYPosArray[i];
                canvas.drawLine(
                    offsetX, minorTickMarksYPos + offsetY,
                    offsetX + this.minorTickMarkLength, minorTickMarksYPos + offsetY);
            }
        }

        // Draw the legend title (on a 90 deg angle)
        if (this.titleFont != null && this.legendTitles != null) {
            canvas.setColor(this.titleTextColour);
            canvas.setFont(this.titleFont);

            AffineTransform originalTransform = canvas.getTransform();
            canvas.rotate(Math.PI / 2.0);
            // NOTE: X and Y are inverted since the canvas is rotated 90 deg.
            int offset = this.titleLineHeight * 2/3;
            for (String line : this.legendTitles) {
                canvas.drawString(
                        line,
                        offsetY,
                        -(offsetX + this.width - offset));
                offset += this.titleLineHeight;
            }
            canvas.setTransform(originalTransform);
        }
    }

    public int getWidth() {
        return this.width;
    }
}
