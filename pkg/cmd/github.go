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
	githubCmd = &cobra.Command{
		Use:   "github",
		Short: "github related build commands",
		Long:  "Various commands to interact with GitHub to help the build pipeline along",
	}

	githubRepository  string
	githubUsername    string
	githubPassword    string
	githubCredentials string
)

func init() {
	githubCmd.PersistentFlags().StringVarP(&githubRepository, "repository", "", "", "repository")
	githubCmd.PersistentFlags().StringVarP(&githubUsername, "username", "", "", "username")
	githubCmd.PersistentFlags().StringVarP(&githubPassword, "password", "", "", "password")
	githubCmd.PersistentFlags().StringVarP(&githubCredentials, "credentials", "", "", "credentials file")

	githubCmd.MarkPersistentFlagRequired("repository")

	rootCmd.AddCommand(githubCmd)
}

func githubGetBasicAuth() (string, error) {
	if githubUsername == "" && githubPassword == "" && githubCredentials == "" {
		return "", errors.New("Username/password or credentials file has not been provided")
	}

	if githubUsername != "" && githubPassword == "" {
		return "", errors.New("Username provided but no password")
	}

	if githubUsername == "" && githubPassword != "" {
		return "", errors.New("Password provided but no username")
	}

	if githubCredentials != "" && (githubUsername != "" || githubPassword != "") {
		return "", errors.New("Credentials file provided, but also username or password")
	}

	if githubCredentials != "" {
		var creds galasayaml.Credentials

		b, err := ioutil.ReadFile(githubCredentials)
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

		githubUsername = creds.Username
		githubPassword = creds.Password //Not a secret but logic for a secret //pragma: allowlist secret 
	}

	auth := fmt.Sprintf("%v:%v", githubUsername, githubPassword) //Not a secret but logic for a secret //pragma: allowlist secret 
	sEnc := base64.StdEncoding.EncodeToString([]byte(auth))

	basicAuth := fmt.Sprintf("Basic %v", sEnc) //Not a secret but logic for a secret //pragma: allowlist secret 

	return basicAuth, nil
}
