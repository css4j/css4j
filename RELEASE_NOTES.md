# css4j version 6.0 Release Notes

### May 16, 2025

<br/>

## Highlights

### CSS nesting

CSS nesting is widely used in today's web sites, see MDN's [Using CSS nesting](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_nesting/Using_CSS_nesting) for information on how to use it. The new lexical parser is intended to be compatible with the latest specification, which was implemented by the major web browsers in late 2024 (Chrome 130, Firefox 132 and Safari 18.2).

The draft of the Syntax 3 specification explicitly requires the use of 'marks' (like the the `mark()` and `reset()` methods of Java's `InputStream`) to parse nested rules but this library so far manages to do without that (which should benefit performance). If you find any issue, please report.

Relevant specifications:

- https://drafts.csswg.org/css-nesting-1/
- https://drafts.csswg.org/css-syntax-3/

<br/>

### Comments in values

Both `LexicalUnit` and `CSSValue` now have `getPrecedingComments()` and `getTrailingComments()` methods. Please note that in some cases it is difficult to determine whether a comment is intended for a value or for another.

If your use case does not care about comments, you may want to use the `VALUE_COMMENTS_IGNORE` NSAC parser flag to avoid the small performance hit. The new `setFlag(Flag)` and `unsetFlag(Flag)` factory methods may help, in addition to the `EnumSet<Parser.Flag>` constructors that all the factories already had.

<br/>

## Detail of changes

- CSS nesting.
- agent: remove the archaic cookie management code which was deprecated for removal.
- agent: remove the deprecated `LogUserAgentErrorHandler`.
- agent: deprecate for removal the legacy `CSSCanvas` and `Viewport` in the `agent` package.
- NSAC: support `url()` modifiers.
- NSAC: support the `:state` pseudo-class.
- NSAC: parse and serialize the `:host` and `:host-context` pseudo-classes (no shadow DOM so never match).
- NSAC: parse and serialize the `::part` pseudo-element (no shadow DOM so never matches).
- NSAC: support the `anchor-size()` function (not yet in the box model).
- NSAC: be lenient to unknown pseudo-classes with non-identifier arguments.
- NSAC: deprecate `CSSHandler`'s `startViewport()` and `endViewport()` for removal.
- NSAC/CSSOM: include `env()` in the dimensional analysis of expressions.
- NSAC/CSSOM: add `getPrecedingComments()` and `getTrailingComments()` for values.
- CSSOM: `AbstractCSSRule.getOrigin()` now returns an `int`, to accommodate new layers.
- CSSOM: deprecate `CSSStyleSheet.createViewportRule()` for removal.
- CSSOM: remove primitive value types `ATTR` and `VAR`, which were deprecated for removal (do not confuse them with the lexical types which are still valid).
- CSSOM: remove the long-deprecated `STRING_SINGLE_QUOTE` and `STRING_DOUBLE_QUOTE` constants from `AbstractCSSStyleSheetFactory`.
- CSSOM: add `setFlag(Flag)` and `unsetFlag(Flag)` to `AbstractCSSStyleSheetFactory`.
- CSSOM: add `setErrorHandler(SheetErrorHandler)` to `AbstractCSSStyleSheet`.
- CSSOM: add two protected methods for default parser instantiation in `ValueFactory`.
- CSSOM: accept `calc()` in the minified serialization of `getPropertyValue("margin")`
- CSSOM: add the `margin-inline` and `padding-inline` shorthands.
- CSSOM: make the `transition` shorthand lenient to zero values.
- CSSOM: support recent `flex-basis` identifiers in shorthand decompositions.
- CSSOM: switch to `raw-string` as an `attr()` attribute type instead of `string`.
- CSSOM: use minified longhand serialization in `getPropertyValue("background")` in cases where it wasn't used.
- CSSOM: ignore shorthand unassigned values with IE hacks in `DefaultStyleDeclarationErrorHandler`.
- DOM: be more lenient in accepting URIs.
- DOM wrapper: allow `element.setTextContent(String)`.
- Upgrade to JUnit 5.12.2.
- Upgrade Gradle wrapper to 8.14.
- Upgrade to checkstyle 10.23.0.
