
package dev.galasa.framework.api.images;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class ImageRenderer {

    public void myFunction() throws Exception {
        throw new Exception("It failed!");
    }

    // public static void main(String[] args) {
    // try {
    // BufferedImage image = new BufferedImage(560, 351,
    // BufferedImage.TYPE_INT_ARGB);
    // Graphics2D g2d = image.createGraphics();
    // g2d.setColor(new Color(0, 0, 0));
    // g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

    // // Set font and text
    // int fontSize = 12;
    // g2d.setFont(new Font("Monospaced", Font.PLAIN, fontSize ));
    // g2d.setColor(new Color(0, 255, 0));
    // g2d.drawString("Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! Hello, World! Hello, World! Hello, World! Hello, World! Hello,
    // World! Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! Hello, World! Hello, World! Hello, World! Hello, World! Hello,
    // World! Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! Hello, World! Hello, World! Hello, World! Hello, World! Hello,
    // World! Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! Hello, World! Hello, World! Hello, World! Hello, World! Hello,
    // World! Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! Hello, World! Hello, World! Hello, World! Hello, World! Hello,
    // World! Hello, World! Hello, World! Hello, World! Hello, World! Hello, World!
    // Hello, World! ", 0, fontSize);

    // // Save the image
    // File imageFile = new File("black_image_with_green_text.png");
    // OutputStream out = new FileOutputStream(imageFile);
    // ImageIO.write(image, "png", out);
    // out.close();

    // System.out.println("Image saved as 'black_image_with_green_text.png'");

    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
}
