# css4j version 4.3 Release Notes

### July 31, 2024

<br/>

## Highlights

This version comes with CSSOM updates and a less strict check for the style
sheet content types.

Also, the deprecated `doPrivileged()` calls were removed.

<br/>

## Detail of changes

- Remove deprecated `doPrivileged()` calls.
- NSAC: make `parsePseudoElement(String)` part of the `Parser` interface.
- NSAC: make content type check less strict.
- CSSOM: include a default `getSeparator()` in `CSSCounterValue`.
- CSSOM: add a name setter to `CSSKeyframesRule`.
- CSSOM: make content type checking less strict.
- Tests: simplify SelectorMatcher Test.
- Tests: use the float variant of assertEquals in RuleParserTest.
- Bump year to 2024 in copyrights.
- Upgrade to JUnit 5.10.3.
- Upgrade Gradle wrapper to 8.5.
- README: update Javadoc links.
