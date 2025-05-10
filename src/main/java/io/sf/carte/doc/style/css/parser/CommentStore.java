/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.StringList;

interface CommentStore {

	void addPrecedingComment(String comment);

	void setPrecedingComments(LexicalUnitImpl lu);

	void addTrailingComment(String comment);

	void setTrailingComments();

	void setTrailingComments(LexicalUnitImpl lu);

	void setLastParameterTrailingComments(LexicalUnitImpl param);

	boolean haveTrailingComments();

	StringList getPrecedingCommentsAndClear();

	StringList getTrailingCommentsAndClear();

	void set(LexicalProvider caller);

	void resetTrailingComments();

	void reset();

}
