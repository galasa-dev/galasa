/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package main

import (
	"os"

	"github.com/dev-galasa/buildutils/openapi2beans/pkg/utils"
	"github.com/dev-galasa/buildutils/openapi2beans/pkg/cmd"
)

func main() {
	args := os.Args[1:]
	factory := utils.NewRealFactory()
	
	err := cmd.Execute(factory, args)

	if err != nil {
		os.Exit(1)
		panic(err)
	}
}
