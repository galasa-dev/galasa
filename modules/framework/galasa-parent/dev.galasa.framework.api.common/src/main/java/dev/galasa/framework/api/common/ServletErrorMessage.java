/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

public enum ServletErrorMessage {

    // RunQuery...
    GAL5001_INVALID_DATE_TIME_FIELD                   (5001,"E: Error parsing the date-time field ''{0}'' in the request URL. Invalid value ''{1}''. Expecting a java LocalDateTime format. For example: ''2023-04-11T09:42:06.589180Z''"),
    GAL5002_INVALID_RUN_ID                            (5002,"E: Error retrieving ras run from identifier ''{0}''."),
    GAL5003_ERROR_RETRIEVING_RUNS                     (5003,"E: Error retrieving runs. Report the problem to your Galasa Ecosystem owner."),
    GAL5004_ERROR_RETRIEVING_PAGE                     (5004,"E: Error retrieving page. Report the problem to your Galasa Ecosystem owner."),
    GAL5005_INVALID_QUERY_PARAM_NOT_INTEGER           (5005,"E: Error parsing the query parameter ''{0}'' in the request URL. Invalid value ''{1}''. Expecting an integer."),
    GAL5006_INVALID_QUERY_PARAM_DUPLICATES            (5006,"E: Error parsing the query parameters. Duplicate instances of query parameter ''{0}'' found in the request URL. Expecting only one."),
    GAL5090_INVALID_QUERY_PARAM_NOT_BOOLEAN           (5090,"E: Error parsing the query parameter ''{0}'' in the request URL. Invalid value ''{1}''. Expecting a boolean."),

    GAL5010_FROM_DATE_IS_REQUIRED                     (5010,"E: Error parsing the query parameters. 'from' time is a mandatory field if no 'runname', 'group', or 'submissionId' is supplied."),
    GAL5011_SORT_VALUE_NOT_RECOGNIZED                 (5011,"E: Error parsing the query parameters. 'sort' value ''{0}'' not recognised. Expected query parameter in the format 'sort={fieldName}:{order}' where order is 'asc' for ascending or 'desc' for descending."),
    GAL5012_SORT_VALUE_MISSING                        (5012,"E: Error parsing the query parameters. 'sort' value was not supplied. Expected query parameter in the format 'sort={fieldName}:{order}' where order is 'asc' for ascending or 'desc' for descending."),
    GAL5013_RESULT_NAME_NOT_RECOGNIZED                (5013,"E: Error parsing the query parameters. 'result' value ''{0}'' not recognised. Expected result name to match one of the following ''{1}''."),
    GAL5014_STATUS_NAME_NOT_RECOGNIZED                (5014,"E: Error parsing the query parameters. 'status' value ''{0}'' not recognised. Expected status name to match one of the following ''{1}''."),

    // RunsReset/Cancel...
    GAL5045_INVALID_STATUS_UPDATE_REQUEST             (5045, "E: Error occurred. The field ''status'' in the request body is invalid. The ''status'' value ''{0}'' supplied is not supported. Supported values are: ''queued'' and ''finished''."),
    GAL5046_UNABLE_TO_CANCEL_RUN_INVALID_RESULT       (5046, "E: Error occurred when trying to cancel the run ''{0}''. The ''result'' ''{1}''' supplied is not supported. Supported values are: ''cancelled''."),
    GAL5047_UNABLE_TO_RESET_RUN                       (5047, "E: Error occurred when trying to reset the run ''{0}''. Report the problem to your Galasa Ecosystem owner."),
    GAL5048_UNABLE_TO_CANCEL_RUN                      (5048, "E: Error occurred when trying to cancel the run ''{0}''. Report the problem to your Galasa Ecosystem owner."),
    GAL5049_UNABLE_TO_RESET_COMPLETED_RUN             (5049, "E: Error occurred when trying to reset the run ''{0}''. The run has already completed."),
    GAL5050_UNABLE_TO_CANCEL_COMPLETED_RUN            (5050, "E: Error occurred when trying to cancel the run ''{0}''. The run has already completed."),

    // RunArtifactsList...
    GAL5007_ERROR_RETRIEVING_ARTIFACTS_LIST           (5007,"E: Error retrieving artifacts for run with identifier ''{0}''."),

