/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.rps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.internal.rps.ResourceString;
import dev.galasa.framework.spi.FrameworkResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourcePoolingServiceException;

/**
 * This test method creates resource tests strings and checks the behaviour of
 * generating resources.
 * 
 *  
 */
public class ResourcesPoolingServiceTest {

    /**
     * This test metho ensures that a resource string can hold and maintain constant
     * characters.
     * 
     * @throws ResourcePoolingServiceException
     */
    @Test
    public void testResourceString() throws ResourcePoolingServiceException {

        ResourceString rs = new ResourceString("Hello");
        assertEquals("Unexpected resource. ", "Hello", rs.getRandomResource());
    }

    /**
     * This test method ensures that variable characters can be parsed.
     * 
     * @throws ResourcePoolingServiceException
     */
    @Test
    public void testComplexConstantString() throws ResourcePoolingServiceException {

        ResourceString rs = new ResourceString("Hello{0}{0}{0}{0}Test");
        assertEquals("Unexpected resource", "Hello0000Test", rs.getRandomResource());
    }

    /**
     * This test method makes sure the getNextResource provides resources in the
     * correct order.
     * 
     * @throws ResourcePoolingServiceException
     */
    @Test
    public void testGetNextResource() throws ResourcePoolingServiceException {
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();

        expected.add("Helloa0Test");
        expected.add("Helloa1Test");
        expected.add("Helloa2Test");
        expected.add("Helloa3Test");
        expected.add("Helloa4Test");
        expected.add("Helloa5Test");
        expected.add("Helloa6Test");
        expected.add("Helloa7Test");
        expected.add("Helloa8Test");
        expected.add("Helloa9Test");
        expected.add("Hellob0Test");

        ResourceString rs = new ResourceString("Hello{z}{9}Test");
        results.add(rs.getFirstResource());
        for (int i = 0; i < 11; i++) {
            results.add(rs.getNextResource());
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(" Unexpected resource.", expected.get(i), results.get(i));
        }
    }

    /**
     * This test method checks the most simple obtain resources method.
     * 
     * @throws InsufficientResourcesAvailableException
     */
    @Test
    public void testResourcePoolingObtainSimple()
            throws InsufficientResourcesAvailableException, ResourcePoolingServiceException {
        FrameworkResourcePoolingService frps = new FrameworkResourcePoolingService();
        List<String> resourceStrings = new ArrayList<>();

        resourceStrings.add("GAL{9}{9}{9}{z}");

        List<String> resources = frps.obtainResources(resourceStrings, null);
        assertEquals("Unexpected number of resources retrieved", 10, resources.size());
    }

    /**
     * This test method checks the most simple obtain resources method.
     * 
     * @throws InsufficientResourcesAvailableException
     */
    @Test
    public void testResourcePoolingObtainWithReturnNumber()
            throws InsufficientResourcesAvailableException, ResourcePoolingServiceException {
        FrameworkResourcePoolingService frps = new FrameworkResourcePoolingService();
        List<String> resourceStrings = new ArrayList<>();

        resourceStrings.add("GAL{9}{9}{9}{z}");

        List<String> resources = frps.obtainResources(resourceStrings, null, 30);
        assertEquals("Unexpected number of resources retrieved", 30, resources.size());
    }

    /**
     * This test method checks the consecutive resources are created correctly.
     */
    @Test
    public void testResourcePoolingObtainConsecutive() throws InsufficientResourcesAvailableException {
        FrameworkResourcePoolingService frps = new FrameworkResourcePoolingService();
        List<String> resourceStrings = new ArrayList<>();
        List<String> bannedStrings = new ArrayList<>();
        List<String> expected = new ArrayList<>();

        expected.add("APPLID00");
        expected.add("APPLID01");
        expected.add("APPLID02");
        expected.add("APPLID03");
        expected.add("APPLID04");

        expected.add("APPLID15");
        expected.add("APPLID16");
        expected.add("APPLID17");
        expected.add("APPLID18");
        expected.add("APPLID19");

        resourceStrings.add("APPLID{1}{9}");

        bannedStrings.add("APPLID05");
        bannedStrings.add("APPLID06");
        bannedStrings.add("APPLID07");
        bannedStrings.add("APPLID08");
        bannedStrings.add("APPLID09");
        bannedStrings.add("APPLID10");
        bannedStrings.add("APPLID11");
        bannedStrings.add("APPLID12");
        bannedStrings.add("APPLID13");
        bannedStrings.add("APPLID14");

        List<String> resources = frps.obtainResources(resourceStrings, bannedStrings, 10, 5);
        Collections.sort(resources);

        assertEquals("Unexpected resources retrieved", expected, resources);
    }

    /**
     * This test method checks a unlikely but complicated case pulling from multiple
     * resource string definitions, where there is only one possible way to pick the
     * reousrces.
     * 
     * It has the possibility to randomly generate the resources (but unlikely) but
     * them generates sequentially if failed.
     * 
     * @throws InsufficientResourcesAvailableException
     */
    @Test
    public void testResourcePoolingObtainFromMultipleResourceStrings() throws InsufficientResourcesAvailableException {
        FrameworkResourcePoolingService frps = new FrameworkResourcePoolingService();
        List<String> resourceStrings = new ArrayList<>();
        List<String> bannedStrings = new ArrayList<>();
        List<String> answers = new ArrayList<>();

        answers.add("APPLID0");
        answers.add("APPLID1");
        answers.add("APPLID2");
        answers.add("APPLID3");
        answers.add("APPLID4");
        answers.add("APPLID5");

        answers.add("GAL0");
        answers.add("GAL1");
        answers.add("GAL2");
        answers.add("GAL3");
        answers.add("GAL4");
        answers.add("GAL5");
        answers.add("GAL7");
        answers.add("GAL8");
        answers.add("GAL9");

        answers.add("GALASA3");
        answers.add("GALASA4");
        answers.add("GALASA5");
        answers.add("GALASA6");
        answers.add("GALASA7");
        answers.add("GALASA8");

        resourceStrings.add("APPLID{9}");
        resourceStrings.add("GAL{9}");
        resourceStrings.add("GALASA{9}");

        bannedStrings.add("GAL6");
        bannedStrings.add("APPLID6");
        bannedStrings.add("APPLID8");
        bannedStrings.add("GALASA2");
        bannedStrings.add("GALASA9");

        for (int i = 0; i < 10; i++) {
            List<String> resources = frps.obtainResources(resourceStrings, bannedStrings, 21, 3);
            Collections.sort(resources);
            assertEquals("Unexpected resources retrieved on run  " + i, answers, resources);
        }
    }

    /**
     * This tests to see if an exception is thrown if the returnConsecutive is not a
     * mulitple of the return min.
     */
    @Test
    public void testNonMultipleConsecutiveResource() {
        FrameworkResourcePoolingService frps = new FrameworkResourcePoolingService();
        List<String> resourceStrings = new ArrayList<>();
        Boolean caught = false;

        resourceStrings.add("GAL{9}{9}{9}{9}");

        try {
            frps.obtainResources(resourceStrings, null, 41, 5);
        } catch (InsufficientResourcesAvailableException e) {
            caught = true;
        }

        assertTrue("Exception not thrown", caught);
    }
}
