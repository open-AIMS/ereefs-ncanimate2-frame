/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.layers2svg.Layers2SVGUtils;
import au.gov.aims.layers2svg.graphics.GeoJSONShape;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.sld.geom.Layer;
import org.json.JSONObject;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GeoJSONLayerGenerator extends AbstractLayerGenerator {
    private boolean cached = false;
    private List<Layer> styledLayers;

    public GeoJSONLayerGenerator(S3Client s3Client) {
        super(s3Client);
    }

    @Override
    public String getLayerType() {
        return "GeoJSON";
    }

    @Override
    public void render(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {
        if (!this.cached) {
            this.cached = true;

            File layerFile = this.getLayerFile();

            if (layerFile != null && layerFile.canRead()) {
                Layer layer = this.getGeoJSONLayer(this.getLayerTitle(), layerFile);
                this.styledLayers = this.styleLayer(layer);
            }
        }

        if (this.styledLayers != null && !this.styledLayers.isEmpty()) {
            for (Layer styledLayer : this.styledLayers) {
                styledLayer.setName(this.getLayerTitle());
                canvas.fillAndStroke(styledLayer.createTransformedLayer(AffineTransform.getTranslateInstance(leftScaledOffset, topScaledOffset)));
            }
        }
    }

    private Layer getGeoJSONLayer(String layerName, File datasourceFile) throws IOException {
        InputStream datasourceStream = null;
        JSONObject datasourceJson = null;
        try {
            datasourceStream = new FileInputStream(datasourceFile);
            String datasourceString = Layers2SVGUtils.readFile(datasourceStream);
            datasourceJson = new JSONObject(datasourceString);
        } finally {
            if (datasourceStream != null) {
                datasourceStream.close();
            }
        }

        GeoJSONShape datasourceGeoJSONShape = new GeoJSONShape(datasourceJson, layerName);
        datasourceGeoJSONShape.parse();
        Layer layer = new Layer(layerName);
        layer.add(datasourceGeoJSONShape);

        return layer;
    }
}
