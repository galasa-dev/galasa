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
	secvulnCmd = &cobra.Command{
		Use:   "secvuln",
		Short: "security vulnerability related commands",
		Long:  "Various commands to generate security vulnerability reports",
	}
)

func init() {
	rootCmd.AddCommand(secvulnCmd)
}
