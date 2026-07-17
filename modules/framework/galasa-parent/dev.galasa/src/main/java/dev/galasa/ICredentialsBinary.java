/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

/**
 * Represents credentials stored as an opaque binary value.
 *
 * This credential type is used to store arbitrary binary data such as
 * licence JAR files, certificates in DER format, or any other binary
 * payload that should be made available to a test or manager at runtime.
 *
 * The binary data is stored and transmitted in base64-encoded form.
 */
public interface ICredentialsBinary extends ICredentials {

    /**
     * Get the raw bytes of the binary credential.
     *
     * @return the binary content as a byte array
     */
    byte[] getData();

    /**
     * Get the binary content as a base64-encoded string.
     *
     * This is the form used when storing the value in the Credentials Store
     * and when transmitting it via the Secrets REST API.
     *
     * @return base64-encoded representation of the binary data
     */
    String getEncodedData();
}
