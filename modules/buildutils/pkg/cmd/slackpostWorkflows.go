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
	"strings"
	"time"

	"github.com/spf13/cobra"
)

var (
	slackpostWorkflowsCmd = &cobra.Command{
		Use:   "workflows",
		Short: "Make a Slack post of a failed GitHub workflow",
		Long:  "",
		Run:   slackpostWorkflowsExecute,
	}
	repo				string
	workflowName        	string
	workflowRunNumber  	string
	ref          		string
)
 
func init() {
	slackpostWorkflowsCmd.PersistentFlags().StringVar(&repo, "repo", "", "The name of the repository of the workflow that failed")
	slackpostWorkflowsCmd.PersistentFlags().StringVar(&workflowName, "workflowName", "", "The name of the workflow that failed")
	slackpostWorkflowsCmd.PersistentFlags().StringVar(&workflowRunNumber, "workflowRunNum", "", "The number of the workflow run that failed")
	slackpostWorkflowsCmd.PersistentFlags().StringVar(&ref, "ref", "", "The name of the branch/ref that was being built")

	slackpostWorkflowsCmd.MarkPersistentFlagRequired("repo")
	slackpostWorkflowsCmd.MarkPersistentFlagRequired("workflowName")
	slackpostWorkflowsCmd.MarkPersistentFlagRequired("workflowRunNum")
	slackpostWorkflowsCmd.MarkPersistentFlagRequired("ref")

	slackpostCmd.AddCommand(slackpostWorkflowsCmd)
}
 
func slackpostWorkflowsExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Slack Failed GitHub Workflow Report - version %v\n", rootCmd.Version)

	linkToWorkflowRun := fmt.Sprintf("https://github.com/galasa-dev/%s/actions/runs/%s", repo, workflowRunNumber)

	content := fmt.Sprintf("Galasa GitHub workflow failure:\n\nThe '%s' workflow failed for the '%s' repository when building the '%s' ref. Please see %s for details.", workflowName, repo, ref, linkToWorkflowRun)

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
