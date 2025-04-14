/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"bytes"
	"fmt"
	"log"
	"strings"
	"time"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/galasa-dev/cli/pkg/spi"
)

// A local test which gets run.
type LocalTest struct {
	process Process
	stdout  *JVMOutputProcessor
	stderr  *bytes.Buffer

	// A go channel. Anything waiting for the test to complete will wait on
	// this channel. When the test completes, a string message is placed
	// on this channel to wake up any waiting threads.
	reportingChannel chan string

	// What runId is this test using ?
	// We don't initially know it. This info is extracted from the JVM trace.
	runId string

	// Where is the RAS folder storing results for this test ?
	// We don't initially know it. This info is extracted from the JVM trace.
	// eg: file:///C:/temp/ras
	rasFolderPathUrl string

	// A local test has a "testRun" which reports back to the submitter in the
	// same way the HTTP submitter reports back. Using common structures.
	// So the reporting code can all be re-used to summarise the results.
	testRun *galasaapi.TestRun

	// A time service. When a significant event occurs, we interrupt it.
	mainPollLoopSleeper spi.TimedSleeper

	// The file system the local test deposits results onto.
	// We use this to read the results back to find out if it passed/failed. ...etc.
	fileSystem spi.FileSystem

	// Something which can create new processes in the operating system
	processFactory ProcessFactory
}

// A structure which tells us all we know about a JVM process we launched.
func NewLocalTest(
	mainPollLoopSleeper spi.TimedSleeper,
	fileSystem spi.FileSystem,
	processFactory ProcessFactory,
) *LocalTest {

	localTest := new(LocalTest)

	localTest.stdout = NewJVMOutputProcessor()
	localTest.stderr = bytes.NewBuffer([]byte{})
	localTest.runId = ""
	localTest.testRun = nil
	localTest.mainPollLoopSleeper = mainPollLoopSleeper
	localTest.fileSystem = fileSystem
	localTest.processFactory = processFactory

	localTest.reportingChannel = make(chan string, 100)

	return localTest
}

// Launch a test within a JVM.
// Hang around waiting for the JVM to trace the runID and ras location.
func (localTest *LocalTest) launch(cmd string, args []string) error {

	// Create a new process, so we can track it and all we know about it.
	localTest.process = localTest.processFactory.NewProcess()

	// Start the process so it invokes the command.
	err := localTest.process.Start(cmd, args, localTest.stdout, localTest.stderr)
	if err != nil {
		log.Printf("Failed to start the JVM. %s\n", err.Error())
		log.Printf("Failing command is %s %v\n", cmd, args)
	} else {

		log.Printf("JVM test started. Spawning a go routine to wait for it to complete.\n")
		go localTest.waitForCompletion()

		localTest.runId, err = localTest.waitForRunIdAllocation(localTest.stdout)
		if err == nil {
			localTest.rasFolderPathUrl, err = localTest.waitForRasFolderPathUrl(localTest.stdout, localTest.runId)

			if err == nil {
				log.Printf("JVM test started and in progress. We know how to monitor it now.\n")
			}
		}
	}
	return err
}

// Block this thread until we can gather where the RAS folder is for this test.
// It is resolved within the JVM, and traced, where we pick it up from.
func (localTest *LocalTest) waitForRasFolderPathUrl(outputProcessor *JVMOutputProcessor, runId string) (string, error) {
	var err error
	var rasFolderPathUrl string

	// Wait for the ras location to be detected in the JVM output.
	isDoneWaiting := false

	for !isDoneWaiting {

		// No ras path available yet.
		if localTest.isCompleted() {
			// Completed before the ras location was available.
			isDoneWaiting = true
		} else {
			timer := time.After(time.Duration(time.Second))
			select {
			case <-outputProcessor.publishResultChannel:

				isDoneWaiting = true
			case <-timer:
			}
		}
	}

	rasFolderPathUrl = outputProcessor.detectedRasFolderPathUrl

	if rasFolderPathUrl == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_RAS_FOLDER_NOT_DETECTED, runId)
	}

	return rasFolderPathUrl, err
}

