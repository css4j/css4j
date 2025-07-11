/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.util.Visitor;

/**
 * Subclasses can be used to visit a selector (or a selector list) and replace
 * the namespaceURI, localName or value of the contained
 * {@code AttributeCondition}s.
 */
abstract public class AttributeConditionVisitor extends ConditionVisitor
		implements Visitor<AttributeCondition> {

	protected AttributeConditionVisitor() {
		super();
	}

	/**
	 * Visit a condition.
	 * 
	 * @param condition the condition.
	 */
	@Override
	protected void visit(Condition condition) {
		if (condition instanceof AttributeCondition) {
			visit((AttributeCondition) condition);
		} else {
			switch (condition.getConditionType()) {
			case AND:
				CombinatorCondition comb = (CombinatorCondition) condition;
				int len = comb.getLength();
				for (int i = 0; i < len; i++) {
					visit(comb.getCondition(i));
				}
				break;
			case POSITIONAL:
				SelectorList selist = ((PositionalCondition) condition).getOfList();
				if (selist != null) {
					visit(selist);
				}
				break;
			case SELECTOR_ARGUMENT:
				selist = ((ArgumentCondition) condition).getSelectors();
				if (selist != null) {
					visit(selist);
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Sets the namespaceURI of the attribute condition.
	 * 
	 * @param cond         the attribute condition to be set.
	 * @param namespaceURI the new namespaceURI.
	 * @throws DOMException INVALID_ACCESS_ERR if the namespaceURI is {@code null}
	 *                      or empty.
	 */
	protected void setConditionNamespaceURI(AttributeCondition cond, String namespaceURI)
			throws DOMException {
		if (namespaceURI == null) {
			throw new DOMInvalidAccessException("Null namespaceURI.");
		}
		namespaceURI = namespaceURI.trim();
		if (namespaceURI.length() == 0) {
			throw new DOMInvalidAccessException("Empty namespaceURI.");
		}
		((NSACSelectorFactory.AttributeConditionImpl) cond).setNamespaceURI(namespaceURI);
	}

	/**
	 * Sets the LocalName of the attribute condition.
	 * 
	 * @param cond         the attribute condition to be set.
	 * @param newLocalName the new LocalName.
	 * @throws DOMException INVALID_ACCESS_ERR if the newLocalName is {@code null}
	 *                      or empty.
	 */
	protected void setConditionLocalName(AttributeCondition cond, String newLocalName)
			throws DOMException {
		if (newLocalName == null) {
			throw new DOMInvalidAccessException("Null local name.");
		}
		newLocalName = newLocalName.trim();
		if (newLocalName.length() == 0) {
			throw new DOMInvalidAccessException("Empty local name.");
		}
		((NSACSelectorFactory.AttributeConditionImpl) cond).setLocalName(newLocalName);
	}

	/**
	 * Sets the value of the attribute condition.
	 * 
	 * @param cond     the attribute condition to be set.
	 * @param newValue the new value.
	 * @throws DOMException INVALID_ACCESS_ERR if the {@code ConditionType} is
	 *                      {@code CLASS} and the value is {@code null} or empty.
	 */
	protected void setConditionValue(AttributeCondition cond, String newValue) throws DOMException {
		if (newValue != null) {
			newValue = newValue.trim();
			if (newValue.length() == 0) {
				newValue = null;
				if (cond.getConditionType() == Condition.ConditionType.CLASS) {
					throw new DOMInvalidAccessException("Empty value.");
				}
			}
		} else if (cond.getConditionType() == Condition.ConditionType.CLASS) {
			throw new DOMInvalidAccessException("Null value.");
		}
		((NSACSelectorFactory.AttributeConditionImpl) cond).setValue(newValue);
	}

	/**
	 * Visit an attribute condition, which can be used to modify its namespaceURI,
	 * localName or value.
	 * 
	 * @param condition the attribute condition.
	 */
	@Override
	abstract public void visit(AttributeCondition condition);

}
