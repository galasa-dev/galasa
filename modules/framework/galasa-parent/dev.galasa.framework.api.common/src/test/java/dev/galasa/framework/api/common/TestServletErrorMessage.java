/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import static dev.galasa.framework.api.common.ServletErrorMessage.GALxxx_NEXT_MESSAGE_NUMBER_TO_USE;
import static org.assertj.core.api.Assertions.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class TestServletErrorMessage {

    @Test
    public void TestCanGetAMessageOutOfTheList() throws Exception {
        // Given... we force the class to load.
        ServletErrorMessage msg = ServletErrorMessage.GAL5002_INVALID_RUN_ID;
        assertThat(msg).isNotNull();
    }

    @Test
    public void testAllErrorMessageNumbersAreUnique() throws Exception {

        Map<Integer,ServletErrorMessage> messagesLookedAtSoFar = new HashMap<>();

        StringBuilder buff = new StringBuilder();

        for( ServletErrorMessage msg : EnumSet.allOf(ServletErrorMessage.class)) {

            int msgNumber = msg.getTemplateNumber();
            ServletErrorMessage clashingErrorMessage = messagesLookedAtSoFar.get(msgNumber);
            if( clashingErrorMessage != null) {
                buff.append("Error message number clashes: Template number "+msgNumber+" is used more than once!\n");
                buff.append("  Template1: "+msg.toString()+"\n");
                buff.append("  Template2: "+clashingErrorMessage.toString()+"\n");
            } else {
                messagesLookedAtSoFar.put(msgNumber,msg);
            }
        }

        String errorMsg = buff.toString();
        assertThat(errorMsg).isBlank();
    }

    @Test
    public void testNextMessageNumberToAllocateIsHighestSoFar() {

        int highestMsgNumber = 0 ;
        for( ServletErrorMessage msg : EnumSet.allOf(ServletErrorMessage.class)) {
            int msgNumber = msg.getTemplateNumber();
            if (msgNumber>highestMsgNumber) {
                highestMsgNumber = msgNumber;
            }
        }
        assertThat(highestMsgNumber)
            .as("Highest message number in use is higher than the GALxxx_NEXT_MESSAGE_NUMBER_TO_USE marker value."+
                " Edit the GALxxx_NEXT_MESSAGE_NUMBER_TO_USE value to be higher than that.")
            .isLessThan(GALxxx_NEXT_MESSAGE_NUMBER_TO_USE);
    }






}