/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

/**
 * Represents credentials of type {@code Opaque} in the Galasa Credentials Store.
 *
 * <p>An opaque secret holds a single, opaque blob of data whose internal structure
 * is not interpreted by the framework. Typical uses include licence JAR files, 
 * DER-encoded certificates, or any other binary payload that a manager needs at runtime.
 *
 * <p>When stored in the Credentials Store the value is base64-encoded and held
 * under a {@code .data} property key.  The same base64-encoded value is surfaced
 * in the {@code opaqueData} field of the {@code GalasaSecretData} REST response.
 *
 * @see ICredentials
 */
public interface ICredentialsOpaque extends ICredentials {

    /**
     * Returns the raw bytes of the opaque credential.
     *
     * <p>The returned array is decoded from the base64 value that was supplied
     * when the secret was created (via {@code --secret-file} or
     * {@code --base64-secret}).
     *
     * @return the opaque content as a byte array; never {@code null}
     */
    byte[] getData();

    /**
     * Returns the opaque content as a base64-encoded string.
     *
     * <p>This is the stored base64 value and is returned as-is in the
     * {@code opaqueData} field of the {@code GET /secrets/{name}} REST response.
     * Callers can base64-decode it once to obtain the original raw bytes.
     *
     * @return base64-encoded representation of the opaque data; never {@code null}
     */
    String getEncodedData();
}
