/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"log"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestCanCreatePollingJob(t *testing.T) {
	job := NewPollingJob(
		t.Name(),
		DEFAULT_MILLISECS_BETWEEN_POLLS,
		func() error {
			return nil
		},
	)
	assert.NotNil(t, job, "job should have been created ok")
}

func TestCanStartAndStopJobAndFunctionGetsCalled(t *testing.T) {
	pollFunctionCalledCounter := 0
	job := NewPollingJob(
		t.Name(),
		1, // second between polls
		func() error {
			log.Printf("Polling task function called.")
			pollFunctionCalledCounter += 1
			return nil
		},
	)

	job.Start()
	defer job.Stop()                  // Stop the job just in case.
	time.Sleep(50 * time.Millisecond) // Give the job a chance to schedule this function.
	// Stop the job explicitly.
	job.Stop()
	assert.Greater(t, pollFunctionCalledCounter, 0, "Poll function was never called")
}