    // RunArtifactsDownload...
    GAL5008_ERROR_LOCATING_ARTIFACT                   (5008,"E: Error locating artifact ''{0}'' for run with identifier ''{1}''."),
    GAL5009_ERROR_RETRIEVING_ARTIFACT                 (5009,"E: Error retrieving artifact ''{0}'' for run with identifier ''{1}''."),

    // RunDelete...
    GAL5091_ERROR_RUN_NOT_FOUND_BY_ID                 (5091,"E: Error occurred when seaching for a run with identifier ''{0}''."),

    // GenericErrors...
    GAL5000_GENERIC_API_ERROR                         (5000,"E: Error occurred when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."),
    GAL5400_BAD_REQUEST                               (5400,"E: Error occurred when trying to execute request ''{0}''. Check your request parameters or report the problem to your Galasa Ecosystem owner."),
    GAL5401_UNAUTHORIZED                              (5401,"E: Unauthorized. Please ensure you have provided a valid 'Authorization' header with a valid bearer token and try again."),
    GAL5404_UNRESOLVED_ENDPOINT_ERROR                 (5404,"E: Error occurred when trying to identify the endpoint ''{0}''. Check your endpoint URL or report the problem to your Galasa Ecosystem owner."),
    GAL5405_METHOD_NOT_ALLOWED                        (5405,"E: Error occurred when trying to access the endpoint ''{0}''. The method ''{1}'' is not allowed."),
    GAL5406_UNSUPPORTED_CONTENT_TYPE_REQUESTED        (5406, "E: Unsupported ''Accept'' header value set. Supported response types are: [{0}]. Ensure the ''Accept'' header in your request contains a valid value and try again"),
    GAL5411_NO_REQUEST_BODY                           (5411,"E: Error occurred when trying to access the endpoint ''{0}''. The request body is empty."),
    GAL5412_UNSUPPORTED_QUERY_PARAMETERS              (5412,"E: One or more query parameters sent to the Galasa service are not supported ({0}). Supported query parameters are: {1}"),

    //CPS Namespaces...
    GAL5015_INTERNAL_CPS_ERROR                        (5015,"E: Error occurred when trying to access the Configuration Property Store. Report the problem to your Galasa Ecosystem owner."),
    GAL5016_INVALID_NAMESPACE_ERROR                   (5016,"E: Error occurred when trying to access namespace ''{0}''. The namespace provided is invalid."),
    GAL5017_PROPERTY_DOES_NOT_EXIST_ERROR             (5017,"E: Error occurred when trying to access property ''{0}''. The property does not exist."),
    GAL5018_PROPERTY_ALREADY_EXISTS_ERROR             (5018,"E: Error occurred when trying to access property ''{0}''. The property name provided already exists in the ''{1}'' namespace."),
    GAL5028_PROPERTY_NAMESPACE_DOES_NOT_MATCH_ERROR   (5028,"E: The GalasaProperty namespace ''{0}'' must match the url namespace ''{1}''."),
    GAL5029_PROPERTY_NAME_DOES_NOT_MATCH_ERROR        (5029,"E: The GalasaProperty name ''{0}'' must match the url namespace ''{1}''."),
    GAL5030_UNABLE_TO_DELETE_PROPERTY_ERROR           (5030,"E: Error occurred when trying to delete Property ''{0}''. Report the problem to your Galasa Ecosystem owner."),

    //Schedule Runs...
    GAL5019_UNABLE_TO_RETRIEVE_RUNS                   (5019, "E: Unable to retrieve runs for Run Group: ''{0}''."),
    GAL5020_UNABLE_TO_CONVERT_TO_SCHEDULE_REQUEST     (5020, "E: Error occurred when trying to translate the payload into a run."),
    GAL5021_UNABLE_TO_SUBMIT_RUNS                     (5021, "E: Error occurred when trying to submit run ''{0}''."),
    GAL5022_UNABLE_TO_PARSE_SHARED_ENVIRONMENT_PHASE  (5022, "E: Error occurred trying parse the sharedEnvironmentPhase ''{0}''. Valid options are 'BUILD', 'DISCARD'."),

