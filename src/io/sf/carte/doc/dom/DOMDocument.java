/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.stylesheets.LinkStyle;
import org.xml.sax.SAXException;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.dom.DOMElement.ClassList;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DOMUtil;
import io.sf.carte.doc.style.css.om.DefaultErrorHandler;
import io.sf.carte.doc.style.css.om.MediaList;
import io.sf.carte.doc.style.css.om.StyleSheetList;
import io.sf.carte.doc.xml.dtd.ContentModel;

/**
 * <p>
 * Implementation of a DOM <code>Document</code>.
 * </p>
 */
abstract public class DOMDocument extends DOMParentNode implements CSSDocument {

	static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
	static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

	private boolean strictErrorChecking = true;

	private String documentURI = null;

	final Set<LinkStyleDefiner> linkedStyle = new LinkedHashSet<LinkStyleDefiner>(4);

	final Set<LinkStyleDefiner> embeddedStyle = new LinkedHashSet<LinkStyleDefiner>(3);

	private BaseDocumentCSSStyleSheet mergedStyleSheet = null;

	private final MyOMStyleSheetList sheets = new MyOMStyleSheetList(7);

	private final ErrorHandler errorHandler = createErrorHandler();

	/*
	 * Default style set according to 'Default-Style' meta.
	 */
	private String metaDefaultStyleSet = "";

	/*
	 * Default referrer policy according to 'Referrer-Policy' header/meta.
	 */
	private String metaReferrerPolicy = "";

	private String lastStyleSheetSet = null;

	private String targetMedium = null;

	private Map<String, CSSCanvas> canvases = new HashMap<String, CSSCanvas>(3);

	public DOMDocument(DocumentType documentType) {
		super(Node.DOCUMENT_NODE);
		if (documentType != null && documentType.getOwnerDocument() == null) {
			DocumentTypeImpl doctype = (DocumentTypeImpl) documentType;
			getNodeList().add(doctype);
			doctype.setParentNode(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CSSDocument.ComplianceMode getComplianceMode() {
		DocumentType doctype = getDoctype();
		if (doctype != null) {
			return CSSDocument.ComplianceMode.STRICT;
		}
		return CSSDocument.ComplianceMode.QUIRKS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMElement getDocumentElement() {
		return getLastElementChild();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentType getDoctype() {
		Node node = getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
				return (DocumentType) node;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNodeName() {
		return "#document";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMDocument getOwnerDocument() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode getParentNode() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVisitedURI(String href) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMDocument cloneNode(boolean deep) {
		DocumentType docType = getDoctype();
		boolean hasDocType = docType != null;
		if (hasDocType) {
			docType = (DocumentType) docType.cloneNode(deep);
		}
		// We need docType regardless of deep being true, to obtain the right
		// type of Document.
		DOMDocument doc = cloneDocument(docType);
		if (deep) {
			boolean foundDoctype = !hasDocType;
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
					if (foundDoctype) {
						doc.appendChild(doc.importNode(node, true));
					} else {
						doc.insertBefore(doc.importNode(node, true), docType);
					}
				} else {
					foundDoctype = true;
				}
				node = node.getNextSibling();
			}
		} else if (hasDocType) {
			// Remove docType
			doc.removeChild(docType);
		}
		callUserHandlers(UserDataHandler.NODE_CLONED, this, doc);
		return doc;
	}

	/**
	 * Produce a document identical to this one, with a document type (if any), but
	 * without a document element.
	 * 
	 * @param docType the document type of this document.
	 * @return the cloned document.
	 */
	DOMDocument cloneDocument(DocumentType docType) {
		String nsUri = null;
		String qName = null;
		DOMElement docElm = getDocumentElement();
		if (docElm != null) {
			nsUri = docElm.getNamespaceURI();
			qName = docElm.getTagName();
		}
		if (nsUri == null && !"html".equals(qName)
				&& (docType == null || !"html".equalsIgnoreCase(docType.getName()))) {
			nsUri = "";
		}
		// We need docType regardless of deep being true, to obtain the right
		// type of Document.
		DOMDocument doc = getImplementation().createDocument(nsUri, qName, docType);
		if (docElm != null) {
			doc.removeChild(doc.getDocumentElement());
		}
		return doc;
	}

	/**
	 * Get the DOM implementation that created this document.
	 * 
	 * @return the DOM implementation that created this document.
	 */
	@Override
	abstract public CSSDOMImplementation getImplementation();

	protected abstract CSSDOMImplementation getStyleSheetFactory();

	/*
	 * Abstract class to be inherited by nodes that are not a Document node
	 */
	abstract class MyNode extends NDTNode {

		MyNode(short nodeType) {
			super(nodeType);
		}

		@Override
		void checkAppendNode(Node newChild) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot append the node to " + getNodeName());
		}

		@Override
		public DOMDocument getOwnerDocument() {
			return DOMDocument.this;
		}

		@Override
		public String getBaseURI() {
			return DOMDocument.this.getBaseURI();
		}

		@Override
		public Node cloneNode(boolean deep) {
			return this;
		}

	}

	class DOMDocumentFragment extends DOMParentNode implements DocumentFragment {

		DOMDocumentFragment() {
			super(Node.DOCUMENT_FRAGMENT_NODE);
		}

		@Override
		public String getNodeName() {
			return "#document-fragment";
		}

		@Override
		public String lookupNamespaceURI(String prefix) {
			return null;
		}

		@Override
		void checkAppendNodeHierarchy(Node newChild) {
			super.checkAppendNodeHierarchy(newChild);
			if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Doctype must be added to document.");
			}
		}

		@Override
		public DOMDocument getOwnerDocument() {
			return DOMDocument.this;
		}

		@Override
		public String getBaseURI() {
			return DOMDocument.this.getBaseURI();
		}

		@Override
		public DOMDocumentFragment cloneNode(boolean deep) {
			DOMDocumentFragment my = new DOMDocumentFragment();
			if (deep) {
				Node node = getFirstChild();
				while (node != null) {
					my.appendChild(node.cloneNode(true));
					node = node.getNextSibling();
				}
			}
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

		@Override
		public String toString() {
			int sz = getChildNodes().getLength();
			StringBuilder buf = new StringBuilder(64 + sz * 32);
			Node node = getFirstChild();
			while (node != null) {
				buf.append(node.toString());
				node = node.getNextSibling();
			}
			return buf.toString();
		}
	}

	class MyEntityReference extends MyNode implements EntityReference {

		private final String name;

		MyEntityReference(String name) {
			super(Node.ENTITY_REFERENCE_NODE);
			this.name = name;
		}

		@Override
		public String getNodeName() {
			return name;
		}

		@Override
		void checkAppendNodeHierarchy(Node newChild) {
			super.checkAppendNodeHierarchy(newChild);
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"This implementation does not support appending nodes to an entity reference.");
		}

		@Override
		public String lookupNamespaceURI(String prefix) {
			return null;
		}

		@Override
		public String toString() {
			return '&' + name + ';';
		}
	}

	class MyProcessingInstruction extends MyNode implements ProcessingInstruction {
		String data;
		private final String target;

		MyProcessingInstruction(String target, String data) {
			super(Node.PROCESSING_INSTRUCTION_NODE);
			this.target = target;
			this.data = data;
		}

		@Override
		public String getNodeName() {
			return getTarget();
		}

		@Override
		public String getData() {
			return data;
		}

		@Override
		public String getTarget() {
			return target;
		}

		@Override
		public void setData(String data) throws DOMException {
			if (data.indexOf('>') != -1) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "PI is not allowed to contain '>'");
			}
			this.data = data;
		}

		@Override
		public String getNodeValue() throws DOMException {
			return getData();
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			setData(nodeValue);
		}

		@Override
		public ProcessingInstruction cloneNode(boolean deep) {
			ProcessingInstruction my = new MyProcessingInstruction(getTarget(), getData());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

		@Override
		public String toString() {
			return "<?" + getTarget() + " " + getData() + "?>";
		}
	}

	interface LinkStyleDefiner extends LinkStyle, Node {
		@Override
		AbstractCSSStyleSheet getSheet();

		void resetLinkedSheet();
	}

	interface LinkStyleProcessingInstruction extends LinkStyleDefiner, ProcessingInstruction {
		String getPseudoAttribute(String name);
	}

	class MyStyleProcessingInstruction extends MyProcessingInstruction implements LinkStyleProcessingInstruction {
		private AbstractCSSStyleSheet linkedSheet = null;
		private final LinkedHashMap<String, String> pseudoAttrs = new LinkedHashMap<String, String>();

		MyStyleProcessingInstruction(String data) {
			super("xml-stylesheet", data);
			parseData();
		}

		@Override
		public void setData(String data) throws DOMException {
			super.setData(data);
			parseData();
			resetLinkedSheet();
			if (getParentNode() != null) {
				DOMDocument.this.onSheetModify();
			}
		}

		private void parseData() throws DOMException {
			DOMUtil.parsePseudoAttributes(getData(), pseudoAttrs);
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) throws DOMException {
			super.setParentNode(parentNode);
			// Rescan of sheets may be required
			onSheetModify();
		}

		@Override
		public AbstractCSSStyleSheet getSheet() {
			if (linkedSheet == null) {
				String type = getPseudoAttribute("type");
				if (type.length() != 0 && !"text/css".equals(type)) {
					return null;
				}
				MediaQueryList media = parseMediaList(getPseudoAttribute("media").trim(), this);
				if (media == null) {
					return null;
				}
				String title = getPseudoAttribute("title");
				if (title.length() == 0) {
					title = null;
				}
				boolean alternate = "yes".equalsIgnoreCase(getPseudoAttribute("alternate"));
				if (alternate && title == null) {
					getErrorHandler().linkedStyleError(this, "Alternate sheet without title");
					return null;
				}
				String href = getPseudoAttribute("href");
				int hreflen = href.length();
				if (hreflen > 1) {
					if (href.charAt(0) != '#') {
						linkedSheet = loadStyleSheet(linkedSheet, href, title, media, this);
					} else {
						String id = href.substring(1);
						DOMElement elm = getElementById(id);
						if (elm != null) {
							String text = elm.getTextContent().trim();
							linkedSheet = parseEmbeddedStyleSheet(linkedSheet, text, title, media, this);
						} else {
							getErrorHandler().linkedStyleError(this, "Could not find element with id: " + id);
						}
					}
					if (alternate && linkedSheet != null) {
						linkedSheet.setDisabled(true);
					}
				} else {
					getErrorHandler().linkedStyleError(this, "Missing or void href pseudo-attribute.");
				}
			}
			return linkedSheet;
		}

		@Override
		public String getPseudoAttribute(String attrname) {
			String value = pseudoAttrs.get(attrname);
			if (value == null) {
				value = "";
			}
			return value;
		}

		@Override
		public void resetLinkedSheet() {
			linkedSheet = null;
			getOwnerDocument().onSheetModify();
		}

		@Override
		public ProcessingInstruction cloneNode(boolean deep) {
			ProcessingInstruction my = new MyStyleProcessingInstruction(getData());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

	}

	MediaQueryList parseMediaList(String media, Node node) {
		MediaQueryList mediaList;
		if (media.length() == 0) {
			mediaList = MediaList.createMediaList();
		} else {
			mediaList = getStyleSheetFactory().createMediaQueryList(media, node);
			if (mediaList.isNotAllMedia() && mediaList.hasErrors()) {
				return null;
			}
		}
		return mediaList;
	}

	AbstractCSSStyleSheet loadStyleSheet(AbstractCSSStyleSheet sheet, String href, String title, MediaQueryList media,
			Node ownerNode) {
		if (sheet == null) {
			sheet = getStyleSheetFactory().createLinkedStyleSheet(ownerNode, title, media);
		} else {
			CSSDOMImplementation.MyCSSStyleSheet mysheet = (CSSDOMImplementation.MyCSSStyleSheet) sheet;
			mysheet.setTitle(title);
			mysheet.setMedia(media);
			mysheet.getCssRules().clear();
		}
		String referrerPolicy = getReferrerpolicyAttribute(ownerNode);
		try {
			URL url = getURL(href);
			sheet.setHref(url.toExternalForm());
			sheet.loadStyleSheet(url, referrerPolicy);
		} catch (Exception e) {
			getErrorHandler().linkedSheetError(e, sheet);
		}
		return sheet;
	}

	private String getReferrerpolicyAttribute(Node node) {
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null) {
			Node rp = nnm.getNamedItem("referrerpolicy");
			if (rp != null) {
				return rp.getNodeValue();
			}
		}
		return "";
	}

	AbstractCSSStyleSheet parseEmbeddedStyleSheet(AbstractCSSStyleSheet sheet, String styleText, String title,
			MediaQueryList media, Node ownerNode) {
		if (sheet == null) {
			sheet = getStyleSheetFactory().createLinkedStyleSheet(ownerNode, title, media);
		} else {
			CSSDOMImplementation.MyCSSStyleSheet mysheet = (CSSDOMImplementation.MyCSSStyleSheet) sheet;
			mysheet.setTitle(title);
			mysheet.setMedia(media);
			mysheet.getCssRules().clear();
		}
		sheet.setHref(getBaseURI());
		if (styleText.length() != 0) {
			InputSource source = new InputSource();
			Reader re = new StringReader(styleText);
			source.setCharacterStream(re);
			try {
				sheet.parseStyleSheet(source);
			} catch (Exception e) {
				getErrorHandler().linkedSheetError(e, sheet);
			}
		} else {
			sheet.getCssRules().clear();
		}
		return sheet;
	}

	abstract class MyCharacterData extends MyNode implements CharacterData {

		String data = null;

		MyCharacterData(short nodeType) {
			super(nodeType);
		}

		@Override
		void checkAppendNodeHierarchy(Node newChild) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot append the node to text/comment/cdatasection");
		}

		@Override
		public String getData() throws DOMException {
			return data;
		}

		@Override
		public void setData(String data) throws DOMException {
			this.data = data;
		}

		@Override
		public int getLength() {
			return data.length();
		}

		@Override
		public String substringData(int offset, int count) throws DOMException {
			int dl = data.length();
			int oc = offset + count;
			if (offset <= dl && oc >= dl) {
				return data;
			}
			try {
				return data.substring(offset, oc);
			} catch (IndexOutOfBoundsException e) {
				DOMException ex = new DOMException(DOMException.INDEX_SIZE_ERR, e.getMessage());
				ex.initCause(e);
				throw ex;
			}
		}

		@Override
		public void appendData(String arg) throws DOMException {
			StringBuilder buf = new StringBuilder(data.length() + arg.length());
			buf.append(data).append(arg);
			setData(buf.toString());
		}

		@Override
		public void insertData(int offset, String arg) throws DOMException {
			int dl = data.length();
			if (offset < 0 || offset > dl) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, "Wrong arguments");
			}
			StringBuilder buf = new StringBuilder(dl + arg.length());
			buf.append(data.subSequence(0, offset)).append(arg).append(data.subSequence(offset, dl));
			setData(buf.toString());
		}

