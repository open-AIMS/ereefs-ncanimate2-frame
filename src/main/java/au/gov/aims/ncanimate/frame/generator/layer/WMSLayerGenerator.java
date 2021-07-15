/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.ncanimate.commons.NcAnimateUtils;
import org.apache.http.client.utils.URIBuilder;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class WMSLayerGenerator extends AbstractLayerGenerator {
    private boolean cached = false;
    private BufferedImage dataImage;

    public WMSLayerGenerator(S3Client s3Client) {
        super(s3Client);
    }

    @Override
    public String getLayerType() {
        return "WMS";
    }

    @Override
    public void render(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {
        if (!this.cached) {
            this.cached = true;

            URL url = this.getWMSLayerImageURL();
            this.dataImage = ImageIO.read(url);
            this.dataImage.flush();
        }

        if (this.dataImage != null) {
            canvas.createLayer(this.getLayerTitle());
            canvas.drawImage(this.dataImage, leftScaledOffset, topScaledOffset, null);
        }
    }

    private URL getWMSLayerImageURL() throws URISyntaxException, MalformedURLException {
        NcAnimateLayerBean layerConf = this.getLayerConf();

        BoundingBox bbox = this.getBoundingBox();
        int width = this.getScaledPanelWidth();
        int height = this.getScaledPanelHeight();

        String serverUrl = NcAnimateUtils.parseString(layerConf.getServer(), this.getContext(), this.getLayerContextMap());
        String layerName = NcAnimateUtils.parseString(layerConf.getLayerName(), this.getContext(), this.getLayerContextMap());
        String styleName = NcAnimateUtils.parseString(layerConf.getStyleName(), this.getContext(), this.getLayerContextMap());

        /*
        Example of a GetMap URL, used for reference.
        https://maps.eatlas.org.au:80/maps/wms
            ?SERVICE=WMS
            &LAYERS=ea-be%3AWorld_Bright-Earth-e-Atlas-basemap
            &TRANSPARENT=TRUE
            &VERSION=1.1.1
            &REQUEST=GetMap
            &STYLES=
            &FORMAT=image%2Fpng
            &SRS=EPSG%3A4326
            &BBOX=143.4375,-19.6875,146.25,-16.875
            &WIDTH=256
            &HEIGHT=256
         */
        URIBuilder uriBuilder = new URIBuilder(serverUrl);
        uriBuilder.addParameter("SERVICE", "WMS");
        uriBuilder.addParameter("LAYERS", layerName);
        uriBuilder.addParameter("TRANSPARENT", "TRUE");
        uriBuilder.addParameter("VERSION", "1.1.1");
        uriBuilder.addParameter("REQUEST", "GetMap");
        if (styleName != null) {
            uriBuilder.addParameter("STYLES", styleName);
        }
        uriBuilder.addParameter("FORMAT", "image/png");
        uriBuilder.addParameter("SRS", "EPSG:4326");
        uriBuilder.addParameter("BBOX", bbox.getMinX() + "," + bbox.getMinY() + "," + bbox.getMaxX() + "," + bbox.getMaxY());
        uriBuilder.addParameter("WIDTH", "" + width);
        uriBuilder.addParameter("HEIGHT", "" + height);
        return uriBuilder.build().toURL();
    }
}
