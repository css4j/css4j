# css4j version 1.3.0 Release Notes

### January 28, 2022

<br/>

## Highlights

### Build

- New Gradle build.

### JPMS

- The library now depends on `io.sf.jclf.text` module.

### CSSOM

- Support the `:dir()` pseudo-class in computed styles.

### Bug fixes and smaller improvements

- Several NSAC parser fixes.

- A few DOM improvements.

- A couple of CSSOM fixes.

## Detail of changes

- module-info: depend on `io.sf.jclf.text` module.
- NSAC impl.: fix a bug parsing numbers with an explicit plus sign.
- NSAC impl.: correctly handle comments splitting values and selectors.
- NSAC impl.: use a faster way to append codepoints to a StringBuilder.
- Fix a couple bugs in ParseHelper escape/unescape.
- DOM: DOMWriter: use a faster way to append codepoints to a StringBuilder.
- DOM: serialize elements with the EmptyElemTag production of XML when 
  appropriate.
- DOM: initialize text nodes to the empty string to avoid risk of NPE.
- CSSOM: support the `:dir()` pseudo-class in computed styles.
- CSSOM: `turn` is not to be considered a length unit, when decomposing 
  shorthands.
- CSSOM: fix a class cast exception when computing styles, related to `@page`
  rules.
- Use StringBuilder instead of StringBuffer in a couple of classes.
- Switch from Maven to a Gradle build.
- Add a CONTRIBUTING and a Developer Certificate of Origin files.
- Bump year to 2022 in copyright notices.
- Add a release helper script that creates a CHANGES draft.
- Upgrade to JCLF 5.0.0.
- Upgrade to commons-codec 1.15 or higher.
- Tests: add facility to load arbitrary documents from classpath.
- Tests: add a counter-style test that broke master branch (1.x not affected).
- Tests: fix a Java 8/9 compatibility issue in BaseCSSStyleSheetTest2.
- Tests: update Font Awesome CSS file.
- Tests: update metro-all.css.
