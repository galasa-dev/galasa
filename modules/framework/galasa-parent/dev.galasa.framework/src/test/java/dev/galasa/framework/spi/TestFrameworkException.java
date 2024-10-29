/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestFrameworkException {

    @Test
    public void testRaisingWithNoCodeResultsInUnknownCodeStored() {
        FrameworkException ex = new FrameworkException();
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorDetailsBase.UNKNOWN);
    }

    @Test
    public void testRaisingWithErrorCodeCausesCodeToBeStored() {
        FrameworkException ex = new FrameworkException(new FrameworkErrorDetailsBase(2,"this is a message"));
        assertThat(ex.getErrorCode()).isEqualTo(2);
    }

    @Test
    public void testRaisingWithRandomExceptionCausesUnknownCodeToBeStored() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException(cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorDetailsBase.UNKNOWN);
    }

    @Test
    public void testRaisingWithFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(new FrameworkErrorDetailsBase(2,"this is a message"));
        FrameworkException ex = new FrameworkException(cause);
        assertThat(ex.getErrorCode()).isEqualTo(2);
    }

    @Test
    public void testRaisingWithMessageAndRandomExceptionCausesUnknownToBeTransferred() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorDetailsBase.UNKNOWN);
    }

    @Test
    public void testRaisingWithMessageAndFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(new FrameworkErrorDetailsBase(5,"random exception for unit tests"));
        FrameworkException ex = new FrameworkException("exception for unit testing",cause);
        assertThat(ex.getErrorCode()).isEqualTo(5);
    }

    @Test
    public void testRaisingWithLoadsOfParametersAndRandomExceptionCausesUnknownToBeStored() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause,true,true);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorDetailsBase.UNKNOWN);
    }

    @Test
    public void testRaisingWithLoadsOfParametersAndFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(new FrameworkErrorDetailsBase(2,"random exception for unit tests"));
        FrameworkException ex = new FrameworkException("exception for unit testing",cause,true,true);
        assertThat(ex.getErrorCode()).isEqualTo(2);
    }
}
