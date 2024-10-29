/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package openapi2beans_errors

import (
	"errors"
	"fmt"
	"log"
)

func NewError(template string, params ... interface{}) error {
	msg := fmt.Sprintf(template, params...)
	log.Println(msg)
	err := errors.New(msg)
	return err
}