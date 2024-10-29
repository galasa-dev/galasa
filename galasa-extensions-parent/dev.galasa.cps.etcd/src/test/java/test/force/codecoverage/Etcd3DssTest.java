/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.force.codecoverage;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import dev.galasa.cps.etcd.internal.Etcd3DynamicStatusStore;
import dev.galasa.cps.etcd.internal.Etcd3DynamicStatusStoreRegistration;
import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;

/**
 * This test class is a for testing the implementation of the JETD client is
 * correct and behaving as expected.
 * 
 * All Responses from a etcd cluster have been mocked using mockito.
 * 
 * @author James Davies
 */
//@RunWith(MockitoJUnitRunner.class)
public class Etcd3DssTest {

    /**
     * Creates a fake URI to test with
     * 
     * @return URI - for the etcd
     */
    private URI createDssUri() {
        URI testUri;
        try {
            testUri = new URI("http://something");
        } catch (URISyntaxException e) {
            testUri = null;
        }
        return testUri;
    }

    /**
     * This mocks the client for the etcd server and injects it into the Etcd3Dss
     * class
     */
    @Mock
    KV                      mockKvCLient;

    /**
     * Creates a dss for the etcd and injects the mock above.
     */
    @InjectMocks
    Etcd3DynamicStatusStore mockDss = new Etcd3DynamicStatusStore(createDssUri());

    /**
     * This test method tests the put method for a simple example.
     * 
     * @throws DynamicStatusStoreException
     */
//    @Test
    public void testPutSingleValue() throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);
        ByteSequence bsValue = ByteSequence.from("bar", UTF_8);

        PutResponse response = Mockito.mock(PutResponse.class);
        CompletableFuture<PutResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mockKvCLient.put(bsKey, bsValue)).thenReturn(mockFuture);

        mockDss.put("foo", "bar");

        assertTrue("dummy", true);
    }

    /**
     * This test methods checks the put for a map of values. It uses the etcd
     * transactions rather than a deicated put.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     */
//    @Test
    public void testPutMapOfValues() throws DynamicStatusStoreException, InterruptedException {
        Map<String, String> kvs = new HashMap<>();

        kvs.put("foo", "bar");
        kvs.put("foo-er", "bar-er");

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mocktxn.Then(any())).thenReturn(mockRequest);
        when(mockKvCLient.txn()).thenReturn(mocktxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mockRequest.commit()).thenReturn(mockFuture);

        mockDss.put(kvs);

        assertTrue("dummy", true);
    }

    /**
     * This test methods does a atomic/swap put. This looks for a IF arguament
     * before completing a put.
     * 
     * In this case it is a simple example of just one value for another.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapBasic() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        String key = "foo";
        String oldValue = "bar";
        String newValue = "bar-er";

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        Txn mockCheck = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mockKvCLient.txn()).thenReturn(mocktxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mocktxn.If(any())).thenReturn(mockCheck);
        when(mockCheck.Then(any())).thenReturn(mockRequest);
        when(mockRequest.commit()).thenReturn(mockFuture);
        when(mockFuture.get().isSucceeded()).thenReturn(true);

        assertTrue("dummy", mockDss.putSwap(key, oldValue, newValue));
    }

    /**
     * This atomic/swap put checks the case where the key doesnt exist prior to the
     * put.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapBasicWithNullOld()
            throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        String key = "foo";
        String oldValue = null;
        String newValue = "bar-er";

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        Txn mockCheck = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mockKvCLient.txn()).thenReturn(mocktxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mocktxn.If(any())).thenReturn(mockCheck);
        when(mockCheck.Then(any())).thenReturn(mockRequest);
        when(mockRequest.commit()).thenReturn(mockFuture);
        when(mockFuture.get().isSucceeded()).thenReturn(true);

        assertTrue("dummy", mockDss.putSwap(key, oldValue, newValue));
    }

    /**
     * This test method also does a atomic/swap put but also includes the map of
     * values to set if the condition is satisfied.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapWithMap() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        String key = "foo";
        String oldValue = "bar";
        String newValue = "bar-er";
        Map<String, String> otherKvs = new HashMap<>();

        otherKvs.put("marco", "pollo");
        otherKvs.put("Tom", "Jerry");

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        Txn mockCheck = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mockKvCLient.txn()).thenReturn(mocktxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mocktxn.If(any())).thenReturn(mockCheck);
        when(mockCheck.Then(any())).thenReturn(mockRequest);
        when(mockRequest.commit()).thenReturn(mockFuture);
        when(mockFuture.get().isSucceeded()).thenReturn(true);

        assertTrue("dummy", mockDss.putSwap(key, oldValue, newValue, otherKvs));
    }

    /**
     * This atomic/swap put checks the case where the key doesnt exist prior to the
     * put. This too includes a map of values to set if successful.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapWithMapWithNullOld()
            throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        String key = "foo";
        String oldValue = null;
        String newValue = "bar-er";
        Map<String, String> otherKvs = new HashMap<>();

        otherKvs.put("marco", "pollo");
        otherKvs.put("Tom", "Jerry");

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        Txn mockCheck = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mockKvCLient.txn()).thenReturn(mocktxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mocktxn.If(any())).thenReturn(mockCheck);
        when(mockCheck.Then(any())).thenReturn(mockRequest);
        when(mockRequest.commit()).thenReturn(mockFuture);
        when(mockFuture.get().isSucceeded()).thenReturn(true);

        assertTrue("dummy", mockDss.putSwap(key, oldValue, newValue, otherKvs));
    }

    /**
     * This test does a simple get from etcd (mocked) of a single k-v pair.
     * 
     * @throws DynamicStatusStoreException
     */
