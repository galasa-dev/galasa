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
	versioningSuffixCmd = &cobra.Command{
		Use:   "suffix",
		Short: "Manipulates the suffix of source code recursively.",
		Long:  "Manipulates the suffix of source code recursively.",
	}
)

func init() {
	versioningCmd.AddCommand(versioningSuffixCmd)
}
