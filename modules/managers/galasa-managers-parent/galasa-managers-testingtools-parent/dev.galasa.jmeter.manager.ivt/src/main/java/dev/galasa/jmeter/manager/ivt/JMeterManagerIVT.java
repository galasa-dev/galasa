/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.JMeterSession;

@Test
 public class JMeterManagerIVT {

    @Logger
    public Log logger;

    @BundleResources
    public IBundleResources resources;

    @JMeterSession(jmxPath = "DynamicTest.jmx", propPath = "jmeter.properties")
    public IJMeterSession session;

    @JMeterSession(jmxPath = "ExistingTest.jmx")
    public IJMeterSession session2;

    @Test
    public void provisionedNotNull() {

      assertThat(logger).isNotNull();
      assertThat(session).isNotNull();
      assertThat(session2).isNotNull();
    
    }

    @Test
    public void startJMeterTestWithDynamicHosts() throws JMeterManagerException, TestBundleResourceException {
      InputStream jmxStream = resources.retrieveFile("/DynamicTest.jmx");
      InputStream propStream = resources.retrieveFile("/jmeter.properties");

      /**
       * Substituting variables from the adapted JMX-file 
       * If we want to dynamically change the host, the JMX-file needs to have $VARIABLE to substitute
       * This is a special case to adapt JMX-files to make them dynamic during runtime
       * 
       * USABLE VARIABLES in this Dynamic JMX: HOST, PORT, PROTOCOL, PATH, THREADS (DEFAULT 1), RAMPUP, DURATION (DEFAULT 15 seconds)
       * HOST is Mandatory
       * 
       * look at the DynamicTest.jmx file for further clarification
       *  */ 
      HashMap<String,Object> map = new HashMap<>();
      map.put("HOST", "galasa.dev");
      map.put("PATH", "/docs");

      session.setChangedParametersJmxFile(jmxStream, map);
      session.applyProperties(propStream);
      session.startJmeter();
      assertThat(session.statusTest()).isTrue();
    }

    @Test
    public void startJMeterTestStatically() throws JMeterManagerException, TestBundleResourceException {
      InputStream jmxStream = resources.retrieveFile("/ExistingTest.jmx");

      session2.setDefaultGeneratedJmxFile(jmxStream);
      session2.startJmeter();

      assertThat(session2.statusTest()).isTrue();
    }

  
 }