		@Override
		public void deleteData(int offset, int count) throws DOMException {
			int dl = data.length();
			if (offset < 0 || count < 0 || offset >= dl) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, "Wrong arguments");
			}
			int begin2 = offset + count;
			if (begin2 > dl) {
				begin2 = dl;
				count = dl - offset;
			}
			StringBuilder buf = new StringBuilder(dl - count);
			buf.append(data.subSequence(0, offset)).append(data.subSequence(begin2, dl));
			setData(buf.toString());
		}

		@Override
		public void replaceData(int offset, int count, String arg) throws DOMException {
			int dl = data.length();
			StringBuilder buf = new StringBuilder(dl + arg.length() - count);
			try {
				buf.append(data.subSequence(0, offset)).append(arg);
				if (offset + count < dl) {
					buf.append(data.subSequence(offset + count, dl));
				}
			} catch (IndexOutOfBoundsException e) {
				DOMException ex = new DOMException(DOMException.INDEX_SIZE_ERR, e.getMessage());
				ex.initCause(e);
				throw ex;
			}
			setData(buf.toString());
		}

		@Override
		public String getNodeValue() {
			return getData();
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			setData(nodeValue);
		}

	}

	class MyComment extends MyCharacterData implements Comment {

		MyComment() {
			super(Node.COMMENT_NODE);
		}

		@Override
		public String getNodeName() {
			return "#comment";
		}

		@Override
		public void setData(String data) throws DOMException {
			if (data.contains("-->")) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Comment cannot contain '--'");
			}
			super.setData(data);
		}

		@Override
		public Comment cloneNode(boolean deep) {
			Comment my = new MyComment();
			my.setData(getData());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

		@Override
		public String toString() {
			return "<!--" + getData() + "-->";
		}
	}

	class MyText extends MyCharacterData implements Text {

		boolean elementContentWhitespace = false;

		MyText() {
			super(Node.TEXT_NODE);
		}

		MyText(short nodeType) {
			super(nodeType);
		}

		@Override
		void setParentNode(AbstractDOMNode parentNode) {
			DOMNode oldParent = getParentNode();
			if (oldParent != null && oldParent.getNodeType() == Node.ELEMENT_NODE) {
				onDOMChange((DOMElement) oldParent);
			}
			if (parentNode == null) {
				super.setParentNode(parentNode);
			} else {
				super.setParentNode(parentNode);
				if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
					onDOMChange((DOMElement) parentNode);
				}
			}
		}

		@Override
		public void setData(String data) throws DOMException {
			super.setData(data);
			DOMNode parent = getParentNode();
			if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
				onDOMChange((DOMElement) parent);
			}
		}

		void onDOMChange(DOMElement container) {
			LinkStyleDefiner definer = getEmbeddedStyleDefiner(container);
			if (definer != null) {
				definer.resetLinkedSheet();
			}
		}

		@Override
		public Text splitText(int offset) throws DOMException {
			Text newnode;
			Node parent = getParentNode();
			try {
				String newdata = data.substring(0, offset);
				newnode = getOwnerDocument().createTextNode(data.substring(offset, data.length()));
				if (parent != null) {
					parent.insertBefore(newnode, getNextSibling());
				}
				setData(newdata);
			} catch (IndexOutOfBoundsException e) {
				DOMException ex = new DOMException(DOMException.INDEX_SIZE_ERR, e.getMessage());
				ex.initCause(e);
				throw ex;
			}
			return newnode;
		}

		@Override
		public boolean isElementContentWhitespace() {
			if (this.data != null) {
				int dl = this.data.length();
				for (int i = 0; i < dl; i++) {
					if (!Character.isWhitespace(this.data.charAt(i))) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public String getWholeText() {
			Node firstnode = this;
			Node node = getPreviousSibling();
			while (node != null) {
				short type = node.getNodeType();
				if (type != Node.TEXT_NODE && type != Node.ENTITY_REFERENCE_NODE) {
					break;
				}
				firstnode = node;
				node = node.getPreviousSibling();
			}
			Node lastnode = this;
			node = getNextSibling();
			while (node != null) {
				short type = node.getNodeType();
				if (type != Node.TEXT_NODE && type != Node.ENTITY_REFERENCE_NODE) {
					break;
				}
				lastnode = node;
				node = node.getNextSibling();
			}
			if (firstnode == lastnode) {
				return getData();
			}
			StringBuilder buf = new StringBuilder(data.length() * 2);
			node = firstnode;
			while (node != lastnode) {
				buf.append(node.toString());
				node = node.getNextSibling();
			}
			buf.append(lastnode.toString());
			return buf.toString();
		}

		@Override
		public Text replaceWholeText(String content) throws DOMException {
			if (content == null) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null content (use empty string instead)");
			}
			Node parent = getParentNode();
			if (parent != null) {
				Node node = getPreviousSibling();
				while (node != null) {
					short type = node.getNodeType();
					if (type != Node.ENTITY_REFERENCE_NODE && type != Node.TEXT_NODE) {
						break;
					}
					Node sibling = node.getPreviousSibling();
					parent.removeChild(node);
					node = sibling;
				}
				node = getNextSibling();
				while (node != null) {
					short type = node.getNodeType();
					if (type != Node.ENTITY_REFERENCE_NODE && type != Node.TEXT_NODE) {
						break;
					}
					Node sibling = node.getNextSibling();
					parent.removeChild(node);
					node = sibling;
				}
			}
			setData(content);
			if (content.length() == 0) {
				if (parent != null) {
					parent.removeChild(this);
				}
				return null;
			}
			return this;
		}

		@Override
		public String getNodeName() {
			return "#text";
		}

		@Override
		public Text cloneNode(boolean deep) {
			Text my = new MyText();
			my.setData(getData());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

		@Override
		public String toString() {
			String text = getData();
			if (!isElementContentWhitespace()) {
				Node node = getParentNode();
				if (node != null && node.getNodeType() == Node.ELEMENT_NODE
						&& ((DOMElement) node).isRawText()) {
					// raw text element
					String parentLName = node.getLocalName();
					text = escapeCloseTag(parentLName, text);
				} else {
					text = escapeLtGtEntities(text);
				}
			}
			return text;
		}

	}

	/**
	 * If the given element is a defined container for embedded style, get the definer for
	 * that style.
	 * <p>
	 * If the document is HTML and the <code>element</code> is a <code>style</code> element,
	 * returns itself.
	 * 
	 * @param element
	 *            the candidate for being a container for embedded style. Could be
	 *            <code>null</code>.
	 * @return the definer for an embedded style container, or <code>null</code> if the
	 *         <code>element</code> is not a defined embedded style container.
	 */
	LinkStyleDefiner getEmbeddedStyleDefiner(DOMElement element) {
		if (element != null) {
			return getEmbeddedStyleDefiner(element.getId());
		}
		return null;
	}

	/**
	 * Determine whether the given name is a valid XML name, excluding the colon
	 * (':').
	 * 
	 * @param name the name to check.
	 * @return <code>true</code> if the name is valid.
	 */
	static boolean isValidName(String name) {
		int len = name.length();
		if (len == 0) {
			return false;
		}
		if (!isValidStartCharacter(name.codePointAt(0))) {
			return false;
		}
		int i = name.offsetByCodePoints(0, 1);
		for (; i < len; i = name.offsetByCodePoints(i, 1)) {
			if (!isValidCharacter(name.codePointAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidCharacter(int cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| (cp >= 0x30 && cp <= 0x39) // 0-9
				|| cp == 0x2d // -
				|| cp == 0x5f // _
				|| cp == 0x2e // .
				|| cp == 0xB7 // Middle dot
				|| (cp >= 0xC0 && cp <= 0xD6) // #xC0-#xD6
				|| (cp >= 0xD8 && cp <= 0xF6) // #xD8-#xF6
				|| (cp >= 0xF8 && cp <= 0x2FF) // #xF8-#x2FF
				|| (cp >= 0x300 && cp <= 0x37D) // #x300-#x37D
				|| (cp >= 0x37F && cp <= 0x1FFF) // #x37F-#x1FFF
				|| (cp >= 0x200C && cp <= 0x200D) // #x200C-#x200D
				|| (cp >= 0x203F && cp <= 0x2040) // #x203F-#x2040
				|| (cp >= 0x2070 && cp <= 0x218F) // #x2070-#x218F
				|| (cp >= 0x2C00 && cp <= 0x2FEF) // #x2C00-#x2FEF
				|| (cp >= 0x3001 && cp <= 0xD7FF) // #x3001-#xD7FF
				|| (cp >= 0xF900 && cp <= 0xFDCF) // #xF900-#xFDCF
				|| (cp >= 0xFDF0 && cp <= 0xFFFD) // #xFDF0-#xFFFD
				|| (cp >= 0x10000 && cp <= 0xEFFFF);// #x10000-#xEFFFF
	}

	private static boolean isValidStartCharacter(int cp) {
		return (cp >= 0x61 && cp <= 0x7A) // a-z
				|| (cp >= 0x41 && cp <= 0x5A) // A-Z
				|| cp == 0x5f // _
				|| (cp >= 0xC0 && cp <= 0xD6) // #xC0-#xD6
				|| (cp >= 0xD8 && cp <= 0xF6) // #xD8-#xF6
				|| (cp >= 0xF8 && cp <= 0x2FF) // #xF8-#x2FF
				|| (cp >= 0x370 && cp <= 0x37D) // #x370-#x37D
				|| (cp >= 0x37F && cp <= 0x1FFF) // #x37F-#x1FFF
				|| (cp >= 0x200C && cp <= 0x200D) // #x200C-#x200D
				|| (cp >= 0x2070 && cp <= 0x218F) // #x2070-#x218F
				|| (cp >= 0x2C00 && cp <= 0x2FEF) // #x2C00-#x2FEF
				|| (cp >= 0x3001 && cp <= 0xD7FF) // #x3001-#xD7FF
				|| (cp >= 0xF900 && cp <= 0xFDCF) // #xF900-#xFDCF
				|| (cp >= 0xFDF0 && cp <= 0xFFFD) // #xFDF0-#xFFFD
				|| (cp >= 0x10000 && cp <= 0xEFFFF);// #x10000-#xEFFFF
	}

	static String escapeCloseTag(String tagname, String data) {
		int idx = data.indexOf('<');
		if (idx == -1) {
			return data;
		}
		int tnidx = data.indexOf(tagname, 2);
		if (tnidx == -1) {
			return data;
		}
		StringBuilder buf = null;
		int tnlen = tagname.length();
		int lenm1 = data.length() - 1;
		while (idx < lenm1) {
			char c = data.charAt(idx);
			if (c == '<') {
				int i = idx + 1;
				char d = data.charAt(i);
				if (d == '/' && lenm1 - i > tnlen) {
					i++;
					i = skipIgnorableChars(i, data, lenm1);
					if (i < lenm1) {
						if (data.regionMatches(true, i, tagname, 0, tnlen)) {
							i = skipIgnorableChars(i + tnlen, data, lenm1);
							if (i <= lenm1 && data.charAt(i) == '>') {
								// Escape
								if (buf == null) {
									buf = new StringBuilder(lenm1 + 4);
									buf.append(data.subSequence(0, idx));
								}
								buf.append("&lt;");
								buf.append(data.subSequence(idx + 1, i + 1));
								idx = i + 1;
								continue;
							}
						}
					}
				}
			}
			if (buf != null) {
				buf.append(c);
			}
			idx++;
		}
		if (buf != null && idx == lenm1) {
			buf.append(data.charAt(idx));
		}
		return buf == null ? data : buf.toString();
	}

	private static int skipIgnorableChars(int idx, String text, int lenm1) {
		while (idx < lenm1) {
			char c = text.charAt(idx);
			if (c != ' ' && c != '\t' && c != '\n') {
				if (c != '\r') {
					break;
				} else {
					int i = idx + 1;
					if (text.charAt(i) == '\n') {
						idx = i;
					}
				}
			}
			idx++;
		}
		return idx;
	}

	static String escapeLtGtEntities(String text) {
		StringBuilder buf = null;
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			// Check whether c is '<' or '>' Xml Predefined Entities
			if (c == '<') {
				buf = appendEntityToBuffer(buf, "lt", text, i, len);
			} else if (c == '>') {
				buf = appendEntityToBuffer(buf, "gt", text, i, len);
			} else if (buf != null) {
				buf.append(c);
			}
		}
		if (buf != null) {
			text = buf.toString();
		}
		return text;
	}

	static StringBuilder appendEntityToBuffer(StringBuilder buf, String string, String text, int index, int inilen) {
		if (buf == null) {
			buf = new StringBuilder(inilen + string.length() + 2);
			buf.append(text.subSequence(0, index));
		}
		buf.append('&').append(string).append(';');
		return buf;
	}

	class MyCDATASection extends MyText implements CDATASection {

		MyCDATASection() {
			super(Node.CDATA_SECTION_NODE);
		}

		@Override
		public String getNodeName() {
			return "#cdata-section";
		}

		@Override
		public void setData(String data) throws DOMException {
			if (data.contains("]]>")) {
				throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "cdata-section cannot contain ']]>'");
			}
			super.setData(data);
		}

		@Override
		public CDATASection cloneNode(boolean deep) {
			CDATASection my = new MyCDATASection();
			my.setData(getData());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

		@Override
		public String toString() {
			return "<![CDATA[" + getData() + "]]>";
		}
	}

	class MyAttr extends DOMAttr {

		MyAttr(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		public DOMDocument getOwnerDocument() {
			return DOMDocument.this;
		}

		@Override
		public String getBaseURI() {
			return DOMDocument.this.getBaseURI();
		}

		@Override
		public boolean isId() {
			String nsuri = getNamespaceURI();
			DOMElement owner = getOwnerElement();
			return owner != null && owner.isIdAttributeNS(nsuri, getLocalName());
		}

		@Override
		public Attr cloneNode(boolean deep) {
			MyAttr my = (MyAttr) getOwnerDocument().createAttributeNS(getNamespaceURI(), getName());
			// directly cloned attributes always have 'specified = true' (which is default)
			my.setValue(getValue());
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

	}

	class XmlnsAttr extends MyAttr {

		XmlnsAttr() {
			super("xmlns", "http://www.w3.org/2000/xmlns/");
		}

		@Override
		public void setPrefix(String prefix) throws DOMException {
			throw new DOMException(DOMException.NAMESPACE_ERR, "Cannot set prefix for xmlns attribute");
		}
	}

	interface StyleAttr extends Attr {
		AbstractCSSStyleDeclaration getStyle();
	}

	class MyStyleAttr extends MyAttr implements StyleAttr {

		private AbstractCSSStyleDeclaration inlineStyle = null;

		MyStyleAttr(String name) {
			super(name, null);
		}

		@Override
		void setAttributeOwner(DOMElement newOwner) {
			super.setAttributeOwner(newOwner);
			onDOMChange();
		}

		@Override
		public String getValue() {
			if (inlineStyle == null) {
				return super.getValue();
			}
			return inlineStyle.getCssText();
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			if (inlineStyle != null) {
				setInlineStyle(value);
			} else {
				getStyle();
			}
			onDOMChange();
		}

		@Override
		public AbstractCSSStyleDeclaration getStyle() {
			if (inlineStyle == null) {
				inlineStyle = getOwnerDocument().getStyleSheetFactory().createInlineStyle(this);
				setInlineStyle(super.getValue());
			}
			return inlineStyle;
		}

		void setInlineStyle(String value) {
			if (value == null) {
				value = "";
			}
			try {
				inlineStyle.setCssText(value);
			} catch (DOMException e) {
				getErrorHandler().inlineStyleError(getOwnerElement(), e, value);
			}
		}

		void onDOMChange() {
			DOMDocument.this.onStyleModify();
		}

	}

	/**
	 * An attribute that changes the meaning of its style-definer owner element.
	 */
	class StyleEventAttr extends MyAttr {

		StyleEventAttr(String name, String namespaceURI) {
			super(name, namespaceURI);
		}

		@Override
		void setAttributeOwner(DOMElement newOwner) {
			super.setAttributeOwner(newOwner);
			onDOMChange(newOwner);
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			onDOMChange(getOwnerElement());
		}

		void onDOMChange(Node ownerNode) {
			if (ownerNode != null && ownerNode instanceof LinkStyleDefiner) {
				((LinkStyleDefiner) ownerNode).resetLinkedSheet();
			}
		}

	}

	class ClassAttr extends MyAttr {

		ClassAttr(String namespaceURI) {
			super("class", namespaceURI);
		}

		@Override
		void setAttributeOwner(DOMElement newOwner) throws DOMException {
			DOMElement oldOwner;
			if (newOwner == null && (oldOwner = getOwnerElement()) != null && isSameNamespace(oldOwner.getNamespaceURI())) {
				// getElementsByClassName is a collection of child elements,
				// so we get the owner's parent
				Node parent = oldOwner.getParentNode();
				if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement parentElm = (DOMElement) parent;
					parentElm.updateClasslistsOnRemove(oldOwner, parentElm);
				}
				if (oldOwner.classList != null) {
					// Restore attribute value from owner's classList and clear it.
					this.value = oldOwner.classList.getValue();
					oldOwner.classList.clear();
				}
			}
			super.setAttributeOwner(newOwner);
			if (newOwner != null) {
				if (isSameNamespace(newOwner.getNamespaceURI())) {
					if (newOwner.classList != null) {
						newOwner.classList.setValue(this.value);
					}
					Node parent = newOwner.getParentNode();
					if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
						DOMElement parentElm = (DOMElement) parent;
						parentElm.updateClasslists(newOwner, parentElm);
					}
				}
			}
		}

		private boolean isSameNamespace(String ownerNamespaceURI) {
			String namespaceURI = getNamespaceURI();
			if (namespaceURI == null) {
				if (ownerNamespaceURI == null || isDefaultNamespace(ownerNamespaceURI)) {
					return true;
				}
			} else {
				return namespaceURI.equals(ownerNamespaceURI);
			}
			return false;
		}

		@Override
		public String getValue() {
			ClassList list = getListValue();
			if (list == null) {
				return super.getValue();
			}
			return list.getValue();
		}

		@Override
		public void setValue(String value) throws DOMException {
			super.setValue(value);
			ClassList list = getListValue();
			if (list != null) {
				list.setValue(value);
			}
		}

		ClassList getListValue() {
			DOMElement owner = getOwnerElement();
			if (owner == null || !isSameNamespace(owner.getNamespaceURI())) {
				return null;
			}
			return (ClassList) owner.getClassList();
		}

	}

	class MyXMLElement extends DOMElement {

		private HashMap<String, String> idAttrNameMap = new HashMap<String, String>();

		MyXMLElement(String localName, String namespaceURI) {
			super(localName, namespaceURI);
		}

		@Override
		public String getId() {
			String idAttrName = idAttrNameMap.get(null); // get the 'id' attribute name
			if (idAttrName != null) {
				Attr attr = nodeMap.getNamedItem(idAttrName);
				if (attr != null) {
					return attr.getNodeValue();
				}
			}
			return super.getId();
		}

		@Override
		boolean isIdAttributeNS(String namespaceURI, String localName) {
			return localName.equals(idAttrNameMap.get(namespaceURI));
		}

		@Override
		public void setIdAttribute(String name, boolean isId) throws DOMException {
			if (!hasAttribute(name)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			setIdAttrNS(null, name, isId);
		}

		@Override
		public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
			if (!hasAttributeNS(namespaceURI, localName)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			setIdAttrNS(namespaceURI, localName, isId);
		}

		@Override
		public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
			if (idAttr == null || !nodeMap.getNodeList().contains(idAttr)) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, "Not an attribute of this element");
			}
			setIdAttrNS(idAttr.getNamespaceURI(), idAttr.getLocalName(), isId);
		}

		private void setIdAttrNS(String namespaceURI, String localName, boolean isId) {
			if (isId) {
				idAttrNameMap.put(namespaceURI, localName);
			} else if (localName.equals(idAttrNameMap.get(namespaceURI))) {
				idAttrNameMap.remove(namespaceURI);
			}
		}

		@Override
		boolean isVoid() {
			DocumentType docType = getDoctype();
			if (docType != null) {
				ContentModel contentModel;
				try {
					contentModel = ContentModel.getModel(docType);
				} catch (IOException | SAXException e) {
					contentModel = null;
				}
				if (contentModel != null) {
					return contentModel.isEmpty(localName);
				}
			}
			return false;
		}

		@Override
		public DOMDocument getOwnerDocument() {
			return DOMDocument.this;
		}

		@Override
		protected BaseCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMDocument.this.getStyleSheetFactory();
		}

		@Override
		public String getBaseURI() {
			return DOMDocument.this.getBaseURI();
		}

		@Override
		public DOMElement cloneNode(boolean deep) {
			return cloneElementNode(new MyXMLElement(getLocalName(), getNamespaceURI()), deep);
		}

		DOMElement cloneElementNode(MyXMLElement my, boolean deep) {
			my.setPrefix(getPrefix());
			Iterator<DOMNode> it = nodeMap.getNodeList().iterator();
			while (it.hasNext()) {
				DOMAttr attr = (DOMAttr) it.next();
				DOMAttr myattr = (DOMAttr) attr.cloneNode(deep);
				// directly cloned attributes always have 'specified = true'
				myattr.specified = attr.getSpecified();
				my.setAttributeNode(myattr);
				if (attr.isId()) {
					my.setIdAttributeNode(myattr, true);
				}
			}
			if (deep) {
				Node node = getFirstChild();
				while (node != null) {
					my.appendChild(node.cloneNode(true));
					node = node.getNextSibling();
				}
			}
			callUserHandlers(UserDataHandler.NODE_CLONED, this, my);
			return my;
		}

	}

	/**
	 * Creates an element of the type specified, with a <code>null</code> namespace URI.
	 * <p>
	 * The <code>tagName</code> is transformed to lower case.
	 * <p>
	 * No <code>IMPLIED</code> attributes are created.
	 * 
	 * @param tagName the tag name of the element to create.
	 * @return the new <code>DOMElement</code>.
	 * @throws DOMException INVALID_CHARACTER_ERR if the name is not an XML valid
	 *                      name.
	 */
	@Override
	public DOMElement createElement(String tagName) throws DOMException {
		if (tagName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null tag name");
		}
		return createElementNS(null, tagName);
	}

	/**
	 * Creates an element with the given qualified name and namespace URI.
	 * <p>
	 * If <code>namespaceURI</code> is <code>null</code> or the empty string, the
	 * <code>qualifiedName</code> is transformed to lower case.
	 * <p>
	 * No <code>IMPLIED</code> attributes are created.
	 * 
	 * @param namespaceURI  the namespace URI of the element to create.
	 * @param qualifiedName the qualified name of the element to create. The
	 *                      namespace prefix, if any, is extracted from this name.
	 * @return the new <code>DOMElement</code>.
	 * @throws DOMException INVALID_CHARACTER_ERR if the name is not an XML valid
	 *                      name.<br/>
	 *                      NAMESPACE_ERR: if the <code>qualifiedName</code> is a
	 *                      malformed qualified name, if the
	 *                      <code>qualifiedName</code> has a prefix and the
	 *                      </code>namespaceURI</code> is <code>null</code>, or if
	 *                      the <code>qualifiedName</code> has a prefix that is
	 *                      <code>"xml"</code> and the <code>namespaceURI</code> is
	 *                      different from
	 *                      <code>"http://www.w3.org/XML/1998/namespace"</code> , or
	 *                      if the <code>qualifiedName</code> or its prefix is
	 *                      <code>"xmlns"</code> and the <code>namespaceURI</code>
	 *                      is different from
	 *                      <code>"http://www.w3.org/2000/xmlns/"</code>, or if the
	 *                      <code>namespaceURI</code> is
	 *                      <code>"http://www.w3.org/2000/xmlns/"</code> and neither
	 *                      the <code>qualifiedName</code> nor its prefix is
	 *                      <code>"xmlns"</code>.
	 */
	@Override
	public DOMElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		if (qualifiedName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null qualified name");
		}
		String localName;
		String prefix = null;
		if (namespaceURI != null) {
			if (namespaceURI.length() == 0) {
				namespaceURI = null;
				localName = qualifiedName.toLowerCase(Locale.ROOT);
			} else {
				namespaceURI = namespaceURI.intern();
				int idx = qualifiedName.indexOf(':');
				if (idx == -1) {
					prefix = lookupPrefix(namespaceURI);
					localName = qualifiedName;
				} else if (idx == qualifiedName.length() - 1) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty local name");
				} else if (idx == 0) {
					throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Empty prefix");
				} else {
					prefix = qualifiedName.substring(0, idx).intern();
					localName = qualifiedName.substring(idx + 1);
				}
			}
		} else {
			if (qualifiedName.indexOf(':') != -1) {
				throw new DOMException(DOMException.NAMESPACE_ERR, "Prefix with null namespace");
			}
			localName = qualifiedName.toLowerCase(Locale.ROOT);
		}
		if (!isValidName(localName)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid name: " + localName);
		}
		localName = localName.intern();
		DOMElement myelem = new MyXMLElement(localName, namespaceURI);
		if (prefix != null) {
			myelem.setPrefix(prefix);
		}
		return myelem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DocumentFragment createDocumentFragment() {
		return new DOMDocumentFragment();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Text createTextNode(String data) {
		if (data == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null data");
		}
		Text text = new MyText();
		text.setData(data);
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comment createComment(String data) {
		if (data == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null data");
		}
		MyComment my = new MyComment();
		my.setData(data);
		return my;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CDATASection createCDATASection(String data) throws DOMException {
		if (data == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null data");
		}
		CDATASection my = new MyCDATASection();
		my.setData(data);
		return my;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		if (target == null || target.length() == 0) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Void target");
		}
		if (target.equalsIgnoreCase("xml")) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
					"An xml declaration is not a processing instruction");
		}
		if (!isValidName(target)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid target: " + target);
		}
		if (data.contains("?>")) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
					"A processing instruction data cannot contain a '?>'");
		}
		if ("xml-stylesheet".equals(target)) {
			return new MyStyleProcessingInstruction(data);
		}
		return new MyProcessingInstruction(target, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityReference createEntityReference(String name) throws DOMException {
		if (name == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null entity reference name");
		}
		return new MyEntityReference(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attr createAttribute(String name) throws DOMException {
		return createAttributeNS(null, name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		if (qualifiedName == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "null name");
		}
		String localName = qualifiedName;
		String prefix = null;
		if (namespaceURI != null) {
			if (namespaceURI.length() != 0) {
				namespaceURI = namespaceURI.intern();
				int idx = qualifiedName.indexOf(':');
				if (idx == -1) {
					prefix = lookupPrefix(namespaceURI);
				} else if (idx == qualifiedName.length() - 1) {
					throw new DOMException(DOMException.NAMESPACE_ERR, "Empty local name");
				} else if (idx == 0) {
					throw new DOMException(DOMException.NAMESPACE_ERR, "Empty prefix");
				} else {
					prefix = qualifiedName.substring(0, idx).intern();
					localName = qualifiedName.substring(idx + 1);
				}
				if (HTMLDocument.HTML_NAMESPACE_URI == namespaceURI) {
					localName = localName.toLowerCase(Locale.ROOT);
				}
			} else {
				if (qualifiedName.indexOf(':') != -1) {
					throw new DOMException(DOMException.NAMESPACE_ERR, "Prefix with null namespace");
				}
				namespaceURI = null;
			}
		} else if (qualifiedName.indexOf(':') != -1) {
			throw new DOMException(DOMException.NAMESPACE_ERR, "Prefix with null namespace");
		}
		localName = localName.intern();
		Attr my = createAttributeNS(namespaceURI, prefix, localName);
		return my;
	}

	Attr createAttributeNS(String namespaceURI, String prefix, String localName) throws DOMException {
		if (!isValidName(localName)) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Invalid name: " + localName);
		}
		Attr my;
		if (localName == "xmlns") {
			if (namespaceURI != null && !"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
				throw new DOMException(DOMException.NAMESPACE_ERR, "xmlns local name but not xmlns namespace");
			}
			my = new XmlnsAttr();
		} else if (localName == "class") {
			my = new ClassAttr(namespaceURI);
		} else if (localName == "base" && "xml".equals(prefix)) {
			my = new StyleEventAttr(localName, namespaceURI);
		} else if (localName == "style" && prefix == null) {
			my = new MyStyleAttr(localName);
		} else {
			my = new MyAttr(localName, namespaceURI);
		}
		if (prefix != null) {
			my.setPrefix(prefix);
		}
		return my;
	}

	/**
	 * Create a <code>NodeIterator</code> object with the given root node,
	 * <code>whatToShow</code> bitmask, and <code>filter</code> callback.
	 * <p>
	 * Example:
	 * <pre>
	 * NodeIterator it = document.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null);
	 * </pre>
	 * </p>
	 * <p>
	 * Based on the <a href=
	 * "https://www.w3.org/TR/dom/#dom-document-createnodeiterator"><code>createNodeIterator()</code>
	 * method in DOM Level 4</a>.
	 * </p>
	 * <p>
	 * The returned object can be cast to
	 * {@link org.w3c.dom.traversal.NodeIterator}, but the behaviour is not exactly
	 * what that interface documents (see this library's {@link NodeIterator}
	 * interface description for more details). If you need compatibility with the
	 * W3C-specified behaviour, never use a <code>NodeFilter</code> that returns
	 * {@link NodeFilter#FILTER_SKIP_NODE_CHILD} (that is, W3C's
	 * {@link org.w3c.dom.traversal.NodeFilter#FILTER_REJECT
	 * NodeFilter.FILTER_REJECT}).
	 * </p>
	 * 
	 * @param rootNode   the root node.
	 * @param whatToShow a bitmask specifying what types of nodes to show.
	 * @param filter     an optional filter callback, see {@link NodeFilter}.
	 * @return the node iterator.
	 */
	public NodeIterator createNodeIterator(Node rootNode, int whatToShow, NodeFilter filter) {
		return new NodeIteratorImpl((AbstractDOMNode) rootNode, whatToShow, filter);
	}

	/**
	 * Create a <code>TreeWalker</code> object with the given root node,
	 * <code>whatToShow</code> bitmask, and <code>filter</code> callback.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * TreeWalker tw = document.createTreeWalker(document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null);
	 * </pre>
	 * </p>
	 * <p>
	 * Based on the <a href=
	 * "https://www.w3.org/TR/dom/#dom-document-createtreewalker"><code>createTreeWalker()</code>
	 * method in DOM Level 4</a>.
	 * </p>
	 * <p>
	 * Beware that this library's <code>NodeFilter</code> uses names different to
	 * W3C's for the filter values: {@link NodeFilter#FILTER_SKIP_NODE_CHILD}
	 * instead of W3C's {@link org.w3c.dom.traversal.NodeFilter#FILTER_REJECT
	 * NodeFilter.FILTER_REJECT}), and {@link NodeFilter#FILTER_SKIP_NODE} instead
	 * of {@link org.w3c.dom.traversal.NodeFilter#FILTER_SKIP
	 * NodeFilter.FILTER_SKIP}. But the defined numeric values are the same, so they
	 * can be used interchangeably in the <code>TreeWalker</code>.
	 * </p>
	 * 
	 * @param rootNode   the root node.
	 * @param whatToShow a bitmask specifying what types of nodes to show.
	 * @param filter     an optional filter callback, see {@link NodeFilter}.
	 * @return the tree walker.
	 */
	public TreeWalker createTreeWalker(Node rootNode, int whatToShow, NodeFilter filter) {
		return new TreeWalkerImpl((AbstractDOMNode) rootNode, whatToShow, filter);
	}

	/**
	 * Gives a live NodeList containing all child elements which have all of the given class
	 * names under this reference element.
	 * 
	 * @param names
	 *            the names of the classes, separated by whitespace.
	 * @return the live NodeList containing all child elements which have all of the given
	 *         class names under this reference element.
	 */
	@Override
	public ElementList getElementsByClassName(String names) {
		return getNodeList().getElementsByClassName(names, getComplianceMode());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String lookupPrefix(String namespaceURI) {
		DOMElement html = getDocumentElement();
		if (html == null) {
			return null;
		}
		return lookupPrefix(html, namespaceURI);
	}

	private String lookupPrefix(Node node, String namespaceURI) {
		if (node.getNamespaceURI() == namespaceURI) {
			String prefix = node.getPrefix();
			if (prefix != null) {
				return prefix;
			}
			return null;
		}
		Node cnode = node.getFirstChild();
		while (cnode != null) {
			if (cnode.getNodeType() == Node.ELEMENT_NODE) {
				String prefix = lookupPrefix(cnode, namespaceURI);
				if (prefix != null) {
					return prefix;
				}
			}
			cnode = cnode.getNextSibling();
		}
		return null;
	}

	/**
	 * Returns the Element that has an ID attribute with the given value.
	 * <p>
	 * If no such element exists, this returns <code>null</code>. If more than one element has
	 * an ID attribute with that value, what is returned is undefined.
	 * <p>
	 * By default, this implementations uses <code>'id'</code> as the ID attribute, but this
	 * can be changed to a different case (e.g. 'ID') with the
	 * {@link Element#setIdAttribute(String, boolean)}. Changes to the ID attribute on a
	 * single element are <code>Document</code>-wide.
	 * 
	 * @param elementId
	 *            The unique id value for an element.
	 * @return The matching element or <code>null</code> if there is none.
	 */
	@Override
	public DOMElement getElementById(String elementId) {
		return findElementById(getFirstChild(), elementId);
	}

	private DOMElement findElementById(Node node, String elementId) {
		while (node != null) {
			DOMElement elm;
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elm = (DOMElement) node;
				String idValue = elm.getId();
				if (idValue.equals(elementId)) {
					return elm;
				}
			}
			elm = findElementById(node.getFirstChild(), elementId);
			if (elm != null) {
				return elm;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * Give the encoding used when the document was parsed, if known.
	 * 
	 * @return <code>null</code>.
	 */
	@Override
	public String getInputEncoding() {
		return null;
	}

	/**
	 * Now deprecated, this method gave the encoding specified at the XML declaration.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public String getXmlEncoding() {
		return null;
	}

	/**
	 * Get whether this document is standalone as specified at the XML declaration.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public boolean getXmlStandalone() {
		return false;
	}

	/**
	 * Set whether this document is standalone as specified at the XML declaration.
	 * <p>
	 * Calling this method has no effect.
	 * 
	 * @param xmlStandalone argument is ignored.
	 */
	@Deprecated
	@Override
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
	}

	/**
	 * Get the version for the "XML" feature.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public String getXmlVersion() {
		return null;
	}

	/**
	 * Set the version for the "XML" feature.
	 * 
	 * @param xmlVersion this parameter is ignored.
	 */
	@Deprecated
	@Override
	public void setXmlVersion(String xmlVersion) throws DOMException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getStrictErrorChecking() {
		return strictErrorChecking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		this.strictErrorChecking = strictErrorChecking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDocumentURI() {
		return documentURI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDocumentURI(String documentURI) {
		this.documentURI = documentURI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
		switch (importedNode.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			Attr importedAttr = (Attr) importedNode;
			DOMAttr attr;
			// Foreign attributes can have null local name
			if (importedAttr.getLocalName() != null) {
				attr = (DOMAttr) createAttributeNS(importedAttr.getNamespaceURI(), importedAttr.getLocalName());
			} else {
				attr = (DOMAttr) createAttribute(importedNode.getNodeName());
			}
			attr.specified = importedAttr.getSpecified();
			attr.setValue(importedNode.getNodeValue());
			if (importedAttr.getPrefix() != null) {
				attr.setPrefix(importedAttr.getPrefix());
			}
			return attr;
		case Node.ELEMENT_NODE:
			Element elm;
			Element foreignElm = (Element) importedNode;
			// Foreign elements can have null local name
			if (foreignElm.getLocalName() != null) {
				elm = createElementNS(foreignElm.getNamespaceURI(), foreignElm.getLocalName());
			} else {
				elm = createElement(importedNode.getNodeName());
			}
			NamedNodeMap attributes = foreignElm.getAttributes();
			int count = attributes.getLength();
			for (int i = 0; i < count; i++) {
				importedAttr = (Attr) attributes.item(i);
				attr = (DOMAttr) importNode(importedAttr, true);
				attr.specified = importedAttr.getSpecified();
				elm.setAttributeNode(attr);
				if (importedAttr.isId()) {
					elm.setIdAttributeNode(attr, true);
				}
			}
			if (deep) {
				Node node = importedNode.getFirstChild();
				while (node != null) {
					elm.appendChild(importNode(node, true));
					node = node.getNextSibling();
				}
			}
			return elm;
		case Node.TEXT_NODE:
			return createTextNode(importedNode.getNodeValue());
		case Node.CDATA_SECTION_NODE:
			return createCDATASection(importedNode.getNodeValue());
		case Node.COMMENT_NODE:
			return createComment(importedNode.getNodeValue());
		case Node.DOCUMENT_FRAGMENT_NODE:
			DocumentFragment df = createDocumentFragment();
			if (deep) {
				Node node = importedNode.getFirstChild();
				while (node != null) {
					df.appendChild(importNode(node, true));
					node = node.getNextSibling();
				}
			}
			return df;
		case Node.ENTITY_REFERENCE_NODE:
			return createEntityReference(importedNode.getNodeName());
		case Node.PROCESSING_INSTRUCTION_NODE:
			return createProcessingInstruction(importedNode.getNodeName(), importedNode.getNodeValue());
		default:
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot import this node type.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node adoptNode(Node source) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Node adoption not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode insertBefore(Node newChild, Node refChild) throws DOMException {
		short newType = newChild.getNodeType();
		if (newType == Node.ELEMENT_NODE) {
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
							"Document already has a root element.");
				}
				node = node.getNextSibling();
			}
		} else if (newType == Node.DOCUMENT_TYPE_NODE) {
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document already has a doctype.");
				}
				node = node.getNextSibling();
			}
		}
		return super.insertBefore(newChild, refChild);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DOMNode replaceChild(Node newChild, Node oldChild) throws DOMException {
		short newType = newChild.getNodeType();
		if (newType != oldChild.getNodeType()) {
			if (newType == Node.ELEMENT_NODE) {
				Node node = getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
								"Document already has a root element.");
					}
					node = node.getNextSibling();
				}
			} else if (newType == Node.DOCUMENT_TYPE_NODE) {
				Node node = getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document already has a doctype.");
					}
					node = node.getNextSibling();
				}
			}
		}
		return super.replaceChild(newChild, oldChild);
	}

	@Override
	void preAddChild(Node newChild) {
		super.preAddChild(newChild);
		if (newChild.getNodeType() == Node.ELEMENT_NODE) {
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document already has a root element.");
				}
				node = node.getNextSibling();
			}
		} else if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			Node node = getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document already has a doctype.");
				}
				node = node.getNextSibling();
			}
		}
	}

	@Override
	void checkDocumentOwner(Node newChild) {
		if (newChild.getOwnerDocument() != this && newChild.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Different document owners.");
		}
	}

	@Override
	void preReplaceChild(AbstractDOMNode newChild, AbstractDOMNode replaced) {
		super.preAddChild(newChild);
		if (newChild.getNodeType() == Node.ELEMENT_NODE) {
			if (replaced.getNodeType() != Node.ELEMENT_NODE) {
				Node node = getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
								"Document already has a root element.");
					}
					node = node.getNextSibling();
				}
			}
		} else if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			if (replaced.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
				Node node = getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document already has a doctype.");
					}
					node = node.getNextSibling();
				}
			}
		}
	}

	@Deprecated
	@Override
	public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This operation is not supported.");
	}

	/**
	 * <code>DOMConfiguration</code> is deprecated and not supported.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public DOMConfiguration getDomConfig() {
		return null;
	}

	/**
	 * Normalizes the document element, if there is one.
	 */
	@Override
	public void normalizeDocument() {
		DOMElement docelm = getDocumentElement();
		if (docelm != null) {
			docelm.normalize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String lookupNamespaceURI(String prefix) {
		DOMElement docelm = getDocumentElement();
		return docelm != null ? docelm.lookupNamespaceURI(prefix) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		DOMElement docelm = getDocumentElement();
		if (docelm != null) {
			return docelm.getNamespaceURI() == namespaceURI;
		} else {
			return namespaceURI == null;
		}
	}

	/**
	 * A list containing all the style sheets explicitly linked into or embedded in a
	 * document. For HTML documents, this includes external style sheets, included via the
	 * HTML LINK element, and inline STYLE elements. In XML, this includes external style
	 * sheets, included via style sheet processing instructions (see [XML StyleSheet]).
	 */
	@Override
	public StyleSheetList getStyleSheets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets;
	}

	void updateStyleLists() {
		// Find the linked styles
		linkedStyle.clear();
		NodeList nl = getLinkedStyleNodeList();
		int len = nl.getLength();
		for (int i = 0; i < len; i++) {
			LinkStyleDefiner link = (LinkStyleDefiner) nl.item(i);
			if (link.getSheet() != null) {
				linkedStyle.add(link);
			}
		}
		// Find the embedded styles
		embeddedStyle.clear();
		nl = getEmbeddedStyleNodeList();
		len = nl.getLength();
		for (int i = 0; i < len; i++) {
			embeddedStyle.add((LinkStyleDefiner) nl.item(i));
		}
		/*
		 * Add the linked and embedded styles. Must be added in this order, as mandated by the CSS
		 * spec.
		 */
		// Add styles referenced by links
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			addLinkedSheet(links.next().getSheet());
		}
		// Add embedded styles
		Iterator<LinkStyleDefiner> embd = embeddedStyle.iterator();
		while (embd.hasNext()) {
			addLinkedSheet(embd.next().getSheet());
		}
		sheets.setNeedsUpdate(false);
		if (lastStyleSheetSet != null) {
			setSelectedStyleSheetSet(lastStyleSheetSet);
		} else if (metaDefaultStyleSet.length() > 0) {
			setSelectedStyleSheetSet(metaDefaultStyleSet);
			lastStyleSheetSet = null;
		} else {
			setSelectedStyleSheetSet(sheets.getPreferredStyleSheetSet());
			lastStyleSheetSet = null;
		}
		if (getCanvas() != null) {
			getCanvas().reloadStyleState();
		}
	}

	private void addLinkedSheet(AbstractCSSStyleSheet linkedSheet) {
		if (linkedSheet != null) {
			sheets.add(linkedSheet);
		}
	}

	NodeList getLinkedStyleNodeList() {
		return getLinkedStyleNodeList(true);
	}

	NodeList getEmbeddedStyleNodeList() {
		return getLinkedStyleNodeList(false);
	}

	private NodeList getLinkedStyleNodeList(boolean external) {
		DefaultNodeList list = null;
		AbstractDOMNode node = getNodeList().getFirst();
		while (node != null) {
			short type = node.getNodeType();
			if (type == Node.PROCESSING_INSTRUCTION_NODE && "xml-stylesheet".equals(node.getNodeName())) {
				LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) node;
				String href = pi.getPseudoAttribute("href");
				if (href.length() > 1) {
					if (href.charAt(0) == '#') {
						if (!external) {
							if (list == null) {
								list = new DefaultNodeList();
							}
							list.add(node);
						}
					} else {
						if (external) {
							if (list == null) {
								list = new DefaultNodeList();
							}
							list.add(node);
						}
					}
				}
			} else if (type == Node.ELEMENT_NODE) {
				break;
			}
			node = node.nextSibling;
		}
		return list == null ? AbstractDOMNode.emptyNodeList : list;
	}

	LinkStyleDefiner getEmbeddedStyleDefiner(String id) {
		Node node = getFirstChild();
		while (node != null) {
			short type = node.getNodeType();
			if (type == Node.PROCESSING_INSTRUCTION_NODE && "xml-stylesheet".equals(node.getNodeName())) {
				LinkStyleProcessingInstruction pi = (LinkStyleProcessingInstruction) node;
				String href = pi.getPseudoAttribute("href");
				if (href.length() > 1 && href.charAt(0) == '#' && id.equals(href.substring(1))) {
					return pi;
				}
			} else if (type == Node.ELEMENT_NODE) {
				break;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * Gets the merged style sheet that applies to this document, resulting from the merge of
	 * the document's default style sheet, the document linked or embedded style sheets, and
	 * the non-important part of the user style sheet. Does not include overriden styles nor
	 * the 'important' part of the user-defined style sheet.
	 * <p>
	 * The style sheet is lazily built.
	 * 
	 * @return the merged style sheet that applies to this document.
	 */
	@Override
	public DocumentCSSStyleSheet getStyleSheet() {
		if (mergedStyleSheet == null) {
			mergeStyleSheets();
		}
		return mergedStyleSheet;
	}

	private void mergeStyleSheets() {
		getStyleSheets(); // Make sure that sheets is up to date
		BaseDocumentCSSStyleSheet defSheet = getStyleSheetFactory().getDefaultStyleSheet(getComplianceMode());
		if (targetMedium == null) {
			mergedStyleSheet = defSheet.clone();
		} else {
			mergedStyleSheet = defSheet.clone(targetMedium);
		}
		mergedStyleSheet.setOwnerDocument(this);
		// Add styles referenced by link and style elements
		Iterator<AbstractCSSStyleSheet> it = sheets.iterator();
		while (it.hasNext()) {
			mergedStyleSheet.addStyleSheet(it.next());
		}
	}

	/**
	 * Gets the list of available alternate styles.
	 * 
	 * @return the list of available alternate style titles.
	 */
	@Override
	public DOMStringList getStyleSheetSets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets.getStyleSheetSets();
	}

	/**
	 * Gets the title of the currently selected style sheet set.
	 * 
	 * @return the title of the currently selected style sheet, the empty string if none is
	 *         selected, or <code>null</code> if there are style sheets from different style
	 *         sheet sets that have their style sheet disabled flag unset.
	 */
	@Override
	public String getSelectedStyleSheetSet() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		String selectedSetName = "";
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0) {
				if (!sheet.getDisabled()) {
					if (selectedSetName.length() > 0) {
						if (!selectedSetName.equalsIgnoreCase(title)) {
							return null;
						}
					} else {
						selectedSetName = title;
					}
				}
			}
		}
		return selectedSetName;
	}

	/**
	 * Selects a style sheet set, disabling the other non-persistent sheet sets. If the name
	 * is the empty string, all non-persistent sheets will be disabled. Otherwise, if the name
	 * does not match any of the sets, does nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to select.
	 */
	@Override
	public void setSelectedStyleSheetSet(String name) {
		if (name == null || (name.length() > 0 && !getStyleSheetSets().contains(name))) {
			return;
		}
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			String title;
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() != 0) {
				if (title.equalsIgnoreCase(name)) {
					sheet.setDisabled(false);
					lastStyleSheetSet = name;
				} else {
					sheet.setDisabled(true);
				}
			}
		}
	}

	/**
	 * Gets the style sheet set that was last selected.
	 * 
	 * @return the last selected style sheet set, or <code>null</code> if none.
	 */
	@Override
	public String getLastStyleSheetSet() {
		return lastStyleSheetSet;
	}

	/**
	 * Enables a style sheet set. If the name does not match any of the sets, does nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to enable.
	 */
	@Override
	public void enableStyleSheetsForSet(String name) {
		if (name == null || name.length() == 0) {
			return;
		}
		Iterator<LinkStyleDefiner> links = linkedStyle.iterator();
		while (links.hasNext()) {
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0) {
				if (title.equals(name)) {
					sheet.setDisabled(false);
				}
			}
		}
	}

	/**
	 * Notifies the document about any change in the style sheets.
	 * 
	 */
	void onSheetModify() {
		mergedStyleSheet = null;
		sheets.setNeedsUpdate(true);
		onStyleModify();
	}

	/**
	 * Notifies the document about any change in style.
	 * 
	 */
	void onStyleModify() {
	}

	/**
	 * Gets the override style declaration for an element and pseudo-element.
	 * <p>
	 * The getOverrideStyle method provides a mechanism through which a DOM author could
	 * effect immediate change to the style of an element without modifying the explicitly
	 * linked style sheets of a document or the inline style of elements.
	 * </p>
	 * <p>
	 * The override style sheet comes after the author style sheet in the cascade algorithm.
	 * DOM Level 2.
	 * </p>
	 * 
	 * @param elt
	 *            the element.
	 * @param pseudoElt
	 *            the pseudo-element, or <code>null</code> if none.
	 * @return the override style sheet for the given element and pseudo-element.
	 */
	@Override
	public ExtendedCSSStyleDeclaration getOverrideStyle(Element elt, String pseudoElt) {
		return ((DOMElement) elt).getOverrideStyle(pseudoElt);
	}

	/**
	 * Gets the style database currently used to apply specific styles to this document.
	 * 
	 * @return the style database, or null if no style database has been selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		StyleDatabase sdb = null;
		if (targetMedium != null) {
			DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
			if (df != null) {
				sdb = df.getStyleDatabase(targetMedium);
			}
		}
		return sdb;
	}

	/**
	 * This document's current target medium name.
	 * 
	 * @return the target medium name of this document.
	 */
	@Override
	public String getTargetMedium() {
		return targetMedium;
	}

	/**
	 * Set the medium that will be used to compute the styles of this document.
	 * 
	 * @param medium
	 *            the name of the target medium, like 'screen' or 'print'.
	 * @throws CSSMediaException
	 *             if the document is unable to target the given medium.
	 */
	@Override
	public void setTargetMedium(String medium) throws CSSMediaException {
		medium = medium.intern();
		if ("all".equals(medium)) {
			targetMedium = null;
		} else {
			targetMedium = medium;
		}
		onSheetModify();
	}

	/**
	 * Gets the document's canvas for the current target medium.
	 * 
	 * @return the canvas, or null if no target medium has been set, or the DeviceFactory does
	 *         not support canvas for the target medium.
	 */
	@Override
	public CSSCanvas getCanvas() {
		if (targetMedium == null) {
			return null;
		}
		if (canvases.containsKey(targetMedium)) {
			return canvases.get(targetMedium);
		}
		CSSCanvas canvas;
		DeviceFactory df = getStyleSheetFactory().getDeviceFactory();
		if (df != null) {
			canvas = df.createCanvas(targetMedium, this);
			canvases.put(targetMedium, canvas);
		} else {
			canvas = null;
		}
		return canvas;
	}

	ErrorHandler createErrorHandler() {
		return new MyDefaultErrorHandler();
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Has any of the linked or embedded style sheets any error or warning ?
	 * 
	 * @return <code>true</code> if any of the linked or embedded style sheets has any SAC or rule error or
	 *         warning, <code>false</code> otherwise.
	 */
	@Override
	public boolean hasStyleIssues() {
		return sheets.hasErrorsOrWarnings() || getErrorHandler().hasErrors() || getErrorHandler().hasWarnings();
	}

	/**
	 * Notify the document about the addition of a <code>META</code> element that
	 * may affect the inner workings of this document.
	 * <p>
	 * This method avoids having to rescan <code>META</code> elements for each
	 * style-related operation.
	 * 
	 * @param name      the <code>META</code> name.
	 * @param attribute the attribute.
	 */
	public void onMetaAdded(String name, String attribute) {
		if ("Default-Style".equals(name)) {
			metaDefaultStyleSet = attribute;
		} else if ("referrer".equals(name)) {
			metaReferrerPolicy = attribute;
		}
	}

	/**
	 * Notify the document about the removal of a <code>META</code> element that may
	 * affect the inner workings of this document.
	 * <p>
	 * This method avoids having to rescan <code>META</code> elements for each
	 * style-related operation.
	 * 
	 * @param name      the <code>META</code> name.
	 * @param attribute the attribute.
	 */
	public void onMetaRemoved(String name, String attribute) {
		if ("Default-Style".equals(name)) {
			metaDefaultStyleSet = "";
		} else if ("referrer".equals(name)) {
			metaReferrerPolicy = "";
		}
	}

	/**
	 * Gets the base URL of this Document.
	 * <p>
	 * If the Document's <code>head</code> element has a <code>base</code> child element, the
	 * base URI is computed using the value of the href attribute of the <code>base</code>
	 * element.
	 * 
	 * @return the base URL, or null if no base URL could be found.
	 */
	@Override
	public URL getBaseURL() {
		URL baseURL = null;
		String buri = getBaseURI();
		if (buri != null) {
			try {
				baseURL = new URL(buri);
			} catch (MalformedURLException e) {
			}
		}
		return baseURL;
	}

	/**
	 * Gets the absolute base URI of this node.
	 * 
	 * @return the absolute base URI of this node, or null if an absolute URI could not be
	 *         obtained.
	 */
	@Override
	public String getBaseURI() {
		String buri = getDocumentURI();
		DOMElement elm = getDocumentElement();
		if (elm != null) {
			String attr = elm.getAttribute("xml:base");
			if (attr.length() != 0) {
				if (buri != null && attr.startsWith("//")) {
					try {
						URL url = new URL(buri);
						url = new URL(url, attr);
						buri = url.toExternalForm();
					} catch (MalformedURLException e) {
					}
				} else {
					buri = attr;
				}
			}
		}
		return buri;
	}

	/**
	 * Gets an URL for the given URI, taking into account the Base URL if appropriate.
	 * 
	 * @param uri
	 *            the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException
	 *             if the uri was wrong.
	 */
	@Override
	public URL getURL(String uri) throws MalformedURLException {
		if (uri.length() == 0) {
			throw new MalformedURLException("Empty URI");
		}
		URL url;
		if (uri.indexOf("://") < 0) {
			url = new URL(getBaseURL(), uri);
		} else {
			url = new URL(uri);
		}
		return url;
	}

	/**
	 * Is the provided URL a safe origin to load certain external resources?
	 * 
	 * @param linkedURL
	 *            the URL of the external resource.
	 * 
	 * @return <code>true</code> if is a safe origin, <code>false</code> otherwise.
	 */
	@Override
	public boolean isSafeOrigin(URL linkedURL) {
		URL base = getBaseURL();
		String docHost = base.getHost();
		int docPort = base.getPort();
		if (docPort == -1) {
			docPort = base.getDefaultPort();
		}
		String linkedHost = linkedURL.getHost();
		int linkedPort = linkedURL.getPort();
		if (linkedPort == -1) {
			linkedPort = linkedURL.getDefaultPort();
		}
		return (docHost.equalsIgnoreCase(linkedHost) || linkedHost.endsWith(docHost)) && docPort == linkedPort;
	}

	/**
	 * Get the referrer policy obtained through the 'Referrer-Policy' header or a meta
	 * element.
	 * 
	 * @return the referrer policy, or the empty string if none was specified.
	 */
	@Override
	public String getReferrerPolicy() {
		return metaReferrerPolicy;
	}

	protected void setReferrerPolicyHeader(String policy) {
		if (metaReferrerPolicy.length() == 0) {
			metaReferrerPolicy = policy;
		}
	}

	@Override
	public String toString() {
		return getChildNodes().toString();
	}

	/**
	 * Opens an InputStream for the given URI, taking into account the Base URL if needed.
	 * 
	 * @param uri
	 *            the uri to open a connection.
	 * @return the InputStream.
	 * @throws IOException
	 *             if the uri was wrong, or the stream could not be opened.
	 */
	public InputStream openStream(String uri) throws IOException {
		return openConnection(getURL(uri)).getInputStream();
	}

	class MyOMStyleSheetList extends StyleSheetList {

		protected MyOMStyleSheetList(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		protected boolean hasErrorsOrWarnings() {
			boolean hasRuleErrors = false;
			Iterator<AbstractCSSStyleSheet> it = iterator();
			while (it.hasNext()) {
				AbstractCSSStyleSheet sheet = it.next();
				SheetErrorHandler eh = sheet.getErrorHandler();
				if (sheet.hasRuleErrorsOrWarnings() || eh.hasSacErrors() || eh.hasSacWarnings() || eh.hasOMErrors()
						|| eh.hasOMWarnings()) {
					hasRuleErrors = true;
					break;
				}
			}
			return hasRuleErrors;
		}

		@Override
		protected Iterator<AbstractCSSStyleSheet> iterator() {
			return super.iterator();
		}

		@Override
		protected void clear() {
			super.clear();
		}

		@Override
		protected boolean needsUpdate() {
			return super.needsUpdate();
		}

		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			super.setNeedsUpdate(needsUpdate);
		}

		@Override
		protected void update() {
			super.update();
			updateStyleLists();
		}

	}

	class MyDefaultErrorHandler extends DefaultErrorHandler {

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return DOMDocument.this.getStyleSheetFactory();
		}

	}

}
