/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package main

import (
	"fmt"
	"os"

	"galasa.dev/buildUtilities/pkg/cmd"
)

func main() {

	fmt.Println(os.Args)

	cmd.Execute()

}