// Block this thread until we can gather what the RunId for this test is
// It is allocated within the JVM, and traced, where we pick it up from.
func (localTest *LocalTest) waitForRunIdAllocation(outputProcessor *JVMOutputProcessor) (string, error) {
	var err error
	var runId string

	// Wait for the runId to be detected in the JVM output.
	isDoneWaiting := false

	for !isDoneWaiting {

		// No runid available yet.
		if localTest.isCompleted() {
			// Completed before the runId was available.
			isDoneWaiting = true
		} else {

			timer := time.After(time.Duration(time.Second))

			select {
			case <-outputProcessor.publishResultChannel:
				// We have a RunId
				isDoneWaiting = true

			case <-timer:
			}
		}
	}

	// If the timer went, and we hadn't noted that the localTest was completed yet, there
	// is a timing window where we don't collect the detected runId.
	runId = outputProcessor.detectedRunId

	if runId == "" {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_RUN_ID_NOT_DETECTED)
	}

	return runId, err
}

// This method is called by the launching thread as a go routine.
// The go routine waits for the JVM to complete, then emits
// a 'DONE' message which can be recieved by the monitoring thread.
// This call always blocks waiting for the launched JVM to complete and exit.
func (localTest *LocalTest) waitForCompletion() error {

	log.Printf("waiting for the JVM to complete within a go routine.\n")

	err := localTest.process.Wait()
	if err != nil {
		log.Printf("Failed to wait for the JVM test to complete. %s\n", err.Error())
	} else {
		log.Printf("JVM has completed. Detected by waiting go routine.\n")
	}

	// Read any final status from the file created by the JVM
	localTest.updateTestStatusFromRasFile()

	// Tell any polling thread that the JVM is complete now.
	localTest.reportingChannel <- "DONE"
	close(localTest.reportingChannel)

	msg := fmt.Sprintf("Test run %s completed.", localTest.runId)
	localTest.mainPollLoopSleeper.Interrupt(msg)

	return err
}

// If we can find it, read the status report for the test from the
// ras folder.
func (localTest *LocalTest) updateTestStatusFromRasFile() error {

	var err error

	if localTest.runId == "" || localTest.rasFolderPathUrl == "" {
		log.Printf("Don't have enough information to find the structure.json in the RAS folder yet. Test JVM is starting up.\n")
	} else {

		jsonFilePath := strings.TrimPrefix(localTest.rasFolderPathUrl, "file:///") + "/" + localTest.runId + "/structure.json"
		log.Printf("Reading latest test status from '%s'\n", jsonFilePath)

		var testRun *galasaapi.TestRun
		testRun, err = readTestRunFromJsonFile(localTest.fileSystem, jsonFilePath)

		if err == nil {
			localTest.testRun = testRun
		}
	}
	return err
}

// This method is called by a thread monitoring the state of the JVM.
// It can receive messages from the JVM launcher go routine.
// This call never blocks waiting for anything.
func (localTest *LocalTest) isCompleted() bool {

	isComplete := false

	if localTest.testRun != nil && localTest.testRun.GetStatus() == "finished" {
		// The test is already complete.
		// log.Printf("Test is already complete\n")
		isComplete = true
	} else {

		// The JVM may not be finished. So check the channel where the output monitor tells us
		// when the JVM is shutting down.
		select {
		case msg := <-localTest.reportingChannel:
			log.Printf("Message received from JVM launch thread: %s\n", msg)
			if msg == "DONE" || msg == "" {
				isComplete = true
			}

		default:
			// log.Printf("No message received from JVM launch thread. Would block. JVM is not finished.")
			isComplete = false
		}

		localTest.updateTestStatusFromRasFile()
		if localTest.testRun != nil && localTest.testRun.GetStatus() == "finished" {
			// The test is already complete.
			log.Printf("Test is already complete when it wasn't before.\n")
			isComplete = true
		}
	}
	return isComplete
}
