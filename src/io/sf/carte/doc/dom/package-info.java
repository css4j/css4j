/**
 * This package provide an implementation of the
 * <a href="https://www.w3.org/TR/DOM-Level-3-Core/" target="_blank">Document
 * Object Model (DOM) Level 3 Core Specification</a> that can be used for XML or
 * HTML documents, albeit with a few deviations from the specification.
 * <p>
 * The following behavior is believed to be more user-friendly from the point of
 * view of a developer that is handling an HTML document, but is non-conformant:
 * <p>
 * <ol>
 * <li>On elements and attributes, <code>Node.getLocalName()</code> returns the
 * tag name instead of <code>null</code>, when the node was created with a DOM
 * Level 1 method such as Document.createElement(). In HTML documents, all the
 * elements have implicitly the HTML namespace unless they have a different
 * one.</li>
 * <li>As all the HTML elements have an implicit namespace and the idea is to
 * handle HTML and XHTML in the same way,
 * {@link io.sf.carte.doc.dom.DOMElement#getTagName() DOMElement.getTagName()}
 * does not return an upper-cased name.</li>
 * <li>The methods <code>Element.setIdAttribute</code>,
 * <code>Element.setIdAttributeNS</code> and
 * <code>Element.setIdAttributeNode</code> are now deprecated by W3C, but they
 * do work in this implementation. In HTML documents, only case changes to the
 * 'id' attribute (like 'ID' or 'Id') are allowed, and any change has
 * Document-wide effects (according to the HTML specification, there is only one
 * ID attribute in HTML).</li>
 * <li>Entity references are allowed as a last-resort solution in case that an
 * entity is unknown. No known current parser uses that, though. This limited
 * support for entity references may be dropped in future versions.</li>
 * <li>The class list obtained by <code>getClassList()</code> is not read-only:
 * changes to it are reflected in the attribute, and vice-versa.</li>
 * <li>Calling <code>normalize()</code> on a <code>STYLE</code> element sets its
 * text content to the contents of the associated style sheet.</li>
 * <li>The order of the element attributes is as specified, while other
 * implementations like the one shipped with most JDKs (<code>Xerces-j</code>)
 * do not enforce any particular order.</li>
 * <li>By default, not-<code>specified</code> attributes are not set, omitting
 * the <a href=
 * "https://www.w3.org/TR/2008/REC-xml-20081126/#sec-attr-defaults">default
 * value</a> if any.</li>
 * </ol>
 * <h3>Traversing the DOM</h3>
 * <p>
 * There are several alternative procedures to retrieve the child nodes of a
 * parent node. The most straightforward is also the fastest: get the first (or
 * last) child, and then iterate through the next (or previous) siblings:
 * </p>
 * 
 * <pre>
 * DOMNode node = getFirstChild();
 * while (node != null) {
 * 	someNodeProcessing(node); // do something with that node
 * 	node = node.getNextSibling();
 * }
 * </pre>
 * <p>
 * or, if you are used to <code>for</code> loops:
 * </p>
 * 
 * <pre>
 * for (DOMNode node = getFirstChild(); node != null; node = node.getNextSibling()) {
 * 	someNodeProcessing(node); // do something with that node
 * }
 * </pre>
 * <p>
 * The iterators are also fast:
 * </p>
 * 
 * <pre>
 * Iterator&lt;DOMNode&gt; it = parentNode.iterator();
 * while (it.hasNext()) {
 * 	DOMNode node = it.next();
 * 	someNodeProcessing(node); // do something with that node
 * }
 * </pre>
 * <p>
 * There are several different iterators, like the <code>elementIterator</code>:
 * </p>
 * 
 * <pre>
 * Iterator&lt;DOMElement&gt; it = parentNode.elementIterator();
 * while (it.hasNext()) {
 * 	DOMElement element = it.next();
 * 	someElementProcessing(element); // do something with that element
 * }
 * </pre>
 * <p>
 * or the <code>typeIterator</code>:
 * 
 * <pre>
 * Iterator&lt;Node&gt; it = parentNode.typeIterator(Node.PROCESSING_INSTRUCTION_NODE);
 * while (it.hasNext()) {
 * 	ProcessingInstruction pi = (ProcessingInstruction) it.next();
 * 	someProcessing(pi); // do something with that processing instruction
 * }
 * </pre>
 * <p>
 * Finally, the old {@link org.w3c.dom.NodeList NodeList} interface, which in
 * this library is implemented in the more modern flavours of
 * {@link io.sf.carte.doc.dom.DOMNodeList DOMNodeList} and
 * {@link io.sf.carte.doc.dom.ElementList ElementList}:
 * </p>
 * 
 * <pre>
 * NodeList list = parentNode.getChildNodes();
 * for (int i = 0; i &lt; list.getLength(); i++) {
 * 	Node node = list.item(i);
 * 	someNodeProcessing(node); // do something with that node
 * }
 * </pre>
 * <p>
 * which is a less efficient way to examine the child nodes, but still useful.
 * Using it as an <code>Iterable</code> is more efficient (be sure to use
 * <code>DOMNodeList</code> or <code>ElementList</code>):
 * </p>
 * 
 * <pre>
 * DOMNodeList list = parentNode.getChildNodes();
 * for (DOMNode node : list) {
 * 	someNodeProcessing(node);
 * }
 * </pre>
 * 
 * or:
 * 
 * <pre>
 * ElementList list = parentNode.getChildren();
 * for (DOMElement element : list) {
 * 	someElementProcessing(element);
 * 	// Let's do something with the attributes
 * 	AttributeNamedNodeMap attributes = element.getAttributes();
 * 	for (Attr attr : attributes) {
 * 		attr.setValue("foo");
 * 	}
 * }
 * </pre>
 * <p>
 * To iterate across the document (as opposed to just the child nodes), there is
 * the
 * {@link io.sf.carte.doc.dom.DOMDocument#createNodeIterator(org.w3c.dom.Node, int, NodeFilter)
 * createNodeIterator(Node, int, NodeFilter)} method. For example:
 * </p>
 * 
 * <pre>
 * NodeIterator it = document.createNodeIterator(document, NodeFilter.SHOW_ELEMENT, null);
 * while (it.hasNext()) {
 * 	DOMElement element = (DOMElement) it.next();
 * 	someElementProcessing(element); // do something with that element
 * }
 * </pre>
 * <p>
 * This library's version of {@link io.sf.carte.doc.dom.NodeIterator
 * NodeIterator} implements {@link java.util.ListIterator ListIterator}.
 * </p>
 * <p>
 * And finally the {@link io.sf.carte.doc.dom.TreeWalker TreeWalker}, which can
 * be created with the
 * {@link io.sf.carte.doc.dom.DOMDocument#createTreeWalker(org.w3c.dom.Node, int, NodeFilter)
 * createTreeWalker(Node, int, NodeFilter)} method:
 * </p>
 * 
 * <pre>
 * TreeWalker tw = document.createTreeWalker(document, NodeFilter.SHOW_ELEMENT, null);
 * DOMNode node;
 * while ((node = tw.nextNode()) != null) {
 * 	someNodeProcessing(node);
 * }
 * </pre>
 * 
 * <h3>Serializing the DOM</h3>
 * <p>
 * The class {@link io.sf.carte.doc.dom.DOMWriter DOMWriter} can be used to
 * pretty-print a document or a subtree. To do that, it takes into account the
 * default values of the <code>display</code> CSS property for the elements,
 * according to the user agent's default style sheet. Also allows to replace a
 * specified subset of codepoints with the proper entity references, when
 * serializing a <code>Text</code> node.
 * </p>
 */
package io.sf.carte.doc.dom;
