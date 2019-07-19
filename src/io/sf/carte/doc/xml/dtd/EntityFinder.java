/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenHandler;
import io.sf.carte.uparser.TokenProducer;

/**
 * Entity finder.
 * <p>
 * Useful if you want to find the entity equivalences to a set of codepoints, as
 * found in a DTD.
 * 
 * @author Carlos Amengual
 */
public class EntityFinder {

	private EntityResolver2 resolver;

	public EntityFinder(EntityResolver2 resolver) {
		super();
		this.resolver = resolver;
	}

	/**
	 * Find the entities corresponding to the keys in the supplied map, and put them
	 * as the corresponding map values.
	 * 
	 * @param codePoint2Entity the codePoint-entity map.
	 * @param dtdReader        reads from a DTD.
	 * @return the number of entity mappings that could be made. It is always less
	 *         or equal to the size of the supplied map.
	 * @throws IOException if an I/O error occurred while reading the DTD.
	 */
	public int findEntities(Map<Integer, String> codePoint2Entity, Reader dtdReader) throws IOException {
		DTDTokenHandler handler = new DTDTokenHandler(codePoint2Entity);
		int[] allowInWords = { '<', '!' };
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		try {
			tp.parse(dtdReader, "<!--", "-->");
		} catch (StopParsingException e) {
		}
		return handler.mapCount;
	}

	private class DTDTokenHandler implements TokenHandler {

		private Map<Integer, String> codePoint2Entity;
		private int mapCount = 0;
		private final int mapInitialSize;

		private byte stage = 0;

		private String lastEntity = null;

		DTDTokenHandler(Map<Integer, String> codePoint2Entity) {
			super();
			this.codePoint2Entity = codePoint2Entity;
			mapInitialSize = codePoint2Entity.size();
		}

		@Override
		public void tokenControl(TokenControl control) {
		}

		@Override
		public void word(int index, CharSequence word) {
			// Check for ENTITY
			// <!ENTITY nbsp "&#160;">
			if ("<!ENTITY".contentEquals(word)) {
				stage = 1;
			} else if (stage == 1) {
				lastEntity = word.toString();
				stage = 2;
			} else if (stage == 10) {
				if ("SYSTEM".contentEquals(word)) {
					stage = 11;
				}
			} else {
				stage = -1;
			}
		}

		@Override
		public void separator(int index, int codePoint) {
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quote) {
			if (stage == 2) {
				int cp;
				if (quoted.charAt(2) == 'x') {
					String s = quoted.subSequence(3, quoted.length() - 1).toString();
					try {
						cp = Integer.parseInt(s, 16);
					} catch (NumberFormatException e) {
						stage = -1;
						return;
					}
				} else {
					String s = quoted.subSequence(2, quoted.length() - 1).toString();
					try {
						cp = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						stage = -1;
						return;
					}
				}
				if (codePoint2Entity.containsKey(cp) && codePoint2Entity.get(cp) == null) {
					codePoint2Entity.put(cp, lastEntity);
					mapCount++;
					if (mapCount + mapInitialSize == codePoint2Entity.size()) {
						// We have them all
						throw new StopParsingException();
					}
				}
				stage = 3;
			} else if (stage == 11) {
				try {
					InputSource is = resolver.resolveEntity(null, quoted.toString());
					if (is != null) {
						mapCount += findEntities(codePoint2Entity, is.getCharacterStream());
						if (mapCount + mapInitialSize == codePoint2Entity.size()) {
							throw new StopParsingException();
						}
					}
				} catch (SAXException | IOException e) {
				}
				stage = 0;
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
		}

		@Override
		public void quotedNewlineChar(int index, int codePoint) {
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
				stage = 0;
			} else if (codePoint == '%' && stage == 1) {
				stage = 10;
			}
		}

		@Override
		public void escaped(int index, int codePoint) {
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

	private class StopParsingException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		StopParsingException() {
			super();
		}
	}
}
