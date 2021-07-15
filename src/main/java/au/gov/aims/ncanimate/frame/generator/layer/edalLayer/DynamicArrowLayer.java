/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.edalLayer;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import org.apache.log4j.Logger;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.graphics.style.GriddedImageLayer;
import uk.ac.rdg.resc.edal.graphics.utils.VectorFactory;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

// Inspired from uk.ac.rdg.resc.edal.graphics.style.ArrowLayer
public class DynamicArrowLayer extends GriddedImageLayer {
    private static final Logger LOGGER = Logger.getLogger(DynamicArrowLayer.class);

    private static final int DEFAULT_ARROW_SIZE = 20;
    private static final Color DEFAULT_ARROW_COLOUR = Color.black;
    private static final Color DEFAULT_ARROW_BACKGROUND = new Color(0, true);

    private String directionFieldName;
    private String magnitudeFieldName;

    private NetCDFUtils.DataDomain magnitudeDataDomain;
    private Float directionTurns; // 360 for degrees. See: https://en.wikipedia.org/wiki/Angular_unit
    private Float northAngle;

    private Color arrowColour;
    private Color arrowBackground;

    private int arrowSize;

    public enum ArrowStyle {
        UPSTREAM, THIN_ARROW, FAT_ARROW, TRI_ARROW, WIND_BARBS, DYNA_FAT_ARROW
    };

    private DynamicArrowLayer.ArrowStyle arrowStyle = DynamicArrowLayer.ArrowStyle.UPSTREAM;

    public DynamicArrowLayer(
            String directionFieldName, Float directionTurns,
            String magnitudeFieldName, NetCDFUtils.DataDomain magnitudeDataDomain,
            Float northAngle,
            Integer arrowSize, Color arrowColour,
            Color arrowBackground, DynamicArrowLayer.ArrowStyle arrowStyle) {
        this.directionFieldName = directionFieldName;
        this.magnitudeFieldName = magnitudeFieldName;

        this.magnitudeDataDomain = magnitudeDataDomain;
        this.directionTurns = directionTurns;
        this.northAngle = northAngle;

        this.arrowColour = arrowColour == null ? DEFAULT_ARROW_COLOUR : arrowColour;
        this.arrowBackground = arrowBackground == null ? DEFAULT_ARROW_BACKGROUND : arrowBackground;
        this.setArrowSize(arrowSize);
        this.arrowStyle = arrowStyle;
    }

    private void setArrowSize(Integer arrowSize) {
        if (arrowSize == null || arrowSize < 1) {
            LOGGER.warn(String.format("Invalid arrow size %s. Arrow size must be non-null and > 0. Using default %d", arrowSize, DEFAULT_ARROW_SIZE));
            this.arrowSize = DEFAULT_ARROW_SIZE;
        } else {
            this.arrowSize = arrowSize;
        }
    }

    private static Path2D getDynamicArrowVector(float lengthRatio) {
        float length = 25 * lengthRatio;

        Path2D path = new Path2D.Double();
        path.moveTo(0, 1);
        path.lineTo(0, -1);
        path.lineTo(6 + length , -1);
        path.lineTo(length, -8);
        path.lineTo(12 + length, 0);
        path.lineTo(length, 8);
        path.lineTo(6 + length, 1);
        path.closePath();

        return path;
    }

    public static void renderVector(
            Path2D arrow,
            double angle,
            int i, int j,
            float scale,
            Graphics2D g
    ) {
        Path2D ret = (Path2D) arrow.clone();
        /* Rotate and set position */
        ret.transform(AffineTransform.getRotateInstance(-Math.PI / 2));
        ret.transform(AffineTransform.getRotateInstance(angle));
        ret.transform(AffineTransform.getScaleInstance(scale, scale));
        ret.transform(AffineTransform.getTranslateInstance(i, j));

        g.fill(ret);
        g.draw(ret);
    }


