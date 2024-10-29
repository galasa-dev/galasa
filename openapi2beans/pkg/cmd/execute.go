/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import "github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"


func Execute(factory utils.Factory, args []string) error {
	rootCmd := NewRootCommand(factory, Openapi2beansFlagStore{})
	rootCmd.SetArgs(args)

	// Execute the command
	err := rootCmd.Execute()

	return err
}
