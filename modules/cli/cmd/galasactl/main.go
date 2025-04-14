/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package main

import (
	"os"

	"github.com/galasa-dev/cli/pkg/cmd"
)

func main() {

	factory := cmd.NewRealFactory()
	cmd.Execute(factory, os.Args[1:])
}
