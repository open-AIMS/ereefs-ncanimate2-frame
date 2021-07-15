/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.edalLayer;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.GriddedImageLayer;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrueColourLayer extends GriddedImageLayer {
    private List<NamedColourScheme> namedColourSchemes;

    public TrueColourLayer() {
        this.namedColourSchemes = new ArrayList<NamedColourScheme>();
    }

    public TrueColourLayer(String dataFieldName, ColourScheme colourScheme) {
        this();
        this.namedColourSchemes.add(new NamedColourScheme(dataFieldName, colourScheme));
    }

    public void addVariable(String dataFieldName, ColourScheme colourScheme) {
        this.namedColourSchemes.add(new NamedColourScheme(dataFieldName, colourScheme));
    }

    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {

        /*
         * Initialise the array to store colour values
         * NOTE: "int" is 32 bits
         *   BufferedImage pixels are 8 bits (range from [0, 255]) per colour, including alpha
         *   We can only store 16,777,216 variable in a "int" array without overflow.
         *   "int" is plenty of space to store the added up values of each pixels.
         */
        int nbPixels = image.getWidth() * image.getHeight();
        int[] redPixelsSum = new int[nbPixels];
        int[] greenPixelsSum = new int[nbPixels];
        int[] bluePixelsSum = new int[nbPixels];
        int[] alphaPixelsSum = new int[nbPixels];

        // Initialise the image - make it all black, transparent
        int index;
        for (index = 0; index < nbPixels; index++) {
            redPixelsSum[index] = 0;
            greenPixelsSum[index] = 0;
            bluePixelsSum[index] = 0;
            alphaPixelsSum[index] = 0;
        }

        for (NamedColourScheme namedColourScheme : this.namedColourSchemes) {
            /*
             * Extract the data from the catalogue
             */
            Array2D<Number> values = dataReader.getDataForLayerName(namedColourScheme.getDataFieldName());

            /*
             * The iterator iterates over the x-dimension first, which is the same
             * convention as expected for the colour-values array in image.setRGB
             * below
             */
            index = 0;
            for (Number value : values) {
                Color pixelColour = namedColourScheme.getColourScheme().getColor(value);
                redPixelsSum[index]   += pixelColour.getRed();
                greenPixelsSum[index] += pixelColour.getGreen();
                bluePixelsSum[index]  += pixelColour.getBlue();
                alphaPixelsSum[index] += pixelColour.getAlpha();
                index++;
            }
        }

        // Calculate the mean for each pixels and convert it to int
        int[] pixels = new int[nbPixels];
        float[] colourScalingValues = this.getColourScalingValues();
        for (index = 0; index < nbPixels; index++) {
            Color pixelColour = new Color(
                (int)(redPixelsSum[index] / colourScalingValues[0]),
                (int)(greenPixelsSum[index] / colourScalingValues[1]),
                (int)(bluePixelsSum[index] / colourScalingValues[2]),
                (int)(alphaPixelsSum[index] / colourScalingValues[3])
            );
            pixels[index] = pixelColour.getRGB();
        }

        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    }

    private float[] getColourScalingValues() {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        for (NamedColourScheme namedColourScheme : this.namedColourSchemes) {
            ColourScheme colourScheme = namedColourScheme.getColourScheme();
            Color maxColour = colourScheme.getColor(colourScheme.getScaleMax());
            totalRed += maxColour.getRed();
            totalGreen += maxColour.getGreen();
            totalBlue += maxColour.getBlue();
        }

        int totalMax = Math.max(Math.max(totalRed, totalGreen), totalBlue);
        float maxColourScalingValue = totalMax / 255f;

        return new float[]{maxColourScalingValue, maxColourScalingValue, maxColourScalingValue, this.namedColourSchemes.size()};
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<NameAndRange>();
        for (NamedColourScheme namedColourScheme : this.namedColourSchemes) {
            ret.add(
                new NameAndRange(
                    namedColourScheme.getDataFieldName(),
                    Extents.newExtent(
                        namedColourScheme.getColourScheme().getScaleMin(),
                        namedColourScheme.getColourScheme().getScaleMax()
                    )
                )
            );
        }
        return ret;
    }

    @Override
    public MetadataFilter getMetadataFilter() {
        return new MetadataFilter() {
            @Override
            public boolean supportsMetadata(VariableMetadata metadata) {
                if (metadata.getParameter().getUnits().equalsIgnoreCase("degrees")) {
                    /*
                     * We want to exclude directional fields - they look
                     * terrible plotted as rasters, and we already have
                     * ArrowLayers for that...
                     */
                    return false;
                }
                if (metadata.getParameter().getCategories() != null) {
                    /*
                     * If we have categorical data we only want to support it if
                     * we're using a MappedColourScheme
                     */
                    return false;
                }
                return true;
            }
        };
    }

    private static class NamedColourScheme {
        private String dataFieldName;
        private ColourScheme colourScheme;

        public NamedColourScheme(String dataFieldName, ColourScheme colourScheme) {
            this.dataFieldName = dataFieldName;
            this.colourScheme = colourScheme;
        }

        public String getDataFieldName() {
            return this.dataFieldName;
        }

        public ColourScheme getColourScheme() {
            return this.colourScheme;
        }
    }
}
