# css4j version 3.8 Release Notes

### August 21, 2022

<br/>

## Highlights

### Support OKLab and OKLCH from Color Level 4

Those colours are [supported by Safari/Webkit](https://bugs.webkit.org/show_bug.cgi?id=233507)
and have a lot of potential.

See issue #15.

### Level 4 `:lang` pseudoclass supported in computed styles

The library fully supported the `:lang` pseudoclass [from level 3 selectors](https://www.w3.org/TR/selectors-3/#lang-pseudo),
and was able to parse and serialize [level 4](https://www.w3.org/TR/selectors-4/#the-lang-pseudo)
(with range arguments like `:lang(\*-Latn)`). But selectors with level 4 ranges
were not matched when computing the cascade.

The new implementation isn't 100% compliant because it does not support implicit
wildcard matching, although no browser supports that yet.

See #14.

### Native DOM: use `<style>` elements from any namespace for computing styles

Unlike the DOM wrapper, in HTML documents the native DOM did not use (for style
computation) inline styles that were in a different namespace than the document
element's namespace.

Now it uses style elements having any namespace, which matches the current
behaviour of web browsers and makes it easier to support inlined SVG.

### Improvements to the customizable serialization of computed styles

Now the minified serialization can also be configured, and the overall
serialization customizability was improved (_e.g._ in gradients).

For example, if you do the following:

```java
CSSDOMImplementation impl = new CSSDOMImplementation();
// Serialize colors to sRGB
impl.setStyleFormattingFactory(new RGBStyleFormattingFactory());
```
not only the colours in `computedStyle.getCssText()` will be converted to sRGB
but also those in `computedStyle.getMinifiedCssText()`.

### Bug fixes

A few bugs were fixed.

## Detail of changes

- Support OKLab and OKLCH from Color Level 4 (#15)
- NSAC: support HWB colors as NSAC lexical units (#13)
- NSAC impl.: set a 30s timeout in `CSSParser.parseStyleSheet(String)`.
- NSAC impl.: set a 30s timeout in `CSSParser.parseStyleSheet(InputSource)` when 
connecting to URI.
- DOM: use `<style>` elements from any namespace for computing styles.
- CSSOM: support matching level 4 :lang pseudoclass (#14).
- CSSOM: add `getUnitType()` and `fillBoxValues()` to `BoxValues`.
- CSSOM: provide several default implementations in `StyleDatabase` and `CSSCanvas`.
- CSSOM: add customizable minified serialization of computed styles.
- CSSOM: serialize gradients and shorthand images through 
`DeclarationFormattingContext`.
- CSSOM: `prefers-color-scheme` no longer has the `no-preference` value, per
CSSWG resolution of May 27, 2020.
- CSSOM: add initial values for some Transform properties.
- CSSOM: fix a class cast exception in `RGBColorDeclarationFormattingContext`.
- CSSOM: fix a bug in the processing of `@media` rules targeting all media.
- Use `CSSUnit`'s static methods to determine unit types in `ValueFactory `
(refactor).
- Allow comparing to a `CharSequence` in `ParseHelper.equalsIgnoreCase()`
(implementation detail).
- Javadoc: add a module description.
- DOM javadoc overview: configurable ID support was dropped by `[419b0e13]`.
- Add a `RELEASE_HOWTO` with the release steps.
- Tests: use namespace aware processing in `XMLDocumentWrapperTest`.
- Tests: add a few more SVG tests, use a constant for SVG namespace URI.
- Upgrade to xml-dtd 4.1.0.
- Upgrade to carte-util 3.6.0.
- Gradle: upgrade wrapper to 7.5.1
- Actions: switch from 'adopt' distribution to 'temurin'.
- Actions: build packages with Java 17.
- Actions: upgrade `actions/checkout` and `codeql-action` to current versions.
- Actions: bump `actions/setup-java` from 2 to 3.
