/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestSingleValidObrIsValid(t *testing.T) {
	obrInputs := []string{"mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr"}
	mavenCoordinates, err := ValidateObrs(obrInputs)
	assert.Nil(t, err)
	assert.Len(t, mavenCoordinates, 1)
	assert.NotNil(t, mavenCoordinates)
	assert.Equal(t, mavenCoordinates[0].ArtifactId, "dev.galasa.example.banking.obr")
	assert.Equal(t, mavenCoordinates[0].Classifier, "obr")
	assert.Equal(t, mavenCoordinates[0].GroupId, "dev.galasa.example.banking")
	assert.Equal(t, mavenCoordinates[0].Version, "0.0.1-SNAPSHOT")
}

func TestSingleObrIsInvalidTooFewPartsWithSlashSeparator(t *testing.T) {
	obrInputs := []string{"mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOTobr"}
	mavenCoordinates, err := ValidateObrs(obrInputs)
	assert.NotNil(t, err)
	assert.NotNil(t, mavenCoordinates)
	assert.Len(t, mavenCoordinates, 0)
	assert.Contains(t, err.Error(), "GAL1060E")
}

func TestSingleObrIsInvalidTooManyPartsWithSlashSeparator(t *testing.T) {
	obrInputs := []string{"mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT//obr"}
	mavenCoordinates, err := ValidateObrs(obrInputs)
	assert.NotNil(t, err)
	assert.NotNil(t, mavenCoordinates)
	assert.Len(t, mavenCoordinates, 0)
	assert.Contains(t, err.Error(), "GAL1061E")
}

func TestSingleObrIsInvalidTooManyPartsWithMissingMvnPrefix(t *testing.T) {
	obrInputs := []string{"dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr"}
	mavenCoordinates, err := ValidateObrs(obrInputs)
	assert.NotNil(t, err)
	assert.NotNil(t, mavenCoordinates)
	assert.Len(t, mavenCoordinates, 0)
	assert.Contains(t, err.Error(), "GAL1062E")
}

func TestSingleObrIsInvalidTooManyPartsWithMissingObrSuffix(t *testing.T) {
	obrInputs := []string{"mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/mysuffix"}
	mavenCoordinates, err := ValidateObrs(obrInputs)
	assert.NotNil(t, err)
	assert.NotNil(t, mavenCoordinates)
	assert.Len(t, mavenCoordinates, 0)
	assert.Contains(t, err.Error(), "GAL1063E")
}
