# css4j version 5.2 Release Notes

### February 23, 2025

<br/>

## Highlights

### Better handling of mathematical functions

The handling of mathematical functions is now much faster, and the `round`, `mod`, `rem`, `log` and `exp` functions from Values 4 were implemented.

The library is now on parity with the CSS numeric functions supported by modern web browsers.

### `src()` values

The `src()` function is now considered a valid URL value, and therefore matches `<url>` and `<image>`.

### Update of `background` and `mask` shorthands

Both shorthand properties now behave according to the latest specification, supporting all the new identifiers in `background-clip` and `mask-clip`.

### Values of `border-width` identifiers

The main browsers consistently use the same figures for the computed values of border-width's `thin`, `thick` and `medium` identifiers, which were subsequently defined in May 2022.

This leads to the deprecation of `StyleDatabase.getWidthSize`.

<br/>

## Detail of changes

- NSAC,CSSOM: support the `round`, `mod`, `rem`, `log` and `exp` functions from Values 4.
- NSAC,CSSOM: more efficient handling of functions.
- NSAC,CSSOM: support `src()` values.
- CSSOM: add method `getFloatValue()` to `CSSTypedValue`, to retrieve raw value.
- CSSOM: implement `Cloneable` in `StringList` and `CSSFontFeatureValuesMap` implementation.
- CSSOM: add `ShorthandDatabase.getInstance(ClassLoader)`.
- CSSOM: deprecate `StyleDatabase.getWidthSize`. The `thin`, `thick` and `medium` identifiers have fixed values since May 2022, see CSSWG issue 7254.
- CSSOM: support `text` and `border-area` as `background-clip` values.
- CSSOM: support `border`, `padding`, `content` and `text` as `mask-clip` values.
- CSSOM: ignore case when optimizing `background-position` in `getPropertyValue("background")`.
- CSSOM: avoid unlikely NPE when optimizing `getPropertyValue("background")` of a declared style when `background-origin` is not set.
- CSSOM: avoid unlikely NPE when optimizing `getPropertyValue("mask")` of a declared style when `mask-origin` is not set.
- CSSOM: other updates to `background` and `mask` shorthands.
- CSSOM: use private constructors in a few singleton classes.
- Agent: use a more modern user agent identification string.
- Tests: clean up HTMLDocumentTest.
- Upgrade Jazzer to 0.24.0.
- Upgrade to checkstyle 10.21.2.
- README: use the assignment operator in the Gradle example.