//    @Test
    public void testGetSimple() throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        GetResponse response = Mockito.mock(GetResponse.class);
        KeyValue kv = Mockito.mock(KeyValue.class);

        when(kv.getValue()).thenReturn(ByteSequence.from("bar", UTF_8));
        when(response.getKvs()).thenReturn(asList(kv));

        CompletableFuture<GetResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.get(bsKey)).thenReturn(futureResponse);

        String out = mockDss.get("foo");
        assertEquals("Unexpected Response", "bar", out);
    }

    /**
     * This test does a simple get a new key from etcd (mocked) of a single k-v
     * pair.
     * 
     * @throws DynamicStatusStoreException
     */
//    @Test
    public void testGetSimpleWithNew() throws DynamicStatusStoreException {
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        GetResponse response = Mockito.mock(GetResponse.class);
        when(response.getKvs()).thenReturn(new ArrayList<KeyValue>());

        CompletableFuture<GetResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.get(bsKey)).thenReturn(futureResponse);

        String out = mockDss.get("foo");
        assertEquals("Unexpected Response", null, out);
    }

    /**
     * This test method does a get for serveral keys using a prefix.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testGetprefix() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        ByteSequence bsPrefix = ByteSequence.from("foo", UTF_8);
        ByteSequence bsValue = ByteSequence.from("bar", UTF_8);

        GetResponse response = Mockito.mock(GetResponse.class);
        KeyValue kv = Mockito.mock(KeyValue.class);

        when(kv.getValue()).thenReturn(bsValue);
        when(kv.getKey()).thenReturn(bsPrefix);
        when(response.getKvs()).thenReturn(asList(kv));

        CompletableFuture<GetResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.get(Mockito.eq(bsPrefix), any(GetOption.class))).thenReturn(futureResponse);

        Map<String, String> map = mockDss.getPrefix("foo");
        assertEquals("Map was the wrong size", 1, map.size());

        String out = map.get("foo");

        assertEquals("Incorrect Value", "bar", out);
    }

    /**
     * This test method does a get for serveral keys using a prefix, but no eksy
     * exsist.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testGetprefixWithoKeys() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        ByteSequence bsPrefix = ByteSequence.from("foo", UTF_8);
        ByteSequence bsValue = ByteSequence.from("bar", UTF_8);

        GetResponse response = Mockito.mock(GetResponse.class);

        when(response.getKvs()).thenReturn(new ArrayList<KeyValue>());

        CompletableFuture<GetResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.get(Mockito.eq(bsPrefix), any(GetOption.class))).thenReturn(futureResponse);

        Map<String, String> map = mockDss.getPrefix("foo");
        assertEquals("Map was the wrong size", 0, map.size());

        String out = map.get("foo");

        assertEquals("Incorrect Value", null, out);
    }

    /**
     * This test method does a basic delete of a single key value pair.
     * 
     * @throws DynamicStatusStoreException
     */
