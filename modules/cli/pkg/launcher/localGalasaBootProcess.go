/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"bytes"
	"log"

	"github.com/galasa-dev/cli/pkg/spi"
)

// Represents a local Galasa boot process which gets run.
type LocalGalasaBootProcess struct {
	process Process
	stdout  *JVMOutputProcessor
	stderr  *bytes.Buffer

	// A go channel. Anything waiting for the process to complete will wait on
	// this channel. When complete, a string message is placed on this channel to wake
	// up any waiting threads.
	reportingChannel chan string

	// A time service. When a significant event occurs, we interrupt it.
	mainPollLoopSleeper spi.TimedSleeper

	// Something which can create new processes in the operating system
	processFactory ProcessFactory
}

// Tell any polling thread that the JVM is complete now.
func notifyComplete(msg string, reportingChannel chan string, mainPollLoopSleeper spi.TimedSleeper) {
	reportingChannel <- "DONE"
	close(reportingChannel)

	mainPollLoopSleeper.Interrupt(msg)
}

// This method is called by a thread monitoring the state of the JVM.
// It can receive messages from the JVM launcher go routine.
// This call never blocks waiting for anything.
func isJvmCompleted(reportingChannel chan string) bool {
	isComplete := false

	// The JVM may not be finished. So check the channel where the output monitor tells us
	// when the JVM is shutting down.
	select {
	case msg := <-reportingChannel:
		log.Printf("Message received from JVM launch thread: %s\n", msg)
		if msg == "DONE" || msg == "" {
			isComplete = true
		}

	default:
		isComplete = false
	}
	return isComplete
}
