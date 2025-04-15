/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

// -------------------------------------------------
// A console where things can be written to so the user
// can see them.
type Console interface {
	WriteString(text string) error
	Write(p []byte) (n int, err error)
}
