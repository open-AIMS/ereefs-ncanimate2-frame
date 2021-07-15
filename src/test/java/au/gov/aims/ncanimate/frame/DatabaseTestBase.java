/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.ereefs.database.table.DatabaseTable;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ereefs.helper.TestHelper;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

public class DatabaseTestBase {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTestBase.class);
    private static final String DATABASE_NAME = "testdb";

    private MongoServer server;
    private DatabaseClient databaseClient;

    protected long beforeSaveData;
    protected long afterSaveData;

    public DatabaseClient getDatabaseClient() {
        return this.databaseClient;
    }

    @Before
    public void init() throws Exception {
        File dbCacheDir = DatabaseTable.getDatabaseCacheDirectory();
        Utils.deleteDirectory(dbCacheDir);

        this.server = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = this.server.bind();

        this.databaseClient = new DatabaseClient(new ServerAddress(serverAddress), DATABASE_NAME);
        this.createTables();
    }

    @After
    public void shutdown() {
        NcAnimateConfigHelper.clearMetadataCache();
        if (this.server != null) {
            this.server.shutdown();
        }
    }

    private void createTables() throws Exception {
        TestHelper.createTables(this.databaseClient);
    }



    public void insertData() throws Exception {
        this.beforeSaveData = new Date().getTime();

        Utils.deleteDirectory(new File("/tmp/ncanimateTests"));

        this.insertTestConfigParts();
        this.insertTestConfigs();

        this.copyLayerFiles();
        this.copyStyleFiles();
        this.copyPaletteFiles();

        this.afterSaveData = new Date().getTime();
    }


    public void insertInputData_fakeData_hydro_gbr4() throws Exception {
        String definitionId = "downloads/gbr4_v2";

        {
            URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr4_v2_2014-12-01.nc");
            File netCDFFileOrig = new File(netCDFFileUrl.getFile());
            File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr4_v2_2014-12-01.nc");
            netCDFFileCopy.getParentFile().mkdirs();
            Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String datasetId = "gbr4_v2_2014-12-01.nc";
            URI fileURI = netCDFFileCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileCopy, netCDFFileCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }

        {
            URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr4_v2_2014-12-02_missingFrames.nc");
            File netCDFFileOrig = new File(netCDFFileUrl.getFile());
            File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr4_v2_2014-12-02_missingFrames.nc");
            netCDFFileCopy.getParentFile().mkdirs();
            Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String datasetId = "gbr4_v2_2014-12-02_missingFrames.nc";
            URI fileURI = netCDFFileCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileCopy, netCDFFileCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    public void insertInputData_fakeData_bgc() throws Exception {
        String definitionId = "downloads/gbr4_bgc_924";

        {
            URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr4_bgc_2014-12.nc");
            File netCDFFileOrig = new File(netCDFFileUrl.getFile());
            File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr4_bgc_2014-12.nc");
            netCDFFileCopy.getParentFile().mkdirs();
            Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String datasetId = "gbr4_bgc_2014-12.nc";
            URI fileURI = netCDFFileCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileCopy, netCDFFileCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    public void insertInputData_fakeData_hydro_gbr1() throws Exception {
        String definitionId = "downloads/gbr1_2-0";

        {
            URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr1_2014-12-01.nc");
            File netCDFFileOrig = new File(netCDFFileUrl.getFile());
            File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr1_2014-12-01.nc");
            netCDFFileCopy.getParentFile().mkdirs();
            Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String datasetId = "gbr1_2014-12-01.nc";
            URI fileURI = netCDFFileCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileCopy, netCDFFileCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
        {
            URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr1_2014-12-02.nc");
            File netCDFFileOrig = new File(netCDFFileUrl.getFile());
            File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr1_2014-12-02.nc");
            netCDFFileCopy.getParentFile().mkdirs();
            Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String datasetId = "gbr1_2014-12-02.nc";
            URI fileURI = netCDFFileCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileCopy, netCDFFileCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    // The "realData" methods are used for manual tests. Not for automated tests.
    public void insertInputData_realData_hydro_gbr1_BAD() throws Exception {
        String definitionId = "downloads/gbr1_2-0";

        // NcAggregate struggle with this file. Let see what NcAnimate can do with it
        {
            File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1_simple_2016-08-14_BAD.nc");

            String datasetId = "gbr1_simple_2016-08-14_BAD.nc";
            URI fileURI = netCDFFile.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, netCDFFile.lastModified(), false);

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }
    public void insertInputData_realData_hydro_gbr1() throws Exception {
        String definitionId = "downloads/gbr1_2-0";

        {
            File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1_simple_2017-07-21.nc");

            String datasetId = "gbr1_simple_2017-07-21.nc";
            URI fileURI = netCDFFile.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, netCDFFile.lastModified(), false);

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    public void insertInputData_realData_hydro_gbr4() throws Exception {
        String definitionId = "downloads/gbr4_v2";

        {
            File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_simple_2014-12.nc");

            String datasetId = "gbr4_simple_2014-12.nc";
            URI fileURI = netCDFFile.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, netCDFFile.lastModified(), false);

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
        {
            File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_simple_2012-10.nc");

            String datasetId = "gbr4_simple_2012-10.nc";
            URI fileURI = netCDFFile.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, netCDFFile.lastModified(), false);

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    public void insertInputData_fakeData_noaa() throws Exception {
        {
            URL gribsFile_waveDirUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/multi_1.glo_30m.dp.201412.nc");
            File gribsFile_waveDirOrig = new File(gribsFile_waveDirUrl.getFile());
            File gribsFile_waveDirCopy = new File("/tmp/ncanimateTests/netcdfFiles/multi_1.glo_30m.dp.201412.nc");
            gribsFile_waveDirCopy.getParentFile().mkdirs();
            Files.copy(gribsFile_waveDirOrig.toPath(), gribsFile_waveDirCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);


            String definitionId = "downloads/noaa_wave-dir";
            String datasetId = "multi_1.glo_30m.dp.201412.nc";
            URI fileURI = gribsFile_waveDirCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, gribsFile_waveDirCopy, gribsFile_waveDirCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }

        {
            URL gribsFile_waveDirUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/multi_1.glo_30m.hs.201412.nc");
            File gribsFile_waveDirOrig = new File(gribsFile_waveDirUrl.getFile());
            File gribsFile_waveDirCopy = new File("/tmp/ncanimateTests/netcdfFiles/multi_1.glo_30m.hs.201412.nc");
            gribsFile_waveDirCopy.getParentFile().mkdirs();
            Files.copy(gribsFile_waveDirOrig.toPath(), gribsFile_waveDirCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);


            String definitionId = "downloads/noaa_wave-height";
            String datasetId = "multi_1.glo_30m.hs.201412.nc";
            URI fileURI = gribsFile_waveDirCopy.toURI();

            NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, gribsFile_waveDirCopy, gribsFile_waveDirCopy.lastModified());

            MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
            metadataManager.save(metadata.toJSON());
        }
    }

    public void insertInputData_multiHypercubes() throws Exception {
        URL netCDFFileUrl = DatabaseTestBase.class.getClassLoader().getResource("netcdf/gbr4_v2_2000-01-01_multiHypercubes.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());
        File netCDFFileCopy = new File("/tmp/ncanimateTests/netcdfFiles/gbr4_v2_2000-01-01_multiHypercubes.nc");
        netCDFFileCopy.getParentFile().mkdirs();

        Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String definitionId = "downloads/multiHypercubes";
        String datasetId = "gbr4_v2_2000-01-01_multiHypercubes.nc";
        URI fileURI = netCDFFileCopy.toURI();

        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileOrig, netCDFFileOrig.lastModified());

        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CacheStrategy.DISK);
        metadataManager.save(metadata.toJSON());
    }



    private void insertTestConfigParts() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CacheStrategy.DISK);

        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/canvas",    "canvas");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/input",     "input");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/layers",    "layer");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/panels",    "panel");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/regions/old", "region");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/regions/new", "region");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/render",    "render");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/variables", "variable");
    }

    private void insertTestConfigs() throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CacheStrategy.DISK);

        TestHelper.insertTestConfigs(configManager, "ncanimate", "NcAnimate configuration");
    }

    private void copyLayerFiles() throws IOException {
        DatabaseTestBase.copyDir("layers", new File("/tmp/ncanimateTests/s3/layers"));
    }

    private void copyStyleFiles() throws IOException {
        DatabaseTestBase.copyDir("styles", new File("/tmp/ncanimateTests/s3/styles"));
    }

    private void copyPaletteFiles() throws IOException {
        DatabaseTestBase.copyDir("colourPalettes", new File("/tmp/ncanimateTests/s3/palettes"));
    }

    protected static void copyDir(String resourcesSourceStr, File destination) throws IOException {
        URL sourceUrl = DatabaseTestBase.class.getClassLoader().getResource(resourcesSourceStr);
        if (sourceUrl == null) {
            throw new IOException(String.format("Missing resource directory: %s", resourcesSourceStr));
        }

        Utils.prepareDirectory(destination);

        File resourcesSourceDir = new File(sourceUrl.getFile());
        FileUtils.copyDirectory(resourcesSourceDir, destination);
    }
}
