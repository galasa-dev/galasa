/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

public interface IBufferHolder {

    String getStringWithoutNulls();
    
    char getChar();

}
