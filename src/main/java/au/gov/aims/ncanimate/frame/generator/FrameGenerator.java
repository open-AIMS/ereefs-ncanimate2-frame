/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator;

import au.gov.aims.aws.s3.FileWrapper;
import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VerticalDomainBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateCanvasBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateIdBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePaddingBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePanelBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateTextBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePositionBean;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.layers2svg.graphics.GeoGraphicsFormat;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.generator.context.GeneratorContext;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetable;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetableMap;
import au.gov.aims.ncanimate.commons.timetable.NetCDFMetadataFrame;
import au.gov.aims.ncanimate.commons.timetable.NetCDFMetadataSet;
import au.gov.aims.ncanimate.frame.generator.layer.edalLayer.NoDataLayer;
import au.gov.aims.ncanimate.frame.generator.context.FrameGeneratorContext;
import au.gov.aims.ncanimate.commons.generator.context.LayerContext;
import au.gov.aims.ncanimate.frame.generator.layer.AbstractLayerGenerator;
import au.gov.aims.ncanimate.frame.generator.layer.CSVLayerGenerator;
import au.gov.aims.ncanimate.frame.generator.layer.GeoJSONLayerGenerator;
import au.gov.aims.ncanimate.frame.generator.layer.Grib2LayerGenerator;
import au.gov.aims.ncanimate.frame.generator.layer.NetCDFLayerGenerator;
import au.gov.aims.ncanimate.frame.generator.layer.WMSLayerGenerator;
import au.gov.aims.sld.SldUtils;
import au.gov.aims.sld.TextAlignment;
import org.apache.log4j.Logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameGenerator {
    private static final Logger LOGGER = Logger.getLogger(FrameGenerator.class);

    private static final int DEFAULT_TEXT_FONT_SIZE = 20;
    private static final int DEFAULT_TITLE_FONT_SIZE = 30;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
    private static final int DEFAULT_PANEL_BORDER_WIDTH = 2;

    // Value used to remove an int value using an overwrite (used with text position)
    private static final int NULL_VALUE = -1000000000;

    private static Map<String, AbstractLayerGenerator> layerGeneratorCache;

    private GroupFrameGenerator groupFrameGenerator;

    public FrameGenerator(GroupFrameGenerator groupFrameGenerator) {
        this.groupFrameGenerator = groupFrameGenerator;
    }

    public void generateFrame(FrameGeneratorContext context) throws Exception {
        NcAnimateConfigBean ncAnimateConfig = this.groupFrameGenerator.getNcAnimateConfig();
        if (!Utils.prepareDirectory(context.getFrameDirectory())) {
            throw new IOException(String.format("Can not create the frame directory: %s", context.getFrameDirectory()));
        }

        Map<NcAnimateRenderMapBean.MapFormat, FileWrapper> frameFileWrapperMap = context.getFrameFileWrapperMap();

        S3Client s3Client = this.groupFrameGenerator.getS3Client();

        boolean missingFrameFile = false;
        // Check if the frame files already exists (PNG, SVG, etc. for that single frame)
        for (FileWrapper frameFileWrapper : frameFileWrapperMap.values()) {
            File frameFile = frameFileWrapper.getFile();

            if (frameFile == null || !frameFile.exists() || frameFile.lastModified() < this.groupFrameGenerator.getInputLastModified()) {
                missingFrameFile = true;
            }
        }

        if (missingFrameFile) {
            LOGGER.info(String.format("Creating frame file %s", context.getFrameFileWithoutExtension()));

            NcAnimateRenderBean renderConf = ncAnimateConfig.getRender();
            NcAnimateCanvasBean canvasConf = ncAnimateConfig.getCanvas();
            NcAnimatePaddingBean paddingConf = canvasConf.getPadding();

            float scale = renderConf == null ? 1 : renderConf.getScale();

            boolean enableRasterDrawing = false,
                enableVectorDrawing = false;

            for (NcAnimateRenderMapBean.MapFormat format : frameFileWrapperMap.keySet()) {
                if (format.isRaster()) {
                    enableRasterDrawing = true;
                }
                if (format.isVector()) {
                    enableVectorDrawing = true;
                }
            }

            Map<String, NcAnimateRenderVideoBean> videoConfs = renderConf == null ? null : renderConf.getVideos();
            boolean hasVideo = videoConfs != null && !videoConfs.isEmpty();

            // Creating the canvas
            VectorRasterGraphics2D canvas = new VectorRasterGraphics2D(context.getScaledCanvasWidth(), context.getScaledCanvasHeight(), 0);
            try {
                if (!enableVectorDrawing) {
                    canvas.disableVectorGeneration();
                }
                if (!enableRasterDrawing) {
                    canvas.disableRasterGeneration();
                }
                FrameGenerator.initCanvas(canvas);


                // The library automatically discard shapes which are out of bounds.
                // Disabling this feature can be very useful when debugging.
                //canvas.setCrop(false);


                // Set the background colour
                canvas.createLayer("Background");
                FrameGenerator.setPaintColour(canvas, canvasConf.getBackgroundColour(), DEFAULT_BACKGROUND_COLOR);
                canvas.fillRect(0, 0, context.getScaledCanvasWidth(), context.getScaledCanvasHeight());

                // Add panels
                int betweenPanelScaledPadding = NcAnimateUtils.scale(NcAnimateUtils.getInt(canvasConf.getPaddingBetweenPanels()), scale);

                int panelScaledLeftOffset = NcAnimateUtils.scale(NcAnimateUtils.getInt(paddingConf.getLeft()), scale);
                int panelScaledTopOffset = NcAnimateUtils.scale(NcAnimateUtils.getInt(paddingConf.getTop()), scale);
                List<NcAnimatePanelBean> panelConfs = ncAnimateConfig.getPanels();

                Map<String, LayerContext> layerContextMap = new HashMap<String, LayerContext>();

                if (panelConfs != null) {
                    // Fill the layer context map
                    for (NcAnimatePanelBean panelConf : panelConfs) {
                        context.setPanelConfig(panelConf);

                        List<NcAnimateLayerBean> layers = panelConf.getLayers();
                        if (layers != null) {
                            for (NcAnimateLayerBean layer : layers) {
                                NcAnimateIdBean layerId = layer.getId();
                                if (layerId != null) {
                                    String layerIdStr = layerId.getValue();

                                    FrameTimetableMap frameTimetableMap = this.groupFrameGenerator.getFrameTimetableMap();
                                    FrameTimetable frameTimetable = frameTimetableMap.get(context.getFrameDateRange());
                                    if (frameTimetable != null && !frameTimetable.isEmpty()) {
                                        NetCDFMetadataSet netCDFMetadataSet = frameTimetable.get(layerIdStr);
                                        if (netCDFMetadataSet != null) {
                                            NetCDFMetadataFrame netCDFMetadataFrame = netCDFMetadataSet.first();
                                            if (netCDFMetadataFrame != null) {
                                                List<String> targetHeightStrings = new ArrayList<String>();
                                                targetHeightStrings.add(layer.getTargetHeight());
                                                targetHeightStrings.add("0.0");
                                                String parsedTargetHeightStr = NcAnimateUtils.parseString(targetHeightStrings, context);

                                                Double closestDepth = null;
                                                if (parsedTargetHeightStr != null) {
                                                    Double targetHeight = null;
                                                    try {
                                                        targetHeight = Double.parseDouble(parsedTargetHeightStr);
                                                    } catch(Exception ex) {
                                                        LOGGER.error(String.format("Invalid target height: %s", layer.getTargetHeight()), ex);
                                                    }

                                                    if (targetHeight != null) {
                                                        NcAnimateNetCDFVariableBean variable = NcAnimateConfigHelper.getMostSignificantVariable(layer);
                                                        if (variable != null) {
                                                            NetCDFMetadataBean metadata = netCDFMetadataFrame.getMetadata();
                                                            Map<String, VariableMetadataBean> variableMetadataMap = metadata.getVariableMetadataBeanMap();
                                                            if (variableMetadataMap != null) {
                                                                VariableMetadataBean variableMetadataBean = variableMetadataMap.get(variable.getVariableId());
                                                                if (variableMetadataBean != null) {
                                                                    VerticalDomainBean verticalDomainBean = variableMetadataBean.getVerticalDomainBean();
                                                                    if (verticalDomainBean != null) {
                                                                        closestDepth = verticalDomainBean.getClosestHeight(targetHeight);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                LayerContext layerContext = new LayerContext(layerIdStr, netCDFMetadataFrame, closestDepth);
                                                layerContextMap.put(layerIdStr, layerContext);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (NcAnimatePanelBean panelConf : panelConfs) {
                        context.setPanelConfig(panelConf);
                        int panelScaledWidth = NcAnimateUtils.scale(NcAnimateUtils.getInt(panelConf.getWidth()), scale);

                        NcAnimateTextBean panelTitleBean = panelConf.getTitle();
                        String panelTitleStr = NcAnimateUtils.parseString(panelTitleBean, context, layerContextMap);
                        String safePanelTitleStr = panelTitleStr == null ? "Unnamed panel" : panelTitleStr;

                        this.generateFramePanel(canvas,
                            panelScaledLeftOffset, panelScaledTopOffset,
                            panelConf, context, layerContextMap,
                            panelTitleStr, safePanelTitleStr);

                        Map<String, NcAnimateTextBean> textConfs = panelConf.getTexts();
                        if (textConfs != null) {
                            canvas.createLayer(String.format("%s texts", safePanelTitleStr));
                            for (NcAnimateTextBean textConf : textConfs.values()) {
                                if (textConf != null && !textConf.isHidden()) {
                                    this.renderPanelText(
                                        canvas, textConf,
                                        panelScaledLeftOffset, panelScaledTopOffset,
                                        panelConf,
                                        context, layerContextMap);
                                }
                            }
                        }

                        panelScaledLeftOffset += panelScaledWidth + betweenPanelScaledPadding;
                    }
                }

                // Draw texts
                canvas.createLayer("Texts");
                Map<String, NcAnimateTextBean> textConfs = canvasConf.getTexts();
                if (textConfs != null) {
                    for (NcAnimateTextBean textConf : textConfs.values()) {
                        if (textConf != null && !textConf.isHidden()) {
                            this.renderText(canvas, textConf, context.getScaledCanvasWidth(), context.getScaledCanvasHeight(), context, layerContextMap);
                        }
                    }
                }

                // Write graphic to file (generate PNG, SVG, etc)
                for (Map.Entry<NcAnimateRenderMapBean.MapFormat, FileWrapper> frameFileEntry : frameFileWrapperMap.entrySet()) {
                    NcAnimateRenderMapBean.MapFormat mapFormat = frameFileEntry.getKey();

                    GeoGraphicsFormat renderFormat = null;
                    switch(mapFormat) {
                        case PNG:
                            renderFormat = GeoGraphicsFormat.PNG;
                            break;

                        case GIF:
                            renderFormat = GeoGraphicsFormat.GIF;
                            break;

                        case JPG:
                            renderFormat = GeoGraphicsFormat.JPG;
                            break;

                        case SVG:
                            renderFormat = GeoGraphicsFormat.SVG;
                            break;

                        default:
                            LOGGER.error("Unsupported render format: " + renderFormat);
                    }

                    if (renderFormat != null) {
                        FileWrapper fileWrapper = frameFileEntry.getValue();
                        canvas.render(renderFormat, fileWrapper.getFile());

                        // Upload video frames to S3 for "download video frame" feature
                        if (hasVideo && GeneratorContext.VIDEO_FRAME_FORMAT.equals(mapFormat)) {
                            fileWrapper.uploadFile(s3Client);
                        }
                    }
                }

            } finally {
                // Free some memory - we won't need that canvas anymore.
                canvas.dispose();
            }
        }
    }

    /**
     * Method used to set anti-aliasing and other properties that
     * needs to be set on the Graphics2D.
     * This is used with the main canvas and the canvas used for caching layers.
     */
    private static void initCanvas(VectorRasterGraphics2D canvas) {
        // Smooth polygons
        canvas.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        // Smooth text
        canvas.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void setPaintColour(VectorRasterGraphics2D canvas, String colourStr, Color defaultColour) {
        Color colour = null;
        if (colourStr != null) {
            colour = SldUtils.parseHexColour(colourStr);
        }
        canvas.setPaint(colour == null ? defaultColour : colour);
    }

    private void renderText(
            VectorRasterGraphics2D canvas,
            NcAnimateTextBean textConf,
            int canvasScaledWidth,
            int canvasScaledHeight,
            FrameGeneratorContext context,
            Map<String, LayerContext> layerContextMap
    ) {

        String text = NcAnimateUtils.parseString(textConf, context, layerContextMap);
        if (text != null && !text.trim().isEmpty()) {
            float scale = context.getRenderScale();

            int rawTextFontSize = textConf.getFontSize() == null ? DEFAULT_TEXT_FONT_SIZE : textConf.getFontSize();
            int textFontSize = NcAnimateUtils.scale(rawTextFontSize, scale);
            FrameGenerator.setPaintColour(canvas, textConf.getFontColour(), DEFAULT_FOREGROUND_COLOR);
            canvas.setFont(new Font("SansSerif", NcAnimateUtils.getFontStyle(textConf), textFontSize));

            // Default: Text is centred in the middle of the frame
            TextAlignment textAlign = TextAlignment.CENTRE;
            int x = canvasScaledWidth / 2,
                y = canvasScaledHeight / 2;

            NcAnimatePositionBean positionConf = textConf.getPosition();
            if (positionConf != null) {
                if (positionConf.getRight() != null && positionConf.getRight() > NULL_VALUE) {
                    textAlign = TextAlignment.RIGHT;
                    x = canvasScaledWidth - NcAnimateUtils.scale(positionConf.getRight(), scale);
                } else if (positionConf.getLeft() != null && positionConf.getLeft() > NULL_VALUE) {
                    textAlign = TextAlignment.LEFT;
                    x = NcAnimateUtils.scale(positionConf.getLeft(), scale);
                }

                if (positionConf.getBottom() != null && positionConf.getBottom() > NULL_VALUE) {
                    y = canvasScaledHeight - NcAnimateUtils.scale(positionConf.getBottom(), scale);
                } else if (positionConf.getTop() != null && positionConf.getTop() > NULL_VALUE) {
                    y = NcAnimateUtils.scale(positionConf.getTop(), scale);
                }
            }

            canvas.drawString(text, x, y, textAlign);
        }
    }

    private void renderPanelText(
            VectorRasterGraphics2D canvas,
            NcAnimateTextBean textConf,
            int leftScaledOffset, int topScaledOffset,
            NcAnimatePanelBean panelConf,
            FrameGeneratorContext context,
            Map<String, LayerContext> layerContextMap
    ) {
        String text = NcAnimateUtils.parseString(textConf, context, layerContextMap);
        if (text != null && !text.trim().isEmpty()) {
            float scale = context.getRenderScale();

            int panelScaledWidth = context.getScaledPanelWidth(panelConf),
                panelScaledHeight = context.getScaledPanelHeight(panelConf);

            int rawTextFontSize = textConf.getFontSize() == null ? DEFAULT_TEXT_FONT_SIZE : textConf.getFontSize();
            int textFontSize = NcAnimateUtils.scale(rawTextFontSize, scale);
            FrameGenerator.setPaintColour(canvas, textConf.getFontColour(), DEFAULT_FOREGROUND_COLOR);
            canvas.setFont(new Font("SansSerif", NcAnimateUtils.getFontStyle(textConf), textFontSize));

            // Default: Text is centred in the middle of the frame
            TextAlignment textAlign = TextAlignment.CENTRE;
            int x = panelScaledWidth / 2,
                y = panelScaledHeight / 2;

            NcAnimatePositionBean positionConf = textConf.getPosition();
            if (positionConf != null) {
                if (positionConf.getRight() != null && positionConf.getRight() > NULL_VALUE) {
                    textAlign = TextAlignment.RIGHT;
                    x = panelScaledWidth - NcAnimateUtils.scale(positionConf.getRight(), scale);
                } else if (positionConf.getLeft() != null && positionConf.getLeft() > NULL_VALUE) {
                    textAlign = TextAlignment.LEFT;
                    x = NcAnimateUtils.scale(positionConf.getLeft(), scale);
                }

                if (positionConf.getBottom() != null && positionConf.getBottom() > NULL_VALUE) {
                    y = panelScaledHeight - NcAnimateUtils.scale(positionConf.getBottom(), scale);
                } else if (positionConf.getTop() != null && positionConf.getTop() > NULL_VALUE) {
                    y = NcAnimateUtils.scale(positionConf.getTop(), scale);
                }
            }

            canvas.drawString(text, leftScaledOffset + x, topScaledOffset + y, textAlign);
        }
    }

    // Expected to return a Graphic object
    private void generateFramePanel(
            VectorRasterGraphics2D canvas,
            int leftScaledOffset, int topScaledOffset,
            NcAnimatePanelBean panelConf,
            FrameGeneratorContext context,
            Map<String, LayerContext> layerContextMap,
            String panelTitleStr, String safePanelTitleStr
    ) {
        if (panelConf == null) {
            return;
        }

        float scale = context.getRenderScale();

        int panelScaledWidth = context.getScaledPanelWidth(panelConf),
            panelScaledHeight = context.getScaledPanelHeight(panelConf);

        // Draw panel background (panel canvas)
        canvas.createLayer(String.format("%s background", safePanelTitleStr));

        FrameGenerator.setPaintColour(canvas, panelConf.getBackgroundColour(), DEFAULT_BACKGROUND_COLOR);
        Rectangle2D panelRectangle = new Rectangle2D.Double(leftScaledOffset, topScaledOffset, panelScaledWidth, panelScaledHeight);

        canvas.fill(panelRectangle);


        // Add layers
        canvas.setClip(new Rectangle2D.Double(leftScaledOffset, topScaledOffset, panelScaledWidth, panelScaledHeight));
        List<NcAnimateLayerBean> layerConfs = panelConf.getLayers();
        if (layerConfs != null) {
            List<AbstractLayerGenerator> layerGenerators = new ArrayList<AbstractLayerGenerator>();
            for (NcAnimateLayerBean layerConf : layerConfs) {
                try {
                    AbstractLayerGenerator layerGenerator = this.generateFramePanelLayer(canvas, leftScaledOffset, topScaledOffset, panelConf, safePanelTitleStr, layerConf, context, layerContextMap);
                    if (layerGenerator != null) {
                        // Collect layer generator for the post render
                        layerGenerators.add(layerGenerator);
                    }
                } catch (Exception ex) {
                    LOGGER.error(String.format("Error occurred while generating the layer %s", layerConf.getId().getValue()), ex);
                }
            }

            boolean dataAvailable = false;
            for (AbstractLayerGenerator layerGenerator : layerGenerators) {
                try {
                    layerGenerator.postRender(canvas, leftScaledOffset, topScaledOffset);
                } catch (Exception ex) {
                    LOGGER.error("Error occurred while post-rendering a layer", ex);
                }

                if (layerGenerator.isDataAvailable()) {
                    dataAvailable = true;
                }
            }

            if (!dataAvailable) {
                NoDataLayer noDataLayer = new NoDataLayer(context.getNcAnimateConfig());
                noDataLayer.drawIntoImage(canvas, safePanelTitleStr,
                        panelScaledWidth, panelScaledHeight,
                        leftScaledOffset, topScaledOffset);
            }
        }
        canvas.setClip(null);

        // Draw panel border (on top of layers to hide bits that are too close to the edge)
        int rawBorderWidth = panelConf.getBorderWidth() == null ? DEFAULT_PANEL_BORDER_WIDTH : panelConf.getBorderWidth();
        if (rawBorderWidth > 0) {
            canvas.createLayer(String.format("%s border", safePanelTitleStr));

            FrameGenerator.setPaintColour(canvas, panelConf.getBorderColour(), DEFAULT_FOREGROUND_COLOR);

            int scaledBorderWidth = NcAnimateUtils.scale(rawBorderWidth, scale);
            Stroke oldStroke = canvas.getStroke();
            canvas.setStroke(new BasicStroke(scaledBorderWidth));
            canvas.draw(panelRectangle);
            canvas.setStroke(oldStroke);
        }


        // Write panel title
        NcAnimateTextBean panelTitleBean = panelConf.getTitle();
        if (panelTitleBean != null && !panelTitleBean.isHidden()) {
            canvas.createLayer(String.format("%s title", safePanelTitleStr));

            int rawFontSize = panelTitleBean.getFontSize() == null ? DEFAULT_TITLE_FONT_SIZE : panelTitleBean.getFontSize();
            int fontSize = NcAnimateUtils.scale(rawFontSize, scale);
            FrameGenerator.setPaintColour(canvas, panelTitleBean.getFontColour(), DEFAULT_FOREGROUND_COLOR);
            canvas.setFont(new Font("SansSerif", NcAnimateUtils.getFontStyle(panelTitleBean, true, false), fontSize));

            int titleX = leftScaledOffset + panelScaledWidth / 2;
            int titleY = topScaledOffset;
            TextAlignment titleTextAlign = TextAlignment.CENTRE;

            NcAnimatePositionBean titlePosition = panelTitleBean.getPosition();
            if (titlePosition != null) {
                if (titlePosition.getRight() != null && titlePosition.getRight() > NULL_VALUE) {
                    titleX = leftScaledOffset + panelScaledWidth - NcAnimateUtils.scale(titlePosition.getRight(), scale);
                    titleTextAlign = TextAlignment.RIGHT;
                } else if (titlePosition.getLeft() != null && titlePosition.getLeft() > NULL_VALUE) {
                    titleX = leftScaledOffset + NcAnimateUtils.scale(titlePosition.getLeft(), scale);
                    titleTextAlign = TextAlignment.LEFT;
                }

                if (titlePosition.getBottom() != null && titlePosition.getBottom() > NULL_VALUE) {
                    titleY = topScaledOffset + panelScaledHeight + NcAnimateUtils.scale(titlePosition.getBottom(), scale);
                } else if (titlePosition.getTop() != null && titlePosition.getTop() > NULL_VALUE) {
                    titleY = topScaledOffset - NcAnimateUtils.scale(titlePosition.getTop(), scale);
                }
            }

            if (panelTitleStr != null && !panelTitleStr.trim().isEmpty()) {
                canvas.drawString(panelTitleStr, titleX, titleY, titleTextAlign);
            }
        }
    }

    private AbstractLayerGenerator generateFramePanelLayer(
            VectorRasterGraphics2D canvas,
            int leftScaledOffset, int topScaledOffset,
            NcAnimatePanelBean panelConf, String panelTitleStr,
            NcAnimateLayerBean layerConf,
            FrameGeneratorContext context,
            Map<String, LayerContext> layerContextMap
    ) throws Exception {

        if (layerConf == null) {
            LOGGER.error("Layer configuration is null");
            return null;
        }

        NcAnimateLayerBean.LayerType layerType = layerConf.getType();
        // NOTE Layer type might be null if the configuration contains layerID which are not found in the system.
        //     The user is warned about those errors during config parsing.
        if (layerType != null) {
            NcAnimateIdBean layerId = layerConf.getId();
            if (layerId != null) {
                String layerIdStr = layerId.getValue();

                // Caching for NetCDF layer works differently
                // - NetCDF: One layerGenerator per layer ID (cache loaded NetCDF file)
                // - Others: One layerGenerator per config (cache generated output)
                int panelWidth = context.getPanelWidth(panelConf),
                    panelHeight = context.getPanelHeight(panelConf);
                String uniqueLayerId = NcAnimateLayerBean.LayerType.NETCDF.equals(layerType) || NcAnimateLayerBean.LayerType.GRIB2.equals(layerType) ?
                        layerIdStr :
                        context.getRegion().getId().getValue() + "_" + NcAnimateUtils.parseString(layerConf.toJSON().toString(), context, layerContextMap) + "_" + panelWidth + "x" + panelHeight;

                AbstractLayerGenerator layerGenerator = FrameGenerator.getCachedLayerGenerator(uniqueLayerId);

                if (layerGenerator == null) {
                    switch(layerType) {
                        case CSV:
                            layerGenerator = new CSVLayerGenerator(this.groupFrameGenerator.getS3Client());
                            break;

                        case GEOJSON:
                            layerGenerator = new GeoJSONLayerGenerator(this.groupFrameGenerator.getS3Client());
                            break;

                        case GRIB2:
                            layerGenerator = new Grib2LayerGenerator(
                                    this.groupFrameGenerator.getS3Client(),
                                    this.groupFrameGenerator.getDatabaseClient(),
                                    this.groupFrameGenerator.getFrameTimetableMap());
                            break;

                        case NETCDF:
                            layerGenerator = new NetCDFLayerGenerator(
                                    this.groupFrameGenerator.getS3Client(),
                                    this.groupFrameGenerator.getDatabaseClient(),
                                    this.groupFrameGenerator.getFrameTimetableMap());
                            break;

                        case WMS:
                            layerGenerator = new WMSLayerGenerator(this.groupFrameGenerator.getS3Client());
                            break;

                        default:
                            LOGGER.warn(String.format("Unsupported layer type: %s", layerType));
                    }

                    FrameGenerator.cacheLayerGenerator(uniqueLayerId, layerGenerator);
                }

                if (layerGenerator != null) {
                    layerGenerator.init(panelConf, layerConf, panelTitleStr, context, layerContextMap);
                    layerGenerator.render(canvas, leftScaledOffset, topScaledOffset);
                    return layerGenerator;
                }
            }
        }

        return null;
    }

    private static AbstractLayerGenerator getCachedLayerGenerator(String uniqueLayerId) {
        if (FrameGenerator.layerGeneratorCache == null) {
            return null;
        }

        return FrameGenerator.layerGeneratorCache.get(uniqueLayerId);
    }

    private static void cacheLayerGenerator(String uniqueLayerId, AbstractLayerGenerator layerGenerator) {
        if (FrameGenerator.layerGeneratorCache == null) {
            FrameGenerator.layerGeneratorCache = new HashMap<String, AbstractLayerGenerator>();
        }

        FrameGenerator.layerGeneratorCache.put(uniqueLayerId, layerGenerator);
    }

    public static void clearCache() {
        if (FrameGenerator.layerGeneratorCache != null) {
            FrameGenerator.layerGeneratorCache.clear();
            FrameGenerator.layerGeneratorCache = null;
        }
    }
}
