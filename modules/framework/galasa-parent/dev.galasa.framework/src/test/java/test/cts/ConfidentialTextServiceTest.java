/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.cts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import dev.galasa.framework.internal.cts.FrameworkConfidentialTextService;
import dev.galasa.framework.spi.ConfidentialTextException;

/**
 * This test class ensures that confidential texts that have been registered are
 * reomved from text.
 * 
 *  
 */
public class ConfidentialTextServiceTest {

    /**
     * The test method adds a confidential text to the service.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRegisterText() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        assertTrue("dummy", true);
    }

    /**
     * This test method ensures that any regitered words or phrases are removed from
     * a text.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRemoveConfidentialText() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        ctsService.registerText("test2", "This is a test comment");
        ctsService.registerText("test3", "This is a test comment");

        String testSentence = "The current password is test1, the old password is test3, and the new password is test2";
        String expected = "The current password is **1**, the old password is **3**, and the new password is **2**";

        String result = ctsService.removeConfidentialText(testSentence);
        System.out.println(result + "\n" + expected);
        assertEquals("Did not remove confidential imfomation ", expected, result);
    }

    /**
     * This test method ensures that any regitered words or phrases are removed from
     * a text.
     * 
     * @throws ConfidentialTextException
     * @throws IOException
     */
    @Test
    public void testRemoveConfidentialTextWithDollarSymbol() throws ConfidentialTextException, IOException {
        FrameworkConfidentialTextService ctsService = new FrameworkConfidentialTextService();

        ctsService.registerText("test1", "This is a test comment");
        ctsService.registerText("te$t2", "This is a test comment");
        ctsService.registerText("test3", "This is a test comment");

        String testSentence = "The current password is test1, the old password is test3, and the new password is te$t2";
        String expected = "The current password is **1**, the old password is **3**, and the new password is **2**";

        String result = ctsService.removeConfidentialText(testSentence);
        System.out.println(result + "\n" + expected);
        assertEquals("Did not remove confidential imfomation ", expected, result);
    }
}
