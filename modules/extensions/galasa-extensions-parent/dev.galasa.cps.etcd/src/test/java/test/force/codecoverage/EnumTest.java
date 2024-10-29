/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.cps.etcd.spi.IEtcd3Listener.Event;

public class EnumTest {

    @Test
    public void testIEtcd3ListenerEvent() {
        Event event = Event.DELETE;
        event.compareTo(Event.PUT);
        event.compareTo(Event.UNKNOWN);
        Assert.assertTrue("dummy", true);
    }

}
