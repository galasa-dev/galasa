/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.cts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfidentialTextService;

/**
 * This class is to provide the framework with a confidential text service that
 * can remove usernames, passwords, keys,etc, from logs and console outputs if
 * the work is registered.
 * 
 *  
 */
public class FrameworkConfidentialTextService implements IConfidentialTextService {
    private List<ConfidentialText> confidentialTexts = new ArrayList<>();
    private Log                    logger            = LogFactory.getLog(FrameworkConfidentialTextService.class);

    /**
     * This method is for registering a text with the service. It creates the
     * replacement tag (******1******) which represents any registered password,
     * etc, in a log or output. A comment can also be given explaining what the
     * credential is for.
     * 
     * @param text    - the word or phrase the manager wants obscuring.
     * @param comment - a comment explaining the conidential text.
     */
    public void registerText(String text, String comment) {
        String number = Integer.toString(confidentialTexts.size() + 1);
        StringBuilder builder = new StringBuilder();
        final String star = "*";
        builder.append(number);

        while (builder.toString().length() != text.length()) {
            builder.append(star);
            if (builder.toString().length() != text.length()) {
                builder.reverse();
                builder.append(star);
                builder.reverse();
            }
        }

        ConfidentialText ct = new ConfidentialText(text, builder.toString(), comment);
        confidentialTexts.add(ct);

        logger.info("Confidential text registered as '" + ct.replacementTag + "', with comment " + ct.getComment());
    }

    /**
     * This method removes any registered strings with the service from any passed
     * text.
     * 
     * @param text - the log or text that needs checking for confidential text.
     */
    public String removeConfidentialText(String text) {

        for (ConfidentialText confidentials : confidentialTexts) {
            text = text.replaceAll(Matcher.quoteReplacement(confidentials.getText()), confidentials.getTag());
        }

        return text;

    }

    /**
     * This class houses all the confidential texts and related infomation,
     * including the replacement tag and the comment.
     */
    private class ConfidentialText {
        private String text;
        private String replacementTag;
        private String comment;

        /**
         * Registering a new confidential text
         * 
         * @param text           - the confidential text to hide
         * @param replacementTag - what will replace the confidential text in the log or
         *                       output
         * @param comment        - a quick comment explaining the text
         */
        public ConfidentialText(String text, String replacementTag, String comment) {
            this.text = text;
            this.replacementTag = replacementTag;
            this.comment = comment;
        }

        /**
         * Retrieves the text to be referenced against any logs or ouput.
         * 
         * @return text - registered confidnetial text.
         */
        public String getText() {
            return text;
        }

        /**
         * Retieves the replacement string for a particular registered text.
         * 
         * @return replacementTag - string replacement.
         */
        public String getTag() {
            return replacementTag;
        }

        /**
         * retireves any comment about a registered text.
         * 
         * @return comment - string comment about the registered text.
         */
        public String getComment() {
            return comment;
        }
    }

    @Override
    public void shutdown() {
        // *** Nothing to shutdown
    }
}