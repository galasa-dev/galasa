/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package errors

import (
	"log"
	"runtime"
)

func LogStackTrace() {
	// Log what the stack is.
	var stack [4096]byte
	// Only want the stack trace from the recovered execution thread, not all go routines running.
	isWantAllStackTraces := false
	n := runtime.Stack(stack[:], isWantAllStackTraces)

	log.Printf("%s\n", stack[:n])
}
