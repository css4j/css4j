/*
 *
 * Copyright (c) 2005-2020, Carlos Amengual.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Licensed under a BSD-style License. You can find the license here:
 * https://css4j.github.io/LICENSE.txt
 *
 */
module io.sf.carte.css4j {
	exports io.sf.carte.doc.style.css;
	exports io.sf.carte.doc.style.css.parser;
	exports io.sf.carte.doc.style.css.property;
	exports io.sf.carte.doc;
	exports io.sf.carte.doc.dom;
	exports io.sf.carte.doc.style.css.om;
	exports io.sf.carte.doc.agent;
	exports io.sf.carte.doc.style.css.nsac;

	requires io.sf.carte.tokenproducer;
	requires transitive io.sf.carte.util;
	requires static io.sf.carte.xml.dtd;
	requires transitive java.xml;
	requires transitive jdk.xml.dom;
	requires io.sf.jclf;
	requires static org.slf4j;
}
