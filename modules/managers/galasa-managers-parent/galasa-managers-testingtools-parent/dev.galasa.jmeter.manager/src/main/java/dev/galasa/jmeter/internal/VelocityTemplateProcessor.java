/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Utility class for processing Velocity templates in JMeter files
 */
public class VelocityTemplateProcessor {
    
    private boolean velocityInitialized = false;
    
    /**
     * Initialize Velocity engine
     */
    private void initializeVelocity() throws JMeterManagerException {
        if (!velocityInitialized) {
            try {
                Velocity.init();
                velocityInitialized = true;
            } catch (Exception e) {
                throw new JMeterManagerException("Failed to initialize Velocity engine", e);
            }
        }
    }
    
    /**
     * Process a template string with the given parameters
     * 
     * @param template The template string to process
     * @param parameters Map of parameter names to values
     * @return Processed template as string
     * @throws JMeterManagerException if template processing fails
     */
    public String processTemplate(String template, Map<String, Object> parameters) 
            throws JMeterManagerException {
        initializeVelocity();
        
        VelocityContext context = new VelocityContext();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        
        StringWriter writer = new StringWriter();
        try {
            Velocity.evaluate(context, writer, JMeterConstants.VELOCITY_TEMPLATE_NAME, template);
            return writer.toString();
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to process Velocity template", e);
        }
    }
    
    /**
     * Process a template from an InputStream with the given parameters
     * 
     * @param templateStream The template input stream to process
     * @param parameters Map of parameter names to values
     * @return Processed template as InputStream
     * @throws JMeterManagerException if template processing fails
     */
    public InputStream processStream(InputStream templateStream, Map<String, Object> parameters) 
            throws JMeterManagerException {
        initializeVelocity();
        
        // Add safe EOF to prevent stream issues
        InputStream safeEOF = new ByteArrayInputStream(" ".getBytes(StandardCharsets.UTF_8));
        InputStream streamPlus = new SequenceInputStream(templateStream, safeEOF);
        InputStreamReader reader = new InputStreamReader(streamPlus, StandardCharsets.UTF_8);
        
        VelocityContext context = new VelocityContext();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        
        try {
            Velocity.evaluate(context, writer, JMeterConstants.VELOCITY_TEMPLATE_NAME, reader);
            writer.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception e) {
            throw new JMeterManagerException("Failed to process Velocity template stream", e);
        }
    }
}
