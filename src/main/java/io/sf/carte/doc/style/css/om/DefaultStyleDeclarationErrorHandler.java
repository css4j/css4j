/*

 Copyright (c) 2005-2023, Carlos Amengual.

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

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

public class DefaultStyleDeclarationErrorHandler implements StyleDeclarationErrorHandler, java.io.Serializable {

	private static final long serialVersionUID = 1L;

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
	// NSAC Errors
	private List<CSSParseException> sacWarnings = null;
	private List<CSSParseException> sacErrors = null;

	@Override
	public void malformedURIValue(String uri) {
		if (malformedURIs == null) {
			malformedURIs = new LinkedList<>();
		}
		malformedURIs.add(uri);
	}

	@Override
	public void shorthandError(String shorthandName, String message) {
		if (seShorthands == null) {
			seShorthands = new LinkedHashMap<>();
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
			unassigned = new LinkedList<>();
		}
		for (LexicalUnit lu : unassignedValues) {
			unassigned.add(lu.toString());
		}
	}

	@Override
	public void wrongSubpropertyCount(String shorthandName, int count) {
		if (wrongCount == null) {
			wrongCount = new LinkedList<>();
		}
		wrongCount.add(shorthandName);
	}

	@Override
	public void unknownIdentifier(String propertyName, String ident) {
		if (unknownIdent == null) {
			unknownIdent = new HashMap<>();
		}
		unknownIdent.put(propertyName, ident);
	}

	@Override
	public void missingRequiredProperty(String propertyName) {
		if (missingReq == null) {
			missingReq = new LinkedList<>();
		}
		missingReq.add(propertyName);
	}

	@Override
	public void wrongValue(String propertyName, CSSPropertyValueException e) {
		if (wrongValue == null) {
			wrongValue = new HashMap<>();
		}
		wrongValue.put(propertyName, e.getValueText());
	}

	@Override
	public void syntaxWarning(String message) {
		if (valueWarnings == null) {
			valueWarnings = new LinkedList<>();
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
			noContainer = new LinkedList<>();
		}
		noContainer.add(ownerNode);
	}

	@Override
	public void unassignedShorthandValue(String shorthandName, String valueCss) {
		if (unassignedValue == null) {
			unassignedValue = new HashMap<>();
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
		return malformedURIs != null || seShorthands != null || wrongCount != null || missingReq != null
				|| unknownIdent != null || wrongValue != null || unassignedValue != null || sacErrors != null;
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
	}

	@Override
	public void sacWarning(CSSParseException exception, int previousIndex) {
		if (sacWarnings == null) {
			sacWarnings = new LinkedList<>();
		}
		sacWarnings.add(exception);
	}

	@Override
	public void sacError(CSSParseException exception, int previousIndex) {
		if (sacErrors == null) {
			sacErrors = new LinkedList<>();
		}
		sacErrors.add(exception);
	}

	public List<CSSParseException> getSACWarnings() {
		return sacWarnings;
	}

	public List<CSSParseException> getSACErrors() {
		return sacErrors;
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
			for (Entry<String, String> entry : seShorthands.entrySet()) {
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
			for (Entry<String, String> me : unknownIdent.entrySet()) {
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (wrongValue != null && !wrongValue.isEmpty()) {
			buf.append("Wrong values:");
			for (Entry<String, String> me : wrongValue.entrySet()) {
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (noContainer != null && !noContainer.isEmpty()) {
			buf.append("There were ").append(noContainer.size()).append(" nodes without containing block.\n");
		}
		if (unassignedValue != null) {
			buf.append("Shorthands with unassigned values:\n");
			for (Entry<String, String> me : unassignedValue.entrySet()) {
				buf.append(' ').append(me.getKey()).append(':').append(' ').append(me.getValue());
			}
			buf.append('\n');
		}
		if (sacErrors != null) {
			buf.append("There were ").append(sacErrors.size()).append(" NSAC errors.\n");
		}
	}

	public void warningSummary(StringBuilder buf) {
		if (sacWarnings != null) {
			buf.append("There were ").append(sacWarnings.size()).append(" NSAC warnings.\n");
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
