/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import static org.assertj.core.api.Assertions.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import dev.galasa.plugin.common.ErrorRaiser;

public class ErrorRaiserMavenImplTest {
    @Test
    public void TestCanLogErrorWithOneParameterOk() throws Exception {

        MockMavenLog mockLog = new MockMavenLog();
        ErrorRaiser<MojoExecutionException> raiser = new ErrorRaiserMavenImpl(mockLog);
        Exception ex = catchException(()-> raiser.raiseError("simple template {0}","param1"));

        assertThat(ex).isInstanceOf(MojoExecutionException.class).hasMessageContaining("simple template param1");
        mockLog.assertContainsRecord("ERROR:simple template param1");
    }

    @Test
    public void TestCanLogErrorWithTwoParametersOk() throws Exception {
        MockMavenLog mockLog = new MockMavenLog();
        ErrorRaiser<MojoExecutionException> raiser = new ErrorRaiserMavenImpl(mockLog);
        Exception ex = catchException(()-> raiser.raiseError("simple template {0} {1}","param1","param2"));

        assertThat(ex).isInstanceOf(MojoExecutionException.class).hasMessageContaining("simple template param1 param2");
        mockLog.assertContainsRecord("ERROR:simple template param1 param2");
    }

    @Test
    public void TestCanLogErrorWithZeroParametersOk() throws Exception {
        MockMavenLog mockLog = new MockMavenLog();
        ErrorRaiser<MojoExecutionException> raiser = new ErrorRaiserMavenImpl(mockLog);
        Exception ex = catchException(()-> raiser.raiseError("simple template"));

        assertThat(ex).isInstanceOf(MojoExecutionException.class).hasMessageContaining("simple template");
        mockLog.assertContainsRecord("ERROR:simple template");
    }

}
