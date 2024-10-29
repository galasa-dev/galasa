/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

public enum AttentionIdentification {

    ENTER((byte) 0x7d),
    CLEAR((byte) 0x6d),
    PF1((byte) 0xf1),
    PF2((byte) 0xf2),
    PF3((byte) 0xf3),
    PF4((byte) 0xf4),
    PF5((byte) 0xf5),
    PF6((byte) 0xf6),
    PF7((byte) 0xf7),
    PF8((byte) 0xf8),
    PF9((byte) 0xf9),
    PF10((byte) 0x7a),
    PF11((byte) 0x7b),
    PF12((byte) 0x7c),
    PF13((byte) 0xc1),
    PF14((byte) 0xc2),
    PF15((byte) 0xc3),
    PF16((byte) 0xc4),
    PF17((byte) 0xc5),
    PF18((byte) 0xc6),
    PF19((byte) 0xc7),
    PF20((byte) 0xc8),
    PF21((byte) 0xc9),
    PF22((byte) 0x4a),
    PF23((byte) 0x4b),
    PF24((byte) 0x4c),
    PA1((byte) 0x6c),
    PA2((byte) 0x6e),
    PA3((byte) 0x6b),
    NONE((byte) 0x60),
    STRUCTURED_FIELD((byte) 0x88);

    private final byte keyValue;

    AttentionIdentification(byte keyValue) {
        this.keyValue = keyValue;
    }

    public byte getKeyValue() {
        return this.keyValue;
    }

    public static AttentionIdentification valueOfAid(byte aid) {
        for (AttentionIdentification a : values()) {
            if (a.keyValue == aid) {
                return a;
            }
        }

        return null;
    }

}
