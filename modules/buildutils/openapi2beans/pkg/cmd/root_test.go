/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"testing"

	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
	galasaUtils "github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func checkOutput(expectedStdOutput string, expectedStdErr string, factory utils.Factory, t *testing.T) {
	stdOutConsole := factory.GetStdOutConsole().(*galasaUtils.MockConsole)
	outText := stdOutConsole.ReadText()
	if expectedStdOutput != "" {
		assert.Contains(t, outText, expectedStdOutput)
	} else {
		assert.Empty(t, outText)
	}

	stdErrConsole := factory.GetStdErrConsole().(*galasaUtils.MockConsole)
	errText := stdErrConsole.ReadText()
	if expectedStdErr != "" {
		assert.Contains(t, errText, expectedStdErr)
	} else {
		assert.Empty(t, errText)
	}
}

func TestRootCmdOutputsInfo(t *testing.T) {
	// Given...
	args := []string{}
	mockFactory := utils.NewMockFactory()

	// When...
	err := Execute(mockFactory, args)

	// Then...
	assert.Nil(t, err)
	checkOutput("A tool for generating java beans from an openapi yaml file", "", mockFactory, t)
}
