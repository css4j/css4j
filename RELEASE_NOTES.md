# css4j version 4.3.1 Release Notes

### August 5, 2024

<br/>

## Highlights

A small spec-compliance fix for native DOM (`io.sf.carte.doc.dom` package).

<br/>

## Detail of changes

- NSAC impl: avoid `IndexOutOfBoundsException` when parsing invalid functions. Detected via Jazzer fuzz testing.
- NSAC impl: fix a bug where an error in nested rules wasn't reported. Detected via Jazzer fuzz testing.
- DOM: make case sensitivity of non-HTML embedded into HTML documents more spec-compliant.
- DOM: do not check for <svg> elements with the wrong namespace in HTML mode. Use an HTML parser instead.
- Gradle: add a test sources jar.
