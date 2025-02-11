/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.RBACValidator;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.Action;

public class GalasaPropertyProcessorTest extends ResourcesServletTest {

    @Test
    public void testProcessGalasaPropertyValidPropertyReturnsOK() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();

        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyWithNewNamespaceReturnsOK() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "newnamespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet("framework");
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyInvalidPropertyNameReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property1!";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);


        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property1!' much have at least two parts separated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyNameWithTrailingDotReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property.name.";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5044E: Invalid property name. Property name 'property.name.' must not end with a . (dot) separator.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyNameWithLeadingDotReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = ".property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5041E: Invalid property name. '.property.name' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadPropertyNameReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property' much have at least two parts separated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyNameReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyNamespaceReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadNamespaceReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "namespace@";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace@' must not contain the '@' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNamespaceWithTrailingDotReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "namespace.";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace.' must not contain the '.' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNamespaceWithLeadingDotReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = ".namespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5032E: Invalid namespace name. '.namespace' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyValueIsTreatedAsBlankReturnsOK() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    public void testProcessGalasaPropertyEmptyFieldsReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        assertThat(errors.get(1)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNoMetadataOrDataReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        String jsonString = "{\"apiVersion\": \"galasa-dev/v1alpha1\",\n\"kind\": \"GalasaProperty\",\"metadata\": {},\"data\": {}}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY, username);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.get(0)).contains("GAL5415E");
        assertThat(errors.get(1)).contains("GAL5417E");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingApiVersionReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "");

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, APPLY, username);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5027E: Error occurred. The field 'apiVersion' in the request body is invalid.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadJsonReturnsError() throws Exception {
        //Given...
        String username = "myuser";
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        IFramework mockFramework = servlet.getFramework();
        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);
        String jsonString = "{\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, APPLY, username);
        });

        //Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(
            thrown.getMessage(),
            5069,
            "GAL5069E",
            "Invalid request body provided. The following mandatory fields are missing",
            "[metadata, data]"
        );
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testValidateCreatePermissionsWithMissingPropertiesSetReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockIConfigurationPropertyStoreService cpsService = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(cpsService);
        mockFramework.setRBACService(mockRbacService);

        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            propertyProcessor.validateActionPermissions(CREATE, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_SET");
    }

    @Test
    public void testValidateApplyPermissionsWithMissingPropertiesSetReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockIConfigurationPropertyStoreService cpsService = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(cpsService);
        mockFramework.setRBACService(mockRbacService);

        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            propertyProcessor.validateActionPermissions(APPLY, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_SET");
    }

    @Test
    public void testValidateUpdatePermissionsWithMissingPropertiesSetReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockIConfigurationPropertyStoreService cpsService = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(cpsService);
        mockFramework.setRBACService(mockRbacService);

        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            propertyProcessor.validateActionPermissions(UPDATE, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_SET");
    }

    @Test
    public void testValidateDeletePermissionsWithMissingPropertiesDeleteReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockIConfigurationPropertyStoreService cpsService = new MockIConfigurationPropertyStoreService();
        MockFramework mockFramework = new MockFramework(cpsService);
        mockFramework.setRBACService(mockRbacService);

        CPSFacade cps = new CPSFacade(mockFramework);
        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            propertyProcessor.validateActionPermissions(DELETE, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_DELETE");
    }
}
