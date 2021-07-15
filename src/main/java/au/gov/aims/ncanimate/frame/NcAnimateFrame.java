/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.timetable.DateTimeRange;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetableMap;
import au.gov.aims.ncanimate.frame.generator.GroupFrameGenerator;
import com.mongodb.ServerAddress;
import com.mongodb.internal.connection.ServerAddressHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.Map;

public class NcAnimateFrame {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateFrame.class);

    private static final String NCANIMATE_REGION_ENV_VARIABLE = "NCANIMATE_REGION";
    private static final String NCANIMATE_DATABASE_SERVER_ADDRESS_ENV_VARIABLE = "DATABASE_SERVER_ADDRESS";
    private static final String NCANIMATE_DATABASE_SERVER_PORT_ENV_VARIABLE = "DATABASE_SERVER_PORT";
    private static final String NCANIMATE_DATABASE_NAME_ENV_VARIABLE = "DATABASE_NAME";

    private DatabaseClient dbClient;
    private S3Client s3Client;

    private String regionId;

    public static void main(String ... args) {
        if (args == null || args.length != 3) {
            LOGGER.error("Invalid ereefs-ncanimate2-frame parameters");
            System.exit(1);
        }

        String productId = args[0];
        String dateFromStr = args[1];
        String dateToStr = args[2];

        if ("null".equalsIgnoreCase(dateFromStr)) {
            dateFromStr = null;
        }
        if ("null".equalsIgnoreCase(dateToStr)) {
            dateToStr = null;
        }

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame();

        try {
            ncAnimateFrame.generateFromContext(productId, dateFromStr, dateToStr);
        } catch(Exception ex) {
            LOGGER.fatal("Exception occurred while generating NcAnimate frames", ex);
            System.exit(1);
        }
    }

    public NcAnimateFrame() {
        this(NcAnimateFrame.getDatabaseClient(), NcAnimateFrame.createS3Client(), getRegionId());
    }

    private static S3Client createS3Client() {
        try {
            return new S3Client();
        } catch(Exception ex) {
            LOGGER.warn("Exception occurred while initialising the S3Client. Please ignore if encountered in unit tests.", ex);
            return null;
        }
    }

    private static DatabaseClient getDatabaseClient() {
        String customDatabaseServerAddress = System.getenv(NCANIMATE_DATABASE_SERVER_ADDRESS_ENV_VARIABLE);
        String customDatabaseServerPortStr = System.getenv(NCANIMATE_DATABASE_SERVER_PORT_ENV_VARIABLE);
        String customDatabaseName = System.getenv(NCANIMATE_DATABASE_NAME_ENV_VARIABLE);

        if (customDatabaseServerAddress != null && !customDatabaseServerAddress.isEmpty() &&
                customDatabaseServerPortStr != null && !customDatabaseServerPortStr.isEmpty() &&
                customDatabaseName != null && !customDatabaseName.isEmpty()) {

            // Using a in-memory database, in a Unit test (when called by NcAnimate2)
            int customDatabaseServerPort = Integer.parseInt(customDatabaseServerPortStr);
            ServerAddress serverAddress = ServerAddressHelper.createServerAddress(customDatabaseServerAddress, customDatabaseServerPort);

            return new DatabaseClient(serverAddress, customDatabaseName);
        }

        // Using the real Database
        return new DatabaseClient(NcAnimateUtils.APP_NAME);
    }

    private static String getRegionId() {
        String regionId = System.getenv(NCANIMATE_REGION_ENV_VARIABLE);
        if (regionId != null && !regionId.isEmpty()) {
            return regionId;
        }
        return null;
    }

    public NcAnimateFrame(DatabaseClient dbClient, S3Client s3Client, String regionId) {
        this.dbClient = dbClient;
        this.s3Client = s3Client;
        this.regionId = regionId;
    }

    public void generateFromContext(String productId, String dateFromStr, String dateToStr) throws Exception {
        this.generateFromContext(
            productId,
            DateTimeRange.create(
                dateFromStr == null ? null : new DateTime(dateFromStr),
                dateToStr == null ? null : new DateTime(dateToStr)
            )
        );
    }

    public void generateFromContext(String productId, DateTimeRange productDateRange) throws Exception {
        NcAnimateUtils.printMemoryUsage("NcAnimate frame init");
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory == Long.MAX_VALUE) {
            LOGGER.info(String.format("%n    Max memory: UNLIMITED"));
        } else {
            LOGGER.info(String.format("%n    Max memory: LIMITED TO %.2f MB", (maxMemory / (1024 * 1024.0))));
        }

        NcAnimateConfigHelper configHelper = new NcAnimateConfigHelper(this.dbClient, CacheStrategy.DISK);
        NcAnimateConfigBean ncAnimateConfig = configHelper.getNcAnimateConfig(productId);
        FrameTimetableMap frameTimetableMap = new FrameTimetableMap(ncAnimateConfig, productDateRange, this.dbClient);

        // Find input last modified date
        Map.Entry<NetCDFMetadataBean, Long> inputLastModifiedMap = frameTimetableMap.getInputLastModifiedEntry();

        GroupFrameGenerator groupFrameGenerator = new GroupFrameGenerator(
            this.dbClient, this.s3Client, ncAnimateConfig, productDateRange, frameTimetableMap,
            inputLastModifiedMap == null ? 0 : inputLastModifiedMap.getValue(),
            this.regionId);

        groupFrameGenerator.generateAllFrames();
    }
}
