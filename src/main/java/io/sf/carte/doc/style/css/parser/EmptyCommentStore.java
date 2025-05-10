/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.StringList;

class EmptyCommentStore implements CommentStore {

	public EmptyCommentStore() {
		super();
	}

	@Override
	public void addPrecedingComment(String comment) {
	}

	@Override
	public void setPrecedingComments(LexicalUnitImpl lu) {
	}

	@Override
	public void addTrailingComment(String comment) {
	}

	@Override
	public void setTrailingComments() {
	}

	@Override
	public void setTrailingComments(LexicalUnitImpl lu) {
	}

	@Override
	public void setLastParameterTrailingComments(LexicalUnitImpl param) {
	}

	@Override
	public boolean haveTrailingComments() {
		return false;
	}

	@Override
	public StringList getPrecedingCommentsAndClear() {
		return null;
	}

	@Override
	public StringList getTrailingCommentsAndClear() {
		return null;
	}

	@Override
	public void set(LexicalProvider caller) {
		caller.getPrecedingCommentsAndClear();
		caller.getTrailingCommentsAndClear();
	}

	@Override
	public void resetTrailingComments() {
	}

	@Override
	public void reset() {
	}

}