    //Galasa Property...
    GAL5023_UNABLE_TO_CAST_TO_GALASAPROPERTY          (5023, "E: Error occurred trying to interpret resource ''{0}''. This could indicate a mis-match between client and server levels. Check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program."),
    GAL5024_INVALID_GALASAPROPERTY                    (5024, "E: Error occurred because the Galasa Property is invalid. ''{0}''"),
    GAL5415_INVALID_GALASAPROPERTY_EMPTY_METADATA     (5415, "E: Error occurred because the Galasa Property is invalid. The 'metadata' field cannot be empty. The fields 'name' and 'namespace' are mandatory for the type GalasaProperty."),
    GAL5416_INVALID_GALASAPROPERTY_NULL_VALUE         (5416, "E: Error occurred because the Galasa Property is invalid. The 'value' field cannot be null. The field 'value' is mandatory for the type GalasaProperty."),
    GAL5417_INVALID_GALASAPROPERTY_DATA_FIELD_MISSING (5417, "E: Error occurred because the Galasa Property is invalid. The 'data' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty."),

    GAL5031_EMPTY_NAMESPACE                           (5031, "E: Invalid namespace. Namespace is empty."),
    GAL5032_INVALID_FIRST_CHARACTER_NAMESPACE         (5032, "E: Invalid namespace name. ''{0}'' must not start with the ''{1}'' character. Allowable first characters are 'a'-'z' or 'A'-'Z'."),
    GAL5033_INVALID_NAMESPACE_INVALID_MIDDLE_CHAR     (5033, "E: Invalid namespace name. ''{0}'' must not contain the ''{1}'' character. Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9'."),
    GAL5034_INVALID_PREFIX_MISSING_OR_EMPTY           (5034, "E: Invalid property name prefix. Prefix is missing or empty."),
    GAL5035_INVALID_FIRST_CHAR_PROPERTY_NAME_PREFIX   (5035, "E: Invalid property name prefix. ''{0}'' must not start with the ''{1}'' character. Allowable first characters are 'a'-'z' or 'A'-'Z'."),
    GAL5036_INVALID_PROPERTY_NAME_PREFIX_INVALID_CHAR (5036, "E: Invalid property name prefix. ''{0}'' must not contain the ''{1}'' character. Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '.' (dot) , '_' (underscore) and '@' (at)."),
    GAL5037_INVALID_PROPERTY_NAME_SUFFIX_EMPTY        (5037, "E: Invalid property name. Property name is missing or empty."),
    GAL5038_INVALID_PROPERTY_NAME_SUFFIX_FIRST_CHAR   (5038, "E: Invalid property name suffix. ''{0}'' must not start with the ''{1}'' character. Allowable first characters are 'a'-'z' or 'A'-'Z'."),
    GAL5039_INVALID_PROPERTY_NAME_SUFFIX_INVALID_CHAR (5039, "E: Invalid property name suffix. ''{0}'' must not contain the ''{1}'' character. Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '.' (dot) , '_' (underscore) and '@' (at)."),
    GAL5040_INVALID_PROPERTY_NAME_EMPTY               (5040, "E: Invalid property name. Property name is missing or empty."),
    GAL5041_INVALID_PROPERTY_NAME_FIRST_CHAR          (5041, "E: Invalid property name. ''{0}'' must not start with the ''{1}'' character. Allowable first characters are 'a'-'z' or 'A'-'Z'."),
    GAL5042_INVALID_PROPERTY_NAME_INVALID_CHAR        (5042, "E: Invalid property name. ''{0}'' must not contain the ''{1}'' character. Allowable characters after the first character are 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '.' (dot) , '_' (underscore) and '@' (at)."),
    GAL5043_INVALID_PROPERTY_NAME_NO_DOT_SEPARATOR    (5043, "E: Invalid property name. Property name ''{0}'' much have at least two parts separated by a '.' (dot)."),
    GAL5044_INVALID_PROPERTY_NAME_TRAILING_DOT        (5044, "E: Invalid property name. Property name ''{0}'' must not end with a '.' (dot) separator."),

