/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSRule;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSRuleList;
import io.sf.carte.doc.style.css.ExtendedCSSStyleRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import io.sf.carte.doc.xml.dtd.EntityFinder;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Serializes a node and its subtree.
 * <p>
 * To pretty-print a document (or any node), uses heuristics that take into
 * account the default values of the <code>display</code> CSS property for the
 * elements, according to a reference style sheet (by default, the user agent's
 * style sheet). And when serializing a <code>Text</code> node, also allows to
 * replace a specified subset of codepoints with the proper entity references.
 * </p>
 * <p>
 * This class could be subclassed to customize the printing.
 * </p>
 */
public class DOMWriter {

	private String indentingUnit = "  ";

	private final StringBuilder indentString = new StringBuilder(48);

	private HashMap<Integer, String> entityMap = null;
	private EntityResolver2 resolver = null;

	private ExtendedCSSStyleSheet<?> uaSheet;
	private HashMap<String, String> displayMap = null;

	private Node rootNode = null;

	/**
	 * Construct a <code>DOMWriter</code> with default settings.
	 */
	public DOMWriter() {
		super();
	}

	/**
	 * Construct a <code>DOMWriter</code> with a reference style sheet for
	 * formatting heuristics.
	 * <p>
	 * Use this constructor when you know that the user agent's sheet is not
	 * adequate for the nodes that you are going to print (or that you'll be using
	 * always the same reference sheet, and you want to spare the retrieval of the
	 * UA sheet for each {@link #writeNode(Node, SimpleWriter)} call).
	 * </p>
	 * 
	 * @param refSheet the reference sheet.
	 */
	public DOMWriter(ExtendedCSSStyleSheet<? extends ExtendedCSSRule> refSheet) {
		super();
		uaSheet = refSheet;
	}

	/**
	 * Configure the writer to replace the given codePoints by entity references (if
	 * the document type allows it).
	 * <p>
	 * If you want to use a specific <code>EntityResolver2</code> in the process, be
	 * sure to call {@link #setEntityResolver(EntityResolver2)} first.
	 * 
	 * @param docType  the document type.
	 * @param entities the codePoints that should be replaced by entity references.
	 * @return the number of requested entities that could be found.
	 * @throws IOException          if an I/O problem occurred reading the DTD.
	 * @throws SAXException         if a DTD-related error happened.
	 * @throws NullPointerException if either <code>docType</code> or
	 *                              <code>entities</code> are <code>null</code>.
	 */
	public int setEntityCodepoints(DocumentType docType, int[] entities) throws SAXException, IOException {
		if (docType == null || entities == null) {
			throw new NullPointerException();
		}
		int ret = 0;
		if (entityMap == null) {
			entityMap = new HashMap<Integer, String>(entities.length + 2);
			entityMap.put(60, "lt");
			entityMap.put(62, "gt");
		}
		for (int i = 0; i < entities.length; i++) {
			entityMap.putIfAbsent(entities[i], null);
		}
		if (resolver == null) {
			resolver = new DefaultEntityResolver();
		}
		InputSource is = resolver.resolveEntity(docType.getName(), docType.getPublicId(), docType.getBaseURI(),
				docType.getSystemId());
		if (is != null) {
			EntityFinder finder = new EntityFinder(resolver);
			Reader re = is.getCharacterStream();
			ret = finder.findEntities(entityMap, re);
			re.close();
		}
		return ret;
	}

	/**
	 * Sets the entity resolver to be used to read the <code>DTD</code>.
	 * <p>
	 * If no entity resolver is set, this library's default resolver will be used.
	 * 
	 * @param resolver the entity resolver.
	 */
	public void setEntityResolver(EntityResolver2 resolver) {
		this.resolver = resolver;
	}

	/**
	 * Sets a whitespace string of the <code>whitespaceCount</code> length as the
	 * level indenting unit (that is, every indenting depth shall be
	 * <code>whitespaceCount</code> larger).
	 * 
	 * @param whitespaceCount the length of the indenting unit.
	 * @throws IllegalArgumentException if the <code>whitespaceCount</code> is
	 *                                  negative.
	 */
	public void setIndentingUnit(int whitespaceCount) {
		if (whitespaceCount < 0) {
			throw new IllegalArgumentException("Negative count");
		}
		StringBuilder buf = new StringBuilder(whitespaceCount);
		for (int i = 0; i < whitespaceCount; i++) {
			buf.append(' ');
		}
		this.indentingUnit = buf.toString();
	}

