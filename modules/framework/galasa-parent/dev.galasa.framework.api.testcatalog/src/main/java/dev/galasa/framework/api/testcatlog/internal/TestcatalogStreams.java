/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.testcatlog.internal;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.galasa.framework.spi.utils.GalasaGson;

/**
 * Basic Test Catalog store
 * 
 *  
 *
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/testcatalog/*" }, configurationPid = {
                "dev.galasa.testcatalog" }, configurationPolicy = ConfigurationPolicy.OPTIONAL, name = "Galasa Test Catalog Streams")
public class TestcatalogStreams extends HttpServlet {
    private static final long serialVersionUID       = 1L;

    private static final Log  logger                 = LogFactory.getLog(TestcatalogStreams.class);

    private final Pattern     patternValidStreamName = Pattern.compile("/[a-z0-9-_]+");

    private final GalasaGson  gson                   = new GalasaGson();

    private Path              catalogDirectory;                                                       // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            checkDirectory();

            String extraPath = req.getPathInfo();
            if (!checkPath(resp, extraPath)) {
                return;
            }

            String streamName = extraPath.substring(1);

//			String contentType = req.getHeader("Accept");  //TODO add a proper way of checking for this
//			if (!"application/json".equals(contentType)) {
//				resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "only application/json supported");
//				return;
//			}

            Path actualFile = catalogDirectory.resolve(streamName);

            if (!Files.exists(actualFile)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Test Catalog is missing");
                return;
            }

            resp.setContentType("application/json");
            resp.setContentLengthLong(Files.size(actualFile));

            Files.copy(actualFile, resp.getOutputStream());

            resp.setStatus(200);
        } catch (JsonParseException e) {
            throw new IOException("Problem processing the test catalog request", e); // NOSONAR TODO put in proper json
                                                                                     // error response
        } catch (Throwable t) {
            throw new IOException("Problem processing the test catalog request", t); // NOSONAR TODO put in proper json
                                                                                     // error response
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            checkDirectory();

            String extraPath = req.getPathInfo();
            if (!checkPath(resp, extraPath)) {
                return;
            }

            String streamName = extraPath.substring(1);

            String contentType = req.getHeader("Content-Type");
            if (!"application/json".equals(contentType)) {
                resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "only application/json supported");
                return;
            }

            // *** Read it in just to make sure it looks ok
            // *** TODO need to check the length or send to disk or something to avoid DOS

            String jsonData = IOUtils.toString(req.getReader());
            JsonObject tc = gson.fromJson(jsonData, JsonObject.class);

            if (tc == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Test Catalog content is missing");
                return;
            }

            if (!tc.has("classes") || !tc.has("bundles") || !tc.has("packages")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Test Catalog");
                return;
            }

            Path actualFile = catalogDirectory.resolve(streamName);

            Files.write(actualFile, jsonData.getBytes("utf-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Test Catalog written for stream " + streamName);

            resp.setStatus(200);
        } catch (JsonParseException e) {
            throw new IOException("Problem processing the test catalog request", e); // NOSONAR TODO put in proper json
                                                                                     // error response
        } catch (Throwable t) {
            throw new IOException("Problem processing the test catalog request", t); // NOSONAR TODO put in proper json
                                                                                     // error response
        }
    }

    private boolean checkPath(HttpServletResponse resp, String path) throws IOException {
        Matcher m = patternValidStreamName.matcher(path);
        if (!m.matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid stream name '" + path + "', must match regex '" + patternValidStreamName.pattern() + "'");
            return false;
        }
        return true;
    }

    private void checkDirectory() throws IOException {
        synchronized (Testcatalogs.class) {
            if (catalogDirectory == null) {
                throw new IOException("Catalog directory has not been defined");
            }
            if (!Files.exists(catalogDirectory)) {
                Files.createDirectories(catalogDirectory);
            }
        }
    }

    @Activate
    void activate(Map<String, Object> properties) {
        modified(properties);
    }

    @Modified
    void modified(Map<String, Object> properties) {
        Object oDirectoryProperty = properties.get("framework.testcatalog.directory");
        if (oDirectoryProperty != null && oDirectoryProperty instanceof String) {
            String directoryProperty = (String) oDirectoryProperty;
            try {
                catalogDirectory = Paths.get(new URL(directoryProperty).toURI());
                logger.info("Catalog directorty set to " + catalogDirectory.toUri().toString());
            } catch (Exception e) {
                logger.error("Problem with the catalog directory url", e);
            }
        } else {
            catalogDirectory = null;
        }
    }

    @Deactivate
    void deactivate() {
        this.catalogDirectory = null;
    }

}