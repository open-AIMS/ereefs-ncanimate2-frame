/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateRegionBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.timetable.DateTimeRange;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetableMap;
import au.gov.aims.ncanimate.frame.generator.context.FrameGeneratorContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GroupFrameGenerator {
    private static final Logger LOGGER = Logger.getLogger(GroupFrameGenerator.class);

    private DatabaseClient dbClient;
    private S3Client s3Client;

    // Beans (configuration)
    private NcAnimateConfigBean ncAnimateConfig;
    private DateTimeRange productDateRange;

    private FrameTimetableMap frameTimetableMap; // Complex structure containing all timestamps available for each variables from each input files

    private long inputLastModified; // Used to figure out if a frame is outdated

    private FrameGenerator frameGenerator;

    private String regionId;

    public GroupFrameGenerator(
            DatabaseClient dbClient,
            S3Client s3Client,
            NcAnimateConfigBean ncAnimateConfig,
            DateTimeRange productDateRange,
            FrameTimetableMap frameTimetableMap, // Pass in parameter to avoid calculating it over and over
            long inputLastModified,
            String regionId
    ) {

        this.dbClient = dbClient;
        this.s3Client = s3Client;

        this.ncAnimateConfig = ncAnimateConfig;
        this.productDateRange = productDateRange;

        this.frameTimetableMap = frameTimetableMap;
        this.inputLastModified = inputLastModified;

        this.regionId = regionId;

        this.init();
    }

    private void init() {
        FrameGenerator.clearCache();
        this.frameGenerator = new FrameGenerator(this);
    }

    public void generateAllFrames() throws Exception {
        NcAnimateConfigBean ncAnimateConfig = this.getNcAnimateConfig();

        Map<String, NcAnimateRegionBean> regionMap = ncAnimateConfig.getRegions();
        List<Double> targetHeights =  ncAnimateConfig.getTargetHeights();
        if (targetHeights == null || targetHeights.isEmpty()) {
            targetHeights = new ArrayList<Double>();
            targetHeights.add(null);
        }

        if (regionMap != null) {

            Collection<NcAnimateRegionBean> regions = regionMap.values();
            // If regionId is specified, filter out regions
            if (this.regionId != null) {
                regions = new ArrayList<NcAnimateRegionBean>();
                NcAnimateRegionBean region = regionMap.get(this.regionId);
                if (region == null) {
                    throw new IllegalArgumentException(String.format("Invalid region. Product ID %s do not support region ID %s",
                            ncAnimateConfig.getId().getValue(), this.regionId));
                }
                regions.add(region);
            }

            for (NcAnimateRegionBean region : regions) {
                for (Double targetHeight : targetHeights) {
                    FrameGeneratorContext context = new FrameGeneratorContext(ncAnimateConfig);
                    context.setRegion(region);
                    context.setTargetHeight(targetHeight);
                    context.setDateRange(this.productDateRange);
                    this.generateFrame(context);
                }
            }
        }
    }

    public void generateFrame(FrameGeneratorContext context) throws Exception {
        NcAnimateConfigBean ncAnimateConfig = this.getNcAnimateConfig();
        if (ncAnimateConfig == null) {
            LOGGER.warn("NcAnimate config is null?");
            return;
        }

        NcAnimateRenderBean render = ncAnimateConfig.getRender();
        if (render == null) {
            LOGGER.warn(String.format("Render config is null for product ID %s", ncAnimateConfig.getId().getValue()));
            return;
        }

        TimeIncrement frameTimeIncrement = this.ncAnimateConfig.getFrameTimeIncrement();
        FrameGenerator frameGenerator = this.getFrameGenerator();

        DateTime startDate = null, endDate = null;
        DateTimeRange productDateRange = context.getDateRange();
        if (productDateRange != null) {
            startDate = productDateRange.getStartDate();
            endDate = productDateRange.getEndDate();
        }

        context.setFrameTimeIncrement(frameTimeIncrement);

        // Loop through all available date frames and select the one that are between start and end dates.
        int frameCounter = 0;
        for (DateTimeRange frameDateRange : this.frameTimetableMap.keySet()) {
            boolean includeFrame = true;
            if (startDate != null && frameDateRange.getStartDate().compareTo(startDate) < 0) {
                includeFrame = false;
            }
            if (endDate != null && frameDateRange.getEndDate().compareTo(endDate) > 0) {
                includeFrame = false;
            }

            if (includeFrame) {
                context.setFrameDateRange(frameDateRange);
                frameGenerator.generateFrame(context);
            }

            frameCounter++;
            if (frameCounter % 10 == 0) {
                NcAnimateUtils.printMemoryUsage(String.format("NcAnimate generateFrame %d", frameCounter));
            }
        }
    }

    public DatabaseClient getDatabaseClient() {
        return this.dbClient;
    }

    public S3Client getS3Client() {
        return this.s3Client;
    }

    public NcAnimateConfigBean getNcAnimateConfig() {
        return this.ncAnimateConfig;
    }

    public FrameTimetableMap getFrameTimetableMap() {
        return this.frameTimetableMap;
    }

    public long getInputLastModified() {
        return this.inputLastModified;
    }

    public FrameGenerator getFrameGenerator() {
        return this.frameGenerator;
    }
}
