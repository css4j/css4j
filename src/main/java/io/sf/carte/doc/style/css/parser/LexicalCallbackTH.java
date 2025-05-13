/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.Flag;

abstract class LexicalCallbackTH extends CallbackTokenHandler implements LexicalProvider {

	LexicalUnitImpl currentlu;

	private final CommentStore commentStore;

	LexicalCallbackTH(LexicalProvider caller) {
		super(caller);
		currentlu = caller.getCurrentLexicalUnit();
		commentStore = createCommentStore();
		commentStore.set(caller);
	}

	protected CommentStore createCommentStore() {
		if (hasParserFlag(Flag.VALUE_COMMENTS_IGNORE)) {
			return new EmptyCommentStore();
		} else {
			return new DefaultCommentStore(this);
		}
	}

	@Override
	public boolean hasParserFlag(Parser.Flag flag) {
		return getCaller().hasParserFlag(flag);
	}

	@Override
	protected LexicalProvider getCaller() {
		return (LexicalProvider) caller;
	}

	@Override
	public void endFunctionArgument(int index) {
		if (currentlu != null) {
			commentStore.setTrailingComments();
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
		commentStore.setPrecedingComments(lu);
		if (isFunctionOrExpressionContext()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				commentStore.setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				commentStore.setTrailingComments(currentlu);
			}
			currentlu = lu;
		}
		return lu;
	}

	LexicalUnitImpl addFunctionOrExpressionUnit(LexicalUnitImpl lu) {
		commentStore.setPrecedingComments(lu);
		if (isFunctionOrExpressionContext()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				commentStore.setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				commentStore.setTrailingComments(currentlu);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
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
	public boolean isFunctionOrExpressionContext() {
		return getCaller().isFunctionOrExpressionContext();
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

	/*
	 * Comment management
	 */

	@Override
	public void commented(int index, int commentType, String comment) {
		if (buffer.length() != 0) {
			processBuffer(index, 12);
			if (commentType == 0) {
				commentStore.addTrailingComment(comment);
				commentStore.setTrailingComments();
			}
		} else if (commentType == 0) {
			if (!isPrevCpWhitespace() && (prevcp != 12 || commentStore.haveTrailingComments())) {
				commentStore.addTrailingComment(comment);
			} else {
				commentStore.addPrecedingComment(comment);
				commentStore.resetTrailingComments();
			}
		}
		prevcp = 12;
	}

	CommentStore getCommentStore() {
		return commentStore;
	}

	@Override
	public StringList getPrecedingCommentsAndClear() {
		return commentStore.getPrecedingCommentsAndClear();
	}

	@Override
	public StringList getTrailingCommentsAndClear() {
		return commentStore.getTrailingCommentsAndClear();
	}

}
