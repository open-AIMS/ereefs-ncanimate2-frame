/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.context;

import au.gov.aims.aws.s3.FileWrapper;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean;
import au.gov.aims.ncanimate.commons.generator.context.GeneratorContext;
import au.gov.aims.ncanimate.commons.timetable.DateTimeRange;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

public class FrameGeneratorContext extends GeneratorContext {
    private static final Logger LOGGER = Logger.getLogger(FrameGeneratorContext.class);

    // Config
    private DateTimeRange frameDateRange;

    public FrameGeneratorContext(NcAnimateConfigBean ncAnimateConfig) {
        super(ncAnimateConfig);
    }

    public DateTimeRange getFrameDateRange() {
        return this.frameDateRange;
    }

    public void setFrameDateRange(DateTimeRange frameDateRange) {
        this.frameDateRange = frameDateRange;
    }

    public File getFrameFileWithoutExtension() {
        return this.getFrameFileWithoutExtension(this.frameDateRange);
    }

    public String getFrameFilenameWithoutExtension() {
        return this.getFrameFilenameWithoutExtension(this.frameDateRange);
    }

    /**
     * Get the frame file, in all expected rendered format
     * NOTE: A frame file might need to be generated into multiple format (svg, png, jpg, etc)
     * @return
     */
    public Map<NcAnimateRenderMapBean.MapFormat, FileWrapper> getFrameFileWrapperMap() {
        return this.getFrameFileWrapperMap(this.frameDateRange);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON()
            .put("frameDateRange", this.frameDateRange == null ? null : this.frameDateRange.toJSON())

            // Used with templates
            .put("frameDateFrom", this.frameDateRange == null ? null : this.frameDateRange.getStartDate())
            .put("frameDateTo", this.frameDateRange == null ? null : this.frameDateRange.getEndDate());

        Map<NcAnimateRenderMapBean.MapFormat, FileWrapper> frameFileMap = this.getFrameFileWrapperMap();
        if (frameFileMap != null && !frameFileMap.isEmpty()) {
            JSONObject jsonFrameFiles = new JSONObject();

            for (Map.Entry<NcAnimateRenderMapBean.MapFormat, FileWrapper> frameFileEntry : frameFileMap.entrySet()) {
                NcAnimateRenderMapBean.MapFormat mapFormat = frameFileEntry.getKey();
                FileWrapper fileWrapper = frameFileEntry.getValue();
                if (mapFormat != null && fileWrapper != null) {
                    jsonFrameFiles.put(
                        mapFormat.name(),
                        new JSONObject()
                            .put("filename", fileWrapper.getFilename())
                            .put("file", fileWrapper.getFile())
                            .put("URI", fileWrapper.getURI())
                            .put("S3URI", fileWrapper.getS3URI())
                    );
                }
            }

            json.put("frameFiles", jsonFrameFiles);
        }

        return json;
    }
}
