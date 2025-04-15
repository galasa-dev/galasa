/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"

	"galasa.dev/buildUtilities/pkg/galasajson"
	"github.com/spf13/cobra"
)

var (
	slackpostTestsCmd = &cobra.Command{
		Use:   "tests",
		Short: "Make a Slack post of the failing tests from a galasactl runs submit report",
		Long:  "",
		Run:   slackpostTestsExecute,
	}
	testReportPath string
	testSuiteName  string
	testSuiteDesc  string
)

func init() {
	slackpostTestsCmd.PersistentFlags().StringVar(&testReportPath, "path", "", "Path to the galasactl report")
	slackpostTestsCmd.PersistentFlags().StringVar(&testSuiteName, "name", "", "Name of the test suite we are reporting on")
	slackpostTestsCmd.PersistentFlags().StringVar(&testSuiteDesc, "desc", "", "Description of the test suite we are reporting on")

	slackpostTestsCmd.MarkPersistentFlagRequired("path")
	slackpostTestsCmd.MarkPersistentFlagRequired("name")

	slackpostCmd.AddCommand(slackpostTestsCmd)
}

func slackpostTestsExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Slack Test Report - version %v\n", rootCmd.Version)

	report, err := unmarshalReport()
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}

	total := 0
	passed := 0
	failed := 0
	fwd := 0
	pwd := 0
	other := 0
	var failingTests []string
	var fwdTests []string
	var otherFailTests []string

	for _, test := range report.Tests {
		if test.Result != "Passed" {

			classNameFull := strings.Split(test.Class, ".")
			failure := fmt.Sprintf("\t%s %s: %s\n", test.Name, classNameFull[len(classNameFull)-1], test.Result)

			if test.Result == "Failed" {
				failed++
				failingTests = append(failingTests, failure)
			}
			if test.Result == "Failed With Defects" {
				fwd++
				fwdTests = append(fwdTests, failure)
			}
			if test.Result == "Passed With Defects" {
				pwd++
			}
			if !strings.HasPrefix(test.Result, "Passed") && !strings.HasPrefix(test.Result, "Failed") {
				other++
				otherFailTests = append(otherFailTests, failure)
			}

		} else {
			passed++
		}
		total++
	}

	content := fmt.Sprintf("Galasa Regression Testing - Failure Report\nTest Suite: %s\n%s\nTotal: %v\nPassed: %v, Failed: %v, Failed With Defects: %v, Passed With Defects: %v, Other: %v\n", testSuiteName, testSuiteDesc, total, passed, failed, fwd, pwd, other)
	for _, f := range failingTests {
		content += f
	}
	for _, f := range fwdTests {
		content += f
	}
	for _, f := range otherFailTests {
		content += f
	}

	client := http.Client{
		Timeout: time.Second * 30,
	}

	body := fmt.Sprintf("{\"text\":\"%s\"}", content)

	resp, err := client.Post(slackWebhook, "application/json", strings.NewReader(body))
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}

	fmt.Fprintln(os.Stdout, resp.Status)
}

func unmarshalReport() (galasajson.Results, error) {

	jsonReport, _ := os.ReadFile(testReportPath)

	var report galasajson.Results
	err := json.Unmarshal([]byte(jsonReport), &report)
	if err != nil {
		return report, err
	}

	if len(report.Tests) <= 0 {
		return report, fmt.Errorf("No results found in file %s", testReportPath)
	}
	return report, nil
}
