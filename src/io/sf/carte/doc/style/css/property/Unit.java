/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSUnit;

/**
 * CSS unit.
 * <p>
 * The result of a CSS function or expression may not be expressed in one of the
 * standard CSS units, even though the final result of the calculation may. That
 * is the reason why this class exists.
 * <p>
 * For version 2.0, this shall be integrated with a new Value API.
 */
class Unit {

	private short unitType;

	private int exponent;

	private Unit nextUnit = null;

	public Unit() {
		super();
		unitType = CSSUnit.CSS_NUMBER;
		exponent = 0;
	}

	Unit(short unitType) {
		super();
		setUnitType(unitType);
	}

	/**
	 * Get the exponent that applies to this unit.
	 * <p>
	 * Normally, the exponent is <code>1</code>. If the exponent is <code>0</code>,
	 * the value is dimensionless.
	 * <p>
	 * For example, if the exponent is <code>2</code> and the unit is
	 * <code>mm</code>, then the unit is square millimeters.
	 * 
	 * @return the exponent that applies to this unit.
	 */
	public int getExponent() {
		return exponent;
	}

	void setExponent(int exponent) {
		this.exponent = exponent;
	}

	void incrExponent() {
		this.exponent++;
	}

	void decrExponent() {
		this.exponent--;
	}

	/**
	 * Get the primitive unit type that this unit is applying to.
	 * 
	 * @return the associated primitive unit type.
	 */
	public short getUnitType() {
		return unitType;
	}

	void setUnitType(short unitType) {
		this.unitType = unitType;
		if (unitType == CSSUnit.CSS_NUMBER) {
			exponent = 0;
		} else {
			exponent = 1;
		}
	}

	/**
	 * Get the next unit.
	 * <p>
	 * This method allows to chain several unit objects to represent compound units.
	 * Multiplication of chained units is implied.
	 * <p>
	 * For example, if the first Unit object represents millimeters
	 * (<code>CSS_MM</code>) with exponent <code>1</code> and the next one has
	 * seconds of time (<code>CSS_S</code>) with exponent <code>-1</code>, that
	 * represents millimeters per second.
	 * 
	 * @return the next unit.
	 */
	public Unit getNextUnit() {
		return nextUnit;
	}

	void setNextUnit(Unit nextUnit) {
		this.nextUnit = nextUnit;
	}

	/**
	 * Convert a value from this unit to a destination unit.
	 * 
	 * @param value               the value to convert.
	 * @param destinationUnitType the destination unit type.
	 * @return the converted value.
	 */
	public float convert(float value, short destinationUnitType) {
		if (exponent != 0 && unitType != destinationUnitType) {
			double factor = NumberValue.floatValueConversion(1f, unitType, destinationUnitType);
			if (exponent > 1) {
				for (int i = 2; i <= exponent; i++) {
					factor *= factor;
				}
			} else if (exponent < 0) {
				factor = 1d / factor;
				for (int i = -2; i >= exponent; i--) {
					factor *= factor;
				}
			}
			return (float) (value * factor);
		}
		return value;
	}

}
