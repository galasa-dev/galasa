/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

type GalasaHome interface {
	GetNativeFolderPath() string
	GetUrlFolderPath() string
}
