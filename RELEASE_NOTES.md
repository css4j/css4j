# css4j version 5.0 Release Notes

### October 5, 2024

<br/>

## Highlights

### API refactors

The API was adjusted so it can provide more functionality to other implementations,
like EchoSVG's.

<br/>

## Detail of changes

- NSAC: do not account for the input serialization of a value in `equals()` and `hashCode()`.
- NSAC: use try-with-resources in CSSParser.parseStyleSheet(String).
- CSSOM: deprecate `AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE` and `STRING_DOUBLE_QUOTE` for removal. Use the `CSSStyleSheetFactory` replacements instead.
- CSSOM: make style sheet content-type check stricter for http/https.
- CSSOM: new `CSSMathValue` and `CSSNumberValue` interfaces.
- CSSOM: suppress a warning in `DirectionalityHelper`.
- CSSOM: push `isPrimitiveValue()` to `CSSValue`.
- CSSOM: add `clone()` to `CSSTypedValue`.
- CSSOM: make the handling of math expressions and media queries independent of the `CSSValue` implementation.
- CSSOM: add a default `getMinifiedCssText()` method to `CSSValue`.
- CSSOM: set a default implementation for `CSSValue.getMinifiedCssText(String)`.
- CSSOM: remove deprecated `inlineStyleError()` from `SheetErrorHandler`.
- CSSOM: `AbstractCSSCanvas` no longer has a `CSSDocument` field.
- CSSOM: refactor the `@supports`-related interfaces to not use `CSSValue`s.
- CSSOM: use `getMedia()` instead of the deprecated `getMediaText()` in `@import` and `@media` rules.
- Refactor the parsing of supports conditions so it uses a context interface instead of an `AbstractCSSStyleSheet`.
- Move unit string-to-numeric conversions from `ParseHelper` to new `UnitStringToId`.
- Reorder a few imports.
- Upgrade JUnit to version 5.11.1.
- Upgrade to checkstyle 10.18.1.
- Upgrade Gradle wrapper to 8.10.2.
