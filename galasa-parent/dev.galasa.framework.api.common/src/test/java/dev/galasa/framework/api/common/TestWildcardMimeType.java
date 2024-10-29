/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.WildcardMimeType.*;

public class TestWildcardMimeType {

    @Test
    public void testWildcardMimeTypeToStringReturnsTypeAsExpected() {
        assertThat(APPLICATION_WILDCARD.toString()).isEqualTo("application/*");
        assertThat(TEXT_WILDCARD.toString()).isEqualTo("text/*");
        assertThat(WILDCARD.toString()).isEqualTo("*/*");
    }

    @Test
    public void testGetWildcardTypeFromStringReturnsApplicationTypeAsExpected() {
        assertThat(WildcardMimeType.getFromString("application/*")).isEqualTo(APPLICATION_WILDCARD);
        assertThat(WildcardMimeType.getFromString("APPLICATION/*")).isEqualTo(APPLICATION_WILDCARD);
        assertThat(WildcardMimeType.getFromString("  aPPlication/*")).isEqualTo(APPLICATION_WILDCARD);
    }

    @Test
    public void testGetWildcardTypeFromStringReturnsTextTypeAsExpected() {
        assertThat(WildcardMimeType.getFromString("text/*")).isEqualTo(TEXT_WILDCARD);
        assertThat(WildcardMimeType.getFromString("TEXT/*")).isEqualTo(TEXT_WILDCARD);
        assertThat(WildcardMimeType.getFromString("tExT/*  ")).isEqualTo(TEXT_WILDCARD);
    }

    @Test
    public void testGetWildcardTypeFromStringReturnsWildcardTypeAsExpected() {
        assertThat(WildcardMimeType.getFromString("*/*")).isEqualTo(WILDCARD);
        assertThat(WildcardMimeType.getFromString("*/*  ")).isEqualTo(WILDCARD);
        assertThat(WildcardMimeType.getFromString("  */*  ")).isEqualTo(WILDCARD);
    }
}
