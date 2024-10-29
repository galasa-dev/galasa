/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.test;
import java.util.*;

import dev.galasa.plugin.common.WrappedLog;

import static org.assertj.core.api.Assertions.*;

public class MockLog implements WrappedLog {

    private  List<String> logRecords = new ArrayList<String>();

    public boolean isDebugEnabled = true ;
    public boolean isInfoEnabled = true ;
    public boolean isWarnEnabled = true ;
    public boolean isErrorEnabled = true ;

    public void assertContainsRecord(String expectedRecord) {
        boolean matched = false;
        for(String record: logRecords) {
            if (record.equals(expectedRecord)) {
                matched = true ;
                break;
            }
        }
        assertThat(matched).as("Log does not contain expected record '"+expectedRecord+"'").isTrue();
    }

    @Override
    public void info(String message) {
        logRecords.add("INFO:"+message);
    }

    @Override
    public void error(String message) {
        logRecords.add("ERROR:"+message);
    }
    
    @Override
    public void warn(String message) {
        logRecords.add("WARNING:"+message);
    }
}