//    @Test
    public void testDeleteBasic() throws DynamicStatusStoreException {
        String key = "foo";
        ByteSequence bsKey = ByteSequence.from(key, UTF_8);

        DeleteResponse response = Mockito.mock(DeleteResponse.class);

        CompletableFuture<DeleteResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.delete(bsKey)).thenReturn(futureResponse);

        mockDss.delete("foo");

        assertTrue("dummy", true);
    }

    /**
     * This test method does a delete of a set of kv pairs provided.
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testDeleteSetOfValues() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Set<String> keys = new HashSet<>();
        keys.add("foo");
        keys.add("bar");

        TxnResponse response = Mockito.mock(TxnResponse.class);
        Txn mockTxn = Mockito.mock(Txn.class);
        Txn mockRequest = Mockito.mock(Txn.class);

        when(mockKvCLient.txn()).thenReturn(mockTxn);

        CompletableFuture<TxnResponse> mockFuture = CompletableFuture.completedFuture(response);

        when(mockTxn.Then(any())).thenReturn(mockRequest);
        when(mockRequest.commit()).thenReturn(mockFuture);

        mockDss.delete(keys);

        assertTrue("dummy", true);
    }

    /**
     * This test method does a delete of all keys with a prefix.
     * 
     * @throws DynamicStatusStoreException
     */
//    @Test
    public void testDeletePrefix() throws DynamicStatusStoreException {
        String keyPrefix = "foo";
        ByteSequence bsKeyPrefix = ByteSequence.from(keyPrefix, UTF_8);

        DeleteResponse response = Mockito.mock(DeleteResponse.class);

        CompletableFuture<DeleteResponse> futureResponse = CompletableFuture.completedFuture(response);

        when(mockKvCLient.delete(Mockito.eq(bsKeyPrefix), any(DeleteOption.class))).thenReturn(futureResponse);

        mockDss.deletePrefix("foo");

        assertTrue("dummy", true);
    }

    /**
     * This method checks the registration of a etcd dss from a http URI
     * 
     * @throws DynamicStatusStoreException
     * @throws URISyntaxException
     */
//    @Test
    public void testRegistration() throws DynamicStatusStoreException, URISyntaxException {

        FrameworkInitialisation fi = Mockito.mock(FrameworkInitialisation.class);
        Etcd3DynamicStatusStoreRegistration regi = new Etcd3DynamicStatusStoreRegistration();

        when(fi.getDynamicStatusStoreUri()).thenReturn(new URI("http://thisIsAEtcd3"));
        regi.initialise(fi);

        assertTrue("dummy", true);
    }

    /**
     * This method checks the quiet failure of not registring if the URI is a file.
     * 
     * @throws DynamicStatusStoreException
     * @throws URISyntaxException
     */
