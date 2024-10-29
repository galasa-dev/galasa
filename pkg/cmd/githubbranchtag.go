/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"

	"galasa.dev/buildUtilities/pkg/githubjson"
)

var (
	githubBranchTagCmd = &cobra.Command{
		Use:   "tag",
		Short: "Create a tag for a branch",
		Long:  "Create a tag for a branch",
		Run:   githubBranchTagExecute,
	}

	branchTagBranch string
	branchTagTag    string
)

func init() {
	githubBranchTagCmd.PersistentFlags().StringVarP(&branchTagBranch, "branch", "", "", "branch to create the tag")
	githubBranchTagCmd.PersistentFlags().StringVarP(&branchTagTag, "tag", "", "", "tag to create")

	githubBranchTagCmd.MarkPersistentFlagRequired("branch")
	githubBranchTagCmd.MarkPersistentFlagRequired("tag")

	githubBranchCmd.AddCommand(githubBranchTagCmd)
}

func githubBranchTagExecute(cmd *cobra.Command, args []string) {

	basicAuth, err := githubGetBasicAuth()
	if err != nil {
		panic(err)
	}

	// First get the sha of the from branch

	var url = fmt.Sprintf("https://api.github.com/repos/galasa-dev/%v/git/ref/heads/%v", githubRepository, branchTagBranch)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		panic(err)
	}

	req.Header.Set("Authorization", basicAuth)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}

	if resp.StatusCode != http.StatusOK {
		fmt.Printf("Get from sha failed for url %v - status line - %v\n", url, resp.Status)
		os.Exit(1)
	}

	defer resp.Body.Close()
	var reference githubjson.Reference

	err = json.NewDecoder(resp.Body).Decode(&reference)

	if err != nil {
		panic(err)
	}

	fmt.Printf("SHA for branch %v is %v\n", branchTagBranch, reference.Object.Sha)

	// Now create the new branch based on that sha

	var newReference githubjson.NewReference
	newReference.Ref = fmt.Sprintf("refs/tags/%v", branchTagTag)
	newReference.Sha = reference.Object.Sha

	newReferenceBuffer := new(bytes.Buffer)
	err = json.NewEncoder(newReferenceBuffer).Encode(newReference)
	if err != nil {
		panic(err)
	}

	httpType := "POST"
	url = fmt.Sprintf("https://api.github.com/repos/galasa-dev/%v/git/refs", githubRepository)

	req, err = http.NewRequest(httpType, url, newReferenceBuffer)
	if err != nil {
		panic(err)
	}

	req.Header.Set("Authorization", basicAuth)
	req.Header.Set("Content-Type", "application/json")

	respNew, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer respNew.Body.Close()

	if respNew.StatusCode != http.StatusOK && respNew.StatusCode != http.StatusCreated {
		fmt.Printf("%v to set sha failed %v - status line - %v\n", httpType, url, respNew.Status)
		os.Exit(1)
	}

	fmt.Printf("Tag %v created on repository %v, now sha %v\n", branchTagTag, githubRepository, reference.Object.Sha)
}
