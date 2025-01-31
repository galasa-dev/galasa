/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class TestErrors {
    @Test
    public void testCanCreateErrorMessageWithTwoParameterOK() {
        String msg = Errors.ERROR_URI_IS_INVALID.getMessage("param1","param2");
        assertThat(msg).contains("GAL7001E:").contains("param1").contains("param2");
    }

    @Test
    public void testErrorNeedsTwoParamsIGiveItOneFails() {
        String msg = Errors.ERROR_URI_IS_INVALID.getMessage("param1");
        assertThat(msg).contains("GAL6999E:").contains("1").contains("2");
    }
}
