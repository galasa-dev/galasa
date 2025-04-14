/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

func FormatTimeToNearestDate(rawTime string) string {
	var formattedTimeString string
	if len(rawTime) < 19 {
		formattedTimeString = ""
	} else {
		formattedTimeString = rawTime[0:10]
	}
	return formattedTimeString
}

// -----------------------------------------------------
// Functions for time formats and duration
func FormatTimeToNearestDateTimeMins(rawTime string) string {
	var formattedTimeString string
	if len(rawTime) < 19 {
		formattedTimeString = ""
	} else {
		formattedTimeString = rawTime[0:16]
	}
	return formattedTimeString
}
