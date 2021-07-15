/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ereefs.helper.TestHelper;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This test class is used to test NcAnimate config before
 * going live on ereefs-definitions.
 *
 * It tests files in test/resources/liveConfig/data/definitions_benchmark
 *
 * NOTE: Those tests are not automated tests. They are run manually.
 *     The test output files are manually inspected.
 */
public class NcAnimateFrameTestBenchmark extends DatabaseTestBase {

    @Before
    public void insertDevResources() throws Exception {
        String protocol = "file://";
        File tmpDir = new File("/tmp/ncanimate");
        File privateBucket = new File(tmpDir, "private");
        File publicBucket = new File(tmpDir, "public");

        File layerDir = new File(privateBucket, "ncanimate/resources/layers");
        File styleDir = new File(privateBucket, "ncanimate/resources/styles");
        File paletteDir = new File(privateBucket, "ncanimate/resources/palettes");

        Utils.deleteDirectory(tmpDir);

        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("${STORAGE_PROTOCOL}", protocol);
        substitutions.put("${PRIVATE_BUCKET_NAME}", privateBucket.getAbsolutePath());
        substitutions.put("${PUBLIC_BUCKET_NAME}", publicBucket.getAbsolutePath());

        this.insertBenchmarkConfigParts(substitutions);
        this.insertBenchmarkConfigs(substitutions);

        this.copyLiveLayerFiles(layerDir);
        this.copyLiveStyleFiles(styleDir);
        this.copyLivePaletteFiles(paletteDir);
    }