    //Resources APIs...
    GAL5025_UNSUPPORTED_ACTION                        (5025, "E: Error occurred. The field ''action'' in the request body is invalid. Supported actions are: create, apply and update. This could indicate a mis-match between client and server levels.Check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program so that the levels of client and server match."),
    GAL5026_UNSUPPORTED_RESOURCE_TYPE                 (5026, "E: Error occurred. The field ''kind'' in the request body is invalid. This could indicate a mis-match between client and server levels. Check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program so that the levels of client and server match."),
    GAL5027_UNSUPPORTED_API_VERSION                   (5027, "E: Error occurred. The field ''apiVersion'' in the request body is invalid. Currently the ecosystem accepts the ''{0}'' api version. This could indicate a mis-match between client and server levels. Check the level with your Ecosystem administrator. You may have to upgrade/downgrade your client program so that the levels of client and server match."),
    GAL5067_NULL_RESOURCE_IN_BODY                     (5067, "E: Error occurred. A ''NULL'' value is not a valid resource. Check the request format, or check with your Ecosystem administrator."),
    GAL5068_EMPTY_JSON_RESOURCE_IN_BODY               (5068, "E: Error occurred. The JSON element for a resource can not be empty. Check the request format, or check with your Ecosystem administrator."),
    GAL5069_MISSING_REQUIRED_FIELDS                   (5069, "E: Invalid request body provided. The following mandatory fields are missing from the request body: [{0}]. Check that your request body contains these fields and try again."),

    // GalasaSecret validation...
    GAL5070_INVALID_GALASA_SECRET_MISSING_FIELDS      (5070, "E: Invalid GalasaSecret provided. One or more of the following mandatory fields are missing from the ''{0}'' field: [{1}]. Check that your request payload is correct and try again."),
    GAL5072_INVALID_GALASA_SECRET_MISSING_TYPE_DATA   (5072, "E: Invalid GalasaSecret provided. The ''{0}'' type was provided but the following fields are missing from the ''data'' field: [{1}]. Check that your request payload is correct and try again."),
    GAL5073_UNSUPPORTED_GALASA_SECRET_ENCODING        (5073, "E: Unsupported data encoding scheme provided. Supported encoding schemes are: [{0}]. Check that your request payload is correct and try again."),
    GAL5074_UNKNOWN_GALASA_SECRET_TYPE                (5074, "E: Unknown GalasaSecret type provided. Supported GalasaSecret types are: [{0}]. Check that your request payload is correct and try again."),
    GAL5075_ERROR_SECRET_ALREADY_EXISTS               (5075, "E: Error occurred when trying to create a secret with the given name. A secret with the provided name already exists."),
    GAL5076_ERROR_SECRET_DOES_NOT_EXIST               (5076, "E: Error occurred. A secret with the provided name does not exist. Check that your provided secret name is correct and try again."),
    GAL5077_FAILED_TO_SET_SECRET                      (5077, "E: Failed to set a secret with the given ID in the credentials store. The credentials store might be experiencing temporary issues. Report the problem to your Galasa Ecosystem owner."),
    GAL5078_FAILED_TO_DELETE_SECRET                   (5078, "E: Failed to delete a secret with the given ID from the credentials store. The credentials store might be experiencing temporary issues. Report the problem to your Galasa Ecosystem owner."),
    GAL5079_FAILED_TO_GET_SECRET                      (5079, "E: Failed to retrieve the secret with the given ID from the credentials store. A secret with the provided name does not exist and therefore cannot be updated."),

