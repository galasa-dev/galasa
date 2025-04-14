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

const (
	API_VERSION = "galasa-dev/v1alpha1"
)

func createTestMonitors() []galasaapi.GalasaMonitor {

	monitor1 := galasaapi.NewGalasaMonitor()
	monitor1.SetApiVersion(API_VERSION)
	monitor1.SetKind("GalasaResourceCleanupMonitor")

	monitor1Metadata := *galasaapi.NewGalasaMonitorMetadata()
	monitor1Metadata.SetName("monitor1Name")
	monitor1Metadata.SetDescription("monitor1Description")
	monitor1.Metadata = &monitor1Metadata

	monitor1Data := *galasaapi.NewGalasaMonitorData()
	monitor1Data.SetIsEnabled(true)
	monitor1.Data = &monitor1Data

	monitor1ResourceCleanupData := *galasaapi.NewGalasaMonitorDataResourceCleanupData()
	monitor1ResourceCleanupData.SetStream("monitor1Stream")

	monitor1Data.ResourceCleanupData = &monitor1ResourceCleanupData

	monitor1Filters := *galasaapi.NewGalasaMonitorDataResourceCleanupDataFilters()
	monitor1Filters.SetIncludes([]string{ "dev.galasa.*", "my.company.*" })
	monitor1Filters.SetExcludes([]string{ "dev.galasa.core.*", "dev.galasa.docker.*" })

	monitor1ResourceCleanupData.Filters = &monitor1Filters

	monitor2 := galasaapi.NewGalasaMonitor()
	monitor2.SetApiVersion(API_VERSION)
	monitor2.SetKind("GalasaResourceCleanupMonitor")

	monitor2Metadata := *galasaapi.NewGalasaMonitorMetadata()
	monitor2Metadata.SetName("monitor2Name")
	monitor2Metadata.SetDescription("monitor2Description")
	monitor2.Metadata = &monitor2Metadata

	monitor2Data := *galasaapi.NewGalasaMonitorData()
	monitor2Data.SetIsEnabled(false)
	monitor2.Data = &monitor2Data

	monitor2ResourceCleanupData := *galasaapi.NewGalasaMonitorDataResourceCleanupData()
	monitor2ResourceCleanupData.SetStream("monitor2Stream")

	monitor2Data.ResourceCleanupData = &monitor1ResourceCleanupData

	monitor2Filters := *galasaapi.NewGalasaMonitorDataResourceCleanupDataFilters()
	monitor2Filters.SetIncludes([]string{ "*" })
	monitor2Filters.SetExcludes([]string{ "my.company.*" })

	monitor2ResourceCleanupData.Filters = &monitor2Filters

	monitors := []galasaapi.GalasaMonitor{ *monitor1, *monitor2 }
	return monitors
}

func TestMonitorsSummaryFormatterHasCorrectName(t *testing.T) {
	formatter := NewMonitorsSummaryFormatter()
	assert.Equal(t, formatter.GetName(), "summary")
}

func TestMonitorsSummaryFormatterValidDataReturnsTotalCountTwo(t *testing.T) {
	// Given...
	formatter := NewMonitorsSummaryFormatter()
	monitors := createTestMonitors()

	// When...
	actualFormattedOutput, err := formatter.FormatMonitors(monitors)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput :=
`name         kind                         is-enabled
monitor1Name GalasaResourceCleanupMonitor true
monitor2Name GalasaResourceCleanupMonitor false

Total:2
`
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestMonitorsSummaryFormatterNoDataReturnsTotalCountAllZeros(t *testing.T) {
	// Given...
	formatter := NewMonitorsSummaryFormatter()
	monitors := make([]galasaapi.GalasaMonitor, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatMonitors(monitors)

	// Then...
	assert.Nil(t, err)
	expectedFormattedOutput := "Total:0\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
