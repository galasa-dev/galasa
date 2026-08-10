/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;

public class {{.CapitalizedManagerName}}ManagerImplTest {

    @Test
    public void testResourceCreationWithTag() {
        String expectedTag = "TEST_TAG";
        I{{.CapitalizedManagerName}}Resource resource = new {{.CapitalizedManagerName}}ResourceImpl(expectedTag);
        
        assertNotNull(resource);
        assertEquals(expectedTag, resource.getTag());
    }

    @Test
    public void testResourceCreationWithNullTagThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new {{.CapitalizedManagerName}}ResourceImpl(null);
        });
    }
}

