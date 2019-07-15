/**
 * This package and its subpackages provide an implementation of the
 * <a href="https://www.w3.org/TR/2000/REC-DOM-Level-2-Style-20001113/" target=
 * "_blank">CSS Object Model API</a>. The root <code>css</code> package essentially
 * contains interface definitions (many derived from W3C) and generic classes.
 * <h2>Extended W3C CSSOM and SAC interfaces</h2>
 * <p>
 * There are two flavors of extended W3C interfaces in this library (those that extend
 * previously existing W3C types, as opposed to those that are new ones):
 * </p>
 * <ul>
 * <li>Interfaces suffixed with a "2" (like
 * {@link io.sf.carte.doc.style.css.CSSPrimitiveValue2 CSSPrimitiveValue2}) have new
 * methods that -in the opinion of the author- are required to handle current CSS.</li>
 * <li>Interfaces that are prefixed with "<code>Extended</code>" offer additional
 * functionality that you may or may not want to use.</li>
 * </ul>
 * <h2>Compliance with CSS specification(s)</h2>
 * <p>
 * Although this implementation attempts to follow the various CSS specifications, there
 * are known deviations from what was specified by the W3C. Most of those differences
 * arise from the fact that the W3C specifications are intended to be implemented by user
 * agents, and not by tools like this library. One example is serialization.
 * </p>
 * <h3>Serialization and <code>getCssText()</code></h3>
 * <p>
 * The CSSOM specification requires
 * {@link org.w3c.dom.css.CSSStyleDeclaration#getCssText() getCssText()} to return the
 * serialization of property declarations, following an algorithm that -whenever possible-
 * builds shorthand properties from the longhands. However, this is inconvenient for most
 * use cases of this library, so the <code>getCssText()</code> method returns the
 * properties in a form that is close to what was specified with
 * <code>setCssText()</code>, either longhands or shorthands.
 * </p>
 * <p>
 * The reason should be obvious: this library is generally not the final recipient for
 * those declarations, but just middleware. If a CSS author specifies style in a certain
 * way, he/she may be targeting more that one browser, but if this library produced its
 * own shorthand constructions (and it has the logic to do that), that could break what
 * was intended by the author. The library does remove obviously wrong declarations (and
 * also redundant ones), however. The final result may not exactly reflect what was
 * specified by the author, but is a good compromise.
 * </p>
 * <p>
 * Another deviation from the specification comes with the computed styles. Instead of
 * returning the empty string (as the Working Group recommends), or a serialization of all
 * the property name/value pairs known to the library (like some browser does), only those
 * values that come from the cascade or are inherited from another element are included in
 * both the <code>item</code> collection and <code>getCssText()</code>. Note that
 * {@link org.w3c.dom.css.CSSStyleDeclaration#getPropertyCSSValue(String)
 * getPropertyCSSValue(String)} and
 * {@link org.w3c.dom.css.CSSStyleDeclaration#getPropertyValue(String)
 * getPropertyValue(String)} both return values for properties that are not included in
 * the <code>item</code> collection, although this may seem counter-intuitive.
 * </p>
 * <p>
 * This approach has better flexibility for the different use cases of this library, and
 * prevents potential problems (for example a downstream application not understanding all
 * the properties in the declaration text, or re-parsing texts that are too large). Also,
 * downstream users can check whether some declared style ended up in an element's
 * computed style (as opposed to originating from initial values) by just checking
 * {@link org.w3c.dom.css.CSSStyleDeclaration#getLength()
 * CSSStyleDeclaration.getLength()}.
 * </p>
 */
package io.sf.carte.doc.style.css;
