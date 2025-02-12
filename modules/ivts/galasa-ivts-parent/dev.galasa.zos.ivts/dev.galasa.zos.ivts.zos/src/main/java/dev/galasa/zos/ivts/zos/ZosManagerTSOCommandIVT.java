/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;

@Test
public class ZosManagerTSOCommandIVT {
    
    private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
   
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosTSOCommand(imageTag =  IMG_TAG)
    public IZosTSOCommand command;
    
    @TestProperty(prefix = "IVT.RUN",suffix = "NAME", required = false)
    public String providedRunName;
    
    private String runName  = new String();

    @Test
    public void preFlightTests() throws Exception {
        // Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
        runName = coreManager.getRunName();
        logger.info("Using Run ID of: " + runName);
    }
    
    @Test
    public void basicCommandExecution() throws ZosTSOCommandException  {
    	String machineTime = command.issueCommand("TIME").trim();
    	Calendar current = Calendar.getInstance();
    	
    	String pattern = "[A-Z0-9]+\\sTIME-([0-9:]+)\\s([AMP]+).\\s[A-Z0-9:-]+\\s[A-Z0-9:-]+\\s[A-Z0-9:-]+\\s([A-Z]+)\\s([0-9]+),([0-9]+)";
    	final Pattern extractDate = Pattern.compile(pattern);
    	Matcher m = extractDate.matcher(machineTime);
    	assertThat(m.find()).isTrue();
    	
    	assertThat(m.group(5)).isEqualTo(Integer.toString(current.get(Calendar.YEAR)));
    	assertThat(m.group(4)).isEqualTo(Integer.toString(current.get(Calendar.DAY_OF_MONTH)));
    	assertThat(m.group(3)).isEqualToIgnoringCase(current.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH));
    }
   
}
