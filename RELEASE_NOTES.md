# css4j version 1.3.1 Release Notes

### February 15, 2022

<br/>

## Highlights

This release fixes several bugs, including two regressions introduced in 1.3.0.


## Detail of changes

- NSAC impl.: fix a regression in signed value parsing, introduced in 1.3.0.
- NSAC impl.: fix a regression in attribute selector parsing when preceded by a
  comment, introduced in 1.3.0.
- NSAC impl.: improve detection of unbalanced parentheses.
- CSSOM: fix a bug decomposing and serializing the `background` shorthand.
- CSSOM: fix a bug in the minified serialization of `calc()` values.
- More efficient use of a few collections.
- Remove several unnecessary `return` statements.
- Gradle: upgrade wrapper to 7.4.
