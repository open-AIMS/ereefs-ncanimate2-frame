/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ncanimate.frame;

import au.gov.aims.junit.AssertImage;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Used to test the computer's environment.
 */
public class EnvironmentTest {
    private static final Logger LOGGER = Logger.getLogger(EnvironmentTest.class);

    /**
     * Test if the environment have some sort of font library.
     * If it's not the case, the following test will crash or product a white image.
     */
    @Test
    public void testFont() throws Exception {
        File outputFile = File.createTempFile("ncanimateTest_testFont_", ".png");
        int width = 150, height = 40;

        BufferedImage g2dImage = new BufferedImage(width, height, 2);
        Graphics2D g2d = g2dImage.createGraphics();

        // Paint background white
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set font (and check if it exists)
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        Assert.assertNotNull("FontMetrics is null. Install a font library such as 'ttf-freefont'", fontMetrics);

        LOGGER.info("FontMetrics: " + fontMetrics);

        // Write black text on the image (roughly centered vertically)
        int textHeight = fontMetrics.getHeight();
        g2d.setColor(Color.BLACK);
        g2d.drawString("Test Font", 15f, (height + textHeight) / 2);

        // Save the image to file
        ImageIO.write(g2dImage, "png", outputFile);


        // Check if the image has been generated
        Assert.assertTrue(String.format("The output image doesn't exist: %s", outputFile), outputFile.exists());


        // Check if it looks like what we expect

        // Create File objects referring to the expected image (and the "not expected" image).
        File blankImage = AssertImage.getResourceFile("expectedImages/testFont_blank.png");
        File filledImage = AssertImage.getResourceFile("expectedImages/testFont_filled.png");

        // Check that the generated image is not a completely white.
        AssertImage.assertNotEquals("The output image is completely white. No text hasn't been written on it.",
                blankImage, outputFile, 0);

        // Check that it's not almost completely white.
        AssertImage.assertNotEquals("The output image is almost completely white. The text hasn't been written on it properly.",
                blankImage, outputFile, 0.01);

        // Check that it's mostly as expected, considering font discrepancy (allowed 15% difference).
        AssertImage.assertEquals("The output image is widely different from expectation.",
                filledImage, outputFile, 0.15);


        // Cleanup
        outputFile.delete();
    }
}
