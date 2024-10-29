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
	v2Api     = "/api/v2.0"
	harborCmd = &cobra.Command{
		Use:   "harbor",
		Short: "Interact with a Harbor docker registry",
		Long:  "Allows certain interations with a Harbor docker registry to manage build images.",
	}

	harborRepository  string
	harborUsername    string
	harborPassword    string
	harborCredentials string
)

func init() {
	harborCmd.PersistentFlags().StringVarP(&harborRepository, "harbor", "", "", "Harbor URL endpoint")
	harborCmd.PersistentFlags().StringVarP(&harborUsername, "username", "", "", "User for Harbor login. Must have sufficent authority")
	harborCmd.PersistentFlags().StringVarP(&harborPassword, "password", "", "", "password")
	harborCmd.PersistentFlags().StringVarP(&harborCredentials, "credentials", "", "", "A file path to a credentials file")
	rootCmd.AddCommand(harborCmd)
}
