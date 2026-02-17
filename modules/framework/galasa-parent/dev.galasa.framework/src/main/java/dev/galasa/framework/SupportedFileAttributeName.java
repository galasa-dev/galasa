/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

// Expected attributes which are available to use are :
// "size" with a value of class: class java.lang.Long which renders to a string of:"910" (for example)
// "ras:contentType" with a value of class: class java.lang.String which renders to a string of:"text/plain" (for example)
// "posix:size" with a value of class: class java.lang.Long which renders to a string of:"910" (for example)
public enum SupportedFileAttributeName {
    SIZE("size"),
    CONTENT_TYPE("ras:contentType"),
    POSIX_SIZE("posix:size");

    private String value ;

    SupportedFileAttributeName(String value){
        this.value = value ;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return this.value;
    }
}
