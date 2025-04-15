/*
 * Copyright contributors to the Galasa project
 */
package runs

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestOrderedListInCorrectOrder(t *testing.T) {
	timeUnits := GetTimeUnitsOrderedList()
	assert.Equal(t, timeUnits[0].GetShortName(), "w")
	assert.Equal(t, timeUnits[1].GetShortName(), "d")
	assert.Equal(t, timeUnits[2].GetShortName(), "h")
	assert.Equal(t, timeUnits[3].GetShortName(), "m")
}

func TestErrorMessageAppearsInCorrectOrder(t *testing.T) {
	message := GetTimeUnitsForErrorMessage()

	weekIndex := strings.Index(message, "'w'")
	dayIndex := strings.Index(message, "'d'")
	hourIndex := strings.Index(message, "'h'")
	minuteIndex := strings.Index(message, "'m'")

	assert.Less(t, weekIndex, dayIndex)
	assert.Less(t, dayIndex, hourIndex)
	assert.Less(t, hourIndex, minuteIndex)
}
