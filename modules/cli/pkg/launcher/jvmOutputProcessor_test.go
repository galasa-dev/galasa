/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestJvmOutputProcessorWritesCorrectNumberOfBytes(t *testing.T) {

	processorToTest := NewJVMOutputProcessor()
	n, _ := processorToTest.Write([]byte("A short string"))

	assert.Equal(t, 14, n, "Incorrect number of bytes written to the JVMOutputProcessor")
}

func TestJvmOutputProcessorCanDetectARunId(t *testing.T) {

	processorToTest := NewJVMOutputProcessor()
	processorToTest.Write([]byte("14/02/2023 12:19:11.990 INFO  d.g.f.FrameworkInitialisation - Allocated Run Name U525 to this run"))

	assert.Equal(t, "U525", processorToTest.detectedRunId, "Runid was not detected by the JVMOutputProcessor")
}

func TestJvmOutputProcessorSignalsWhenRunIdFound(t *testing.T) {
	processorToTest := NewJVMOutputProcessor()
	go processorToTest.Write([]byte("14/02/2023 12:19:11.990 INFO  d.g.f.FrameworkInitialisation - Allocated Run Name U525 to this run"))

	msg := <-processorToTest.publishResultChannel
	assert.Equal(t, "ALERT", msg, "unexpected message received from the output detector.")
}

func TestJvmOutputProcessorCollectsRasFolderPathUrl(t *testing.T) {
	processorToTest := NewJVMOutputProcessor()
	expectedLocation := "file:///Users/mcobbett/.galasa/ras"

	// When...
	// The jvm output is processed...
	go func() {
		processorToTest.Write([]byte("14/02/2023 12:19:11.990 INFO  Result Archive Stores are [" + expectedLocation + "]"))
	}()

	// We get a signal message...
	msg := <-processorToTest.publishResultChannel
	assert.Equal(t, "ALERT", msg, "unexpected message received from the output detector.")

	// And the RAS location should be known.
	assert.NotEmpty(t, processorToTest.detectedRasFolderPathUrl, "RAS folder path was not detected in simulated JVM trace output")
	assert.Equal(t, expectedLocation, processorToTest.detectedRasFolderPathUrl, "Wrong RAS folder path parsed from trace outpout")
}

func TestJvmOutputProcessorCanDetectAFrameworkShutdown(t *testing.T) {

	processorToTest := NewJVMOutputProcessor()
	go processorToTest.Write([]byte("14/02/2023 12:19:11.990 INFO  d.g.f.Framework - Framework shutdown"))

	msg := <-processorToTest.publishResultChannel
	assert.Equal(t, "ALERT", msg, "unexpected message received from the output detector.")
}
