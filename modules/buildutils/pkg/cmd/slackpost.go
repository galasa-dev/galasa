/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/spf13/cobra"
)

var (
	slackpostCmd = &cobra.Command{
		Use:   "slackpost",
		Short: "Make a Slack post to notify the development team of test or build failures",
		Long:  "",
	}
	slackWebhook string
)

func init() {
	slackpostCmd.PersistentFlags().StringVar(&slackWebhook, "hook", "", "Webhook to post to Slack")

	slackpostCmd.MarkPersistentFlagRequired("hook")

	rootCmd.AddCommand(slackpostCmd)
}
