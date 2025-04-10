/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package auth

import (
	"log"
	"net/url"
	"strings"
	"time"

	"github.com/galasa-dev/cli/pkg/spi"
	"github.com/galasa-dev/cli/pkg/utils"
	"github.com/golang-jwt/jwt/v5"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
)

const (
	// If a JWT only has less than a certain time left before it expires, don't use it.
	// This sets the boundary where tokens about-to-expire are never returned from the cache.
	TOKEN_EXPIRY_BUFFER_MINUTES = 10
)

type JwtCache interface {

	// Adds a jwt to the cache.
	Put(serverApiUrl string, galasaToken string, jwt string) error

	// Returns the jwt if we have one, or "" if not.
	Get(serverApiUrl string, galasaToken string) (jwt string, err error)

	// Clears the jwt if we have one in the cache. ie: Logs out.
	Clear(serverApiUrl string, galasaToken string) error

	// Clears all the cache content.
	ClearAll() error
}

type fileBasedJwtCache struct {
	fileSystem  spi.FileSystem
	galasaHome  spi.GalasaHome
	timeService spi.TimeService
}

func NewJwtCache(
	fileSystem spi.FileSystem,
	galasaHome spi.GalasaHome,
	timeService spi.TimeService,
) JwtCache {
	cache := new(fileBasedJwtCache)

	cache.fileSystem = fileSystem
	cache.galasaHome = galasaHome
	cache.timeService = timeService

	return cache
}

func (cache *fileBasedJwtCache) Put(serverApiUrl string, galasaToken string, jwt string) (err error) {
	filename := cache.urlToFileName(serverApiUrl) + ".json"
	file := utils.NewBearerTokenFile(cache.fileSystem, cache.galasaHome, filename, cache.timeService)
	err = file.WriteJwt(jwt, galasaToken)
	return err
}

func (cache *fileBasedJwtCache) Clear(serverApiUrl string, galasaToken string) error {
	var err error
	filename := cache.urlToFileName(serverApiUrl) + ".json"
	file := utils.NewBearerTokenFile(cache.fileSystem, cache.galasaHome, filename, cache.timeService)
	file.DeleteJwt()
	return err
}

func (cache *fileBasedJwtCache) ClearAll() error {
	err := utils.DeleteAllBearerTokenFiles(cache.fileSystem, cache.galasaHome)
	return err
}

// Gets the jwt from the cache, or returns a string if it's not present.
// Only jwts which are valid (not expired) are returned.
func (cache *fileBasedJwtCache) Get(serverApiUrl string, galasaToken string) (jwt string, err error) {
	var possiblyInvalidJwt string

	filename := cache.urlToFileName(serverApiUrl) + ".json"
	file := utils.NewBearerTokenFile(cache.fileSystem, cache.galasaHome, filename, cache.timeService)

	var isExists bool
	isExists, err = file.Exists()
	if err == nil {

		if isExists {
			possiblyInvalidJwt, err = file.ReadJwt(galasaToken)

			if err != nil {
				log.Printf("Could not read JWT from file. Perhaps the Galasa token has changed since it was stored ?. Ignoring. %s", err.Error())
				err = nil
			} else {
				var isValid bool
				isValid, err = cache.isBearerTokenValid(possiblyInvalidJwt, cache.timeService, filename)
				if err != nil {
					log.Printf("Bearer token we read from encrypted file is invalid. Possibly due to Galasa token changing since it was stored. Ignoring. Reason: %s", err.Error())
					err = nil
				} else {
					if isValid {
						jwt = possiblyInvalidJwt
					}
				}
			}
		}
	}

	log.Printf("JwtCache: Get. Returning jwt of length %v. err: %v\n", len(jwt), err)

	return jwt, err
}

// Converts an arbitrary URL to a name we can use as a filename.
func (cache *fileBasedJwtCache) urlToFileName(urlToConvert string) string {

	// Strip off the https:// or http:// part
	s := strings.Replace(urlToConvert, "https://", "", 1)
	s1 := strings.Replace(s, "http://", "", 1)
	filename := url.QueryEscape(s1)
	return filename
}

// Checks whether a given bearer token is valid or not, returning true if it is valid and false otherwise
func (cache *fileBasedJwtCache) isBearerTokenValid(bearerTokenString string, timeService spi.TimeService, filename string) (bool, error) {
	var err error
	var bearerToken *jwt.Token
	var isValid bool = false

	// Decode the bearer token without verifying its signature
	bearerToken, _, err = jwt.NewParser().ParseUnverified(bearerTokenString, jwt.MapClaims{})
	if err != nil {
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_JWT_CANNOT_BE_PARSED, filename, err.Error())
	} else {
		var tokenExpiry *jwt.NumericDate
		tokenExpiry, err = bearerToken.Claims.GetExpirationTime()
		if err != nil {
			err = galasaErrors.NewGalasaError(galasaErrors.GALASA_JWT_HAS_NO_EXPIRATION_DATETIME, filename, err.Error())
		} else {
			// Add a buffer to the current time to make sure the bearer token does not expire within
			// this buffer (e.g. if the buffer is 10 mins, make sure the token doesn't expire within 10 mins)
			acceptableExpiryTime := timeService.Now().Add(time.Duration(TOKEN_EXPIRY_BUFFER_MINUTES) * time.Minute)
			if (tokenExpiry.Time).After(acceptableExpiryTime) {
				isValid = true
			} else {
				log.Printf("JWT token is valid, but due to expire shortly, so ignoring it.\n")
			}
		}
	}
	return isValid, err
}