    @Override
    protected void drawIntoImage(BufferedImage image, MapFeatureDataReader dataReader)
            throws EdalException {
        Array2D<Number> dirValues = this.directionFieldName == null ? null : dataReader.getDataForLayerName(this.directionFieldName);
        Array2D<Number> magValues = this.magnitudeFieldName == null ? null : dataReader.getDataForLayerName(this.magnitudeFieldName);

        Graphics2D g = image.createGraphics();
        g.setColor(this.arrowBackground);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(this.arrowColour);

        int width = image.getWidth();
        int height = image.getHeight();

        /*
         * Calculate the (floating point) number of pixels per arrow. In ideal
         * situations, this will be an integer equal to the arrow size * 2
         *
         * For non-ideal situations it means that the arrows will not be evenly
         * spaced (they will be either n or n+1 pixels apart). They will tile
         * perfectly though.
         *
         * NOTE: The right side of the division "width / (this.arrowSize * 2)" are Integer operations.
         *     That's almost the same as "this.arrowSize * 2.0", quantised to an integer grid.
         */
        double xPixelsPerArrow = ((double) width) / (width / (this.arrowSize * 2));
        double yPixelsPerArrow = ((double) height) / (height / (this.arrowSize * 2));
        double xLoc = xPixelsPerArrow / 2;
        double yLoc = yPixelsPerArrow / 2;

        Array<HorizontalPosition> domainObjects = dataReader
                .getMapDomainObjects(this.directionFieldName);

        for (int j = 0; j < height; j++) {
            if (yLoc > yPixelsPerArrow) {
                yLoc -= yPixelsPerArrow;
                for (int i = 0; i < width; i++) {
                    if (xLoc > xPixelsPerArrow) {
                        xLoc -= xPixelsPerArrow;

                        /*
                         * We are at a point where we need to draw an arrow
                         */
                        HorizontalPosition position = domainObjects.get(j, i);
                        Double angle = dirValues == null ? null : GISUtils.transformWgs84Heading(dirValues.get(j, i), position);
                        Double mag = magValues == null ? null : GISUtils.transformWgs84Heading(magValues.get(j, i), position);

                        float normalisedMag = 1;
                        float magArrowSize = this.arrowSize;
                        if (mag != null && this.magnitudeDataDomain != null) {
                            // normalisedMag = magnitude between [0, 1]
                            normalisedMag = (float)((mag - this.magnitudeDataDomain.getMin()) / this.magnitudeDataDomain.getMax());
                            if (normalisedMag < 0) normalisedMag = 0;
                            if (normalisedMag > 1) normalisedMag = 1;

                            // magArrowSize = size of the arrow relative to magnitude value
                            magArrowSize = this.arrowSize * normalisedMag;
                        }

                        if (angle != null && !Float.isNaN(angle.floatValue())) {
                            double radianAngle = this.getNormalisedRadianAngle(angle);

                            switch (this.arrowStyle) {
                                case DYNA_FAT_ARROW:
                                    DynamicArrowLayer.renderVector(
                                            DynamicArrowLayer.getDynamicArrowVector(normalisedMag),
                                            radianAngle,
                                            i, j,
                                            this.arrowSize / 20f,
                                            g
                                    );
                                    break;

                                case UPSTREAM:
                                    /* Calculate the end point of the arrow */
                                    double iEnd = i + magArrowSize * Math.sin(radianAngle);
                                    /*
                                     * Screen coordinates go down, but north is up,
                                     * hence the minus sign
                                     */
                                    double jEnd = j - magArrowSize * Math.cos(radianAngle);
                                    /* Draw a dot representing the data location */
                                    g.fillOval(i - 2, j - 2, 4, 4);
                                    /* Draw a line representing the vector direction */
                                    g.setStroke(new BasicStroke(1));
                                    g.drawLine(i, j, (int) Math.round(iEnd), (int) Math.round(jEnd));
                                    break;

                                case FAT_ARROW:
                                    VectorFactory.renderVector("STUMPVEC", radianAngle,
                                            i, j, magArrowSize / 11f, g);
                                    break;

                                case TRI_ARROW:
                                    VectorFactory.renderVector("TRIVEC", radianAngle,
                                            i, j, magArrowSize / 11f, g);
                                    break;

                                case THIN_ARROW:
                                default:
                                    /*
                                     * The overall arrow size is 11 for things
                                     * returned from the VectorFactory, so we divide
                                     * the arrow size by 11 to get the scale factor.
                                     */
                                    VectorFactory.renderVector("LINEVEC", radianAngle,
                                            i, j, magArrowSize / 11f, g);
                                    break;
                            }
                        }
                    }
                    xLoc += 1.0;
                }
            }
            yLoc += 1.0;
        }
    }

    public double getNormalisedRadianAngle(double angle) {
        // Convert the angle to degrees
        double degreeAngle = angle;
        if (this.directionTurns != null && this.directionTurns != 360.0 && this.directionTurns != 0) {
            degreeAngle = degreeAngle * 360.0 / this.directionTurns;
        }

        // Put North to the North
        if (this.northAngle != null && this.northAngle != 0) {
            degreeAngle -= this.northAngle;
        }

        // Convert degree to radian
        return degreeAngle * GISUtils.DEG2RAD;
    }

    @Override
    public Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new HashSet<NameAndRange>();
        ret.add(new NameAndRange(this.directionFieldName, Extents.newExtent(0f, 360f)));
        return ret;
    }

    @Override
    public MetadataFilter getMetadataFilter() {
        return new MetadataFilter() {
            @Override
            public boolean supportsMetadata(VariableMetadata metadata) {
                /*
                 * Arrows should only be plotted for layers where the units are
                 * "degrees"
                 */
                return metadata.getParameter().getUnits().equalsIgnoreCase("degrees");
            }
        };
    }
}
