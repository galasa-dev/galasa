/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestFrameworkErrorDetailsBase {

    @Test
    public void testDefaultsToUnknown() {
        FrameworkErrorDetailsBase details = new FrameworkErrorDetailsBase("something");
        assertThat(details.getErrorCode()).isEqualTo(FrameworkErrorDetailsBase.UNKNOWN);
    }

    @Test
    public void testWithErrorCodeAndMessage(){
        FrameworkErrorDetailsBase details = new FrameworkErrorDetailsBase( 100, "the message");
        assertThat(details.getErrorCode()).isEqualTo(100);
        assertThat(details.getMessage()).isEqualTo("the message");
    }

    @Test
    public void testJsonRendersCodeAndMessage() {
        FrameworkErrorDetailsBase details = new FrameworkErrorDetailsBase( 100, "the message");
        String json = details.toJson();

        assertThat(json).isEqualTo("{\"error_code\":100,\"error_message\":\"the message\"}");
    }

}
