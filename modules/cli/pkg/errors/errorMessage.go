/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package errors

import (
	"fmt"
	"log"
	"net/http"
	"strconv"
	"strings"
)

var (
	RATE_LIMIT_STATUS_CODES_MAP = map[int]struct{}{
		http.StatusServiceUnavailable: {},
		http.StatusTooManyRequests:    {},
		http.StatusUnauthorized:       {},
	}

	AUTH_STATUS_CODES_MAP = map[int]struct{}{
		http.StatusUnauthorized: {},
	}
)

// The 'type' of a message, used inside Galasa errors
type MessageType struct {
	Template           string
	Ordinal            int
	IsStackTraceWanted bool
}

func NewMessageType(template string, ordinal int, isStackTraceWanted bool) *MessageType {
	messageType := new(MessageType)
	messageType.Ordinal = ordinal
	messageType.Template = template

	// As a sanity check... Make sure that the ordinal declared by the message template
	// is also rendered within the message, and that the numbers are the same...
	if !strings.Contains(template, strconv.Itoa(ordinal)) {
		panic(
			fmt.Sprintf("Programming error: Template does not contain a number the same as the ordinal number. "+
				"Ordinal is %d. Template is %s", ordinal, template))
	}

	messageType.IsStackTraceWanted = isStackTraceWanted

	// Add the error message to the list of all messages.
	// (So we can render documentation for each message.)
	GALASA_ALL_MESSAGES[ordinal] = messageType

	return messageType
}

// NewMessageTypeFromError creates a basic message type that includes an error message as its template
func NewMessageTypeFromError(err error) *MessageType {
	messageType := new(MessageType)
	messageType.Template = err.Error()
	return messageType
}

// A Galasa errror instance.
// It can be treated as a normal error, but also holds a message-type
// So we can programmatically tell the difference between various errors as required.
type GalasaError struct {
	msgType    *MessageType
	message    string
	httpStatus int
	cause      error
}

type GalasaCommsError interface {
	IsRateLimitedRetryRequired() bool
	IsReauthRequired() bool
}

func (err *GalasaError) GetMessageType() *MessageType {
	return err.msgType
}

func (err *GalasaError) GetHttpStatusCode() int {
	return err.httpStatus
}

func (err *GalasaError) GetCause() error {
	return err.cause
}

// NewGalasaError creates a new GalasaError structure.
//
// params - are substituted into the message indicated by the message type.
func NewGalasaError(msgType *MessageType, params ...interface{}) *GalasaError {
	template := msgType.Template
	message := fmt.Sprintf(template, params...) // how to do this with variadic variable ?
	var galasaError *GalasaError = new(GalasaError)
	galasaError.msgType = msgType
	galasaError.message = message

	log.Println(message)
	if galasaError.msgType.IsStackTraceWanted {
		LogStackTrace()
	}

	return galasaError
}

func NewGalasaErrorWithHttpStatusCode(httpStatusCode int, msgType *MessageType, params ...interface{}) *GalasaError {
	err := NewGalasaError(msgType, params...)
	err.httpStatus = httpStatusCode
	return err
}

func NewGalasaErrorWithCause(cause error, msgType *MessageType, params ...interface{}) *GalasaError {
	err := NewGalasaError(msgType, params...)
	err.cause = cause
	return err
}

func GetGalasaErrorFromCommsResponse(httpResponse *http.Response, possibleCommsError error) error {
	err := possibleCommsError
	var statusCode int
	if httpResponse != nil {
		defer httpResponse.Body.Close()
		statusCode = httpResponse.StatusCode
	}

	if err != nil {
		err = NewGalasaErrorWithHttpStatusCode(statusCode, NewMessageTypeFromError(err))
	}
	return err
}

// Render a galasa error into a string, so the GalasaError structure can be used
// as a normal error.
func (err *GalasaError) Error() string {
	return err.message
}

func (err *GalasaError) IsRateLimitedRetryRequired() bool {
	isRetryRequired := isRootErrorStatusCodeInMap(err, RATE_LIMIT_STATUS_CODES_MAP)
	return isRetryRequired
}

func (err *GalasaError) IsReauthRequired() bool {
	isReauthRequired := isRootErrorStatusCodeInMap(err, AUTH_STATUS_CODES_MAP)
	return isReauthRequired
}

func isRootErrorStatusCodeInMap(err *GalasaError, errorCodeMap map[int]struct{}) bool {
	var statusCode int
	currentError := err
	isGalasaError := true

	// Unwrap all Galasa errors to get the original error
	for isGalasaError && currentError.GetCause() != nil {
		currentError, isGalasaError = currentError.GetCause().(*GalasaError)
	}

	// Pull the HTTP status code out of the original error, which could be 0 if the error is client-side
	if isGalasaError && currentError != nil {
		statusCode = currentError.GetHttpStatusCode()
	}

	_, isStatusCodeInMap := errorCodeMap[statusCode]

	return isStatusCodeInMap
}

const (
	STACK_TRACE_WANTED     = true
	STACK_TRACE_NOT_WANTED = false
)

