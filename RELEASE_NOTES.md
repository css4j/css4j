# css4j version 3.9.1 Release Notes

### January 23, 2023

<br/>

## Highlights

### NSAC & CSSOM: support `url()` values with a `var()` argument

Now it is possible to provide the value inside the `url()` function with a `var()` reference.
This is an extension to the specification that one mainstream browser has suggested it may implement.

<br/>

### DOM: performance boost in positional selector matching and base URL computation

If you are using the native DOM implementation to process large documents, you should upgrade.

<br/>

### Bug fixes

The style sheets of the native DOM are now updated on removal of META `http-equiv` default-style attribute.
Same when setting the value of the attribute in the DOM wrapper.

Also in the wrapper, `Node.getBaseURI()` is computed consistently for all nodes, and an I/O error is reported
on malformed URIs (same behaviour as native DOM and css4j-dom4j).

<br/>

## Detail of changes

- NSAC&CSSOM: support `url()` values with a `var()` argument.
- CSSOM: add a default implementation for `BoxValues.fillBoxValues()`.
- DOM: improve performance in tree-structural selector matching and a few other pseudo-classes.
- DOM: speed up the computation of the base URL in HTML documents.
- DOM: update sheets on removal of META `http-equiv` `default-style`.
- DOM wrapper: compute `Node.getBaseURI()` consistently for all nodes, report I/O error on malformed URI.
- DOM wrapper: update sheets on setting values of META `http-equiv` and `content` attributes.
- Clean up selector matcher tests.
- Remove a few unnecessary casts
- Bump year to 2023 in copyrights.
- Various Javadoc improvements.
- Configure automated dependency update PRs with `dependabot.yml`.
- Add a CI badge to README.
- README: recommend xml-dtd 4.1.1 or higher
- Rename `LICENSES.txt` file as `NOTICE.txt`
- Make sure that animate.css test sample is not updated.
- Upgrade to xml-dtd 4.1.1
- Upgrade to tokenproducer 1.1.2
- Upgrade Gradle wrapper to 7.6
