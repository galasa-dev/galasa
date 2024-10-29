/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.auth.couchdb.internal;


import java.text.MessageFormat;

public enum Errors {

    ERROR_FAILED_TO_PARSE_COUCHDB_DESIGN_DOC                          (7500,
        "GAL7500E: The Galasa auth extension could not check that couchdb has the correct definition for the dababase in which access tokens are stored."+
        "The design of the database could not be parsed. Please report this error to your Galasa system administrator. Detailed cause of this problem: {}"),
    ERROR_FAILED_TO_UPDATE_COUCHDB_DESING_DOC_CONFLICT                (7501,
        "GAL7501E: The Galasa auth extension could not upgrade the definition of the couchdb database in which access tokens are stored."+
        "The design of the database could not be updated due to clashing updates.  Please report this error to your Galasa system administrator."),
    ;

    private String template;
    private int expectedParameterCount;
    private Errors(int ordinal, String template ) {
        this.template = template ;
        this.expectedParameterCount = this.template.split("[{]").length-1;
    }

    public String getMessage() {
        String msg ;
        int actualParameterCount = 0;

        if (actualParameterCount!= this.expectedParameterCount) {
            msg = getWrongNumberOfParametersErrorMessage(actualParameterCount,expectedParameterCount);
        } else {
            msg = this.template;
        }

        return msg;
    }

    public String getMessage(Object o1) {

        String msg ;
        int actualParameterCount = 1;

        if (actualParameterCount!= this.expectedParameterCount) {
            msg = getWrongNumberOfParametersErrorMessage(actualParameterCount,expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1);
        }

        return msg;
    }

    public String getMessage(Object o1, Object o2) {

        String msg ;
        int actualParameterCount = 2;

        if (actualParameterCount!= this.expectedParameterCount) {
            msg = getWrongNumberOfParametersErrorMessage(actualParameterCount,expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1,o2);
        }

        return msg;
    }

    public String getMessage(Object o1, Object o2, Object o3) {

        String msg ;
        int actualParameterCount = 3;

        if (actualParameterCount!= this.expectedParameterCount) {
            msg = getWrongNumberOfParametersErrorMessage(actualParameterCount,expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1,o2,o3);
        }

        return msg;
    }

    private String getWrongNumberOfParametersErrorMessage(int actualParameterCount,int expectedParameterCount) {
        String template = "Failed to render message template. Not the expected number of parameters. Got ''{0}''. Expected ''{1}''";
        String msg = MessageFormat.format(template,actualParameterCount, this.expectedParameterCount);
        return msg ;
    }
}
