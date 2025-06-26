# css4j version 6.1 Release Notes

### June 26, 2025

<br/>

## Highlights

### Upgraded generic CSS minifier

The [`Minify`](https://css4j.github.io/api/latest/io.sf.carte.css4j/io/sf/carte/doc/style/css/util/Minify.html)
class was upgraded to be more appropriate for generic minification tasks, and
the serialization is now shorter. For example, the following style rule:
```css
div {
 /* Verbose longhand properties */
  color: rgba(255,255,0,255);
  background-color: rgb(from lime r calc(g - 127) b);

 /* Shorthands with inefficient values */
  border-radius: initial; /* Some compressors would remove this but that's wrong */
  margin: 2px 2px 2px 2px;
  background: url('bkg.png') left top round space padding-box border-box local;
}
```
is minified as
```css
div{color:#ff0;background-color:green;border-radius:0;margin:2px;background:url(bkg.png) round space local}
```

`Minify` can be used like a normal class, or via the command line with the `alldeps` _jar_:
```shell
java -jar path/to/css4j-6.1-alldeps.jar verbose.css --charset ISO-8859-1 > minified.css
```
Note that `UTF-8` is the default character set. The `alldeps` fat _jar_ is now
part of the standard css4j distribution.

The command-line minifier exits with a status code of `0` if it ran successfully,
`2` if the arguments were incorrect, and `1` if a parsing error was found in the
style sheet and a simpler minifier was used.

More information is available at the [minification section of the usage guide](https://css4j.github.io/usage.html#minification).
Css4j is now used to minify the CSS in the css4j website.

<br/>

### Relative color syntax

Relative color syntax from Color Level 5 is supported.
```css
background: oklch(from var(--base) l c calc(h + 180));
```
Relevant specification: https://www.w3.org/TR/css-color-5/#relative-colors

<br/>

### Parse and serialize the `if` function

The `if` function was shipped by Chrome 137. The argument contains both colons
and semicolons, which prevented the parsing with previous versions of the
library:
```css
background-color: if (style(--color: white): black; else: white);
```
The library can now do a basic parsing of the function but cannot provide the
intended final value at the computed style level.

Relevant specification: https://drafts.csswg.org/css-values-5/#if-notation

<br/>

### Selectors: allow multiple conditions in Combinator (AND) conditions

This only affects you in the unlikely case that you work with selectors at low
level. The old API was a bottleneck in the processing of conditional selectors.

<br/>

## Detail of changes

- NSAC: allow multiple conditions in Combinator (AND) conditions.
- NSAC/CSSOM: relative colors.
- NSAC/CSSOM: parse the `if` function.
- Add a parser flags constructor to `CSSOMParser`.
- CSSOM: matching of `of-type` pseudo-classes was case sensitive.
- CSSOM: allow `var()` values in media query features.
- CSSOM: add `equals()` and `hashCode()` to `AbstractStyleSheet`.
- CSSOM: make the deprecated `CSSStyleSheet.createSupportsRule()` a default method.
- util: improve the generic CSS minifier.
- Remove unnecessary argument in grouping rule check.
- Upgrade to TokenProducer 3.3.
- Upgrade to checkstyle 10.26.0.
- Upgrade JUnit to 5.13.2.
- Upgrade Gradle wrapper to 8.14.2.
