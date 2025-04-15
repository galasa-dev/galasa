/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

type Terminal struct {
	Id          string          `json:"id"`
	RunId       string          `json:"runId"`
	Sequence    int             `json:"sequence"`
	Images      []TerminalImage `json:"images"`
	DefaultSize TerminalSize    `json:"defaultSize"`
}

type TerminalImage struct {
	Id           string          `json:"id"`
	Sequence     int             `json:"sequence"`
	Inbound      bool            `json:"inbound"`
	Type         string          `json:"type"`
	ImageSize    TerminalSize    `json:"imageSize"`
	CursorRow    int             `json:"cursorRow"`
	CursorColumn int             `json:"cursorColumn"`
	Aid          string          `json:"aid"`
	Fields       []TerminalField `json:"fields"`
}

type TerminalField struct {
	Row                 int             `json:"row"`
	Column              int             `json:"column"`
	Unformatted         bool            `json:"unformatted"`
	FieldProtected      bool            `json:"fieldProtected"`
	FieldNumeric        bool            `json:"fieldNumeric"`
	FieldDisplay        bool            `json:"fieldDisplay"`
	FieldIntenseDisplay bool            `json:"fieldIntenseDisplay"`
	FieldSelectorPen    bool            `json:"fieldSelectorPen"`
	FieldModified       bool            `json:"fieldModified"`
	ForegroundColor     string          `json:"foregroundColour"`
	BackgroundColor     string          `json:"backgroundColour"`
	Highlight           string          `json:"highlight"`
	Contents            []FieldContents `json:"contents"`
}

type FieldContents struct {
	Characters []string `json:"chars"`
	Text       string   `json:"text"`
}

type TerminalSize struct {
	Rows    int `json:"rows"`
	Columns int `json:"columns"`
}
