/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"regexp"
	"sort"

	"github.com/galasa-dev/cli/pkg/cmd"
	galasaErrors "github.com/galasa-dev/cli/pkg/errors"
	"github.com/spf13/cobra/doc"
)

const (
	TARGET_ERROR_FILE_NAME = "errors-list.md"
)

func main() {
	var targetFolder string
	factory := cmd.NewRealFactory()
	commands, err := cmd.NewCommandCollection(factory)
	if err == nil {
		targetFolder = os.Args[1]
		err = doc.GenMarkdownTree(commands.GetRootCommand().CobraCommand(), targetFolder)
	}

	if err != nil {
		log.Fatal(err)
	}

	renderErrorsToFile(targetFolder, TARGET_ERROR_FILE_NAME)
}

func renderErrorsToFile(targetFolder string, targetFileName string) {

	targetFilePath := fmt.Sprintf("%s/%s", targetFolder, targetFileName)
	f, err := os.Create(targetFilePath)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	w := bufio.NewWriter(f)
	defer w.Flush()

	renderErrorsToWriter(w)
}

func renderErrorsToWriter(writer *bufio.Writer) {

	// Write out the title of the content
	_, err := writer.WriteString("## Errors\n" + "The `galasactl` tool can generate the following errors:\n\n")
	if err != nil {
		log.Fatal(err)
	}

	// Get a sorted list of all the ordinals... so the errors are listed
	// in sort-order.
	ordinals := make([]int, 0, len(galasaErrors.GALASA_ALL_MESSAGES))
	for ordinal := range galasaErrors.GALASA_ALL_MESSAGES {
		ordinals = append(ordinals, ordinal)
	}
	sort.Ints(ordinals)

	// Build a regex so w can recognise %s %v %d characters, and clean them up...
	percentSubstitutionFinder := regexp.MustCompile(`%.`)

	// Write out the list of errors
	for _, messageTypeOrdinal := range ordinals {
		messageType := galasaErrors.GALASA_ALL_MESSAGES[messageTypeOrdinal]

		// Replace any %s %v %d characters with {} as that looks cleaner.
		message := percentSubstitutionFinder.ReplaceAllString(messageType.Template, "{}")

		_, err := writer.WriteString(fmt.Sprintf("- %s\n", message))
		if err != nil {
			log.Fatal(err)
		}
	}

}