	/**
	 * Create a <code>DOMWriter</code> object, use it to serialize the given node
	 * and descendants.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Document document = ...
	 * BufferSimpleWriter writer = new BufferSimpleWriter();
	 * DOMWriter.writeTree(document, writer);
	 * System.out.println(writer.toString());
	 * </pre>
	 * 
	 * @param root   the root node.
	 * @param writer the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	public static void writeTree(Node root, SimpleWriter writer) throws IOException {
		DOMWriter domWriter = new DOMWriter();
		domWriter.writeNode(root, writer);
	}

	/**
	 * Serializes <code>root</code> into a <code>String</code> using the
	 * configurable serialization provided by this class.
	 * <p>
	 * Named for similarity with W3C's <code>XMLSerializer</code> interface.
	 * 
	 * @param root the root node.
	 * @return the serialization.
	 */
	public String serializeToString(Node root) {
		BufferSimpleWriter writer = new BufferSimpleWriter(512);
		try {
			writeNode(root, writer);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return writer.toString();
	}

	/**
	 * Serialize the given node and descendants.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Document document = ...
	 * BufferSimpleWriter writer = new BufferSimpleWriter();
	 * DOMWriter domwriter = new DOMWriter(document);
	 * domwriter.writeNode(document, writer);
	 * System.out.println(writer.toString());
	 * </pre>
	 * 
	 * @param root   the node.
	 * @param writer the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	public void writeNode(Node root, SimpleWriter writer) throws IOException {
		this.rootNode = root;
		ExtendedCSSStyleSheet<?> oldUaSheet = uaSheet;
		if (oldUaSheet == null) {
			DOMDocument doc = (DOMDocument) getOwnerDocument();
			uaSheet = doc.getImplementation().getUserAgentStyleSheet(doc.getComplianceMode());
		}
		writeNode(root, writer, true);
		uaSheet = oldUaSheet;
		this.rootNode = null;
	}

	/**
	 * Serialize the given node and descendants, with an initial indenting context.
	 * 
	 * @param node     the node.
	 * @param wri   the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeNode(Node node, SimpleWriter wri, boolean indented) throws IOException {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			writeElement((DOMElement) node, wri, indented);
			break;
		case Node.TEXT_NODE:
			Text text = (Text) node;
			if (!text.isElementContentWhitespace()) {
				writeText(text, wri, indented);
			} else {
				writeElementContentWhitespace(text, wri);
			}
			break;
		case Node.CDATA_SECTION_NODE:
			writeCDataSection((CDATASection) node, wri);
			break;
		case Node.COMMENT_NODE:
			writeComment((Comment) node, wri, indented);
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			writeProcessingInstruction((ProcessingInstruction) node, wri);
			break;
		case Node.DOCUMENT_TYPE_NODE:
			writeDocumentType((DocumentType) node, wri);
			break;
		case Node.DOCUMENT_NODE:
		case Node.DOCUMENT_FRAGMENT_NODE:
			if (node.hasChildNodes()) {
				writeChildNodes(node, wri, indented);
			}
			break;
		case Node.ENTITY_REFERENCE_NODE:
			writeEntityReference(node, wri);
			break;
		default:
			wri.write(node.toString());
		}
	}

	/**
	 * Serialize the given element and descendants, with an initial indenting
	 * context.
	 * 
	 * @param element  the element.
	 * @param wri      the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeElement(DOMElement element, SimpleWriter wri, boolean indented) throws IOException {
		if (indented) {
			startIndentedNode(element, wri);
		}
		String tagname = element.getTagName();
		wri.write('<');
		wri.write(tagname);
		writeAttributes(element.getAttributes(), wri);
		if (!element.isVoid()) {
			wri.write('>');
			boolean ast = afterStartTag(element, wri);
			if (element.hasChildNodes()) {
				if (ast) {
					startIndentedNodeList(element, wri);
				}
				writeChildNodes(element, wri, ast);
				if (ast) {
					endIndentedNodeList(element, wri);
				}
			}
			if (ast) {
				writeFullIndent(wri);
			}
			wri.write("</");
			wri.write(tagname);
			wri.write(">");
		} else {
			closeEmptyElementTag(wri);
		}
		if (indented) {
			endIndentedNode(element, wri);
		}
	}

	/**
	 * Serialize the attributes of an element.
	 * <p>
	 * Only explicitly specified attributes are serialized.
	 * 
	 * @param nodeMap the attribute map.
	 * @param wri     the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeAttributes(AttributeNamedNodeMap nodeMap, SimpleWriter wri) throws IOException {
		for (Attr attr : nodeMap) {
			if (attr.getSpecified()) {
				wri.write(' ');
				writeAttribute(attr, wri);
			}
		}
	}

	/**
	 * Serialize an element attribute.
	 * 
	 * @param attr the attribute.
	 * @param wri  the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeAttribute(Attr attr, SimpleWriter wri) throws IOException {
		((DOMAttr) attr).write(wri);
	}

	/**
	 * Serialize a list of (child) nodes.
	 * 
	 * @param parent   the parent node.
	 * @param wri      the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeChildNodes(Node parent, SimpleWriter wri, boolean indented) throws IOException {
		DOMNodeList list = ((DOMNode) parent).getChildNodes();
		for (DOMNode node : list) {
			writeNode(node, wri, indented);
		}
	}

	/**
	 * Serialize a <code>Text</code> node.
	 * 
	 * @param text     the text node.
	 * @param wri      the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeText(Text text, SimpleWriter wri, boolean indented) throws IOException {
		boolean doIndent = indented && !previousSiblingWasTextOrERef(text);
		Node node = text.getParentNode();
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE && isRawTextElement((DOMElement) node)) {
			// raw text element
			String parentLName = node.getLocalName();
			writeRawText(text, parentLName, wri);
		} else {
			writeNonRawText(text, wri, doIndent);
		}
	}

	private boolean previousSiblingWasTextOrERef(Text text) {
		Node previous = text.getPreviousSibling();
		short type;
		return previous != null
				&& ((type = previous.getNodeType()) == Node.TEXT_NODE || type == Node.ENTITY_REFERENCE_NODE);
	}

	protected boolean isRawTextElement(DOMElement element) {
		return element.isRawText();
	}

	/**
	 * Serialize a <code>Text</code> node that is child of a raw text element.
	 * <p>
	 * See <a href="https://www.w3.org/TR/html51/syntax.html#raw-text">Raw text
	 * elements</a> and also <code>escapable raw text elements</code>.
	 * 
	 * @param text the text node to be serialized as raw text.
	 * @param wri  the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeRawText(Text text, String parentLocalName, SimpleWriter wri) throws IOException {
		wri.write(DOMDocument.escapeCloseTag(parentLocalName, text.getData()));
	}

	/**
	 * Serialize a <code>Text</code> node that is not the child of a raw text
	 * element (that is, a normal <code>Text</code> node).
	 * 
	 * @param text     the text node to be serialized as normal text.
	 * @param wri      the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeNonRawText(Text text, SimpleWriter wri, boolean indented) throws IOException {
		String s = text.getData();
		if (indented) {
			String last = s;
			StringTokenizer st = new StringTokenizer(s, "\n");
			while (st.hasMoreTokens()) {
				writeFullIndent(wri);
				last = st.nextToken();
				writeTextLine(last, wri);
			}
			if (!last.endsWith("\n")) {
				endIndentedNode(text, wri);
			}
		} else {
			writeTextLine(s, wri);
		}
	}

	/**
	 * Serialize a line of a normal <code>Text</code> node.
	 * <p>
	 * If there is the mandate to replace characters by entity references, call
	 * {@link #replaceByEntities(String)} before writing.
	 * 
	 * @param line the line to write.
	 * @param wri  the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeTextLine(String line, SimpleWriter wri) throws IOException {
		if (line.length() != 0) {
			if (entityMap != null) {
				try {
					line = replaceByEntities(line);
				} catch (SAXException e) {
				}
			} else {
				line = DOMDocument.escapeLtGtEntities(line);
			}
			wri.write(line);
		}
	}

	/**
	 * Verify if the given line contains characters for which there is the mandate
	 * to replace by entity references.
	 * 
	 * @param line the line where the characters should be replaced.
	 * @return the line, with the relevant characters replaced by entity references.
	 * @throws SAXException if there was a non-I/O problem handling the DTD.
	 * @throws IOException  if an I/O problem occurred while reading a DTD or
	 *                      serializing.
	 */
	protected String replaceByEntities(String line) throws SAXException, IOException {
		StringBuilder buf = null;
		int len = line.length();
		for (int i = 0; i < len; i = line.offsetByCodePoints(i, 1)) {
			int cp = line.codePointAt(i);
			if (entityMap.containsKey(cp)) {
				String entity = entityMap.get(cp);
				if (buf == null) {
					buf = new StringBuilder(len + 64);
					buf.append(line.subSequence(0, i));
				}
				buf.append('&');
				if (entity != null) {
					buf.append(entity);
				} else {
					buf.append('#').append(Integer.toString(cp));
				}
				buf.append(';');
				continue;
			}
			if (buf != null) {
				buf.append(Character.toChars(cp));
			}
		}
		return buf == null ? line : buf.toString();
	}

	private Document getOwnerDocument() {
		Document doc;
		if (rootNode.getNodeType() == Node.DOCUMENT_NODE) {
			doc = (Document) rootNode;
		} else {
			doc = rootNode.getOwnerDocument();
		}
		return doc;
	}

	/**
	 * Serialize element content whitespace.
	 * <p>
	 * By default, this method does nothing (the whole idea of pretty-printing is to
	 * ignore those non-structural text nodes and do your own formatting).
	 * 
	 * @param text the text node.
	 * @param wri  the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeElementContentWhitespace(Text text, SimpleWriter wri) throws IOException {
	}

	/**
	 * Serialize a CDATA section.
	 * 
	 * @param data the CDATA section.
	 * @param wri  the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeCDataSection(CDATASection data, SimpleWriter wri) throws IOException {
		startIndentedNode(data, wri);
		wri.write("<![CDATA[");
		wri.write(data.getData());
		wri.write("]]>");
		endIndentedNode(data, wri);
	}

	/**
	 * Serialize a comment.
	 * 
	 * @param comment  the comment node.
	 * @param wri      the writer.
	 * @param indented <code>true</code> if the current behaviour (specified by
	 *                 parent or by direct method call) is to put each child node on
	 *                 its own line and indent it.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeComment(Comment comment, SimpleWriter wri, boolean indented) throws IOException {
		if (indented) {
			startIndentedNode(comment, wri);
		}
		wri.write("<!--");
		wri.write(comment.getData());
		wri.write("-->");
		if (indented) {
			endIndentedNode(comment, wri);
		}
	}

	/**
	 * Serialize a <code>DOCTYPE</code> node.
	 * 
	 * @param docType the <code>DocumentType</code> node.
	 * @param wri     the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeDocumentType(DocumentType docType, SimpleWriter wri) throws IOException {
		startIndentedNode(docType, wri);
		String systemId = docType.getSystemId();
		boolean hasSystemId = systemId != null;
		wri.write("<!DOCTYPE ");
		String name = docType.getName();
		name = DOMAttr.escapeAttributeEntities(name);
		wri.write(name);
		String publicId = docType.getPublicId();
		if (publicId != null) {
			wri.write(" PUBLIC \"");
			wri.write(DOMAttr.escapeAttributeEntities(publicId));
			wri.write('"');
		} else if (hasSystemId) {
			wri.write(" SYSTEM");
		}
		if (hasSystemId) {
			wri.write(" \"");
			wri.write(DOMAttr.escapeAttributeEntities(systemId));
			wri.write('"');
		}
		wri.write('>');
		endIndentedNode(docType, wri);
	}

	/**
	 * Serialize a <code>ProcessingInstruction</code> node.
	 * 
	 * @param pi  the <code>ProcessingInstruction</code> node.
	 * @param wri the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeProcessingInstruction(ProcessingInstruction pi, SimpleWriter wri) throws IOException {
		startIndentedNode(pi, wri);
		wri.write("<?");
		wri.write(pi.getTarget());
		wri.write(' ');
		wri.write(pi.getData());
		wri.write("?>");
		endIndentedNode(pi, wri);
	}

	protected void writeEntityReference(Node node, SimpleWriter wri) throws IOException {
		wri.write('&');
		wri.write(node.getNodeName());
		wri.write(';');
	}

	protected void startIndentedNodeList(Node parent, SimpleWriter wri) throws IOException {
		deepenIndent();
	}

	protected void startIndentedNode(Node node, SimpleWriter wri) throws IOException {
		writeFullIndent(wri);
	}

	protected void endIndentedNode(Node node, SimpleWriter wri) throws IOException {
		wri.newLine();
	}

	protected void endIndentedNodeList(Node listParent, SimpleWriter wri) throws IOException {
		updateIndent(listParent);
	}

	/**
	 * Take decisions after serializing the start tag of an element.
	 * 
	 * @param element the element whose start tag was serialized.
	 * @param wri     the writer.
	 * @return <code>true</code> if children of the given element should be
	 *         serialized indented.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected boolean afterStartTag(DOMElement element, SimpleWriter wri) throws IOException {
		boolean indentChild = false;
		ElementList elist = element.getChildren();
		if (elist.getLength() != 0) {
			for (DOMElement el : elist) {
				String display = getDisplayProperty(el);
				if ("block".equalsIgnoreCase(display) || "table".equalsIgnoreCase(display)
						|| "table-row".equalsIgnoreCase(display)) {
					indentChild = true;
					break;
				}
			}
		}
		String tc = element.getTextContent();
		if (!indentChild) {
			indentChild = tc.length() > 64 || tc.indexOf('\n') != -1;
		}
		if (indentChild && !tc.startsWith("\n")) {
			wri.newLine();
		}
		return indentChild;
	}

	/**
	 * Get the value of the <code>display</code> CSS property as specified in the
	 * user agent style sheet for that element.
	 * 
	 * @param element the element.
	 * @return the value of the <code>display</code> property, or <code>null</code>
	 *         if no rule with that type selector defining that property was found.
	 */
	protected String getDisplayProperty(Element element) {
		String localName = element.getLocalName();
		if (displayMap == null) {
			displayMap = new HashMap<String, String>();
		}
		String display = displayMap.get(localName);
		if (display == null && uaSheet != null) {
			ExtendedCSSRuleList<? extends ExtendedCSSRule> list = uaSheet.getRulesForProperty("display");
			if (list != null) {
				Iterator<? extends ExtendedCSSRule> it = list.iterator();
				while (it.hasNext()) {
					ExtendedCSSRule rule = it.next();
					if (rule.getType() == CSSRule.STYLE_RULE) {
						ExtendedCSSStyleRule stylerule = (ExtendedCSSStyleRule) rule;
						SelectorList selist = stylerule.getSelectorList();
						for (int i = 0; i < selist.getLength(); i++) {
							Selector sel = selist.item(i);
							if (sel.getSelectorType() == Selector.SAC_ELEMENT_NODE_SELECTOR
									&& localName.equals(((ElementSelector) sel).getLocalName())) {
								display = stylerule.getStyle().getPropertyValue("display");
								if (display != null) {
									displayMap.put(localName, display);
									return display;
								}
							}
						}
					}
				}
			}
		}
		return display;
	}

	/**
	 * Close an empty element tag.
	 * <p>
	 * This method could be overridden to, for example, not let any whitespace
	 * before the slash.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void closeEmptyElementTag(SimpleWriter wri) throws IOException {
		wri.write(" />");
	}

	/**
	 * Deepen the indenting level.
	 */
	protected void deepenIndent() {
		indentString.append(indentingUnit);
	}

	/**
	 * Update the indenting level to the parent of the given node.
	 * 
	 * @param node the current parent node.
	 */
	protected void updateIndent(Node node) {
		indentString.setLength(0);
		Node parent = node.getParentNode();
		if (parent != null) {
			while (parent != rootNode) {
				deepenIndent();
				parent = parent.getParentNode();
			}
		}
	}

	/**
	 * Write a full indent to the writer.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem occurred while writing.
	 */
	protected void writeFullIndent(SimpleWriter wri) throws IOException {
		wri.write(indentString);
	}

}
