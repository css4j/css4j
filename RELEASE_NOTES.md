# css4j version 4.2 Release Notes

### September 15, 2023

<br/>

## Highlights

### Conditional Rules Module Level 4

Recent browsers implement [CSS Conditional Rules Module Level 4](https://www.w3.org/TR/css-conditional-4/),
and its `selector()` function is used by several high-profile web sites including Github. Now this
library supports it.

<br/>

### Added `setUserStyleSheet(String, Reader)` to `CSSStyleSheetFactory`

In previous versions, the `CSSStyleSheetFactory.setUserStyleSheet(Reader)` method
could be used to set the user style sheet, but had the disadvantage that it did not
set the sheet's URI. The new method allows supplying the URI and/or a `Reader` with
the style sheet contents.

<br/>

### `Parser.parseStyleSheet(String)` Content-Type check

Now `Parser.parseStyleSheet(String)` rejects connections that have an invalid
`Content-Type` when connecting to the supplied URL, as mandated by the specification.
**Beware that this could produce backwards-compatibility issues** if you are a direct user
of that method (the CSS Object Model was already doing that check).

<br/>

### DOM: spec update

The HTML support in the native DOM was updated to not produce errors (when error checking
is strict) for cases accepted by the latest HTML specification. The strict error checking
now works better in general, and the `XMLDocumentBuilder` now detects the error mode from
the DOM implementation setting, if instantiated from the `DOMImplementation` constructor.

<br/>

### Convenience minification class

The `Minify` class in the `io.sf.carte.doc.style.css.util` package is an easy way to
minify CSS, providing a static `minifyCSS(String)` method as well as a `main(String[])`
one.

<br/>

## Detail of changes

- Implement CSS Conditional Rules Module Level 4.
- Add a convenience minification class.
- NSAC: check for the correct content type in `Parser.parseStyleSheet(String)`.
- NSAC: add a flags constructor to `CSSParser`.
- NSAC: untie `@supports` condition processing from the `CSSValue` API.
- NSAC: allow overriding the `SupportsConditionFactory` in the parser, remove 
  deprecated methods from `DeclarationCondition`.
- NSAC impl.: check for ASCII digits instead of using `Character.isDigit`.
- DOM: allow META elements that have the `name` attribute.
- DOM: attempt to obtain the error checking mode from the DOM implementation in 
  `XMLDocumentBuilder`.
- DOM: tie a couple of HTML checks to strict error checking, improve error 
  messages.
- DOM, DOM wrapper: handle also embedded sheets when setting and enabling sets 
  by title.
- CSSOM: add `setUserStyleSheet(String, Reader)` to `CSSStyleSheetFactory`.
- CSSOM: less strict check for CSS files without Content-Type.
- CSSOM: `place-self` shorthand processing was missing some valid 
  `justify-self` identifier values.
- CSSOM: fix removing a style sheet by title from the style sheet list.
- CSSOM: assign initial values and inheritance to `math-depth` and `math-style`.
- CSSOM: improve the mapping from legacy HTML presentational attributes to 
  style.
- CSSOM: fix a bug and enable the minified serialization of computed value of 
  `grid-template`.
- CSSOM: check for null condition in `@supports` rules.
- Agent: reduce default connection timeout to 15 seconds.
- More work on the optimised shorthand serialization (not yet in public API).
- Convert iterator and array loops to advanced for loop when appropriate.
- Tests: additional tests and improvements.
- Javadoc: various improvements.
- Upgrade to tokenproducer 3.0.
- Upgrade to xml-dtd 4.2.1.
- Upgrade to Jazzer 0.20.1.
- Upgrade to Github actions/checkout@v4.
- Remove outdated css support table from README.
