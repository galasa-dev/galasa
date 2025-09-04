/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;

import static org.assertj.core.api.Assertions.*;

public class TestImageRenderer {

  @Rule
  public TestName testName = new TestName();

  private void saveImage(String fileNameUniquePart, ImageFormat format, byte[] imageBytes) throws IOException {
    // Make sure the images folder exists.
    String folderName = "images/"+this.getClass().getSimpleName();
    new File(folderName).mkdirs();

    // Dump the binary data into the file
    String fileName = folderName + "/" + testName.getMethodName() + "-" + fileNameUniquePart + "."+format.getValue();
    try (FileOutputStream out = new FileOutputStream(fileName)) {
      out.write(imageBytes);
    }
  }

  @Test
  public void testCanCreateABlankImage() throws Exception {

    // Given
    ImageRenderer renderer = new ImageRenderer();

    int sequence = 23;
    String id = "imageId123";
    boolean isInbound = false;
    String interactionType = "interactionType";
    String attentionIdForOutboundMessages = "Hi";
    int cursorColumn = 10 ;
    int cursorRow = 4;

    int columns = 80;
    int rows = 24;
    TerminalSize imageSize = new TerminalSize(columns,rows);

    TerminalImage image = new TerminalImage( 
            sequence,  id, isInbound, interactionType, attentionIdForOutboundMessages,
            imageSize, cursorColumn, cursorRow);

    ImageFormat desiredFormat = ImageFormat.PNG;

    // When
    byte[] imageBytes = renderer.render(image, desiredFormat);

    // Then
    assertThat(imageBytes).isNotNull().isNotEmpty();
    assertThat(imageBytes.length).isGreaterThan(40);

    // So we can view the image manually... save it to disk.
    saveImage("1", ImageFormat.PNG, imageBytes);
  }


  @Test
  public void testCanCreateABlackPageWithHelloTopLeft() throws Exception {

    // Given
    ImageRenderer renderer = new ImageRenderer();

    int sequence = 23;
    String id = "imageId123";
    boolean isInbound = false;
    String interactionType = "interactionType";
    String attentionIdForOutboundMessages = "Hi";
    int cursorColumn = 10 ;
    int cursorRow = 4;

    int columns = 80;
    int rows = 24;
    TerminalSize imageSize = new TerminalSize(columns,rows);

    TerminalImage image = new TerminalImage( 
            sequence,  id, isInbound, interactionType, attentionIdForOutboundMessages,
            imageSize, cursorColumn, cursorRow);

    // Add a text field top-left
    int row = 0;
    int column= 0; 
    boolean isUnFormatted = false;
    boolean isFieldProtected = false;
    boolean isFieldNumeric = false;
    boolean isFieldDisplayed = true;
    boolean isFieldIntenseDisplay = false;
    boolean isFieldSelectorPen = false;
    boolean isFieldModified = false;
    Character foregroundColour = ImageColor.GREEN.getShortName();
    Character backgroundColour = ImageColor.BLACK.getShortName();
    Character highlight = 'a';

    // Create a field to contain some text
    TerminalField field = new TerminalField(row,column,isUnFormatted,isFieldProtected,isFieldNumeric, 
    isFieldDisplayed, isFieldIntenseDisplay, isFieldSelectorPen, isFieldModified, 
    foregroundColour , backgroundColour, highlight);

    // Add some text to the field
    String message = "Hello";
    char[] chars = message.toCharArray();
    Character[] characters = new Character[chars.length];
    int i = 0 ;
    for(i = 0 ; i<chars.length; i++) {
      characters[i] = chars[i];
    }
    FieldContents fieldContents = new FieldContents(characters);
    field.getContents().add(fieldContents);

    // Add the field to the image
    image.getFields().add(field);

    ImageFormat desiredFormat = ImageFormat.PNG;

    // When
    byte[] imageBytes = renderer.render(image, desiredFormat);

    // Then
    assertThat(imageBytes).isNotNull().isNotEmpty();
    assertThat(imageBytes.length).isGreaterThan(40);

    // So we can view the image manually... save it to disk.
    saveImage("1", ImageFormat.PNG, imageBytes);
  }
}
