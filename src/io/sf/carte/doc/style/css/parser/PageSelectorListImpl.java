/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.PageSelector;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;

class PageSelectorListImpl extends AbstractSACList<PageSelector> implements PageSelectorList {

	private static final long serialVersionUID = 1L;

	PageSelectorListImpl() {
		super(6);
	}

}
