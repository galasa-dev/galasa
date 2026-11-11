/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestIZosBatch {

    /**
     * Manual mock of IZosBatch that records the arguments of the two argument submitJob. 
     * It does not override submitJob(String), so that the default method is the one under test.
     */
    private static class RecordingZosBatch implements IZosBatch {

        private int submitJobCalls = 0;
        private String submittedJcl;
        private IZosBatchJobname submittedJobname = new ZosBatchJobnameStub();
        private IZosBatchJob jobToReturn;

        @Override
        public IZosBatchJob submitJob(String jcl, IZosBatchJobname jobname) throws ZosBatchException {
            submitJobCalls++;
            submittedJcl = jcl;
            submittedJobname = jobname;
            return jobToReturn;
        }

        @Override
        public IZosBatchJob submitJob(String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobCard) throws ZosBatchException {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @Override
        public IZosBatchJob submitJobWithSuppliedJobcard(String jcl) throws ZosBatchException {
            throw new UnsupportedOperationException("not expected to be called");
        }

        @Override
        public List<IZosBatchJob> getJobs(String jobname, String owner) throws ZosBatchException {
            throw new UnsupportedOperationException("not expected to be called");
        }
    }

    /**
     * Placeholder so that the recorded jobname is not null before submitJob is called, 
     * which lets the test tell the difference between "not called" and "called with null".
     */
    private static class ZosBatchJobnameStub implements IZosBatchJobname {
        @Override
        public String getName() {
            return "STUB";
        }
    }

    @Test
    public void testSubmitJobWithJclOnlySubmitsWithNullJobname() throws Exception {
        RecordingZosBatch zosBatch = new RecordingZosBatch();
        String jcl = "//STEP1    EXEC PGM=IEFBR14";

        zosBatch.submitJob(jcl);

        Assert.assertEquals("submitJob(jcl, jobname) should be called once", 1, zosBatch.submitJobCalls);
        Assert.assertEquals("the JCL should be passed on unchanged", jcl, zosBatch.submittedJcl);
        Assert.assertNull("a null jobname should be passed so that the Manager provisions one", zosBatch.submittedJobname);
    }

    @Test
    public void testSubmitJobWithJclOnlyReturnsTheSubmittedJob() throws Exception {
        RecordingZosBatch zosBatch = new RecordingZosBatch();
        zosBatch.jobToReturn = createJobWhoseMethodsMustNotBeCalled();

        Assert.assertSame("the job returned by submitJob(jcl, jobname) should be returned", zosBatch.jobToReturn, zosBatch.submitJob("//STEP1    EXEC PGM=IEFBR14"));
    }

    @Test
    public void testSubmitJobWithJclOnlyPropagatesException() {
        IZosBatch zosBatch = new RecordingZosBatch() {
            @Override
            public IZosBatchJob submitJob(String jcl, IZosBatchJobname jobname) throws ZosBatchException {
                throw new ZosBatchException("submit failed");
            }
        };

        ZosBatchException e = Assert.assertThrows(ZosBatchException.class, () -> zosBatch.submitJob("//STEP1    EXEC PGM=IEFBR14"));
        Assert.assertEquals("submit failed", e.getMessage());
    }

    /**
     * A job is needed only as a value to check was returned, so none of its methods are expected to be called.
     * A dynamic proxy avoids implementing every method of IZosBatchJob.
     */
    private static IZosBatchJob createJobWhoseMethodsMustNotBeCalled() {
        return (IZosBatchJob) Proxy.newProxyInstance(
            IZosBatchJob.class.getClassLoader(),
            new Class<?>[] { IZosBatchJob.class },
            (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    // Allow assertion failure messages to describe the proxy
                    return method.getName().equals("toString") ? "IZosBatchJob proxy" : null;
                }
                throw new UnsupportedOperationException("not expected to be called: " + method.getName());
            });
    }
}