    @Test
    public void testSetup() throws Exception {
        int expectedNumberConfig = 90;
        int expectedNumberConfigParts = 180;

        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CacheStrategy.DISK);
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CacheStrategy.DISK);

        int configCount = 0;
        for (JSONObject jsonConfig : configManager.selectAll()) {
            configCount++;
        }
        Assert.assertEquals("Wrong number of NcAnimate configs", expectedNumberConfig, configCount);

        int configPartCount = 0;
        for (JSONObject jsonConfigPart : configPartManager.selectAll()) {
            configPartCount++;
        }
        Assert.assertEquals("Wrong number of NcAnimate config parts", expectedNumberConfigParts, configPartCount);

        // This will implicitly run the validator on every ncAnimate config
        int configBeanCount = 0;
        NcAnimateConfigHelper configHelper = new NcAnimateConfigHelper(this.getDatabaseClient(), CacheStrategy.DISK);
        for (NcAnimateConfigBean ncAnimateConfigBean : configHelper.getAllNcAnimateConfig()) {
            Assert.assertNotNull("Invalid NcAnimate configuration found.", ncAnimateConfigBean);
            configBeanCount++;
        }
        Assert.assertEquals("Wrong number of NcAnimate config beans", expectedNumberConfig, configBeanCount);
    }

    // Test the regex used by the download manager
    @Test
    public void testRegexGBR1() {
        String filenameRegex = "gbr1_simple_2017-0[123]-[0-9]{2}\\.nc";
        Pattern pattern = Pattern.compile(filenameRegex);

        String[] invalidFilenames = new String[] {
            "gbr1_simple_2016-01-01.nc",
            "gbr1_simple_2016-02-01.nc",
            "gbr1_simple_2016-03-01.nc",

            "gbr1_simple_2017-01-01onc",
            "gbr1_simple_2017-04-01.nc",
            "gbr1_simple_2017-04-02.nc",
            "gbr1_simple_2017-04-03.nc",
            "gbr1_simple_2017-10-01.nc",
            "gbr1_simple_2017-11-01.nc",
            "gbr1_simple_2017-12-01.nc",

            "gbr1_simple_2018-01-01.nc",
            "gbr1_simple_2018-02-01.nc",
            "gbr1_simple_2018-03-01.nc",

            "gbr1_simple_2020-01-01.nc",
            "gbr1_simple_2020-02-01.nc",
            "gbr1_simple_2020-03-01.nc",
            "gbr1_simple_2020-04-01.nc",
            "gbr1_simple_2020-05-14.nc"
        };

        String[] validFilenames = new String[] {
            "gbr1_simple_2017-01-01.nc",
            "gbr1_simple_2017-01-02.nc",
            "gbr1_simple_2017-01-03.nc",
            "gbr1_simple_2017-01-04.nc",
            "gbr1_simple_2017-01-10.nc",
            "gbr1_simple_2017-01-19.nc",
            "gbr1_simple_2017-01-28.nc",
            "gbr1_simple_2017-01-31.nc",

            "gbr1_simple_2017-02-01.nc",

            "gbr1_simple_2017-03-01.nc",
            "gbr1_simple_2017-03-10.nc",
            "gbr1_simple_2017-03-31.nc"
        };

        for (String invalidFilename : invalidFilenames) {
            Assert.assertFalse(String.format("Invalid filename %s passed the pattern", invalidFilename),
                    pattern.matcher(invalidFilename).matches());
        }

        for (String validFilename : validFilenames) {
            Assert.assertTrue(String.format("Valid filename %s did not passed the pattern", validFilename),
                    pattern.matcher(validFilename).matches());
        }
    }

    @Test
    public void testRegexGBR4_BGC() {
        String filenameRegex = "gbr4_bgc_simple_2017-0[123]\\.nc";
        Pattern pattern = Pattern.compile(filenameRegex);

        String[] invalidFilenames = new String[] {
            "gbr4_bgc_simple_2016-01.nc",
            "gbr4_bgc_simple_2016-02.nc",
            "gbr4_bgc_simple_2016-03.nc",

            "gbr4_bgc_simple_2017-01onc",
            "gbr4_bgc_simple_2017-04.nc",
            "gbr4_bgc_simple_2017-10.nc",
            "gbr4_bgc_simple_2017-11.nc",
            "gbr4_bgc_simple_2017-12.nc",

            "gbr4_bgc_simple_2018-01.nc",
            "gbr4_bgc_simple_2018-02.nc",
            "gbr4_bgc_simple_2018-03.nc"
        };

        String[] validFilenames = new String[] {
            "gbr4_bgc_simple_2017-01.nc",
            "gbr4_bgc_simple_2017-02.nc",
            "gbr4_bgc_simple_2017-03.nc"
        };

        for (String invalidFilename : invalidFilenames) {
            Assert.assertFalse(String.format("Invalid filename %s passed the pattern", invalidFilename),
                    pattern.matcher(invalidFilename).matches());
        }

        for (String validFilename : validFilenames) {
            Assert.assertTrue(String.format("Valid filename %s did not passed the pattern", validFilename),
                    pattern.matcher(validFilename).matches());
        }
    }

    @Test
    public void testRegexGBR4() {
        String filenameRegex = "gbr4_simple_2017-0[123]\\.nc";
        Pattern pattern = Pattern.compile(filenameRegex);

        String[] invalidFilenames = new String[] {
            "gbr4_simple_2016-01.nc",
            "gbr4_simple_2016-02.nc",
            "gbr4_simple_2016-03.nc",

            "gbr4_simple_2017-01onc",
            "gbr4_simple_2017-04.nc",
            "gbr4_simple_2017-10.nc",
            "gbr4_simple_2017-11.nc",
            "gbr4_simple_2017-12.nc",

            "gbr4_simple_2018-01.nc",
            "gbr4_simple_2018-02.nc",
            "gbr4_simple_2018-03.nc"
        };

        String[] validFilenames = new String[] {
            "gbr4_simple_2017-01.nc",
            "gbr4_simple_2017-02.nc",
            "gbr4_simple_2017-03.nc"
        };

        for (String invalidFilename : invalidFilenames) {
            Assert.assertFalse(String.format("Invalid filename %s passed the pattern", invalidFilename),
                    pattern.matcher(invalidFilename).matches());
        }

        for (String validFilename : validFilenames) {
            Assert.assertTrue(String.format("Valid filename %s did not passed the pattern", validFilename),
                    pattern.matcher(validFilename).matches());
        }
    }


    @Test
    @Ignore
    public void testGenerate_gbr4_hydro() throws Exception {
        this.insertInputData_liveData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "torres-strait");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cape-york");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "wet-tropics");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burdekin");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "mackay-whitsunday");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "fitzroy");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burnett-mary");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "brisbane");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_v2__temp-wind-salt-current_hourly",
                "2012-10-01T00:00:00.000+10:00", "2012-10-01T02:00:00.000+10:00");

    }

    @Test
    @Ignore
    public void testGenerate_gbr1_hydro() throws Exception {
        this.insertInputData_liveData_hydro_gbr1();

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "torres-strait");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cape-york");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "wet-tropics");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burdekin");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "mackay-whitsunday");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "fitzroy");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burnett-mary");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "brisbane");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__temp-wind-salt-current_hourly",
                "2014-12-02T00:00:00.000+10:00", "2014-12-02T01:00:00.000+10:00");

    }

    @Test
    @Ignore
    public void testGenerate_bgc_daily() throws Exception {
        this.insertInputData_liveData_bgc_gbr4_daily();

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "torres-strait");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cape-york");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "wet-tropics");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burdekin");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "mackay-whitsunday");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "fitzroy");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "burnett-mary");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "brisbane");

        String fromDate = "2017-08-01T00:00:00.000+10:00";
        String toDate = "2017-08-03T00:00:00.000+10:00";

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__alk_ph_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__chl-a-sum_din_tss_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__cs-n_cs-chl_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__detpl-n_detbl-n_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__detr-c_detr-n_detr-p_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__din_dip_dic_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__dor-c_dor-n_dor-p_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__ma-n_sg-n_sgh-n_ch-n_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__mud_finesed_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__no3_nh4_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__oxygen_cod_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__phys-chl_phyl-chl_mpb-chl_tricho-chl_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__phys-i_phyl-i_mpb-i_tricho-i_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__phys-n_phyl-n_mpb-n_tricho-n_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__phys-nr_phyl-nr_mpb-nr_tricho-nr_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__phys-pr_phyl-pr_mpb-pr_tricho-pr_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__pip_pipi-sed_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__poc_tss645_kd490_plume-rms_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__secchi-kd488_secchi-surf_secchi_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__true-colour_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_924__zool-n_zoos-n_daily", fromDate, toDate);
    }

    @Test
    @Ignore
    public void testGenerate_bgc_monthly() throws Exception {
        this.insertInputData_liveData_bgc_gbr4_monthly();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        String fromDate = "2019-03-01T00:00:00.000+10:00";
        String toDate = "2019-04-01T00:00:00.000+10:00";

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__alk_ph_omega-ar_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__chl-a-sum_din_efi_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__cs-n_cs-chl_ch-n_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__din_dip_dic_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__din_no3_nh4_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__dor-c_dor-n_dor-p_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__efi_dust_mud-carbonate_mud-mineral_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__ma-n_sg-n_sgh-n_sgd-n_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__oxygen_oxy-sat_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__pip_pip-sed_monthly", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__secchi_kd-490_epipar-sg_monthly", fromDate, toDate);

        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/working/output/frame/products__ncanimate__ereefs__gbr4_bgc_baseline__alk_ph_omega-ar_monthly");
        File qldDir = new File(outputDir, "queensland-1");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }




    private void insertBenchmarkConfigParts(Map<String, String> substitutions) throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configPartManager, "liveConfig/data/definitions_benchmark/ncanimateConfigParts", "Benchmark NcAnimate config part", substitutions, true);
    }

    private void insertBenchmarkConfigs(Map<String, String> substitutions) throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configManager, "liveConfig/data/definitions_benchmark/ncanimateConfigs", "Benchmark NcAnimate configuration", substitutions, true);
    }

    private void copyLiveLayerFiles(File layerDir) throws IOException {
        DatabaseTestBase.copyDir("liveConfig/resources/ncanimate/layers", layerDir);
    }

    private void copyLiveStyleFiles(File styleDir) throws IOException {
        DatabaseTestBase.copyDir("liveConfig/resources/ncanimate/styles", styleDir);
    }

    private void copyLivePaletteFiles(File paletteDir) throws IOException {
        DatabaseTestBase.copyDir("liveConfig/resources/ncanimate/palettes", paletteDir);
    }


    public void insertInputData_liveData_hydro_gbr4() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr4_v2",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/hourly"));
    }

    public void insertInputData_liveData_hydro_gbr1() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr1_2-0",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly"));
    }

    public void insertInputData_liveData_bgc_gbr4_daily() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr4_bgc_924",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/v924/daily"));
    }

    public void insertInputData_liveData_bgc_gbr4_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr4_bgc_924__operational__monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/v924/monthly"));
    }
}
