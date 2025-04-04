/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package spi

import "time"

type TimeService interface {
	Now() time.Time
	Sleep(duration time.Duration)
}
