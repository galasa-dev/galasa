/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"fmt"
	"net/http"
	"os"

	"github.com/spf13/cobra"
)

var (
	githubBranchDeleteCmd = &cobra.Command{
		Use:   "delete",
		Short: "Delete a branch",
		Long:  "Delete an existing branch",
		Run:   githubBranchDeleteExecute,
	}

	branchDeleteBranch string
)

func init() {
	githubBranchDeleteCmd.PersistentFlags().StringVarP(&branchDeleteBranch, "branch", "", "", "branch to be deleted")

	githubBranchDeleteCmd.MarkPersistentFlagRequired("branch")

	githubBranchCmd.AddCommand(githubBranchDeleteCmd)
}

func githubBranchDeleteExecute(cmd *cobra.Command, args []string) {

	if branchDeleteBranch == "main" {
		fmt.Print("Not allowed delete the main branch\n")
		os.Exit(1)
	}

	basicAuth, err := githubGetBasicAuth()
	if err != nil {
		panic(err)
	}

	url := fmt.Sprintf("https://api.github.com/repos/galasa-dev/%v/git/ref/heads/%v", githubRepository, branchDeleteBranch)
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

	if resp.StatusCode == http.StatusNotFound {
		fmt.Printf("Branch %v is already deleted\n", branchDeleteBranch)
		os.Exit(0)
	}

	if resp.StatusCode != http.StatusOK {
		fmt.Printf("Get branch failed for url %v - status line - %v\n", url, resp.Status)
		os.Exit(1)
	}

	url = fmt.Sprintf("https://api.github.com/repos/galasa-dev/%v/git/refs/heads/%v", githubRepository, branchDeleteBranch)

	req, err = http.NewRequest("DELETE", url, nil)
	if err != nil {
		panic(err)
	}

	req.Header.Set("Authorization", basicAuth)
	req.Header.Set("Content-Type", "application/json")

	respDelete, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer respDelete.Body.Close()

	if respDelete.StatusCode != http.StatusNoContent {
		fmt.Printf("Delete failed for url %v - status line - %v\n", url, respDelete.Status)
		os.Exit(1)
	}

	fmt.Printf("Branch %v deleted on repository %v\n", branchDeleteBranch, githubRepository)

}
