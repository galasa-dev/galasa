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
	sourceCodeFolderPath string

	versioningCmd = &cobra.Command{
		Use:   "versioning",
		Short: "Setting/Clearing the build version suffix of source code.",
		Long:  "Commands to manipulate the versions of source code modules.",
	}
)

const SOURCE_FOLDER_PATH = "sourcefolderpath"

func init() {
	// The --sourcefolderepath flag. Refers to the top-level source folder to process.
	versioningCmd.PersistentFlags().StringVarP(&sourceCodeFolderPath, SOURCE_FOLDER_PATH, "p", "", "Path to the source tree to manipulate.")
	versioningCmd.MarkPersistentFlagRequired(SOURCE_FOLDER_PATH)

	rootCmd.AddCommand(versioningCmd)
}
