/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

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
import java.util.HashMap;
import java.util.Map;

/**
 * This test class is used to test NcAnimate config during development,
 * before going live on ereefs-definitions.
 *
 * It tests files in test/resources/liveConfig/data/definitions_dev
 *
 * NOTE: Those tests are not automated tests. They are run manually.
 *     The test output files are manually inspected.
 */
public class NcAnimateFrameTestDev extends NcAnimateFrameTestLive {

    @Before
    public void insertDevResources() throws Exception {
        String protocol = "file://";
        File tmpDir = new File("/tmp/ncanimate");
        File privateBucket = new File(tmpDir, "private");
        File publicBucket = new File(tmpDir, "public");

        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("${STORAGE_PROTOCOL}", protocol);
        substitutions.put("${PRIVATE_BUCKET_NAME}", privateBucket.getAbsolutePath());
        substitutions.put("${PUBLIC_BUCKET_NAME}", publicBucket.getAbsolutePath());

        this.insertDevConfigParts(substitutions);
        this.insertDevConfigs(substitutions);
    }

    @Test
    public void testSetup() throws Exception {
        int expectedNumberConfig = 92;
        int expectedNumberConfigParts = 187;

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
    // NOTE: The DHW product was removed
    // Those files contain only zeros for DHWx variables (the model hasn't run in summer months yet)
    public void testGenerate_gbr1_dhw() throws Exception {
        this.insertInputData_liveData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "queensland-1");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_hourly",
                "2020-04-17T00:00:00.000+10:00", "2020-04-18T00:00:00.000+10:00");

        ncAnimateFrame.generateFromContext("products__ncanimate__ereefs__gbr1_2-0__dhw_hourly",
                "2020-05-14T00:00:00.000+10:00", "2020-05-15T00:00:00.000+10:00");
    }






    private void insertDevConfigParts(Map<String, String> substitutions) throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configPartManager, "liveConfig/data/definitions_dev/ncanimateConfigParts", "DEV NcAnimate config part", substitutions, true);
    }

    private void insertDevConfigs(Map<String, String> substitutions) throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CacheStrategy.DISK);
        TestHelper.insertTestConfigs(configManager, "liveConfig/data/definitions_dev/ncanimateConfigs", "DEV NcAnimate configuration", substitutions, true);
    }
}
