/**
 * NSAC: a non-standard revision to
 * <a href="https://www.w3.org/Style/CSS/SAC/">W3C's SAC api</a>.
 * <h2>NSAC 2.0</h2>
 * <p>
 * NSAC 1.0 was intended to be an easy upgrade path for SAC users, and it
 * preserved most of SAC. That 1.0 API was not convenient for modern CSS,
 * however, so NSAC 2.0 was written as a better replacement.
 * </p>
 * <p>
 * The SAC jar file is no longer necessary, as NSAC 2.0 contains all the
 * required classes and interfaces.
 * </p>
 * <h3>Selector Serialization</h3>
 * <p>
 * Implementations of the selector interfaces are not required to provide a
 * <code>toString()</code> serialization, although the reference implementation
 * (the <code>doc.style.css.parser</code> package) does. It is not recommended
 * to use it for serialization, however, as it may not reflect namespace changes
 * made after the parsing took place (<i>i.e.</i> changing the namespace
 * prefix). The rule serialization in the Object Model implementation of css4j
 * does account for that, and it does not use the selector's
 * <code>toString()</code>.
 * </p>
 * <p>
 * If you combine NSAC with your own Object Model code, you may want to follow
 * the same approach and serialize the selectors yourself.
 * </p>
 * <h3>Parser Flags</h3>
 * <p>
 * To let the parser be configurable, two methods were added to the
 * <code>Parser</code> interface:
 * </p>
 * <p>
 * <code>void setFlag(Flag)</code>
 * </p>
 * <p>
 * <code>void unsetFlag(Flag)</code>
 * </p>
 * <p>
 * where <code>Flag</code> is a flag from an enumeration:
 * <ul>
 * <li><code>STARHACK</code>. When <code>STARHACK</code> is set, the parser will
 * handle asterisk-prefixed property names as accepted names. This hack (that
 * targets old IE browsers) is ubiquitous in present-day websites, and in plain
 * SAC parsers it was producing 'unexpected character' errors because the
 * property names are not valid according to the specification. However, these
 * declarations weren't real mistakes and the style authors wanted them to be
 * there.</li>
 * <li><code>IEVALUES</code> accepts values with some IE hacks like IE
 * expressions (progid included), and also values ending with <code>\9</code>
 * and <code>\0</code>. The non-standard values produce
 * <code>LexicalType.COMPAT_IDENT</code> values as a result.</li>
 * <li><code>IEPRIO</code> accepts values with the <code>!ie</code> priority
 * hack, and again produces <code>LexicalType.COMPAT_IDENT</code>
 * values.</li>
 * <li><code>IEPRIOCHAR</code> accepts values with the <code>!important!</code>
 * priority hack, and instead produces <code>LexicalType.COMPAT_PRIO</code>
 * values.</li>
 * </ul>
 * </p>
 * <h3 id="luextensions"><code>LexicalUnit</code> Extensions</h3>
 * <p>
 * Some of the above Internet Explorer compatibility flags require the use of
 * two pseudo-values that do not follow standard CSS syntax:
 * </p>
 * <ul>
 * <li><code>COMPAT_IDENT</code> values are produced only when the
 * <code>IEVALUES</code> and <code>IEPRIO</code> flags are used, and contain
 * ident-like values.</li>
 * <li><code>COMPAT_PRIO</code> values are produced by the
 * <code>IEPRIOCHAR</code> flag, representing values that its compatible browser
 * interprets as being of <code>!important</code> priority.</li>
 * </ul>
 * </p>
 * <p>
 * Caution is advised when using these compatibility pseudo-values, as they may
 * conflict with syntax-conformant values.
 * </p>
 * <h2>W3C Copyright Notice</h2>
 * <p>
 * This software includes material derived from SAC
 * (<a href="https://www.w3.org/TR/SAC/">https://www.w3.org/TR/SAC/</a>).
 * Copyright © 1999,2000 <a href="http://www.w3.org/">W3C</a>®
 * (<a href="http://www.csail.mit.edu/">MIT</a>,
 * <a href="http://www.inria.fr/">INRIA</a>,
 * <a href="http://www.keio.ac.jp/">Keio</a>).
 * </p>
 */
package io.sf.carte.doc.style.css.nsac;
