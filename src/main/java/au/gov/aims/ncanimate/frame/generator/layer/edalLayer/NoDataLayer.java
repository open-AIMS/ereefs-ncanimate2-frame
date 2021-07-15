/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.edalLayer;

import au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import au.gov.aims.sld.TextAlignment;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

public class NoDataLayer {
    // When there is no data for a given date, we draw all the other layers and write "Data not available"
    //   inside a white rectangle (with transparency).
    //   Since this is quite uncommon, this behaviour is not configurable (hardcoded)
    private static final Color DATA_NOT_AVAILABLE_BACKGROUND_COLOR = new Color(255, 255, 255, 191);
    private static final int DATA_NOT_AVAILABLE_BACKGROUND_WIDTH = 250;
    private static final int DATA_NOT_AVAILABLE_BACKGROUND_HEIGHT = 130;

    private static final Color DATA_NOT_AVAILABLE_TEXT_COLOR = new Color(80, 80, 80, 127);
    private static final int DATA_NOT_AVAILABLE_FONT_SIZE = 30;
    private static final int DATA_NOT_AVAILABLE_FONT_HEIGHT = 20; // Height of the text height, in pixel, to facilitated centering
    private static final String DATA_NOT_AVAILABLE_LINE_1 = "DATA NOT";
    private static final String DATA_NOT_AVAILABLE_LINE_2 = "AVAILABLE";

    private NcAnimateConfigBean ncAnimateConfig;

    public NoDataLayer(NcAnimateConfigBean ncAnimateConfig) {
        this.ncAnimateConfig = ncAnimateConfig;
    }

    public void drawIntoImage(VectorRasterGraphics2D canvas, String layerPrefix, int scaledPanelWidth, int scaledPanelHeight, int scaledOffsetX, int scaledOffsetY) {
        String layerName = "Data not available";
        if (layerPrefix != null && !layerPrefix.isEmpty()) {
            layerName = layerPrefix + " " + layerName;
        }
        canvas.createLayer(layerName);

        NcAnimateRenderBean render = this.ncAnimateConfig.getRender();
        float scale = render.getScale();

        // Write "DATA NOT AVAILABLE"
        int scaledCentreX = scaledPanelWidth / 2;
        int scaledCentreY = scaledPanelHeight / 2;

        // Draw rectangle
        canvas.setPaint(DATA_NOT_AVAILABLE_BACKGROUND_COLOR);
        canvas.fill(new Rectangle(
                scaledCentreX - NcAnimateUtils.scale(DATA_NOT_AVAILABLE_BACKGROUND_WIDTH / 2, scale) + scaledOffsetX,
                scaledCentreY - NcAnimateUtils.scale(DATA_NOT_AVAILABLE_BACKGROUND_HEIGHT / 2, scale) + scaledOffsetY,
                NcAnimateUtils.scale(DATA_NOT_AVAILABLE_BACKGROUND_WIDTH, scale),
                NcAnimateUtils.scale(DATA_NOT_AVAILABLE_BACKGROUND_HEIGHT, scale)
        ));

        // Draw text
        int halfFontHeight = DATA_NOT_AVAILABLE_FONT_HEIGHT / 2;
        canvas.setPaint(DATA_NOT_AVAILABLE_TEXT_COLOR);
        canvas.setFont(new Font("SansSerif", Font.BOLD, NcAnimateUtils.scale(DATA_NOT_AVAILABLE_FONT_SIZE, scale)));

        canvas.drawString(DATA_NOT_AVAILABLE_LINE_1,
            scaledCentreX + scaledOffsetX,
            scaledCentreY + scaledOffsetY + NcAnimateUtils.scale(halfFontHeight - 20, scale),
            TextAlignment.CENTRE);

        canvas.drawString(DATA_NOT_AVAILABLE_LINE_2,
            scaledCentreX + scaledOffsetX,
            scaledCentreY + scaledOffsetY + NcAnimateUtils.scale(halfFontHeight + 20, scale),
            TextAlignment.CENTRE);
    }
}
