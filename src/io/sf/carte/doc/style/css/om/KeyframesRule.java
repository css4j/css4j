/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSKeyframesRule;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.KeyframesHandler;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSKeyframesRule.
 * 
 */
public class KeyframesRule extends BaseCSSRule implements CSSKeyframesRule {

	private String name = null;

	private final CSSRuleArrayList cssRules;

	protected KeyframesRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, ExtendedCSSRule.KEYFRAMES_RULE, origin);
		this.cssRules = new CSSRuleArrayList();
	}

	KeyframesRule(AbstractCSSStyleSheet parentSheet, KeyframesRule copyfrom) {
		super(parentSheet, ExtendedCSSRule.KEYFRAMES_RULE, copyfrom.getOrigin());
		this.name = copyfrom.name;
		this.cssRules = copyfrom.cssRules;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName(String keyframesName) {
		name = keyframesName;
	}

	@Override
	public CSSRuleArrayList getCssRules() {
		return cssRules;
	}

	/**
	 * Appends a new rule into this keyframes rule collection.
	 * <p>
	 * According to the specification this method does not throw exceptions, but for this
	 * library's use cases it seems reasonable to check for syntax errors.
	 * 
	 * @param rule
	 *            The parsable text representing the rule.
	 * @throws DOMException
	 *             if there was a problem parsing the rule.
	 */
	@Override
	public void appendRule(String rule) throws DOMException {
		KeyframeRule krule = new KeyframeRule(KeyframesRule.this);
		krule.setCssText(rule);
		cssRules.add(krule);
	}

	@Override
	public void deleteRule(String select) {
		try {
			select = keyframeSelector(select);
		} catch (DOMException e) {
		}
		for (int i = cssRules.getLength() - 1; i >= 0; i--) {
			CSSKeyframeRule rule = (CSSKeyframeRule) cssRules.get(i);
			if (rule.getKeyText().equals(select)) {
				cssRules.remove(i);
				break;
			}
		}
	}

	@Override
	public CSSKeyframeRule findRule(String select) throws DOMException {
		try {
			select = keyframeSelector(select);
		} catch (DOMException e) {
		}
		for (int i = cssRules.getLength() - 1; i >= 0; i--) {
			CSSKeyframeRule rule = (CSSKeyframeRule) cssRules.get(i);
			if (rule.getKeyText().equals(select)) {
				return rule;
			}
		}
		return null;
	}

	String keyframeSelector(String rawselector) {
		Reader re = new StringReader(rawselector);
		InputSource source = new InputSource(re);
		Parser parser = createSACParser();
		LexicalUnit selunit;
		try {
			selunit = parser.parsePropertyValue(source);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return keyframeSelector(selunit);
	}

	private static String keyframeSelector(LexicalUnit selunit) {
		StringBuilder buffer = new StringBuilder();
		appendSelector(buffer, selunit);
		LexicalUnit lu = selunit.getNextLexicalUnit();
		while (lu != null) {
			LexicalUnit nextlu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalUnit.SAC_OPERATOR_COMMA || nextlu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR,
						"Wrong keyframe selector syntax: " + selunit.toString());
			}
			buffer.append(',');
			appendSelector(buffer, nextlu);
			lu = nextlu.getNextLexicalUnit();
		}
		return buffer.toString();
	}

	private static void appendSelector(StringBuilder buffer, LexicalUnit selunit) {
		short type = selunit.getLexicalUnitType();
		if (type == LexicalUnit.SAC_IDENT || type == LexicalUnit.SAC_STRING_VALUE) {
			buffer.append(selunit.getStringValue());
		} else if (type == LexicalUnit.SAC_PERCENTAGE) {
			float floatValue = selunit.getFloatValue();
			if(floatValue % 1 != 0) {
				buffer.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buffer.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			buffer.append('%');
		} else if (type == LexicalUnit.SAC_INTEGER && selunit.getIntegerValue() == 0) {
			buffer.append('0');
		} else {
			throw new DOMException(DOMException.SYNTAX_ERR, "Wrong keyframe selector: " + selunit.toString());
		}
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(30 + getCssRules().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		if (name != null || !getCssRules().isEmpty()) {
			StringBuilder sb = new StringBuilder(30 + getCssRules().getLength() * 20);
			sb.append("@keyframes");
			if (name != null) {
				String sname = getName();
				if (sname.indexOf(' ') == -1) {
					sb.append(' ').append(sname);
				} else {
					sb.append(' ').append('\'').append(sname).append('\'');
				}
			}
			sb.append('{');
			Iterator<AbstractCSSRule> it = getCssRules().iterator();
			while (it.hasNext()) {
				sb.append(it.next().getMinifiedCssText());
			}
			sb.append('}');
			return sb.toString();
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (name != null || !getCssRules().isEmpty()) {
			context.startRule(wri);
			wri.write("@keyframes ");
			if (name != null) {
				String sname = getName();
				if (sname.indexOf(' ') == -1) {
					wri.write(sname);
				} else {
					wri.write('\'');
					wri.write(sname);
					wri.write('\'');
				}
			}
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			getCssRules().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endRuleList(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int len = cssText.length();
		if (len < 16 || cssText.charAt(len - 1) != '}') {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @keyframes rule: " + cssText);
		}
		CharSequence atkeyword = cssText.subSequence(0, 11);
		if (!ParseHelper.startsWithIgnoreCase(atkeyword, "@keyframes ")) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a @keyframes rule: " + cssText);
		}
		String body = cssText.substring(11, len);
		PropertyDocumentHandler handler = new MyKeyframesHandler();
		CSSParser parser;
		try {
			parser = (CSSParser) createSACParser();
		} catch (ClassCastException e) {
			parser = new CSSParser();
		}
		parser.setDocumentHandler(handler);
		try {
			parser.parseKeyFramesBody(body);
		} catch (CSSParseException e) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, e.getMessage());
		}
	}

	private class MyKeyframesHandler extends PropertyDocumentHandler implements KeyframesHandler {

		private String name = null;
		private final CSSRuleArrayList cssRules = new CSSRuleArrayList();

		private KeyframeRule currentRule = null;

		private LinkedList<String> comments = null;

		MyKeyframesHandler() {
			super();
		}

		@Override
		public void startKeyframes(String name) {
			this.name = name;
		}

		@Override
		public void endKeyframes() {
			KeyframesRule.this.name = name;
			KeyframesRule.this.cssRules.clear();
			KeyframesRule.this.cssRules.addAll(cssRules);
		}

		@Override
		public void startKeyframe(LexicalUnit keyframeSelector) {
			currentRule = new KeyframeRule(KeyframesRule.this);
			currentRule.setKeyText(keyframeSelector(keyframeSelector));
			setLexicalPropertyListener(currentRule.getLexicalPropertyListener());
			this.cssRules.add(currentRule);
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) throws CSSException {
			if (currentRule != null) {
				if (important) {
					// Declarations marked as important must be ignored
					CSSPropertyValueException ex = new CSSPropertyValueException(
							"Important declarations in a keyframe rule are not valid");
					ex.setValueText(value.toString() + " !important");
					currentRule.getStyleDeclarationErrorHandler().wrongValue(name, ex);
				} else {
					try {
						super.property(name, value, important);
					} catch (DOMException e) {
						if (currentRule.getStyleDeclarationErrorHandler() != null) {
							CSSPropertyValueException ex = new CSSPropertyValueException(e);
							ex.setValueText(value.toString());
							currentRule.getStyleDeclarationErrorHandler().wrongValue(name, ex);
						}
					}
				}
			} else {
				throw new CSSException("Declaration outside of keyframe rule");
			}
		}

		@Override
		public void endKeyframe() {
			setCommentsToRule(currentRule);
			currentRule = null;
			setLexicalPropertyListener(null);
		}

		@Override
		public void comment(String text) throws CSSException {
			if (currentRule == null) {
				if (comments == null) {
					comments = new LinkedList<String>();
				}
				comments.add(text);
			}
		}

		private void setCommentsToRule(AbstractCSSRule rule) {
			if (comments != null && !comments.isEmpty()) {
				ArrayList<String> ruleComments = new ArrayList<String>(comments.size());
				ruleComments.addAll(comments);
				rule.setPrecedingComments(ruleComments);
			}
			resetCommentStack();
		}

		private void resetCommentStack() {
			if (comments != null) {
				comments.clear();
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cssRules == null) ? 0 : cssRules.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		KeyframesRule other = (KeyframesRule) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (cssRules == null) {
			if (other.cssRules != null) {
				return false;
			}
		} else if (!cssRules.equals(other.cssRules)) {
			return false;
		}
		return true;
	}

	@Override
	public KeyframesRule clone(AbstractCSSStyleSheet parentSheet) {
		return new KeyframesRule(parentSheet, this);
	}

}
