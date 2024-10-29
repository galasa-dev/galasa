/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"archive/zip"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"

	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"

	"galasa.dev/buildUtilities/pkg/galasayaml"
)

var (
	jacocofetchCmd = &cobra.Command{
		Use:   "jacocofetch",
		Short: "Fetch jacoco exec files",
		Long:  "Fetch and unzip Jacoco exec files that were created during zip run",

		Run: executeJacocofetch,
	}

	jacocofetchExecsUri        string
	jacocofetchResultsFile     string
	jacocofetchOutputDirectory string
)

func init() {
	jacocofetchCmd.PersistentFlags().StringVarP(&jacocofetchExecsUri, "execs", "", "", "The URI containing the exec zips")
	jacocofetchCmd.PersistentFlags().StringVarP(&jacocofetchResultsFile, "results", "", "", "The test results yaml file")
	jacocofetchCmd.PersistentFlags().StringVarP(&jacocofetchOutputDirectory, "output", "", "", "The output directory store the execs")

	jacocofetchCmd.MarkPersistentFlagRequired("execs")
	jacocofetchCmd.MarkPersistentFlagRequired("results")
	jacocofetchCmd.MarkPersistentFlagRequired("output")

	rootCmd.AddCommand(jacocofetchCmd)
}

func executeJacocofetch(cmd *cobra.Command, args []string) {
	fmt.Println("Reading the results file")

	// Read in the results file

	b, err := ioutil.ReadFile(jacocofetchResultsFile)
	if err != nil {
		panic(err)
	}

	var results galasayaml.Results
	err = yaml.Unmarshal(b, &results)
	if err != nil {
		panic(err)
	}

	if len(results.Tests) < 1 {
		fmt.Printf("No tests found")
		os.Exit(1)
	}

	// Create the output directory
	err = os.MkdirAll(jacocofetchOutputDirectory, 0775)
	if err != nil {
		panic(err)
	}

	// Pull jacoco exec zip
	fmt.Println("Pulling the Jacoco exec zips")
	for _, result := range results.Tests {
		fmt.Printf("    retrieving zip for %v/%v\n", result.Bundle, result.Class)
		url := jacocofetchExecsUri + "/" + result.Bundle + "/" + result.Class + ".zip"
		resp, err := http.Get(url)
		if err != nil {
			fmt.Printf("Failed to retrieve Jacoco exec zip from %v\n", url)
			panic(err)
		}

		defer resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			fmt.Printf("Failed to retrieve Jacoco exec zip from %v - status line - %v\n", url, resp.Status)
			os.Exit(1)
		}

		tempFile, err := os.Create("temp.zip")
		if err != nil {
			panic(err)
		}

		_, err = io.Copy(tempFile, resp.Body)
		if err != nil {
			panic(err)
		}

		fmt.Println("    retrieved, now unzipping")

		targetDirectory := jacocofetchOutputDirectory + "/" + result.Bundle
		err = os.MkdirAll(targetDirectory, 0775)
		if err != nil {
			panic(err)
		}

		zf, err := zip.OpenReader("temp.zip")
		if err != nil {
			panic(err)
		}
		defer zf.Close()

		for _, file := range zf.File {
			targetFile := targetDirectory + "/" + file.Name
			f, err := file.Open()
			if err != nil {
				panic(err)
			}
			defer f.Close()

			tempFile, err := os.Create(targetFile)
			if err != nil {
				panic(err)
			}

			_, err = io.Copy(tempFile, f)
			if err != nil {
				panic(err)
			}

			fmt.Println("    unzipped")
		}
	}
}