var (
	// A map of all the messages. Indexed by ordinal number.
	GALASA_ALL_MESSAGES = make(map[int]*MessageType)

	SEE_COMMAND_REFERENCE = " Use the --help flag for more information, or refer to the documentation at https://galasa.dev/docs/reference/cli-commands."

	GALASA_ERROR_UNSUPPORTED_BOOTSTRAP_URL                = NewMessageType("GAL1001E: Unsupported bootstrap URL %s. Acceptable values start with 'http' or 'https'."+SEE_COMMAND_REFERENCE, 1001, STACK_TRACE_WANTED)
	GALASA_ERROR_BOOTSTRAP_URL_BAD_ENDING                 = NewMessageType("GAL1002E: Bootstrap url does not end in '/bootstrap'. Bootstrap url is '%s'."+SEE_COMMAND_REFERENCE, 1002, STACK_TRACE_WANTED)
	GALASA_ERROR_BAD_BOOTSTRAP_CONTENT                    = NewMessageType("GAL1003E: Bootstrap contents is badly formed. Bootstrap is at %s. Reason is: %s", 1003, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_GET_BOOTSTRAP                  = NewMessageType("GAL1004E: Failed to load the bootstrap from %s. Reason is %s. If the URL is not resolving, try adding the hostname to your /etc/hosts file. This might especially be needed if communicating over a VPN connection.", 1004, STACK_TRACE_WANTED)
	GALASA_ERROR_THROTTLE_FILE_WRITE                      = NewMessageType("GAL1005E: Failed to write to 'throttle' file %v. Reason is %s", 1005, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_MIX_FLAGS_AND_PORTFOLIO           = NewMessageType("GAL1006E: The submit command does not support mixing of the test selection flags and a portfolio."+SEE_COMMAND_REFERENCE, 1006, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_CREATE_REPORT_YAML                = NewMessageType("GAL1007E: Failed to create report yaml file %s. Reason is %s", 1007, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_RUNS_GROUP_CHECK                  = NewMessageType("GAL1008E: Failed to check if group %s exists already. Reason is %s", 1008, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_MISSING_ACTION_FLAGS              = NewMessageType("GAL1009E: The submit command requires either test selection flags (eg: --stream, --class, --bundle, --package, --tag, --regex, --test, --gherkin) or --portfolio flag to be specified."+SEE_COMMAND_REFERENCE, 1009, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_INVALID_OVERRIDE                  = NewMessageType("GAL1010E: Invalid override '%v'", 1010, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_REPORT_YAML_ENCODE                = NewMessageType("GAL1011E: Failed to encode the yaml file %s. Reason is %s", 1011, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_REPORT_JSON_MARSHAL               = NewMessageType("GAL1012E: Failed to prepare test report for writing to json file %s. Reason is %s", 1012, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_REPORT_JSON_WRITE_FAIL            = NewMessageType("GAL1013E: Failed to write test report json file %s. Reason is %s", 1013, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_REPORT_JUNIT_PREPARE              = NewMessageType("GAL1014E: Failed to prepare test report for writing to the junit results file %s. Reason is %s", 1014, STACK_TRACE_WANTED)
	GALASA_ERROR_SUBMIT_REPORT_JUNIT_WRITE_FAIL           = NewMessageType("GAL1015E: Failed to  write test report junit results file %s. Reason is %s", 1015, STACK_TRACE_WANTED)
	GALASA_ERROR_EMPTY_PORTFOLIO                          = NewMessageType("GAL1016E: There are no tests in the test porfolio %s", 1016, STACK_TRACE_WANTED)
	GALASA_ERROR_TESTS_FAILED                             = NewMessageType("GAL1017E: Not all runs passed. %v failed.", 1017, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NO_TESTS_SELECTED                        = NewMessageType("GAL1018E: No tests were selected.", 1018, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_PREPARE_INVALID_OVERRIDE                 = NewMessageType("GAL1019E: Invalid override '%v'", 1019, STACK_TRACE_WANTED)
	GALASA_ERROR_OPEN_LOG_FILE_FAILED                     = NewMessageType("GAL1020E: Failed to open log file '%s' for writing. Reason is %s", 1020, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_OPEN_PORTFOLIO_FILE_FAILED               = NewMessageType("GAL1021E: Failed to open portfolio file '%s' for reading. Reason is %s", 1021, STACK_TRACE_WANTED)
	GALASA_ERROR_PORTFOLIO_BAD_FORMAT                     = NewMessageType("GAL1022E: Failed to read portfolio file '%s' because the content is in the wrong format. Reason is %s", 1022, STACK_TRACE_WANTED)
	GALASA_ERROR_PORTFOLIO_BAD_FORMAT_VERSION             = NewMessageType("GAL1023E: Failed to read portfolio file '%s' because the content are not using format '%s'.", 1023, STACK_TRACE_WANTED)
	GALASA_ERROR_PORTFOLIO_BAD_RESOURCE_KIND              = NewMessageType("GAL1024E: Failed to read portfolio file '%s' because the content are not a resource of type '%s'.", 1024, STACK_TRACE_WANTED)
	GALASA_ERROR_CATALOG_NOT_FOUND                        = NewMessageType("GAL1025E: Unable to locate test stream '%s' catalog location", 1025, STACK_TRACE_WANTED)
	GALASA_ERROR_PROPERTY_GET_FAILED                      = NewMessageType("GAL1026E: Failed to find location of tests in stream '%s'. Reason is %s", 1026, STACK_TRACE_WANTED)
	GALASA_ERROR_CATALOG_COPY_FAILED                      = NewMessageType("GAL1027E: Failed to copy test catalog from REST reply for property '%s', stream '%s'. Reason is %s", 1027, STACK_TRACE_WANTED)
	GALASA_ERROR_CATALOG_UNMARSHAL_FAILED                 = NewMessageType("GAL1028E: Failed to unmarshal test catalog from REST reply for property '%s', stream '%s'. Reason is %s", 1028, STACK_TRACE_WANTED)
	GALASA_ERROR_NO_STREAMS_CONFIGURED                    = NewMessageType("GAL1029E: Stream '%s' is not found in the ecosystem. There are no streams set up. Ask your Galasa system administrator to add a new stream with the desired name.", 1029, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_STREAM                           = NewMessageType("GAL1030E: Stream '%s' is not found in the ecosystem. Valid streams are:%s. Try again using a valid stream, or ask your Galasa system administrator to add a new stream with the desired name.", 1030, STACK_TRACE_WANTED)
	GALASA_ERROR_STREAM_FLAG_REQUIRED                     = NewMessageType("GAL1031E: Invalid flags. --bundle, --package, --test, --tag, and --class flags can only be specified if --stream is provided."+SEE_COMMAND_REFERENCE, 1031, STACK_TRACE_WANTED)
	GALASA_ERROR_SELECTION_REGEX_ERROR                    = NewMessageType("GAL1032E: Invalid select regex '%v'. Reason is %v", 1032, STACK_TRACE_WANTED)
	GALASA_ERROR_SELECTION_REGEX_QUOTED_ERROR             = NewMessageType("GAL1033E: Invalid select quoted regex '%v'. Reason is %v", 1033, STACK_TRACE_WANTED)
	GALASA_ERROR_CLASS_FORMAT                             = NewMessageType("GAL1034E: Class '%v' is not format 'bundle/class'", 1034, STACK_TRACE_WANTED)
	GALASA_ERROR_CLASS_NAME_BLANK                         = NewMessageType("GAL1035E: Class '%v' is not format. Name is blank", 1035, STACK_TRACE_WANTED)
	GALASA_ERROR_CANNOT_OVERWRITE_FILE                    = NewMessageType("GAL1036E: File '%s' exists. Use the --force flag to overwrite it.", 1036, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_CHAR_IN_PACKAGE_NAME             = NewMessageType("GAL1037E: Invalid Java package name '%s' should not contain the '%s' character."+SEE_COMMAND_REFERENCE, 1037, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_FIRST_CHAR_IN_PKG_NAME           = NewMessageType("GAL1038E: Invalid Java package name '%s' should not start with the '%s' character."+SEE_COMMAND_REFERENCE, 1038, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_LAST_CHAR_IN_PKG_NAME            = NewMessageType("GAL1039E: Invalid Java package name '%s' should not end with the '%s' character."+SEE_COMMAND_REFERENCE, 1039, STACK_TRACE_WANTED)
	GALASA_ERROR_PACKAGE_NAME_BLANK                       = NewMessageType("GAL1040E: Invalid Java package name. Package name should not be blank."+SEE_COMMAND_REFERENCE, 1040, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_CREATE_FOLDERS                 = NewMessageType("GAL1041E: Failed to create folders '%s'. Reason is '%s'. Check that you have permissions to write to that folder, and that there is enough disk space available and try again.", 1041, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_WRITE_FILE                     = NewMessageType("GAL1042E: Failed to open file '%s' for writing. Reason is '%s'. Check that you have permissions to write to that folder and file, and that there is enough disk space available and try again.", 1042, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_READ_FILE                      = NewMessageType("GAL1043E: Failed to open file '%s' for reading. Reason is '%s'. Check that you have permissions to read the file and try again.", 1043, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_PKG_RESERVED_WORD                = NewMessageType("GAL1044E: Invalid Java package name. Package name '%s' contains the reserved java keyword '%s'.", 1044, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_FEATURE_NAME                     = NewMessageType("GAL1045E: Invalid feature name. Feature name '%s' cannot be used as a java package name. '%s'", 1045, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_FIND_USER_HOME                 = NewMessageType("GAL1046E: Failed to determine the home folder of this user. '%s'", 1046, STACK_TRACE_WANTED)
	GALASA_ERROR_CREATE_REPORT_YAML_EXISTS                = NewMessageType("GAL1047E: Cannot create the yaml report in file '%s' as that file already exists.", 1047, STACK_TRACE_WANTED)
	GALASA_ERROR_THROTTLE_FILE_READ                       = NewMessageType("GAL1048E: Failed to read from 'throttle' file '%v'. Reason is '%s'", 1048, STACK_TRACE_WANTED)
	GALASA_ERROR_THROTTLE_FILE_INVALID                    = NewMessageType("GAL1049E: Invalid value '%v' read from 'throttle' file '%v'. Reason is '%s'."+SEE_COMMAND_REFERENCE, 1049, STACK_TRACE_WANTED)
	GALASA_ERROR_JAVA_HOME_NOT_SET                        = NewMessageType("GAL1050E: JAVA_HOME environment variable is not set. It must be for when --local flag is used.", 1050, STACK_TRACE_WANTED)
	GALASA_ERROR_JAVA_HOME_BIN_PRESENCE_FAIL              = NewMessageType("GAL1051E: Failed to determine if folder '%s' exists. Reason is '%s'", 1051, STACK_TRACE_WANTED)
	GALASA_ERROR_JAVA_HOME_BIN_MISSING                    = NewMessageType("GAL1052E: Folder '%s' is missing. JAVA_HOME environment variable should refer to a folder which contains a 'bin' folder.", 1052, STACK_TRACE_WANTED)
	GALASA_ERROR_JAVA_PROGRAM_PRESENCE_FAIL               = NewMessageType("GAL1053E: Failed to determine if '%s' exists. Reason is '%s'", 1053, STACK_TRACE_WANTED)
	GALASA_ERROR_JAVA_PROGRAM_MISSING                     = NewMessageType("GAL1054E: Program '%s' should exist. JAVA_HOME has been set incorrectly.", 1054, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_READ_BOOTSTRAP_FILE            = NewMessageType("GAL1055E: Failed to read bootstrap file '%s'. Reason is '%s'", 1055, STACK_TRACE_WANTED)
	GALASA_ERROR_RAS_FOLDER_NOT_DETECTED                  = NewMessageType("GAL1056E: The RAS folder path could not be detected in trace output for runId '%s'", 1056, STACK_TRACE_WANTED)
	GALASA_ERROR_RUN_ID_NOT_DETECTED                      = NewMessageType("GAL1057E: The run identifier could not be detected in trace output of the child process", 1057, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_LOAD_BOOTSTRAP_FILE            = NewMessageType("GAL1058E: Failed to load bootstrap file '%s'. Reason is '%s'", 1058, STACK_TRACE_WANTED)
	GALASA_ERROR_FAILED_TO_LOAD_OVERRIDES_FILE            = NewMessageType("GAL1059E: Failed to load overrides file '%s'. Reason is '%s'", 1059, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_OBR_NOT_ENOUGH_PARTS             = NewMessageType("GAL1060E: Badly formed OBR parameter '%s'. Expected it to be of the form mvn:<GROUP_ID>/<ARTIFACT_ID>/<VERSION>/obr with 4 parts separated by slash characters.", 1060, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_OBR_TOO_MANY_PARTS               = NewMessageType("GAL1061E: Badly formed OBR parameter '%s'. Expected it to be of the form mvn:<GROUP_ID>/<ARTIFACT_ID>/<VERSION>/obr with 4 parts separated by slash characters.", 1061, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_OBR_NO_MVN_PREFIX                = NewMessageType("GAL1062E: Badly formed OBR parameter '%s'. Expected it to be of the form mvn:<GROUP_ID>/<ARTIFACT_ID>/<VERSION>/obr with a 'mvn:' prefix.", 1062, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_OBR_NO_OBR_SUFFIX                = NewMessageType("GAL1063E: Badly formed OBR parameter '%s'. Expected it to be of the form mvn:<GROUP_ID>/<ARTIFACT_ID>/<VERSION>/obr with an '/obr' suffix.", 1063, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_CLASS_INPUT_NO_SLASH             = NewMessageType("GAL1064E: Badly formed Class parameter '%s'. Expected it to be of the form <OSGiBundleId>/<FullyQualifiedJavaClass> with no .class suffix. No slash found.", 1064, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_CLASS_TOO_MANY_SLASHES           = NewMessageType("GAL1065E: Badly formed Class parameter '%s'. Expected it to be of the form <OSGiBundleId>/<FullyQualifiedJavaClass> with no .class suffix. Too many slashes found.", 1065, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_CLASS_SUFFIX_FOUND               = NewMessageType("GAL1066E: Badly formed Class parameter '%s'. Expected it to be of the form <OSGiBundleId>/<FullyQualifiedJavaClass> with no .class suffix. Unwanted .class suffix detected.", 1066, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_OUTPUT_FORMAT                    = NewMessageType("GAL1067E: Unsupported value '%s' for parameter --format. Supported values are: %s."+SEE_COMMAND_REFERENCE, 1067, STACK_TRACE_WANTED)
	GALASA_ERROR_QUERY_RUNS_FAILED                        = NewMessageType("GAL1068E: Could not query run results. Reason: '%s'", 1068, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_LOG_FILE_IS_A_FOLDER                     = NewMessageType("GAL1069E: Could not open log file for writing. '%s' is a directory, the --log parameter should refer to a file path (existing or not), or '-' (the console)", 1069, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BOOTSTRAP_BAD_DEBUG_MODE_VALUE           = NewMessageType("GAL1070E: Invalid value '%s' detected for optional property '%s' in bootstrap properties. Valid values are 'listen' or 'attach'. Only used when --debug flag is set. Defaults to 'listen'. Can be overridden with the --debugMode flag.", 1070, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_ARG_BAD_DEBUG_MODE_VALUE                 = NewMessageType("GAL1071E: Invalid value '%s' detected for optional --debugMode argument. Valid values are 'listen' or 'attach'. Only used when --debug flag is set. Defaults to 'listen'. Default can be set with an optional property '%s' in bootstrap properties.", 1071, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BOOTSTRAP_BAD_DEBUG_PORT_VALUE           = NewMessageType("GAL1072E: Invalid value '%s' detected for optional property '%s' in bootstrap properties. Valid values must be a non-zero positive integer, for a port number. Only used when --debug flag is set. Defaults to '%s'. Can be overridden with the --debugPort flag.", 1072, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_ARTIFACTS_FAILED              = NewMessageType("GAL1073E: Could not get run artifacts. Reason: '%s'", 1073, STACK_TRACE_WANTED)
	GALASA_ERROR_DOWNLOADING_ARTIFACT_FAILED              = NewMessageType("GAL1074E: Could not download artifact '%s'. Reason: '%s'", 1074, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_RUN_NAME                         = NewMessageType("GAL1075E: run name '%s' is invalid. Expected it to be in format starting with letters, and ending in a number with no non-alphanumeric characters.", 1075, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_FROM_OR_TO_PARAMETER             = NewMessageType("GAL1076E: Badly formed from or to value '%s' specified in the age parameter. The value could not be converted into an integer value.", 1076, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FROM_AGE_SMALLER_THAN_TO_AGE             = NewMessageType("GAL1077E: Invalid value '%s' detected for age parameter. The 'from' value must be greater than the 'to' value.", 1077, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_AGE_PARAMETER                    = NewMessageType("GAL1078E: Badly formed '--age' parameter value '%s' specified. Age of the test runs should be specified in the format '{FROM}{TIME-UNIT}:{TO}{TIME-UNIT}' or '{FROM}{TIME-UNIT}', where 'FROM' is a positive, non-zero integer, 'TO' is a non-negative integer, and 'TIME-UNIT' can be %s. 'FROM' must be greater than 'TO'. 'TO' defaults to 0 if not specified."+SEE_COMMAND_REFERENCE, 1078, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NO_TEST_RUN_IDENTIFIER_FLAG_SPECIFIED    = NewMessageType("GAL1079E: The --age, --name, or --group parameter must be used to identify which test run(s) you want see."+SEE_COMMAND_REFERENCE, 1079, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_FROM_AGE_SPECIFIED               = NewMessageType("GAL1080E: Invalid 'from' value '%s' in the '--age' parameter. Age of the test runs should be specified in the format '{FROM}{TIME-UNIT}:{TO}{TIME-UNIT}' or '{FROM}{TIME-UNIT}', where 'FROM' is a positive, non-zero integer, 'TO' is a non-negative integer, and 'TIME-UNIT' can be %s. 'FROM' must be greater than 'TO'. 'TO' defaults to 0 if not specified."+SEE_COMMAND_REFERENCE, 1080, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NEGATIVE_AGE_SPECIFIED                   = NewMessageType("GAL1081E: Unable use a negative value '%s' in the '--age' parameter. Age of the test runs should be specified in the format '{FROM}{TIME-UNIT}:{TO}{TIME-UNIT}' or '{FROM}{TIME-UNIT}', where 'FROM' is a positive, non-zero integer, 'TO' is a non-negative integer, and 'TIME-UNIT' can be %s. 'FROM' must be greater than 'TO'. 'TO' defaults to 0 if not specified."+SEE_COMMAND_REFERENCE, 1081, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BAD_TIME_UNIT_AGE_SPECIFIED              = NewMessageType("GAL1082E: Invalid time unit specified '%s' in the '--age' parameter. Age of the test runs should be specified in the format '{FROM}{TIME-UNIT}:{TO}{TIME-UNIT}' or '{FROM}{TIME-UNIT}', where 'FROM' is a positive, non-zero integer, 'TO' is a non-negative integer, and 'TIME-UNIT' can be %s. 'FROM' must be greater than 'TO'. 'TO' defaults to 0 if not specified."+SEE_COMMAND_REFERENCE, 1082, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NO_ARTIFACTS_TO_DOWNLOAD                 = NewMessageType("GAL1083E: No artifacts to download for run: '%s'", 1083, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_HTTP_RESPONSE_CLOSE_FAILED               = NewMessageType("GAL1084E: Communications failure while closing an HTTP response. '%s'", 1084, STACK_TRACE_WANTED)
	GALASA_ERROR_EMBEDDED_FS_READ_FAILED                  = NewMessageType("GAL1085E: Programming logic error. ReadTextFile operation on embedded file system failed. Reason is %s", 1085, STACK_TRACE_WANTED)
	GALASA_ERROR_QUERY_RESULTNAMES_FAILED                 = NewMessageType("GAL1086E: Communications problem between the command-line tool and the target Galasa Ecosystem. The tool could not retrieve the list of valid result names. Reason: '%s'", 1086, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_RESULT_ARGUMENT                  = NewMessageType("GAL1087E: Invalid '--result' parameter value: %s. The possible result values currently in the Ecosystem Result Archive Store (RAS) are: %s", 1087, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_ACTIVE_AND_RESULT_ARE_MUTUALLY_EXCLUSIVE = NewMessageType("GAL1088E: --active and --result must not be used at the same time, they are mutually exclusive."+SEE_COMMAND_REFERENCE, 1088, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_MAVEN_AND_OR_GRADLE_FLAG_MUST_BE_SET     = NewMessageType("GAL1089E: Need to use --maven and/or --gradle parameter"+SEE_COMMAND_REFERENCE, 1089, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_USERNAME_FAILED               = NewMessageType("GAL1090E: Could not get username of current requestor. Reason is '%s'", 1090, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BAD_BOOTSTRAP_FILE_URL                   = NewMessageType("GAL1091E: '%s' is not a properly formed file URL"+SEE_COMMAND_REFERENCE, 1091, STACK_TRACE_WANTED)
	GALASA_ERROR_TEST_NOT_IN_RUN_GROUP_LOST               = NewMessageType("GAL1092E: Galasa Ecosystem error: A test was submitted for launch. The galasa runtime is not reporting test progress. "+
		"The test is lost and may execute but test progress cannot be monitored from this tool. (bundle: %s, class: %s).", 1092, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_SUBMIT_TEST                   = NewMessageType("GAL1093E: Failed to submit test (bundle: %s, class: %s). Reason is: %s", 1093, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NO_OBR_SPECIFIED_ON_INPUTS              = NewMessageType("GAL1094E: User error: Cannot run test %s on a local JVM because no OBR information is available. Supply an OBR using the --obr parameter, or (if using a portfolio) ensure the portfolio contains an OBR for this test."+SEE_COMMAND_REFERENCE, 1094, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_NAMESPACE                       = NewMessageType("GAL1095E: namespace '%s' is invalid. Expected it to be in a format starting with letters, and ending in a number with no non-alphanumeric characters."+SEE_COMMAND_REFERENCE, 1095, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_QUERY_NAMESPACE_FAILED                  = NewMessageType("GAL1096E: Could not query namespace results. Reason: '%s'", 1096, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_PROPERTIES_FLAG_COMBINATION     = NewMessageType("GAL1097E: Property --name cannot be set alongside --prefix, --suffix, or --infix."+SEE_COMMAND_REFERENCE, 1097, STACK_TRACE_WANTED)
	GALASA_ERROR_PUT_PROPERTY_FAILED                     = NewMessageType("GAL1098E: Property '%v' could not be updated. Reason: '%s'", 1098, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_PROPERTY_FAILED                  = NewMessageType("GAL1099E: Property '%v' could not be deleted. Reason: '%s'", 1099, STACK_TRACE_WANTED)
	GALASA_ERROR_POST_PROPERTY_FAILED                    = NewMessageType("GAL1100E: Property '%v' could not be created. Reason: '%s'", 1100, STACK_TRACE_WANTED)
	GALASA_ERROR_MISSING_NAMESPACE_FLAG                  = NewMessageType("GAL1101E: namespace '%s' is invalid. '--namespace' is a mandatory flag for this command."+SEE_COMMAND_REFERENCE, 1101, STACK_TRACE_WANTED)
	GALASA_ERROR_MISSING_NAME_FLAG                       = NewMessageType("GAL1102E: name '%s' is invalid. '--name' is a mandatory flag for this command."+SEE_COMMAND_REFERENCE, 1102, STACK_TRACE_WANTED)
	GALASA_ERROR_QUERY_CPS_FAILED                        = NewMessageType("GAL1103E: Could not query CPS results. Reason: '%s'", 1103, STACK_TRACE_WANTED)
	GALASA_ERROR_UNABLE_TO_DELETE_BEARER_TOKEN_FILE      = NewMessageType("GAL1104E: Unable to delete the bearer token file '%s'.", 1104, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_GROUP_NAME_PROVIDED             = NewMessageType("GAL1105E: Invalid group name provided. Group names must only contain characters in the following ranges: 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '_' (underscore).", 1105, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_BEARER_TOKEN_FROM_API_SERVER = NewMessageType("GAL1106E: Could not get security bearer token from API server. Reason: '%s'. Ensure you have allocated a personal access token and configured your client program by setting your GALASA_TOKEN as an environment variable or by storing it in your galasactl.properties file", 1106, STACK_TRACE_WANTED)
	GALASA_ERROR_RETRIEVING_BEARER_TOKEN_FROM_FILE       = NewMessageType("GAL1107E: Could not get security bearer token from file '%s'. Reason: '%s'. Ensure you are authenticated by running 'galasactl auth login' and that your personal access token has not expired or been revoked", 1107, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_BEARER_TOKEN                    = NewMessageType("GAL1108E: Invalid bearer token. Ensure you are authenticated by running 'galasactl auth login' and that your personal access token has not expired or been revoked", 1108, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UNABLE_TO_GET_FILE_NAME                 = NewMessageType("GAL1109E: Unable to access the file. Reason: '%s'", 1109, STACK_TRACE_WANTED)
	GALASA_ERROR_COULD_NOT_GET_YAML_CONTENT              = NewMessageType("GAL1110E: Could not retrieve the content of the file provided. Reason: '%s'", 1110, STACK_TRACE_WANTED)
	GALASA_ERROR_UNABLE_TO_UNMARSHAL_YAML                = NewMessageType("GAL1111E: Error parsing the file '{}'. The file is not in the expected yaml format. Reason: '%s'. Invalid yaml:'%s'", 1111, STACK_TRACE_WANTED)
	GALASA_ERROR_UNABLE_TO_MARSHAL_INTO_JSON             = NewMessageType("GAL1112E: Error converting the parsed yaml content into a json payload for the http request. Reason: '%s'", 1112, STACK_TRACE_WANTED)
	GALASA_ERROR_RESOURCES_RESP_BAD_REQUEST              = NewMessageType("GAL1113E: Failure reported by the Galasa Ecosystem. The Ecosystem believes there is a problem with this client program or the user input. Errors returned: '%s'", 1113, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RESOURCES_RESP_SERVER_ERROR             = NewMessageType("GAL1114E: The resources operation failed due to a problem on the server. Collect a log with the --log option and contact your Galasa Ecosystem adminstrator.", 1114, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RESP_UNEXPECTED_ERROR                   = NewMessageType("GAL1115E: An unexpected response was received from the Galasa Ecosystem. Collect a log with the --log option and contact your Galasa Ecosystem adminstrator.", 1115, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UNABLE_TO_READ_RESPONSE_BODY            = NewMessageType("GAL1116E: Error reading the HTTP Response body. Reason: '%s'", 1116, STACK_TRACE_WANTED)
	GALASA_ERROR_DELETE_PROPERTY_RESPONSE_PARSING        = NewMessageType("GAL1117E: The delete operation failed. Unable to process the error information returned from the server.", 1117, STACK_TRACE_WANTED)
	GALASA_ERROR_RESOURCE_RESPONSE_PARSING               = NewMessageType("GAL1118E: The resource operation failed. Unable to process the error information returned from the server. Reason: '%s'", 1118, STACK_TRACE_WANTED)
	GALASA_ERROR_RESOURCE_RESP_UNAUTHORIZED_OPERATION    = NewMessageType("GAL1119E: The server thinks you are unauthorized to perform this operation.", 1119, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_COMMAND_NOT_FOUND_IN_CMD_COLLECTION     = NewMessageType("GAL1120E: Program logic error. Collect a log using the '--log' option and send to the Galasa development team.", 1120, STACK_TRACE_WANTED)
	GALASA_ERROR_UNABLE_TO_RETRIEVE_REST_API_VERSION     = NewMessageType("GAL1121E: Unable to retrieve rest api version. Reason is: %s. Try downloading the latest version of galasa or rebuilding a clean version.", 1121, STACK_TRACE_WANTED)
	GALASA_ERROR_AUTH_PROPERTY_NOT_AVAILABLE             = NewMessageType("GAL1122E: Authentication property %s is not available, which is needed to connect to the Galasa Ecosystem. It either needs to be in a file '%s' or set as an environment variable.", 1122, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BAD_TERMINAL_JSON_FORMAT                = NewMessageType("GAL1123E: Failed to read 3270 terminal JSON because the content is in the wrong format. Reason: %s", 1123, STACK_TRACE_WANTED)
	GALASA_ERROR_PNG_ENCODING_FAILED                     = NewMessageType("GAL1124E: Internal Failure. Terminal image could not be encoded into PNG format. Reason: %s", 1124, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_BAD_TOKEN_PROPERTY_FORMAT               = NewMessageType("GAL1125E: Authentication property %s is invalid. Ensure that it the value is made up of two parts that are separated by a '%s'.", 1125, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_OPEN_GZIP_FILE                = NewMessageType("GAL1225E: Failed to open file '%s' cause: %v. Check that this file exists, and that you have read permissions.", 1225, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_SETUP_READER_GZIP_FILE        = NewMessageType("GAL1226E: Internal failure. Contents of gzip could be read, but not decoded. New gzip reader failed: file: %s error: %v", 1226, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_UNCOMPRESS_GZIP_FILE          = NewMessageType("GAL1227E: Internal failure. Contents of gzip could not be decoded. %v error: %v", 1227, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_COMPRESS_BINARY_DATA          = NewMessageType("GAL1228E: Internal failure. Contents of gzip could not be encoded and compressed. %v error: %v", 1228, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_FLUSH_BINARY_DATA             = NewMessageType("GAL1229E: Internal failure. Contents of gzip could not be flushed while encoding and compressing. %v error: %v", 1229, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_CLOSE_GZIP_FILE               = NewMessageType("GAL1230E: Internal failure. Gzip file could not be closed while encoding and compressing. %v error: %v", 1230, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_NO_ACTIVE_RUNS_WITH_RUNNAME             = NewMessageType("GAL1132E: No active run found matching run name '%s'.", 1132, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RESET_RUN_FAILED                        = NewMessageType("GAL1133E: Error resetting run '%v'. Reason: '%s'", 1133, STACK_TRACE_WANTED)
	GALASA_ERROR_RESET_RUN_RESPONSE_PARSING              = NewMessageType("GAL1134E: The runs reset operation failed. Unable to process the error information returned from the server.", 1134, STACK_TRACE_WANTED)
	GALASA_ERROR_CANCEL_RUN_FAILED                       = NewMessageType("GAL1135E: Error cancelling run '%v'. Reason: '%s'", 1135, STACK_TRACE_WANTED)
	GALASA_ERROR_CANCEL_RUN_RESPONSE_PARSING             = NewMessageType("GAL1136E: The runs cancel operation failed. Unable to process the error information returned from the server.", 1136, STACK_TRACE_WANTED)
	GALASA_ERROR_GHERKIN_URL_BAD_EXTENSION               = NewMessageType("GAL1137E: The gherkin URL supplied is invalid. '%s' does not end with the expected '.feature' extension.", 1137, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GHERKIN_URL_BAD_URL_PREFIX              = NewMessageType("GAL1138E: Unsupported gherkin URL %s. Acceptable values start with 'file://'", 1138, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_COMPILE_NAMESPACE_REGEX       = NewMessageType("GAL1139E: Unable to compile the regex pattern for Galasa Property field 'namespace'. Reason: '%s'", 1139, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_PROPERTY_NAMESPACE_FORMAT       = NewMessageType("GAL1140E: The namespace, '%s', provided does not match formatting requirements. "+
		"The namespace must start with a character in the 'a-z' range, followed by characters in the 'a'-'z' or '0'-'9' ranges only.", 1140, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_COMPILE_PROPERTY_FIELD_REGEX = NewMessageType("GAL1141E: Unable to compile the regex pattern for Galasa Property field '%s'. Reason: '%s'", 1141, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_PROPERTY_FIELD_FORMAT          = NewMessageType("GAL1142E: The %s field value, '%s', provided does not match formatting requirements. "+
		"The %s field value must start with a character in the 'a-z' or 'A-Z' range, followed by any characters in the 'a'-'z', 'A'-'Z', '0'-'9', '.' (period), '-' (dash) or '_' (underscore) or '@' (at) ranges only.", 1142, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_QUERY_RUNS_NON_OK_STATUS              = NewMessageType("GAL1143E: Could not query run results. Server returned a non-200 code (%s)", 1143, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_TEST_CATALOG_CONTENTS_FAILED      = NewMessageType("GAL1144E: Could not use url '%s' to retrieve the contents of the test catalog from stream '%s'. Http error from the Galasa server is '%v'", 1144, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_CREATE_BEARER_TOKEN_FOLDER  = NewMessageType("GAL1145E: Failed to create folder for bearer tokens at '%s'\n", 1145, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_TOKEN_LIST_FROM_API_SERVER = NewMessageType("GAL1146E: Could not get list of tokens from API server. Reason: '%s'."+
		" Ensure you have allocated a personal access token and configured your client program by setting your GALASA_TOKEN as an environment variable or by storing it in your galasactl.properties file", 1146, STACK_TRACE_WANTED)
	GALASA_JWT_CANNOT_BE_PARSED = NewMessageType("GAL1147E: The cache of access tokens contains a java web token (jwt) which is invalid or can't be parsed. File is '%s'. This could indicate a corruption in the file. To resolve,"+
		" manually delete the file and authenticate against the server again with the 'galasactl auth login' command. If the problem persists, contact your Galasa system administrator. Detailed cause of this problem: '%s'", 1147, STACK_TRACE_NOT_WANTED)
	GALASA_JWT_HAS_NO_EXPIRATION_DATETIME = NewMessageType("GAL1148E: The cache of access tokens contains a java web token (jwt) in file '%s', from which an expiration time could not be extracted."+
		" This could indicate a problem with the authentication configuration on the Galasa server. Contect your Galasa system administrator. Detailed problem : '%s'\n", 1148, STACK_TRACE_NOT_WANTED)
	GALASA_JWT_ENCRYPTION_INVALID_GALASA_TOKEN        = NewMessageType("GAL1149E: Programming logic error: Cannot encrypt because the length of the key is too small.", 1149, STACK_TRACE_NOT_WANTED)
	GALASA_JWT_DECRYPTION_FAILED_BASE64               = NewMessageType("GAL1150E: Programming logic error: Decryption of cached bearer token failed. Reason: %s", 1150, STACK_TRACE_NOT_WANTED)
	GALASA_JWT_DECRYPTION_FAILED_BLOCK_TOO_SMALL      = NewMessageType("GAL1151E: Programming logic error: Decryption of cached bearer token failed. Cipher is not long enough. Cipher size: %d, AES block size: %d", 1151, STACK_TRACE_NOT_WANTED)
	GALASA_ENCRYPTION_DATA_TOO_LONG                   = NewMessageType("GAL1152E: Programming logic error: Too much data passed to the encryption process. Please contact your Galasa systems administrator.", 1152, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_REVOKE_TOKEN_FAILED                  = NewMessageType("GAL1153E: Failed to revoke the token with ID '%v'. Reason: '%s'.", 1153, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_TOKEN_ID_FORMAT              = NewMessageType("GAL1154E: The provided token ID, '%s', does not match formatting requirements. The token ID can contain any character in the 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), or '_' (underscore) ranges only.", 1154, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_MISSING_USER_LOGIN_ID_FLAG           = NewMessageType("GAL1155E: The loginId provided by the --login-id field cannot be an empty string.", 1155, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_USER_LIST_FROM_API_SERVER = NewMessageType("GAL1156E: Could not get list of users from API server. Reason: '%s'."+
		" Ensure you have allocated a personal access token and configured your client program by setting your GALASA_TOKEN as an environment variable or by storing it in your galasactl.properties file", 1156, STACK_TRACE_WANTED)
	GALASA_ERROR_DELETE_RUN_FAILED         = NewMessageType("GAL1157E: An attempt to delete a run named '%s' failed. Cause is %s", 1157, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SERVER_DELETE_RUNS_FAILED = NewMessageType("GAL1158E: An attempt to delete a run named '%s' failed. Sending the delete request to the Galasa service failed. Cause is %v", 1158, STACK_TRACE_NOT_WANTED)

	// 4 related but slightly different errors, when an HTTP response arrives from the Galasa server, and we can/can't parse the payload to get the message details out.
	GALASA_ERROR_DELETE_RUNS_NO_RESPONSE_CONTENT         = NewMessageType("GAL1159E: An attempt to delete a run named '%s' failed. Unexpected http status code %v received from the server.", 1159, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_RUNS_RESPONSE_PAYLOAD_UNREADABLE = NewMessageType("GAL1160E: An attempt to delete a run named '%s' failed. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1160, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_RUNS_UNPARSEABLE_CONTENT         = NewMessageType("GAL1161E: An attempt to delete a run named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1161, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_RUNS_SERVER_REPORTED_ERROR       = NewMessageType("GAL1162E: An attempt to delete a run named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1162, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_SERVER_DELETE_RUN_NOT_FOUND      = NewMessageType("GAL1163E: The run named '%s' could not be deleted because it was not found by the Galasa service. Try listing runs using 'galasactl runs get' to identify the one you wish to delete", 1163, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_RUNS_EXPLANATION_NOT_JSON = NewMessageType("GAL1164E: An attempt to delete a run named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1164, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_INVALID_LOGIN_ID        = NewMessageType("GAL1165E: '%s' is not supported as a valid login ID. Login ID should not contain spaces.", 1165, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_USER_FLAG_VALUE = NewMessageType("GAL1166E: The loginId provided by the --login-id field cannot be an empty string.", 1166, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_DELETE_SECRET_NO_RESPONSE_CONTENT      = NewMessageType("GAL1167E: An attempt to delete a secret named '%s' failed. Unexpected http status code %v received from the server.", 1167, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_SECRET_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1168E: An attempt to delete a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1168, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_SECRET_UNPARSEABLE_CONTENT      = NewMessageType("GAL1169E: An attempt to delete a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1169, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_SECRET_SERVER_REPORTED_ERROR    = NewMessageType("GAL1170E: An attempt to delete a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1170, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_SECRET_EXPLANATION_NOT_JSON     = NewMessageType("GAL1171E: An attempt to delete a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1171, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_SECRET_NAME                    = NewMessageType("GAL1172E: Invalid secret name provided. The name provided with the --name flag cannot be empty and must only contain characters in the following ranges: 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '_' (underscore).", 1172, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_SECRET_REQUEST_FAILED           = NewMessageType("GAL1173E: An attempt to delete a secret named '%s' failed. Sending the delete request to the Galasa service failed. Cause is %v", 1173, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_GET_SECRET_NO_RESPONSE_CONTENT      = NewMessageType("GAL1174E: An attempt to get a secret named '%s' failed. Unexpected http status code %v received from the server.", 1174, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRET_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1175E: An attempt to get a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1175, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRET_UNPARSEABLE_CONTENT      = NewMessageType("GAL1176E: An attempt to get a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1176, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRET_SERVER_REPORTED_ERROR    = NewMessageType("GAL1177E: An attempt to get a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1177, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRET_EXPLANATION_NOT_JSON     = NewMessageType("GAL1178E: An attempt to get a secret named '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1178, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRET_REQUEST_FAILED           = NewMessageType("GAL1179E: An attempt to get a secret named '%s' failed. Sending the get request to the Galasa service failed. Cause is %v", 1179, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_GET_SECRETS_NO_RESPONSE_CONTENT      = NewMessageType("GAL1180E: Failed to get secrets. Unexpected http status code %v received from the server.", 1180, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRETS_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1181E: Failed to get secrets. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1181, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRETS_UNPARSEABLE_CONTENT      = NewMessageType("GAL1182E: Failed to get secrets. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1182, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRETS_SERVER_REPORTED_ERROR    = NewMessageType("GAL1183E: Failed to get secrets. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1183, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRETS_EXPLANATION_NOT_JSON     = NewMessageType("GAL1184E: Failed to get secrets. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1184, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_SECRETS_REQUEST_FAILED           = NewMessageType("GAL1185E: Failed to get secrets. Sending the get request to the Galasa service failed. Cause is %v", 1185, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_INVALID_SECRET_TYPE_PROVIDED        = NewMessageType("GAL1186E: Invalid secret type provided. Supported secret types are: %v. Check your provided command parameters and try again.", 1186, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_NO_RESPONSE_CONTENT      = NewMessageType("GAL1187E: Failed to set a secret named '%s'. Unexpected http status code %v received from the server.", 1187, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1188E: Failed to set a secret named '%s'. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1188, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_UNPARSEABLE_CONTENT      = NewMessageType("GAL1189E: Failed to set a secret named '%s'. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1189, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_SERVER_REPORTED_ERROR    = NewMessageType("GAL1190E: Failed to set a secret named '%s'. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1190, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_EXPLANATION_NOT_JSON     = NewMessageType("GAL1191E: Failed to set a secret named '%s'. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1191, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_REQUEST_FAILED           = NewMessageType("GAL1192E: Failed to set a secret named '%s'. Sending the put request to the Galasa service failed. Cause is %v", 1192, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SET_SECRET_INVALID_FLAG_COMBINATION = NewMessageType("GAL1193E: Invalid flag combination provided. --username cannot be provided with --base64-username, --password cannot be provided with --base64-password, and --token cannot be provided with --base64-token."+SEE_COMMAND_REFERENCE, 1193, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_INVALID_SECRET_DESCRIPTION              = NewMessageType("GAL1194E: Invalid secret description provided. The description provided with the --description flag cannot be an empty string, and must only contain characters in the Latin-1 character set.", 1194, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_FAILED_TO_DELETE_USER                   = NewMessageType("GAL1195E: Failed to delete user from database by user number.", 1195, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SERVER_DELETE_USER_NOT_FOUND            = NewMessageType("GAL1196E: The user could not be deleted by login ID because it was not found by the Galasa service. Try listing users using 'galasactl users get' to identify the one you wish to delete", 1196, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_SERVER_DELETE_USER_FAILED               = NewMessageType("GAL1197E: An attempt to delete a user failed. Sending the delete request to the Galasa service failed. Cause is %v", 1197, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_USER_NO_RESPONSE_CONTENT         = NewMessageType("GAL1198E: An attempt to delete a user '%s' failed. Unexpected http status code %v received from the server.", 1198, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_USER_RESPONSE_PAYLOAD_UNREADABLE = NewMessageType("GAL1199E: An attempt to delete a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1199, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_USER_UNPARSEABLE_CONTENT         = NewMessageType("GAL1200E: An attempt to delete a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1200, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_USER_SERVER_REPORTED_ERROR       = NewMessageType("GAL1201E: An attempt to delete a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1201, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_USER_EXPLANATION_NOT_JSON        = NewMessageType("GAL1202E: An attempt to delete a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1202, STACK_TRACE_NOT_WANTED)

	// When getting multiple roles...
	GALASA_ERROR_GET_ROLES_REQUEST_FAILED           = NewMessageType("GAL1203E: Failed to get roles. Sending the get request to the Galasa service failed. Cause is %v", 1203, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_ROLES_NO_RESPONSE_CONTENT      = NewMessageType("GAL1204E: Failed to get roles. Unexpected http status code %v received from the server.", 1204, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_ROLES_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1205E: Failed to get roles. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1205, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_ROLES_UNPARSEABLE_CONTENT      = NewMessageType("GAL1206E: Failed to get roles. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1206, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_ROLES_SERVER_REPORTED_ERROR    = NewMessageType("GAL1207E: Failed to get roles. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1207, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_ROLES_EXPLANATION_NOT_JSON     = NewMessageType("GAL1208E: Failed to get roles. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1208, STACK_TRACE_NOT_WANTED)

	// When getting a single named role...
	GALASA_ERROR_INVALID_ROLE_NAME   = NewMessageType("GAL1209E: Invalid role name provided. The name provided with the --name flag cannot be empty and must only contain characters in the following ranges: 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '_' (underscore).", 1209, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_ROLE_NAME_NOT_FOUND = NewMessageType("GAL1210E: Role name %v is not known on the Galasa service.", 1210, STACK_TRACE_NOT_WANTED)

	// When updating a user
	GALASA_ERROR_SERVER_USER_NOT_FOUND                   = NewMessageType("GAL1211E: User with login id %v is not known on the Galasa service.", 1211, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATING_USER_RECORD                    = NewMessageType("GAL1212E: Failed to update user record on the Galasa service.", 1212, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_USER_NO_RESPONSE_CONTENT         = NewMessageType("GAL1213E: An attempt to update a user '%s' failed. Unexpected http status code %v received from the server.", 1213, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_USER_RESPONSE_PAYLOAD_UNREADABLE = NewMessageType("GAL1214E: An attempt to update a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1214, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_USER_UNPARSEABLE_CONTENT         = NewMessageType("GAL1215E: An attempt to update a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1215, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_USER_SERVER_REPORTED_ERROR       = NewMessageType("GAL1216E: An attempt to update a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1216, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_USER_EXPLANATION_NOT_JSON        = NewMessageType("GAL1217E: An attempt to update a user '%s' failed. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1217, STACK_TRACE_NOT_WANTED)

	GALASA_ERROR_MISSING_STREAM_NAME_FLAG             = NewMessageType("GAL1233E: The stream name provided by the --name field cannot be an empty string.", 1233, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_RETRIEVING_STREAMS_FROM_API_SERVER   = NewMessageType("GAL1234E: Could not get list of test streams from API server. Reason: '%s'.", 1234, STACK_TRACE_WANTED)
	GALASA_ERROR_INVALID_STREAM_NAME                  = NewMessageType("GAL1235E: The name provided with the --name flag cannot be empty and must only contain characters in the following ranges: 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '_' (underscore).", 1235, STACK_TRACE_WANTED)
	GALASA_ERROR_GET_STREAMS_NO_RESPONSE_CONTENT      = NewMessageType("GAL1236E: Failed to get streams. Unexpected http status code %v received from the server.", 1236, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_STREAMS_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1237E: Failed to get streams. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1237, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_STREAMS_UNPARSEABLE_CONTENT      = NewMessageType("GAL1238E: Failed to get streams. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1238, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_STREAMS_SERVER_REPORTED_ERROR    = NewMessageType("GAL1239E: Failed to get streams. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1239, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_STREAMS_EXPLANATION_NOT_JSON     = NewMessageType("GAL1240E: Failed to get streams. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1240, STACK_TRACE_NOT_WANTED)

	// Streams delete errors
	GALASA_ERROR_FAILED_TO_DELETE_STREAM                 = NewMessageType("GAL12412E: Failed to delete test stream with the given name from the Galasa service", 1241, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_STREAMS_NO_RESPONSE_CONTENT      = NewMessageType("GAL1242E: Failed to delete stream %s. Unexpected http status code %v received from the server.", 1242, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_STREAMS_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1243E: Failed to delete stream %s. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1243, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_STREAMS_UNPARSEABLE_CONTENT      = NewMessageType("GAL1244E: Failed to delete stream %s. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1244, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_STREAMS_SERVER_REPORTED_ERROR    = NewMessageType("GAL1245E: Failed to delete stream %s. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1245, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_DELETE_STREAMS_EXPLANATION_NOT_JSON     = NewMessageType("GAL1246E: Failed to delete stream %s. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1246, STACK_TRACE_NOT_WANTED)

	// When getting multiple monitors...
	GALASA_ERROR_GET_MONITORS_REQUEST_FAILED           = NewMessageType("GAL1218E: Failed to get monitors. Sending the get request to the Galasa service failed. Cause is %v", 1218, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_MONITORS_NO_RESPONSE_CONTENT      = NewMessageType("GAL1219E: Failed to get monitors. Unexpected http status code %v received from the server.", 1219, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_MONITORS_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1220E: Failed to get monitors. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1220, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_MONITORS_UNPARSEABLE_CONTENT      = NewMessageType("GAL1221E: Failed to get monitors. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1221, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_MONITORS_SERVER_REPORTED_ERROR    = NewMessageType("GAL1222E: Failed to get monitors. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1222, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_GET_MONITORS_EXPLANATION_NOT_JSON     = NewMessageType("GAL1223E: Failed to get monitors. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1223, STACK_TRACE_NOT_WANTED)

	// Getting a single monitor by name...
	GALASA_ERROR_MONITOR_NAME_NOT_FOUND = NewMessageType("GAL1224E: Galasa Monitor named %v is not known on the Galasa service.", 1224, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_INVALID_MONITOR_NAME   = NewMessageType("GAL1225E: Invalid monitor name provided. The name provided with the --name flag cannot be empty and must only contain characters in the following ranges: 'a'-'z', 'A'-'Z', '0'-'9', '-' (dash), '_' (underscore).", 1225, STACK_TRACE_NOT_WANTED)

	// When updating a monitor...
	GALASA_ERROR_INVALID_IS_ENABLED_FLAG                 = NewMessageType("GAL1226E: Invalid '--is-enabled' value provided. Supported values are 'true' and 'false'. Check your command parameters and try again.", 1226, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_REQUEST_FAILED           = NewMessageType("GAL1227E: Failed to update a monitor named '%s'. Sending the put request to the Galasa service failed. Cause is %v", 1227, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_NO_RESPONSE_CONTENT      = NewMessageType("GAL1228E: Failed to update a monitor named '%s'. Unexpected http status code %v received from the server.", 1228, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_RESPONSE_BODY_UNREADABLE = NewMessageType("GAL1229E: Failed to update a monitor named '%s'. Unexpected http status code %v received from the server. Error details from the server could not be read. Cause: %s", 1229, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_UNPARSEABLE_CONTENT      = NewMessageType("GAL1230E: Failed to update a monitor named '%s'. Unexpected http status code %v received from the server. Error details from the server are not in a valid json format. Cause: '%s'", 1230, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_SERVER_REPORTED_ERROR    = NewMessageType("GAL1231E: Failed to update a monitor named '%s'. Unexpected http status code %v received from the server. Error details from the server are: '%s'", 1231, STACK_TRACE_NOT_WANTED)
	GALASA_ERROR_UPDATE_MONITOR_EXPLANATION_NOT_JSON     = NewMessageType("GAL1232E: Failed to update a monitor named '%s'. Unexpected http status code %v received from the server. Error details from the server are not in the json format.", 1232, STACK_TRACE_NOT_WANTED)

	// Warnings...
	GALASA_WARNING_MAVEN_NO_GALASA_OBR_REPO = NewMessageType("GAL2000W: Warning: Maven configuration file settings.xml should contain a reference to a Galasa repository so that the galasa OBR can be resolved. The official release repository is '%s', and 'pre-release' repository is '%s'", 2000, STACK_TRACE_WANTED)

	// Information messages...
	GALASA_INFO_FOLDER_DOWNLOADED_TO = NewMessageType("GAL2501I: Downloaded %d artifacts to folder '%s'\n", 2501, STACK_TRACE_NOT_WANTED)
	GALASA_INFO_RUNS_RESET_SUCCESS   = NewMessageType("GAL2503I: The request to reset run '%s' has been accepted by the server.\n", 2503, STACK_TRACE_NOT_WANTED)
	GALASA_INFO_RUNS_CANCEL_SUCCESS  = NewMessageType("GAL2504I: The request to cancel run '%s' has been accepted by the server.\n", 2504, STACK_TRACE_NOT_WANTED)
)
