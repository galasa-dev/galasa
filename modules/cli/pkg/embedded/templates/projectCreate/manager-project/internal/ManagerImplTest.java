/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import static org.junit.Assert.*;

import org.junit.Test;

import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;

/**
 * {{.CapitalizedManagerName}}ManagerImplTest
 * 
 * Unit tests for the {{.CapitalizedManagerName}} Manager implementation.
 */
public class {{.CapitalizedManagerName}}ManagerImplTest {

    /**
     * Test that a resource can be created with a tag
     */
    @Test
    public void testResourceCreationWithTag() {
        // Given
        String expectedTag = "TEST_TAG";
        
        // When
        I{{.CapitalizedManagerName}}Resource resource = new {{.CapitalizedManagerName}}ResourceImpl(expectedTag);
        
        // Then
        assertNotNull("Resource should not be null", resource);
        assertEquals("Resource tag should match", expectedTag, resource.getTag());
    }

    /**
     * Test that the sample value method returns expected format
     */
    @Test
    public void testSampleValueContainsTag() {
        // Given
        String tag = "PRIMARY";
        I{{.CapitalizedManagerName}}Resource resource = new {{.CapitalizedManagerName}}ResourceImpl(tag);
        
        // When
        String sampleValue = resource.getSampleValue();
        
        // Then
        assertNotNull("Sample value should not be null", sampleValue);
        assertTrue("Sample value should contain the tag", sampleValue.contains(tag));
    }

    /**
     * Test manager info method
     */
    @Test
    public void testManagerInfo() {
        // Given
        {{.CapitalizedManagerName}}ManagerImpl manager = new {{.CapitalizedManagerName}}ManagerImpl();
        
        // When
        String info = manager.getManagerInfo();
        
        // Then
        assertNotNull("Manager info should not be null", info);
        assertTrue("Manager info should contain manager name", 
            info.contains("{{.CapitalizedManagerName}}"));
    }
}

