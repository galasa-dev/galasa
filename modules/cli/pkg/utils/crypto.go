/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"io"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

const (
	// The maximum value we can cope with for the encryption.
	// We hold all the data in memory so need to be defensive we don't try to encrypt too much.
	MAX_TEXT_TO_ENCRYPT = 2048
)

func Encrypt(secret string, textToEncrypt string) (string, error) {
	var encryptedText string
	var err error
	var block cipher.Block

	if len(textToEncrypt) > MAX_TEXT_TO_ENCRYPT {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ENCRYPTION_DATA_TOO_LONG)
	} else {

		secret, err = makeSecretCorrectLength(secret)
		if err == nil {

			block, err = aes.NewCipher([]byte(secret))
			if err == nil {

				textToEncryptBytes := []byte(textToEncrypt)

				// To make things harder to crack, we add a random piece of data at
				// the start, before the data we actually want to encrypt.
				// It gets ignored once it gets decrypted anyway.
				data := make([]byte, aes.BlockSize+len(textToEncryptBytes))
				randomPart := data[:aes.BlockSize]
				// So fill-up the random part now.
				_, err = io.ReadFull(rand.Reader, randomPart)
				if err == nil {
					stream := cipher.NewCFBEncrypter(block, randomPart)

					// Copy in the plain text into the cipher block.
					stream.XORKeyStream(data[aes.BlockSize:], textToEncryptBytes)

					// Data now contains the random part + the plain text.

					// Now apply the encryption.
					encryptedText = base64.RawStdEncoding.EncodeToString(data)
				}
			}
		}
	}

	return encryptedText, err
}

func makeSecretCorrectLength(secret string) (string, error) {
	var newSecret string
	var err error

	if len(secret) == 0 {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_JWT_ENCRYPTION_INVALID_GALASA_TOKEN)
	} else {

		hasher := sha256.New()
		hasher.Write([]byte(secret))

		hashedBytes := hasher.Sum(nil)
		hashedString := base64.URLEncoding.EncodeToString(hashedBytes)

		// Crop the secret to the first 32 bytes.
		newSecret = hashedString[:32]
	}

	return newSecret, err
}

func Decrypt(secret string, encryptedText string) (string, error) {

	var err error
	var decryptedText string

	secret, err = makeSecretCorrectLength(secret)
	if err == nil {
		var ciphertext []byte
		ciphertext, err = base64.RawStdEncoding.DecodeString(encryptedText)
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_JWT_DECRYPTION_FAILED_BASE64, err)
		} else {
			var block cipher.Block
			block, err = aes.NewCipher([]byte(secret))
			if err == nil {

				// The IV needs to be unique, but not secure. Therefore it's common to
				// include it at the beginning of the ciphertext.
				if len(ciphertext) < aes.BlockSize {
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_JWT_DECRYPTION_FAILED_BLOCK_TOO_SMALL, len(ciphertext), aes.BlockSize)
				} else {
					randomPart := ciphertext[:aes.BlockSize]
					textToDecrypt := ciphertext[aes.BlockSize:]

					stream := cipher.NewCFBDecrypter(block, randomPart)

					decyptedTextBytes := make([]byte, len(textToDecrypt))
					stream.XORKeyStream(decyptedTextBytes, textToDecrypt)

					decryptedText = string(decyptedTextBytes)
				}
			}
		}
	}

	return decryptedText, err
}
