/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

// Environment is a thin interface layer above the os package which can be mocked out
type Environment interface {
	GetEnv(propertyName string) string
	GetUserName() (string, error)
}
