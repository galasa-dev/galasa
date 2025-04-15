/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/galasa-dev/cli/pkg/launcher"
)

// ---------------------------------------------------
// Functions
func TestAreSelectionFlagsProvidedDoesntPanicWhenFlagsAreNotSet(t *testing.T) {
	flags := NewTestSelectionFlagValues()
	areTheyProvided := AreSelectionFlagsProvided(flags)
	assert.False(t, areTheyProvided)
}

func TestAllocatingNewFlagsStructureHasEmptyArrays(t *testing.T) {
	flags := NewTestSelectionFlagValues()
	assert.NotNil(t, flags)
	assert.NotNil(t, flags.Bundles)
	assert.Equal(t, len(*flags.Bundles), 0)

	assert.NotNil(t, flags.Packages)
	assert.Equal(t, len(*flags.Packages), 0)

	assert.NotNil(t, flags.Tests)
	assert.Equal(t, len(*flags.Tests), 0)

	assert.NotNil(t, flags.Tags)
	assert.Equal(t, len(*flags.Tags), 0)

	assert.NotNil(t, flags.Classes)
	assert.Equal(t, len(*flags.Classes), 0)

	assert.NotNil(t, flags.RegexSelect)

	assert.Empty(t, flags.Stream)
}

func TestStreamBasedValidatorNoStreamButClassSpecifiedCausesError(t *testing.T) {
	flags := NewTestSelectionFlagValues()
	validator := NewStreamBasedValidator()
	// No stream set.

	*flags.Classes = make([]string, 1)
	(*flags.Classes)[0] = "myclass"

	flags.Stream = ""

	err := validator.Validate(flags)

	assert.NotNil(t, err)
	if err != nil {
		errorMessage := err.Error()
		fmt.Printf("Error returned is : %s\n", errorMessage)
		assert.Contains(t, err.Error(), "GAL1031E:")
	}
}

func TestStreamBasedValidatorWithStreamAndClassSpecifiedIsOk(t *testing.T) {
	flags := NewTestSelectionFlagValues()
	validator := NewStreamBasedValidator()
	// No stream set.

	*flags.Classes = make([]string, 1)
	(*flags.Classes)[0] = "myclass"

	flags.Stream = "myStream"

	err := validator.Validate(flags)

	assert.Nil(t, err)

}

func TestSelectTestFromGherkinUrlArrayReturnsTests(t *testing.T) {
	// Given...
	launcher := launcher.NewMockLauncher()
	flags := NewTestSelectionFlagValues()

	*flags.GherkinUrl = make([]string, 1)
	(*flags.GherkinUrl)[0] = "gherkin.feature"

	// When...
	testSelection, err := SelectTests(launcher, flags)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, testSelection)
	assert.Equal(t, testSelection.Classes[0].GherkinUrl, "gherkin.feature")
}

func TestSelectTestMultiplesFromGherkinUrlArrayReturnsTests(t *testing.T) {
	// Given...
	launcher := launcher.NewMockLauncher()
	flags := NewTestSelectionFlagValues()

	*flags.GherkinUrl = make([]string, 3)
	(*flags.GherkinUrl)[0] = "gherkin.feature"
	(*flags.GherkinUrl)[1] = "test.feature"
	(*flags.GherkinUrl)[2] = "excellent.feature"

	// When...
	testSelection, err := SelectTests(launcher, flags)

	// Then...
	assert.Nil(t, err)
	assert.NotNil(t, testSelection)
	assert.Equal(t, testSelection.Classes[0].GherkinUrl, "gherkin.feature")
	assert.Equal(t, testSelection.Classes[1].GherkinUrl, "test.feature")
	assert.Equal(t, testSelection.Classes[2].GherkinUrl, "excellent.feature")
}
