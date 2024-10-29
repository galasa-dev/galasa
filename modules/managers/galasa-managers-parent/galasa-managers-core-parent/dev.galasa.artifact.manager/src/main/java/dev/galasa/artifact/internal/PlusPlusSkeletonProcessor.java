/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import dev.galasa.artifact.ISkeletonProcessor;
import dev.galasa.artifact.SkeletonProcessorException;
import dev.galasa.framework.spi.IFramework;

/**
 * A simple skeleton processor to replace velocity
 * 
 *  
 * 
 */
public class PlusPlusSkeletonProcessor implements ISkeletonProcessor {

    private CharsetEncoder encoder = Charset.defaultCharset().newEncoder();

    // Define all sets of delimiters handled
    private final Delimiter[] delimiters = { new Delimiter("++", true) // RTS style
//			new Delimiter("&", "", false) // SEM style
    };

    // Define all loop formats handled
    private final LoopForm[]    loopForms    = {
            new LoopForm("<for " + LoopForm.MEMBER + " in ++" + LoopForm.COLLECTION + "++>", "</for>") };

    private final LoopLimiter[] loopLimiters = { new LoopLimiter("<x" + LoopLimiter.LIMIT + ">", "</x>") };

    private final Anchor[]      anchors      = { new Anchor("<a" + Anchor.COLUMN + ">") };

    public PlusPlusSkeletonProcessor(IFramework framework) {
    }

    @Override
    public InputStream processSkeleton(InputStream skeleton, Map<String, Object> parameters)
            throws SkeletonProcessorException {

        byte[] skeletonBytes;
        try {
            skeletonBytes = IOUtils.toByteArray(skeleton);
        } catch (IOException e) {
            throw new SkeletonProcessorException(e);
        }

        String skeletonString = new String(skeletonBytes);

        // If the input is not text then do not bother to process as we
        // risk corrupting binary data if we do so
        if (!encoder.canEncode(skeletonString) || parameters == null) {
            return new ByteArrayInputStream(skeletonBytes);
        }

        for (int i = 0; i < 2; i++) {

            for (Entry<String, Object> substitution : parameters.entrySet()) {
                if (substitution.getValue() == null) {
                    continue;
                }
                if (substitution.getValue() instanceof String) {
                    skeletonString = directSubstitute(skeletonString, substitution.getKey(),
                            (String) substitution.getValue());
                } else if (substitution.getValue() instanceof Integer) {
                    skeletonString = directSubstitute(skeletonString, substitution.getKey(),
                            ((Integer) substitution.getValue()).toString());
                } else if (substitution.getValue() instanceof Collection<?>) {
                    skeletonString = loopSubstitute(skeletonString, substitution.getKey(),
                            (Collection<?>) substitution.getValue());
                } else if (substitution.getValue() instanceof String[]) {
                    skeletonString = loopSubstitute(skeletonString, substitution.getKey(),
                            Arrays.asList((String[]) substitution.getValue()));
                } else {
                    throw new SkeletonProcessorException("The passed value for '" + substitution.getKey()
                            + "' is not a String, String array, or collection");
                }
            }
        }

        skeletonString = reAlign(skeletonString);

        skeletonString = purgeUnusedMarkers(skeletonString);

        return new ByteArrayInputStream(skeletonString.getBytes());
    }

    /**
     * Perform a direct substitution of all instances of key, delimited by any
     * defined delimiters, with value.
     * 
     * @param skeletonContent
     * @param key
     * @param value
     * @return
     */
    private String directSubstitute(String skeletonContent, String key, String value) {

        for (Delimiter delimiter : delimiters) {
            skeletonContent = delimiter.substitute(skeletonContent, key, value);
        }

        return skeletonContent;
    }

    /**
     * For any phrase within a loop, perform the required substitution for each
     * member of the collection
     * 
     * @param skeletonContent
     * @param key
     * @param collection
     * @return
     * @throws SkeletonProcessorException
     */
    private String loopSubstitute(String skeletonContent, String key, Collection<?> collection)
            throws SkeletonProcessorException {

        for (LoopForm loopForm : loopForms) {
            skeletonContent = loopForm.substitute(skeletonContent, key, collection);
        }

        return skeletonContent;
    }

