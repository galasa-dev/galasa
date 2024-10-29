/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Zos3270Terminal Manager
 * 
 * @galasa.manager Zos3270Terminal
 * 
 * @galasa.release.state Alpha
 * 
 * @galasa.description
 *
 * Enables 3270 terminal interactions with back-end application programs and subsystems. Live terminal updates are displayed in Eclipse and terminal images are logged to enable swift diagnosis of failures. The Confidential Text Filtering service enables confidential information such as passwords to be replaced with a numbered shield in these generated logs.
 * <br><br>
 * This Manager makes the following methods available:
 * <br><br>
 * <code>positionCursorToFieldContaining(<string>)</code> - which positions the cursor to a field containing a specific label
 * <br><br>
 * <code>tab()</code> - which presses the TAB key in the application under test
 * <br><br>
 * <code>type(<string>)</code> - where a sequence of characters are typed into an input field
 * <br><br> 
 * <code>enter()</code> - where the ENTER key is pressed
 * <br><br>
 *
 * There is an example which uses the Zos3270Terminal Manager in <a href="https://galasa.dev/docs/running-simbank-tests/simbank-IVT">the SimBank IVT</a> tutorial.
 * <br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/zos3270/package-summary.html">Javadoc documentation for this Manager here</a>.
 * <br><br>

 */
package dev.galasa.zos3270.spi;
