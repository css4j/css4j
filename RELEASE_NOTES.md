# css4j version 4.2.2 Release Notes

### November 19, 2023

<br/>

## Highlights

### Require `Flag.IEVALUES` to parse legacy IE expressions

This implies a stricter parsing of `calc()` expressions involving a plus sign.

Previously, the library would accept invalid values like `calc(2+2)`, while
correctly serializing them as `calc(2 + 2)`, because they are unambiguous and
some legacy Internet Explorer expressions allow that syntax. However that was
problematic for the people that uses the library for error detection, so now
you have to configure the parser with `Flag.IEVALUES` if you want the more
relaxed behaviour.

### Specificity fixes

Fixes issues with the computation of selector specificity.

<br/>

## Detail of changes

- NSAC: require `Flag.IEVALUES` to parse IE expressions.
- CSSOM: fix issues with computation of selector specificity.
- A couple of Javadoc improvements.
- Upgrade to Jazzer 0.22.1.
- Upgrade to JUnit 5.10.1.
