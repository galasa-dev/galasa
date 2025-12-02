/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package launcher

// ----------------------------------------------------------------------------------
// ResourceCleanupLauncher a launcher that kicks off resource cleanup services.
type ResourceCleanupLauncher interface {

	// RunResourceCleanup launches resource cleanup
	RunResourceCleanup() error
}
