/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package cmd

import (
	"galasa.dev/buildUtilities/pkg/utils"
	"galasa.dev/buildUtilities/pkg/versioning"
	"github.com/spf13/cobra"
)

var (
	versioningSuffixRemoveCmd = &cobra.Command{
		Use:   "remove",
		Short: "Removes the build version suffix of source code recursively.",
		Long:  "Removes the build version suffix of source code recursively.",
		Run:   versioningSuffixRemoveExecute,
	}
)

func init() {
	versioningSuffixCmd.AddCommand(versioningSuffixRemoveCmd)
}

func versioningSuffixRemoveExecute(cmd *cobra.Command, args []string) {

	fs := utils.NewOSFileSystem()
	err := versioning.SuffixRemoveExecute(fs, sourceCodeFolderPath)

	if err != nil {
		panic(err)
	}

}
