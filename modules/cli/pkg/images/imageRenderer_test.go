/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"encoding/json"
	"fmt"
	"path/filepath"
	"strconv"
	"testing"

	"github.com/galasa-dev/cli/pkg/embedded"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

// ----------------------------------------------------
// Utility functions
// ----------------------------------------------------
func assertTerminalImageMatchesExpectedSnapshot(t *testing.T, actualImageBytes []byte) {
	compareImage(t, actualImageBytes, "./testdata/renderedimages/images-to-compare", t.Name()+".png")
}

func createTextField(row int, column int, text string, textColor string) TerminalField {
	fieldContents := FieldContents{Text: text}

	return TerminalField{
		Row:                 row,
		Column:              column,
		Unformatted:         false,
		FieldProtected:      false,
		FieldNumeric:        false,
		FieldDisplay:        true,
		FieldIntenseDisplay: false,
		FieldSelectorPen:    false,
		FieldModified:       false,
		Contents:            []FieldContents{fieldContents},
		ForegroundColor:     textColor,
	}
}

func createTerminal(id string, terminalImage TerminalImage) Terminal {
	return Terminal{
		Id:     id,
		Images: []TerminalImage{terminalImage},
	}
}

// ----------------------------------------------------
// Tests
// ----------------------------------------------------
func TestRenderEmptyTerminalRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFieldRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{createTextField(10, 13, "single text field in the middle", "d")},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithSmallerSizeRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    18,
		Columns: 66,
	}

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{createTextField(9, 15, "this terminal should be 66x18", "d")},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFieldAtOriginRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{createTextField(0, 0, "^ this is the origin (top left)", "d")},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFieldAtTopRightRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	textField := createTextField(10, 20, "The '^' should be at the top right", "d")
	topRightField := createTextField(0, 79, "^", "d")

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{topRightField, textField},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFieldAtBottomLeftRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	textField := createTextField(10, 20, "The 'v' should be at the bottom left", "d")
	bottomLeftField := createTextField(26, 0, "v", "d")

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      false,
		Aid:          "my-aid",
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{bottomLeftField, textField},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFieldAtBottomRightRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	textField := createTextField(10, 20, "The 'v' should be at the bottom right", "d")
	bottomRightField := createTextField(26, 79, "v", "d")

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{bottomRightField, textField},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFullRowRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	textField := createTextField(10, 0, "0          1          2          3          4          5          6          7", "d")
	fullRowField := createTextField(11, 0, "01234567890123456789012345678901234567890123456789012345678901234567890123456789", "d") //This is a unit test value, not a secret //pragma: allowlist secret

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{fullRowField, textField},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, false)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithFullColumnRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	rows := 26
	terminalSize := TerminalSize{
		Rows:    rows,
		Columns: 80,
	}

	terminalFields := make([]TerminalField, 0)
	for i := 0; i < rows; i++ {
		terminalFields = append(terminalFields, createTextField(i, 0, strconv.Itoa(i), "d"))
	}
	terminalFields = append(terminalFields, createTextField(10, 20, "Each of the 26 rows should have a number in", "d"))

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       terminalFields,
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithWrappingRowRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	textField := createTextField(10, 0, "The next row should wrap around and continue on the row below it", "d")
	wrappedField := createTextField(11, 20, "0123456789012345678901234567890123456789012345678901234567890123456789", "d") //This is a unit test value, not a secret //pragma: allowlist secret

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{wrappedField, textField},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminaColorsRenderOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	defaultField := createTextField(10, 20, "This is the default color", "d")
	neutralField := createTextField(11, 20, "This is the neutral color", "n")
	redField := createTextField(12, 20, "This is red", "r")
	greenField := createTextField(13, 20, "This is green", "g")
	blueField := createTextField(14, 20, "This is blue", "b")
	pinkField := createTextField(15, 20, "This is pink", "p")
	turquoiseField := createTextField(16, 20, "This is turquoise", "t")
	yellowField := createTextField(17, 20, "This is yellow", "y")
	unknownColorField := createTextField(18, 20, "This is unknown, should render using the default color", "blah")

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields: []TerminalField{
			defaultField,
			neutralField,
			redField,
			greenField,
			blueField,
			pinkField,
			turquoiseField,
			yellowField,
			unknownColorField,
		},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminaUnicodeTextRendersOk(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	symbolField := createTextField(9, 20, "Symbols: © Ø ® ß ◊ ¥ Ô º ™ € ¢ ∞ § Ω`", "d")
	greekField := createTextField(10, 20, "Greek: Χαίρετε", "d")
	japaneseField := createTextField(11, 20, "Japanese: こんにちは", "d")
	russianField := createTextField(12, 20, "Russian: Здравствуйте", "d")
	chineseField := createTextField(13, 20, "Chinese: 你好", "d")
	koreanField := createTextField(14, 20, "Korean: 안녕하세요", "d")

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields: []TerminalField{
			symbolField,
			greekField,
			japaneseField,
			russianField,
			chineseField,
			koreanField,
		},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}

func TestRenderTerminalWithMissingFontDefaultsToMonoFont(t *testing.T) {
	// Given...
	fs := files.NewMockFileSystem()
	tempDir, _ := fs.MkTempDir()

	embeddedFs := embedded.NewMockReadOnlyFileSystem()

	imageId := t.Name()
	terminalSize := TerminalSize{
		Rows:    26,
		Columns: 80,
	}

	terminalImage := TerminalImage{
		Id:           imageId,
		Sequence:     1,
		Inbound:      true,
		ImageSize:    terminalSize,
		CursorRow:    0,
		CursorColumn: 0,
		Fields:       []TerminalField{createTextField(10, 13, "this text should be visible", "d")},
	}

	terminal := createTerminal(imageId, terminalImage)
	terminalJsonBytes, _ := json.Marshal(terminal)

	imageFileWriter := NewImageFileWriter(fs, tempDir, true)
	imageRenderer := NewImageRenderer(embeddedFs)

	// When...
	err := imageRenderer.RenderJsonBytesToImageFiles(terminalJsonBytes, imageFileWriter)
	assert.Nil(t, err, "Should have created a PNG image without error")

	expectedPngFileName := fmt.Sprintf("%s-%05d.png", terminal.Id, terminalImage.Sequence)
	imageBytes, err := fs.ReadBinaryFile(filepath.Join(tempDir, expectedPngFileName))
	assert.Nil(t, err, "PNG file should exist and should be readable")

	// Then...
	assertTerminalImageMatchesExpectedSnapshot(t, imageBytes)
}
