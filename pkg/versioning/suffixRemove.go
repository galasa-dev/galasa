/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package versioning

import (
	"galasa.dev/buildUtilities/pkg/utils"
)

func SuffixRemoveExecute(fs utils.FileSystem, sourceCodeFolderPath string) error {
	var err error

	var modules []Module
	modules, err = getModules(fs, sourceCodeFolderPath)
	if err == nil {
		err = setSuffixOnAllModules(fs, modules, "")
	}

	return err
}
