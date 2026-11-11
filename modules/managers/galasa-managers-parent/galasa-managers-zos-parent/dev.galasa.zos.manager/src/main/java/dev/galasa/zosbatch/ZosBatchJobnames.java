/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch;

import dev.galasa.zosbatch.internal.ZosBatchJobnameImpl;

/**
 * Creates {@link IZosBatchJobname} instances for jobnames chosen by the test.
 * <p>
 * By default the Manager provisions a unique jobname, either when {@link IZosBatch#submitJob(String)} is used
 * (or a <code>null</code> jobname is passed to {@link IZosBatch#submitJob(String, IZosBatchJobname)})
 * or by using the {@link ZosBatchJobname} annotation.
 * Use this class when the job must run under a particular jobname, for example to run a library member
 * under its documented name.
 * <p>
 * A valid jobname is 1 to 8 characters. The first character must be a letter or one of <code>#</code>, <code>$</code>
 * or <code>@</code>, and the remaining characters must be letters, digits or <code>#</code>, <code>$</code> or <code>@</code>.
 * A name that is not valid is rejected with a {@link ZosBatchException}. Names are converted to upper case.
 * <p>
 * For example: <code>IZosBatchJob batchJob = zosBatch.submitJob(jcl, ZosBatchJobnames.exact("PAYROLL"));</code>
 */
public final class ZosBatchJobnames {

    private ZosBatchJobnames() {
        // Static factory methods only
    }

    /**
     * Create a jobname with exactly the supplied name, converted to upper case.
     * <p>
     * <b>Parallel runs:</b> Galasa does not make this name unique. If tests that run at the same time on the same
     * z/OS system submit jobs with the same name, a search by name, such as {@link IZosBatch#getJobs(String, String)},
     * returns the jobs of all of those tests. Each job still has its own job ID, which Galasa uses to track it. To avoid collisions,
     * let the Manager provision the jobname, or use {@link #randomWithPrefix(String)} to make a collision less likely.
     * 
     * @param name the jobname, 1 to 8 characters
     * @return the {@link IZosBatchJobname}
     * @throws ZosBatchException if the name is <code>null</code>, empty, longer than 8 characters or contains invalid characters
     */
    public static IZosBatchJobname exact(String name) throws ZosBatchException {
        return ZosBatchJobnameImpl.exact(name);
    }

    /**
     * Create a jobname that starts with the supplied prefix, converted to upper case, followed by random alphanumeric
     * characters that make the jobname 8 characters long. For example, the prefix <code>PAY</code> produces a jobname
     * such as <code>PAYK3Z9X</code>. A new random suffix is generated on every call.
     * <p>
     * This is the same as the jobname provisioned by the Manager, except that the prefix is chosen by the test
     * rather than from the <code>zosbatch.jobname.[imageid].prefix</code> property.
     * <p>
     * The jobname is random, not guaranteed to be unique. The longer the prefix, the fewer the random characters
     * and the greater the chance of a collision with another job. A 7 character prefix leaves only one random character.
     * 
     * @param prefix the start of the jobname, 1 to 7 characters
     * @return the {@link IZosBatchJobname}
     * @throws ZosBatchException if the prefix is <code>null</code>, empty, longer than 7 characters or contains invalid characters
     */
    public static IZosBatchJobname randomWithPrefix(String prefix) throws ZosBatchException {
        return ZosBatchJobnameImpl.randomWithPrefix(prefix);
    }
}
