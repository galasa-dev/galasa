/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.force.codecoverage;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyRegistration;
import dev.galasa.cps.etcd.internal.Etcd3ConfigurationPropertyStore;
import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;

//@RunWith(MockitoJUnitRunner.class)
public class Ectd3CpsTest {

    public URI createURI() {
        URI testUri;
        try {
            testUri = new URI("http://something");
        } catch (URISyntaxException e) {
            testUri = null;
        }
        return testUri;
    }

    @Mock
    KV                              mockKvClient;

    @InjectMocks
    Etcd3ConfigurationPropertyStore mockCps = new Etcd3ConfigurationPropertyStore(createURI());

//	@Test
    public void testEtcd3ConfigurationPropertyStore() throws ConfigurationPropertyStoreException, URISyntaxException {
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        GetResponse response1 = Mockito.mock(GetResponse.class);
        KeyValue kv = Mockito.mock(KeyValue.class);

        when(kv.getValue()).thenReturn(ByteSequence.from("bar", UTF_8));
        when(response1.getKvs()).thenReturn(asList(kv));

        CompletableFuture<GetResponse> response = CompletableFuture.completedFuture(response1);

        when(mockKvClient.get(bsKey)).thenReturn(response);

        String out = mockCps.getProperty("foo");
        Assert.assertEquals("Unexpected Response", "bar", out);
    }

//	@Test
    public void testEtcd3ConfigurationPropertyStorewithnull()
            throws ConfigurationPropertyStoreException, URISyntaxException {
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        GetResponse response1 = Mockito.mock(GetResponse.class);

        when(response1.getKvs()).thenReturn(new ArrayList<>());

        CompletableFuture<GetResponse> response = CompletableFuture.completedFuture(response1);

        when(mockKvClient.get(bsKey)).thenReturn(response);

        String out = mockCps.getProperty("foo");
        Assert.assertEquals("Was not null as expected", null, out);
    }

    // @Ignore // Issue #1250 raised to re-add this test
    // @Test 
    public void testRegistration() throws ConfigurationPropertyStoreException, URISyntaxException {
        FrameworkInitialisation fi = Mockito.mock(FrameworkInitialisation.class);
        Etcd3ConfigurationPropertyRegistration regi = new Etcd3ConfigurationPropertyRegistration();

        when(fi.getBootstrapConfigurationPropertyStore()).thenReturn(new URI("etcd:http://thisIsALie"));
        regi.initialise(fi);

        assertTrue("dummy", true);
    }

    // @Ignore // Issue #1250 raised to re-add this test
    // @Test 
    public void testRegistrationwithFile() throws ConfigurationPropertyStoreException, URISyntaxException {
        FrameworkInitialisation fi = Mockito.mock(FrameworkInitialisation.class);
        Etcd3ConfigurationPropertyRegistration regi = new Etcd3ConfigurationPropertyRegistration();

        when(fi.getBootstrapConfigurationPropertyStore()).thenReturn(new URI("file:///blah"));
        regi.initialise(fi);

        assertTrue("dummy", true);
    }
}
