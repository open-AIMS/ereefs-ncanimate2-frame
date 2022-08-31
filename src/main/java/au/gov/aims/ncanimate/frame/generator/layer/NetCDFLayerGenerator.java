/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.NetCDFUtils;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.VariableMetadataBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateIdBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLegendBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFTrueColourVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateNetCDFVariableBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.helper.MetadataHelper;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetable;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetableMap;
import au.gov.aims.ncanimate.commons.timetable.NetCDFMetadataFrame;
import au.gov.aims.ncanimate.commons.timetable.NetCDFMetadataSet;
import au.gov.aims.ncanimate.frame.generator.layer.edalLayer.DynamicArrowLayer;
import au.gov.aims.ncanimate.frame.generator.layer.edalLayer.TrueColourLayer;
import au.gov.aims.ncanimate.frame.generator.context.FrameGeneratorContext;
import au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.LegendGenerator;
import au.gov.aims.sld.SldUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.graphics.style.*;
import uk.ac.rdg.resc.edal.graphics.utils.ColourPalette;
import uk.ac.rdg.resc.edal.graphics.utils.PlottingDomainParams;
import uk.ac.rdg.resc.edal.graphics.utils.SimpleFeatureCatalogue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class NetCDFLayerGenerator extends AbstractLayerGenerator {
    private static final Logger LOGGER = Logger.getLogger(NetCDFLayerGenerator.class);

    private static final Color NO_DATA_COLOUR = new Color(0, true); // transparent
    private static final int DEFAULT_ARROW_SIZE = 20;
    private static final float DEFAULT_SCALE_MIN = -50.0f;
    private static final float DEFAULT_SCALE_MAX = +50.0f;
    private static final String DEFAULT_COLOUR_PALETTE_NAME = "x-Rainbow";

    private static final String NETCDF_MAGNITUDE_TYPE = "mag";
    private static final String NETCDF_DIRECTION_TYPE = "dir";

    // Size of a Float mantissa
    private static final float FLOAT_MANTISSA = 1E-6f;
    private static final float DATA_EPSILON = Float.MIN_NORMAL * 100;

    private FrameTimetableMap frameTimetableMap;
    private MetadataHelper metadataHelper;

    // Those 2 properties are used for caching
    // - Loading a dataset takes a fair amount of time
    // - If the netCDFFile do not match the file required to generate the layer, the dataset is reloaded.
    // - If the netCDFFile match, the cached dataset is used.
    private File cachedNetCDFFile;
    private SimpleFeatureCatalogue<Dataset> cachedFeatures;

    // Cached reference to the <code>MapImage</code> object used to render the data
    private MapImage mapImage;

    // Cached reference to downloaded palette (used to avoid trying downloading default colour palettes over and over)
    private static Map<String, File> downloadedPaletteMap = new HashMap<String, File>();

    private List<LegendGenerator> legendGenerators;
    private boolean dataAvailable;

    public NetCDFLayerGenerator(S3Client s3Client, DatabaseClient dbClient, FrameTimetableMap frameTimetableMap) {
        super(s3Client);

        this.frameTimetableMap = frameTimetableMap;
        this.metadataHelper = new MetadataHelper(dbClient, CacheStrategy.DISK);
        this.mapImage = new MapImage();
        this.legendGenerators = new ArrayList<LegendGenerator>();
    }

    public static String getUniqueId(String layerId) {
        return "NetCDF_" + layerId;
    }

    @Override
    public boolean isDataAvailable() {
        return this.dataAvailable;
    }

    @Override
    public String getLayerType() {
        return "NetCDF";
    }

    @Override
    public void render(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {
        // This is used in FrameGenerator to display "DATA NOT AVAILABLE" when there is no NetCDF layer containing data
        this.dataAvailable = false;

        if (this.frameTimetableMap != null && !this.frameTimetableMap.isEmpty()) {
            NcAnimateLayerBean layerConf = this.getLayerConf();
            NcAnimateIdBean layerId = layerConf.getId();
            if (layerId != null) {
                String layerIdStr = layerId.getValue();

                FrameTimetable frameTimetable = this.frameTimetableMap.get(this.getContext().getFrameDateRange());
                if (frameTimetable != null && !frameTimetable.isEmpty()) {
                    NetCDFMetadataSet netCDFMetadataSet = frameTimetable.get(layerIdStr);

                    if (netCDFMetadataSet != null && !netCDFMetadataSet.isEmpty()) {
                        this.renderFrame(
                                canvas,
                                leftScaledOffset, topScaledOffset,
                                netCDFMetadataSet.first());
                    }
                }
            }
        }
    }

    private void renderFrame(
            VectorRasterGraphics2D canvas,
            int leftScaledOffset, int topScaledOffset,
            NetCDFMetadataFrame netCDFMetadataFrame
    ) throws Exception {
        NcAnimateLayerBean layerConf = this.getLayerConf();
        NcAnimateIdBean layerId = layerConf.getId();
        if (layerId != null) {
            String layerIdStr = layerId.getValue();

            // Loop through all available files until we find one that has data for frame date
            // NOTE:
            //     All netCDFFile of the set report to have data for the frame date.
            //     If for some reason the NetCDF library refuse to render the variable,
            //     the app will try with the next file.
            //     The set of file is used to find the best available NetCDF file for the frame date.
            //     The set is very likely to contain only one file.
            //     By far the most common case scenario: the first file of the set is used.
            //
            // CACHE NOTE:
            //     The instance only cache the features of one dataset at a time to reduce memory usage.
            //     In the unlikely scenario where the set contains 2 files and the first one contains
            //     no data, the caching will be useless: the app will waste time
            //     loading and caching file 1, realising there is no data, than loading and caching file 2
            //     for each successive frame in that scenario.
            DateTime netCDFFrameDate = netCDFMetadataFrame == null ? null : netCDFMetadataFrame.getFrameDateTime();
            if (netCDFFrameDate != null) {
                NetCDFMetadataBean netCDFMetadata = netCDFMetadataFrame.getMetadata();

                // Find out where the NetCDF file needs to be downloaded
                File netCDFDir = new File(this.getContext().getNetCDFDirectory(), Utils.safeFilename(netCDFMetadata.getDefinitionId()));
                if (!Utils.prepareDirectory(netCDFDir)) {
                    throw new IllegalStateException(String.format("Could not create the NetCDF directory %s", netCDFDir));
                }

                File netCDFFile = NcAnimateUtils.getInputFile(netCDFDir, netCDFMetadata);

                // If the file doesn't exists (or can not be read), re-download it
                if (!netCDFFile.canRead()) {
                    this.beforeDownloadingInputFile(netCDFFile);
                    // Download NetCDF file to local disk (and delete the previous one if any)
                    netCDFFile = NcAnimateUtils.downloadInputFile(this.metadataHelper, this.getS3Client(), netCDFFile, netCDFMetadata);
                    this.afterDownloadingInputFile(netCDFFile);
                }

                if (netCDFFile != null && netCDFFile.canRead()) {
                    // Load the NetCDF file feature catalogue
                    // This operation can be expensive with some files, so it's better to cache the result (until we start using a different NetCDF file)
                    if (!netCDFFile.equals(this.cachedNetCDFFile)) {
                        this.cachedNetCDFFile = netCDFFile;
                        GriddedDataset dataset = NetCDFUtils.getNetCDFDataset(netCDFFile);
                        this.cachedFeatures = new SimpleFeatureCatalogue<Dataset>(dataset, false);
                    }

                    if (this.cachedFeatures != null) {
                        this.prepareInputFile(netCDFFile);

                        Map<String, VariableMetadataBean> variableMetadataMap = netCDFMetadata.getVariableMetadataBeanMap();
                        if (variableMetadataMap != null) {
                            List<Drawable> drawables = this.mapImage.getLayers();

                            VariableMetadataBean rasterVariableMetadata = this.getRasterVariableMetadata(variableMetadataMap);
                            if (rasterVariableMetadata != null) {
                                drawables.clear();
                                this.addNetCDFRasterVariable(drawables, rasterVariableMetadata);
                                NcAnimateNetCDFVariableBean variableConf = layerConf.getVariable();

                                try {
                                    PlottingDomainParams params = this.getParams(netCDFMetadataFrame);
                                    BufferedImage dataImage = this.mapImage.drawImage(params, this.cachedFeatures);
                                    dataImage.flush();

                                    canvas.createLayer(String.format("%s (raster %s)", this.getLayerTitle(), rasterVariableMetadata.getId()));
                                    canvas.drawImage(dataImage, leftScaledOffset, topScaledOffset, null);

                                    // Create a legend generator, which will be used in postRender
                                    NcAnimateLegendBean legendConf = variableConf.getLegend();
                                    if (legendConf != null && !legendConf.isHidden()) {
                                        String svgLayerName = String.format("%s (legend %s)", this.getLayerTitle(), rasterVariableMetadata.getId());
                                        LegendGenerator legendGenerator = new LegendGenerator(this.getContext(), this.getLayerContextMap(), rasterVariableMetadata, params, variableConf, svgLayerName);
                                        legendGenerator.prepare(this.mapImage, leftScaledOffset, topScaledOffset);
                                        this.legendGenerators.add(legendGenerator);
                                    }
                                    this.dataAvailable = true;
                                } catch (IncorrectDomainException ex) {
                                    LOGGER.warn(String.format("Invalid domain specified for layer %s variable %s (%s) for NetCDF file: %s", layerIdStr, variableConf.getId().getValue(), variableConf.getVariableId(), this.cachedNetCDFFile));
                                } catch (Exception ex) {
                                    LOGGER.error(String.format("Error occurred while generating the raster layer %s variable %s (%s) for NetCDF file: %s", layerIdStr, variableConf.getId().getValue(), variableConf.getVariableId(), this.cachedNetCDFFile), ex);
                                }
                            }

                            VariableMetadataBean arrowVariableMetadata = this.getArrowVariableMetadata(variableMetadataMap);
                            if (arrowVariableMetadata != null) {
                                drawables.clear();
                                this.addNetCDFArrowVariable(drawables, arrowVariableMetadata);
                                try {
                                    BufferedImage dataImage = this.mapImage.drawImage(this.getParams(netCDFMetadataFrame), this.cachedFeatures);
                                    dataImage.flush();

                                    canvas.createLayer(String.format("%s (arrows %s)", this.getLayerTitle(), arrowVariableMetadata.getId()));
                                    canvas.drawImage(dataImage, leftScaledOffset, topScaledOffset, null);
                                    this.dataAvailable = true;
                                } catch (IncorrectDomainException ex) {
                                    NcAnimateNetCDFVariableBean variableConf = layerConf.getArrowVariable();
                                    LOGGER.warn(String.format("Invalid domain specified for arrow layer %s variable %s (%s) for NetCDF file: %s", layerIdStr, variableConf.getId().getValue(), variableConf.getVariableId(), this.cachedNetCDFFile));
                                } catch(Exception ex) {
                                    NcAnimateNetCDFVariableBean variableConf = layerConf.getArrowVariable();
                                    LOGGER.error(String.format("Error occurred while generating the arrow layer %s variable %s (%s) for NetCDF file: %s", layerIdStr, variableConf.getId().getValue(), variableConf.getVariableId(), this.cachedNetCDFFile), ex);
                                }
                            }

                            List<VariableMetadataBean> trueColourVariableMetadataList = this.getTrueColourVariableMetadataList(variableMetadataMap);
                            if (trueColourVariableMetadataList != null && !trueColourVariableMetadataList.isEmpty()) {
                                drawables.clear();
                                this.addNetCDFTrueColourVariables(drawables);
                                try {
                                    PlottingDomainParams params = this.getParams(netCDFMetadataFrame);
                                    BufferedImage dataImage = this.mapImage.drawImage(params, this.cachedFeatures);
                                    dataImage.flush();

                                    canvas.createLayer(String.format("%s (true colour)", this.getLayerTitle()));
                                    canvas.drawImage(dataImage, leftScaledOffset, topScaledOffset, null);

                                    // True colour layer doesn't have legend
                                    this.dataAvailable = true;
                                } catch (IncorrectDomainException ex) {
                                    Map<String, NcAnimateNetCDFTrueColourVariableBean> variablesConf = layerConf.getTrueColourVariables();
                                    String[] variableIds = variablesConf.keySet().toArray(new String[0]);
                                    LOGGER.warn(String.format("Invalid domain specified for trueColour layer %s variables %s for NetCDF file: %s",
                                            layerIdStr, Arrays.toString(variableIds), this.cachedNetCDFFile));
                                } catch(Exception ex) {
                                    Map<String, NcAnimateNetCDFTrueColourVariableBean> variablesConf = layerConf.getTrueColourVariables();
                                    String[] variableIds = variablesConf.keySet().toArray(new String[0]);
                                    LOGGER.error(String.format("Error occurred while generating the trueColour layer %s variables %s for NetCDF file: %s",
                                            layerIdStr, Arrays.toString(variableIds), this.cachedNetCDFFile), ex);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public void beforeDownloadingInputFile(File netCDFFile) throws Exception {
        File netCDFDir = netCDFFile.getParentFile();

        // If there is old NetCDF files in the definitionId directory, delete them
        File[] downloadedFiles = netCDFDir.listFiles();
        if (downloadedFiles != null) {
            for (File oldNetCDFFile : downloadedFiles) {
                if (!oldNetCDFFile.equals(netCDFFile)) {
                    LOGGER.info(String.format("Deleting NetCDF file %s before downloading new NetCDF file %s", oldNetCDFFile, netCDFFile));
                    if (!oldNetCDFFile.delete()) {
                        throw new IllegalStateException(String.format("Could not delete the old NetCDF file %s", netCDFFile));
                    }
                }
            }
        }
    }

    // This class is intended to be overwritten in subclasses (Grib2)
    public void afterDownloadingInputFile(File netCDFFile) throws Exception {}

    // This class is intended to be overwritten in subclasses (Grib2)
    public void prepareInputFile(File netCDFFile) throws Exception {}

    private PlottingDomainParams getParams(NetCDFMetadataFrame layerContext) {
        return PlottingDomainParams.paramsForGriddedDataset(
            this.getScaledPanelWidth(), this.getScaledPanelHeight(),
            this.getBoundingBox(), this.getTargetHeight(),
            layerContext.getFrameDateTime());
    }

    public void postRender(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {
        if (this.legendGenerators != null && !this.legendGenerators.isEmpty()) {
            for (LegendGenerator legendGenerator : this.legendGenerators) {
                legendGenerator.drawLegend(canvas);
            }
            this.legendGenerators.clear();
        }
    }

    /**
     * Set the NetCDF variables we want to render in the MapImage
     */
    private void addNetCDFRasterVariable(List<Drawable> drawables, VariableMetadataBean variableMetadata) throws IOException, URISyntaxException {
        if (variableMetadata == null) {
            return;
        }

        NcAnimateLayerBean layerConf = this.getLayerConf();

        NcAnimateNetCDFVariableBean variableConf = layerConf.getVariable();
        String colourPaletteName = variableConf.getColourPaletteName();
        if (colourPaletteName == null) {
            colourPaletteName = DEFAULT_COLOUR_PALETTE_NAME;
        }
        LOGGER.debug(String.format("Variable %s (%s) colourPaletteName: %s", variableConf.getId().getValue(), variableConf.getVariableId(), colourPaletteName));

        // Download palette if needed
        // NOTE: The palette file is downloaded  in the palette directory, where the NetCDF library is looking.
        //     No extra work is required with the returned file.
        //     See:
        //         uk.ac.rdg.resc.edal.graphics.utils.ColourPalette: Use resource directory "palettes"
        //         ColourPalette.addPaletteDirectory(paletteDirectory): To add another directory
        this.getPaletteFile(colourPaletteName);

        // Generate a ColourScheme, used to generate the layer.
        // The legend is generated with a linear ColourScheme
        // when the scale is logarithmic.
        ColourScheme colourScheme =
                NetCDFLayerGenerator.getColourScheme(variableConf, variableConf.isLogarithmic());

        drawables.add(
            new RasterLayer(
                variableMetadata.getId(),
                colourScheme
            )
        );
    }

    /**
     * Return either a SegmentColourScheme or ThresholdColourScheme, depending on the existence of threshold and
     * colour band values in the variable configuration.
     * 
     * @param variableConf Variable configuration
     * @param logarithmic In case of SegmentColourScheme, determine if scale range should be logarithmic or linear
     * @return A colour scheme for the layer
     */
    public static ColourScheme getColourScheme(NcAnimateNetCDFVariableBean variableConf, Boolean logarithmic) {
        String colourPaletteName = variableConf.getColourPaletteName();
        NcAnimateLegendBean legendBean = variableConf.getLegend();
        Integer colourBands = null;
        if (legendBean != null) {
            colourBands = legendBean.getColourBandColourCount();
        }
        
        ColourScheme colourScheme;
        if (variableConf.getThresholds() != null && colourBands != null) {
            // extract colours from colour palette
            ColourPalette colourPalette = ColourPalette.fromString(colourPaletteName, colourBands);
            List<Color> colours = new ArrayList<>();
            for (int i = 0; i < colourBands; i++) {
                // bring i into range of 0 to 1
                float index = 1.0f / (colourBands - 1) * i;
                colours.add(colourPalette.getColor(index));
            }

            ArrayList<Float> thresholds = variableConf.getThresholds();
            // make sure thresholds are sorted ascending
            Collections.sort(thresholds);
            colourScheme = new ThresholdColourScheme(thresholds, colours, NO_DATA_COLOUR);
        }
        else {
            colourScheme = new SegmentColourScheme(
                    getScaleRange(variableConf, logarithmic == null ? false : logarithmic), null, null,
                    NO_DATA_COLOUR,
                    colourPaletteName,
                    colourBands == null ? 250 : colourBands
            );
        }

        return colourScheme;
    }

    private void addNetCDFArrowVariable(List<Drawable> drawables, VariableMetadataBean variableMetadata) {
        if (variableMetadata == null) {
            return;
        }

        NcAnimateLayerBean layerConf = this.getLayerConf();
        float scale = this.getContext().getRenderScale();

        VariableMetadataBean magnitudeVariableMetadata = null;
        VariableMetadataBean directionVariableMetadata = null;

        Map<String, VariableMetadataBean> childrenVariableMetadata = variableMetadata.getChildren();
        // If the arrow variable have 2 or more children, it's a Dynamic arrow (arrow that change size depending on magnitude child variable)
        if (childrenVariableMetadata != null && childrenVariableMetadata.size() >= 2) {
            for (VariableMetadataBean childVariableMetadata : childrenVariableMetadata.values()) {
                String role = childVariableMetadata.getRole();

                if (NETCDF_DIRECTION_TYPE.equalsIgnoreCase(role)) {
                    directionVariableMetadata = childVariableMetadata;
                } else if (NETCDF_MAGNITUDE_TYPE.equalsIgnoreCase(role)) {
                    magnitudeVariableMetadata = childVariableMetadata;
                }
            }
        }

        // Normal arrow variable (or vector arrow variable that fail to provide a direction)
        if (directionVariableMetadata == null) {
            directionVariableMetadata = variableMetadata;
            magnitudeVariableMetadata = null;
        }

        int arrowSize = layerConf.getArrowSize() == null ? DEFAULT_ARROW_SIZE : layerConf.getArrowSize();

        NcAnimateNetCDFVariableBean arrowVariableConf = layerConf.getArrowVariable();
        if (arrowVariableConf == null) {
            arrowVariableConf = layerConf.getVariable();
        }

        DynamicArrowLayer.ArrowStyle arrowStyle = DynamicArrowLayer.ArrowStyle.FAT_ARROW;
        String magnitudeVariableId = null;

        float min = arrowVariableConf.getScaleMin() == null ? DEFAULT_SCALE_MIN : arrowVariableConf.getScaleMin();
        float max = arrowVariableConf.getScaleMax() == null ? DEFAULT_SCALE_MAX : arrowVariableConf.getScaleMax();
        Double targetHeight = this.getContext().getTargetHeight();
        if (targetHeight == null) {
            targetHeight = 0.0;
        }
        NetCDFUtils.DataDomain magnitudeDataDomain = new NetCDFUtils.DataDomain(targetHeight, min, max);

        if (magnitudeVariableMetadata != null) {
            magnitudeVariableId = magnitudeVariableMetadata.getId();
            arrowStyle = DynamicArrowLayer.ArrowStyle.DYNA_FAT_ARROW;
        }

        drawables.add(
            new DynamicArrowLayer(
                directionVariableMetadata.getId(), arrowVariableConf.getDirectionTurns(),
                magnitudeVariableId, magnitudeDataDomain,

                arrowVariableConf.getNorthAngle(),
                NcAnimateUtils.scale(arrowSize, scale),
                Color.BLACK,
                new Color(0, true), // Transparent background
                arrowStyle
            )
        );
    }

    private void addNetCDFTrueColourVariables(List<Drawable> drawables) {
        NcAnimateLayerBean layerConf = this.getLayerConf();
        Map<String, NcAnimateNetCDFTrueColourVariableBean> trueColourVariableMap = layerConf.getTrueColourVariables();
        boolean isTrueColourLayer = trueColourVariableMap != null && !trueColourVariableMap.isEmpty();

        // True colour layers
        if (isTrueColourLayer) {
            TrueColourLayer trueColourLayer = new TrueColourLayer();

            for (NcAnimateNetCDFTrueColourVariableBean trueColourVariable : trueColourVariableMap.values()) {
                ScaleRange scaleRange = new ScaleRange(trueColourVariable.getScaleMin(), trueColourVariable.getScaleMax(), false);

                List<String> hexColours = trueColourVariable.getHexColours();
                if (hexColours != null && !hexColours.isEmpty()) {
                    Color[] colours = new Color[hexColours.size()];
                    for (int i=0; i<hexColours.size(); i++) {
                        String hexColour = hexColours.get(i);
                        colours[i] = SldUtils.parseHexColour(hexColour);
                    }

                    ColourScheme colourScheme = new SegmentColourScheme(
                            scaleRange, null, null,
                            NO_DATA_COLOUR,
                            colours, 250
                    );
                    trueColourLayer.addVariable(trueColourVariable.getVariableId(), colourScheme);
                }
            }

            drawables.add(trueColourLayer);
        }
    }

    private File getPaletteFile(String paletteName) throws URISyntaxException, IOException {
        if (paletteName == null || paletteName.isEmpty()) {
            return null;
        }

        if (paletteName.endsWith("-inv")) {
            paletteName = paletteName.substring(0, paletteName.lastIndexOf("-inv"));
        }

        if (!downloadedPaletteMap.containsKey(paletteName)) {
            File paletteFile = null;

            FrameGeneratorContext context = this.getContext();
            NcAnimateConfigBean ncAnimateConfig = context.getNcAnimateConfig();
            NcAnimateRenderBean render = ncAnimateConfig == null ? null : ncAnimateConfig.getRender();
            String paletteDirectoryUri = render == null ? null : render.getPaletteDirectoryUri();

            if (paletteDirectoryUri != null && !paletteDirectoryUri.isEmpty()) {
                URI uri = new URI(NcAnimateUtils.parseString(paletteDirectoryUri + "/" + paletteName + ".pal", context, this.getLayerContextMap()));
                File paletteDir = context.getPaletteDirectory();
                try {
                    paletteFile = NcAnimateUtils.downloadFileToDirectory(uri, this.getS3Client(), paletteDir);

                    // Register the new palette
                    // NOTE: ColourPalette.addPaletteDirectory scans the repository and adds the missing files
                    ColourPalette.addPaletteDirectory(paletteDir);
                } catch(FileNotFoundException ex) {
                    LOGGER.info(String.format("Palette file not found on S3: %s. Assuming it's a default palette.", uri));
                }
            }

            // NOTE: paletteFile is null when the colour palette file can not be found (ex: default colour palette)
            downloadedPaletteMap.put(paletteName, paletteFile);
        }

        return downloadedPaletteMap.get(paletteName);
    }



    private VariableMetadataBean getVariableMetadata(String variableType, VariableMetadataBean variableMetadata) {
        if (variableType != null && variableMetadata != null) {
            Map<String, VariableMetadataBean> childrenVariableMetadata = variableMetadata.getChildren();
            if (childrenVariableMetadata != null && childrenVariableMetadata.size() >= 2) {
                for (VariableMetadataBean childVariableMetadata : childrenVariableMetadata.values()) {
                    String role = childVariableMetadata.getRole();

                    if (variableType.equalsIgnoreCase(role)) {
                        return childVariableMetadata;
                    }
                }
            } else {
                if (NETCDF_MAGNITUDE_TYPE.equalsIgnoreCase(variableType)) {
                    return variableMetadata;
                }
            }
        }

        return null;
    }


    private VariableMetadataBean getArrowVariableMetadata(Map<String, VariableMetadataBean> variableMetadataMap) {
        NcAnimateLayerBean layerConf = this.getLayerConf();

        NcAnimateNetCDFVariableBean arrowVariableConf = layerConf.getArrowVariable();
        if (arrowVariableConf != null) {
            return variableMetadataMap.get(arrowVariableConf.getVariableId());
        }

        NcAnimateNetCDFVariableBean variableConf = layerConf.getVariable();
        if (variableConf != null) {
            // Get direction variable from the layer variable, if it's a vector variable
            String variableId = variableConf.getVariableId();
            return this.getVariableMetadata(NETCDF_DIRECTION_TYPE, variableMetadataMap.get(variableId));
        }

        return null;
    }

    private VariableMetadataBean getRasterVariableMetadata(Map<String, VariableMetadataBean> variableMetadataMap) {
        NcAnimateLayerBean layerConf = this.getLayerConf();

        NcAnimateNetCDFVariableBean variableConf = layerConf.getVariable();
        if (variableConf != null) {
            // Get magnitude variable from the layer variable, if it's a vector variable
            String variableId = variableConf.getVariableId();
            return this.getVariableMetadata(NETCDF_MAGNITUDE_TYPE, variableMetadataMap.get(variableId));
        }

        return null;
    }

    private List<VariableMetadataBean> getTrueColourVariableMetadataList(Map<String, VariableMetadataBean> variableMetadataMap) {
        List<VariableMetadataBean> trueColourVariableMetadataList = new ArrayList<VariableMetadataBean>();

        NcAnimateLayerBean layerConf = this.getLayerConf();

        Map<String, NcAnimateNetCDFTrueColourVariableBean> trueColourVariableMap = layerConf.getTrueColourVariables();
        if (trueColourVariableMap != null) {
            for (NcAnimateNetCDFTrueColourVariableBean trueColourVariable : trueColourVariableMap.values()) {
                // Get magnitude variable from the layer variable, if it's a vector variable
                String variableId = trueColourVariable.getVariableId();
                VariableMetadataBean variableMetadata =
                    this.getVariableMetadata(NETCDF_MAGNITUDE_TYPE, variableMetadataMap.get(variableId));

                if (variableMetadata != null) {
                    trueColourVariableMetadataList.add(variableMetadata);
                }
            }
        }

        return trueColourVariableMetadataList;
    }

    private static ScaleRange getScaleRange(NcAnimateNetCDFVariableBean variable, boolean logarithmic) {
        float rawMin = variable.getScaleMin() == null ? DEFAULT_SCALE_MIN : variable.getScaleMin(),
            rawMax = variable.getScaleMax() == null ? DEFAULT_SCALE_MAX : variable.getScaleMax();

        float min = Math.min(rawMin, rawMax),
            max = Math.max(rawMin, rawMax);

        float range = max - min,
            absoluteMax = Math.max(Math.abs(max), Math.abs(min));

        // Adjust max value
        if (range == 0 || range / absoluteMax <= FLOAT_MANTISSA) {
            max += FLOAT_MANTISSA * absoluteMax + DATA_EPSILON;
        }

        return new ScaleRange(min, max, logarithmic);
    }
}
