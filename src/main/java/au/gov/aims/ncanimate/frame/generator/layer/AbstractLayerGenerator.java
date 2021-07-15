/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimatePanelBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateRegionBean;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.generator.context.LayerContext;
import au.gov.aims.ncanimate.frame.generator.context.FrameGeneratorContext;
import au.gov.aims.sld.SldParser;
import au.gov.aims.sld.StyleSheet;
import au.gov.aims.sld.geom.Layer;
import org.apache.log4j.Logger;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractLayerGenerator {
    private static final Logger LOGGER = Logger.getLogger(AbstractLayerGenerator.class);

    private static final int DEFAULT_MAP_SCALE = 3000000;

    private S3Client s3Client;
    private FrameGeneratorContext context;

    // Map of layer context, used to replace layer placeholders in strings.
    // Key: layer id
    private Map<String, LayerContext> layerContextMap;

    private NcAnimatePanelBean panelConf;
    private NcAnimateLayerBean layerConf;
    private String layerTitlePrefix;

    // Params
    private Integer scaledPanelWidth;
    private Integer scaledPanelHeight;
    private BoundingBox boundingBox;
    private double targetHeight;

    public AbstractLayerGenerator(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean isDataAvailable() {
        return false;
    }

    /**
     * Init the generator with config that may changes from one generation to another
     * @param panelConf Configuration of the panel the layer will be drawn
     * @param layerConf Configuration of the layer itself
     * @param layerTitlePrefix Prefix to add at the beginning of the SVG layer name (panel title)
     * @param context Frame context (region, height, etc)
     * @param layerContextMap Map of layer context, used to replace layer placeholders in strings.
     */
    public void init(NcAnimatePanelBean panelConf, NcAnimateLayerBean layerConf, String layerTitlePrefix, FrameGeneratorContext context, Map<String, LayerContext> layerContextMap) {
        this.panelConf = panelConf;
        this.layerConf = layerConf;
        this.layerTitlePrefix = layerTitlePrefix;
        this.context = context;
        this.layerContextMap = layerContextMap;

        // Init the PlottingDomainParams, used for layer transformation and NetCDF rendering
        float scale = this.context.getRenderScale();
        this.scaledPanelWidth = context.getScaledPanelWidth(panelConf);
        this.scaledPanelHeight = context.getScaledPanelHeight(panelConf);

        this.boundingBox = null;
        NcAnimateRegionBean region = this.context.getRegion();
        if (region != null) {
            this.boundingBox = NcAnimateUtils.convertBoundingBox(region.getBbox());
        }

        // NOTE: Do not use the height found in context!
        //     The configuration may overwrite it for a given layer to have a fixed layer height
        this.targetHeight = 0.0;
        String targetHeightStr = this.layerConf.getTargetHeight();
        if (targetHeightStr != null) {
            List<String> targetHeightStrings = new ArrayList<String>();
            targetHeightStrings.add(targetHeightStr);
            targetHeightStrings.add("0.0");
            String parsedTargetHeightStr = NcAnimateUtils.parseString(targetHeightStrings, this.context, this.layerContextMap);
            if (parsedTargetHeightStr != null) {
                try {
                    this.targetHeight = Double.parseDouble(parsedTargetHeightStr);
                } catch(Exception ex) {
                    LOGGER.error(String.format("Invalid target height %s (parsed as %s) found in layer %s",
                            targetHeightStr, parsedTargetHeightStr, this.layerConf.getId().getValue()));
                }
            }
        }
    }

    public Map<String, LayerContext> getLayerContextMap() {
        return this.layerContextMap;
    }

    protected static String getUniqueId(String layerId, NcAnimateRegionBean regionConf) {
        return String.format("%s_%s", layerId, regionConf.getId().getValue());
    }

    public abstract void render(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception;
    public void postRender(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {}
    public abstract String getLayerType();

    public String getLayerTitle() {
        return String.format("%s %s (%s layer)", this.getLayerTitlePrefix(), this.layerConf.getId().getValue(), this.getLayerType());
    }

    public S3Client getS3Client() {
        return this.s3Client;
    }

    public FrameGeneratorContext getContext() {
        return this.context;
    }

    public NcAnimatePanelBean getPanelConf() {
        return this.panelConf;
    }

    public NcAnimateLayerBean getLayerConf() {
        return this.layerConf;
    }

    public String getLayerTitlePrefix() {
        return this.layerTitlePrefix;
    }

    public Integer getScaledPanelWidth() {
        return this.scaledPanelWidth;
    }

    public Integer getScaledPanelHeight() {
        return this.scaledPanelHeight;
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public double getTargetHeight() {
        return this.targetHeight;
    }

    public List<Layer> styleLayer(Layer layer) throws Exception {
        AffineTransform transform = this.getTransformation();

        List<Layer> styledLayers = null;
        if (layer != null) {
            float scale = this.context.getRenderScale();

            Layer scaledLayer = layer.createTransformedLayer(transform);
            File styleFile = this.getStyleFile();
            StyleSheet styleSheet = this.getStyleSheet(styleFile);
            if (styleSheet != null) {
                if (scale > 0 && scale != 1.0) {
                    styleSheet.setFontSizeRatio(scale);
                    styleSheet.setStrokeWidthRatio(scale);
                    styleSheet.setPointSizeRatio(scale);
                }

                Integer mapScale = panelConf.getMapScale();
                if (mapScale == null) {
                    mapScale = DEFAULT_MAP_SCALE;
                }

                styledLayers = styleSheet.generateStyledLayers(scaledLayer, mapScale);
            } else {
                LOGGER.error(String.format("Could not parse style file for layer %s: %s",
                        this.getLayerTitle(),
                        styleFile));
            }
        }

        return styledLayers;
    }

    /**
     * The layers need to be scaled, flipped and translate (moved)
     * @return
     */
    private AffineTransform getTransformation() {
        AffineTransform transform = new AffineTransform();

        int width = this.getScaledPanelWidth();
        int height = this.getScaledPanelHeight();

        BoundingBox bbox = this.getBoundingBox();
        HorizontalPosition lowerCorner = bbox.getLowerCorner();
        HorizontalPosition upperCorner = bbox.getUpperCorner();

        // NOTE: Transformations are apply in reverse order.

        // Scale
        // The scale does the flip at the same time (using "-" in scaleY)
        double scaleX = width / bbox.getWidth();
        double scaleY = -height / bbox.getHeight();
        transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));

        // Translate (move)
        // We need to use the "upperCorner" for Y because the map has been flipped.
        transform.concatenate(AffineTransform.getTranslateInstance(-lowerCorner.getX(), -upperCorner.getY()));

        return transform;
    }

    private StyleSheet getStyleSheet(File sld) throws Exception {
        StyleSheet styleSheet = null;
        if (sld != null && sld.canRead()) {
            try (InputStream sldStream = new FileInputStream(sld)) {
                SldParser parser = new SldParser();
                styleSheet = parser.parse(sldStream);
            }
        }

        return styleSheet;
    }

    public File getLayerFile() throws URISyntaxException, IOException {
        String uriStr = this.getLayerConf().getDatasource();

        if (uriStr == null || uriStr.isEmpty()) {
            return null;
        }

        URI uri = new URI(NcAnimateUtils.parseString(uriStr, this.context, this.layerContextMap));
        File layerDir = this.context.getLayerDirectory();
        return NcAnimateUtils.downloadFileToDirectory(uri, this.s3Client, layerDir);
    }

    public File getStyleFile() throws URISyntaxException, IOException {
        String uriStr = this.getLayerConf().getStyle();

        if (uriStr == null || uriStr.isEmpty()) {
            LOGGER.warn(String.format("Layer %s has no defined style", this.getLayerTitle()));
            return null;
        }

        URI uri = new URI(NcAnimateUtils.parseString(uriStr, this.context, this.layerContextMap));
        File styleDir = this.context.getStyleDirectory();
        File styleFile = NcAnimateUtils.downloadFileToDirectory(uri, this.s3Client, styleDir);
        if (styleFile == null || !styleFile.canRead()) {
            LOGGER.error(String.format("Could not download style URI %s to directory %s for layer %s",
                    uri,
                    styleDir,
                    this.getLayerTitle()));
        }

        return styleFile;
    }
}