    /**
     * If any anchors have been used to preserve column position of particular
     * expressions, find and process them.
     * 
     * @param skeletonContent
     * @return
     * @throws SkeletonProcessorException
     */
    private String reAlign(String skeletonContent) throws SkeletonProcessorException {

        for (Anchor anchor : anchors) {
            skeletonContent = anchor.reAlign(skeletonContent);
        }

        return skeletonContent;
    }

    /**
     * Remove any lines which contain any unsubstituted loops or variables
     * 
     * @param skeletonContent
     * @return
     */
    public String purgeUnusedMarkers(String skeletonContent) {

        for (LoopForm loopForm : loopForms) {
            skeletonContent = skeletonContent.replaceAll(loopForm.getGeneralRegexPattern(), "");
            Pattern p = Pattern.compile(loopForm.getGeneralRegexPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(skeletonContent);

            while (m.find()) {
                skeletonContent = skeletonContent.replace(m.group(), "");
            }
        }

        StringBuilder sb = new StringBuilder();

        for (String line : skeletonContent.split("\n")) {
            boolean purge = false;
            for (Delimiter delimiter : delimiters) {

                if (!delimiter.safeToPurge()) {
                    continue;
                }

                if (line.matches(".*" + delimiter.getGeneralRegexPattern() + ".*")) {
                    purge = true;
                    break;
                }
            }
            if (!purge) {
                sb.append(line + "\n");
            }
        }

        return sb.toString();
    }

    /**
     * Describes a known form of delimiting a value to be substituted
     * 
     *  
     * 
     */
    private class Delimiter {

        private final String  start;
        private final String  end;
        private final boolean purgeSafe;

        /**
         * Construct a Delimiter which has the same start and end delimiters e.g.
         * ++var++
         * 
         * @param delimiter
         */
        public Delimiter(String delimiter, boolean purgeSafe) {
            this(delimiter, delimiter, purgeSafe);
        }

        /**
         * Construct a Delimiter with different start and end delimiters e.g. ${var}
         * 
         * @param start
         * @param end
         */
        public Delimiter(String start, String end, boolean purgeSafe) {
            this.start = start;
            this.end = end;
            this.purgeSafe = purgeSafe;
        }

        public boolean safeToPurge() {
            return purgeSafe;
        }

        public String getGeneralRegexPattern() {
            if (end.length() > 0) {
                return "\\Q" + start + "\\E[\\w\\d\\.-_]+?\\Q" + end + "\\E";
            } else {
                return "\\Q" + start + "\\E[\\w\\d\\.-_]+?\\W";
            }
        }

        /**
         * Return a regex-safe delimited String for use in a
         * {@link String#replaceAll(String, String)};
         * 
         * @param string
         * @return
         */
        public String getRegexPattern(String string) {
            if (end.length() > 0) {
                return "\\Q" + start + string + end + "\\E";
            } else {
                return "\\Q" + start + string + "\\E\\W";
            }
        }

        public String substitute(String input, String key, String value) {

            Pattern p = Pattern.compile(getRegexPattern(key), Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(input);

            String output = input;

            while (m.find()) {
                output = output.substring(0, m.start()) + value
                        + output.substring((end.length() > 0) ? m.end() : m.end() - 1);
                m = p.matcher(output);
            }
            return output;
        }
    }

    /**
     * Describes a known way of describing a substitution for each member of some
     * collection
     * 
     *  
     * 
     */
    private class LoopForm {

        protected static final String MEMBER     = "MEMBER";
        protected static final String COLLECTION = "COLLECTION";

        private final String          loopOpen;
        private final String          loopClose;

        public LoopForm(String loopOpen, String loopClose) {
            this.loopOpen = loopOpen;
            this.loopClose = loopClose;
        }

        public String getGeneralRegexPattern() {

            StringBuilder sb = new StringBuilder();

            sb.append("\\Q");
            sb.append(loopOpen.replaceFirst(MEMBER, "\\\\E\\\\w+?\\\\Q").replaceAll(COLLECTION, "\\\\E\\\\w+?\\\\Q"));
            sb.append("\\E\\r?\\n?.+?\\Q");
            sb.append(loopClose.replaceAll(MEMBER, "\\\\E\\\\w+?\\\\Q").replaceAll(COLLECTION, "\\\\E\\\\w+?\\\\Q"));
            sb.append("\\E\\r?\\n?");

            return sb.toString();
        }

        public String getRegexPattern(String collection) {

            StringBuilder sb = new StringBuilder();

            sb.append("\\Q");
            sb.append(loopOpen.replaceFirst(MEMBER, "\\\\E(\\\\w+?)\\\\Q").replaceAll(COLLECTION, collection));
            sb.append("\\E\\r?\\n?(.+?)\\Q");
            sb.append(loopClose.replaceAll(MEMBER, "\\\\E\\\\w+?\\\\Q").replaceAll(COLLECTION, collection));
            sb.append("\\E\\r?\\n?");

            return sb.toString();
        }

        public String substitute(String output, String key, Collection<?> values) throws SkeletonProcessorException {

            Pattern p = Pattern.compile(getRegexPattern(key), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(output);

            while (m.find()) {

                if (m.groupCount() != 2) {
                    throw new SkeletonProcessorException(
                            "The pattern matcher for this loop has returned an " + "unexpected number of groups.");
                }

                String member = m.group(1);
                String phrase = m.group(2);
                String limitee = null;
                int limit = 0;

                for (LoopLimiter limiter : loopLimiters) {
                    Pattern repeatLimiter = Pattern.compile(limiter.getRegexPattern());
                    Matcher limitMatcher = repeatLimiter.matcher(phrase);

                    if (limitMatcher.find()) {
                        if (limitMatcher.groupCount() != 2) {
                            throw new SkeletonProcessorException("The pattern matcher for this loop has returned an "
                                    + "unexpected number of groups.");
                        }

                        limit = Integer.parseInt(limitMatcher.group(1));
                        limitee = limitMatcher.group(2);

                        if (limit < 0) {
                            limit = values.size() + limit;
                        }

                        phrase = phrase.substring(0, limitMatcher.start()) + limitee
                                + phrase.substring(limitMatcher.end());

                        break;
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (Object sub : values) {
                    if (!(sub instanceof String)) {
                        throw new SkeletonProcessorException("The collection given to substitute for '" + key
                                + "' contains members which are not Strings");
                    }

                    if (limitee != null) {
                        if (limit == 0) {
                            phrase = phrase.replace(limitee, "");
                        }
                        limit--;
                    }

                    sb.append(directSubstitute(phrase, member, (String) sub));
                }
                output = output.substring(0, m.start()) + sb.toString() + output.substring(m.end());

                m = p.matcher(output);
            }

            return output;
        }
    }

    private class Anchor {

        protected static final String COLUMN = "COLUMN";

        private final String          anchorExpr;

        public Anchor(String anchorExpr) {
            this.anchorExpr = anchorExpr;
        }

        public String getRegexPattern() {

            StringBuilder sb = new StringBuilder();

            sb.append("^(.+?)\\Q");
            sb.append(anchorExpr.replaceAll(COLUMN, "\\\\E(\\\\d+)\\\\Q"));
            sb.append("\\E(.+?)$");

            return sb.toString();
        }

        public String reAlign(String output) throws SkeletonProcessorException {

            Pattern p = Pattern.compile(getRegexPattern(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(output);

            while (m.find()) {

                if (m.groupCount() != 3) {
                    throw new SkeletonProcessorException(
                            "The pattern matcher for this anchor has returned an " + "unexpected number of groups.");
                }

                String staticPhrase = m.group(1).replaceAll("\\s*$", "");
                int columnNumber = Integer.parseInt(m.group(2));
                String alignedPhrase = m.group(3);

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-" + (columnNumber - 1) + "s", staticPhrase));
                sb.append(alignedPhrase);

                output = output.substring(0, m.start()) + sb.toString() + output.substring(m.end());

                m = p.matcher(output);
            }

            return output;
        }
    }

    private class LoopLimiter {

        protected static final String LIMIT = "LIMIT";

        private final String          openLimit;
        private final String          closeLimit;

        private LoopLimiter(String openLimit, String closeLimit) {
            this.openLimit = openLimit;
            this.closeLimit = closeLimit;
        }

        private String getRegexPattern() {

            StringBuilder sb = new StringBuilder();

            sb.append("\\Q");
            sb.append(openLimit.replaceAll(LIMIT, "\\\\E(-?\\\\d+)\\\\Q"));
            sb.append("\\E(.+?)\\Q");
            sb.append(closeLimit);
            sb.append("\\E");

            return sb.toString();
        }
    }
}