    // Auth APIs...
    GAL5051_INVALID_GALASA_TOKEN_PROVIDED             (5051, "E: Invalid GALASA_TOKEN value provided. Ensure you have set the correct GALASA_TOKEN property for the targeted ecosystem at ''{0}'' and try again."),
    GAL5052_FAILED_TO_RETRIEVE_CLIENT                 (5052, "E: Unable to retrieve client for authentication. Ensure you have set the correct GALASA_TOKEN property for the targeted ecosystem at ''{0}'' and try again."),
    GAL5053_FAILED_TO_RETRIEVE_TOKENS                 (5053, "E: Internal server error occurred when retrieving tokens from the auth store. The auth store could be badly configured or could be experiencing temporary issues. Report the problem to your Galasa Ecosystem owner."),
    GAL5054_FAILED_TO_GET_CONNECTOR_URL               (5054, "E: Internal server error. The REST API server could not get the URL of the authentication provider (e.g. GitHub/LDAP) from the Galasa Dex component. The Dex component of Galasa could be badly configured, or could be experiencing a temporary issue. Report the problem to your Galasa Ecosystem owner."),
    GAL5055_FAILED_TO_GET_TOKENS_FROM_ISSUER          (5055, "E: Failed to get a JWT and a refresh token from the Galasa Dex server. The Dex server did not respond with a JWT and refresh token. This could be because the Dex server is experiencing an outage or other temporary issues. Report the problem to your Galasa Ecosystem owner."),
    GAL5056_FAILED_TO_STORE_TOKEN_IN_AUTH_STORE       (5056, "E: Internal server error occurred when storing the new Galasa token with description ''{0}'' in the auth store. The auth store could be badly configured, or could be experiencing a temporary issue. Report the problem to your Galasa Ecosystem owner."),
    GAL5057_FAILED_TO_RETRIEVE_USERNAME_FROM_JWT      (5057, "E: Unable to retrieve a username from the given JWT. No JWT claim exists in the given JWT that matches the supplied claims: ''{0}''. This could be because the Galasa Ecosystem is badly configured and the chosen authentication provider does not include the expected claims in JWTs. Report the problem to your Galasa Ecosystem owner."),
    GAL5058_NO_USERNAME_JWT_CLAIMS_PROVIDED           (5058, "E: Unable to retrieve a username from the given JWT. No JWT claims to retrieve a username from were provided. This could be because the Galasa Ecosystem is badly configured. Report the problem to your Galasa Ecosystem owner."),
    GAL5059_INVALID_ISSUER_URI_PROVIDED               (5059, "E: Invalid Galasa Dex server URL provided. This could be because the Galasa Ecosystem is badly configured. Report the problem to your Galasa Ecosystem owner."),
    GAL5060_INVALID_OIDC_URI_RECEIVED                 (5060, "E: Internal server error. Invalid OpenID Connect URL received from the Galasa Dex server. Report the problem to your Galasa Ecosystem owner."),
    GAL5061_MISMATCHED_OIDC_URI_RECEIVED              (5061, "E: Internal server error. OpenID Connect URL received from the Galasa Dex server does not match the expected Dex server scheme or host. Report the problem to your Galasa Ecosystem owner."),
    GAL5062_INVALID_TOKEN_REQUEST_BODY                (5062, "E: Invalid request body provided. Ensure that you have provided a client ID and either a refresh token or a authorization code in your request. Allowable characters for these parameters are 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), and '_' (underscore)"),
    GAL5063_FAILED_TO_DELETE_CLIENT                   (5063, "E: Internal server error. Failed to delete Dex client with the given ID. Report the problem to your Galasa Ecosystem owner."),
    GAL5064_FAILED_TO_REVOKE_TOKEN                    (5064, "E: Failed to revoke the token with the given ID. Ensure that you have provided a valid ID representing an existing auth token in your request and try again"),
    GAL5065_FAILED_TO_GET_TOKEN_ID_FROM_URL           (5065, "E: Failed to retrieve a token ID from the request path. Ensure that you have provided a valid ID representing an existing auth token in your request and try again"),
    GAL5066_ERROR_NO_SUCH_TOKEN_EXISTS                (5066, "E: No such token with the given ID exists. Ensure that you have provided a valid ID representing an existing auth token in your request and try again"),
    GAL5127_ERROR_INVALID_LOGINID                     (5127, "E: Invalid login ID provided. This could be because no value was given for the loginId query parameter. Check your provided loginId query parameter value and try again."),

    // OpenAPI Servlet...
    GAL5071_FAILED_TO_PARSE_YAML_INTO_JSON            (5071, "E: Internal server error. Failed to convert OpenAPI specification from YAML into JSON. Report the problem to your Galasa Ecosystem owner"),

