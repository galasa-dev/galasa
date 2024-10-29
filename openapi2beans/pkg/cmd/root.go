/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
	"github.com/spf13/cobra"
)

type Openapi2beansFlagStore struct {
	force         bool
	apiFilePath   string
	packageName   string
	storeFilepath string
	logFileName   string
}

func NewRootCommand(factory utils.Factory, flags Openapi2beansFlagStore) *cobra.Command {
	cmd := &cobra.Command{
		Use:          "openapi2beans",
		Short:        "CLI for openapi2beans",
		Long:         "A tool for generating java beans from an openapi yaml file.",
		SilenceUsage: true,
	}

	cmd.SetErr(factory.GetStdErrConsole())
	cmd.SetOut(factory.GetStdOutConsole())

	cmd.Flags().BoolP("help", "h", false, "Displays the options for the 'openapi2beans' command.")
	cmd.SetHelpCommand(&cobra.Command{Hidden: true})
	addFlags(cmd, &flags)

	addChildCommands(factory, flags, cmd)

	return cmd
}

func addChildCommands(factory utils.Factory, flags Openapi2beansFlagStore, rootCmd *cobra.Command) {
	generateCmd := NewGenerateCommand(factory, flags)
	rootCmd.AddCommand(generateCmd)
}

func addFlags(cmd *cobra.Command, flagStore *Openapi2beansFlagStore) {
	cmd.PersistentFlags().StringVarP(&flagStore.apiFilePath, "yaml", "y", "", "Specifies where to pull the openapi yaml from.")
	cmd.PersistentFlags().StringVarP(&flagStore.packageName, "package", "p", "generated", "Specifies what package the Java files belong to. Directories will be generated in accordance.")
	cmd.PersistentFlags().StringVarP(&flagStore.storeFilepath, "output", "o", "generated", "Specifies the file path to store the resulting generated java beans.")
	cmd.PersistentFlags().StringVarP(&flagStore.logFileName, "log", "l", "-", "Specifies the output file for logs.")
}
