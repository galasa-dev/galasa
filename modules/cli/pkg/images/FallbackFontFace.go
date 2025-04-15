/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package images

import (
	"image"
	"io/fs"

	"github.com/galasa-dev/cli/pkg/embedded"
	"golang.org/x/image/font"
	"golang.org/x/image/font/opentype"
	"golang.org/x/image/math/fixed"
)

// Implementation of the font.Face interface
type FallbackFontFace struct {
	primary   font.Face
	fallbacks []font.Face
}

// Loads a .ttf or .otf font from the embedded filesystem
func loadFont(fs embedded.ReadOnlyFileSystem, fontFilePath string) (font.Face, error) {
	var err error
	var fontBytes []byte
	var opentypeFont *opentype.Font
	var fontFace font.Face

	fontBytes, err = fs.ReadFile(fontFilePath)
	if err == nil {
		opentypeFont, err = opentype.Parse(fontBytes)
		if err == nil {
			fontFace, err = opentype.NewFace(opentypeFont, nil)
		}
	}
	return fontFace, err
}

// Loads a .ttf or .otf font from the embedded filesystem
func loadFontsFromDirectory(fileSystem embedded.ReadOnlyFileSystem, fontDirectoryPath string) ([]font.Face, error) {
	var err error
	var fontFiles []fs.DirEntry
	fonts := make([]font.Face, 0)

	fontFiles, err = fileSystem.ReadDir(fontDirectoryPath)
	if err == nil {
		for _, fontFile := range fontFiles {
			var fontFace font.Face

			// Don't use filepath.Join to build path, as that uses the OS file separator, and
			// we need to use the file separator from the file system, which may be different.
			// eg: The embedded file system in golang uses '/' even when on Windows...
			fontFilePath := fontDirectoryPath + fileSystem.GetFileSeparator() + fontFile.Name()
			fontFace, err = loadFont(fileSystem, fontFilePath)
			if err == nil {
				fonts = append(fonts, fontFace)
			}
		}
	}
	return fonts, err
}

func NewFallbackFontFace(primaryFont font.Face) *FallbackFontFace {
	return &FallbackFontFace{primary: primaryFont}
}

func (f *FallbackFontFace) AddFallbackFont(fontFace font.Face) {
	f.fallbacks = append(f.fallbacks, fontFace)
}

// ----------------------------------------------
// Functions implementing the font.Face interface
// ----------------------------------------------
func (f *FallbackFontFace) Close() error {
	err := f.primary.Close()
	for _, fallback := range f.fallbacks {
		fallbackErr := fallback.Close()
		if fallbackErr != nil && err == nil {
			err = fallbackErr
			break
		}
	}
	return err
}

func (f *FallbackFontFace) Metrics() font.Metrics {
	return f.primary.Metrics()
}

func (f *FallbackFontFace) Kern(r0 rune, r1 rune) fixed.Int26_6 {
	kern := f.primary.Kern(r0, r1)
	if kern == 0 {
		for _, fallback := range f.fallbacks {
			fallbackKern := fallback.Kern(r0, r1)
			if fallbackKern != 0 {
				kern = fallbackKern
				break
			}
		}
	}
	return kern
}

func (f *FallbackFontFace) Glyph(dot fixed.Point26_6, r rune) (image.Rectangle, image.Image, image.Point, fixed.Int26_6, bool) {
	rect, img, point, advance, ok := f.primary.Glyph(dot, r)
	if !ok {
		for _, fallback := range f.fallbacks {
			rect, img, point, advance, ok = fallback.Glyph(dot, r)
			if ok {
				break
			}
		}
	}
	return rect, img, point, advance, ok
}

func (f *FallbackFontFace) GlyphAdvance(r rune) (fixed.Int26_6, bool) {
	advance, ok := f.primary.GlyphAdvance(r)
	if !ok {
		for _, fallback := range f.fallbacks {
			fallbackAdv, fallbackOk := fallback.GlyphAdvance(r)
			if fallbackOk {
				advance, ok = fallbackAdv, fallbackOk
				break
			}
		}
	}
	return advance, ok
}

func (f *FallbackFontFace) GlyphBounds(r rune) (bounds fixed.Rectangle26_6, advance fixed.Int26_6, ok bool) {
	bounds, adv, ok := f.primary.GlyphBounds(r)
	if !ok {
		for _, fallback := range f.fallbacks {
			fallbackBounds, fallbackAdvance, fallbackOk := fallback.GlyphBounds(r)
			if ok {
				bounds, adv, ok = fallbackBounds, fallbackAdvance, fallbackOk
				break
			}
		}
	}
	return bounds, adv, ok
}
