/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

public class DefaultStyleDeclarationErrorHandler implements StyleDeclarationErrorHandler {

	/*
	 * Lazily instantiated lists and maps.
	 */
	private List<String> malformedURIs = null;
	private LinkedHashMap<String, String> seShorthands = null;
	private List<String> unassigned = null;
	private List<String> wrongCount = null;
	private List<String> missingReq = null;
	private List<Node> noContainer = null;
	private Map<String, String> unknownIdent = null;
	private Map<String, String> wrongValue = null;
	private LinkedList<String> valueWarnings = null;
	private Map<String, String> unassignedValue = null;
	// SAC Errors
	private List<Short> sacWarnings = null;
	private List<Short> sacErrors = null;
	private List<Short> sacFatalErrors = null;

	@Override
	public void malformedURIValue(String uri) {
		if (malformedURIs == null) {
			malformedURIs = new LinkedList<String>();
		}
		malformedURIs.add(uri);
	}

	@Override
	public void shorthandError(String shorthandName, String message) {
		if (seShorthands == null) {
			seShorthands = new LinkedHashMap<String, String>();
		}
		seShorthands.put(shorthandName, message);
	}

	@Override
	public void shorthandSyntaxError(String shorthandName, String message) {
		shorthandError(shorthandName, message);
	}

	@Override
	public void shorthandWarning(String shorthandName, String valueText) {
		shorthandError(shorthandName, "Unsafe shorthand value: " + valueText);
	}

	@Override
	public void unassignedShorthandValues(String shorthandName, String[] unassignedProperties,
			LexicalUnit[] unassignedValues) {
		if (unassigned == null) {
			unassigned = new LinkedList<String>();
		}
		for (int i = 0; i < unassignedValues.length; i++) {
			LexicalUnit lu = unassignedValues[i];
			unassigned.add(lu.toString());
		}
	}

	@Override
	public void wrongSubpropertyCount(String shorthandName, int count) {
		if (wrongCount == null) {
			wrongCount = new LinkedList<String>();
		}
		wrongCount.add(shorthandName);
	}

	@Override
	public void unknownIdentifier(String propertyName, String ident) {
		if (unknownIdent == null) {
			unknownIdent = new HashMap<String, String>();
		}
		unknownIdent.put(propertyName, ident);
	}

	@Override
	public void missingRequiredProperty(String propertyName) {
		if (missingReq == null) {
			missingReq = new LinkedList<String>();
		}
		missingReq.add(propertyName);
	}

	@Override
	public void wrongValue(String propertyName, CSSPropertyValueException e) {
		if (wrongValue == null) {
			wrongValue = new HashMap<String, String>();
		}
		wrongValue.put(propertyName, e.getValueText());
	}

	@Override
	public void syntaxWarning(String message) {
		if (valueWarnings == null) {
			valueWarnings = new LinkedList<String>();
		}
		valueWarnings.add(message);
	}

	@Override
	public void compatWarning(String propertyName, String cssText) {
		syntaxWarning("IE compat value in property " + propertyName + ": " + cssText);
	}

	/**
	 * Unable to find containing block for <code>containedNode</code>.
	 * 
	 * @param containedNode
	 *            the contained node.
	 * @param ownerNode
	 *            the owner node.
	 */
	@Override
	public void noContainingBlock(String containedNode, Node ownerNode) {
		if (noContainer == null) {
			noContainer = new LinkedList<Node>();
		}
		noContainer.add(ownerNode);
	}

	@Override
	public void unassignedShorthandValue(String shorthandName, String valueCss) {
		if (unassignedValue == null) {
			unassignedValue = new HashMap<String, String>();
		}
		unassignedValue.put(shorthandName, valueCss);
	}

	/**
	 * Has any error been reported?
	 * 
	 * @return <code>true</code> if some error was reported, <code>false</code> otherwise. Unassigned subproperties
	 *         aren't considered an error.
	 */
	@Override
	public boolean hasErrors() {
		return malformedURIs != null || seShorthands != null || wrongCount != null
				|| missingReq != null || unknownIdent != null || wrongValue != null || unassignedValue != null
				|| sacErrors != null || sacFatalErrors != null;
	}

	@Override
	public boolean hasWarnings() {
		return sacWarnings != null || valueWarnings != null;
	}

	public List<String> getMalformedURIs() {
		return malformedURIs;
	}

	public Map<String, String> getShorthandsWithErrors() {
		return seShorthands;
	}

	public List<String> getUnassignedSubproperties() {
		return unassigned;
	}

	public List<String> getWrongSubpropertyCount() {
		return wrongCount;
	}

	public List<String> getMissingRequiredValues() {
		return missingReq;
	}

	public Map<String, String> getUnknownIdentifiers() {
		return unknownIdent;
	}

	public Map<String, String> getWrongValues() {
		return wrongValue;
	}

	public List<Node> getNoContainingBlock() {
		return noContainer;
	}

	public Map<String, String> getUnassignedValues() {
		return unassignedValue;
	}

	public LinkedList<String> getSyntaxWarnings() {
		return valueWarnings;
	}

