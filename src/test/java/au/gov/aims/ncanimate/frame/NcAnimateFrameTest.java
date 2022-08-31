/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.junit.AssertImage;
import org.junit.Assert;
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
public class NcAnimateFrameTest extends DatabaseTestBase {
    // NOTE: Difference with image generated in Docker,
    //     when there is text on the image,
    //     is roughly 0.12% = 0.0012
    private static final double VERY_HIGH_TOLERANCE = 0.005;
    private static final double HIGH_TOLERANCE = 0.003;
    private static final double MED_TOLERANCE = 0.002;
    private static final double LOW_TOLERANCE = 0.001;
    private static final double VERY_LOW_TOLERANCE = 0.0005;

    // Used to check (locally) if images have changed at all
    /*
    private static final double VERY_HIGH_TOLERANCE = 0;
    private static final double HIGH_TOLERANCE = 0;
    private static final double MED_TOLERANCE = 0;
    private static final double LOW_TOLERANCE = 0;
    private static final double VERY_LOW_TOLERANCE = 0;
    */

    /**
     * Simple test to check if NcAnimate-frame can generate frame
     * for a list of region * depth * date
     * @throws Exception
     */
    @Test
    public void testGenerate_fakeData_gbr4_hydro() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        // Generate the first 2 frames
        ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current", "2014-12-01T00:00:00.000+10:00", "2014-12-01T02:00:00.000+10:00");
        // Generate the last 2 frames
        ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current", "2014-12-01T22:00:00.000+10:00", "2014-12-02T00:00:00.000+10:00");


        // Check the generated frames on fake S3 (file path on disk used to avoid using S3 in tests)

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr4_v2_temp-wind-salt-current");

