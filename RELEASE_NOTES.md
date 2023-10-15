# css4j version 4.2.1 Release Notes

### October 15, 2023

<br/>

## Highlights

### Fixes for Conditional Rules

Fix a few issues with conditional rules, including a regression introduced in
4.2 which could cause the wrong `@supports` and `@media` rules being used in the
cascade.

<br/>

## Detail of changes


- Add `SelectorFunction` interface and `StyleDatabase.supports(SelectorList)` method, use in supports condition evaluation.
- Fix a few issues with conditional rules, including a regression introduced in 4.2 which could cause the wrong rules being used in the cascade.
- Use advanced for-loop instead of iterators.
- Code style: use diamond operator in `ValueList`.
- Upgrade to Jazzer 0.21.1.
- Upgrade Gradle wrapper to 8.4.
