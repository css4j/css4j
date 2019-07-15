/**
 * NSAC: a non-standard extension to <a href="https://www.w3.org/Style/CSS/SAC/">W3C's SAC
 * api</a>.
 * <h2>NSAC Base Extension</h2>
 * <p>
 * The base extension is intended to be an easy upgrade path for current SAC users,
 * providing an API that is very similar to SAC but is able to process modern selectors,
 * and supports recent syntax and units. The original SAC interfaces are on a package
 * under the <code>org.w3c</code> hierarchy and cannot be modified directly, so a set of
 * interfaces that inherit from SAC was created under a new package called
 * <code>nsac</code> (for Non-Standard Api for Css) in the <code>doc.style.css</code>
 * hierarchy of this project.
 * </p>
 * <h3>The New Interfaces</h3>
 * <p>
 * The modifications that the new interfaces introduce are basically the following:
 * </p>
 * <ol>
 * <li><code>LexicalUnit</code>: add new identifiers (constants) and the method
 * <a href="#lugetcsstext"><code>getCssText()</code></a>. (<a href="#lexicalunit">see below
 * </a>)</li>
 * <li><code>Selector</code>: add numeric identifiers for combinators
 * <code>SAC_SUBSEQUENT_SIBLING_SELECTOR</code> and
 * <code>SAC_COLUMN_COMBINATOR_SELECTOR</code>. Also add the
 * <code>SAC_SCOPE_SELECTOR</code> pseudo-selector, intended to be a placeholder for scope
 * in an selector argument (like in <code>not()</code> or <code>has()</code>).</li>
 * <li><code>Condition</code>: add numeric identifiers for
 * <code>SAC_BEGINS_ATTRIBUTE_CONDITION</code> and others, including
 * <code>SAC_PSEUDO_ELEMENT_CONDITION</code>. Pseudo-elements are handled as conditional
 * selectors with this extension. Also, a new
 * <code>Condition.SAC_SELECTOR_ARGUMENT_CONDITION</code> identifier is introduced to
 * support the new <code>ArgumentCondition</code>.</li>
 * <li>New <code>ArgumentCondition</code> (<a href="#argcond">see below</a>).</li>
 * <li>New methods added to <code>AttributeCondition</code> and
 * <code>PositionalCondition</code> (<a href="#poscond">see below</a>).</li>
 * </ol>
 * <h3>New Methods and Constants</h3>
 * <h4 id="lexicalunit">Interface <code>LexicalUnit</code></h4>
 * <h5>Constants</h5>
 * <ul>
 * <li>Add identifiers for level 3-4 units like <code>SAC_CAP</code>.</li>
 * <li>Add <code>SAC_ELEMENT_REFERENCE</code> for images by element reference.</li>
 * <li>For unicode range wildcards, add <code>SAC_UNICODE_WILDCARD</code>.</li>
 * <li>Also add the following identifiers for
 * <a href="https://www.w3.org/TR/css-grid-1/">grid support</a>:
 * <code>SAC_LEFT_BRACKET</code>, <code>SAC_RIGHT_BRACKET</code> and
 * <code>SAC_FR</code> (<code>fr</code> flexible length unit).</li>
 * <li>Finally, two values that are non-conformant and intended to be used for
 * compatibility with the Internet Explorer browser:
 * <code>SAC_COMPAT_IDENT</code> and <code>SAC_COMPAT_PRIO</code> values (<a
 * href="#luextensions">see <code>LexicalUnit</code> Extensions</a>).</li>
 * </ul>
 * <h5>Method</h5>
 * <dl>
 * <dt id="lugetcsstext"><code>String getCssText()</code></dt>
 * <dd>Get a parsable representation of the unit. The serialization must only include
 * this lexical unit, ignoring the next units if they exist.</dd>
 * </dl>
 * <h4>Interface <code>AttributeCondition</code></h4>
 * <dl>
 * <dt><code>boolean hasFlag(Flag)</code></dt>
 * <dd>Check for the given flag. Currently supports <code>CASE_I</code> for the
 * <code>i</code> flag (case insensitivity) and <code>CASE_S</code> for
 * <code>s</code>.</dd>
 * </dl>
 * <h4 id="poscond">Interface <code>PositionalCondition</code></h4>
 * <dl>
 * <dt><code>boolean isForwardCondition()</code></dt>
 * <dd>Is this a forward condition? return <code>true</code> if this is a forward condition like
 * <code>:nth-child</code> (also <code>true</code> for <code>:only-child</code>), <code>false</code>
 * otherwise (like in <code>:nth-last-child</code>).</dd>
 * <dt><code>boolean isOfType()</code></dt>
 * <dd>Is this an of-type selector? This method only returns <code>true</code> if the selector has been
 * explicitly declared to apply to the same type, like in <code>:first-of-type</code> or
 * <code>:nth-of-type</code>. It should return <code>false</code> otherwise, for example for selectors
 * like <code>div:nth-child(1 of div)</code> despite being equivalent to
 * <code>div:first-of-type</code>. This method is possibly the same as the old
 * <code>PositionalCondition.getType()</code>, but with a potentially different
 * specification.</dd>
 * <dt><code>int getFactor()</code></dt>
 * <dd>Get the <code>An+B</code> expression factor (<i>i.e.</i> 'A'). Zero if there is no
 * factor (or no expression).</dd>
 * <dt><code>int getOffset()</code></dt>
 * <dd>Get the <code>An+B</code> expression offset (<i>i.e.</i> 'B'). If there is no
 * expression, it must return the positive integer position value (<i>e.g.</i>
 * <code>1</code> for <code>:first-child</code> or <code>:last-child</code>).</dd>
 * <dt><code>SelectorList getOfList()</code></dt>
 * <dd>Get the list of selectors that the child list have to match, or <code>null</code>
 * if not specified.</dd>
 * <dt><code>boolean hasArgument()</code></dt>
 * <dd>Was the selector specified with an argument ?. Serves to tell apart selectors
 * that have no functional notation from their functional equivalents (like <code>:first-child</code>
 * and <code>:nth-child(1)</code>.</dd>
 * <dt><code>boolean hasKeyword()</code></dt>
 * <dd>Get whether the AnB expression was specified as a keyword (like <code>even</code>).</dd>
 * </dl>
 * <h4 id="argcond">New Interface <code>ArgumentCondition</code></h4>
 * <dl>
 * <dt><code>String getName()</code></dt>
 * <dd>Get the name of the condition, like <code>not</code> or <code>has</code>.</dd>
 * <dt><code>SelectorList getSelectors()</code></dt>
 * <dd>Get the selector list to which the condition has to be applied.</dd>
 * </dl>
 * <h3>Other Changes to Current SAC Usage</h3>
 * <p>
 * Although the changes to SAC described so far are required to handle modern selectors,
 * the NSAC API behaves differently in other aspects:
 * </p>
 * <ul>
 * <li>The names of pseudo-classes are retrieved with the
 * <code>AttributeCondition</code>'s <code>getLocalName()</code> method instead of
 * <code>getValue()</code>. This leaves <code>getValue()</code> to retrieve the
 * pseudo-class argument if there is any, like in <code>:dir(ltr)</code>.</li>
 * <li>The identifier <code>SAC_ANY_NODE_SELECTOR</code> is used for the universal
 * selector, albeit it is still implemented as an <code>ElementSelector</code> with the
 * <code>*</code> local name.</li>
 * <li>Margin rules are handled through <code>startPage</code> and <code>endPage</code>,
 * as nested page rules. Not very elegant, but allows the reuse of the old
 * <code>DocumentHandler</code> interface.</li>
 * <li>Implementations of <code>LexicalUnit</code> must provide a serialization of
 * the value, at least for <code>SAC_IDENT</code> and <code>SAC_RGBCOLOR</code> values,
 * available through <code>LexicalUnit2</code>'s new <code>getCssText()</code> method.
 * That serialization should be close to how the value was specified (for example,
 * preserving hex or functional notation in rgb colors) but must parse without errors
 * (except for compatibility values like <code>SAC_COMPAT_IDENT</code>).</li>
 * </ul>
 * <h3>Selector Serialization</h3>
 * <p>
 * Implementations of the selector interfaces are not required to provide a
 * <code>toString()</code> serialization, although the reference implementation (the
 * <code>doc.style.css.parser</code> package) does. It is not recommended to use it for
 * serialization, however, as it may not reflect namespace changes made after the parsing
 * took place (<i>i.e.</i> changing the namespace prefix). The rule serialization in the
 * Object Model implementation of css4j does account for that, and it does not use the
 * selector's <code>toString()</code>.
 * </p>
 * <p>
 * If you combine NSAC with your own Object Model code, you may want to follow the same
 * approach and serialize the selectors yourself.
 * </p>
 * <h2>The Parser Extensions</h2>
 * <p>
 * The previously described base SAC extensions allowed to handle level 4 selectors and
 * values. However, for the API to be of more general usefulness in real-world usage a few
 * methods were added to the <code>Parser</code> interface.
 * </p>
 * <h3>Parser Flags</h3>
 * <p>
 * To let the parser be configurable, two methods were added to the <code>Parser</code>
 * interface:
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
 * <li><code>STARHACK</code>. When <code>STARHACK</code> is set, the parser will handle
 * asterisk-prefixed property names as accepted names. This hack (that targets old IE
 * browsers) is ubiquitous in present-day websites, and in plain SAC parsers it was
 * producing 'unexpected character' errors because the property names are not valid
 * according to the specification. However, these declarations weren't real mistakes and
 * the style authors wanted them to be there.</li>
 * <li><code>IEVALUES</code> accepts values with some IE hacks like IE expressions (progid
 * included), and also values ending with <code>\9</code> and <code>\0</code>. The
 * non-standard values produce <code>LexicalUnit2.SAC_COMPAT_IDENT</code> values as a
 * result.</li>
 * <li><code>IEPRIO</code> accepts values with the <code>!ie</code> priority hack, and
 * again produces <code>LexicalUnit2.SAC_COMPAT_IDENT</code> values.</li>
 * <li><code>IEPRIOCHAR</code> accepts values with the <code>!important!</code> priority
 * hack, and instead produces <code>LexicalUnit2.SAC_COMPAT_PRIO</code> values.</li>
 * </ul>
 * </p>
 * <h3>Rule Parsing</h3>
 * <p>
 * The original SAC api includes this method:
 * </p>
 * <p>
 * <code>void parseRule(InputSource)</code>
 * </p>
 * <p>
 * The method can be used to parse individual rules, like those supplied to the CSSOM's
 * <code>CSSStyleSheet.insertRule</code> method:
 * </p>
 * <p>
 * <code>int insertRule(String, int)</code>
 * </p>
 * <p>
 * However, when a rule that contains a namespace prefix is parsed, the style sheet may
 * contain the appropriate <code>CSSNamespaceRule</code> for that prefix, but it is
 * unclear why/how the <code>Parser</code> instance is going to know about it. To be able
 * to handle that case, NSAC adds the following method to the <code>Parser</code>
 * interface:
 * </p>
 * <p>
 * <code>void parseRule(InputSource, NamespaceMap)</code>
 * <p>
 * where <code>NamespaceMap</code> is a simple sub-interface that just gives the namespace
 * URI associated to a prefix:
 * </p>
 * <p>
 * <code>String getNamespaceURI(String)</code>
 * </p>
 * <h3 id="luextensions"><code>LexicalUnit</code> Extensions</h3>
 * <p>Some of the above Internet Explorer compatibility flags require the use of two
 * pseudo-values that do not follow standard CSS syntax:</p>
 * <ul>
 * <li><code>SAC_COMPAT_IDENT</code> values are produced only when the <code>IEVALUES</code>
 * and <code>IEPRIO</code> flags are used, and contain ident-like values.</li>
 * <li><code>SAC_COMPAT_PRIO</code> values are produced by the <code>IEPRIOCHAR</code> flag,
 * representing values that its compatible browser interprets as being of <code>!important</code>
 * priority.</li>
 * </ul>
 * </p>
 * <p>
 * Caution is advised when using these compatibility pseudo-values, as they may conflict with
 * syntax-conformant values.
 * </p>
 * <h2>Bugs</h2>
 * <p>
 * While this API offers an useful improvement to SAC, that API is old and is showing its
 * age; when it was written, CSS was simpler and smaller than it is today. As this API
 * reuses from SAC as much as possible there are obvious limitations with this approach,
 * like:
 * <ul>
 * <li><code>DocumentHandler.ignorableAtRule</code> is a big hammer and not appropriate
 * for today's variety of rules. For example, for <code>{@literal @}supports</code> rules
 * it would be better to have specialized <code>startSupports</code> and
 * <code>endSupports</code> methods than going through <code>ignorableAtRule</code>.</li>
 * <li>Media queries are unparsed and passed as plain media declarations.</li>
 * <li>Combinator selector interfaces were reused, avoiding the creation of a new
 * interface (like <i>e.g.</i> a plain <code>CombinatorSelector</code> interface). Again,
 * this is not elegant but this API tries to depart minimally from SAC (and this kind of
 * interface reuse was already being done by other SAC implementations).</li>
 * </ul>
 * <h2>W3C Copyright Notice</h2>
 * <p>
 * This software includes material derived from SAC
 * (<a href="https://www.w3.org/TR/SAC/">https://www.w3.org/TR/SAC/</a>). Copyright ©
 * 1999,2000 <a href="http://www.w3.org/">W3C</a>®
 * (<a href="http://www.csail.mit.edu/">MIT</a>, <a href="http://www.inria.fr/">INRIA</a>,
 * <a href="http://www.keio.ac.jp/">Keio</a>).
 * </p>
 */
package io.sf.carte.doc.style.css.nsac;
