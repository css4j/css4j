# css4j version 5.3 Release Notes

### March 31, 2025

<br/>

## Highlights

### Improved CSS function management

- Function names are now converted to a canonical form (mostly lowercase, except for names like `rotateX` or `skewY`).
- New type identifiers for many functions.
- New `<easing-function>` syntax.
- Add `LexicalUnit.shallowMatch(CSSValueSyntax)`, use in shorthand decompositions.
- Prefixed functions are processed separately and no longer used in style computations.
- Allow trailing comma in function arguments, see CSSWG issue 4968.
- Added method `getContextIndex()` to `LexicalUnit`.
- Undeprecate `CSSMathFunctionValue.MathFunction`, keep backwards compatibility broken by 5.2.

### `CSSEnvVariableValue` values

`CSSEnvVariableValue` was updated to the latest specification which allows indices.

In web browsers, `env()` is substituted at parse time. But given the multiplicity
of use cases for this library, the substitution is done at computed-value time
and this requires a CSSOM value interface.

### DOM wrapper

A few improvements. For example, insert/remove/replace/append child operations
are now allowed in elements.

<br/>

## Detail of changes

- Undeprecate `CSSMathFunctionValue.MathFunction`, keep backwards compatibility broken by 5.2.
- agent: deprecate archaic cookie management code for removal.
- NSAC: convert function names to a canonical form (mostly lowercase, except for names like `rotateX` or `skewY`).
- NSAC: allow trailing comma in function arguments, see CSSWG issue 4968.
- NSAC: add method `getContextIndex()` to `LexicalUnit`. It may be useful to implementations.
- NSAC: add `LexicalUnit.shallowMatch(CSSValueSyntax)`, use in shorthand decompositions.
- NSAC,CSSOM: new type identifiers for many functions, including the `circle`, `ellipse`, `inset`, `path`, `polygon`, `shape` and `xywh` functions from CSS Shapes Module.
- NSAC,CSSOM: new `<easing-function>` syntax.
- NSAC,CSSOM: prefixed functions are processed separately and no longer used in style computations.
- CSSOM: `CSSEnvVariableValue` update.
- CSSOM: use the syntax match infrastructure in shorthand decomposition and gradients.
- CSSOM: stop using `StyleDatabase.getWidthSize()` in `SimpleBoxModel`.
- CSSOM: use the `BaseCSSStyleSheetFactory` class to load the default UA sheets. This avoids potential classloader issues when the factory is subclassed.
- CSSOM: clamp RGB conversion in `HSLColorValue` (HSL colors belong to the sRGB color space, so in principle it is not needed to clamp. But clamp for safety anyway, in case that some numeric inaccuracy causes out-of-gamut mappings).
- DOM wrapper: a few improvements, especially to element child handling.
- DOM: stricter hierarchy checks in the native DOM.
- Deprecation clean-up: Do not use java.net.URL() constructors.
- Tests: add a couple of no-EntityResolver tests to `XMLDocumentBuilderTest`.
- Tests: more URI management tests in the DOM & wrapper.
- Code clean-up & formatting.
- Gradle: move dependency versions to a separate properties file.
- Add a SECURITY.md.
- Upgrade to JUnit 5.12.1.
- Upgrade to TokenProducer 3.1.
- Upgrade to checkstyle 10.22.0.
- Upgrade Gradle wrapper to 8.13.
