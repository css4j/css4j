# css4j version 5.3.1 Release Notes

### April 1, 2025

<br/>

## Highlights

### Regression fix

This release fixes a DOM regression introduced in 5.3. All users of 5.3 should
upgrade to 5.3.1.

### XML entities in pseudo-attributes

The library now replaces XML predefined entities in the pseudo-attributes of
Processing Instructions, for example `title="1&amp;2"`.

For details about the syntax, see

https://www.w3.org/TR/xml-stylesheet/xml-stylesheet.xml#NT-PseudoAtt

<br/>

## Detail of changes

- Fix DOM regression introduced in be35ef9cea4.
- Support XML entities in PI pseudo-attributes.
