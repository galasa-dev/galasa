/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"testing"
	"time"

	"github.com/galasa-dev/cli/pkg/files"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

func newCache() JwtCache {
	fileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fileSystem, env, "")
	timeService := utils.NewMockTimeService()
	cache := NewJwtCache(fileSystem, galasaHome, timeService)
	return cache
}

func TestCanCreateCache(t *testing.T) {
	cache := newCache()
	assert.NotNil(t, cache)
}

type Header struct {
	Algorithm string `json:"alg"`
	Type      string `json:"typ"`
}

type Claims struct {
	ExpiresAt int64 `json:"exp"`
}

func createDummyJwt() (validJwt string, err error) {
	signingKey := "my-secret-key"
	expirationTime := time.Now().Add(20 * time.Minute).Unix() // Token will expire after 10 mins

	claims := Claims{
		ExpiresAt: expirationTime,
	}

	// Encode claims to JSON
	claimsJSON, err := json.Marshal(claims)
	if err != nil {
		fmt.Println("Error encoding claims:", err)
	} else {

		// Base64 encode the claims
		encodedClaims := base64.RawURLEncoding.EncodeToString(claimsJSON)

		// Create a string to sign by concatenating header and encoded claims
		header := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." // alg: HS256, typ:jwt
		toSign := header + encodedClaims

		// Sign the token with HMAC-SHA256
		hash := hmac.New(sha256.New, []byte(signingKey))
		hash.Write([]byte(toSign))
		signature := base64.RawURLEncoding.EncodeToString(hash.Sum(nil))

		// Construct the final JWT token
		validJwt = toSign + "." + signature
	}

	return validJwt, err
}

func TestCanAddJwtToCache(t *testing.T) {
	cache := newCache()
	mockJwt, _ := createDummyJwt()
	cache.Put("myApiServer", "myToken:myClientId", mockJwt)
}

func TestCanAddAndGetBackJwtFromCache(t *testing.T) {
	cache := newCache()
	mockJwt, err := createDummyJwt()
	if assert.Nil(t, err) {
		if assert.NotEmpty(t, mockJwt) {
			cache.Put("myApiServer", "myToken:myClientId", mockJwt)
			var jwtGotBack string
			jwtGotBack, err = cache.Get("myApiServer", "myToken:myClientId")

			if assert.Nil(t, err) {
				if assert.NotEmpty(t, jwtGotBack) {
					assert.Equal(t, jwtGotBack, mockJwt)
				}
			}
		}
	}
}

func createNewJwtCache() JwtCache {
	fileSystem := files.NewMockFileSystem()
	env := utils.NewMockEnv()
	galasaHome, _ := utils.NewGalasaHome(fileSystem, env, "")
	mockTime := time.UnixMilli(0)
	timeService := utils.NewOverridableMockTimeService(mockTime)

	cache := NewJwtCache(fileSystem, galasaHome, timeService)
	return cache
}

func TestCantGetBackANearlyExpiredToken(t *testing.T) {
	// Given...
	cache := createNewJwtCache()

	// This is a dummy JWT that expires 1 second after the Unix epoch
	expiredJwt := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjF9.2H0EJnt58ApysedXcvNUAy6FhgBIbDmPfq9d79qF4yQ" //pragma: allowlist secret

	cache.Put("myApiServer", "myToken:myClientId", expiredJwt)

	// When...
	tokenGotBack, err := cache.Get("myApiServer", "myToken:myClientId")

	// Then...
	assert.Nil(t, err, "Should not error when a bearer token has expired")
	assert.Empty(t, tokenGotBack)
}

func TestUrlToFileNameReturnsSomething(t *testing.T) {
	cache := fileBasedJwtCache{}
	fileName := cache.urlToFileName("http://a.b.c")
	assert.NotEmpty(t, fileName)
	assert.Equal(t, "a.b.c", fileName)
}

func TestClearingAllCacheDeletesBearerToken(t *testing.T) {
	// Given...
	cache := createNewJwtCache()

	mockJwt, _ := createDummyJwt()

	cache.Put("myApiServer", "myToken:myClientId", mockJwt)
	var jwtGotBack string
	var err error
	jwtGotBack, err = cache.Get("myApiServer", "myToken:myClientId")
	assert.Nil(t, err)
	assert.NotEmpty(t, jwtGotBack)
	assert.Equal(t, jwtGotBack, mockJwt)

	// So there is a jwt in the cache.

	// When... we delete them all.
	err = cache.ClearAll()
	assert.Nil(t, err)

	// Then .. we shouldn't be able to get the jwt back.
	jwtGotBack, err = cache.Get("myApiServer", "myToken:myClientId")
	assert.Nil(t, err)
	assert.Empty(t, jwtGotBack)

}

func TestCacheLooksEmptyIfEncryptionKeyChanges(t *testing.T) {
	// Given...
	cache := createNewJwtCache()

	mockJwt, _ := createDummyJwt()

	cache.Put("myApiServer", "myToken:myClientId", mockJwt)
	var jwtGotBack string
	var err error
	jwtGotBack, err = cache.Get("myApiServer", "myToken:myClientId")
	assert.Nil(t, err)
	assert.NotEmpty(t, jwtGotBack)
	assert.Equal(t, jwtGotBack, mockJwt)

	// So there is a jwt in the cache.

	// When... we change our galasa token

	// Then .. we shouldn't be able to get the jwt back.
	jwtGotBack, err = cache.Get("myApiServer", "myToken:myClientId2")
	assert.Nil(t, err)
	assert.Empty(t, jwtGotBack)

}
