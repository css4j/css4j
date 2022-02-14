# css4j version 3.7.1 Release Notes

### February 15, 2022

<br/>

## Highlights

### Security

In the CSSOM, the native DOM implementation and the DOM Wrapper, now linked
style sheets are not retrieved if the Content-Type provided by the server is
invalid for a CSS style sheet.

That's the same that web browsers do, and may be useful to prevent unwanted URL
retrieval while parsing untrusted documents.


### Values split by comments

Comments splitting values and selectors are now handled like in browsers. For
example, a value like 1/* */2/* */3/* */4 used to be parsed as '1234', now it
would be '1 2 3 4'.


### Empty URLs

Empty URLs (`url()`) are syntactically valid but do not generally represent a
valid value, except when used in `@supports` conditions. The library used to
parse empty URLs correctly but choked when serializing them. Now both parsing
and serialization are handled.


### String-less URLs with a semicolon

String-less URLs are long-supported by this library, but finding a semicolon
inside the URL parenthesis would trigger an error despite being a specially
allowed case. Now semicolons are accepted there, and one can have URLs like

```css
url(data:image/png;base64,MTIzNDU2Nzg5MGFiY2RlZmckJSYvKCk/)
```

or even

```css
url(https://fonts.googleapis.com/css2?family=Raleway:ital,wght@0,200;0,300;0,400;)
```

### New AttrStyleRuleVisitor utility

`AttrStyleRuleVisitor` is a convenience class that can be commonly used in the
context of the Visitor-based methods. It is based on the
[`SelectorRuleVisitor`](https://sourceforge.net/p/carte/carte/ci/155ca5cf29/tree/carte/src/io/sf/carte/report/SelectorRuleVisitor.java)
class in the Carte repository. Given its general usefulness, it is now available
in the new `doc.style.css.util` package.

See [Embedding SVG in HTML documents](https://css4j.github.io/embed-svg.html)
for an example of usage.


### Mask shorthand

The `mask` shorthand, that is only supported by Firefox (and prefixed by Chrome)
but has a certain share of users, is now supported.

See https://www.w3.org/TR/css-masking-1/ for details.


### More syntax tolerance for prefixed properties

Prefixed properties often contain characters like the slash (`/`) that trigger
errors when converted to CSSOM (for example in `-webkit-mask`). Now the library
is more tolerant to custom syntaxes for prefixed properties.


### Advanced color functions in shorthands

The `image()`, `image-set()` and `cross-fade()` color functions are now
processed as valid color values when decomposing shorthands. Beware that
although the library is able to decompose such shorthands, most browsers won't.


### Small cleanups and fixes

A very small code cleanup was performed with the help of the Cleanup utility in
the Eclipse IDE, see the detailed changelog for more information. And a few bugs
were fixed.


### CI: CodeQL analysis

The code is now automatically analysed by the CodeQL static analysis tool as a
standard part of the CI process.


## Detail of changes

- NSAC/CSSOM: support empty URLs.
- NSAC: provide default void interface implementations of `startViewport()` and 
  `endViewport()` in `CSSHandler`.
- NSAC impl.: correctly handle comments splitting values and selectors.
- NSAC impl.: support semicolons inside non-string uri values.
- NSAC impl.: fix a regression in signed value parsing, introduced in 3.5.2.
- NSAC: `attr()` values of type `url` now match `<url>` and `<image>` types.
- NSAC: `image()`, `image-set()` and `cross-fade()` are now recognized as image 
  functions when matching `<image>`.
- NSAC impl.: remove unnecessary `Character.valueOf()`.
- CSSOM, DOM, Wrapper: do not retrieve a style sheet if the provided
  Content-Type is invalid.
- CSSOM: support decomposing and serializing the `mask` shorthand.
- CSSOM: allow custom syntax in agent-prefixed properties.
- CSSOM: add a convenience `AttrStyleRuleVisitor` in the new `doc.style.css.util` 
  package.
- CSSOM: fix a bug in the minified serialization of the `background` shorthand 
  in computed styles.
- CSSOM: fix a bug decomposing the `background` shorthand.
- CSSOM: now `image()`, `image-set()` and `cross-fade()` functions match
  `<image>`.
- CSSOM: support `attr()` length-percentage values when decomposing the 
  `background` shorthand.
- CSSOM: accept `attr()` values of url type as images, when decomposing 
  shorthands.
- CSSOM: when reporting style sheet errors, use line/column information if 
  available in the `DOMException` message.
- CSSOM: very small speed improvement when decomposing shorthands.
- CSSOM: use an `int` counter in `FlexShorthandSetter` to fix a CodeQL warning.
- Use `StringBuilder` instead of `StringBuffer` in a couple of classes.
- Refactor: make several inner classes static.
- Remove several unnecessary null checks.
- Remove unnecessary parentheses in assertions.
- Remove unnecessary public modifier in `CSSUnit`.
- Use more efficient collection methods in a couple of places.
- Remove several unnecessary `return` statements.
- Tests: remove unnecessary casts in `XMLDocumentTest`.
- Tests: update Font Awesome CSS file.
- Tests: update metro-all.css.
- Gradle: use the overview in the javadoc.
- Gradle: upgrade wrapper to 7.4.
- CI: validate Gradle wrapper once.
- Create `codeql-analysis.yml` to automatically scan the code with CodeQL.
- Upgrade xml-dtd preferred dependency to 4.0.0.
- Bump year to 2022 in copyright notices.
