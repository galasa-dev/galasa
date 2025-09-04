/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.images;

// import java.awt.Color;
// import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import dev.galasa.zos3270.common.screens.TerminalImage;

public class ImageRenderer {

    public byte[]  render(
        TerminalImage imageDescriptionToRender, 
        ImageFormat desiredFormat
    ) throws Exception {

        // imageDescriptionToRender.getImageSize();

        BufferedImage image = new BufferedImage(560, 351,
        BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();

        // Paint the entire image with a background color.
        canvas.setColor(ImageColor.BLACK.getRGBColor());
        canvas.fillRect(0, 0, image.getWidth(), image.getHeight());

        // Set font and text
        // int fontSize = 12;
        // canvas.setFont(new Font("Monospaced", Font.PLAIN, fontSize ));
        // canvas.setColor(ImageColor.GREEN.getRGBColor());

        // int x = 0;
        // int y = 0;
        // canvas.drawString("Hello, World!", x,y );

        // Save the graphics as a byte array
        ByteArrayOutputStream renderedImage = new ByteArrayOutputStream();
        ImageIO.write(image, desiredFormat.getValue(), renderedImage);
        renderedImage.close();

        byte[] imageBytes = renderedImage.toByteArray();

        return imageBytes;
    }
}
