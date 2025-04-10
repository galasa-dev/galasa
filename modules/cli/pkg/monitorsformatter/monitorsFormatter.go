/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package monitorsformatter

import (
	"github.com/galasa-dev/cli/pkg/galasaapi"
)

// Displays monitors in the following format:
// name                             kind:                        is-enabled
// _system-certificate-monitor      GalasaCertificateMonitor     true      
// _system-resource-cleanup-monitor GalasaResourceCleanupMonitor true      
// myCustomResourceMonitor          GalasaResourceCleanupMonitor true      
//
// Total: 3
// -----------------------------------------------------
// MonitorsFormatter - implementations can take a collection of monitors
// and turn them into a string for display to the user.
const (
	HEADER_MONITOR_NAME       = "name"
	HEADER_MONITOR_KIND       = "kind"
	HEADER_MONITOR_IS_ENABLED = "is-enabled"
)

type MonitorsFormatter interface {
	FormatMonitors(monitors []galasaapi.GalasaMonitor) (string, error)
	GetName() string
}
