/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package cmd

import (
	"encoding/base64"
	"errors"
	"fmt"
	"io/ioutil"

	"galasa.dev/buildUtilities/pkg/galasayaml"
	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"
)

var (
	mavenCmd = &cobra.Command{
		Use:   "maven",
		Short: "maven related build commands",
		Long:  "Various commands to interact with maven artifacts",
	}

	mavenRepositoryUrl string
	mavenUsername      string
	mavenPassword      string
	mavenCredentials   string
)

func init() {
	mavenCmd.PersistentFlags().StringVarP(&mavenRepositoryUrl, "repository", "", "", "repository")
	mavenCmd.PersistentFlags().StringVarP(&mavenUsername, "username", "", "", "username")
	mavenCmd.PersistentFlags().StringVarP(&mavenPassword, "password", "", "", "password")
	mavenCmd.PersistentFlags().StringVarP(&mavenCredentials, "credentials", "", "", "credentials file")

	mavenCmd.MarkPersistentFlagRequired("repository")

	rootCmd.AddCommand(mavenCmd)
}

func mavenGetBasicAuth() (string, error) {
	if mavenUsername == "" && mavenPassword == "" && mavenCredentials == "" {
		return "", errors.New("Username/password or credentials file has not been provided")
	}

	if mavenUsername != "" && mavenPassword == "" {
		return "", errors.New("Username provided but no password")
	}

	if mavenUsername == "" && mavenPassword != "" {
		return "", errors.New("Password provided but no username")
	}

	if mavenCredentials != "" && (mavenUsername != "" || mavenPassword != "") {
		return "", errors.New("Credentials file provided, but also username or password")
	}

	if mavenCredentials != "" {
		var creds galasayaml.Credentials

		b, err := ioutil.ReadFile(mavenCredentials)
		if err != nil {
			panic(err)
		}

		err = yaml.Unmarshal(b, &creds)
		if err != nil {
			panic(err)
		}

		if creds.Username == "" {
			return "", errors.New("Username not provided in credentials file")
		}

		if creds.Password == "" {
			return "", errors.New("Password not provided in credentials file")
		}

		mavenUsername = creds.Username
		mavenPassword = creds.Password //Not a secret but logic for a secret //pragma: allowlist secret 
	}

	auth := fmt.Sprintf("%v:%v", mavenUsername, mavenPassword) //Not a secret but logic for a secret //pragma: allowlist secret 
	sEnc := base64.StdEncoding.EncodeToString([]byte(auth))

	basicAuth := fmt.Sprintf("Basic %v", sEnc) //Not a secret but logic for a secret //pragma: allowlist secret 

	return basicAuth, nil
}
