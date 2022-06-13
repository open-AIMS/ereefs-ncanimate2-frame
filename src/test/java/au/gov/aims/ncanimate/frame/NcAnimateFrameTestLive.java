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

/**
 * This test class is used to test NcAnimate config before
 * going live on ereefs-definitions.
 *
 * It tests files in test/resources/liveConfig/data/definitions
 *
 * NOTE: Those tests are not automated tests. They are run manually.
 *     The test output files are manually inspected.
 */
public class NcAnimateFrameTestLive extends DatabaseTestBase {

    @Before
    public void insertLiveResources() throws Exception {
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

        this.insertLiveConfigParts(substitutions);
        this.insertLiveConfigs(substitutions);

        this.copyLiveLayerFiles(layerDir);
        this.copyLiveStyleFiles(styleDir);
        this.copyLivePaletteFiles(paletteDir);
    }

    @Test
    public void testSetup() throws Exception {
        int expectedNumberConfig = 89;
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

    @Test
    @Ignore
    public void testGenerate_gbr4_hydro() throws Exception {
        this.insertInputData_liveData_hydro_gbr4();

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "north-2");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "central-2");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "south-2");

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "torres-strait-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "princess-charlotte-bay-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "lizard-island-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cairns-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "townsville-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "whitsundays-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "broad-sound-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "fitzroy-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "hervey-bay-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "brisbane-3");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_v2__temp-wind-salt-current_hourly",
                "2012-10-01T00:00:00.000+10:00", "2012-10-01T02:00:00.000+10:00");


        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/public/ncanimate/frames/products__ncanimate__ereefs__gbr4_v2__temp-wind-salt-current_hourly");
        File qldDir = new File(outputDir, "queensland-1");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_hydro() throws Exception {
        this.insertInputData_liveData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "north-2");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "central-2");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "south-2");

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "torres-strait-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "princess-charlotte-bay-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "lizard-island-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cairns-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "townsville-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "whitsundays-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "broad-sound-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "fitzroy-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "hervey-bay-3");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "brisbane-3");

        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "keppels-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "moreton-bay-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "townsville-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "whitsundays-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "cairns-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "lizard-island-4");
        //NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "heron-4");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__temp-wind-salt-current_hourly",
                "2014-12-02T00:00:00.000+10:00", "2014-12-02T01:00:00.000+10:00");


        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/public/ncanimate/frames/products__ncanimate__ereefs__gbr1_2-0__temp-wind-salt-current_hourly");
        File qldDir = new File(outputDir, "queensland-1");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_fresh_water_exposure() throws Exception {
        this.insertInputData_liveData_hydro_gbr1_monthly();
        this.insertInputData_liveData_fresh_water_exposure_gbr1_monthly();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__fresh-water-exposure_monthly",
                "2015-01-01T00:00:00.000+10:00", "2015-02-01T00:00:00.000+10:00");
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_heatstress_dhw_daily() throws Exception {
        this.insertInputData_liveData_historic_heatstress_gbr1_ncAggregate_daily();
        this.insertInputData_liveData_nrt_heatstress_gbr1_ncAggregate_daily();

        this.insertInputData_liveData_hydro_gbr1_daily();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        // This file contains actual data for DHWx variables
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_heatstress_daily",
                "2020-03-29T00:00:00.000+10:00", "2020-04-01T00:00:00.000+10:00");

        // This one should have some NRT data
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_heatstress_daily",
                "2020-05-01T00:00:00.000+10:00", "2020-06-01T00:00:00.000+10:00");
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_heatstress_dhw_monthly() throws Exception {
        this.insertInputData_liveData_historic_heatstress_gbr1_ncAggregate_monthly();
        this.insertInputData_liveData_nrt_heatstress_gbr1_ncAggregate_monthly();

        this.insertInputData_liveData_hydro_gbr1_monthly();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        // This file contains actual data for DHWx variables
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_heatstress_monthly",
                "2020-03-01T00:00:00.000+10:00", "2020-04-01T00:00:00.000+10:00");

        // This one should have some NRT data
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_heatstress_monthly",
                "2020-05-01T00:00:00.000+10:00", "2020-06-01T00:00:00.000+10:00");
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_hydro_DEBUG() throws Exception {
        this.insertInputData_liveData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-DEBUG");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__temp-wind-salt-current_DEBUG",
                "2014-12-02T00:00:00.000+10:00", "2014-12-02T01:00:00.000+10:00");
    }

    @Test
    @Ignore
    public void testGenerate_mixed_imosVsEreefsTemperature() throws Exception {
        this.insertInputData_liveData_hydro_gbr4_daily_aggregate();
        this.insertInputData_liveData_imos_sst();
        this.insertInputData_liveData_imos_oceanCurrent();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("products__ncanimate__mixed__imos-vs-ereefs-temperature",
                "2012-10-01T00:00:00.000+10:00", "2012-10-03T00:00:00.000+10:00");

        // Trying to reproduce a ncAnimate failure
        ncAnimateFrame.generateFromContext("products__ncanimate__mixed__imos-vs-ereefs-temperature",
                "2020-08-24T00:00:00.000+10:00", "2020-08-25T00:00:00.000+10:00");

        ncAnimateFrame.generateFromContext("products__ncanimate__mixed__imos-vs-ereefs-temperature",
                "2020-08-25T00:00:00.000+10:00", "2020-08-26T00:00:00.000+10:00");

        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/public/ncanimate/frames/products__ncanimate__mixed__imos-vs-ereefs-temperature");
        File qldDir = new File(outputDir, "queensland-1");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_bgc_baseline_daily() throws Exception {
        this.insertInputData_liveData_bgc_gbr4_daily();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        String fromDate = "2019-03-01T00:00:00.000+10:00";
        String toDate = "2019-03-05T00:00:00.000+10:00";

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__alk_ph_omega-ar_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__chl-a-sum_din_efi_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__cs-n_cs-chl_ch-n_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__din_dip_dic_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__din_no3_nh4_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__dor-c_dor-n_dor-p_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__efi_dust_mud-carbonate_mud-mineral_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__ma-n_sg-n_sgh-n_sgd-n_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__oxygen_oxy-sat_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__pip_pip-sed_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__secchi_kd-490_epipar-sg_daily", fromDate, toDate);
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_bgc_baseline__true-colour_daily", fromDate, toDate);

        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/public/ncanimate/frames/products__ncanimate__ereefs__gbr4_bgc_baseline__alk_ph_omega-ar_daily");
        File qldDir = new File(outputDir, "queensland-1");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_bgc_baseline_monthly() throws Exception {
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

    @Test
    @Ignore
    public void testGenerate_gbrf() throws Exception {
        this.insertInputData_liveData_hydro_gbr1();
        this.insertInputData_liveData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__gbrf_salt_hourly",
                "2014-12-02T00:00:00.000+10:00", "2014-12-02T01:00:00.000+10:00");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_v2__gbrf_temp_hourly",
                "2012-10-01T00:00:00.000+10:00", "2012-10-01T01:00:00.000+10:00");


        // Check the generated frames

        File outputDir = new File("/tmp/ncanimate/public/ncanimate/frames/products__ncanimate__ereefs__gbr1_2-0__gbrf_salt_hourly");
        File gbrfDir = new File(outputDir, "gbrf-townsville-4");
        Assert.assertTrue(String.format("Directory %s doesn't exist", gbrfDir), gbrfDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_gbr1_rivers() throws Exception {
        //this.insertInputData_liveData_river_gbr1();

        /*
        NcAnimateTestUtils.insertInputDataFiles(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr1_2-0-river_tracing-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2021-12-01.nc"));
        */

        /*
        NcAnimateTestUtils.insertInputDataFiles(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr1_2-0-river_tracing-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2019-01-31.nc")
            //new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2019-02-01.nc"),
            //new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2019-02-02.nc")
        );
        */

        NcAnimateTestUtils.insertInputDataFiles(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr1_2-0-river_tracing-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2019-01-31.nc"),
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2017-03.nc"),
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily/gbr1_rivers_simple_2022-03-07.nc")
        );

        /*
        Barron (bar)
        Burnett (bnt)
        Boyne (boy)
        Brisbane (bri)
        Burdekin (bur)    <=
        Caboolture (cab)
        Calliope (cal)
        OConnell (con)
        Daintree (dai)
        Don (don)
        Fitzroy (fit)     <=
        Fly (fly)
        Haughton (hau)
        Herbert (her)
        Johnstone (jon)
        Logan (log)
        Mary (mar)        <=
        Mulgrave+Russell (mul)
        Normanby (nom)    <=
        Pine (pin)
        Pioneer (pio)
        Tully (tul)
        */

        /*
        String dateFrom = "2014-12-01T00:00:00.000+10:00";
        String dateTo = "2015-01-01T00:00:00.000+10:00";
        */

        /*
        String dateFrom = "2015-01-01T00:00:00.000+10:00";
        String dateTo = "2015-02-01T00:00:00.000+10:00";
        */

        /*
        String dateFrom = "2019-01-31T00:00:00.000+10:00";
        String dateTo = "2019-02-01T00:00:00.000+10:00";
        //String dateTo = "2019-02-03T00:00:00.000+10:00";
        */

        /*
        String dateFrom = "2021-12-01T00:00:00.000+10:00";
        String dateTo = "2021-12-02T00:00:00.000+10:00";
        */

        /*
        String dateFrom = "2022-05-02T00:00:00.000+10:00";
        String dateTo = "2022-05-03T00:00:00.000+10:00";
        */

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        //ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__rivers",
        //    "2019-01-31T00:00:00.000+10:00", "2019-02-01T00:00:00.000+10:00");
        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__rivers",
            "2017-03-31T00:00:00.000+10:00", "2017-04-01T00:00:00.000+10:00");
        //ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__rivers",
        //    "2022-03-07T00:00:00.000+10:00", "2022-03-08T00:00:00.000+10:00");

        //ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__rivers",
        //    "2017-03-01T00:00:00.000+10:00", "2017-04-01T00:00:00.000+10:00");
    }

    @Test
    @Ignore
    public void testGenerate_gbr4_rivers() throws Exception {
        this.insertInputData_liveData_river_gbr4();

        String dateFrom = "2020-04-01T00:00:00.000+10:00";
        String dateTo = "2020-05-01T00:00:00.000+10:00";

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr4_v2__rivers", dateFrom, dateTo);
    }



    private void insertLiveConfigParts(Map<String, String> substitutions) throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configPartManager, "liveConfig/data/definitions/ncanimateConfigParts", "NcAnimate config part", substitutions, true);
    }

    private void insertLiveConfigs(Map<String, String> substitutions) throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configManager, "liveConfig/data/definitions/ncanimateConfigs", "NcAnimate configuration", substitutions, true);
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
            "products__ncaggregate__ereefs__gbr4_v2__raw",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/hourly"));
    }

    public void insertInputData_liveData_hydro_gbr4_daily_aggregate() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr4_v2__daily-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/daily"));
    }

    public void insertInputData_liveData_river_gbr4() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr4_v2-river_tracing-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/rivers/daily"));
    }

    public void insertInputData_liveData_hydro_gbr1() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0__raw",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly"));
    }

    public void insertInputData_liveData_hydro_gbr1_daily() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0__daily-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/daily"));
    }

    public void insertInputData_liveData_hydro_gbr1_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0__monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/monthly"));
    }

    public void insertInputData_liveData_river_gbr1() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__ereefs__gbr1_2-0-river_tracing-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/rivers/daily"));
    }

    public void insertInputData_liveData_historic_heatstress_gbr1_ncAggregate_daily() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0-historic_heat_stress-daily-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/heat-stress/historic/daily"));
    }
    public void insertInputData_liveData_nrt_heatstress_gbr1_ncAggregate_daily() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0-nrt_heat_stress-daily-daily",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/heat-stress/nrt/daily"));
    }

    public void insertInputData_liveData_historic_heatstress_gbr1_ncAggregate_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0-historic_heat_stress-monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/heat-stress/historic/monthly"));
    }
    public void insertInputData_liveData_nrt_heatstress_gbr1_ncAggregate_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0-nrt_heat_stress-monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/heat-stress/nrt/monthly"));
    }

    public void insertInputData_liveData_fresh_water_exposure_gbr1_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__gbr1_2-0__fresh-water-exposure__monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/fresh-water-exposure/monthly"));
    }

    public void insertInputData_liveData_imos_sst() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__imos__SRS_SST_ghrsst_L3S_6d_ngt",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/imos/SRS_SST_ghrsst_L3S_6d_ngt"));
    }

    public void insertInputData_liveData_imos_oceanCurrent() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "downloads__imos__OceanCurrent_GSLA_NRT00",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/imos/OceanCurrent_GSLA_NRT00"));
    }

    public void insertInputData_liveData_bgc_gbr4_daily() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__GBR4_H2p0_B3p1_Cq3b_Dhnd__raw",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/GBR4_H2p0_B3p1_Cq3b_Dhnd/daily"));

        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__GBR4_H2p0_B3p1_Cq3b_Dhnd__raw__sed",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/GBR4_H2p0_B3p1_Cq3b_Dhnd/daily_sed"));
    }

    public void insertInputData_liveData_bgc_gbr4_monthly() throws Exception {
        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__GBR4_H2p0_B3p1_Cq3b_Dhnd__monthly-monthly",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/GBR4_H2p0_B3p1_Cq3b_Dhnd/monthly"));

        NcAnimateTestUtils.insertInputDataFromDirectory(
            this.getDatabaseClient(),
            "products__ncaggregate__ereefs__GBR4_H2p0_B3p1_Cq3b_Dhnd__monthly-monthly__sed",
            new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/GBR4_H2p0_B3p1_Cq3b_Dhnd/monthly_sed"));
    }
}
