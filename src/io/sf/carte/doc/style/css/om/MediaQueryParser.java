/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenHandler;
import io.sf.carte.uparser.TokenProducer;

class MediaQueryParser {

	public static void parse(String mediaQueryString, MediaQueryHandler mqhandler) {
		int[] allowInWords = { 45, 95 }; // -_
		MQTokenHandler handler = new MQTokenHandler(mqhandler);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(mediaQueryString, "/*", "*/");
	}

	private static ExtendedCSSPrimitiveValue parseMediaFeature(String stringValue) {
		ExtendedCSSPrimitiveValue value;
		try {
			value = new ValueFactory().parseMediaFeature(stringValue);
		} catch (RuntimeException e) {
			value = null;
		}
		return value;
	}

	/**
	 * Determine whether this looks like a media feature (rather than a value).
	 * 
	 * @param string the presumed feature name,
	 * @return <code>true</code> if the string looks like a media feature.
	 */
	private static boolean isKnownFeature(String string) {
		return string.startsWith("min-") || string.startsWith("max-") || MediaQueryFactory.isMediaFeature(string)
				|| string.startsWith("device-");
	}

	private static boolean isValidFeatureSyntax(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isLetter(c) && c != '-') {
				return false;
			}
		}
		return true;
	}

	static class MQTokenHandler implements TokenHandler {
		MediaQueryHandler handler;
		byte stage = 0;
		StringBuilder buffer = new StringBuilder(64);
		boolean spaceFound = false, escaped = false;
		String featureName = null;
		String firstValue = null;
		int prevcp = 32;
		boolean functionToken = false;
		short parendepth = 0;
		byte rangeType = 0; // Type of range expression, 0 if none

		private static final int WORD_UNQUOTED = 0;

		MQTokenHandler(MediaQueryHandler handler) {
			super();
			this.handler = handler;
		}

		@Override
		public void word(int index, CharSequence word) {
			// @formatter:off
			//
			// Stages:
			// not medium and ( feature : value )
			//        0  | 1 |2|   3     |  4    |1
			// not medium and ( value1  <= feature < value2 )
			//        0  | 1 |2|   3     |5|  6     |7       |1
			// 127 = error
			//
			// @formatter:on
			if (stage == 127) {
				return;
			}
			if (functionToken) {
				if (buffer.length() != 0) {
					buffer.append(' ');
				}
				buffer.append(word);
			} else if (ParseHelper.equalsIgnoreCase(word, "not")) {
				if (stage != 0) {
					reportError(index, "Found 'not' at the wrong parsing stage");
				} else {
					handler.negativeQuery();
				}
			} else if (ParseHelper.equalsIgnoreCase(word, "only")) {
				if (stage != 0) {
					reportError(index, "Found 'only' at the wrong parsing stage");
				} else {
					handler.onlyPrefix();
				}
			} else if (ParseHelper.equalsIgnoreCase(word, "and")) {
				if (stage != 0 && stage != 1) {
					reportError(index, "Found 'and' at the wrong parsing stage");
					return;
				}
				if (buffer.length() != 0) {
					handler.mediaType(rawBuffer());
				}
				stage = 2;
			} else if (ParseHelper.equalsIgnoreCase(word, "or")) {
				handler.invalidQuery("Found 'or'");
			} else { // rest of cases are collected to buffer
				if (!appendWord(index, word, WORD_UNQUOTED)) {
					return;
				}
			}
			prevcp = 65; // A
		}

		private void processBuffer() {
			if (buffer.length() != 0) {
				if (stage == 1) {
					handler.mediaType(rawBuffer());
				} else if (stage == 6) {
					featureName = rawBuffer();
				}
			}
		}

		private String rawBuffer() {
			String raw = buffer.toString();
			buffer.setLength(0);
			spaceFound = false;
			escaped = false;
			return raw;
		}

		private boolean appendWord(int index, CharSequence word, int quote) {
			if (buffer.length() != 0) {
				if (prevcp == 32 || prevcp == 13) {
					if (stage == 1) {
						reportError(index, "Found white space between media");
						return false;
					}
					spaceFound = true;
					buffer.append(' ');
				}
			}
			if (quote == WORD_UNQUOTED) {
				buffer.append(word);
			} else {
				char c = (char) quote;
				buffer.append(c).append(word).append(c);
			}
			if (!functionToken) {
				if (stage == 0) {
					stage = 1;
				} else if (stage == 5) { // after "value [<][=]"
					stage = 6;
				}
			}
			return true;
		}

		@Override
		public void openGroup(int index, int codepoint) {
			if (codepoint == 40) { // '('
				if (prevcp != 32 && prevcp != 13) {
					// Function token
					functionToken = true;
					buffer.append('(');
				} else {
					if (stage == 2 || stage == 0) {
						stage = 3;
					}
				}
				parendepth++;
			} else {
				reportError(index, "Unexpected " + new String(Character.toChars(codepoint)));
			}
			prevcp = codepoint;
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			if (codepoint == 41) { // ')'
				parendepth--;
				if (functionToken) {
					buffer.append(')');
					functionToken = false;
				} else {
					if (stage == 6) {
						processBuffer();
						// Need to determine whether we have "value <|>|= feature"
						// or "feature <|>|= value"
						if (firstValue != null && isKnownFeature(firstValue)) {
							String tempstr = firstValue;
							firstValue = featureName;
							featureName = tempstr;
						} else if (!isKnownFeature(featureName)) {
							if (isValidFeatureSyntax(firstValue)) {
								String tempstr = firstValue;
								firstValue = featureName;
								featureName = tempstr;
							} else if (!isValidFeatureSyntax(featureName)) {
								reportError(index,
										"Wrong feature expression near " + featureName + " " + firstValue + ")");
								prevcp = codepoint;
								return;
							} else {
								reverseRangetype();
							}
						} else {
							reverseRangetype();
						}
						ExtendedCSSPrimitiveValue value1 = parseMediaFeature(firstValue);
						if (value1 == null) {
							error(index, ParseHelper.ERR_WRONG_VALUE, firstValue);
						} else {
							handler.featureRange(featureName, rangeType, value1, null);
						}
					} else if (buffer.length() != 0) {
						if (stage == 4) {
							ExtendedCSSPrimitiveValue value = parseMediaFeature(buffer.toString());
							if (value == null) {
								error(index, ParseHelper.ERR_WRONG_VALUE, buffer.toString());
							} else {
								handler.featureValue(featureName, value);
							}
						} else if (stage == 7) {
							ExtendedCSSPrimitiveValue value1 = parseMediaFeature(firstValue);
							ExtendedCSSPrimitiveValue value2 = parseMediaFeature(buffer.toString());
							if (value1 == null) {
								error(index, ParseHelper.ERR_WRONG_VALUE, firstValue);
							} else if (value2 == null) {
								error(index, ParseHelper.ERR_WRONG_VALUE, buffer.toString());
							} else {
								handler.featureRange(featureName, rangeType, value1, value2);
							}
						} else if (stage == 3 && !spaceFound) {
							handler.featureValue(buffer.toString(), null);
						} else {
							error(index, ParseHelper.ERR_EXPR_SYNTAX, buffer.toString());
						}
						buffer.setLength(0);
						spaceFound = false;
						escaped = false;
					} else {
						reportError(index, "Unexpected )");
					}
					if (stage == 5) {
						reportError(index, "Unexpected )");
					} else {
						rangeType = 0;
						stage = 1;
					}
				}
			} else {
				reportError(index, "Unexpected " + new String(Character.toChars(codepoint)));
			}
			prevcp = codepoint;
		}

		/**
		 * Reverse the current range type.
		 * <p>
		 * Range type is a way to numerically characterize a range like 'a <= foo < b'
		 */
		private void reverseRangetype() {
			if ((rangeType & 2) == 2) {
				rangeType ^= 2;
				rangeType = (byte) (rangeType | 4);
			} else if ((rangeType & 4) == 4) {
				rangeType ^= 4;
				rangeType = (byte) (rangeType | 2);
			}
		}

		@Override
		public void character(int index, int codepoint) {
			// ! 33
			// : 58
			// ; 59
			if (functionToken) {
				if (prevcp == 32) {
					buffer.append(' ');
				}
				buffer.append(Character.toChars(codepoint));
			} else {
				if (codepoint == 58) { // ':'
					if (buffer.length() != 0) {
						featureName = rawBuffer();
						stage = 4;
					} else {
						reportError(index, "Empty feature name");
					}
				} else if (codepoint == 44) { // ,
					if (parendepth != 0) {
						reportError(index, "Unmatched parenthesis");
					} else if (stage == 0) {
						reportError(index, "No media found");
					}
					processBuffer();
					stage = 0;
					prevcp = 32;
					spaceFound = false;
					handler.endQuery();
				} else if (codepoint == 46) { // .
					if (stage == 4 || stage == 3 || stage == 7 || functionToken) {
						buffer.append('.');
					} else {
						reportError(index, "Unexpected '.'");
					}
				} else if (codepoint == 47) { // /
					if (stage == 4 || stage == 3 || stage == 6 || stage == 7 || functionToken) {
						buffer.append('/');
					} else {
						reportError(index, "Unexpected '/'");
					}
				} else if (codepoint == 59) {
					error(index, ParseHelper.ERR_UNEXPECTED_CHAR, ";");
				} else if (codepoint == 60) { // <
					// rangeType:
					// = 1, < 2, > 4,
					// <= 3, >= 5
					// a <= foo < b ; 19
					// a >= foo > b ; 37
					if (stage < 3 || (rangeType > 3 && ((rangeType & 16) != 0 || (rangeType & 4) != 0))) {
						reportError(index, "Unexpected <");
					} else {
						if (stage != 6 && stage != 7) {
							rangeType = (byte) (rangeType | 2);
							stage = 5;
						} else {
							processBuffer();
							rangeType = (byte) (rangeType | 16);
							stage = 7;
						}
					}
				} else if (codepoint == 61) { // =
					if (stage < 3 || (rangeType > 5 && (rangeType & 8) != 0)) {
						reportError(index, "Unexpected =");
					} else {
						if (stage != 6 && stage != 7) {
							rangeType = (byte) (rangeType | 1);
							stage = 5;
						} else {
							processBuffer();
							rangeType = (byte) (rangeType | 8);
							stage = 7;
						}
					}
				} else if (codepoint == 62 || (rangeType >= 4 && ((rangeType & 32) != 0 || (rangeType & 2) != 0))) { // >
					if (stage < 3) {
						reportError(index, "Unexpected >");
					} else {
						if (stage != 6 && stage != 7) {
							rangeType = (byte) (rangeType | 4);
							stage = 5;
						} else {
							processBuffer();
							rangeType = (byte) (rangeType | 32);
							stage = 7;
						}
					}
				} else {
					reportError(index, "Unexpected " + new String(Character.toChars(codepoint)));
				}
				if (stage == 5 && firstValue == null && buffer.length() != 0) {
					firstValue = rawBuffer();
				}
			}
			prevcp = codepoint;
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			appendWord(index, quoted, quoteCp);
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			appendWord(index, quoted, quoteCp);
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (stage != 127) {
				if (escaped) {
					reportError(index, "Unexpected \\");
				} else {
					escaped = true;
					buffer.append('\\').append(Character.toChars(codepoint));
					if (stage == 5) {
						stage = 6;
					} else if (stage == 0) {
						stage = 1;
					}
				}
			}
			prevcp = codepoint;
		}

		@Override
		public void separator(int index, int cp) {
			if (escaped && CSSParser.bufferEndsWithEscapedCharOrWS(buffer)) {
				buffer.append(' ');
			} else {
				prevcp = 32;
			}
		}

		@Override
		public void control(int index, int codepoint) {
			if (escaped && CSSParser.bufferEndsWithEscapedCharOrWS(buffer)) {
				buffer.append(' '); // break the escape
				escaped = false;
			}
			prevcp = 13;
		}

		@Override
		public void quotedNewlineChar(int index, int codepoint) {
		}

		@Override
		public void commented(int index, int commentType, String comment) {
		}

		@Override
		public void endOfStream(int len) {
			if (parendepth != 0) {
				handler.invalidQuery("Unmatched parenthesis");
			} else if (stage == 1) {
				processBuffer();
			} else if (stage > 1) {
				handler.invalidQuery("Unexpected end of media text");
			}
			handler.endQuery();
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			reportError(index, "Bad query near: " + context.toString());
		}

		private void reportError(int index, String message) {
			handler.invalidQuery(message);
			stage = 127;
		}

		@Override
		public void tokenControl(TokenControl control) {
		}
	}

}
