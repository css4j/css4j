/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.StringList;

abstract class LexicalCallbackTH extends CallbackTokenHandler implements LexicalProvider {

	LexicalUnitImpl currentlu;

	private StringList precedingComments = null;

	private StringList trailingComments = null;

	LexicalCallbackTH(LexicalProvider caller) {
		super(caller);
		currentlu = caller.getCurrentLexicalUnit();
		precedingComments = caller.getPrecedingCommentsAndClear();
		trailingComments = caller.getTrailingCommentsAndClear();
	}

	@Override
	protected LexicalProvider getCaller() {
		return (LexicalProvider) caller;
	}

	@Override
	public void endFunctionArgument(int index) {
		if (currentlu != null) {
			setTrailingComments();
			if (currentlu.ownerLexicalUnit != null) {
				currentlu = currentlu.ownerLexicalUnit;
			}
		} else {
			getCaller().endFunctionArgument(index);
		}
	}

	/**
	 * Add a non-function (nor expression) lexical unit as the current value.
	 * 
	 * @param lu the lexical unit to add.
	 * @return the lexical unit that should be processed as the current unit.
	 */
	@Override
	public LexicalUnitImpl addPlainLexicalUnit(LexicalUnitImpl lu) {
		setPrecedingComments(lu);
		if (isCurrentUnitAFunction()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				setTrailingComments(currentlu);
			}
			currentlu = lu;
		}
		return lu;
	}

	LexicalUnitImpl addFunctionOrExpressionUnit(LexicalUnitImpl lu) {
		setPrecedingComments(lu);
		if (isCurrentUnitAFunction()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				setTrailingComments(currentlu);
				// Set preceding comments, just in case there was e.g. a comma
				setPrecedingComments(lu);
			}
		}
		currentlu = lu;
		return lu;
	}

	@Override
	public LexicalUnitImpl getCurrentLexicalUnit() {
		return currentlu;
	}

	@Override
	public void setCurrentLexicalUnit(LexicalUnitImpl currentlu) {
		addPlainLexicalUnit(currentlu);
	}

	@Override
	public boolean isCurrentUnitAFunction() {
		return getCaller().isCurrentUnitAFunction();
	}

	@Override
	public void addEmptyLexicalUnit() {
		EmptyUnitImpl empty = new EmptyUnitImpl();
		addPlainLexicalUnit(empty);
	}

	@Override
	void yieldBack() {
		// Set the result
		LexicalProvider c = getCaller();
		c.setCurrentLexicalUnit(getCurrentLexicalUnit());

		super.yieldBack();
	}

	@Override
	public boolean hasLegacySupport() {
		return false;
	}

	/*
	 * Comment management
	 */

	@Override
	public void commented(int index, int commentType, String comment) {
		if (buffer.length() != 0) {
			processBuffer(index, 12);
			if (commentType == 0) {
				addTrailingComment(comment);
				setTrailingComments();
			}
		} else if (commentType == 0) {
			if (!isPrevCpWhitespace() && (prevcp != 12 || haveTrailingComments())) {
				addTrailingComment(comment);
			} else {
				addPrecedingComment(comment);
				trailingComments = null;
			}
		}
		prevcp = 12;
	}

	private void addPrecedingComment(String comment) {
		if (precedingComments == null) {
			precedingComments = new LinkedStringList();
		}
		precedingComments.add(comment);
	}

	void setPrecedingComments(LexicalUnitImpl lu) {
		if (precedingComments != null) {
			if (!lu.addPrecedingComments(precedingComments) && currentlu != null) {
				LexicalUnitImpl plu = lu.previousLexicalUnit;
				if (plu == null) {
					if (isCurrentUnitAFunction()) {
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

	private void addTrailingComment(String comment) {
		if (trailingComments == null) {
			trailingComments = new LinkedStringList();
		}
		trailingComments.add(comment);
	}

	private void setTrailingComments() {
		if (currentlu != null) {
			LexicalUnitImpl lu = currentlu;
			if (isCurrentUnitAFunction()) {
				if (lu.parameters != null) {
					lu = CSSParser.findLastValue(lu.parameters);
				} else {
					return;
				}
			}
			setTrailingComments(lu);
		}
	}

	private void setTrailingComments(LexicalUnitImpl lu) {
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

	private void setLastParameterTrailingComments(LexicalUnitImpl param) {
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

	private boolean haveTrailingComments() {
		if (trailingComments == null) {
			LexicalUnitImpl lu = currentlu;
			if (lu == null) {
				return false;
			}
			if (isCurrentUnitAFunction()) {
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

}
