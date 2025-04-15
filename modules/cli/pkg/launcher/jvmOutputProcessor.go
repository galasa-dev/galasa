/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"bytes"
	"log"
	"regexp"
	"strings"

	"github.com/galasa-dev/cli/pkg/utils"
)

// JVMOutputProcessor Sometjing which pretends to be an io.Writer interface implementation,
// and can be placed as stdout or stderr for a JVM process.
// The JVM process will write out trace statements to stdout, and this object listens to it.
// We watch the trace data as it arrives, searching for some data we want to extract from
// the JVM and Galasa framework as it executes.
// Ideally we'd gather the data in some other way, like in a file to which the properties we
// need are dumped... but using trace works for now and was quick to implement in the CLI
// component, rather than demand changes to the framework component also.
// Items we detect are stored in the structure below as we find them.
type JVMOutputProcessor struct {

	// The data which the processor has been passed so far.
	bytesCollected *bytes.Buffer

	// The runid which has been detected.
	detectedRunId string

	// The location of the RAS folder for this test.
	// "" if it isn't known yet.
	detectedRasFolderPathUrl string

	// The channel that goroutines can wait on until the processor
	// has detected something of interest, or the buffer is closed.
	// The channel can post "ALERT" when something is detected.
	publishResultChannel chan string
}

// Create a new JVM processor.
func NewJVMOutputProcessor() *JVMOutputProcessor {
	processor := new(JVMOutputProcessor)
	processor.detectedRunId = ""
	processor.bytesCollected = bytes.NewBuffer([]byte{})
	processor.publishResultChannel = make(chan string, 10)
	processor.detectedRasFolderPathUrl = ""
	return processor
}

// Some regex expressions we need to use to extract fields from trace strings.
var (
	runIdRegex         *regexp.Regexp = regexp.MustCompile(`Allocated Run Name (?P<runid>\S*) to this run`)
	runIdIndex         int            = runIdRegex.SubexpIndex("runid")
	rasFolderPathRegex *regexp.Regexp = regexp.MustCompile(`Result Archive Stores are \[(?P<ras_location>.*)]`)
	rasLocationIndex   int            = rasFolderPathRegex.SubexpIndex("ras_location")
)

const (
	SHUTDOWN_FRAMEWORK_EYE_CATCHER = `d.g.f.Framework - Framework shutdown`
)

// Part of the io.Writer interface. The JVM process is writing to its' stdout, which
// we are intercepting and monitoring.
func (processor *JVMOutputProcessor) Write(bytesToWrite []byte) (int, error) {

	// Store away the trace information anyway for posterity.
	bytesWrittenCount, err := processor.bytesCollected.Write(bytesToWrite)

	if err == nil {
		// See if we can gather the runId from the trace output.
		// We would expect it to appear in a string like this:
		// "d.g.f.FrameworkInitialisation - Allocated Run Name U525 to this run"
		stringToSearch := string(bytesToWrite)
		jvmStringNoTrailingNewline := strings.TrimSpace(stringToSearch)

		// Golang doesn't like printing 0x0d characters, it would rather they are 0x0a characters instead.
		// So for the purposes of echoing a log record to the terminal, do the conversion so it
		// comes out correctly.
		stringToLog := utils.StringWithNewLinesInsteadOfCRLFs(jvmStringNoTrailingNewline)

		if processor.detectedRunId != "" {
			log.Printf("JVM output: (runid:%s) : %s\n", processor.detectedRunId, stringToLog)
		} else {
			log.Printf("JVM output: %s\n", stringToLog)
		}

		isAlertable := false

		runId := detectRunId(stringToSearch)
		if runId != "" {
			processor.detectedRunId = runId
			isAlertable = true
		}

		rasFolderPathUrl := detectRasFolderPath(stringToSearch)
		if rasFolderPathUrl != "" {
			processor.detectedRasFolderPathUrl = rasFolderPathUrl
			isAlertable = true
		}

		isShutdownDetected := detectShutdown(stringToSearch)
		if isShutdownDetected {
			isAlertable = true
		}

		if isAlertable {
			// Now alert anyone who may be listening on the go channel.
			processor.publishResultChannel <- "ALERT"
		}
	}

	return bytesWrittenCount, err
}

// We expect each test to trace the following:
// "Result Archive Stores are [file:///Users/mcobbett/.galasa/ras]"
// So we should pick up this location and use it to find the json
// file containing the status of this test.
func detectRasFolderPath(stringToSearch string) string {
	var rasFolderPath string = ""

	rasFolderPathMatches := rasFolderPathRegex.FindStringSubmatch(stringToSearch)
	if rasFolderPathMatches == nil {
		// No matches in this string.
	} else {
		rasFolderPath = rasFolderPathMatches[rasLocationIndex]
		log.Printf("JVM Output processor discovered that the RAS folder path for the test is %s\n", rasFolderPath)
	}
	return rasFolderPath
}

// The JVM testcase will contains something like this:
// "Allocated Run Name <runId> to this run"
// So we try to extract the <runId> variable and return it.
// Returning "" if the string to search does not contain that format of string.
func detectRunId(stringToSearch string) string {
	var runId string = ""

	runIdMatches := runIdRegex.FindStringSubmatch(stringToSearch)
	if runIdMatches == nil {
		// No matches in this string.
	} else {
		runId = runIdMatches[runIdIndex]
		log.Printf("JVM Output processor discovered that the RunId for the test is %s\n", runId)
	}
	return runId
}

// Check to see if the input string is an indicator that the JVM is shutting down.
// If so, it contains the string "d.g.f.Framework - Framework shutdown"
func detectShutdown(stringToSearch string) bool {
	return strings.Contains(stringToSearch, SHUTDOWN_FRAMEWORK_EYE_CATCHER)
}
