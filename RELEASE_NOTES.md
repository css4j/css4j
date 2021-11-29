# css4j version 3.7.0 Release Notes

### November 29, 2021

<br/>

## Highlights

### DOM

- Add CSSDocument.getCompatMode().

### Java 8 bug fix

- Two instances of a Java 11 method that was being accidentally used were replaced.
  All users on Java 8 should upgrade.

### Other bug fixes

- A few DOM wrapper fixes.

## Detail of changes

- NSAC impl.: replace two instances of a Java 11 method being accidentally used.
- DOM: add `getCompatMode()` to `CSSDocument` and `DOMDocument`.
- DOM Wrapper: add `getCompatMode()`.
- DOM wrapper: fix `getDoctype()` when `DocumentType` is `null`.
- DOM wrapper: fix `getAttributeNodeNS`, make sure that `getAttributeNS` gets a wrapped value.
- Tests: fix a Java 8/9 compatibility issue in `BaseCSSStyleSheetTest2`.
- Tests: use the `float` instead of the `double` variant of `assertEquals`.
- Gradle: add the ability to run tests with Java 8 (`testOn8` task).
- Gradle: add a test coverage report runnable with `gradlew jacocoTestReport`.
- Gradle: upgrade wrapper to 7.3.
- CI: build with both Java 11 and 17, run tests with Java 8.
