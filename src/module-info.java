/*
 *
 * Copyright (c) 2005-2022, Carlos Amengual.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Licensed under a BSD-style License. You can find the license here:
 * https://css4j.github.io/LICENSE.txt
 *
 */

/**
 * CSS parser with Event and Object Model APIs, a DOM wrapper and a CSS-aware
 * DOM implementation.
 */
module io.sf.carte.css4j {
	exports io.sf.carte.doc;
	exports io.sf.carte.doc.agent;
	exports io.sf.carte.doc.dom;
	exports io.sf.carte.doc.geom;
	exports io.sf.carte.doc.style.css;
	exports io.sf.carte.doc.style.css.nsac;
	exports io.sf.carte.doc.style.css.om;
	exports io.sf.carte.doc.style.css.parser;
	exports io.sf.carte.doc.style.css.property;
	exports io.sf.carte.doc.style.css.util;

	requires transitive io.sf.carte.util;
	requires transitive java.xml;
	requires transitive jdk.xml.dom;
	requires io.sf.carte.tokenproducer;
	requires io.sf.jclf.text;
	requires io.sf.jclf.math.linear3;
	requires static io.sf.carte.xml.dtd;
	requires static org.slf4j;
}