    // User APIs...
    GAL5081_INVALID_QUERY_PARAM_VALUE                 (5081, "E: A request to get the user details for a particular user failed. The query parameter provided is not valid. Supported values for the 'loginId' query parameter are : 'me'. This problem is caused by the client program sending a bad request. Report this problem to the owner of your client program."),
    GAL5082_NO_LOGINID_PARAM_PROVIDED                 (5082, "E: A request to get the user details failed. The request did not supply a 'loginId' filter. A 'loginId' query parameter with a value of : 'me' was expected. This problem is caused by the client program sending a bad request. Report this problem to the owner of your client program."),
    GAL5083_ERROR_USER_NOT_FOUND                      (5083, "E: Unable to retrieve a user with the given user number. No such user exists. Check your request query parameters and try again."),
    GAL5084_FAILED_TO_DELETE_USER                     (5084, "E: Failed to delete a user with the given 'loginId' from the auth store. The auth store might be experiencing temporary issues. Report the problem to your Galasa Ecosystem owner."),
    GAL5085_FAILED_TO_GET_LOGIN_ID_FROM_URL           (5085, "E: Failed to retrieve a loginId from the request path. Ensure that you have provided a valid ID representing an existing auth token in your request and try again"),
    GAL5086_FAILED_TO_GET_DEFAULT_ROLE                (5086, "E: Failed to retrieve a default role id for a user with no role set."),
    GAL5087_BAD_USER_UPDATE_FIELD_ROLE                (5087, "E: Server detected an invalid 'role' field value from a client wishing to update a user record. The role field must be less than 128 characters and a consist of alphanumeric characters, '-' (hyphen) or '_' (underscore)"),
    GAL5088_FORBIDDEN_USER_DELETE_THEMSELVES          (5088, "E: It is not permitted for a user to delete their own user record. Ask another Galasa service administrator to delete your user record for you."),
    GAL5089_FORBIDDEN_USER_DELETE_SERVICE_OWNER       (5089, "E: It is not permitted for a user to delete an owner of the Galasa service. Ask a Galasa service administrator to change the list of owners in the kubernetes configuration."),
    GAL5106_FORBIDDEN_USER_UPDATE_SERVICE_OWNER       (5106, "Attempt to set the role of a user to 'owner' using the REST interface.  Operation is not permitted. The list of Galasa service 'owners' can only be changed by a Galasa service administrator by re-configuring the service. Ask a Galasa service administrator to change the list of owners in the Galasa service deployment configuration if you want to allow a user to have the 'owner' role"),

    // Secrets APIs...
    GAL5092_INVALID_SECRET_NAME_PROVIDED              (5092, "E: Invalid secret name provided. The name of a Galasa secret cannot be empty, contain only spaces or tabs, or contain dots ('.'), and must only contain characters in the Latin-1 character set. Check your request payload and try again."),
    GAL5093_ERROR_SECRET_NOT_FOUND                    (5093, "E: Unable to retrieve a secret with the given name. No such secret exists. Check your request query parameters and try again."),
    GAL5094_FAILED_TO_GET_SECRET_FROM_CREDS           (5094, "E: Failed to retrieve a secret with the given name from the credentials store. The credentials store might be badly configured or could be experiencing a temporary issue. Report the problem to your Galasa Ecosystem owner."),
    GAL5095_ERROR_PASSWORD_AND_TOKEN_PROVIDED         (5095, "E: Invalid secret payload provided. The ''password'' and ''token'' fields are mutually exclusive and cannot be provided in the same secret. Check your request payload and try again."),
    GAL5096_ERROR_MISSING_SECRET_VALUE                (5096, "E: Invalid secret payload provided. One or more secret fields in your request payload are missing a ''value''. Check your request payload and try again."),
    GAL5097_FAILED_TO_DECODE_SECRET_VALUE             (5097, "E: Failed to decode a provided secret value. Expected the value to be encoded in ''{0}'' format but it was not. Check your request values are properly encoded and try again."),
    GAL5098_ERROR_PASSWORD_MISSING_USERNAME           (5098, "E: Invalid secret payload provided. A ''password'' field was provided but the ''username'' field was missing. Check your request payload and try again."),
    GAL5099_ERROR_MISSING_REQUIRED_SECRET_FIELD       (5099, "E: Invalid secret payload provided. The ''{0}'' type was provided but the required ''{1}'' field was missing. Check your request payload and try again."),
    GAL5100_ERROR_UNEXPECTED_SECRET_FIELD_PROVIDED    (5100, "E: Invalid secret payload provided. An unexpected field was given to update a ''{0}'' secret. Only the following fields can be provided to update this secret: ''{1}''. Check your request payload and try again."),
    GAL5101_ERROR_UNEXPECTED_SECRET_TYPE_DETECTED     (5101, "E: Unknown secret type detected. A secret retrieved from the credentials store is in an unknown or unsupported format. Report the problem to your Galasa Ecosystem owner."),
    GAL5102_INVALID_SECRET_DESCRIPTION_PROVIDED       (5102, "E: Invalid secret description provided. The description should not only contain spaces or tabs. When provided, it must contain characters in the Latin-1 character set. Report the problem to your Galasa Ecosystem owner."),
    
