/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ncanimate.commons.timetable.FrameTimetableMap;
import org.apache.log4j.Logger;
import uk.ac.rdg.resc.edal.dataset.cdm.NetcdfDatasetAggregator;

import java.io.File;

public class Grib2LayerGenerator extends NetCDFLayerGenerator {
    private static final Logger LOGGER = Logger.getLogger(Grib2LayerGenerator.class);

    public Grib2LayerGenerator(S3Client s3Client, DatabaseClient dbClient, FrameTimetableMap frameTimetableMap) {
        super(s3Client, dbClient, frameTimetableMap);
    }

    public static String getUniqueId(String layerId) {
        return "Grib2_" + layerId;
    }

    @Override
    public String getLayerType() {
        return "Grib2";
    }

    public void prepareInputFile(File grib2File) throws Exception {
        if (grib2File != null && grib2File.canRead()) {
            if (!this.isSecondaryFilesExists(grib2File)) {
                LOGGER.warn("GRIB2 secondary files have been deleted. They can be recreated, but that takes some time. Fix the problem to avoid wasting time.");
                NetcdfDatasetAggregator.getDataset(grib2File.getAbsolutePath(), true);
            }
        }
    }

    private boolean isSecondaryFilesExists(File grib2File) {
        String grib2FileAbsolutePath = grib2File.getAbsolutePath();
        File[] secondaryFiles = new File[] {
            new File(grib2FileAbsolutePath + ".gbx9"),
            new File(grib2FileAbsolutePath + ".ncx4")
        };
        for (File secondaryFile : secondaryFiles) {
            if (!secondaryFile.exists()) {
                return false;
            }
        }

        return true;
    }
}
