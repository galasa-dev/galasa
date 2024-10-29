/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

/** A record collected from the log interface, by the MockLog class. */
public class MockLogRecord {



    private String content ;
    private MockLogRecordType type ;
    private Throwable t ;

    public MockLogRecord( MockLogRecordType type, Object content , Throwable t) {
        this.type = type ;

        String messageText = "";
        if (content != null) {
            messageText = content.toString();
        }
        this.content = messageText ;
        this.t = t ;
    }

    public String getContent() {
        return this.content;
    }

    public MockLogRecordType getType() {
        return this.type;
    }

    public Throwable getThrowable() {
        return this.t;
    }
}
