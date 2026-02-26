/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;

import org.junit.Test;

import com.google.gson.JsonObject;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestRasHealthRoute extends RasServletTest {

  @Test
  public void testPathRegexMatchesHealthEndpoint() {
    // Given...
    String expectedPath = RasHealthRoute.path;
    String inputPath = "/health";

    // When...
    boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

    // Then...
    assertThat(matches).isTrue();
  }

  @Test
  public void testPathRegexMatchesHealthEndpointWithTrailingSlash() {
    // Given...
    String expectedPath = RasHealthRoute.path;
    String inputPath = "/health/";

    // When...
    boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

    // Then...
    assertThat(matches).isTrue();
  }

  @Test
  public void testHealthEndpointWithHealthyRasReturnsOkStatus() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults);
    mockRasStore.setHealthy(true);

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();
    ServletOutputStream outStream = resp.getOutputStream();

    // When...
    servlet.init();
    servlet.doGet(req, resp);

    // Then...
    JsonObject expectedJson = new JsonObject();
    expectedJson.addProperty("status", "ok");

    assertThat(resp.getStatus()).isEqualTo(200);
    assertThat(outStream.toString()).isEqualTo(expectedJson.toString());
    assertThat(resp.getContentType()).isEqualTo("application/json");
  }

  @Test
  public void testHealthEndpointWithUnhealthyRasReturnsServiceUnavailable() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults);
    mockRasStore.setHealthy(false);

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();

    // When...
    servlet.init();
    servlet.doGet(req, resp);

    // Then...
    assertThat(resp.getStatus()).isEqualTo(503);
    assertThat(resp.getContentType()).isEqualTo("application/json");

    String responseBody = resp.getOutputStream().toString();
    assertThat(responseBody).contains("GAL5449");
  }

  @Test
  public void testHealthEndpointWithExceptionInHealthCheckReturnsServiceUnavailable() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults) {
      @Override
      public boolean isHealthy() throws ResultArchiveStoreException {
        throw new ResultArchiveStoreException("Simulated RAS exception");
      }
    };

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();

    // When...
    servlet.init();
    servlet.doGet(req, resp);

    // Then...
    assertThat(resp.getStatus()).isEqualTo(503);
  }

  @Test
  public void testHealthEndpointWithPostRequestReturnsMethodNotAllowed() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults);
    mockRasStore.setHealthy(true);

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();

    // When...
    servlet.init();
    servlet.doPost(req, resp);

    // Then...
    assertThat(resp.getStatus()).isEqualTo(405);
  }

  @Test
  public void testHealthEndpointWithPutRequestReturnsMethodNotAllowed() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults);
    mockRasStore.setHealthy(true);

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();

    // When...
    servlet.init();
    servlet.doPut(req, resp);

    // Then...
    assertThat(resp.getStatus()).isEqualTo(405);
  }

  @Test
  public void testHealthEndpointWithDeleteRequestReturnsMethodNotAllowed() throws Exception {
    // Given...
    List<IRunResult> mockInputRunResults = new ArrayList<>();
    Map<String, String[]> parameterMap = new HashMap<>();
    MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/health");

    MockResultArchiveStoreDirectoryService mockRasStore = new MockResultArchiveStoreDirectoryService(
        mockInputRunResults);
    mockRasStore.setHealthy(true);

    MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest,
        mockRasStore);

    RasServlet servlet = mockServletEnvironment.getServlet();
    HttpServletRequest req = mockServletEnvironment.getRequest();
    HttpServletResponse resp = mockServletEnvironment.getResponse();

    // When...
    servlet.init();
    servlet.doDelete(req, resp);

    // Then...
    assertThat(resp.getStatus()).isEqualTo(405);
  }
}

// Made with Bob
