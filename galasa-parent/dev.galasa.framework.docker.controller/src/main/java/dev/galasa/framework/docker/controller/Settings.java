/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.docker.controller;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Settings implements Runnable {

    private final Log         logger                 = LogFactory.getLog(getClass());

    private URL               configFileUrl;
    private String            bootstrap              = "http://galasa-api:8181/bootstrap";
    private String            containername;
    private String            engineLabel            = "none";
    private String            engineImage            = "none";
    private int               engineMemory           = 100;
    private int               engineMemoryRequest    = 150;
    private int               engineMemoryLimit      = 200;
    private String            network                = "";
    private ArrayList<String> dnsList                = new ArrayList<>();

    private HashSet<String>   requiredCapabilities   = new HashSet<>();
    private HashSet<String>   capableCapabilities    = new HashSet<>();
    private String            reportCapabilties      = null;

    private int               runPoll                = 20;
    private int               runPollRecheck         = 1;
    private int               maxEngines             = 0;

    private ArrayList<String> requestorsByScheduleID = new ArrayList<>();

    private String            oldConfigFile          = null;

    public Settings() throws MalformedURLException {

        loadEnvironmentProperties();
        retrieveConfigFile();
    }

    @Override
    public void run() {
        retrieveConfigFile();
    }

    private void retrieveConfigFile() {

        Properties properties = new Properties();

        try {
            Path configPath = Paths.get(configFileUrl.toURI());
            String newConfigFile = "";
            if (Files.exists(configPath)) {
                newConfigFile = new String(Files.readAllBytes(configPath), "utf-8");
            }
            if (newConfigFile.equals(oldConfigFile)) {
                return;
            }
            oldConfigFile = newConfigFile;

            logger.info("Config File has been changed, reloading parameters");

            properties.load(new ByteArrayInputStream(newConfigFile.getBytes()));
        } catch (Exception e) {
            logger.error("Failed to read the config file", e);
            return;
        }

        try {
            String newBootstrap = properties.getProperty("bootstrap");
            if (newBootstrap == null || newBootstrap.trim().isEmpty()) {
                newBootstrap = "http://galasa-api:8181/bootstrap";
            }
            if (!newBootstrap.equals(this.bootstrap)) {
                logger.info("Setting Boostrap from '" + this.bootstrap + "' to '" + newBootstrap + "'");
                this.bootstrap = newBootstrap;
            }
        } catch (Exception e) {
            logger.error("Error processing bootstrap in configfile", e);
        }

        try {
            String maxEnginesString = properties.getProperty("max_engines");
            if (maxEnginesString == null || maxEnginesString.trim().isEmpty()) {
                maxEnginesString = "2";
            }
            int newMaxEngines = Integer.parseInt(maxEnginesString);
            if (newMaxEngines != maxEngines) {
                logger.info("Setting Max Engines from " + maxEngines + " to " + newMaxEngines);
                maxEngines = newMaxEngines;
            }
        } catch (Exception e) {
            logger.error("Error processing max_engines in configfile", e);
        }

        try {
            String newEngineLabel = properties.getProperty("engine_label");
            if (newEngineLabel == null || newEngineLabel.trim().isEmpty()) {
                newEngineLabel = "docker_standard_engine";
            }
            if (!newEngineLabel.equals(engineLabel)) {
                logger.info("Setting Engine Label from '" + engineLabel + "' to '" + newEngineLabel + "'");
                engineLabel = newEngineLabel;
            }
        } catch (Exception e) {
            logger.error("Error processing engine_label in configfile", e);
        }

        try {
            String newEngineImage = properties.getProperty("engine_image");
            if (newEngineImage == null || newEngineImage.trim().isEmpty()) {
                newEngineImage = "galasadev/galasa-boot-embedded-amd64:latest";
            }
            if (!newEngineImage.equals(engineImage)) {
                logger.info("Setting Engine Image from '" + engineImage + "' to '" + newEngineImage + "'");
                engineImage = newEngineImage;
            }
        } catch (Exception e) {
            logger.error("Error processing engine_image in configfile", e);
        }

        try {
            String newEngineMemory = properties.getProperty("engine_memory");
            if (newEngineMemory == null || newEngineMemory.trim().isEmpty()) {
                newEngineMemory = "300";
            }
            Integer memory = Integer.parseInt(newEngineMemory);
            if (memory != engineMemory) {
                logger.info("Setting Engine Memory from '" + engineMemory + "' to '" + memory + "'");
                engineMemory = memory;
            }
        } catch (Exception e) {
            logger.error("Error processing engine_memory in configfile", e);
        }

        try {
            String newEngineMemoryRequest = properties.getProperty("engine_memory_request");
            if (newEngineMemoryRequest == null || newEngineMemoryRequest.trim().isEmpty()) {
                newEngineMemoryRequest = Integer.toString(engineMemory + 50);
            }
            Integer memory = Integer.parseInt(newEngineMemoryRequest);
            if (memory != engineMemoryRequest) {
                logger.info("Setting Engine Memory Request from '" + engineMemoryRequest + "' to '" + memory + "'");
                engineMemoryRequest = memory;
            }
        } catch (Exception e) {
            logger.error("Error processing engine_memory_request in configfile", e);
        }

        try {
            String newEngineMemoryLimit = properties.getProperty("engine_memory_limit");
            if (newEngineMemoryLimit == null || newEngineMemoryLimit.trim().isEmpty()) {
                newEngineMemoryLimit = Integer.toString(engineMemory + 100);
            }
            Integer memory = Integer.parseInt(newEngineMemoryLimit);
            if (memory != engineMemoryLimit) {
                logger.info("Setting Engine Memory Limit from '" + engineMemoryLimit + "' to '" + memory + "'");
                engineMemoryLimit = memory;
            }
        } catch (Exception e) {
            logger.error("Error processing engine_memory_limit in configfile", e);
        }

        try {
            String newRunPoll = properties.getProperty("run_poll");
            if (newRunPoll == null || newRunPoll.trim().isEmpty()) {
                newRunPoll = "20";
            }
            Integer poll = Integer.parseInt(newRunPoll);
            if (poll != runPoll) {
                logger.info("Setting Run Poll from '" + runPoll + "' to '" + poll + "'");
                runPoll = poll;
            }
        } catch (Exception e) {
            logger.error("Error processing run_poll in configfile", e);
        }

        try {
            String newRunPoll = properties.getProperty("run_poll_recheck");
            if (newRunPoll == null || newRunPoll.trim().isEmpty()) {
                newRunPoll = "1";
            }
            Integer poll = Integer.parseInt(newRunPoll);
            if (poll != runPollRecheck) {
                logger.info("Setting Run Poll Recheck from '" + runPollRecheck + "' to '" + poll + "'");
                runPollRecheck = poll;
            }
        } catch (Exception e) {
            logger.error("Error processing run_poll_recheck in configfile", e);
        }

        try {
            String newRequestors = properties.getProperty("scheduled_requestors");
            if (newRequestors == null || newRequestors.trim().isEmpty()) {
                newRequestors = "";
            }
            ArrayList<String> newRequestorsByScheduleid = new ArrayList<>();

            String requestors[] = newRequestors.split(",");
            for (String requestor : requestors) {
                newRequestorsByScheduleid.add(requestor);
            }

            if (!requestorsByScheduleID.equals(newRequestorsByScheduleid)) {
                logger.info("Setting Requestors by Schedule from '" + requestorsByScheduleID + "' to '"
                        + newRequestorsByScheduleid + "'");
                requestorsByScheduleID = newRequestorsByScheduleid;
            }
        } catch (Exception e) {
            logger.error("Error processing scheduled_requestors in configfile", e);
        }

        try {
            String newCapabilities = properties.getProperty("engine_capabilities");
            if (newCapabilities == null || newCapabilities.trim().isEmpty()) {
                newCapabilities = "";
            }
            ArrayList<String> newRequiredCapabilties = new ArrayList<>();
            ArrayList<String> newCapableCapabilties = new ArrayList<>();

            String capabalities[] = newCapabilities.split(",");
            for (String capability : capabalities) {
                capability = capability.trim();
                if (capability.startsWith("+")) {
                    capability = capability.substring(1);
                    if (!capability.isEmpty()) {
                        newRequiredCapabilties.add(capability);
                    }
                } else {
                    if (!capability.isEmpty()) {
                        newCapableCapabilties.add(capability);
                    }
                }
            }

            boolean changed = false;
            if (newRequiredCapabilties.size() != requiredCapabilities.size()
                    || newCapableCapabilties.size() != capableCapabilities.size()) {
                changed = true;
            } else {
                for (String cap : newCapableCapabilties) {
                    if (!capableCapabilities.contains(cap)) {
                        changed = true;
                        break;
                    }
                }
                for (String cap : newRequiredCapabilties) {
                    if (!requiredCapabilities.contains(cap)) {
                        changed = true;
                        break;
                    }
                }
            }

            if (changed) {
                capableCapabilities.clear();
                requiredCapabilities.clear();
                capableCapabilities.addAll(newCapableCapabilties);
                requiredCapabilities.addAll(newRequiredCapabilties);
                logger.info("Engine set with Required Capabilities - " + requiredCapabilities);
                logger.info("Engine set with Capabable Capabilities - " + capableCapabilities);

                StringBuilder report = new StringBuilder();
                for (String cap : requiredCapabilities) {
                    if (report.length() > 0) {
                        report.append(",");
                    }
                    report.append("+");
                    report.append(cap);
                }
                for (String cap : capableCapabilities) {
                    if (report.length() > 0) {
                        report.append(",");
                    }
                    report.append(cap);
                }
                if (report.length() > 0) {
                    reportCapabilties = report.toString();
                } else {
                    reportCapabilties = null;
                }

            }
        } catch (Exception e) {
            logger.error("Error processing engine_capabilities in configfile", e);
        }

        return;
    }

    private void loadEnvironmentProperties() throws MalformedURLException {

        containername = System.getenv("CONTAINERNAME");
        if (containername == null || containername.trim().isEmpty()) {
            containername = "docker-controller";
        } else {
            containername = containername.trim();
        }
        logger.info("Setting Container Name to '" + containername + "'");

        String sConfigFileUrl = System.getenv("CONFIG");
        if (sConfigFileUrl == null || sConfigFileUrl.trim().isEmpty()) {
            sConfigFileUrl = "file:/etc/galasa.properties";
        } else {
            sConfigFileUrl = sConfigFileUrl.trim();
        }
        logger.info("Setting Config File Url to '" + configFileUrl + "'");

        configFileUrl = new URL(sConfigFileUrl);

        return;
    }

    public String getPodName() {
        return this.containername;
    }

    public String getEngineLabel() {
        return this.engineLabel;
    }

    public int getMaxEngines() {
        return this.maxEngines;
    }

    public List<String> getRequestorsByGroup() {
        return this.requestorsByScheduleID;
    }

    public String getEngineImage() {
        return this.engineImage;
    }

    public int getEngineMemoryRequest() {
        return this.engineMemoryRequest;
    }

    public int getEngineMemoryLimit() {
        return this.engineMemoryLimit;
    }

    public int getEngineMemory() {
        return this.engineMemory;
    }

    public String getBootstrap() {
        return this.bootstrap;
    }

    public int getRunPoll() {
        return runPoll;
    }

    public int getRunPollRecheck() {
        return runPollRecheck;
    }

    public List<String> getDns() {
        return dnsList;
    }

    public String getNetwork() {
        return network;
    }

    public String getReportCapabilities() {
        return reportCapabilties;
    }
}