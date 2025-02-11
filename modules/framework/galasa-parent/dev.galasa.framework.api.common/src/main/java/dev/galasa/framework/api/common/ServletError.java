/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;

public class ServletError {

    String [] params ;
    ServletErrorMessage template ;
    String message ;

    private static final Log logger = LogFactory.getLog(ServletError.class);

    public ServletError( ServletErrorMessage template , String... params ) {

        String templateString = template.toString();        
        this.message = MessageFormat.format(templateString, (Object[])params);

        this.template = template;
        this.params = params;

        logger.info("ServletError(...) "+message);
    }

    public String toJsonString() {

        JsonObject obj = new JsonObject();
        int templateNumber = template.getTemplateNumber();
        obj.addProperty("error_code", templateNumber);
		obj.addProperty("error_message", this.message);

        String renderedJsonMessage = obj.toString();

        return renderedJsonMessage ;
    }

    public String getMessage() {
        return this.message ;
    }

}