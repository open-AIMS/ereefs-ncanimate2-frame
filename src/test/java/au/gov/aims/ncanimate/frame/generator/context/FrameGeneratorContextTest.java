/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.context;

import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.bean.metadata.TimeIncrementUnit;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateRegionBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.ncanimate.commons.timetable.DateTimeRange;
import au.gov.aims.ncanimate.frame.DatabaseTestBase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class FrameGeneratorContextTest extends DatabaseTestBase {

    @Test
    public void testParseFrameDate() throws Exception {
        super.insertData();

        NcAnimateConfigHelper ncAnimateConfigHelper =
            new NcAnimateConfigHelper(this.getDatabaseClient(), CacheStrategy.DISK);
        NcAnimateConfigBean ncAnimateConfig = ncAnimateConfigHelper.getNcAnimateConfig("gbr4_v2_temp-wind-salt-current");
        DateTimeZone timezone = NcAnimateUtils.getTimezone(ncAnimateConfig);

        DateTime productDateFrom = new DateTime(2010, 1, 1, 0, 30, timezone);
        DateTime productDateTo = new DateTime(2011, 1, 1, 0, 30, timezone);
        DateTimeRange productDateRange = DateTimeRange.create(productDateFrom, productDateTo);

        DateTime frameDateFrom = new DateTime(2010, 5, 2, 12, 30, timezone);
        DateTime frameDateTo = new DateTime(2010, 5, 2, 13, 30, timezone);
        DateTimeRange frameDateRange = DateTimeRange.create(frameDateFrom, frameDateTo);
        TimeIncrement frameTimeIncrement = new TimeIncrement(1, TimeIncrementUnit.HOUR);

        NcAnimateRegionBean regionBrisbane = ncAnimateConfig.getRegions().get("brisbane");

        {
            String pattern = "${ctx.frameDateFrom dd-MMM-yyyy_hh:mm}";
            String expected = "02-May-2010 12:30";

            FrameGeneratorContext context = new FrameGeneratorContext(ncAnimateConfig);
            context.setDateRange(productDateRange);
            context.setRegion(regionBrisbane);
            context.setTargetHeight(-12.0);

            context.setFrameDateRange(frameDateRange);
            context.setFrameTimeIncrement(frameTimeIncrement);

            Assert.assertEquals(expected, NcAnimateUtils.parseString(pattern, context));
        }
    }

    @Test
    public void testParseVideoString() throws Exception {
        super.insertData();

        NcAnimateConfigHelper ncAnimateConfigHelper =
            new NcAnimateConfigHelper(this.getDatabaseClient(), CacheStrategy.DISK);
        NcAnimateConfigBean ncAnimateConfig = ncAnimateConfigHelper.getNcAnimateConfig("gbr4_v2_temp-wind-salt-current");
        DateTimeZone timezone = NcAnimateUtils.getTimezone(ncAnimateConfig);

        DateTime productDateFrom = new DateTime(2010, 1, 1, 0, 30, timezone);
        DateTime productDateTo = new DateTime(2011, 1, 1, 0, 30, timezone);
        DateTimeRange productDateRange = DateTimeRange.create(productDateFrom, productDateTo);

        DateTime frameDateFrom = new DateTime(2010, 5, 2, 12, 30, timezone);
        DateTime frameDateTo = new DateTime(2010, 5, 2, 13, 30, timezone);
        DateTimeRange frameDateRange = DateTimeRange.create(frameDateFrom, frameDateTo);
        TimeIncrement frameTimeIncrement = new TimeIncrement(1, TimeIncrementUnit.HOUR);

        NcAnimateRenderVideoBean renderMp4File = ncAnimateConfig.getRender().getVideos().get("mp4Video");
        NcAnimateRenderVideoBean renderWmvFile = ncAnimateConfig.getRender().getVideos().get("wmvVideo");
        NcAnimateRegionBean regionTorresStrait = ncAnimateConfig.getRegions().get("torres-strait");

        {
            String pattern = "/usr/bin/ffmpeg -y -r \"${ctx.renderFile.fps}\" -i \"${ctx.videoFrameDirectory}/${ctx.frameFilenamePrefix}_%05d.png\" -qscale 10 -s ${ctx.productWidth}x${ctx.productHeight} \"${ctx.outputFile}\"";
            String expected = "/usr/bin/ffmpeg -y -r \"10\" -i \"/tmp/ncanimateTests/working/output/frame/gbr4_v2_temp-wind-salt-current/torres-strait/height_-12.0/videoFrames/frame_%05d.png\" -qscale 10 -s 640x265 \"/tmp/ncanimateTests/working/output/product/gbr4_v2_temp-wind-salt-current/video_2010_torres-strait_-12.0.wmv\"";

            FrameGeneratorContext context = new FrameGeneratorContext(ncAnimateConfig);
            context.setDateRange(productDateRange);
            context.setRegion(regionTorresStrait);
            context.setTargetHeight(-12.0);

            context.setFrameDateRange(frameDateRange);
            context.setFrameTimeIncrement(frameTimeIncrement);

            context.setRenderFile(renderWmvFile);
            context.setOutputFilenamePrefix("video");

            Assert.assertEquals(expected, NcAnimateUtils.parseString(pattern, context));
        }

        {
            String pattern1 = "/usr/bin/ffmpeg -y -r \"${ctx.renderFile.fps}\" -i \"${ctx.videoFrameDirectory}/${ctx.frameFilenamePrefix}_%05d.png\" -vcodec libx264 -profile:v baseline -pix_fmt yuv420p -crf 29 -vf \"pad=${ctx.productWidth}:${ctx.productHeight}:${ctx.padding.left}:${ctx.padding.top}:white\" \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\"";
            String pattern2 = "/usr/bin/qt-faststart \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\" \"${ctx.outputFile}\"";
            String pattern3 = "rm \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\"";

            String expected1 = "/usr/bin/ffmpeg -y -r \"12\" -i \"/tmp/ncanimateTests/working/output/frame/gbr4_v2_temp-wind-salt-current/torres-strait/height_-12.0/videoFrames/frame_%05d.png\" -vcodec libx264 -profile:v baseline -pix_fmt yuv420p -crf 29 -vf \"pad=944:400:6:7:white\" \"/tmp/ncanimateTests/working/output/product/gbr4_v2_temp-wind-salt-current/temp_video_2010_torres-strait_-12.0.mp4\"";
            String expected2 = "/usr/bin/qt-faststart \"/tmp/ncanimateTests/working/output/product/gbr4_v2_temp-wind-salt-current/temp_video_2010_torres-strait_-12.0.mp4\" \"/tmp/ncanimateTests/working/output/product/gbr4_v2_temp-wind-salt-current/video_2010_torres-strait_-12.0.mp4\"";
            String expected3 = "rm \"/tmp/ncanimateTests/working/output/product/gbr4_v2_temp-wind-salt-current/temp_video_2010_torres-strait_-12.0.mp4\"";

            FrameGeneratorContext context = new FrameGeneratorContext(ncAnimateConfig);
            context.setDateRange(productDateRange);
            context.setRegion(regionTorresStrait);
            context.setTargetHeight(-12.0);

            context.setFrameDateRange(frameDateRange);
            context.setFrameTimeIncrement(frameTimeIncrement);

            context.setRenderFile(renderMp4File);
            context.setOutputFilenamePrefix("video");

            Assert.assertEquals(expected1, NcAnimateUtils.parseString(pattern1, context));
            Assert.assertEquals(expected2, NcAnimateUtils.parseString(pattern2, context));
            Assert.assertEquals(expected3, NcAnimateUtils.parseString(pattern3, context));
        }
    }
}
