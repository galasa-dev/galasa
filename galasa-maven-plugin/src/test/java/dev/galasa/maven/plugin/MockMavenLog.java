/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import org.apache.maven.plugin.logging.Log;

public class MockMavenLog implements Log {

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
    public boolean isDebugEnabled() {
        return this.isDebugEnabled() ;
    }

    @Override
    public void debug(CharSequence content) {
        logRecords.add("DEBUG:"+content);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        logRecords.add("DEBUG:"+content+" cause: "+error.getMessage());
    }

    @Override
    public void debug(Throwable error) {
        logRecords.add("DEBUG:"+error.getMessage());
    }

    @Override
    public boolean isInfoEnabled() {
        return this.isInfoEnabled;
    }

    @Override
    public void info(CharSequence content) {
        logRecords.add("INFO:"+content);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        logRecords.add("INFO:"+content+" cause: "+error.getMessage());
    }

    @Override
    public void info(Throwable error) {
        logRecords.add("INFO:"+error.getMessage());
    }

    @Override
    public boolean isWarnEnabled() {
       return this.isWarnEnabled ;
    }

    @Override
    public void warn(CharSequence content) {
        logRecords.add("WARN:"+content);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        logRecords.add("WARN:"+content+" cause: "+error.getMessage());
    }

    @Override
    public void warn(Throwable error) {
        logRecords.add("WARN:"+error.getMessage());
    }

    @Override
    public boolean isErrorEnabled() {
        return this.isErrorEnabled;
    }

    @Override
    public void error(CharSequence content) {
        logRecords.add("ERROR:"+content);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        logRecords.add("ERROR:"+content+" cause: "+error.getMessage());
    }

    @Override
    public void error(Throwable error) {
        logRecords.add("ERROR:"+error.getMessage());
    }
    
}
