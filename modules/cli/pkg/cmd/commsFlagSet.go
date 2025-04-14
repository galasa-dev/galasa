/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/spf13/pflag"
)

type GalasaFlagSet interface {
	Flags() *pflag.FlagSet
	Values() interface{}
}

type CommsFlagSetValues struct {
	bootstrap string
    maxRetries int
    retryBackoffSeconds float64
	*RootCmdValues
}

type CommsFlagSet struct {
	flagSet *pflag.FlagSet
	values  *CommsFlagSetValues
}

// ------------------------------------------------------------------------------------------------
// Constructors
// ------------------------------------------------------------------------------------------------

func NewCommsFlagSet(rootCommand spi.GalasaCommand) (*CommsFlagSet, error) {
	flagSet := new(CommsFlagSet)
	err := flagSet.init(rootCommand)
	return flagSet, err
}

// ------------------------------------------------------------------------------------------------
// Public functions
// ------------------------------------------------------------------------------------------------

func (commsFlagSet *CommsFlagSet) Flags() *pflag.FlagSet {
	return commsFlagSet.flagSet
}

func (commsFlagSet *CommsFlagSet) Values() interface{} {
	return commsFlagSet.values
}

// ------------------------------------------------------------------------------------------------
// Private functions
// ------------------------------------------------------------------------------------------------

func (commsFlagSet *CommsFlagSet) init(rootCmd spi.GalasaCommand) error {

	var err error

	commsFlagSet.values = &CommsFlagSetValues{
		RootCmdValues: rootCmd.Values().(*RootCmdValues),
	}
	commsFlagSet.flagSet, err = commsFlagSet.createFlagSet()

	return err
}

func (commsFlagSet *CommsFlagSet) createFlagSet() (*pflag.FlagSet, error) {

	var err error

	flagSet := pflag.NewFlagSet("comms", pflag.ContinueOnError)

	addBootstrapFlag(flagSet, &commsFlagSet.values.bootstrap)
    addRateLimitRetryFlags(flagSet, &commsFlagSet.values.maxRetries, &commsFlagSet.values.retryBackoffSeconds)

	return flagSet, err
}

