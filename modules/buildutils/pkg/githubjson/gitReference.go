/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package githubjson

type Reference struct {

	Ref       string `json:"ref"`
	NodeId    string `json:"node_id"`
	Url       string `json:"url"`
	Object    ReferenceObject `json:"object"`

}

type ReferenceObject struct {

	Type      string `json:"type"`
	Sha       string `json:"sha"`
	Url       string `json:"url"`

}

type NewReference struct {
	Ref       string `json:"ref"`
	Sha       string `json:"sha"`
	Force     bool   `json:"force"`
}
