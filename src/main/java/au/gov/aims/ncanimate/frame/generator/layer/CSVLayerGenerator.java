/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateLayerBean;
import au.gov.aims.layers2svg.graphics.CSVShape;
import au.gov.aims.layers2svg.graphics.VectorRasterGraphics2D;
import au.gov.aims.sld.geom.Layer;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CSVLayerGenerator extends AbstractLayerGenerator {
    private boolean cached = false;
    private List<Layer> styledLayers;

    public CSVLayerGenerator(S3Client s3Client) {
        super(s3Client);
    }

    @Override
    public String getLayerType() {
        return "CSV";
    }

    @Override
    public void render(VectorRasterGraphics2D canvas, int leftScaledOffset, int topScaledOffset) throws Exception {
        if (!this.cached) {
            this.cached = true;

            File layerFile = this.getLayerFile();

            if (layerFile != null && layerFile.canRead()) {
                NcAnimateLayerBean layerConf = this.getLayerConf();
                Layer layer = this.getCSVLayer(this.getLayerTitle(), layerFile, layerConf.getLongitudeColumn(), layerConf.getLatitudeColumn());
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

    private Layer getCSVLayer(String layerName, File datasourceFile, String longitudeColumn, String latitudeColumn) throws IOException {
        CSVShape csvShape = new CSVShape(datasourceFile, layerName, longitudeColumn, latitudeColumn);
        csvShape.parse();
        Layer layer = new Layer(layerName);
        layer.add(csvShape);

        return layer;
    }
}
