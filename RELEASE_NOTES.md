# css4j version 4.1 Release Notes

### August 18, 2023

<br/>

## Highlights

### Native DOM improvements

- Elements and documents now have `querySelector` in addition to the already present `querySelectorAll`.

- `CSSDOMImplementation` gains the `newDocument()` and `newHTMLDocument()` convenience methods.

- `XMLDocumentBuilder.newDocument()` now creates a plain XML document instead of HTML, as it should have ever been.

<br/>

### CSSOM

- Added `createSupportsRule(String)` to `CSSStyleSheet`.

- The `animation` shorthand was updated to latest Chrome behaviour.

<br/>

## Detail of changes

- DOM: implement `querySelector`.
- DOM: `XMLDocumentBuilder.newDocument()` should create a plain XML document.
- DOM: add convenience methods `newDocument()` and `newHTMLDocument()` to `CSSDOMImplementation`.
- CSSOM: add method `createSupportsRule(String)` to `CSSStyleSheet`.
- CSSOM: support the `animation-timeline`, `animation-range-start` and `animation-range-end` properties.
- Bump com.code-intelligence:jazzer-junit to 0.19.0.
- Bump org.junit.vintage:junit-vintage-engine from to 5.10.0.
- Bump org.junit.jupiter:junit-jupiter to 5.10.0.
- Upgrade Gradle wrapper to 8.3.
- changes.sh: add a dot at the end of each item.
