/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSKeyframeRule;
import io.sf.carte.doc.style.css.CSSKeyframesRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.CommentRemover;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSKeyframesRule.
 * 
 */
public class KeyframesRule extends BaseCSSRule implements CSSKeyframesRule {

	private static final long serialVersionUID = 1L;

	private String name = null;

	private final CSSRuleArrayList cssRules;

	protected KeyframesRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.KEYFRAMES_RULE, origin);
		this.cssRules = new CSSRuleArrayList();
	}

	KeyframesRule(AbstractCSSStyleSheet parentSheet, KeyframesRule copyfrom) {
		super(parentSheet, CSSRule.KEYFRAMES_RULE, copyfrom.getOrigin());
		this.name = copyfrom.name;
		this.cssRules = copyfrom.cssRules;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String keyframesName) {
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
		Parser parser = createSACParser();
		LexicalUnit selunit;
		try {
			selunit = parser.parsePropertyValue(re);
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

	static String keyframeSelector(LexicalUnit selunit) {
		StringBuilder buffer = new StringBuilder();
		appendSelector(buffer, selunit);
		LexicalUnit lu = selunit.getNextLexicalUnit();
		while (lu != null) {
			LexicalUnit nextlu = lu.getNextLexicalUnit();
			if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA || nextlu == null) {
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
		LexicalType type = selunit.getLexicalUnitType();
		if (type == LexicalType.IDENT || type == LexicalType.STRING) {
			buffer.append(selunit.getStringValue());
		} else if (type == LexicalType.PERCENTAGE) {
			float floatValue = selunit.getFloatValue();
			if (floatValue % 1 != 0) {
				buffer.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buffer.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			buffer.append('%');
		} else if (type == LexicalType.INTEGER && selunit.getIntegerValue() == 0) {
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
			context.startRule(wri, getPrecedingComments());
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
			context.endRule(wri, getTrailingComments());
		}
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int len = cssText.length();
		int atIdx = cssText.indexOf('@');
		if (len < 14 || atIdx == -1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @keyframes rule: " + cssText);
		}
		String ncText = CommentRemover.removeComments(cssText).toString().trim();
		CharSequence atkeyword = ncText.subSequence(0, 11);
		if (!ParseHelper.startsWithIgnoreCase(atkeyword, "@keyframes")
				|| !Character.isWhitespace(atkeyword.charAt(10))) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a @keyframes rule: " + cssText);
		}
		String body = cssText.substring(atIdx + 11);
		PropertyCSSHandler handler = new MyKeyframesHandler();
		CSSParser parser = (CSSParser) createSACParser();
		parser.setDocumentHandler(handler);
		try {
			parser.parseKeyFramesBody(body);
		} catch (CSSParseException e) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, e.getMessage());
		}
	}

	@Override
	void clear() {
		cssRules.clear();
		resetComments();
	}

	@Override
	void setRule(AbstractCSSRule copyMe) {
		setPrecedingComments(copyMe.getPrecedingComments());
		setTrailingComments(copyMe.getTrailingComments());
		KeyframesRule other = (KeyframesRule) copyMe;
		name = other.name;
		cssRules.clear();
		cssRules.addAll(other.getCssRules());
		for (AbstractCSSRule rule : cssRules) {
			rule.setParentRule(this);
		}
	}

	private class MyKeyframesHandler extends PropertyCSSHandler {

		private String name = null;
		private final CSSRuleArrayList cssRules = new CSSRuleArrayList();

		private KeyframeRule currentRule = null;

		private KeyframeRule lastRule = null;

		private StringList comments = null;

		private MyKeyframesHandler() {
			super();
		}

		@Override
		public void startKeyframes(String name) {
			this.name = name;
			newRule();
		}

		private void newRule() {
			lastRule = null;
		}

		@Override
		public void endKeyframes() {
			KeyframesRule.this.name = name;
			KeyframesRule.this.cssRules.clear();
			KeyframesRule.this.cssRules.addAll(cssRules);
			resetComments();
		}

		@Override
		public void startKeyframe(LexicalUnit keyframeSelector) {
			newRule();
			currentRule = new KeyframeRule(KeyframesRule.this);
			currentRule.setKeyText(keyframeSelector(keyframeSelector));
			setLexicalPropertyListener(currentRule.getLexicalPropertyListener());
			this.cssRules.add(currentRule);
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
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
			lastRule = currentRule;
			setLexicalPropertyListener(null);
		}

		@Override
		public void comment(String text, boolean precededByLF) {
			if (lastRule != null && !precededByLF) {
				if (lastRule.getTrailingComments() == null) {
					lastRule.setTrailingComments(new LinkedStringList());
				}
				lastRule.getTrailingComments().add(text);
			} else {
				if (currentRule == null) {
					if (comments == null) {
						comments = new LinkedStringList();
					}
					comments.add(text);
				}
			}
		}

		private void setCommentsToRule(AbstractCSSRule rule) {
			if (comments != null && !comments.isEmpty()) {
				LinkedStringList ruleComments = new LinkedStringList();
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
	boolean hasErrorsOrWarnings() {
		for (AbstractCSSRule rule : cssRules) {
			if (rule.hasErrorsOrWarnings()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cssRules.hashCode();
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
