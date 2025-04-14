/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package monitorsformatter

import (
	"testing"

	"github.com/galasa-dev/cli/pkg/galasaapi"
	"github.com/stretchr/testify/assert"
)

func TestMonitorsYamlFormatterHasCorrectName(t *testing.T) {
	formatter := NewMonitorsYamlFormatter()
	assert.Equal(t, formatter.GetName(), "yaml")
}

func TestMonitorsYamlFormatterValidData(t *testing.T) {
	// Given...
	formatter := NewMonitorsYamlFormatter()
	monitors := createTestMonitors()

	// When...
	actualFormattedOutput, err := formatter.FormatMonitors(monitors)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`apiVersion: galasa-dev/v1alpha1
kind: GalasaResourceCleanupMonitor
metadata:
    name: monitor1Name
    description: monitor1Description
data:
    isEnabled: true
    resourceCleanupData:
        stream: monitor1Stream
        filters:
            includes:
                - dev.galasa.*
                - my.company.*
            excludes:
                - dev.galasa.core.*
                - dev.galasa.docker.*
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaResourceCleanupMonitor
metadata:
    name: monitor2Name
    description: monitor2Description
data:
    isEnabled: false
    resourceCleanupData:
        stream: monitor1Stream
        filters:
            includes:
                - dev.galasa.*
                - my.company.*
            excludes:
                - dev.galasa.core.*
                - dev.galasa.docker.*
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestMonitorsYamlFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	// Given...
	formatter := NewMonitorsYamlFormatter()
	monitors := make([]galasaapi.GalasaMonitor, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatMonitors(monitors)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
