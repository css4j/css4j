# css4j version 3.9 Release Notes

### October 18, 2022

<br/>

## Highlights

### NSAC & CSSOM: More compliant serialization of escaped identifiers

See issue #17.

<br/>

### NSAC & CSSOM: support `<counter>` syntax in `matches(CSSValueSyntax)`

Now you can do, for example:

```java
CSSValueSyntax syntax = (new SyntaxParser()).parseSyntax("<string> | <counter>");
Match match = value.matches(syntax);
```

<br/>

### CSSOM: make `MediaQueryList.getMediaQuery(int)` publicly visible

See issue #19.

<br/>

### CSSOM: add accept-visitor methods to `CSSStyleSheetList`

If you have a list of style sheets that you want to process using the Visitor
pattern, you now can execute the 'accept' method directly on the list.

<br/>

### CSSOM: add `getFirstStyleRule()` and `getStyleRules()` utility methods to `AbstractCSSStyleSheet`

New ways to obtain style rules from a sheet, alternative to the tedious process
of scanning the entire sheet with `item()`, or the Visitor pattern methods.

<br/>

### CSSOM: accept a slash (/) in values that follow the `content` syntax

Until now, a CSS value which contained a slash (`/`) produced an error, unless
it was a shorthand or a prefixed property (ratios in media queries are already
supported, but technically those are media features and not actual properties).

Now, such a value would be accepted if it follows the syntax of the `content`
property.

Note: the full [`content` syntax from the current specification](https://www.w3.org/TR/css-content-3/#content-property)
is not supported, but neither do the web browsers.

<br/>

### DOM: add `rebuildCascade()` to `CSSDocument`

In some cases, the library did not detect that a property value had changed,
and kept using an outdated cascade. With the new `rebuildCascade()` method,
developers can now make sure that the cascade is rebuilt once they are done
modifying the properties.

The interface provides a default implementation so it can be used with older
versions of css4j-dom4j.

<br/>

### NSAC: add `contains()` and `containsAll()` to `SelectorList`

This should make it easier to verify whether a given style rule contains a
selector or a set of them.

<br/>

### Implement CSS Object Model's `CSS.escape()` method

See issue #18.

<br/>

### Bug fixes

A few bugs were fixed.

<br/>

## Detail of changes

- NSAC: add `contains()` and `containsAll()` to `SelectorList`.
- NSAC: trim array-backed selector lists, for memory efficiency.
- NSAC: accept escaped attribute selector values.
- NSAC: accept the `clamp()` function as a valid color component.
- NSAC & CSSOM: support `<counter>` syntax in `matches(CSSValueSyntax)`.
- DOM: add `rebuildCascade()` to `CSSDocument`.
- DOM: implement `setTextContent()` for attributes, PIs and Text/CDATA/Comments.
- DOM: prevent NPEs setting null values to PIs and Text/CDATA/Comment nodes.
- DOM: set the `documentURI` earlier in `XMLDocumentBuilder`. This allows potential DOM document policies to be enforced at parse time.
- DOM: more efficient implementation of `getTextContent()`.
- DOM: avoid NPE in `CSSDocument.setTargetMedium(String)` implementations.
- CSSOM: make `MediaQueryList.getMediaQuery(int)` publicly visible (#19)
- CSSOM: add accept-visitor methods to `CSSStyleSheetList`.
- CSSOM: add `getFirstStyleRule()` and `getStyleRules()` utility methods to `AbstractCSSStyleSheet`.
- CSSOM: support linear color hints in gradients.
- CSSOM: accept a slash (`/`) in values that follow the `content` syntax.
- CSSOM: add default implementations for value serializations in `DeclarationFormattingContext` and `StyleFormattingContext`.
- CSSOM: the first item of bracket list was not being minified in `getMinifiedCssText`.
- CSSOM: reduce the connection timeout from 60 to 10 seconds when retrieving style sheets or fonts (security).
- Implement CSS Object Model's `CSS.escape()` method (#18).
- Add `createSimpleSyntax(String)` convenience method to `SyntaxParser`.
- More compliant serialization of escaped identifiers (#17).
- Fix an index out of bounds error in `ParseHelper.startsWithIgnoreCase()`.
- Small code cleanup.
- Tests: add a few additional tests and assertions.
- Tests: add slf4j-simple as a test dependency.
- A few Javadoc improvements.
- Small adjustments to RELEASE_HOWTO.
- Add a small paragraph about remote network tests to CONTRIBUTING.
- README: mention how to run tests on Java 8 with 'testOn8'.
