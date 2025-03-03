/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Test;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreWatcher.Event;


public class TestDssEventWatcher {
    

    @Test
    public void testNullKeyGetsIgnored() {
        BlockingQueue<DssEvent> eventQueue = null;
        IDynamicStatusStoreService dss = null;
        DssEventWatcher watcher = new DssEventWatcher(eventQueue,dss);
        watcher.propertyModified(null , null , null , null);  
    }

    @Test
    public void testNullEventGetsIgnored() {
        BlockingQueue<DssEvent> eventQueue = null;
        IDynamicStatusStoreService dss = null;
        DssEventWatcher watcher = new DssEventWatcher(eventQueue,dss);
        watcher.propertyModified( "something" , null , null , null);  
    }

    @Test
    public void testIgnoresDssModifyIfThereIsNoRunId() {
        BlockingQueue<DssEvent> eventQueue = null;
        IDynamicStatusStoreService dss = null;
        DssEventWatcher watcher = new DssEventWatcher(eventQueue,dss);
        watcher.propertyModified( "somethingWhichFailsTheRegexWithNoRunId" , Event.MODIFIED , "old" , "new");  
    }

    @Test
    public void testDssModifyWithARunIdEnqueuesAnEvent() throws Exception {
        BlockingQueue<DssEvent> eventQueue = new LinkedBlockingQueue<DssEvent>();
        IDynamicStatusStoreService dss = null;
        DssEventWatcher watcher = new DssEventWatcher(eventQueue,dss);
        watcher.propertyModified( "run.U2345.status" , Event.MODIFIED , "old" , "new");

        assertThat(eventQueue).hasSize(1);

        DssEvent eventGotBack = eventQueue.take();
        assertThat(eventGotBack.getRunName()).isEqualTo("U2345");
        assertThat(eventGotBack.getOldValue()).isEqualTo("old");
        assertThat(eventGotBack.getNewValue()).isEqualTo("new");
        assertThat(eventGotBack.getEventType()).isEqualTo(Event.MODIFIED);
    }

    @Test
    public void testDssModifyWithARunIdButNoStatusDoesNothing() throws Exception {
        BlockingQueue<DssEvent> eventQueue = new LinkedBlockingQueue<DssEvent>();
        IDynamicStatusStoreService dss = null;
        DssEventWatcher watcher = new DssEventWatcher(eventQueue,dss);
        watcher.propertyModified( "run.U2345xstatus" , Event.MODIFIED , "old" , "new");

        assertThat(eventQueue).hasSize(0);
    }
}
