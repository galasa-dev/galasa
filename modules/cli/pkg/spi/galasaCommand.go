/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import (
	"github.com/spf13/cobra"
)

// A class which houses both the cobra command and the values structure the command
// puts things into.
type GalasaCommand interface {
	// The name of the galasa command. One of the COMMAND_NAME_* constants.
	Name() string

	// Returns the cobra command which is part of the Galasa command.
	CobraCommand() *cobra.Command

	// Returns the data structure associated with this cobra command.
	Values() interface{}
}
