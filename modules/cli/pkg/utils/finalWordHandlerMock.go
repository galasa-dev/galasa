/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import "github.com/galasa-dev/cli/pkg/spi"

type MockFinalWordHandler struct {
	ReportedObject interface{}
}

func NewMockFinalWordHandler() spi.FinalWordHandler {
	return new(MockFinalWordHandler)
}

func (handler *MockFinalWordHandler) FinalWord(rootCmd spi.GalasaCommand, errorToExctractFrom interface{}) {
	// Capture the final word object to see what was sent.
	handler.ReportedObject = errorToExctractFrom
}
