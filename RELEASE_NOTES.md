# css4j version 6.1.1 Release Notes

### October 16, 2025

<br/>

## Highlights

### XML/HTML builder: better default compatibility with Java 24 and later

Java 24 and 25 shipped with JAXP default parameters that are too small, see

- https://bugs.openjdk.org/browse/JDK-8368902
- https://github.com/google/google-java-format/issues/1210

This affects `XMLDocumentBuilder` which now applies larger values where needed.

<br/>

## Detail of changes

- dom: use reasonable JAXP configuration defaults for `XMLDocumentBuilder` to
  run on Java 24 and later.
- util: remove a few unneeded steps in shorthand minification.
- Additional relative color test.
- javadoc: Minify javadoc was missing two command line options.
- Small javadoc improvements.
- Upgrade to JUnit 5.14.0.
- Upgrade to checkstyle 10.26.1.
- Upgrade Gradle wrapper to 8.14.3.
- actions: remove reference to deleted `1-stable` branch on CI.
- actions: various upgrades.
