/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.parser.CSSParser.SelectorTokenHandler;
import io.sf.carte.doc.style.css.parser.CSSParser.SelectorTokenHandler.SelectorArgumentManager;
import io.sf.carte.uparser.TokenProducer;

class ConditionSetterFactory {

	private static final HashMap<String, ConditionSetter> pcSetters = createPseudoClassSetterMap();

	private static final HashMap<String, ConditionSetter> peSetters = createPseudoElementSetterMap();

	private static final ConditionSetter pseudoClassSetter = new PseudoClassConditionSetter();

	private static final ConditionSetter pseudoElementSetter = new PseudoElementConditionSetter();

	private static final ConditionSetterFactory instance = new ConditionSetterFactory();

	private ConditionSetterFactory() {
		super();
	}

	private static HashMap<String, ConditionSetter> createPseudoClassSetterMap() {
		HashMap<String, ConditionSetter> setters = new HashMap<>(23);

		setters.put("lang", new LangConditionSetter());
		setters.put("first-child", new FirstChildConditionSetter());
		setters.put("last-child", new LastChildConditionSetter());
		setters.put("nth-child", new NthChildConditionSetter());
		setters.put("nth-last-child", new NthLastChildConditionSetter());
		setters.put("first-of-type", new FirstOfTypeConditionSetter());
		setters.put("last-of-type", new LastOfTypeConditionSetter());
		setters.put("nth-of-type", new NthOfTypeConditionSetter());
		setters.put("nth-last-of-type", new NthLastOfTypeConditionSetter());
		setters.put("only-child", new OnlyChildConditionSetter());
		setters.put("only-of-type", new OnlyOfTypeConditionSetter());
		setters.put("not", new SelectorArgumentConditionSetter());
		setters.put("is", new SelectorArgumentConditionSetter());
		setters.put("where", new SelectorArgumentConditionSetter());
		setters.put("host-context", new SelectorArgumentConditionSetter());
		setters.put("has", new HasConditionSetter());
		setters.put("host", new MaybeSelectorArgumentConditionSetter());
		setters.put("dir", new ArgumentPseudoClassConditionSetter());

		// Old-syntax pseudo-elements
		setLegacyPseudoElementSetters(setters);

		return setters;
	}

	private static HashMap<String, ConditionSetter> createPseudoElementSetterMap() {
		HashMap<String, ConditionSetter> setters = new HashMap<>(8);
		setters.put("slotted", new SelectorArgumentPEConditionSetter());
		setters.put("highlight", new ArgumentPseudoElementConditionSetter());
		setters.put("picker", new ArgumentPseudoElementConditionSetter());
		setLegacyPseudoElementSetters(setters);
		return setters;
	}

	private static void setLegacyPseudoElementSetters(Map<String, ConditionSetter> setters) {
		setters.put("first-line", new NoArgumentPseudoElementConditionSetter());
		setters.put("first-letter", new NoArgumentPseudoElementConditionSetter());
		setters.put("before", new NoArgumentPseudoElementConditionSetter());
		setters.put("after", new NoArgumentPseudoElementConditionSetter());
	}

	static ConditionSetterFactory getInstance() {
		return instance;
	}

	/**
	 * Get the pseudo-class setter.
	 * 
	 * @param name the lowercase pseudo-class name.
	 * @return the pseudo-class setter.
	 */
	ConditionSetter getPseudoClassSetter(String name) {
		ConditionSetter setter = pcSetters.get(name);
		if (setter == null) {
			setter = pseudoClassSetter;
		}
		return setter;
	}

	/**
	 * Get the pseudo-element setter.
	 * 
	 * @param name the lowercase pseudo-element name.
	 * @return the pseudo-class setter.
	 */
	ConditionSetter getPseudoElementSetter(String name) {
		ConditionSetter setter = peSetters.get(name);
		if (setter == null) {
			setter = pseudoElementSetter;
		}
		return setter;
	}

	interface ConditionSetter {

