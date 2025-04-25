/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework; 

import static org.junit.Assert.assertArrayEquals;

import java.util.List;

import org.junit.Test;

public class TestResultNames {

    @Test
    public void testGetDefaultResultNames(){
        List<String> resultNames = ResultNames.getDefaultResultNames();
        assertArrayEquals(resultNames.toArray(), new String[]{"Ignored","Passed","Failed","EnvFail","Cancelled","Requeued","Hung"});
    } 
}