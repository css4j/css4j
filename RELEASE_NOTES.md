# css4j version 4.0 Release Notes

### March 30, 2023

<br/>

## Highlights

### Parsing and serialization of colors updated to latest Level 4 specification

Chrome 111 shipped with support for Level 4 colors (which css4j has supported for a while),
albeit implementing a subset of the latest color syntaxes (some did not exist when this library
implemented the colors, and also the serializations varied).

Css4j 4.0 accepts the full syntax for `oklab`, `oklch`, `lab`, etc., although the normalized values
(and serialization) are fully compatible with the Chrome subset.

Also, the useful methods `toColorSpace(String)` and `packInValue()` were added to `CSSColor`.

Related to this, the `RGBColorValue` interface was removed. After the introduction of the
Level 4 `color()` function in Chrome, its usage was a crash waiting to happen (the reason:
not all the colors reported as `RGB` were `RGBColorValue`s).

<br/>

### Implemented the `color-mix()` function from Color Level 5

Chrome 111 also ships with that function, and css4j now fully supports it as a color-like value,
including color conversions. Calling `getColor()` performs the color interpolation.

<br/>

### Improved handling of stand-alone mathematical functions

Now mathematical functions like `sin()` or `sign()` can be used directly, outside of a `calc()`
value in almost all contexts. If you deal directly with CSSOM values, please look at the new
`MATH_FUNCTION` value type.

<br/>

### The `rex`, `rch` and `ric` font-relative units

Again catching up with Chrome 111, css4j 4.0 introduces full support for these units, even in computed styles.

<br/>

### Bug fixes

A few crashes found with the [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer) fuzzer were corrected,
as well as other issues.

<br/>

## Detail of changes

- NSAC: make the parser more robust against wrong input (most issues found with Jazzer).
- NSAC: close rules correctly on unexpected EOF.
- NSAC: if no error handler is provided, throw an exception on selector errors in style sheet parsing.
- NSAC: add `ParseHelper.ERR_IO` to report rare internal I/O exceptions.
- NSAC: check that handler is a `DeclarationRuleHandler` in `CSSParser.parseDeclarationRule(Reader)`
- NSAC: trigger an error if a `@property` rule is found nested.
- NSAC: if selector is wrong and there was no start event for `@counter-style`, `@keyframes` and `@page`, do not fire close-rule events.
- NSAC: check validity of `@counter-style` rule names.
- NSAC: remove the `IllegalArgumentException` declaration from `parsePropertyValue(Reader)`.
- NSAC: exception cleanup in media query and support condition parsing.
- NSAC: more efficient memory access in `CSSParser`.
- NSAC & CSSOM: support the `rex`, `rch` and `ric` font-relative units.
- NSAC & CSSOM: update parsing and serialization of colors to latest Level 4 specification.
- NSAC & CSSOM: implement the `color-mix()` function from Level 5.
- NSAC & CSSOM: improve the support for the advanced `attr()` function.
- CSSOM: remove the `RGBColorValue` interface, which was deprecated 2 years ago.
- CSSOM: remove the `ruleIOError` method from the `ErrorHandler` interface. It was deprecated 3 years ago (for removal 2 years ago).
- CSSOM: do not allow errors on `CSSRule.setCssText()`, allow trailing comments.
- CSSOM: add `createSupportsRule(BooleanCondition)` to `CSSStyleSheet`, deprecate `createSupportsRule()`.
- CSSOM: introduce the new `MATH_FUNCTION` value type.
- CSSOM: add method `toColorSpace(String)` to `CSSColor`.
- CSSOM: add method `packInValue()` to `CSSColor`.
- CSSOM: add method `computeUnitType()` to `CSSExpressionValue`.
- CSSOM: improve the accuracy of math function and expression type matching.
- CSSOM: add method `conditionalRuleError` to `SheetErrorHandler`, deprecate for removal `inlineStyleError` which is never called.
- CSSOM: remove the default implementation for `rebuildCascade()` from `CSSDocument`.
- CSSOM: `var()` inside `counter()` and `counters()` wasn't processed correctly (bug).
- CSSOM: do not use a `LenientSystemDefaultValue` for colors in LENIENT mode (prevents a class cast exception).
- CSSOM: for better reproducibility of minified font serialization, only consider font family as initial if it was not specified, when serializing minified computed styles.
- CSSOM: a few newer types weren't added to the `LexicalValue.getFinalType()` logic.
- CSSOM: throw a `DOMException.TYPE_MISMATCH_ERR` if the `counter()` or `counters()` name is of the wrong type.
- CSSOM: the default `line-height` was changed to 1.2.
- CSSOM: avoid NPEs when checking invalid `@supports` conditions.
- CSSOM: use parent rule to retrieve the parent sheet when necessary in `getParentStyleSheet()`.
- CSSOM: remove an unnecessary cast in `ComputedCSSStyle`.
- DOM: add a `clone()` method to `StringList`.
- DOM: slightly speed up the import of elements without attributes.
- DOM wrapper: allow setting, modifying and removing attributes.
- DOM wrapper: improve the serialization of `Attr.toString()` (useful for debugging purposes).
- Avoid NPE in `SyntaxParser.createSimpleSyntax(String)`.
- Tests: convert the tests to JUnit 5.
- Tests: add a Jazzer-based fuzzing test.
- Tests: remove several redundant shorthand checks, add an error/warning check to `ComputedCustomPropertyTest`.
- Tests: exception cleanup in parser tests.
- Tests: refactor the testing for color keywords.
- Tests: remove privileged blocks (Security Manager is deprecated).
- Global simplification of conditional blocks (with a bit of reformatting).
- Imports cleanup.
- Migrate to a Maven/Gradle source directory layout
- Tokenproducer and JCLF are now runtime dependencies.
- Upgrade Gradle wrapper to 8.0.2.
