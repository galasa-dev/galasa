/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}};

/**
 * I{{.CapitalizedManagerName}}Manager
 *
 * Manager interface for other managers that depend on this manager.
 * Keep this minimal - most functionality should be in I{{.CapitalizedManagerName}}Resource.
 */
public interface I{{.CapitalizedManagerName}}Manager {
    // TODO: Add manager-level methods if needed by dependent managers
}

