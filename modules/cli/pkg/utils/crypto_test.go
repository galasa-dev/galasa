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

func TestCanRoundTripText(t *testing.T) {
	secret := "mysecretisreallylongandhardtoguess34567890$@" //This is a mock secret value //pragma: allowlist secret
	text := "this is my text"
	encrypted, err := Encrypt(secret, text)
	assert.Nil(t, err)
	if err == nil {
		var decrypted string
		decrypted, err = Decrypt(secret, encrypted)
		assert.Nil(t, err)
		assert.Equal(t, text, decrypted)
	}
}

func TestCryptoWithNoSecretFails(t *testing.T) {
	secret := "" // too small to use as an encryption key
	text := "this is my text"
	_, err := Encrypt(secret, text)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1149")
}

func TestCryptoFailsToEncryptHugeAmountsOfDataGracefully(t *testing.T) {
	secret := "a pretty small encryption key which should be big enough." //This is a mock secret value //pragma: allowlist secret
	dataSize := MAX_TEXT_TO_ENCRYPT + 10
	textBytes := make([]byte, dataSize)
	for i := 0; i < dataSize; i++ {
		textBytes[i] = 'a'
	}
	text := string(textBytes)

	_, err := Encrypt(secret, text)
	assert.NotNil(t, err)
	if err != nil {
		assert.Contains(t, err.Error(), "GAL1152")
	}
}