//    @Test
    public void testRegistrationwithFile() throws DynamicStatusStoreException, URISyntaxException {

        FrameworkInitialisation fi = Mockito.mock(FrameworkInitialisation.class);
        Etcd3DynamicStatusStoreRegistration regi = new Etcd3DynamicStatusStoreRegistration();

        when(fi.getDynamicStatusStoreUri()).thenReturn(new URI("file:///blah"));

        regi.initialise(fi);

        assertTrue("dummy", true);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     */
//    @Test
    public void testPutExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);
        ByteSequence bsValue = ByteSequence.from("bar", UTF_8);

        CompletableFuture<PutResponse> response = Mockito.mock(CompletableFuture.class);
        when(mockKvCLient.put(bsKey, bsValue)).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.put("foo", "bar");
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutMapExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        Map<String, String> kvs = new HashMap<>();

        kvs.put("foo", "bar");
        kvs.put("foo-er", "bar-er");

        CompletableFuture<TxnResponse> response = Mockito.mock(CompletableFuture.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        when(mockKvCLient.txn()).thenReturn(mocktxn);
        Txn request = Mockito.mock(Txn.class);
        when(mocktxn.Then(any(Op.class))).thenReturn(request);
        when(request.commit()).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.put(kvs);
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        String key = "foo";
        String oldValue = "notBar";
        String newValue = "bar";

        CompletableFuture<TxnResponse> response = Mockito.mock(CompletableFuture.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        when(mockKvCLient.txn()).thenReturn(mocktxn);
        Txn check = Mockito.mock(Txn.class);
        when(mocktxn.If(any())).thenReturn(check);
        Txn request = Mockito.mock(Txn.class);
        when(check.Then(any(Op.class))).thenReturn(request);
        when(request.commit()).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());
            mockDss.putSwap(key, oldValue, newValue);

        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testPutSwapMapExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        String key = "foo";
        String oldValue = "notBar";
        String newValue = "bar";

        Map<String, String> otherKvs = new HashMap<>();

        otherKvs.put("marco", "pollo");
        otherKvs.put("Tom", "Jerry");

        CompletableFuture<TxnResponse> response = Mockito.mock(CompletableFuture.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        when(mockKvCLient.txn()).thenReturn(mocktxn);
        Txn check = Mockito.mock(Txn.class);
        when(mocktxn.If(any())).thenReturn(check);
        Txn request = Mockito.mock(Txn.class);
        when(check.Then(any(Op.class))).thenReturn(request);
        when(request.commit()).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());
            mockDss.putSwap(key, oldValue, newValue, otherKvs);

        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testGetExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        CompletableFuture<GetResponse> response = Mockito.mock(CompletableFuture.class);
        when(mockKvCLient.get(bsKey)).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.get("foo");
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testGetPrefixException() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        CompletableFuture<GetResponse> response = Mockito.mock(CompletableFuture.class);
        when(mockKvCLient.get(Mockito.eq(bsKey), any(GetOption.class))).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.getPrefix("foo");
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testDeleteException() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        ByteSequence bsKey = ByteSequence.from("foo", UTF_8);

        CompletableFuture<DeleteResponse> response = Mockito.mock(CompletableFuture.class);
        when(mockKvCLient.delete(Mockito.eq(bsKey))).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.delete("foo");
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown correctly
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testDeleteSetExcpetion() throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        Set<String> keys = new HashSet<>();
        keys.add("foo");
        keys.add("bar");

        CompletableFuture<TxnResponse> response = Mockito.mock(CompletableFuture.class);
        Txn mocktxn = Mockito.mock(Txn.class);
        when(mockKvCLient.txn()).thenReturn(mocktxn);
        Txn request = Mockito.mock(Txn.class);
        when(mocktxn.Then(any(Op.class))).thenReturn(request);
        when(request.commit()).thenReturn(response);

        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.delete(keys);
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

    /**
     * Tests the exception is thrown for the delete Prefix
     * 
     * @throws DynamicStatusStoreException
     * @throws InterruptedException
     * @throws ExecutionException
     */
//    @Test
    public void testDeletePrefixException()
            throws DynamicStatusStoreException, InterruptedException, ExecutionException {
        Boolean caught = false;
        ByteSequence bsPrefixKey = ByteSequence.from("foo", UTF_8);

        CompletableFuture<DeleteResponse> response = Mockito.mock(CompletableFuture.class);
        when(mockKvCLient.delete(Mockito.eq(bsPrefixKey), any(DeleteOption.class))).thenReturn(response);
        try {
            when(response.get()).thenThrow(new InterruptedException());

            mockDss.deletePrefix("foo");
        } catch (DynamicStatusStoreException e) {
            caught = true;
        }
        assertTrue("Exception was not caught", caught);
    }

}
