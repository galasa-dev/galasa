/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"log"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
	"gopkg.in/yaml.v3"
)

func ReportYaml(
	fileSystem spi.FileSystem,
	reportYamlFilename string,
	finishedRuns map[string]*TestRun,
	lostRuns map[string]*TestRun) error {

	var testReport TestReport
	testReport.Tests = make([]TestRun, 0)

	for _, run := range finishedRuns {
		testReport.Tests = append(testReport.Tests, *run)
	}

	for _, run := range lostRuns {
		testReport.Tests = append(testReport.Tests, *run)
	}

	// Fail if the report file already exists. We don't want to overwrite anything.
	isExists, err := fileSystem.Exists(reportYamlFilename)
	if err == nil {
		if isExists {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_CREATE_REPORT_YAML_EXISTS, reportYamlFilename)
			return err
		}
	}

	// Turn the test report into yaml data (bytes)
	var bytes []byte
	bytes, err = yaml.Marshal(&testReport)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SUBMIT_REPORT_YAML_ENCODE, reportYamlFilename, err.Error())
		return err
	}

	// Write the report out to disk
	err = fileSystem.WriteBinaryFile(reportYamlFilename, bytes)
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SUBMIT_CREATE_REPORT_YAML, reportYamlFilename, err)
		return err
	}

	log.Printf("Yaml test report written to %v\n", reportYamlFilename)

	return err
}
