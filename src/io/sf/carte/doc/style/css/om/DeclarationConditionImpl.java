package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.DeclarationCondition;
import io.sf.carte.doc.style.css.ExtendedCSSValue;

class DeclarationConditionImpl extends BooleanCondition.Predicate implements DeclarationCondition {

	private ExtendedCSSValue value = null;
	private String cssText = null;

	public DeclarationConditionImpl(String propertyName) {
		super(propertyName);
	}

	@Override
	public ExtendedCSSValue getValue() {
		return value;
	}

	/**
	 * Set the condition feature value.
	 * 
	 * @param value the value.
	 * @throws DOMException if the value is incompatible with the feature being
	 *                      tested with the condition.
	 */
	@Override
	public void setValue(ExtendedCSSValue value) throws DOMException {
		this.value = value;
	}

	/**
	 * Set a serialized value for the property.
	 * <p>
	 * This should be done only when a proper value could not be parsed.
	 * <p>
	 * A condition which has a serialized value but not a real value is never going
	 * to match, although the serialized value shall be used for serializations.
	 * 
	 * @param cssText the serialized value.
	 */
	@Override
	public void setValue(String cssText) {
		this.cssText  = cssText;
	}

	@Override
	public boolean isParsable() {
		return this.cssText == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getName().hashCode();
		result = prime * result
				+ ((value == null) ? ((cssText == null) ? 0 : cssText.hashCode()) : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DeclarationConditionImpl other = (DeclarationConditionImpl) obj;
		if (!getName().equals(other.getName())) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			} else if (cssText == null) {
				if (other.cssText != null) {
					return false;
				}
			} else if (!cssText.equals(other.cssText)) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public void appendText(StringBuilder buf) {
		buf.append('(').append(getName()).append(": ");
		if (value != null) {
			buf.append(value.getCssText());
		} else if (cssText != null) {
			buf.append(cssText);
		}
		buf.append(')');
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		buf.append('(').append(getName()).append(':');
		if (value != null) {
			buf.append(value.getMinifiedCssText(getName()));
		} else if (cssText != null) {
			buf.append(cssText);
		}
		buf.append(')');
	}

}
