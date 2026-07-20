/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import static org.assertj.core.api.Assertions.*;

import java.util.Base64;
import java.util.Properties;

import org.junit.Test;

import dev.galasa.ICredentialsOpaque;
import dev.galasa.framework.spi.creds.CredentialsOpaque;
import dev.galasa.framework.spi.creds.CredentialsException;

/**
 * Unit tests for {@link CredentialsOpaque}.
 */
public class CredentialsOpaqueTest {

    private static final byte[] SAMPLE_BYTES = new byte[] { 0x50, 0x4B, 0x03, 0x04, 0x00, 0x01, 0x02 }; // ZIP magic + filler
    private static final String ENCODED_SAMPLE = Base64.getEncoder().encodeToString(SAMPLE_BYTES);

    // -------------------------------------------------------------------------
    // Plain constructor — happy paths
    // -------------------------------------------------------------------------

    /**
     * Test that valid base64 is accepted and getData() returns the decoded bytes.
     */
    @Test
    public void testConstructWithValidBase64ReturnsDecodedBytes() throws Exception {
        // When...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);

        // Then...
        assertThat(creds.getData()).isEqualTo(SAMPLE_BYTES);
    }

    /**
     * Test that getEncodedData() returns the original base64 string unchanged.
     */
    @Test
    public void testGetEncodedDataReturnsOriginalBase64String() throws Exception {
        // When...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);

        // Then...
        assertThat(creds.getEncodedData()).isEqualTo(ENCODED_SAMPLE);
    }

    // -------------------------------------------------------------------------
    // Plain constructor — error paths
    // -------------------------------------------------------------------------

    /**
     * Test that a null data value throws CredentialsException.
     */
    @Test
    public void testConstructWithNullDataThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque(null))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Opaque credential data cannot be null or empty");
    }

    /**
     * Test that an empty string throws CredentialsException.
     */
    @Test
    public void testConstructWithEmptyStringThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque(""))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Opaque credential data cannot be null or empty");
    }

    /**
     * Test that a blank (whitespace-only) string throws CredentialsException.
     */
    @Test
    public void testConstructWithBlankStringThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque("   "))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Opaque credential data cannot be null or empty");
    }

    /**
     * Test that an invalid base64 string throws CredentialsException.
     */
    @Test
    public void testConstructWithInvalidBase64ThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque("this is not valid base64!!!"))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Failed to base64-decode opaque credential data");
    }

    // -------------------------------------------------------------------------
    // Storage constructor (null key = file-based / unencrypted)
    // -------------------------------------------------------------------------

    /**
     * Test that the storage constructor with a null key treats the value as
     * plain base64 (file-based, unencrypted path).
     */
    @Test
    public void testStorageConstructorWithNullKeyTreatsValueAsPlainBase64() throws Exception {
        // When...
        CredentialsOpaque creds = new CredentialsOpaque(null, ENCODED_SAMPLE);

        // Then...
        assertThat(creds.getData()).isEqualTo(SAMPLE_BYTES);
        assertThat(creds.getEncodedData()).isEqualTo(ENCODED_SAMPLE);
    }

    /**
     * Test that the storage constructor with a null key and null data throws CredentialsException.
     */
    @Test
    public void testStorageConstructorWithNullKeyAndNullDataThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque(null, null))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Opaque credential data cannot be null or empty");
    }

    /**
     * Test that the storage constructor with a null key and invalid base64 throws CredentialsException.
     */
    @Test
    public void testStorageConstructorWithNullKeyAndInvalidBase64ThrowsCredentialsException() {
        assertThatThrownBy(() -> new CredentialsOpaque(null, "!!!invalid!!!"))
            .isInstanceOf(CredentialsException.class)
            .hasMessageContaining("Failed to base64-decode opaque credential data");
    }

    // -------------------------------------------------------------------------
    // toProperties()
    // -------------------------------------------------------------------------

    /**
     * Test that toProperties() writes the base64-encoded data under the expected key.
     */
    @Test
    public void testToPropertiesWritesDataUnderExpectedKey() throws Exception {
        // Given...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);

        // When...
        Properties props = creds.toProperties("MY_LICENSE");

        // Then...
        assertThat(props).isNotNull();
        assertThat(props.getProperty("secure.credentials.MY_LICENSE.data")).isEqualTo(ENCODED_SAMPLE);
    }

    /**
     * Test that toProperties() contains exactly the data key and no extra keys.
     */
    @Test
    public void testToPropertiesContainsOnlyDataKey() throws Exception {
        // Given...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);

        // When...
        Properties props = creds.toProperties("MY_LICENSE");

        // Then...
        assertThat(props).hasSize(1);
    }

    /**
     * Test that toProperties() round-trips: the stored value can be decoded back to the original bytes.
     */
    @Test
    public void testToPropertiesStoredValueDecodesBackToOriginalBytes() throws Exception {
        // Given...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);
        Properties props = creds.toProperties("MY_LICENSE");

        // When...
        String storedValue = props.getProperty("secure.credentials.MY_LICENSE.data");
        byte[] decoded = Base64.getDecoder().decode(storedValue);

        // Then...
        assertThat(decoded).isEqualTo(SAMPLE_BYTES);
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    /**
     * Test that description metadata can be set and retrieved.
     */
    @Test
    public void testMetadataDescriptionCanBeSetAndRetrieved() throws Exception {
        // Given...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);

        // When...
        creds.setDescription("My license JAR");
        creds.setLastUpdatedByUser("operator");

        // Then...
        assertThat(creds.getDescription()).isEqualTo("My license JAR");
        assertThat(creds.getLastUpdatedByUser()).isEqualTo("operator");
    }

    /**
     * Test that getMetadataProperties() returns the description under the expected key.
     */
    @Test
    public void testGetMetadataPropertiesReturnsDescriptionUnderExpectedKey() throws Exception {
        // Given...
        CredentialsOpaque creds = new CredentialsOpaque(ENCODED_SAMPLE);
        creds.setDescription("My license JAR");
        creds.setLastUpdatedByUser("operator");

        // When...
        Properties metaProps = creds.getMetadataProperties("licensecreds");

        // Then...
        assertThat(metaProps.getProperty("secure.credentials.licensecreds.description"))
                .isEqualTo("My license JAR");
        assertThat(metaProps.getProperty("secure.credentials.licensecreds.lastUpdated.user"))
                .isEqualTo("operator");
    }
}
