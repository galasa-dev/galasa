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

// A local resource cleanup process which gets run.
type LocalResourceCleanup struct {
	LocalGalasaBootProcess
}

// A structure which tells us all we know about a JVM process we launched.
func NewLocalResourceCleanup(
	mainPollLoopSleeper spi.TimedSleeper,
	fileSystem spi.FileSystem,
	processFactory ProcessFactory,
) *LocalResourceCleanup {

	localResourceCleanup := new(LocalResourceCleanup)

	localResourceCleanup.stdout = NewJVMOutputProcessor()
	localResourceCleanup.stderr = bytes.NewBuffer([]byte{})
	localResourceCleanup.mainPollLoopSleeper = mainPollLoopSleeper
	localResourceCleanup.processFactory = processFactory

	localResourceCleanup.reportingChannel = make(chan string, 100)

	return localResourceCleanup
}

// Launch a resource management run within a JVM.
func (localResourceCleanup *LocalResourceCleanup) launch(cmd string, args []string) error {

	// Create a new process, so we can track it and all we know about it.
	localResourceCleanup.process = localResourceCleanup.processFactory.NewProcess()

	// Start the process so it invokes the command.
	err := localResourceCleanup.process.Start(cmd, args, localResourceCleanup.stdout, localResourceCleanup.stderr)
	if err != nil {
		log.Printf("Failed to start the JVM. %s\n", err.Error())
		log.Printf("Failing command is %s %v\n", cmd, args)
	} else {

		log.Printf("JVM started. Spawning a go routine to wait for it to complete.\n")
		go localResourceCleanup.waitForCompletion()
	}
	return err
}

// This method is called by the launching thread as a go routine.
// The go routine waits for the JVM to complete, then emits
// a 'DONE' message which can be recieved by the monitoring thread.
// This call always blocks waiting for the launched JVM to complete and exit.
func (localResourceCleanup *LocalResourceCleanup) waitForCompletion() error {

	log.Printf("waiting for the JVM to complete within a go routine.\n")

	err := localResourceCleanup.process.Wait()
	if err != nil {
		log.Printf("Failed to wait for the JVM to complete. %s\n", err.Error())
	} else {
		log.Printf("JVM has completed. Detected by waiting go routine.\n")
	}

	// Tell any polling thread that the JVM is complete now.
	msg := "Resource cleanup completed"
	notifyComplete(msg, localResourceCleanup.reportingChannel, localResourceCleanup.mainPollLoopSleeper)

	return err
}

// This method is called by a thread monitoring the state of the JVM.
// It can receive messages from the JVM launcher go routine.
// This call never blocks waiting for anything.
func (localResourceCleanup *LocalResourceCleanup) isCompleted() bool {
	return isJvmCompleted(localResourceCleanup.reportingChannel)
}
