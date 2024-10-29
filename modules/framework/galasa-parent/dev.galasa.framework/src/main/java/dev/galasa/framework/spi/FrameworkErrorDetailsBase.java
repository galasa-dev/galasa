/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;
import com.google.gson.JsonObject;
public class FrameworkErrorDetailsBase implements FrameworkErrorDetails {
    

    private String message;
    private int code;

    public static final int UNKNOWN = 0 ;

    public FrameworkErrorDetailsBase(String message) {
        this(UNKNOWN, message);
    }

    public FrameworkErrorDetailsBase(int code , String message) {
        this.code = code ;
        this.message = message ;
    }

    @Override
    public int getErrorCode() {
        return this.code;
    }

    @Override
    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("error_code", this.code);
		obj.addProperty("error_message", this.message);

        String renderedJsonMessage = obj.toString();

        return renderedJsonMessage ;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
    
}