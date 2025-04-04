/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"encoding/json"
	"log"
	"path/filepath"

	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/galasa-dev/cli/pkg/spi"
)

type BearerTokenJson struct {
	Jwt string `json:"jwt"`
}

type BearerTokenFile interface {
	WriteJwt(jwt string, encryptionSecret string) error
	ReadJwt(encryptionSecret string) (string, error)
	DeleteJwt() error
	Exists() (bool, error)
}

type BearerTokenFileImpl struct {
	fileSystem   spi.FileSystem
	galasaHome   spi.GalasaHome
	baseFileName string
	timeService  spi.TimeService
}

func NewBearerTokenFile(
	fileSystem spi.FileSystem,
	galasaHome spi.GalasaHome,
	baseFileName string,
	timeService spi.TimeService,
) BearerTokenFile {
	file := new(BearerTokenFileImpl)
	file.fileSystem = fileSystem
	file.galasaHome = galasaHome
	file.baseFileName = baseFileName
	file.timeService = timeService
	return file
}

func ListAllBearerTokenFiles(fileSystem spi.FileSystem, galasaHome spi.GalasaHome) ([]string, error) {
	bearerTokenFolderPath := getBearerTokensFolderPath(galasaHome)
	return fileSystem.GetAllFilePaths(bearerTokenFolderPath)
}

func DeleteAllBearerTokenFiles(fileSystem spi.FileSystem, galasaHome spi.GalasaHome) error {
	bearerTokenFilePaths, err := ListAllBearerTokenFiles(fileSystem, galasaHome)

	if err == nil {
		for _, bearerTokenFilePath := range bearerTokenFilePaths {
			log.Printf("DeleteAllBearerTokenFiles : deleting file '%s'", bearerTokenFilePath)
			fileSystem.DeleteFile(bearerTokenFilePath)
		}
	}
	return err
}

func getBearerTokensFolderPath(galasaHome spi.GalasaHome) string {
	return filepath.Join(galasaHome.GetNativeFolderPath(), "bearer-tokens")
}

// Writes a new bearer-token.json file containing a JWT in the following format:
//
//	{
//	  "jwt": "<bearer-token-here>"
//	}
func (file *BearerTokenFileImpl) WriteJwt(jwt string, encryptionSecret string) error {
	bearerTokenFolderPath := getBearerTokensFolderPath(file.galasaHome)
	var err error
	err = file.fileSystem.MkdirAll(bearerTokenFolderPath)
	if err != nil {
		log.Printf("Failed to make sure beader-tokens folder exists. '%s'", bearerTokenFolderPath)
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_CREATE_BEARER_TOKEN_FOLDER, bearerTokenFolderPath, err.Error())
	} else {

		bearerTokenFilePath := filepath.Join(bearerTokenFolderPath, file.baseFileName)

		log.Printf("Writing bearer token to file '%s'", bearerTokenFilePath)

		var json string
		json, err = buildBearerTokenFileContent(jwt)
		if err == nil {

			var encryptedJwt string
			encryptedJwt, err = Encrypt(encryptionSecret, json)
			if err == nil {
				err = file.fileSystem.WriteTextFile(bearerTokenFilePath, encryptedJwt)

				if err == nil {
					log.Printf("Written bearer token to file '%s' OK", bearerTokenFilePath)
				} else {
					log.Printf("Failed to write bearer token file '%s'", bearerTokenFilePath)
					err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_FAILED_TO_WRITE_FILE, bearerTokenFilePath, err.Error())
				}
			}
		}
	}
	return err
}

// Pack the json string into a structure.
func buildBearerTokenFileContent(jwt string) (contentJson string, err error) {
	content := BearerTokenJson{
		Jwt: jwt,
	}
	var contentJsonBytes []byte
	contentJsonBytes, err = json.Marshal(content)
	if err == nil {
		contentJson = string(contentJsonBytes)
	}
	return contentJson, err
}

// Gets the JWT from the bearer-token.json file if it exists, errors if the file does not exist or if the token is invalid
func (file *BearerTokenFileImpl) ReadJwt(encryptionSecret string) (string, error) {
	var err error
	var bearerToken string = ""

	bearerTokenFolderPath := filepath.Join(file.galasaHome.GetNativeFolderPath(), "bearer-tokens")
	bearerTokenFilePath := filepath.Join(bearerTokenFolderPath, file.baseFileName)

	log.Printf("Retrieving bearer token from file '%s'", bearerTokenFilePath)
	var encryptedBearerTokenJsonContents string
	encryptedBearerTokenJsonContents, err = file.fileSystem.ReadTextFile(bearerTokenFilePath)
	if err == nil {
		var bearerTokenJsonContents string
		bearerTokenJsonContents, err = Decrypt(encryptionSecret, encryptedBearerTokenJsonContents)

		if err != nil {
			log.Printf("Could not retrieve bearer token from file '%s' because it was encrypted with a different GALASA_TOKEN. Ignoring.", bearerTokenFilePath)
			// This will look to the caller like there was nothing to read.
		} else {
			var bearerTokenJson BearerTokenJson
			err = json.Unmarshal([]byte(bearerTokenJsonContents), &bearerTokenJson)
			if err == nil {
				bearerToken = bearerTokenJson.Jwt
				log.Printf("Retrieved bearer token from file '%s' OK", bearerTokenFilePath)
			}
		}
	}

	if err != nil {
		log.Printf("Could not retrieve bearer token from file '%s'", bearerTokenFilePath)
		err = galasaErrors.NewGalasaError(galasaErrors.GALASA_ERROR_RETRIEVING_BEARER_TOKEN_FROM_FILE, bearerTokenFilePath, err.Error())
	}

	return bearerToken, err
}

func (file *BearerTokenFileImpl) DeleteJwt() error {
	var err error
	bearerTokenFolderPath := getBearerTokensFolderPath(file.galasaHome)
	bearerTokenFilePath := filepath.Join(bearerTokenFolderPath, file.baseFileName)
	log.Printf("DeleteJwt file '%s'", bearerTokenFilePath)

	file.fileSystem.DeleteFile(bearerTokenFilePath)
	return err
}

func (file *BearerTokenFileImpl) Exists() (bool, error) {
	bearerTokenFolderPath := getBearerTokensFolderPath(file.galasaHome)
	bearerTokenFilePath := filepath.Join(bearerTokenFolderPath, file.baseFileName)
	isExists, err := file.fileSystem.Exists(bearerTokenFilePath)
	return isExists, err
}
