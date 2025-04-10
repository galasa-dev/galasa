/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

import (
	"strings"
)

var (
	TIME_UNIT_WEEKS   = newTimeUnit("weeks", "w", 10080)
	TIME_UNIT_DAYS    = newTimeUnit("days", "d", 1440)
	TIME_UNIT_HOURS   = newTimeUnit("hours", "h", 60)
	TIME_UNIT_MINUTES = newTimeUnit("minutes", "m", 1)
)

type TimeUnit struct {
	longName         string
	shortName        string
	minuteMultiplier int
}

func newTimeUnit(longName string, shortName string, multiplier int) *TimeUnit {
	return &TimeUnit{
		longName:         longName,
		shortName:        shortName,
		minuteMultiplier: multiplier,
	}
}

var (
	// A simple list of all the time units available.
	// ordered by most-important/longest-duration first.
	// This is the cache. It gets populated the first time anyone needs it.
	timeUnitsOrderedList []*TimeUnit = nil

	// A map of all the time units, where the key is the short name,
	// (or letter)
	// This is the cache. It gets populated the first time anyone needs it.
	timeUnitsMap map[string]*TimeUnit = nil

	// A string containing all the error messages in a form we can display.
	// This is the cache. It gets populated the first time anyone needs it.
	timeUnitsForErrorMessage = ""
)

func (unit *TimeUnit) GetLongName() string {
	return unit.longName
}

func (unit *TimeUnit) GetShortName() string {
	return unit.shortName
}

func (unit *TimeUnit) GetMinuteMultiplier() int {
	return unit.minuteMultiplier
}

func GetTimeUnitsMap() map[string]*TimeUnit {
	if timeUnitsMap == nil {
		timeUnitsMap = createMapOfTimeUnits()
	}
	return timeUnitsMap
}

func GetTimeUnitsOrderedList() []*TimeUnit {
	// The list is a cache. Only set up if we need it.
	if timeUnitsOrderedList == nil {
		timeUnitsOrderedList = createOrderedListOfTimeUnits()
	}
	return timeUnitsOrderedList
}

func createOrderedListOfTimeUnits() []*TimeUnit {
	// Build a complete list (ordered by importance/length of time) of all the time units.
	list := make([]*TimeUnit, 0)
	list = append(list, TIME_UNIT_WEEKS)
	list = append(list, TIME_UNIT_DAYS)
	list = append(list, TIME_UNIT_HOURS)
	list = append(list, TIME_UNIT_MINUTES)
	return list
}

func createMapOfTimeUnits() map[string]*TimeUnit {
	// Populate a map of the time units available, for quick lookup.
	timeUnitMap := make(map[string]*TimeUnit, 0)
	for _, timeUnit := range GetTimeUnitsOrderedList() {
		timeUnitMap[timeUnit.shortName] = timeUnit
	}
	return timeUnitMap
}

func GetTimeUnitsForErrorMessage() string {
	if timeUnitsForErrorMessage == "" {
		timeUnitsForErrorMessage = createTimeUnitsForErrorMessage()
	}
	return timeUnitsForErrorMessage
}

func createTimeUnitsForErrorMessage() string {
	outputString := strings.Builder{}
	count := 0
	for _, timeUnit := range GetTimeUnitsOrderedList() {

		if count != 0 {
			outputString.WriteString(", ")
		}
		outputString.WriteString("'" + timeUnit.GetShortName() + "' (" + timeUnit.GetLongName() + ")")
		count++
	}

	return outputString.String()
}

func GetTimeUnitFromShortName(shortName string) (*TimeUnit, bool) {
	unit, isFound := GetTimeUnitsMap()[shortName]
	return unit, isFound
}
