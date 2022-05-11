/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Those tests generate images containing as little text as possible.
 *     There is too much difference in font rendering between computer environments.
 *     See "EnvironmentTest" for fontTest
 *
 * All input NetCDF files were generated using netcdf-generator:
 *     https://github.com/aims-ks/netcdf-generator
 */
public class NcAnimateFrameTestManual extends DatabaseTestBase {

    @Test
    @Ignore
    public void testGenerate_realData_gbr1_hydro() throws Exception {
        this.insertData();
        this.insertInputData_realData_hydro_gbr1_BAD();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");

        ncAnimateFrame.generateFromContext("gbr1_2-0_current-multi-depth", "2016-08-14T00:00:00.000+10:00", "2016-08-15T00:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr1_2-0_current-multi-depth");

        File qldDir = new File(outputDir, "qld");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());
    }

    /**
     * This test was used to visualise the regions and create the SLD files for the cities
     * @throws Exception
     */
    @Test
    @Ignore
    public void testGenerate_realData_gbr4_hydro_new_regions() throws Exception {
        this.insertData();
        this.insertInputData_realData_hydro_gbr4();

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


        //ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current_new-regions", "2014-12-14T00:00:00.000+10:00", "2014-12-14T02:00:00.000+10:00");
        ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current_new-regions", "2012-10-01T00:00:00.000+10:00", "2012-10-01T02:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/working/output/frame/gbr4_v2_temp-wind-salt-current_new-regions");

        File ts3Dir = new File(outputDir, "torres-strait-3");
        File pcb3Dir = new File(outputDir, "princess-charlotte-bay-3");
        File li3Dir = new File(outputDir, "lizard-island-3");
        File c3Dir = new File(outputDir, "cairns-3");
        File t3Dir = new File(outputDir, "townsville-3");
        File w3Dir = new File(outputDir, "whitsundays-3");
        File bs3Dir = new File(outputDir, "broad-sound-3");
        File f3Dir = new File(outputDir, "fitzroy-3");
        File hb3Dir = new File(outputDir, "hervey-bay-3");
        File b3Dir = new File(outputDir, "brisbane-3");
        File qldDir = new File(outputDir, "queensland-1");
        File n2Dir = new File(outputDir, "north-2");
        File c2Dir = new File(outputDir, "central-2");
        File s2Dir = new File(outputDir, "south-2");
        File k4Dir = new File(outputDir, "keppels-4");
        File mb4Dir = new File(outputDir, "moreton-bay-4");
        File t4Dir = new File(outputDir, "townsville-4");
        File w4Dir = new File(outputDir, "whitsundays-4");
        File c4Dir = new File(outputDir, "cairns-4");
        File li4Dir = new File(outputDir, "lizard-island-4");
        File h4Dir = new File(outputDir, "heron-4");

        Assert.assertTrue(String.format("Directory %s doesn't exist", ts3Dir), ts3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", pcb3Dir), pcb3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", li3Dir), li3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c3Dir), c3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", t3Dir), t3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", w3Dir), w3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", bs3Dir), bs3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", f3Dir), f3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", hb3Dir), hb3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", b3Dir), b3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", n2Dir), n2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c2Dir), c2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", s2Dir), s2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", k4Dir), k4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", mb4Dir), mb4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", t4Dir), t4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", w4Dir), w4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c4Dir), c4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", li4Dir), li4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", h4Dir), h4Dir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_realData_gbr4_hydro_new_colour_ramps() throws Exception {
        this.insertData();
        this.insertInputData_realData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current_new-colour-ramps", "2012-10-01T00:00:00.000+10:00", "2012-10-01T02:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/working/output/frame/gbr4_v2_temp-wind-salt-current_new-colour-ramps");

        File qldDir = new File(outputDir, "queensland-1");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_realData_gbr1_hydro_new_regions() throws Exception {
        this.insertData();
        this.insertInputData_realData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr1_2-0_temp-wind-salt-current_new-regions", "2017-07-21T00:00:00.000+10:00", "2017-07-21T02:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/working/output/frame/gbr1_2-0_temp-wind-salt-current_new-regions");

        File ts3Dir = new File(outputDir, "torres-strait-3");
        File pcb3Dir = new File(outputDir, "princess-charlotte-bay-3");
        File li3Dir = new File(outputDir, "lizard-island-3");
        File c3Dir = new File(outputDir, "cairns-3");
        File t3Dir = new File(outputDir, "townsville-3");
        File w3Dir = new File(outputDir, "whitsundays-3");
        File bs3Dir = new File(outputDir, "broad-sound-3");
        File f3Dir = new File(outputDir, "fitzroy-3");
        File hb3Dir = new File(outputDir, "hervey-bay-3");
        File b3Dir = new File(outputDir, "brisbane-3");
        File qldDir = new File(outputDir, "queensland-1");
        File n2Dir = new File(outputDir, "north-2");
        File c2Dir = new File(outputDir, "central-2");
        File s2Dir = new File(outputDir, "south-2");
        File k4Dir = new File(outputDir, "keppels-4");
        File mb4Dir = new File(outputDir, "moreton-bay-4");
        File t4Dir = new File(outputDir, "townsville-4");
        File w4Dir = new File(outputDir, "whitsundays-4");
        File c4Dir = new File(outputDir, "cairns-4");
        File li4Dir = new File(outputDir, "lizard-island-4");
        File h4Dir = new File(outputDir, "heron-4");

        Assert.assertTrue(String.format("Directory %s doesn't exist", ts3Dir), ts3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", pcb3Dir), pcb3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", li3Dir), li3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c3Dir), c3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", t3Dir), t3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", w3Dir), w3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", bs3Dir), bs3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", f3Dir), f3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", hb3Dir), hb3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", b3Dir), b3Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", n2Dir), n2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c2Dir), c2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", s2Dir), s2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", k4Dir), k4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", mb4Dir), mb4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", t4Dir), t4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", w4Dir), w4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", c4Dir), c4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", li4Dir), li4Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", h4Dir), h4Dir.exists());
    }

    @Test
    @Ignore
    public void testGenerate_realData_gbr4_hydro_data_extract() throws Exception {
        this.insertData();
        this.insertInputData_realData_hydro_gbr4();

        String dateFrom = "2014-12-01T00:00:00.000+10:00";
        String dateTo = "2015-01-01T00:00:00.000+10:00";

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr4_v2_temp_data-extract", dateFrom, dateTo);
        ncAnimateFrame.generateFromContext("gbr4_v2_salt_data-extract", dateFrom, dateTo);

        ncAnimateFrame.generateFromContext("gbr4_v2_wind-u_data-extract", dateFrom, dateTo);
        ncAnimateFrame.generateFromContext("gbr4_v2_wind-v_data-extract", dateFrom, dateTo);

        ncAnimateFrame.generateFromContext("gbr4_v2_current-u_data-extract", dateFrom, dateTo);
        ncAnimateFrame.generateFromContext("gbr4_v2_current-v_data-extract", dateFrom, dateTo);
    }

    @Test
    @Ignore
    public void testCalculateMinMax_realData_gbr4_hydro() throws Exception {
        /*
        variables
            temp [22, 34]
            salt [32, 36]

            wind
                wspeed_u [-10, 10]
                wspeed_v [-10, 10]
                wspeed_u:wspeed_v-dir
                wspeed_u:wspeed_v-mag
                wspeed_u:wspeed_v-group

            current
                u [-3, 3]
                v [-3, 3]
                u:v-dir
                u:v-mag
                u:v-group

            eta
            botz
        */

        // NOTE: The computeMinMax method is broken. It seems like something have changed in the library.
        //     The min/max were approximated using Panoply.
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/hourly/gbr4_simple_2012-10.nc");
        String variableId = "temp";
        NetCDFUtils.DataDomain minmax = NetCDFUtils.computeMinMax(netCDFFile, variableId, -1.5);
        System.out.println(String.format("%s: [%.2f, %.2f]", variableId, minmax.getMin(), minmax.getMax()));
    }
}
