# css4j version 4.0.1 Release Notes

### April 17, 2023

<br/>

## Highlights

### Produce an `EMPTY` value for empty `var()` fallbacks

Until now, in a value like `var(--customProperty,)`, the fallback was ignored
(like no fallback). Now it produces a `VAR` function with an `EMPTY` fallback,
which mimics what conforming web browsers should do.

<br/>

### Upgrade to Tokenproducer 2.0.1

Tokenproducer 2.0.1 introduces a new base interface but otherwise is the same as 1.2
(and is source-level compatible with it). Unfortunately the software compiled with 1.2
cannot figure out that the old `TokenHandler` interface inherits from the new `TokenHandler2`,
so anything compiled with 1.x is incompatible at runtime with 2.x.

If you upgrade to `xml-dtd` 4.2, make sure to upgrade to this css4j 4.0.1 as well.

<br/>

## Detail of changes

- NSAC: produce an `EMPTY` value for empty `var()` fallbacks.
- NSAC: do not convert unnecessarily to a string when serializing lexical units.
- CSSOM: do not convert unnecessarily to a string when serializing lexical values.
- CSSOM: (refactor) avoid overhead in `setCssText(String)`.
- CSSOM: make less strict the proxy check for `attr()` values.
- CSSOM: more accurate handling of advanced `attr()` in `LexicalValue.getFinalType()`.
- Small cosmetic changes to `ExpressionValue` and `GradientValue`.
- A few small simplifications of code.
- Tests: additional test.
- Upgrade to tokenproducer 2.0.1.
- Bump com.code-intelligence:jazzer-junit from 0.16.0 to 0.16.1.
- Upgrade Gradle wrapper to 8.1.
