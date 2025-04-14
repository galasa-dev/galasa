/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"os"
	"strings"
)

// -------------------------------------------------
// A real implementation which writes text to stdout.
type RealConsole struct {
}

func NewRealConsole() *RealConsole {
	return new(RealConsole)
}

func (*RealConsole) WriteString(text string) error {
	_, err := os.Stdout.WriteString(text)
	return err
}

func (*RealConsole) Write(p []byte) (n int, err error) {
	n, err = os.Stdout.Write(p)
	return n, err
}

// -------------------------------------------------
// A mock implementation which writes text to a buffer
// Useful for unit testing.
type MockConsole struct {
	text strings.Builder
}

func NewMockConsole() *MockConsole {
	console := new(MockConsole)
	console.text = strings.Builder{}
	return console
}

func (this *MockConsole) WriteString(text string) error {
	_, err := this.text.WriteString(text)
	return err
}

func (data *MockConsole) ReadText() string {
	return data.text.String()
}

func (data *MockConsole) Write(p []byte) (n int, err error) {
	s := string(p)
	n, err = data.text.WriteString(s)
	return n, err
}
