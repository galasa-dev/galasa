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
	slackpostBuildsCmd = &cobra.Command{
		Use:   "builds",
		Short: "Make a Slack post of a failed build pipeline",
		Long:  "",
		Run:   slackpostBuildsExecute,
	}
	pipeline        string
	pipelineRunName string
	branch          string
)

func init() {
	slackpostBuildsCmd.PersistentFlags().StringVar(&pipeline, "pipeline", "", "The name of the Pipeline that failed")
	slackpostBuildsCmd.PersistentFlags().StringVar(&pipelineRunName, "prun", "", "The name of the PipelineRun that failed")
	slackpostBuildsCmd.PersistentFlags().StringVar(&branch, "branch", "", "The name of the branch that was being built")

	slackpostBuildsCmd.MarkPersistentFlagRequired("pipeline")
	slackpostBuildsCmd.MarkPersistentFlagRequired("prun")

	slackpostCmd.AddCommand(slackpostBuildsCmd)
}

func slackpostBuildsExecute(cmd *cobra.Command, args []string) {
	fmt.Printf("Galasa Build - Slack Failed Build Report - version %v\n", rootCmd.Version)

	linkToPipelineRun := fmt.Sprintf("http://localhost:8001/api/v1/namespaces/tekton-pipelines/services/tekton-dashboard:http/proxy/#/namespaces/galasa-build/pipelineruns/%s", pipelineRunName)
	branchString := ""
	if branch != "" {
		branchString = fmt.Sprintf(" when building the '%s' branch", branch)
	}
	content := fmt.Sprintf("Galasa build pipeline failure:\n\n'%s' pipeline failed%s. See %s for details.", pipeline, branchString, linkToPipelineRun)

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
