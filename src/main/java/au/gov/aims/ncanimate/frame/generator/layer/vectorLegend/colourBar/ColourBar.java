/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Marc Hammerton <m.hammerton@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame.generator.layer.vectorLegend.colourBar;

import uk.ac.rdg.resc.edal.graphics.style.MapImage;

import java.awt.image.BufferedImage;

public interface ColourBar {

    /**
     * Create a buffered image from the mapImage
     * @param mapImage The mapImage from which to create the colour bar.
     * @param scaledWidth The scaled width of the colour bar.
     * @param scaledHeight The scaled height of the colour bar.
     * @return The colour bar as buffered image.
     */
    BufferedImage createImage(MapImage mapImage, int scaledWidth, int scaledHeight);
}
