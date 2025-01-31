/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import java.net.HttpURLConnection;
import static org.assertj.core.api.Assertions.*;

public class TestInternalServletException {

    private InternalServletException createException() {
        ServletErrorMessage template = ServletErrorMessage.GAL5015_INTERNAL_CPS_ERROR ;
        ServletError error = new ServletError(template);
        int code = HttpURLConnection.HTTP_INTERNAL_ERROR;
        Exception cause = new Exception("Original simulated cause");
        InternalServletException ex = new InternalServletException(error, code, cause);
        return ex;
    }

    @Test
    public void testCanCreateExceptionWithCause() {
        InternalServletException ex = createException();
        assertThat(ex).isNotNull();
    }

}
 