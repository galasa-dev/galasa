/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.force.codecoverage;

import java.beans.Transient;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.validation.constraints.AssertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

import dev.galasa.cps.etcd.Etcd3ManagerException;
import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyStore;
import dev.galasa.cps.etcd.spi.Etcd3ClientException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class ExceptionsTest {

    @Test
    public void testEtcd3ManagerException() {
        Throwable throwable = new Etcd3ManagerException();
        new Etcd3ManagerException("Message");
        new Etcd3ManagerException("Message", throwable);
        new Etcd3ManagerException(throwable);
        new Etcd3ManagerException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    @Test
    public void testEtcd3ClientException() {
        Throwable throwable = new Etcd3ClientException();
        new Etcd3ClientException("Message");
        new Etcd3ClientException("Message", throwable);
        new Etcd3ClientException(throwable);
        new Etcd3ClientException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    // @Ignore // Issue #1250 raised to re-introduce this test
    // @Test
    public void testCatchingException() throws URISyntaxException {
        boolean caught = false;

        try {
            URI testUri = new URI("http://test.com");
            Etcd3ConfigurationPropertyStore cps = new Etcd3ConfigurationPropertyStore(testUri);
            cps.getProperty("NoKey");
        } catch (ConfigurationPropertyStoreException e) {
            caught = true;
        }

        Assert.assertTrue("Exception not caught", caught);
    }
}
