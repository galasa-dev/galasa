/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"encoding/json"
	"log"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

type TestReport struct {
	Tests []TestRun `yaml:"tests" json:"tests"`
}

func ReportJSON(
	fileSystem spi.FileSystem,
	reportJsonFilename string,
	finishedRuns map[string]*TestRun,
	lostRuns map[string]*TestRun) error {

	var err error
	var testReport TestReport
	testReport.Tests = make([]TestRun, 0)

	for _, run := range finishedRuns {
		testReport.Tests = append(testReport.Tests, *run)
	}

	for _, run := range lostRuns {
		testReport.Tests = append(testReport.Tests, *run)
	}

	var data []byte
	data, err = json.MarshalIndent(&testReport, "", "  ")
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SUBMIT_REPORT_JSON_MARSHAL, reportJsonFilename, err.Error())
	}

	if err == nil {
		err = fileSystem.WriteBinaryFile(reportJsonFilename, data)
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_SUBMIT_REPORT_JSON_WRITE_FAIL, reportJsonFilename, err.Error())
		}
	}

	if err == nil {
		log.Printf("Json test report written to %v\n", reportJsonFilename)
	}

	return err
}