    // Auth callback API...
    GAL5103_UNEXPECTED_STATE_PARAMETER_PROVIDED       (5103, "E: Unexpected ''state'' query parameter value provided. The provided ''state'' parameter value does not match the stored state identifier or the auth request has timed out. Try to log in again."),
    GAL5104_INVALID_CALLBACK_URL_PROVIDED             (5104, "E: Invalid callback URL provided. The callback URL must be a valid URL. Check your request parameters and try again."),
    GAL5105_INTERNAL_DSS_ERROR                        (5105, "E: Error occurred when trying to access the Dynamic Status Store. Report the problem to your Galasa Ecosystem owner."),


    // RBAC APIs...
    GAL5120_INVALID_ACTION_NAME_PROVIDED              (5120, "E: Invalid action name provided."),
    GAL5121_INVALID_ROLE_ID_PROVIDED                  (5121, "E: Invalid role id provided."),
    GAL5122_ACTION_NAMED_NOT_FOUND                    (5122, "E: Action with that name not found."),
    GAL5123_ROLE_ID_NOT_FOUND                         (5123, "E: Role with that ID not found."),
    GAL5124_ROLE_ID_NOT_FOUND_FOR_USER                (5124, "E: A user has a role which cannot be found in the system. Inconsistent data. Report this issue to your Galasa systems administrator."),
    GAL5125_ACTION_NOT_PERMITTED                      (5125, "E: Insufficient privileges to perform the requested operation. Check with your Galasa systems administrator that you have been assigned the correct role with the ''{0}'' action before trying again."),
    GAL5126_INTERNAL_RBAC_ERROR                       (5126, "E: Error occurred when trying to access the Role Based Access Control service. Report the problem to your Galasa systems administrator."),
    GAL5413_USER_CANNOT_UPDATE_OWN_USER_ROLE          (5413, "E: A user is not allowed to update their own role. Ask a Galasa service administrator to change your role instead."),
    GAL5414_USER_CANNOT_UPDATE_SERVICE_OWNER_ROLE     (5414, "E: A user is not allowed to update the role of the Galasa service owner. Ask a Galasa service administrator to change the list of owners and update the kubernetes configuration of the service."),

    // STREAMS API
    GAL5418_INVALID_STREAM_NAME                       (5418, "E: Invalid 'name' provided. A valid stream name should always start with 'a'-'z' or 'A'-'Z' and end with 'a'-'z', 'A'-'Z' or 0-9."),
    GAL5419_FAILED_TO_GET_STREAM_NAME_FROM_URL        (5419, "E: Failed to retrieve a stream name from the request path. Ensure that you have provided a valid name representing an existing stream in your request and try again"),
    GAL5420_ERROR_STREAM_NOT_FOUND                    (5420, "E: Unable to retrieve a stream with the given stream name. No such stream exists."),

    ;

    // >>>
    // >>> Note: Please keep this up to date, to save us wondering what to allocate next... 
    // >>>       otherwise you have to find a 'gap' in the range.
    // >>>       Unit tests guarantee that this number is 'free' to use for a new error message.
    // >>>       If you do use this number for a new error template, please incriment this value.
    // >>>
    public static final int GALxxx_NEXT_MESSAGE_NUMBER_TO_USE = 5421;


    private String template ;
    private int templateNumber;

    private ServletErrorMessage(int templateNumber , String template) {
        this.template = "GAL"+Integer.toString(templateNumber)+template ;
        this.templateNumber = templateNumber ;
    }

    public String toString() {
        return this.template ;
    }

    public int getTemplateNumber() {
        return this.templateNumber;
    }

}