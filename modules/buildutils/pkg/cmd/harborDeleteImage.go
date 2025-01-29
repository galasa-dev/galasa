/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"time"

	"galasa.dev/buildUtilities/pkg/galasayaml"
	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"
)

var (
	project    string
	repository string
	tag        string

	harborDeleteCmd = &cobra.Command{
		Use:   "deleteimage",
		Short: "Remove a specified image from a Harbor docker registry",
		Long:  "Specify the project, repository and tag to remove the image from the harbor registry",

		Run: executeHarbor,
	}
)

func init() {
	harborDeleteCmd.PersistentFlags().StringVarP(&project, "project", "p", "", "Which project to be interacting with")
	harborDeleteCmd.PersistentFlags().StringVarP(&repository, "repository", "r", "", "Which repository to be interacting with")
	harborDeleteCmd.PersistentFlags().StringVarP(&tag, "tag", "t", "", "Which tag to be interacting with")

	harborCmd.AddCommand(harborDeleteCmd)
}

func executeHarbor(cmd *cobra.Command, args []string) {
	if harborRepository == "" {
		panic("Provide a Harbor endpoint URL using the --harbor flag")
	}
	if project == "" {
		panic("Provide a project using the --project flag")
	}
	if repository == "" {
		panic("Provide a Harbor repository using the --repository flag")
	}
	if tag == "" {
		panic("Provide a repository tag using the --tag flag")
	}

	client := http.Client{
		Timeout: time.Second * 30,
	}
	req, err := http.NewRequest("DELETE", harborRepository+v2Api+"/projects/"+project+"/repositories/"+repository+"/artifacts/"+tag, nil)
	if err != nil {
		panic(err)
	}
	setBasicAuth(req)

	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	if resp.StatusCode == http.StatusOK {
		fmt.Printf("Image %v/%v:%v deleted from harbor\n", project, repository, tag)
	} else if resp.StatusCode == http.StatusNotFound {
		fmt.Printf("Image %v/%v:%v does not exist in harbor harbor\n", project, repository, tag)
	} else {
		fmt.Printf("FAILED - Repsonse from harbor: %s\n", resp.Status)
		os.Exit(1)
	}
}

func setBasicAuth(req *http.Request) {
	if harborCredentials == "" {
		if harborUsername == "" {
			panic("Provide a username using the --username flag")
		}
		if harborPassword == "" {
			panic("Provide a password using the --password flag")
		}

		req.SetBasicAuth(harborPassword, harborUsername)
	} else {
		in, err := ioutil.ReadFile(harborCredentials)
		if err != nil {
			panic(err)
		}
		var creds galasayaml.Credentials
		err = yaml.Unmarshal(in, &creds)
		if err != nil {
			panic(err)
		}
		req.SetBasicAuth(creds.Username, creds.Password)
	}
}
