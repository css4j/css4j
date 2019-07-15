/**
 * User agent classes.
 * <p>
 * To compute styles with a minimum of accuracy, the library needs to use
 * several user agent-related abstractions. Those interfaces and classes are
 * intended to make easier integrating the library with a range of use cases.
 * </p>
 * <p>
 * If you do not want to compute styles, you do not need to use anything in this
 * package (unless you wish to use this infrastructure to retrieve documents and
 * style sheets). On the other hand, if you want accurate style computations you
 * need to write implementations of <code>DeviceFactory</code>,
 * <code>CSSCanvas</code> and <code>StyleDatabase</code> adequate for your use
 * case. You can compute styles without them, but the results may be less
 * accurate.
 * </p>
 */
package io.sf.carte.doc.agent;
