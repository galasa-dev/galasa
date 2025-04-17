/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.mocks;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;

import java.io.PrintWriter;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;

import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class MockServletBaseEnvironment {

    private static final Map<String, String> REQUIRED_HEADERS = new HashMap<>(Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT));

    MockFramework mockFramework;
    MockFileSystem mockFileSystem;
    MockArchiveStore archiveStore;
    IServletUnderTest servlet;

    MockHttpServletRequest req;
    List<IResultArchiveStoreDirectoryService> directoryServices;
    List<IRunResult> mockInputRunResults;
    ServletOutputStream outStream;
    PrintWriter writer;

    HttpServletResponse resp;

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, Map<String, String[]> parameterMap) {
        this(mockInpResults, new MockHttpServletRequest(parameterMap), new MockResultArchiveStoreDirectoryService(mockInpResults));
    }

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem) {
        this(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
        this.mockFileSystem = mockFileSystem;
        this.servlet.setFileSystem(this.mockFileSystem);
    }

    public MockServletBaseEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ) {
        this.setMockInputs(mockInpResults);
        this.directoryServices = setDirectoryService(rasStore);
        this.setArchiveStore(new MockArchiveStore(this.directoryServices));
        this.mockFramework = new MockFramework(this.archiveStore);
        setRequest(mockRequest);
        this.outStream = new MockServletOutputStream();
        this.writer = new PrintWriter(this.outStream);
        this.resp = new MockHttpServletResponse(this.writer, this.outStream);
        this.servlet = createServlet();
        this.servlet.setFramework(this.mockFramework);
    }

    public MockServletBaseEnvironment(MockFramework mockFramework, List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ) {
        this.setMockInputs(mockInpResults);
        this.directoryServices = setDirectoryService(rasStore);
        this.setArchiveStore(new MockArchiveStore(this.directoryServices));
        this.mockFramework = mockFramework;
        setRequest(mockRequest);
        this.outStream = new MockServletOutputStream();
        this.writer = new PrintWriter(this.outStream);
        this.resp = new MockHttpServletResponse(this.writer, this.outStream);
        this.servlet = createServlet();
        this.servlet.setFramework(this.mockFramework);
    }

    public abstract IServletUnderTest createServlet();

    public HttpServletResponse getResponse() {
        return this.resp;
    }

    public HttpServletRequest getRequest() {
        return this.req;
    }

    public void setRequest(MockHttpServletRequest mockRequest) {
        this.req = mockRequest;
        this.req.setHeaders(REQUIRED_HEADERS);
    }

    public RasServlet getRasServlet() {
        RasServlet rasServlet = (RasServlet)this.servlet;
        rasServlet.setResponseBuilder(new ResponseBuilder(new MockEnvironment()));
        return rasServlet;
    }

    public void setMockInputs(List<IRunResult> mockInpResults) {
        this.mockInputRunResults = mockInpResults;
    }

    public List<IResultArchiveStoreDirectoryService> setDirectoryService(MockResultArchiveStoreDirectoryService rasStore) {
        List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
        directoryServices.add(rasStore);
        return directoryServices;
    }

    public List<IResultArchiveStoreDirectoryService> getDirectoryService() {
        return this.directoryServices;
    }

    public void setArchiveStore(MockArchiveStore store) {
        this.archiveStore = store;
    }

}