/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.StringList;

class DefaultCommentStore implements CommentStore {

	private final LexicalProvider parent;

	StringList precedingComments = null;

	StringList trailingComments = null;

	public DefaultCommentStore(LexicalProvider parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void addPrecedingComment(String comment) {
		if (precedingComments == null) {
			precedingComments = new LinkedStringList();
		}
		precedingComments.add(comment);
	}

	@Override
	public void setPrecedingComments(LexicalUnitImpl lu) {
		if (precedingComments != null) {
			LexicalUnitImpl currentlu;
			if (!lu.addPrecedingComments(precedingComments)
					&& (currentlu = parent.getCurrentLexicalUnit()) != null) {
				LexicalUnitImpl plu = lu.previousLexicalUnit;
				if (plu == null) {
					if (parent.isFunctionOrExpressionContext()) {
						plu = currentlu.parameters;
						if (plu != null) {
							plu = CSSParser.findLastValue(plu);
						} else {
							// Unlikely case that first parameter is an operator
							precedingComments = null;
							return;
						}
					} else {
						plu = currentlu;
					}
				}
				// Add comments to previous unit
				plu.addTrailingComments(precedingComments);
			}
			precedingComments = null;
		}
	}

	@Override
	public void addTrailingComment(String comment) {
		if (trailingComments == null) {
			trailingComments = new LinkedStringList();
		}
		trailingComments.add(comment);
	}

	@Override
	public void setTrailingComments() {
		LexicalUnitImpl currentlu = parent.getCurrentLexicalUnit();
		if (currentlu != null) {
			LexicalUnitImpl lu = currentlu;
			if (parent.isFunctionOrExpressionContext()) {
				if (lu.parameters != null) {
					lu = CSSParser.findLastValue(lu.parameters);
				} else {
					return;
				}
			}
			setTrailingComments(lu);
		}
	}

	@Override
	public void setTrailingComments(LexicalUnitImpl lu) {
		if (trailingComments != null) {
			if (precedingComments != null) {
				trailingComments.addAll(precedingComments);
				precedingComments = null;
			}
			if (!lu.addTrailingComments(trailingComments)) {
				// Preceding comments for the next unit
				if (precedingComments != null) {
					precedingComments.addAll(trailingComments);
				} else {
					precedingComments = trailingComments;
				}
			}
			trailingComments = null;
		} else if (precedingComments != null) {
			lu.addTrailingComments(precedingComments);
			precedingComments = null;
		}
	}

	@Override
	public void setLastParameterTrailingComments(LexicalUnitImpl param) {
		if (trailingComments != null) {
			if (precedingComments != null) {
				trailingComments.addAll(precedingComments);
				precedingComments = null;
			}
			LexicalUnitImpl lu = CSSParser.findLastValue(param);
			if (!lu.addTrailingComments(trailingComments)) {
				// Preceding comments for the next unit
				if (precedingComments != null) {
					precedingComments.addAll(trailingComments);
				} else {
					precedingComments = trailingComments;
				}
			}
			trailingComments = null;
		} else if (precedingComments != null) {
			LexicalUnitImpl lu = CSSParser.findLastValue(param);
			lu.addTrailingComments(precedingComments);
			precedingComments = null;
		}
	}

	@Override
	public boolean haveTrailingComments() {
		if (trailingComments == null) {
			LexicalUnitImpl lu = parent.getCurrentLexicalUnit();
			if (lu == null) {
				return false;
			}
			if (parent.isFunctionOrExpressionContext()) {
				if (lu.parameters != null) {
					lu = CSSParser.findLastValue(lu.parameters);
				}
			}
			return lu.getTrailingComments() != null;
		}
		return true;
	}

	@Override
	public StringList getPrecedingCommentsAndClear() {
		StringList ret = precedingComments;
		precedingComments = null;
		return ret;
	}

	@Override
	public StringList getTrailingCommentsAndClear() {
		StringList ret = trailingComments;
		trailingComments = null;
		return ret;
	}

	@Override
	public void set(LexicalProvider caller) {
		precedingComments = caller.getPrecedingCommentsAndClear();
		trailingComments = caller.getTrailingCommentsAndClear();
	}

	@Override
	public void resetTrailingComments() {
		trailingComments = null;
	}

	@Override
	public void reset() {
		precedingComments = null;
		trailingComments = null;
	}

}