		/**
		 * Create a condition.
		 * 
		 * @param index
		 * @param triggerCp
		 * @param name
		 * @param handler
		 * @return the condition, or {@code null} if the condition is in error.
		 */
		AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler);

		/**
		 * Set the argument.
		 *
		 * @param index
		 * @param cond
		 * @param handler the handler. This method may leave the handler in error state.
		 */
		void setArgument(int index, Condition cond, CSSParser.SelectorTokenHandler handler);

	}

	abstract private static class NoArgumentConditionSetter implements ConditionSetter {

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				handler.handleError(index - 2, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected functional argument: " + handler.buffer);
			}
		}

		boolean checkLeftParen(int index, int triggerCp, SelectorTokenHandler handler) {
			if (triggerCp == '(') {
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"Pseudo-class cannot have argument");
				return true;
			}
			return false;
		}

	}

	abstract private static class PseudoConditionSetter implements ConditionSetter {

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				setNameArgument(index, (PseudoConditionImpl) cond, handler);
			} else {
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Pseudo-class "
						+ ((PseudoConditionImpl) cond).getName() + " requires an argument.");
			}
		}

		void setNameArgument(int index, PseudoConditionImpl cond, SelectorTokenHandler handler) {
			String s = handler.unescapeBuffer(index);
			// Check the validity of the argument.
			// We allow quoted arguments and anything inside prefixed selectors.
			if (!isValidPseudoArg(s, cond)) {
				handler.handleWarning(index - s.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected functional argument: " + s);
			}
			cond.argument = s;
		}

		private boolean isValidPseudoArg(String s, PseudoConditionImpl cond) {
			char c;
			if (!CSSUtil.isValidPseudoName(s) && (c = s.charAt(0)) != '"' && c != '\''
					&& cond.name.charAt(0) != '-') {
				if (s.indexOf(' ') >= 0) {
					StringTokenizer st = new StringTokenizer(s, " ");
					while (st.hasMoreElements()) {
						String ident = st.nextToken();
						if (!CSSUtil.isValidPseudoName(ident)) {
							return false;
						}
					}
				} else {
					return false;
				}
			}
			return true;
		}

	}

	private static class PseudoClassConditionSetter extends PseudoConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PseudoConditionImpl cond;
			if (CSSUtil.isValidPseudoName(name)) {
				cond = new PseudoConditionImpl(ConditionType.PSEUDO_CLASS);
				cond.setName(name);
			} else {
				handler.handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
						"Invalid pseudo-class: " + name);
				cond = null;
			}
			return cond;
		}

	}

	private static class ArgumentPseudoClassConditionSetter extends PseudoConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PseudoConditionImpl cond;
			if (CSSUtil.isValidPseudoName(name)) {
				cond = new PseudoConditionImpl(ConditionType.PSEUDO_CLASS);
				cond.setName(name);
			} else {
				handler.handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
						"Invalid pseudo-class: " + name);
				cond = null;
			}
			return cond;
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				setNameArgument(index, (PseudoConditionImpl) cond, handler);
			} else {
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Pseudo-class "
						+ ((PseudoConditionImpl) cond).getName() + " requires an argument.");
			}
		}

	}

	private static class PseudoElementConditionSetter extends PseudoConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PseudoConditionImpl cond = null;
			if (!CSSUtil.isValidPseudoName(name)) {
				handler.handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
						"Invalid pseudo-element: " + name);
			} else if (handler.isInsideHas()) {
				// See CSSWG issue 7463
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"For security reasons, pseudo-elements aren't allowed inside a has().");
			} else {
				cond = new PseudoConditionImpl(ConditionType.PSEUDO_ELEMENT);
				cond.setName(name);
			}
			return cond;
		}

	}

	private static class ArgumentPseudoElementConditionSetter extends PseudoConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PseudoConditionImpl cond = null;
			if (!CSSUtil.isValidPseudoName(name)) {
				handler.handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
						"Invalid pseudo-element: " + name);
			} else if (handler.isInsideHas()) {
				// See CSSWG issue 7463
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"For security reasons, pseudo-elements aren't allowed inside a has().");
			} else {
				cond = new PseudoConditionImpl(ConditionType.PSEUDO_ELEMENT);
				cond.setName(name);
			}
			return cond;
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				setNameArgument(index, (PseudoConditionImpl) cond, handler);
			} else {
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Pseudo-element "
						+ ((PseudoConditionImpl) cond).getName() + " requires an argument.");
			}
		}

	}

	private static class NoArgumentPseudoElementConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PseudoConditionImpl cond = null;
			if (!checkLeftParen(index, triggerCp, handler)) {
				if (handler.isInsideHas()) {
					// See CSSWG issue 7463
					handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
							"For security reasons, pseudo-elements aren't allowed inside a has().");
				} else {
					cond = new PseudoConditionImpl(ConditionType.PSEUDO_ELEMENT);
					cond.setName(name);
				}
			}
			return cond;
		}

	}

	private static class LangConditionSetter extends PseudoConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			return new LangConditionImpl();
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				String s = handler.unescapeBuffer(index);
				int len = s.length();
				if (s.charAt(len - 1) == ',') {
					handler.handleError(index - 2, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected functional argument: " + s);
				} else {
					((LangConditionImpl) cond).lang = s;
				}
			} else {
				handler.unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			}
		}

	}

	private static class FirstChildConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			return new PositionalConditionImpl(false);
		}

	}

	private static class LastChildConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			PositionalConditionImpl condition = new PositionalConditionImpl(false);
			condition.offset = 1;
			condition.forwardCondition = false;
			return condition;
		}

	}

	private static class FirstOfTypeConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			PositionalConditionImpl condition = new PositionalConditionImpl(false);
			condition.oftype = true;
			condition.offset = 1;
			return condition;
		}

	}

	private static class LastOfTypeConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			PositionalConditionImpl condition = new PositionalConditionImpl(false);
			condition.oftype = true;
			condition.offset = 1;
			condition.forwardCondition = false;
			return condition;
		}

	}

	private static class OnlyChildConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			return handler.factory.createAttributeCondition(ConditionType.ONLY_CHILD);
		}

	}

	private static class OnlyOfTypeConditionSetter extends NoArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (checkLeftParen(index, triggerCp, handler)) {
				return null;
			}
			return handler.factory.createAttributeCondition(ConditionType.ONLY_TYPE);
		}

	}

	abstract private static class PositionalArgumentConditionSetter implements ConditionSetter {

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			if (handler.buffer.length() != 0) {
				String arg = handler.rawBuffer();
				if (!parsePositionalArgument((PositionalConditionImpl) cond, arg, handler)) {
					handler.handleError(index, ParseHelper.ERR_EXPR_SYNTAX,
							"Wrong subexpression: " + arg);
				}
			} else {
				handler.unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			}
		}

	}

	private static boolean parsePositionalArgument(PositionalConditionImpl cond, String expression,
			SelectorTokenHandler handler) {
		AnBExpression expr = handler.new MyAnBExpression();
		try {
			expr.parse(expression);
		} catch (IllegalArgumentException e) {
			return false;
		}
		cond.offset = expr.getOffset();
		cond.slope = expr.getStep();
		cond.ofList = expr.getSelectorList();
		cond.hasKeyword = expr.isKeyword();
		return true;
	}

	private static class NthChildConditionSetter extends PositionalArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			return new PositionalConditionImpl(true);
		}

	}

	private static class NthLastChildConditionSetter extends PositionalArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PositionalConditionImpl condition = new PositionalConditionImpl(true);
			condition.offset = 1;
			condition.forwardCondition = false;
			return condition;
		}

	}

	private static class NthOfTypeConditionSetter extends PositionalArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PositionalConditionImpl condition = new PositionalConditionImpl(true);
			condition.oftype = true;
			return condition;
		}

	}

	private static class NthLastOfTypeConditionSetter extends PositionalArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			PositionalConditionImpl condition = new PositionalConditionImpl(true);
			condition.oftype = true;
			condition.forwardCondition = false;
			return condition;
		}

	}

	private static class SelectorArgumentConditionSetter implements ConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (triggerCp != TokenProducer.CHAR_LEFT_PAREN) {
				pseudoMustHaveArgumentError(index, name, triggerCp, handler);
				return null;
			}
			SelectorArgumentConditionImpl condition = new SelectorArgumentConditionImpl();
			condition.setName(name);
			return condition;
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			SelectorArgumentConditionImpl argcond = (SelectorArgumentConditionImpl) cond;
			if (handler.buffer.length() != 0) {
				parseSelectorArgument(index, argcond, handler);
			} else {
				handler.unexpectedCharError(index, TokenProducer.CHAR_RIGHT_PAREN);
			}
		}

		void parseSelectorArgument(int index, SelectorArgumentConditionImpl argcond,
				SelectorTokenHandler handler) {
			try {
				argcond.arguments = ConditionSetterFactory.parseSelectorArgument(handler);
			} catch (CSSParseException e) {
				byte errCode;
				if (e.getClass() == CSSNamespaceParseException.class) {
					errCode = ParseHelper.ERR_UNKNOWN_NAMESPACE;
				} else {
					errCode = ParseHelper.ERR_EXPR_SYNTAX;
				}
				CSSParseException ex = handler.createException(index, errCode, e.getMessage());
				handler.handleError(ex);
				handler.stage = 127;
			}
		}

	}

	private static class SelectorArgumentPEConditionSetter extends SelectorArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (triggerCp != TokenProducer.CHAR_LEFT_PAREN) {
				pseudoMustHaveArgumentError(index, ':' + name, triggerCp, handler);
				return null;
			} else if (handler.isInsideHas()) {
				// See CSSWG issue 7463
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"For security reasons, pseudo-elements aren't allowed inside a has().");
			}
			SelectorArgumentPEConditionImpl condition = new SelectorArgumentPEConditionImpl();
			condition.setName(name);
			return condition;
		}

	}

	private static void pseudoMustHaveArgumentError(int index, String name, int triggerCp,
			SelectorTokenHandler handler) {
		StringBuilder buf = new StringBuilder(name.length() * 2 + 26);
		buf.append("Expected ':").append(name).append("(', found ':").append(name)
				.appendCodePoint(triggerCp).append('\'');
		handler.handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, buf.toString());
	}

	private static SelectorListImpl parseSelectorArgument(SelectorTokenHandler handler)
			throws CSSParseException {
		String seltext = handler.rawBuffer();
		SelectorArgumentManager manager = handler.new SelectorArgumentManager(handler.factory);
		TokenProducer tp = manager.createTokenProducer();
		tp.parse(seltext);
		return manager.getTrimmedSelectorList();
	}

	private static class HasConditionSetter extends SelectorArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			if (handler.isInsideHas()) {
				handler.handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						":has() pseudo-classes cannot be nested.");
				return null;
			}

			AbstractCondition condition = super.create(index, triggerCp, name, handler);
			handler.hasHas = true;
			return condition;
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			super.setArgument(index, cond, handler);
			handler.hasHas = false;
		}

	}

	private static class MaybeSelectorArgumentConditionSetter
			extends SelectorArgumentConditionSetter {

		@Override
		public AbstractCondition create(int index, int triggerCp, String name,
				SelectorTokenHandler handler) {
			AbstractNamedCondition condition;
			if (triggerCp == TokenProducer.CHAR_LEFT_PAREN) {
				condition = new SelectorArgumentConditionImpl();
			} else {
				condition = new PseudoConditionImpl(ConditionType.PSEUDO_CLASS);
			}
			condition.setName(name);
			return condition;
		}

		@Override
		public void setArgument(int index, Condition cond, SelectorTokenHandler handler) {
			SelectorArgumentConditionImpl argcond = (SelectorArgumentConditionImpl) cond;
			if (handler.buffer.length() != 0) {
				parseSelectorArgument(index, argcond, handler);
			}
		}

	}

}
