# css4j version 5.1 Release Notes

### January 24, 2025

<br/>

## Highlights

### Advanced attr() values

The advanced `attr()` support now follows the current Values Level 5 specification.

Although this library has supported the advanced `attr()` value for years now,
it was implementing an old version of the specification which is substantially
different to the current one. The new spec is so recent that the old one is
still used by at least one CSS specification (CSS Lists 3) in their sample style
sheet for HTML.

This library should be compatible with the `attr()` which is shipped with the
forthcoming Google Chrome 133.

The old `CSSAttrValue` and `AttrValue` classes were removed, as the API is
incompatible with the new specification that is being implemented by browsers.

### More compliant registered custom properties

Also, the handling of registered custom properties is now closer to the Google
Chrome behaviour: registered initial values take precedence over the fallbacks.

#### Circularity behaviour changed

On the other hand, when `var()` circularities (and other apparent DoS attacks)
are found, it is no longer attempted to use the supplied property fallbacks.

It has been found that some websites send content with, for example,
`--foo:var(--foo,fallback)` circularities to non-browser user agents, in what
could be a strategy against web crawlers. Due to the kind of use cases that this
library has, it was determined that it is preferable to just report the
circularity and invalidate the whole value.

If your use case is negatively affected by this decision, please open an issue.

<br/>

## Detail of changes

- The advanced `attr()` support now follows the current Values Level 5 
  specification.
- NSAC: drop `EMPTY` units in `countReplaceBy()`.
- NSAC: have `getParameters()` return the sub-values if this is an expression 
  or a	unicode range.
- DOM wrapper: improved serialization of element nodes.
- Gradle: use the assignment operator in the maven repo section.
- Upgrade Gradle wrapper to 8.12.
- Upgrade to JUnit 5.11.4.
- Upgrade to Jazzer 0.23.0.
- Upgrade to checkstyle 10.21.1.
- Bump year to 2025 in copyrights.
