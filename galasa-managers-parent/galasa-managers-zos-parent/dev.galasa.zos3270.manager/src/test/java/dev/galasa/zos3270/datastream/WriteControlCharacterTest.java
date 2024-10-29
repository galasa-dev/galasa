/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;

public class WriteControlCharacterTest {

    @Test
    public void testWCC1() throws Exception {
        WriteControlCharacter wcc = new WriteControlCharacter((byte) 0xf0);

        Assert.assertTrue("bit 7 is wrong", wcc.isNop());
        Assert.assertTrue("bit 6 is wrong", wcc.isReset());
        Assert.assertTrue("bit 5 is wrong", wcc.isPrinter1());
        Assert.assertTrue("bit 4 is wrong", wcc.isPrinter2());
        Assert.assertFalse("bit 3 is wrong", wcc.isStartPrinter());
        Assert.assertFalse("bit 2 is wrong", wcc.isSoundAlarm());
        Assert.assertFalse("bit 1 is wrong", wcc.isKeyboardReset());
        Assert.assertFalse("bit 0 is wrong", wcc.isResetMDT());
    }

    @Test
    public void testWCC2() throws Exception {
        WriteControlCharacter wcc = new WriteControlCharacter((byte) 0x0f);

        Assert.assertFalse("bit 7 is wrong", wcc.isNop());
        Assert.assertFalse("bit 6 is wrong", wcc.isReset());
        Assert.assertFalse("bit 5 is wrong", wcc.isPrinter1());
        Assert.assertFalse("bit 4 is wrong", wcc.isPrinter2());
        Assert.assertTrue("bit 3 is wrong", wcc.isStartPrinter());
        Assert.assertTrue("bit 2 is wrong", wcc.isSoundAlarm());
        Assert.assertTrue("bit 1 is wrong", wcc.isKeyboardReset());
        Assert.assertTrue("bit 0 is wrong", wcc.isResetMDT());
    }

    @Test
    public void testWCC3() throws Exception {
        WriteControlCharacter wcc = new WriteControlCharacter((byte) 0xaa);

        Assert.assertTrue("bit 7 is wrong", wcc.isNop());
        Assert.assertFalse("bit 6 is wrong", wcc.isReset());
        Assert.assertTrue("bit 5 is wrong", wcc.isPrinter1());
        Assert.assertFalse("bit 4 is wrong", wcc.isPrinter2());
        Assert.assertTrue("bit 3 is wrong", wcc.isStartPrinter());
        Assert.assertFalse("bit 2 is wrong", wcc.isSoundAlarm());
        Assert.assertTrue("bit 1 is wrong", wcc.isKeyboardReset());
        Assert.assertFalse("bit 0 is wrong", wcc.isResetMDT());
    }

    @Test
    public void testWCC4() throws Exception {
        WriteControlCharacter wcc = new WriteControlCharacter((byte) 0x55);

        Assert.assertFalse("bit 7 is wrong", wcc.isNop());
        Assert.assertTrue("bit 6 is wrong", wcc.isReset());
        Assert.assertFalse("bit 5 is wrong", wcc.isPrinter1());
        Assert.assertTrue("bit 4 is wrong", wcc.isPrinter2());
        Assert.assertFalse("bit 3 is wrong", wcc.isStartPrinter());
        Assert.assertTrue("bit 2 is wrong", wcc.isSoundAlarm());
        Assert.assertFalse("bit 1 is wrong", wcc.isKeyboardReset());
        Assert.assertTrue("bit 0 is wrong", wcc.isResetMDT());
    }

}
