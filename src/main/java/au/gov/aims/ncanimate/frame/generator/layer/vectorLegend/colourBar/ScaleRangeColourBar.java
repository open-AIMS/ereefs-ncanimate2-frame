/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.colourBar;

import uk.ac.rdg.resc.edal.graphics.style.Drawable;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.utils.LegendDataGenerator;

import java.awt.image.BufferedImage;

public class ScaleRangeColourBar implements ColourBar {

    private final Drawable.NameAndRange nameAndRange;
    private final float extraAmountOutOfRangeLow;
    private final float extraAmountOutOfRangeHigh;
    
    public ScaleRangeColourBar(Drawable.NameAndRange nameAndRange, float extraAmountOutOfRangeLow, 
                               float extraAmountOutOfRangeHigh) {
        this.nameAndRange = nameAndRange;
        this.extraAmountOutOfRangeLow = extraAmountOutOfRangeLow;
        this.extraAmountOutOfRangeHigh = extraAmountOutOfRangeHigh;
    }

    @Override
    public BufferedImage createImage(MapImage mapImage, int scaledWidth, int scaledHeight) {
        // Inspired from
        //     uk.ac.rdg.resc.edal.graphics.style.MapImage.getLegend(...)
        // Get the data for the colourbar and draw it.
        LegendDataGenerator dataGenerator = new LegendDataGenerator(scaledWidth,
                scaledHeight, null, this.extraAmountOutOfRangeLow,
                this.extraAmountOutOfRangeHigh, this.extraAmountOutOfRangeLow,
                this.extraAmountOutOfRangeHigh);
        
        return mapImage.drawImage(dataGenerator.getPlottingDomainParams(),
                dataGenerator.getFeatureCatalogue(null, this.nameAndRange));
    }
}
