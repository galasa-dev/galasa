# zOS 3270 Manager - Gherkin temporary ALPHA documentation

The following are the Gherkin statements that the zOS 3270 Manager currently supports.  This documentation will only exist until the doc site supports the Gherkin reference.

NOTE, the spacing and capitalisation must be exact at this time.  We will try to add flexibility soon.

### Obtaining a Terminal for use in a feature file

`GIVEN a terminal`

`GIVEN a terminal with id of A`

`GIVEN a terminal tagged PRIMARY`

`GIVEN a terminal with id of A tagged PRIMARY`


There are 4 flavours to the "given a terminal".  you can specify an ID for a terminal so that a feature file can operate multiple terminals in the same feature file.  You can also specify a zOS image tag to influence which image the terminal connects to.

The default ID is `A` and the default tag is `PRIMARY`.

In all the following statements,  where you see `terminal` you can leave as default ID of `A` or code `terminal B` to point to a different terminal.

### Move Cursor
  
`AND move terminal cursor to field "xxxxxx"`

Where `xxxxxx` is the text in an protected or unprotected field on the screen.

`AND move terminal A cursor to field "xxxxxx"`

As above, but for the terminal with an id of `A`

### Terminal keys

`AND press terminal key TAB`

`AND press terminal key CLEAR`

`AND press terminal key ENTER`

`AND press terminal key BACKTAB`

`AND press terminal key PFxx` where `xx` is the PF number

`AND press terminal A key ENTER` where `A` is the id of the terminal to use.

### Type something

`AND type "xxxxxx" on terminal` where `xxxxx` is what you want to type where the cursor is

`AND type "xxxxxx" on terminal A` where `A` is the id of the terminal to use.

`AND type "xxxxxx" on terminal in field labelled "yyyyyy"` where `xxxxxx` is the what you want to type,  `yyyyyy` is the field label.  WARNING, this will move the cursor.  It will locate ANY text `yyyyyy` and then press TAB and then type.

`AND type "xxxxxx" on terminal A in field labelled "yyyyyy"` where `A` is the id of the terminal to use.

### Wait for the keyboard to unlock

`AND wait for terminal keyboard`

`AND wait for terminal A keyboard`

### Wait for text on the screen

`THEN wait for "xxxxxx" in any terminal field`

This will wait for the text to appear on the screen on any screen update.  WARNING, the keyboard may not be unlocked when this statement finishes.

`THEN wait for "xxxxxx" in any terminal A field` where `A` is the id of the terminal to use.

### Check single text on screen

`THEN check "xxxxxx" appears only once on terminal`

This will search the screen for text `xxxxxx` and ensure it occurs once.  This is immediate, will not wait for updates.

`THEN check "xxxxxx" appears only once on terminal A` where `A` is the id of the terminal to use.

