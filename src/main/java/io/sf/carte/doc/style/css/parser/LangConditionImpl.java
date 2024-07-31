/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.LangCondition;
import io.sf.jclf.text.TokenParser;

class LangConditionImpl implements LangCondition, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	String lang = null;

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.LANG;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
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
		LangConditionImpl other = (LangConditionImpl) obj;
		if (lang == null) {
			if (other.lang != null) {
				return false;
			}
		} else if (!lang.equals(other.lang)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(":lang(");
		String lang = getLang();
		TokenParser parser = new TokenParser(lang, ", ", "\"'");
		String s = parser.next();
		int commaIdx = lang.indexOf(',') + 1;
		buf.append(escapeLang(s, lang, commaIdx));
		while (parser.hasNext()) {
			s = parser.next();
			commaIdx = lang.indexOf(',', commaIdx) + 1;
			buf.append(',').append(escapeLang(s, lang, commaIdx));
		}
		buf.append(')');
		return buf.toString();
	}

	private String escapeLang(String s, String lang, int commaIdx) {
		int nextCommaIdx = lang.indexOf(',', commaIdx) + 1;
		int nextDQIdx = lang.indexOf('"', commaIdx);
		int nextSQIdx = lang.indexOf('\'', commaIdx);
		boolean noDQ = nextDQIdx == -1 || nextDQIdx > nextCommaIdx;
		CharSequence escaped;
		if (s.indexOf(' ') != -1) {
			char quote = noDQ ? '\'' : '"';
			s = ParseHelper.quote(s, quote);
		} else if ((escaped = ParseHelper.escapeCssCharsAndFirstChar(s)) != s) {
			boolean noSQ = nextSQIdx == -1 || nextSQIdx > nextCommaIdx;
			if (escaped.length() < s.length() + 2 && noDQ && noSQ) {
				s = escaped.toString();
			} else {
				char quote = noDQ ? '\'' : '"';
				s = ParseHelper.quote(s, quote);
			}
		}
		return s;
	}

}
