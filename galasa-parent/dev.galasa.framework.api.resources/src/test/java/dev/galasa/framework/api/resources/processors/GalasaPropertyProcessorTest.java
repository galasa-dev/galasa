/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;

public class GalasaPropertyProcessorTest extends ResourcesServletTest {

    @Test
    public void testProcessGalasaPropertyValidPropertyReturnsOK() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyWithNewNamespaceReturnsOK() throws Exception {
        //Given...
        String namespace = "newnamespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet("framework");
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        checkPropertyInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyInvalidPropertyNameReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property1!";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);


        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property1!' much have at least two parts separated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyNameWithTrailingDotReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property.name.";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5044E: Invalid property name. Property name 'property.name.' must not end with a . (dot) separator.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyPropertyNameWithLeadingDotReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = ".property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5041E: Invalid property name. '.property.name' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadPropertyNameReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5043E: Invalid property name. Property name 'property' much have at least two parts separated by a . (dot).");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyNameReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyNamespaceReturnsError() throws Exception {
        //Given...
        String namespace = "";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadNamespaceReturnsError() throws Exception {
        //Given...
        String namespace = "namespace@";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace@' must not contain the '@' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNamespaceWithTrailingDotReturnsError() throws Exception {
        //Given...
        String namespace = "namespace.";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5033E: Invalid namespace name. 'namespace.' must not contain the '.' character. Allowable characters after the first character are a-z, A-Z, 0-9.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNamespaceWithLeadingDotReturnsError() throws Exception {
        //Given...
        String namespace = ".namespace";
        String propertyname = "property.name";
        String value = "myvalue";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5032E: Invalid namespace name. '.namespace' must not start with the '.' character. Allowable first characters are a-z or A-Z.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingPropertyValueReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyEmptyFieldsReturnsError() throws Exception {
        //Given...
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "galasa-dev/v1alpha1");

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(errors.get(0)).contains("GAL5040E: Invalid property name. Property name is missing or empty.");
        assertThat(errors.get(1)).contains("GAL5031E: Invalid namespace. Namespace is empty.");
        assertThat(errors.get(2)).contains("GAL5024E: Error occurred because the Galasa Property is invalid. 'The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.'");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyNoMetadataOrDataReturnsError() throws Exception {
        //Given...
        String namespace = "";
        String propertyname = "";
        String value = "";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        String jsonString = "{\"apiVersion\": \"galasa-dev/v1alpha1\",\n\"kind\": \"GalasaProperty\",\"metadata\": {},\"data\": {}}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        List<String> errors = propertyProcessor.processResource(propertyJson, APPLY);

        //Then...
        assertThat(errors).isNotNull();
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.get(0)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'metadata' field cannot be empty. The fields 'name' and 'namespace' are mandatory for the type GalasaProperty.");
        assertThat(errors.get(1)).contains("GAL5024E: Error occurred because the Galasa Property is invalid.",
            "The 'data' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyMissingApiVersionReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        JsonObject propertyJson = generatePropertyJson(namespace, propertyname, value, "");

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, APPLY);
        });

        //Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5027E: Error occurred. The field 'apiVersion' in the request body is invalid.");
        checkPropertyNotInNamespace(namespace,propertyname,value);
    }

    @Test
    public void testProcessGalasaPropertyBadJsonReturnsError() throws Exception {
        //Given...
        String namespace = "framework";
        String propertyname = "property.name";
        String value = "value";
        setServlet(namespace);
        MockResourcesServlet servlet = getServlet();
        CPSFacade cps = new CPSFacade(servlet.getFramework());
        GalasaPropertyProcessor propertyProcessor = new GalasaPropertyProcessor(cps);
        String jsonString = "{\"apiVersion\":\"galasa-dev/v1alpha1\","+namespace+"."+propertyname+":"+value+"}";
        JsonObject propertyJson = JsonParser.parseString(jsonString).getAsJsonObject();

        //When...
        Throwable thrown = catchThrowable(() -> {
            propertyProcessor.processResource(propertyJson, APPLY);
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
}
