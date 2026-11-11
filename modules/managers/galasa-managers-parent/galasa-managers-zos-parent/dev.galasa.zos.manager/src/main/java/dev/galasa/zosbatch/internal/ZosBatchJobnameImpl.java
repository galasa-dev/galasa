/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.internal.properties.JobnamePrefix;

/**
 * Implementation of {@link IZosBatchJobname} 
 *
 */
public class ZosBatchJobnameImpl implements IZosBatchJobname {

    public static final int MAX_JOBNAME_LENGTH = 8;

    /*
     * Matches the characters of a valid JES job name or job name prefix, for example "PAYROLL", "#JOB1" or "$A":
     *   ^                the match starts at the beginning of the name
     *   [A-Za-z#$@]      the first character is a letter or one of the national characters # $ @. It cannot be a digit
     *   [A-Za-z0-9#$@]*  any further characters are letters, digits or # $ @. The ASCII ranges are used on purpose,
     *                    so that letters with accents and other non-ASCII characters are not accepted
     *   $                the match ends at the end of the name, so that nothing else follows
     * Used with Matcher.matches(), which must match the whole name. Upper and lower case letters are both
     * accepted as the name is converted to upper case afterwards.
     * The length is not checked by the pattern. It is checked separately so that the error message can be specific.
     */
    private static final Pattern VALID_CHARACTERS = Pattern.compile("^[A-Za-z#$@][A-Za-z0-9#$@]*$");
    
    private String name;

    private String jobNamePrefix;
        
    public ZosBatchJobnameImpl(@NotNull IZosImage image) throws ZosBatchException {

        try {
            jobNamePrefix = JobnamePrefix.get(image.getImageID());
        } catch (ZosBatchManagerException e) {
            throw new ZosBatchException("Problem getting batch jobname prefix", e);
        }
        this.name = jobNamePrefix + RandomStringUtils.insecure().nextAlphanumeric(MAX_JOBNAME_LENGTH - jobNamePrefix.length()).toUpperCase(Locale.ROOT);
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    
    public ZosBatchJobnameImpl(String name) {
        this.name = name.toUpperCase(Locale.ROOT);
    }

    /**
     * Create a jobname with exactly the supplied name, converted to upper case.
     *
     * @param name the jobname, 1 to 8 valid JES characters
     * @return the jobname
     * @throws ZosBatchException if the name is not a valid jobname
     */
    public static ZosBatchJobnameImpl exact(String name) throws ZosBatchException {
        validate("Jobname", name, MAX_JOBNAME_LENGTH);
        return new ZosBatchJobnameImpl(name);
    }

    /**
     * Create a jobname that starts with the supplied prefix, converted to upper case,
     * followed by random alphanumeric characters up to the maximum jobname length.
     * The jobname is random, so it is not guaranteed to be unique.
     *
     * @param prefix the jobname prefix, 1 to 7 valid JES characters
     * @return the jobname
     * @throws ZosBatchException if the prefix is not valid
     */
    public static ZosBatchJobnameImpl randomWithPrefix(String prefix) throws ZosBatchException {
        validate("Jobname prefix", prefix, MAX_JOBNAME_LENGTH - 1);
        String suffix = RandomStringUtils.insecure().nextAlphanumeric(MAX_JOBNAME_LENGTH - prefix.length());
        return new ZosBatchJobnameImpl(prefix + suffix);
    }

    private static void validate(String description, String value, int maxLength) throws ZosBatchException {
        if (value == null || value.isEmpty() || value.length() > maxLength || !VALID_CHARACTERS.matcher(value).matches()) {
            throw new ZosBatchException(description + " \"" + value + "\" is not valid. It must be between 1 and " + maxLength
                    + " characters, start with a letter or one of the characters #, $ or @, and contain only letters, digits, #, $ or @");
        }
    }
}
