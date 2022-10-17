# css4j version 3.9 Release Notes

### October 17, 2022

<br/>

## Highlights

### NSAC & CSSOM: More compliant serialization of escaped identifiers

See issue #17.

### CSSOM: make `MediaQueryList.getMediaQuery(int)` publicly visible

See issue #19.

### CSSOM: add accept-visitor methods to `CSSStyleSheetList`

If you have a list of style sheets that you want to process using the Visitor
pattern, you now can execute the 'accept' method directly on the list.

### CSSOM: add `getFirstStyleRule()` and `getStyleRules()` utility methods to `AbstractCSSStyleSheet`

New ways to obtain style rules from a sheet, alternative to the tedious process
of scanning the entire sheet with `item()`, or the Visitor pattern methods.

### DOM: add `rebuildCascade()` to `CSSDocument`

In some cases, the library did not detect that a property value had changed,
and kept using an outdated cascade. With the new `rebuildCascade()` method,
developers can now make sure that the cascade is rebuilt once they are done
modifying the properties.

The interface provides a default implementation so it can be used with older
versions of css4j-dom4j.

### NSAC: add `contains()` and `containsAll()` to `SelectorList`

This should make it easier to verify whether a given style rule contains a
selector or a set of them.

### Implement CSS Object Model's `CSS.escape()` method

See issue #18.

### Bug fixes

A few bugs were fixed.

## Detail of changes

- NSAC: add `contains()` and `containsAll()` to `SelectorList`.
- NSAC: trim selector lists, for memory efficiency.
- NSAC: accept escaped attribute selector values.
- DOM: add `rebuildCascade()` to `CSSDocument`.
- DOM: implement setTextContent() for attributes, PIs and Text/CDATA/Comments.
- DOM: prevent NPEs setting null values to PIs and Text/CDATA/Comment nodes.
- DOM: set the documentURI earlier in XMLDocumentBuilder. This allows potential DOM document policies to be enforced at parse time.
- DOM: more efficient implementation of getTextContent().
- Avoid NPE in CSSDocument.setTargetMedium(String) implementations.
- CSSOM: make `MediaQueryList.getMediaQuery(int)` publicly visible (#19)
- CSSOM: add accept-visitor methods to `CSSStyleSheetList`.
- CSSOM: add `getFirstStyleRule()` and `getStyleRules()` utility methods to `AbstractCSSStyleSheet`.
- CSSOM: the first item of bracket list was not being minified in `getMinifiedCssText`.
- CSSOM: reduce the connection timeout from 60 to 10 seconds when retrieving style sheets or fonts (security).
- Implement CSS Object Model's `CSS.escape()` method (#18).
- More compliant serialization of escaped identifiers (#17).
- Fix an index out of bounds error in `ParseHelper.startsWithIgnoreCase()`.
- Small code cleanup.
- Tests: add a few additional tests and assertions.
- Tests: add slf4j-simple as a test dependency.
- A few Javadoc improvements.
- Small adjustments to RELEASE_HOWTO.
- Add a small paragraph about remote network tests to CONTRIBUTING.
- README: mention how to run tests on Java 8 with 'testOn8'.
