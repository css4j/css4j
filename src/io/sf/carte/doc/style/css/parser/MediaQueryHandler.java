/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import org.w3c.css.sac.CSSParseException;

public interface MediaQueryHandler {

	void startQuery();

	void mediaType(String mediaType);

	void negativeQuery();

	void onlyPrefix();

	void condition(BooleanCondition condition);

	void endQuery();

	void invalidQuery(CSSParseException queryError);

}
