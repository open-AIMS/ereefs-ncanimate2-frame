/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend;

import au.gov.aims.ereefs.bean.metadata.netcdf.ParameterBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLegendBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePaddingBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePositionBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateTextBean;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.generator.context.GeneratorContext;
import au.gov.aims.ncanimate.commons.generator.context.LayerContext;
import au.gov.aims.ncanimate.frame.generator.layer.NetCDFLayerGenerator;
import au.gov.aims.sld.SldUtils;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.utils.LegendDataGenerator;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LegendGenerator {
    private static final Color DEFAULT_LEGEND_TEXT_COLOUR = Color.BLACK;
    private static final int DEFAULT_LEGEND_TITLE_FONTSIZE = 16;
    private static final int DEFAULT_LEGEND_LABEL_FONTSIZE = 14;
    private static final int DEFAULT_LEGEND_LABEL_PADDING = 8;
    private static final Color DEFAULT_LEGEND_BACKGROUND_COLOUR = Color.WHITE;

    // Default dimensions of the legend colour band
    private static final int DEFAULT_LEGEND_COLOURBAND_WIDTH = 20;
    private static final int DEFAULT_LEGEND_COLOURBAND_HEIGHT = 300;
    private static final float DEFAULT_LEGEND_COLOURBAND_BORDER = 1;
    private static final int DEFAULT_LEGEND_PADDING = 5;

    private GeneratorContext context;
    private Map<String, LayerContext> layerContextMap;

    private VariableMetadataBean variableMetadata;
    private NcAnimateNetCDFVariableBean variableConf;
    private NcAnimateLegendBean legendConf;
    private String svgLayerName;
    private PlottingDomainParams params;


    private BufferedImage colourBarImage;

    private Color backgroundColour;
    private int posX;
    private int posY;
    private int scaledWidth;
    private int scaledHeight;
    private Color labelTextColour;
    private LegendLabels legendLabels;
    private int scaledColourBandWidth;
    private int scaledColourBandHeight;

    private int scaledRightPadding;
    private int scaledLeftPadding;
    private int scaledTopPadding;
    private int scaledBottomPadding;

    public LegendGenerator(GeneratorContext context, Map<String, LayerContext> layerContextMap, VariableMetadataBean variableMetadata, PlottingDomainParams params, NcAnimateNetCDFVariableBean variableConf, String svgLayerName) {
        this.context = context;
        this.layerContextMap = layerContextMap;
        this.variableMetadata = variableMetadata;
        this.params = params;
        this.variableConf = variableConf;
        this.legendConf = variableConf == null ? null : variableConf.getLegend();
        this.svgLayerName = svgLayerName;
    }

    /**
     * Set attributes needed to draw the legend.
     * Those are set prior to draw the legend because we want the legend to be drawn at the end, on top of everything else.
     * @param mapImage
     * @param leftScaledOffset
     * @param topScaledOffset
     */
    public void prepare(MapImage mapImage, int leftScaledOffset, int topScaledOffset) {
        NcAnimateConfigBean config = this.context == null ? null : this.context.getNcAnimateConfig();

        if (mapImage != null && config != null) {
            // If the variable is logarithmic, replace the colour scheme with a linear one,
            // to force the NetCDF library to render a linear legend.
            // The labels need to follow the logarithmic scale, not the legend graphics.
            if (this.variableConf.isLogarithmic() != null && this.variableConf.isLogarithmic()) {
                List<Drawable> drawables = mapImage.getLayers();
                for (Drawable drawable : drawables) {
                    if (drawable instanceof RasterLayer) {
                        RasterLayer rasterLayer = (RasterLayer)drawable;
                        ColourScheme linearColourScheme =
                                NetCDFLayerGenerator.getColourScheme(this.variableConf, false);
                        rasterLayer.setColourScheme(linearColourScheme);
                    }
                }
            }

            Set<Drawable.NameAndRange> fieldsWithScales = mapImage.getFieldsWithScales();

            if (fieldsWithScales.size() == 0) {
                // No data fields, no legend to render.
                return;
            }

            // Hardcoded values
            // This is the fraction of the colourbar which *gets added* as
            // out-of-range data below the minimum / maximum
            //
            // i.e. if it's 1, the result would be 1/3 below min, 1/3 in
            // range, 1/3 above max.
            float extraAmountOutOfRangeLow = 0.1f;
            float extraAmountOutOfRangeHigh = 0.1f;


            // Configuration
            NcAnimateRenderBean render = config.getRender();
            float scale = render.getScale();

            NcAnimatePaddingBean padding = this.legendConf == null ? null : this.legendConf.getPadding();
            int rightPadding = padding == null || padding.getRight() == null ? DEFAULT_LEGEND_PADDING : padding.getRight();
            int leftPadding = padding == null || padding.getLeft() == null ? DEFAULT_LEGEND_PADDING : padding.getLeft();
            int topPadding = padding == null || padding.getTop() == null ? DEFAULT_LEGEND_PADDING : padding.getTop();
            int bottomPadding = padding == null || padding.getBottom() == null ? DEFAULT_LEGEND_PADDING : padding.getBottom();

            this.scaledRightPadding = NcAnimateUtils.scale(rightPadding, scale);
            this.scaledLeftPadding = NcAnimateUtils.scale(leftPadding, scale);
            this.scaledTopPadding = NcAnimateUtils.scale(topPadding, scale);
            this.scaledBottomPadding = NcAnimateUtils.scale(bottomPadding, scale);

            Integer rawColourBandWidth = this.legendConf == null ? null : this.legendConf.getColourBandWidth();
            Integer rawColourBandHeight = this.legendConf == null ? null : this.legendConf.getColourBandHeight();
            int colourBandWidth = rawColourBandWidth == null ? DEFAULT_LEGEND_COLOURBAND_WIDTH : rawColourBandWidth;
            int colourBandHeight = rawColourBandHeight == null ? DEFAULT_LEGEND_COLOURBAND_HEIGHT : rawColourBandHeight;
            this.scaledColourBandWidth = NcAnimateUtils.scale(colourBandWidth, scale);
            this.scaledColourBandHeight = NcAnimateUtils.scale(colourBandHeight, scale);

            String backgroundColourStr = this.legendConf == null ? null : this.legendConf.getBackgroundColour();
            this.backgroundColour = backgroundColourStr == null ? DEFAULT_LEGEND_BACKGROUND_COLOUR : SldUtils.parseHexColour(backgroundColourStr);

            NcAnimateTextBean legendTitleConf = this.legendConf == null ? null : this.legendConf.getTitle();
            NcAnimateTextBean legendLabelConf = this.legendConf == null ? null : this.legendConf.getLabel();


            Color legendTitleTextColour = legendTitleConf == null ? null : SldUtils.parseHexColour(legendTitleConf.getFontColour());
            if (legendTitleTextColour == null) {
                legendTitleTextColour = LegendGenerator.DEFAULT_LEGEND_TEXT_COLOUR;
            }
            int legentTitleFontSize = legendTitleConf == null || legendTitleConf.getFontSize() == null ? DEFAULT_LEGEND_TITLE_FONTSIZE : legendTitleConf.getFontSize();
            int scaledLegendTitleFontSize = NcAnimateUtils.scale(legentTitleFontSize, scale);

            Font legendTitleFont = null;
            if (legendTitleConf != null && !legendTitleConf.isHidden()) {
                legendTitleFont = new Font(Font.SANS_SERIF, NcAnimateUtils.getFontStyle(legendTitleConf), scaledLegendTitleFontSize);
            }

            this.labelTextColour = legendLabelConf == null ? null : SldUtils.parseHexColour(legendLabelConf.getFontColour());
            if (this.labelTextColour == null) {
                this.labelTextColour = LegendGenerator.DEFAULT_LEGEND_TEXT_COLOUR;
            }
            int legendLabelFontSize = legendLabelConf == null || legendLabelConf.getFontSize() == null ? DEFAULT_LEGEND_LABEL_FONTSIZE : legendLabelConf.getFontSize();
            int scaledLegendLabelFontSize = NcAnimateUtils.scale(legendLabelFontSize, scale);

            Font legendLabelFont = null;
            if (legendLabelConf != null && !legendLabelConf.isHidden()) {
                legendLabelFont = new Font(Font.SANS_SERIF, NcAnimateUtils.getFontStyle(legendLabelConf), scaledLegendLabelFontSize);
            }

            // Case where we have a 1D colour bar
            // Get the field name and scale range.
            Drawable.NameAndRange nameAndRange = fieldsWithScales.iterator().next();

            // Inspired from
            //     uk.ac.rdg.resc.edal.graphics.style.MapImage.getLegend(...)
            // Get the data for the colourbar and draw it.
            LegendDataGenerator dataGenerator = new LegendDataGenerator(this.scaledColourBandWidth,
                    this.scaledColourBandHeight, null, extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh,
                    extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh);

            this.colourBarImage = mapImage.drawImage(dataGenerator.getPlottingDomainParams(),
                    dataGenerator.getFeatureCatalogue(null, nameAndRange));

            // Get the legend title
            List<String> legendTitles = legendTitleConf == null ? null : legendTitleConf.getText();
            // The user didn't provide a legend title. Get whatever we can find from the file metadata
            if (legendTitles == null) {
                legendTitles = new ArrayList<String>();
            }

            String defaultLegendTitle = nameAndRange.getFieldLabel();

            // Add the units
            if (this.variableMetadata != null) {
                ParameterBean variableParameter = this.variableMetadata.getParameterBean();
                if (variableParameter != null) {
                    String variableUnit = variableParameter.getUnits();
                    if (variableUnit != null && !variableUnit.isEmpty()) {
                        defaultLegendTitle += " (" + variableUnit + ")";
                    }
                }
            }
            legendTitles.add(defaultLegendTitle);

            String parsedLegendTitle = NcAnimateUtils.parseString(legendTitles, this.context, this.layerContextMap);

            // NOTE: Logarithmic colour band is a bit tricky to generate.
            //     To generate an appropriate colourband, ncAnimate needs to generate
            //     a new layer ColourScheme before rendering the legend graphics,
            //     with logarithmic flag set to false. That way, the NetCDF library
            //     generate a linear legend and ncAnimate put log scale labels next to it.
            Boolean logarithmicLegendLabels = this.variableConf.isLogarithmic();

            // Now generate the labels for this legend
            int scaledLegendLabelTextPadding = NcAnimateUtils.scale(DEFAULT_LEGEND_LABEL_PADDING, scale);
            this.legendLabels = new LegendLabels(nameAndRange, logarithmicLegendLabels,
                    extraAmountOutOfRangeLow, extraAmountOutOfRangeHigh,
                    this.scaledColourBandHeight,
                    parsedLegendTitle, legendTitleFont, legendTitleTextColour,
                    legendLabelFont, this.labelTextColour, scaledLegendLabelTextPadding,
                    this.legendConf == null ? null : this.legendConf.getSteps(),
                    this.legendConf == null ? null : this.legendConf.getLabelPrecision(),
                    this.legendConf == null ? null : this.legendConf.getLabelMultiplier(),
                    this.legendConf == null ? null : this.legendConf.getLabelOffset(),
                    this.legendConf == null ? null : this.legendConf.getMajorTickMarkLength(),
                    this.legendConf == null ? null : this.legendConf.getMinorTickMarkLength(),
                    this.legendConf == null ? null : this.legendConf.getHideLowerLabel(),
                    this.legendConf == null ? null : this.legendConf.getHideHigherLabel());
            this.legendLabels.init();

            // Now create the correctly-sized final image...
            this.scaledWidth = this.scaledColourBandWidth + this.legendLabels.getWidth() + this.scaledLeftPadding + this.scaledRightPadding;
            this.scaledHeight = this.scaledColourBandHeight + this.scaledTopPadding + this.scaledBottomPadding;


            // Determine X relative position
            int scaledRelativeX = 0;
            int scaledRelativeY = 0;

            NcAnimatePositionBean position = this.legendConf == null ? null : this.legendConf.getPosition();
            if (position != null) {
                if (position.getLeft() != null) {
                    scaledRelativeX = NcAnimateUtils.scale(position.getLeft(), scale);
                } else if (position.getRight() != null) {
                    scaledRelativeX = this.params.getWidth() - NcAnimateUtils.scale(position.getRight(), scale) - this.scaledWidth;
                }

                if (position.getTop() != null) {
                    scaledRelativeY = NcAnimateUtils.scale(position.getTop(), scale);
                } else if (position.getBottom() != null) {
                    scaledRelativeY = this.params.getHeight() - NcAnimateUtils.scale(position.getBottom(), scale) - this.scaledHeight;
                }
            }

            this.posX = leftScaledOffset + scaledRelativeX;
            this.posY = topScaledOffset + scaledRelativeY;
        }
    }

    /**
     * Draw a legend to a canvas.
     * Inspired on uk.ac.rdg.resc.edal.graphics.style.MapImage
     */
    public void drawLegend(VectorRasterGraphics2D canvas) {
        NcAnimateConfigBean config = this.context == null ? null : this.context.getNcAnimateConfig();

        if (config != null) {
            NcAnimateRenderBean render = config.getRender();
            float scale = render.getScale();

            canvas.createLayer(this.svgLayerName);

            // Draw the background rectangle (white by default)
            canvas.setColor(this.backgroundColour);
            canvas.fill(new Rectangle(this.posX, this.posY, this.scaledWidth, this.scaledHeight));

            // Add the pre-generated colour bar graphics
            canvas.drawImage(this.colourBarImage, this.posX + this.scaledLeftPadding, this.posY + this.scaledTopPadding, null);

            // Draw the colour bar border, on top of the colour bar
            // NOTE: The graphics is slightly larger than it needs to be, by 1/2 the width of the border
            canvas.setColor(this.labelTextColour);

            Stroke oldStroke = canvas.getStroke();
            canvas.setStroke(new BasicStroke(NcAnimateUtils.scale(DEFAULT_LEGEND_COLOURBAND_BORDER, scale)));
            canvas.draw(new Rectangle(this.posX + this.scaledLeftPadding, this.posY + this.scaledTopPadding, this.colourBarImage.getWidth(), this.colourBarImage.getHeight()));
            canvas.setStroke(oldStroke);

            // Draw the numbers next to the legend
            this.legendLabels.draw(canvas, this.scaledColourBandWidth + this.posX + this.scaledLeftPadding, this.posY + this.scaledTopPadding);
        }
    }
}
