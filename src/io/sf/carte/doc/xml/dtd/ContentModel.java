/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenHandler;
import io.sf.carte.uparser.TokenProducer;

/**
 * XML Content Model information.
 * 
 * @author Carlos Amengual
 */
public class ContentModel {

	private static final Map<String, ContentModel> contentModelMap = new HashMap<String, ContentModel>();

	private final Set<String> emptyElements;

	private ContentModel(Set<String> emptyElements) {
		super();
		this.emptyElements = emptyElements;
	}

	/**
	 * Obtain a set with the names of the elements declared <code>EMPTY</code> in
	 * the given document type declaration.
	 * 
	 * @param docType  the document type.
	 * @param resolver the resolver to retrieve the DTD.
	 * @return the set with <code>EMPTY</code> elements.
	 * @throws SAXException if the <code>docType</code> had errors.
	 * @throws IOException  if an I/O problem happened reading the DTD.
	 */
	private static Set<String> emptyElementsFromDTD(DocumentType docType, EntityResolver2 resolver)
			throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(docType.getName(), docType.getPublicId(), docType.getBaseURI(),
				docType.getSystemId());
		if (isrc == null) {
			throw new IllegalArgumentException("Unable to resolve declaration " + docType.toString());
		}
		Set<String> empty = parseDTD(isrc.getCharacterStream());
		HashSet<String> emptyElementSet = new HashSet<String>(empty.size());
		emptyElementSet.addAll(empty);
		empty.clear();
		return emptyElementSet;
	}

	/**
	 * Obtain a set with the names of the elements declared <code>EMPTY</code> in
	 * the given document type declaration.
	 * 
	 * @param dtDecl   the document type declaration.
	 * @param resolver the resolver to retrieve the DTD.
	 * @return the set with <code>EMPTY</code> elements.
	 * @throws SAXException if the <code>docType</code> had errors.
	 * @throws IOException  if an I/O problem happened reading the DTD.
	 */
	private static Set<String> emptyElementsFromDTD(DocumentTypeDeclaration dtDecl, EntityResolver2 resolver)
			throws SAXException, IOException {
		InputSource isrc = resolver.resolveEntity(dtDecl.getName(), dtDecl.getPublicId(), null,
				dtDecl.getSystemId());
		if (isrc == null) {
			throw new IllegalArgumentException("Unable to resolve declaration " + dtDecl.toString());
		}
		Set<String> empty = parseDTD(isrc.getCharacterStream());
		HashSet<String> emptyElementSet = new HashSet<String>(empty.size());
		emptyElementSet.addAll(empty);
		empty.clear();
		return emptyElementSet;
	}

	private static Set<String> parseDTD(Reader reader) throws IOException {
		Set<String> emptyelmSet = new HashSet<String>(32);
		DTDTokenHandler handler = new DTDTokenHandler(emptyelmSet);
		int[] allowInWords = { '<', '!' };
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(reader, "<!--", "-->");
		return emptyelmSet;
	}

	/**
	 * Gets an instance of ContentModel for the given docType.
	 * 
	 * @param docType the DocumentType.
	 * @return the ContentModel for the given docType, <code>null</code> if not
	 *         found.
	 * @throws SAXException if there was an error in the document type definition or
	 *                      it is not supported.
	 * @throws IOException  if there was an I/O error while retrieving the document
	 *                      type definition.
	 */
	public static ContentModel getModel(DocumentType docType) throws SAXException, IOException {
		String id = docType.getPublicId();
		if (id == null) {
			id = docType.getSystemId();
			if (id == null) {
				id = docType.getName();
			}
		}
		synchronized (contentModelMap) {
			if (id != null && !contentModelMap.containsKey(id)) {
				DefaultEntityResolver resolver = new DefaultEntityResolver();
				ContentModel model;
				try {
					model = new ContentModel(emptyElementsFromDTD(docType, resolver));
				} catch (IllegalArgumentException e) {
					return null;
				}
				contentModelMap.put(id, model);
			}
		}
		return contentModelMap.get(id);
	}

	/**
	 * Gets an instance of ContentModel for the given DOCTYPE declaration.
	 * 
	 * @param docTypeDecl the DOCTYPE declaration.
	 * @return the ContentModel for the given DOCTYPE, <code>null</code> if not
	 *         found or the DOCTYPE declaration could not be parsed.
	 * @throws SAXException if there was an error parsing the document type
	 *                      declaration or it is not supported.
	 * @throws IOException  if there was an I/O error while retrieving the document
	 *                      type definition.
	 */
	public static ContentModel getModel(String docTypeDecl) throws SAXException, IOException {
		DocumentTypeDeclaration dtd = DocumentTypeDeclaration.parse(docTypeDecl);
		String id = dtd.getPublicId();
		if (id == null) {
			id = dtd.getSystemId();
			if (id == null) {
				id = dtd.getName();
			}
		}
		synchronized (contentModelMap) {
			if (id != null && !contentModelMap.containsKey(id)) {
				DefaultEntityResolver resolver = new DefaultEntityResolver();
				ContentModel model;
				try {
					model = new ContentModel(emptyElementsFromDTD(dtd, resolver));
				} catch (IllegalArgumentException e) {
					return null;
				}
				contentModelMap.put(id, model);
			}
		}
		return contentModelMap.get(id);
	}

	/**
	 * Get the content model for XHTML 1.1 transitional.
	 * 
	 * @return the content model.
	 */
	public static ContentModel getXHTML1TransitionalModel() {
		try {
			return getModel(DocumentTypeDeclaration.XHTML1_TRA_DTDECL);
		} catch (SAXException | IOException e) {
		}
		// This should not happen
		throw new IllegalStateException();
	}

	/**
	 * Checks if the content model of the given element is EMPTY.
	 * 
	 * @param name
	 *            the element name.
	 * @return <code>true</code> if the content model is EMPTY, <code>false</code> otherwise.
	 */
	public boolean isEmpty(String name) {
		return emptyElements.contains(name);
	}

	private static class DTDTokenHandler implements TokenHandler {

		private final Set<String> emptySet;

		private byte stage = 0;

		private String currentElement = null;

		DTDTokenHandler(Set<String> emptySet) {
			super();
			this.emptySet = emptySet;
		}

		@Override
		public void tokenControl(TokenControl control) {
		}

		@Override
		public void word(int index, CharSequence word) {
			// Check for ELEMENT
			// <!ELEMENT e EMPTY>
			if ("<!ELEMENT".contentEquals(word)) {
				stage = 1;
			} else if (stage == 1) {
				currentElement = word.toString();
				stage = 2;
			} else if (stage == 2 && "EMPTY".contentEquals(word)) {
				stage = 3;
			} else {
				stage = -1;
			}
		}

		@Override
		public void separator(int index, int codePoint) {
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quote) {
			stage = -1;
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			stage = -1;
		}

		@Override
		public void quotedNewlineChar(int index, int codePoint) {
			stage = -1;
		}

		@Override
		public void openGroup(int index, int codePoint) {
			stage = -1;
		}

		@Override
		public void closeGroup(int index, int codePoint) {
			stage = -1;
		}

		@Override
		public void character(int index, int codePoint) {
			if (codePoint == '>') {
				if (stage == 3) {
					emptySet.add(currentElement);
				}
				stage = 0;
			} else {
				stage = -1;
			}
		}

		@Override
		public void escaped(int index, int codePoint) {
			stage = -1;
		}

		@Override
		public void control(int index, int codePoint) {
		}

		@Override
		public void commented(int index, int commentType, String comment) {
		}

		@Override
		public void endOfStream(int len) {
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
		}

	}

}
