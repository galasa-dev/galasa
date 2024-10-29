/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common;

import java.text.MessageFormat;

public enum Errors {

    // Common errors
    ERROR_URI_DOESNT_START_WITH_EXPECTED_SCHEME                      (7000,"GAL7000E: URL of ''{0}'' is invalid. It does not start with ''{1}''"),
    ERROR_URI_IS_INVALID                                             (7001,"GAL7001E: URL of ''{0}'' is invalid. {1}"),

    // Generic CouchDB errors
    ERROR_FAILED_TO_ACCESS_COUCHDB_SERVER                            (6000,"GAL6000E: Internal server error. Failed to access CouchDB server. Status code {1} from CouchDB server is not 200. The CouchDB server could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_INVALID_COUCHDB_WELCOME_RESPONSE                           (6001,"GAL6001E: Internal server error. Invalid CouchDB Welcome message returned from the CouchDB server. The CouchDB server could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_FAILED_TO_VALIDATE_COUCHDB_SERVER                          (6002,"GAL6002E: Internal server error. Failed to validate CouchDB server configuration. Cause: {0}"),
    ERROR_FAILED_TO_VALIDATE_COUCHDB_DATABASE                        (6003,"GAL6003E: Internal server error. Failed to determine whether the ''{0}'' database exists. Status code {1} from CouchDB server is not 200. The CouchDB server could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_FAILED_TO_CREATE_COUCHDB_DATABASE                          (6004,"GAL6004E: Internal server error. Failed to create CouchDB database ''{0}''. Status code {1} from CouchDB server is not 201. The CouchDB server could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_OUTDATED_COUCHDB_VERSION                                   (6005,"GAL6005E: Outdated CouchDB server version ''{0}'' detected. Expected version ''{1}'' or above. Report the problem to your Galasa Ecosystem owner."),
    ERROR_FAILED_TO_CREATE_COUCHDB_DOCUMENT                          (6006,"GAL6006E: Internal server error. Failed to create new document in the CouchDB database ''{0}''. The CouchDB server could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_UNEXPECTED_COUCHDB_HTTP_RESPONSE                           (6007,"GAL6007E: Internal server error. Unexpected response received from CouchDB server after sending a HTTP request to ''{0}''. Expected status code(s) [{1}] but received {2}. The CouchDB server could be experiencing temporary issues. Report the problem to your Galasa Ecosystem owner."),
    ERROR_FAILURE_OCCURRED_WHEN_CONTACTING_COUCHDB                   (6008,"GAL6008E: Internal server error. Unexpected failure occurred during HTTP request to CouchDB server at URL ''{0}''. Cause: ''{1}''"),
    ERROR_FAILED_TO_GET_DOCUMENTS_FROM_DATABASE                      (6009,"GAL6009E: Internal server error. Failed to get all documents in the ''{0}'' database. Invalid JSON response returned from CouchDB. CouchDB could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_INVALID_COUCHDB_VERSION_FORMAT                             (6010,"GAL6010E: Invalid CouchDB server version format detected. The CouchDB version ''{0}'' must be in the semantic versioning format (e.g. major.minor.patch). Expected version ''{1}'' or above. Report the problem to your Galasa Ecosystem owner."),
    ERROR_FAILED_TO_GET_DOCUMENT_FROM_DATABASE                       (6011,"GAL6011E: Internal server error. Failed to get document with ID ''{0}'' from the ''{1}'' database. Invalid JSON response returned from CouchDB. CouchDB could be experiencing temporary issues or is not correctly configured. Report the problem to your Galasa Ecosystem owner."),
    ERROR_UNEXPECTED_RESPONSE_FROM_CREATE_DOCUMENT                   (6012,"GAL6012E: Internal server error. Unable to store the artifacts document - the JSON response received does not match the expected format."),

    // CouchDB Auth Store errors
    ERROR_GALASA_AUTH_STORE_SHUTDOWN_FAILED                          (6100,"GAL6100E: Failed to shut down Galasa CouchDB auth store. Cause: {0}"),
    ERROR_FAILED_TO_RETRIEVE_TOKENS                                  (6101,"GAL6101E: Failed to get auth tokens from the CouchDB auth store. Cause: {0}"),
    ERROR_FAILED_TO_CREATE_TOKEN_DOCUMENT                            (6102,"GAL6102E: Failed to store auth token in the CouchDB tokens database. Cause: {0}"),
    ERROR_FAILED_TO_INITIALISE_AUTH_STORE                            (6103,"GAL6103E: Failed to initialise the Galasa CouchDB auth store. Cause: {0}"),
    ERROR_FAILED_TO_DELETE_TOKEN_DOCUMENT                            (6104,"GAL6104E: Failed to delete auth token from the CouchDB tokens database. Cause: {0}"),

    ERROR_FAILED_TO_CREATE_USER_DOCUMENT                                (6201,"GAL6201E: Failed to store users token in the CouchDB users database. Cause: {0}"),
    ERROR_FAILED_TO_RETRIEVE_USERS                                      (6202,"GAL6202E: Failed to get user documents from the CouchDB users store. Cause: {0}"),
    ERROR_FAILED_TO_UPDATE_USER_DOCUMENT                                (6203,"GAL6203E: Failed to update user document in the CouchDB users store. Cause: {0}"),
    ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INVALID_RESP                   (6204,"GAL6204E: Failed to update user document in the CouchDB users store. Cause: Couchdb returned an unexpected json response with no _rev or _id field."),
    ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_MISMATCH_DOC_ID                (6205,"GAL6205E: Failed to update user document in the CouchDB users store. Cause: Couchdb returned a document with an unexpected _id field."),
    ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INPUT_INVALID_NULL_USER_NUMBER (6206,"GAL6206E: Failed to update user document in the CouchDB users store. Cause: Bad input. User number is invalid or null."),
    ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INPUT_INVALID_NULL_USER_VERSION(6207,"GAL6207E: Failed to update user document in the CouchDB users store. Cause: Bad input. User document version is invalid or null."),

    // REST CPS errors
    ERROR_GALASA_WRONG_NUMBER_OF_PARAMETERS_IN_MESSAGE                 (6999,"GAL6999E: Failed to render message template. Not the expected number of parameters. Got ''{0}''. Expected ''{1}''"),
    ERROR_GALASA_CONSTRUCTED_URL_TO_REMOTE_CPS_INVALID_SYNTAX          (7002,"GAL7002E: URL ''{0}'' is of an invalid syntax. {1}"),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_FAILED_NON_OK_STATUS    (7003,"GAL7003E: Could not get the CPS property value from URL ''{0}''. Status code ''{1}'' is not 200."),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_FAILED                  (7004,"GAL7004E: Could not get the CPS property value from URL ''{0}''. Cause: {1}"),
    ERROR_GALASA_CANT_GET_JWT_TOKEN                                    (7005,"GAL7005E: Could not find the GALASA_JWT in the available configuration."),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_BAD_JSON_RETURNED       (7006,"GAL7006E: Could not get the CPS property value from URL ''{0}''. Cause: Bad json returned from the server. {1}"),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTY_TOO_FEW_OR_MANY_RETURNED(7007,"GAL7007E: Could not get the CPS property value from URL ''{0}''. Unexpected number of results. Expected {1} got {2}"),
    ERROR_GALASA_CPS_SET_OPERATIONS_NOT_PERMITTED                      (7008,"GAL7008E: Local test runs are not permitted to set CPS properties on the remote Galasa server."),
    ERROR_GALASA_CPS_DELETE_OPERATIONS_NOT_PERMITTED                   (7009,"GAL7009E: Local test runs are not permitted to delete CPS properties on the remote Galasa server."),
    ERROR_GALASA_CPS_SHUTDOWN_FAILED                                   (7010,"GAL7010E: Galasa internal error. Rest-based CPS store failed to shut down. Cause: {0}"),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTIES_FAILED_NON_OK_STATUS  (7011,"GAL7011E: Could not get the CPS property values from URL ''{0}''. Status code ''{1}'' is not 200."),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_PROPERTIES_FAILED                (7012,"GAL7012E: Could not get the CPS property value from URL ''{0}''. Cause: {1}"),
    ERROR_GALASA_REST_CALL_TO_GET_ALL_CPS_PROPERTIES_NON_OK_STATUS     (7013,"GAL7013E: Could not get all the CPS property values from URL ''{0}''. Status code ''{1}'' is not 200."),
    ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_METADATA         (7014,"GAL7014E: Galasa server error. Payload returned is not what we expected. Metadata field is missing from a property definition."),
    ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_NAMESPACE        (7015,"GAL7015E: Galasa server error. Payload returned is not what we expected. Metadata.namespsace field is missing from a property definition."),
    ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_NAME             (7016,"GAL7016E: Galasa server error. Payload returned is not what we expected. Metadata.name field is missing from a property definition."),
    ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_DATA             (7017,"GAL7017E: Galasa server error. Payload returned is not what we expected. Data field is missing from a property definition."),
    ERROR_GALASA_CPS_PROPERTIES_RETURNED_BADLY_FORMED_VALUE            (7018,"GAL7018E: Galasa server error. Payload returned is not what we expected. Data.value field is missing from a property definition."),
    ERROR_GALASA_REST_CALL_TO_GET_ALL_CPS_NAMESPACES_NON_OK_STATUS     (7019,"GAL7019E: Could not get the namespace information from URL ''{0}''. Status code ''{1}'' is not 200."),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_NAMESPACES_FAILED                (7020,"GAL7020E: Could not get the CPS namespaces information from URL ''{0}''. Cause: {1}"),
    ERROR_GALASA_REST_CALL_TO_GET_CPS_NAMESPACES_BAD_JSON_RETURNED     (7021,"GAL7021E: Could not get the CPS namespaces value from URL ''{0}''. Cause: Bad json returned from the server. {1}"),
                    
    ERROR_GALASA_COUCHDB_UPDATED_FAILED_AFTER_RETRIES                  (7022,"GAL7022E: Couchdb operation failed after {0} attempts, due to conflicts."),
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
            msg = ERROR_GALASA_WRONG_NUMBER_OF_PARAMETERS_IN_MESSAGE.getMessage(
                actualParameterCount, 
                this.expectedParameterCount);
        } else {
            msg = this.template;
        }

        return msg;
    }

    public String getMessage(Object o1) {

        String msg ;
        int actualParameterCount = 1;

        if (actualParameterCount!= this.expectedParameterCount) {
            
            msg = ERROR_GALASA_WRONG_NUMBER_OF_PARAMETERS_IN_MESSAGE.getMessage(
                actualParameterCount, 
                this.expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1);
        }

        return msg;
    }

    public String getMessage(Object o1, Object o2) {

        String msg ;
        int actualParameterCount = 2;

        if (actualParameterCount!= this.expectedParameterCount) {
            template = "Failed to render message template. Not the expected number of parameters. Got ''{0}''. Expected ''{1}''";
            msg = MessageFormat.format(template,actualParameterCount, this.expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1,o2);
        }

        return msg;
    }

    public String getMessage(Object o1, Object o2, Object o3) {

        String msg ;
        int actualParameterCount = 3;

        if (actualParameterCount!= this.expectedParameterCount) {
            template = "Failed to render message template. Not the expected number of parameters. Got ''{0}''. Expected ''{1}''";
            msg = MessageFormat.format(template,actualParameterCount, this.expectedParameterCount);
        } else {
            msg = MessageFormat.format(this.template,o1,o2,o3);
        }

        return msg;
    }
}
