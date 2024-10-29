/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.util.BitSet;

public class WriteControlCharacter {

    private boolean nop;
    private boolean reset;
    private boolean printer1;
    private boolean printer2;
    private boolean startPrinter;
    private boolean soundAlarm;
    private boolean keyboardReset;
    private boolean resetMDT;
    
    public WriteControlCharacter() {
    }

    public WriteControlCharacter(byte wcc) {
        BitSet bitSet = BitSet.valueOf(new byte[] { wcc });
        nop = bitSet.get(7);
        reset = bitSet.get(6);
        printer1 = bitSet.get(5);
        printer2 = bitSet.get(4);
        startPrinter = bitSet.get(3);
        soundAlarm = bitSet.get(2);
        keyboardReset = bitSet.get(1);
        resetMDT = bitSet.get(0);
    }

    public WriteControlCharacter(boolean nop, boolean reset, boolean printer1, boolean printer2, boolean startPrinter,
            boolean soundAlarm, boolean keyboardReset, boolean resetMDT) {
        this.nop = nop;
        this.reset = reset;
        this.printer1 = printer1;
        this.printer2 = printer2;
        this.startPrinter = startPrinter;
        this.soundAlarm = soundAlarm;
        this.keyboardReset = keyboardReset;
        this.resetMDT = resetMDT;
    }

    public boolean isNop() {
        return nop;
    }

    public boolean isReset() {
        return reset;
    }

    public boolean isPrinter1() {
        return printer1;
    }

    public boolean isPrinter2() {
        return printer2;
    }

    public boolean isStartPrinter() {
        return startPrinter;
    }

    public boolean isSoundAlarm() {
        return soundAlarm;
    }

    public boolean isKeyboardReset() {
        return keyboardReset;
    }

    public boolean isResetMDT() {
        return resetMDT;
    }

    public byte[] getBytes() {
        BitSet bitSet = new BitSet(8);
        bitSet.set(7, nop);
        bitSet.set(6, reset);
        bitSet.set(5, printer1);
        bitSet.set(4, printer2);
        bitSet.set(3, startPrinter);
        bitSet.set(2, soundAlarm);
        bitSet.set(1, keyboardReset);
        bitSet.set(0, resetMDT);

        return bitSet.toByteArray();
    }

}