        File qldShallowDir = new File(outputDir, "qld/height_-1.5");
        File qldDeepDir = new File(outputDir, "qld/height_-49.0");
        File torresStraitShallowDir = new File(outputDir, "torres-strait/height_-1.5");
        File torresStraitDeepDir = new File(outputDir, "torres-strait/height_-49.0");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldShallowDir), qldShallowDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDeepDir), qldDeepDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", torresStraitShallowDir), torresStraitShallowDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", torresStraitDeepDir), torresStraitDeepDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 4 files were generated per region per depth
        long expectedMinFileSize = 30 * 1024; // 30kB
        for (File dir : new File[]{qldShallowDir, qldDeepDir, torresStraitShallowDir, torresStraitDeepDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 4, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-01_00h00.png"),
                new File(qldShallowDir, "frame_2014-12-01_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-01_01h00.png"),
                new File(qldShallowDir, "frame_2014-12-01_01h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-01_22h00.png"),
                new File(qldShallowDir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-01_23h00.png"),
                new File(qldShallowDir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);


        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-49.0/frame_2014-12-01_00h00.png"),
                new File(qldDeepDir, "frame_2014-12-01_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-49.0/frame_2014-12-01_01h00.png"),
                new File(qldDeepDir, "frame_2014-12-01_01h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-49.0/frame_2014-12-01_22h00.png"),
                new File(qldDeepDir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/qld/height_-49.0/frame_2014-12-01_23h00.png"),
                new File(qldDeepDir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);


        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-1.5/frame_2014-12-01_00h00.png"),
                new File(torresStraitShallowDir, "frame_2014-12-01_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-1.5/frame_2014-12-01_01h00.png"),
                new File(torresStraitShallowDir, "frame_2014-12-01_01h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-1.5/frame_2014-12-01_22h00.png"),
                new File(torresStraitShallowDir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-1.5/frame_2014-12-01_23h00.png"),
                new File(torresStraitShallowDir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);


        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-49.0/frame_2014-12-01_00h00.png"),
                new File(torresStraitDeepDir, "frame_2014-12-01_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-49.0/frame_2014-12-01_01h00.png"),
                new File(torresStraitDeepDir, "frame_2014-12-01_01h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-49.0/frame_2014-12-01_22h00.png"),
                new File(torresStraitDeepDir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current/torres-strait/height_-49.0/frame_2014-12-01_23h00.png"),
                new File(torresStraitDeepDir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);

    }

    /**
     * Test the generation of frame for input file with holes (missing data)
     * @throws Exception
     */
    @Test
    public void testGenerate_fakeData_missingFrame_gbr4_hydro() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");

        // Generate the first 12 frames, for region Qld only
        ncAnimateFrame.generateFromContext("gbr4_v2_temp-wind-salt-current", "2014-12-02T00:00:00.000+10:00", "2014-12-02T12:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr4_v2_temp-wind-salt-current");

        File qldShallowDir = new File(outputDir, "qld/height_-1.5");
        File qldDeepDir = new File(outputDir, "qld/height_-49.0");
        File torresStraitShallowDir = new File(outputDir, "torres-strait/height_-1.5");
        File torresStraitDeepDir = new File(outputDir, "torres-strait/height_-49.0");
        File brisbaneShallowDir = new File(outputDir, "brisbane/height_-1.5");
        File brisbaneDeepDir = new File(outputDir, "brisbane/height_-49.0");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldShallowDir), qldShallowDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDeepDir), qldDeepDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitShallowDir), torresStraitShallowDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDeepDir), torresStraitDeepDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneShallowDir), brisbaneShallowDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDeepDir), brisbaneDeepDir.exists());

        // Verify that exactly 12 files were generated per region per depth
        long expectedMinFileSize = 10 * 1024; // 10kB
        for (File dir : new File[]{qldShallowDir, qldDeepDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 12, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_00h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_01h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_01h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_02h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_02h00.png"), VERY_HIGH_TOLERANCE);
        AssertImage.assertNotEquals("The No data frame have no label",
                AssertImage.getResourceFile("expectedImages/noDataNoLabel4panels.png"),
                new File(qldShallowDir, "frame_2014-12-02_02h00.png"), LOW_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_03h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_03h00.png"), VERY_HIGH_TOLERANCE);
        AssertImage.assertNotEquals("The No data frame have no label",
                AssertImage.getResourceFile("expectedImages/noDataNoLabel4panels.png"),
                new File(qldShallowDir, "frame_2014-12-02_03h00.png"), LOW_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_04h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_04h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_05h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_05h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_06h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_06h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_07h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_07h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_08h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_08h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_09h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_09h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_10h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_10h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-wind-salt-current_missingFrames/qld/height_-1.5/frame_2014-12-02_11h00.png"),
                new File(qldShallowDir, "frame_2014-12-02_11h00.png"), MED_TOLERANCE);
    }

    @Test
    public void testGenerate_fakeData_gbr1_hydro() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");

        ncAnimateFrame.generateFromContext("gbr1_2-0_temp-wind-salt-current", "2014-12-01T00:00:00.000+10:00", "2014-12-01T03:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr1_2-0_temp-wind-salt-current");

        File qldDepth1Dir = new File(outputDir, "qld/height_-2.4");
        File qldDepth2Dir = new File(outputDir, "qld/height_-5.4");
        File qldDepth3Dir = new File(outputDir, "qld/height_-18.0");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth1Dir), qldDepth1Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth2Dir), qldDepth2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth3Dir), qldDepth3Dir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 4 files were generated per region per depth
        long expectedMinFileSize = 30 * 1024; // 30kB
        for (File dir : new File[]{qldDepth1Dir, qldDepth2Dir, qldDepth3Dir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 3, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contains the expected pixels

        // -2.35m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-2.4/frame_2014-12-01_00h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-01_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-2.4/frame_2014-12-01_01h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-01_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-2.4/frame_2014-12-01_02h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-01_02h00.png"), HIGH_TOLERANCE);

        // -5.35m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-5.4/frame_2014-12-01_00h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-01_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-5.4/frame_2014-12-01_01h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-01_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-5.4/frame_2014-12-01_02h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-01_02h00.png"), HIGH_TOLERANCE);

        // -18m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-18.0/frame_2014-12-01_00h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-01_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-18.0/frame_2014-12-01_01h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-01_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_temp-wind-salt-current/qld/height_-18.0/frame_2014-12-01_02h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-01_02h00.png"), HIGH_TOLERANCE);
    }

    @Test
    public void testGenerate_fakeData_gbr1_river() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_river_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");
        ncAnimateFrame.generateFromContext("gbr1_2-0-rivers", "2017-04-01T00:00:00.000+10:00", "2017-04-02T00:00:00.000+10:00");

        // Check the generated frames on fake S3
        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr1_2-0-rivers");
        File qldDepth1Dir = new File(outputDir, "qld/height_-2.4");
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth1Dir), qldDepth1Dir.exists());

        // Verify that exactly 1 file were generated per region per depth
        long expectedMinFileSize = 20 * 1024; // 20kB
        for (File dir : new File[]{qldDepth1Dir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 1, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contains the expected pixels
        // -2.35m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0-rivers/qld/height_-2.4/frame_2017-04-01.png"),
                new File(qldDepth1Dir, "frame_2017-04-01.png"), HIGH_TOLERANCE);

    }

    @Test
    public void testGenerate_fakeData_multiHypercubes() throws Exception {
        this.insertData();
        this.insertInputData_multiHypercubes();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");

        ncAnimateFrame.generateFromContext("multiHypercubes", "2000-01-01T00:00:00.000+10:00", "2000-01-01T08:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/multiHypercubes");

        File qldDir = new File(outputDir, "qld");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 80 * 1024; // 80kB
        for (File dir : new File[]{qldDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 8, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_00h00.png"),
                new File(qldDir, "frame_2000-01-01_00h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_01h00.png"),
                new File(qldDir, "frame_2000-01-01_01h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_02h00.png"),
                new File(qldDir, "frame_2000-01-01_02h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_03h00.png"),
                new File(qldDir, "frame_2000-01-01_03h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_04h00.png"),
                new File(qldDir, "frame_2000-01-01_04h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_05h00.png"),
                new File(qldDir, "frame_2000-01-01_05h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_06h00.png"),
                new File(qldDir, "frame_2000-01-01_06h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/multiHypercubes/qld/frame_2000-01-01_07h00.png"),
                new File(qldDir, "frame_2000-01-01_07h00.png"), MED_TOLERANCE);
    }

    // NOTE: The images generated using Maven (mvn clean test) are 15kb (Difference: 0.04% = 0.0004).
    //     Images generated using IntelliJ are 10kb (the one in test resources).
    //     I don't know why there is such a difference.
    @Test
    public void testGenerate_fakeData_trueColours() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_bgc();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, "qld");

        ncAnimateFrame.generateFromContext("gbr4_bgc_true-colours", "2014-12-01T00:00:00.000+10:00", "2014-12-03T00:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr4_bgc_true-colours");

        File qldDir = new File(outputDir, "qld");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 8 * 1024; // 8kB
        for (File dir : new File[]{qldDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 2, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_bgc_true-colours/qld/frame_2014-12-01.png"),
                new File(qldDir, "frame_2014-12-01.png"), VERY_LOW_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_bgc_true-colours/qld/frame_2014-12-02.png"),
                new File(qldDir, "frame_2014-12-02.png"), VERY_LOW_TOLERANCE);
    }


    /**
     * Test combining datasets of different dimensions (lat, lon, depth, time)
     * @throws Exception
     */
    @Test
    public void testGenerate_fakeData_combined() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr1();
        this.insertInputData_fakeData_bgc();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr1_2-0_true-colours_combined", "2014-12-01T22:00:00.000+10:00", "2014-12-02T02:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr1_2-0_true-colours_combined");

        File qldDepth1Dir = new File(outputDir, "qld/height_-2.4");
        File qldDepth2Dir = new File(outputDir, "qld/height_-5.4");
        File qldDepth3Dir = new File(outputDir, "qld/height_-18.0");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth1Dir), qldDepth1Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth2Dir), qldDepth2Dir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDepth3Dir), qldDepth3Dir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 50 * 1024; // 50kB
        for (File dir : new File[]{qldDepth1Dir, qldDepth2Dir, qldDepth3Dir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 4, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        // -2.35m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-2.4/frame_2014-12-01_22h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-2.4/frame_2014-12-01_23h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-2.4/frame_2014-12-02_00h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-02_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-2.4/frame_2014-12-02_01h00.png"),
                new File(qldDepth1Dir, "frame_2014-12-02_01h00.png"), MED_TOLERANCE);

        // -5.35m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-5.4/frame_2014-12-01_22h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-5.4/frame_2014-12-01_23h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-5.4/frame_2014-12-02_00h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-02_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-5.4/frame_2014-12-02_01h00.png"),
                new File(qldDepth2Dir, "frame_2014-12-02_01h00.png"), MED_TOLERANCE);

        // -18m
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-18.0/frame_2014-12-01_22h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-01_22h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-18.0/frame_2014-12-01_23h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-01_23h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-18.0/frame_2014-12-02_00h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-02_00h00.png"), MED_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr1_2-0_true-colours_combined/qld/height_-18.0/frame_2014-12-02_01h00.png"),
                new File(qldDepth3Dir, "frame_2014-12-02_01h00.png"), MED_TOLERANCE);
    }


    @Test
    public void testGenerate_fakeData_hydro_gbr4_gbr1_combined() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr4();
        this.insertInputData_fakeData_hydro_gbr1();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr4_gbr1_combined_temp-wind-salt-current", "2014-12-02T00:00:00.000+10:00", "2014-12-02T12:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr4_gbr1_combined_temp-wind-salt-current");

        File qldDir = new File(outputDir, "qld/height_-1.5");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 40 * 1024; // 40kB
        for (File dir : new File[]{qldDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 12, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_00h00.png"),
                new File(qldDir, "frame_2014-12-02_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_01h00.png"),
                new File(qldDir, "frame_2014-12-02_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_02h00.png"),
                new File(qldDir, "frame_2014-12-02_02h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_03h00.png"),
                new File(qldDir, "frame_2014-12-02_03h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_04h00.png"),
                new File(qldDir, "frame_2014-12-02_04h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_05h00.png"),
                new File(qldDir, "frame_2014-12-02_05h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_06h00.png"),
                new File(qldDir, "frame_2014-12-02_06h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_07h00.png"),
                new File(qldDir, "frame_2014-12-02_07h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_08h00.png"),
                new File(qldDir, "frame_2014-12-02_08h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_09h00.png"),
                new File(qldDir, "frame_2014-12-02_09h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_10h00.png"),
                new File(qldDir, "frame_2014-12-02_10h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_gbr1_combined_temp-wind-salt-current/qld/height_-1.5/frame_2014-12-02_11h00.png"),
                new File(qldDir, "frame_2014-12-02_11h00.png"), HIGH_TOLERANCE);
    }


    @Test
    public void testGenerate_fakeData_noaa_waves() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr4();
        this.insertInputData_fakeData_noaa();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("noaa_wave", "2014-12-02T00:00:00.000+10:00", "2014-12-02T11:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/noaa_wave");

        File qldDir = new File(outputDir, "qld");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", torresStraitDir), torresStraitDir.exists());
        Assert.assertFalse(String.format("Directory %s exists", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 20 * 1024; // 20kB
        for (File dir : new File[]{qldDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 11, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_00h00.png"),
                new File(qldDir, "frame_2014-12-02_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_01h00.png"),
                new File(qldDir, "frame_2014-12-02_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_02h00.png"),
                new File(qldDir, "frame_2014-12-02_02h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_03h00.png"),
                new File(qldDir, "frame_2014-12-02_03h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_04h00.png"),
                new File(qldDir, "frame_2014-12-02_04h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_05h00.png"),
                new File(qldDir, "frame_2014-12-02_05h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_06h00.png"),
                new File(qldDir, "frame_2014-12-02_06h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_07h00.png"),
                new File(qldDir, "frame_2014-12-02_07h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_08h00.png"),
                new File(qldDir, "frame_2014-12-02_08h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_09h00.png"),
                new File(qldDir, "frame_2014-12-02_09h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/noaa_wave/qld/frame_2014-12-02_10h00.png"),
                new File(qldDir, "frame_2014-12-02_10h00.png"), HIGH_TOLERANCE);
    }

    @Test
    public void testGenerate_fakeData_tempMultiDepth() throws Exception {
        this.insertData();
        this.insertInputData_fakeData_hydro_gbr4();

        NcAnimateFrame ncAnimateFrame = new NcAnimateFrame(this.getDatabaseClient(), null, null);

        ncAnimateFrame.generateFromContext("gbr4_v2_temp-multi-depth_shallow_hourly", "2014-12-02T00:00:00.000+10:00", "2014-12-02T10:00:00.000+10:00");


        // Check the generated frames on fake S3

        File outputDir = new File("/tmp/ncanimateTests/s3/ncanimate/frames/gbr4_v2_temp-multi-depth_shallow_hourly");

        File qldDir = new File(outputDir, "qld");
        File torresStraitDir = new File(outputDir, "torres-strait");
        File brisbaneDir = new File(outputDir, "brisbane");

        Assert.assertTrue(String.format("Directory %s doesn't exist", qldDir), qldDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", torresStraitDir), torresStraitDir.exists());
        Assert.assertTrue(String.format("Directory %s doesn't exist", brisbaneDir), brisbaneDir.exists());

        // Verify that exactly 8 files were generated
        long expectedMinFileSize = 20 * 1024; // 20kB
        for (File dir : new File[]{qldDir, torresStraitDir, brisbaneDir}) {
            File[] files = dir.listFiles();
            Assert.assertNotNull(String.format("Directory %s is empty", dir), files);
            Assert.assertEquals(String.format("Directory %s doesn't contains the expected number of file", dir), 10, files.length);

            for (File file : files) {
                Assert.assertTrue(String.format("The generated file %s is not readable", file), file.canRead());
                Assert.assertTrue(String.format("The generated file %s is smaller than %d", file, expectedMinFileSize),
                        file.length() > expectedMinFileSize);
            }
        }

        // Verify that the expected frames are there and contain the expected pixels

        // Qld
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_00h00.png"),
                new File(qldDir, "frame_2014-12-02_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_01h00.png"),
                new File(qldDir, "frame_2014-12-02_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_02h00.png"),
                new File(qldDir, "frame_2014-12-02_02h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_03h00.png"),
                new File(qldDir, "frame_2014-12-02_03h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_04h00.png"),
                new File(qldDir, "frame_2014-12-02_04h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_05h00.png"),
                new File(qldDir, "frame_2014-12-02_05h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_06h00.png"),
                new File(qldDir, "frame_2014-12-02_06h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_07h00.png"),
                new File(qldDir, "frame_2014-12-02_07h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_08h00.png"),
                new File(qldDir, "frame_2014-12-02_08h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/qld/frame_2014-12-02_09h00.png"),
                new File(qldDir, "frame_2014-12-02_09h00.png"), HIGH_TOLERANCE);


        // Torres Strait
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_00h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_01h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_02h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_02h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_03h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_03h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_04h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_04h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_05h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_05h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_06h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_06h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_07h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_07h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_08h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_08h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/torres-strait/frame_2014-12-02_09h00.png"),
                new File(torresStraitDir, "frame_2014-12-02_09h00.png"), HIGH_TOLERANCE);


        // Brisbane
        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_00h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_00h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_01h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_01h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_02h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_02h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_03h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_03h00.png"), VERY_HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_04h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_04h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_05h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_05h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_06h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_06h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_07h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_07h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_08h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_08h00.png"), HIGH_TOLERANCE);

        AssertImage.assertEquals(
                AssertImage.getResourceFile("expectedImages/gbr4_v2_temp-multi-depth_shallow_hourly/brisbane/frame_2014-12-02_09h00.png"),
                new File(brisbaneDir, "frame_2014-12-02_09h00.png"), HIGH_TOLERANCE);
    }
}