	@Override
	public void reset() {
		malformedURIs = null;
		seShorthands = null;
		unassigned = null;
		wrongCount = null;
		missingReq = null;
		unknownIdent = null;
		wrongValue = null;
		noContainer = null;
		unassignedValue = null;
		sacWarnings = null;
		sacErrors = null;
		sacFatalErrors = null;
	}

	@Override
	public void sacWarning(CSSParseException exception, int previousIndex) {
		if (sacWarnings == null) {
			sacWarnings = new LinkedList<Short>();
		}
		switch (exception.getCode()) {
		case CSSException.SAC_NOT_SUPPORTED_ERR:
			sacWarnings.add(CSSException.SAC_NOT_SUPPORTED_ERR);
			break;
		case CSSException.SAC_SYNTAX_ERR:
			sacWarnings.add(CSSException.SAC_SYNTAX_ERR);
			break;
		default:
			sacWarnings.add(CSSException.SAC_UNSPECIFIED_ERR);
		}
	}

	@Override
	public void sacError(CSSParseException exception, int previousIndex) {
		if (sacErrors == null) {
			sacErrors = new LinkedList<Short>();
		}
		switch (exception.getCode()) {
		case CSSException.SAC_NOT_SUPPORTED_ERR:
			sacErrors.add(CSSException.SAC_NOT_SUPPORTED_ERR);
			break;
		case CSSException.SAC_SYNTAX_ERR:
			sacErrors.add(CSSException.SAC_SYNTAX_ERR);
			break;
		default:
			sacErrors.add(CSSException.SAC_UNSPECIFIED_ERR);
		}
	}

	@Override
	public void sacFatalError(CSSParseException exception, int previousIndex) {
		if (sacFatalErrors == null) {
			sacFatalErrors = new LinkedList<Short>();
		}
		switch (exception.getCode()) {
		case CSSException.SAC_NOT_SUPPORTED_ERR:
			sacFatalErrors.add(CSSException.SAC_NOT_SUPPORTED_ERR);
			break;
		case CSSException.SAC_SYNTAX_ERR:
			sacFatalErrors.add(CSSException.SAC_SYNTAX_ERR);
			break;
		default:
			sacFatalErrors.add(CSSException.SAC_UNSPECIFIED_ERR);
		}
	}

	public List<Short> getSACWarnings() {
		return sacWarnings;
	}

	public List<Short> getSACErrors() {
		return sacErrors;
	}

	public List<Short> getSACFatalErrors() {
		return sacFatalErrors;
	}

	public void errorSummary(StringBuilder buf) {
		if (malformedURIs != null && !malformedURIs.isEmpty()) {
			buf.append("Malformed URIs:");
			Iterator<String> it = malformedURIs.iterator();
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			buf.append('\n');
		}
		if (seShorthands != null && !seShorthands.isEmpty()) {
			buf.append("Shorthands with syntax error:");
			Iterator<Entry<String, String>> it = seShorthands.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				buf.append(' ').append(entry.getKey()).append(" (").append(entry.getValue()).append(')');
			}
			buf.append('\n');
		}
		if (wrongCount != null && !wrongCount.isEmpty()) {
			buf.append("Wrong subproperty count:");
			Iterator<String> it = wrongCount.iterator();
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			buf.append('\n');
		}
		if (missingReq != null && !missingReq.isEmpty()) {
			buf.append("Missing required value(s) for property:");
			Iterator<String> it = missingReq.iterator();
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			buf.append('\n');
		}
		if (unknownIdent != null && !unknownIdent.isEmpty()) {
			buf.append("Unknown identifiers:");
			Iterator<Entry<String, String>> it = unknownIdent.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> me = it.next();
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (wrongValue != null && !wrongValue.isEmpty()) {
			buf.append("Wrong values:");
			Iterator<Entry<String, String>> it = wrongValue.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> me = it.next();
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (noContainer != null && !noContainer.isEmpty()) {
			buf.append("There were ").append(noContainer.size()).append(" nodes without containing block.\n");
		}
		if (unassignedValue != null) {
			buf.append("Shorthands with unassigned values:\n");
			Iterator<Entry<String, String>> it = unassignedValue.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> me = it.next();
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (sacErrors != null) {
			buf.append("There were ").append(sacErrors.size()).append(" SAC errors.\n");
		}
		if (sacFatalErrors != null) {
			buf.append("There were ").append(sacFatalErrors.size()).append(" SAC fatal errors.\n");
		}
	}

	public void warningSummary(StringBuilder buf) {
		if (sacWarnings != null) {
			buf.append("There were ").append(sacWarnings.size()).append(" SAC warnings.\n");
		}
		if (valueWarnings != null) {
			buf.append("There were ").append(valueWarnings.size()).append(" value warnings:\n");
			Iterator<String> it = valueWarnings.iterator();
			while (it.hasNext()) {
				buf.append("- ").append(it.next()).append('\n');
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = null;
		if (hasErrors()) {
			buf = new StringBuilder(256);
			errorSummary(buf);
		}
		if (hasWarnings()) {
			if (buf == null) {
				buf = new StringBuilder(80);
			}
			warningSummary(buf);
		} else if (buf == null) {
			return "No errors";
		}
		return buf.toString();
	}

}
