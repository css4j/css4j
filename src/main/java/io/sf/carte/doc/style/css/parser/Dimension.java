/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

/**
 * A numeric dimension.
 */
class Dimension {

	Category category;

	int exponent = 0;

	private Dimension prevDimension = null;

	private Dimension nextDimension = null;

	/**
	 * Whether &lt;length&gt; was explicitly processed.
	 * <p>
	 * Should be ignored if dimension isn't &lt;length-percentage&gt;.
	 * </p>
	 */
	transient boolean lengthProcessed;

	/**
	 * Whether &lt;percentage&gt; was explicitly processed.
	 * <p>
	 * Should be ignored if dimension isn't &lt;length-percentage&gt;.
	 * </p>
	 */
	transient boolean percentageProcessed;

	/**
	 * The dimension exponent may not be known accurately.
	 * <ul>
	 * <li>0 - Known accurately.</li>
	 * <li>1 - Known approximately, numeric errors are possible.</li>
	 * <li>2 - Pending substitution.</li>
	 * </ul>
	 */
	transient short exponentAccuracy;

	/**
	 * 
	 * @return the first dimension in the chain.
	 */
	public Dimension getFirst() {
		Dimension lastNonNull = this;
		Dimension next = prevDimension;
		while (next != null) {
			lastNonNull = next;
			next = next.prevDimension;
		}
		return lastNonNull;
	}

	/**
	 * @return the next dimension in the chain.
	 */
	public Dimension getNext() {
		return nextDimension;
	}

	/**
	 * {@code length} was processed after type became {@code lengthPercentage}.
	 * 
	 * @return {@code true} if a {@code length was processed} during and operation.
	 */
	public boolean isLengthProcessed() {
		return lengthProcessed;
	}

	/**
	 * {@code percentage} was processed after type became {@code lengthPercentage}.
	 * 
	 * @return {@code true} if a {@code percentage was processed} during and
	 *         operation.
	 */
	public boolean isPercentageProcessed() {
		return percentageProcessed;
	}

	public Dimension incrExponent() {
		exponent++;
		if (exponent == 0) {
			return remove();
		}
		return getFirst();
	}

	public Dimension decrExponent() {
		exponent--;
		if (exponent == 0) {
			return remove();
		}
		return getFirst();
	}

	/**
	 * Sum the given dimension.
	 * 
	 * @param otherdim the dimension to sum.
	 * @return {@code true} if the operation was successful.
	 */
	public boolean sum(Dimension otherdim) {
		Dimension dim = this;
		do {

			if (!dim.sumDimension(otherdim)) {
				return false;
			}

			dim = dim.getNext();
			otherdim = otherdim.getNext();

			if (dim == null) {
				if (otherdim != null) {
					return false;
				}
				break;
			} else if (otherdim == null) {
				return false;
			}

		} while (true);

		return true;
	}

	/**
	 * Sum the given dimension.
	 * 
	 * @param newdim the dimension to sum.
	 * @return {@code true} if the operation was successful.
	 */
	public boolean sumDimension(Dimension newdim) {
		if (category == newdim.category) {
			if (category == Category.lengthPercentage) {
				lengthProcessed = lengthProcessed || newdim.lengthProcessed;
				percentageProcessed = percentageProcessed || newdim.percentageProcessed;
			}
			return checkExponent(newdim);
		}

		switch (category) {
		case length:
			if (newdim.category == Category.lengthPercentage) {
				category = Category.lengthPercentage;
				percentageProcessed = percentageProcessed || newdim.percentageProcessed;
				break;
			}
			if (newdim.category == Category.percentage) {
				category = Category.lengthPercentage;
				percentageProcessed = true;
				break;
			}
			return false;
		case percentage:
			if (newdim.category == Category.lengthPercentage) {
				category = Category.lengthPercentage;
				lengthProcessed = lengthProcessed || newdim.lengthProcessed;
				break;
			}
			if (newdim.category == Category.length) {
				category = Category.lengthPercentage;
				lengthProcessed = true;
				break;
			}
			return false;
		case lengthPercentage:
			if (newdim.category == Category.length) {
				lengthProcessed = true;
				break;
			}
			if (newdim.category == Category.percentage) {
				percentageProcessed = true;
				break;
			}
			return false;
		case number:
			if (newdim.category == Category.integer) {
				break;
			}
			return false;
		case integer:
			if (newdim.category == Category.number) {
				category = Category.number;
				break;
			}
		default:
			return false;
		}

		return checkExponent(newdim);
	}

	private boolean checkExponent(Dimension newdim) {
		if (exponent != newdim.exponent) {
			if (exponentAccuracy == 0) {
				if (newdim.exponentAccuracy == 0) {
					return false;
				}
			} else if (newdim.exponentAccuracy == 0) {
				exponent = newdim.exponent;
			}
		}

		return true;
	}

	/**
	 * Multiply the given dimensions.
	 * 
	 * @param otherdim the dimension to multiply by.
	 * @return the resulting dimension.
	 */
	public Dimension multiply(Dimension otherdim) {
		topLoop: do {
			Dimension dim = this;

			do {
				if (dim.multiplyDimension(otherdim)) {
					// Success
					if (exponent == 0) {
						return remove();
					}
					continue topLoop;
				}
			} while ((dim = dim.getNext()) != null);

			append(otherdim);
		} while ((otherdim = otherdim.getNext()) != null);
		return this;
	}

	/**
	 * Multiply the given dimensions.
	 * 
	 * @param otherdim the dimension to multiply.
	 * @return {@code true} if the operation was successful.
	 */
	private boolean multiplyDimension(Dimension otherdim) {
		if (category == otherdim.category) {
			exponent += otherdim.exponent;
			return true;
		}

		if (otherdim.category == Category.number || otherdim.category == Category.integer) {
			return true;
		}

		Dimension dim = this;
		switch (dim.category) {
		case number:
		case integer:
			category = otherdim.category;
			exponent = otherdim.exponent;
			break;
		case length:
			if (otherdim.category == Category.lengthPercentage || otherdim.category == Category.percentage) {
				dim.category = Category.lengthPercentage;
				dim.exponent += otherdim.exponent;
				break;
			}
			return false;
		case percentage:
			if (otherdim.category == Category.lengthPercentage || otherdim.category == Category.length) {
				category = Category.lengthPercentage;
				exponent += otherdim.exponent;
				break;
			}
			return false;
		case lengthPercentage:
			if (otherdim.category == Category.length) {
				exponent += otherdim.exponent;
				lengthProcessed = true;
				break;
			}
			if (otherdim.category == Category.percentage) {
				exponent += otherdim.exponent;
				percentageProcessed = true;
				break;
			}
			return false;
		case frequency:
			if (otherdim.category == Category.time) {
				exponent -= otherdim.exponent;
				break;
			}
			return false;
		case time:
			if (otherdim.category == Category.frequency) {
				exponent -= otherdim.exponent;
				break;
			}
		default:
			return false;
		}

		return true;
	}

	/**
	 * Divide the given dimensions.
	 * 
	 * @param otherdim the dimension to divide by.
	 * @return the resulting dimension.
	 */
	public Dimension divide(Dimension otherdim) {
		topLoop: do {
			Dimension dim = this;

			do {
				if (dim.divideDimension(otherdim)) {
					// Success
					if (exponent == 0) {
						return remove();
					}
					continue topLoop;
				}
			} while ((dim = dim.getNext()) != null);

			append(otherdim);
		} while ((otherdim = otherdim.getNext()) != null);
		return this;
	}

	/**
	 * Divide the given dimensions.
	 * 
	 * @param otherdim the dimension to divide by.
	 * @return {@code true} if the operation was successful.
	 */
	private boolean divideDimension(Dimension otherdim) {
		if (category == otherdim.category) {
			exponent -= otherdim.exponent;
			return true;
		}

		if (otherdim.category == Category.number || otherdim.category == Category.integer) {
			return true;
		}

		switch (category) {
		case number:
		case integer:
			category = otherdim.category;
			exponent = -otherdim.exponent;
			if (exponent == -1) {
				if (category == Category.time) {
					category = Category.frequency;
					exponent = 1;
				} else if (category == Category.frequency) {
					category = Category.time;
					exponent = 1;
				}
			}
			break;
		case length:
			if (otherdim.category == Category.lengthPercentage || otherdim.category == Category.percentage) {
				category = Category.lengthPercentage;
				exponent -= otherdim.exponent;
				break;
			}
			return false;
		case percentage:
			if (otherdim.category == Category.lengthPercentage || otherdim.category == Category.length) {
				category = Category.lengthPercentage;
				exponent -= otherdim.exponent;
				break;
			}
			return false;
		case lengthPercentage:
			if (otherdim.category == Category.length) {
				exponent -= otherdim.exponent;
				lengthProcessed = true;
				break;
			}
			if (otherdim.category == Category.percentage) {
				exponent -= otherdim.exponent;
				percentageProcessed = true;
				break;
			}
			return false;
		case frequency:
			if (otherdim.category == Category.time) {
				exponent += otherdim.exponent;
				break;
			}
			return false;
		case time:
			if (otherdim.category == Category.frequency) {
				exponent += otherdim.exponent;
				break;
			}
		default:
			return false;
		}

		return true;
	}

	/**
	 * Sum the given unit to this dimension.
	 * 
	 * @param unit
	 * @return {@code true} if the operation was successful.
	 */
	public boolean sumUnit(short unit) {
		switch (category) {
		case number:
		case integer:
			return unit == CSSUnit.CSS_NUMBER;
		case length:
			if (CSSUnit.isLengthUnitType(unit)) {
				lengthProcessed = true;
				break;
			}
			if (unit == CSSUnit.CSS_PERCENTAGE) {
				category = Category.lengthPercentage;
				percentageProcessed = true;
				break;
			}
			return false;
		case percentage:
			if (unit == CSSUnit.CSS_PERCENTAGE) {
				percentageProcessed = true;
				break;
			}
			if (CSSUnit.isLengthUnitType(unit)) {
				category = Category.lengthPercentage;
				lengthProcessed = true;
				break;
			}
			return false;
		case lengthPercentage:
			if (unit == CSSUnit.CSS_PERCENTAGE) {
				percentageProcessed = true;
				return true;
			}
			if (CSSUnit.isLengthUnitType(unit)) {
				lengthProcessed = true;
				return true;
			}
		case angle:
			return CSSUnit.isAngleUnitType(unit);
		case time:
			return CSSUnit.isTimeUnitType(unit);
		case frequency:
			return unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ;
		case flex:
			return unit == CSSUnit.CSS_FR;
		case resolution:
			return CSSUnit.isResolutionUnitType(unit);
		default:
			return false;
		}

		return true;
	}

	/**
	 * Multiply the given unit to this dimension.
	 * 
	 * @param unit
	 * @return the resulting dimension.
	 */
	public Dimension multiplyByUnit(short unit) {
		if (unit == CSSUnit.CSS_NUMBER) {
			// Do nothing
			return this;
		}

		Dimension dim = this;
		if (CSSUnit.isLengthUnitType(unit)) {
			do {
				if (category == Category.length) {
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.percentage) {
					dim.category = Category.lengthPercentage;
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.lengthPercentage) {
					dim = dim.incrExponent();
					lengthProcessed = true;
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.length;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.length;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			pcntLoop: do {
				switch (dim.category) {
				case lengthPercentage:
					percentageProcessed = true;
				case percentage:
					dim = dim.incrExponent();
					break pcntLoop;
				case length:
					dim.category = Category.lengthPercentage;
					dim = dim.incrExponent();
					break pcntLoop;
				case number:
				case integer:
					dim.exponent = 1;
					dim.category = Category.percentage;
					break pcntLoop;
				default:
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.percentage;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isAngleUnitType(unit)) {
			do {
				if (dim.category == Category.angle) {
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.angle;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.angle;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isTimeUnitType(unit)) {
			do {
				if (dim.category == Category.time) {
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.time;
					break;
				}
				if (dim.category == Category.frequency) {
					dim = dim.decrExponent();
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.time;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ) {
			do {
				if (dim.category == Category.frequency) {
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.frequency;
					break;
				}
				if (dim.category == Category.time) {
					dim = dim.decrExponent();
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.frequency;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isResolutionUnitType(unit)) {
			do {
				if (dim.category == Category.resolution) {
					dim = dim.incrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.resolution;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.resolution;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else {
			throw new IllegalStateException("Do not know how to handle unit " + unit);
		}

		if (exponent == 0) {
			return remove();
		}

		return dim;
	}

	/**
	 * Divide the dimension by the given unit.
	 * 
	 * @param unit
	 * @return the resulting dimension.
	 */
	public Dimension divideByUnit(short unit) {
		if (unit == CSSUnit.CSS_NUMBER) {
			// Do nothing
			return this;
		}

		Dimension dim = this;
		if (CSSUnit.isLengthUnitType(unit)) {
			do {
				if (dim.category == Category.length) {
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.percentage) {
					dim.category = Category.lengthPercentage;
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.lengthPercentage) {
					dim = dim.decrExponent();
					lengthProcessed = true;
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = -1;
					dim.category = Category.length;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.length;
					newdim.exponent = -1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (unit == CSSUnit.CSS_PERCENTAGE) {
			pcntLoop: do {
				switch (dim.category) {
				case length:
					dim.category = Category.lengthPercentage;
				case lengthPercentage:
					percentageProcessed = true;
				case percentage:
					dim = dim.decrExponent();
					break pcntLoop;
				case number:
				case integer:
					dim.exponent = -1;
					dim.category = Category.percentage;
					// if <number>, there are no further units
					break pcntLoop;
				default:
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.percentage;
					newdim.exponent = -1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isAngleUnitType(unit)) {
			do {
				if (dim.category == Category.angle) {
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = -1;
					dim.category = Category.angle;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.angle;
					newdim.exponent = -1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isTimeUnitType(unit)) {
			do {
				if (dim.category == Category.time) {
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.frequency;
					break;
				}
				if (dim.category == Category.frequency) {
					dim = dim.incrExponent();
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.frequency;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (unit == CSSUnit.CSS_HZ || unit == CSSUnit.CSS_KHZ) {
			do {
				if (dim.category == Category.frequency) {
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = 1;
					dim.category = Category.time;
					break;
				}
				if (dim.category == Category.time) {
					dim = dim.incrExponent();
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.time;
					newdim.exponent = 1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else if (CSSUnit.isResolutionUnitType(unit)) {
			do {
				if (dim.category == Category.resolution) {
					dim = dim.decrExponent();
					break;
				}
				if (dim.category == Category.number || dim.category == Category.integer) {
					dim.exponent = -1;
					dim.category = Category.resolution;
					break;
				}
				Dimension nextdim = dim.getNext();
				if (nextdim == null) {
					// Create new dimension
					Dimension newdim = new Dimension();
					newdim.category = Category.resolution;
					newdim.exponent = -1;
					dim.append(newdim);
					break;
				}
				dim = nextdim;
			} while (true);
		} else {
			throw new IllegalStateException("Do not know how to handle unit " + unit);
		}

		if (exponent == 0) {
			return remove();
		}

		return dim;
	}

	/**
	 * Remove this dimension from the dimension chain.
	 * <p>
	 * If this dimension is the only element in the chain, it is reset to a
	 * dimensionless &lt;number&gt;.
	 * </p>
	 * 
	 * @return the first dimension in the resulting chain.
	 */
	public Dimension remove() {
		if (nextDimension != null) {
			nextDimension.prevDimension = prevDimension;
		} else {
			if (prevDimension == null) {
				category = Category.number;
				exponent = 0;
				return this;
			} else {
				return prevDimension.getFirst();
			}
		}

		if (prevDimension != null) {
			prevDimension.nextDimension = nextDimension;
			return prevDimension.getFirst();
		}

		return nextDimension;
	}

	public void append(Dimension dim) {
		if (nextDimension != null) {
			nextDimension.prevDimension = dim;
		}
		dim.nextDimension = nextDimension;
		nextDimension = dim;
		dim.prevDimension = this;
	}

	/**
	 * 
	 * @return {@code true} if this dimension represents a valid CSS unit.
	 */
	public boolean isCSS() {
		return nextDimension == null && (exponent == 1 || exponent == 0 || exponentAccuracy != 0);
	}

	public Match matches(CSSValueSyntax syntax) {
		if (nextDimension == null) {
			Category cat = syntax.getCategory();
			if ((category == cat
					|| (category == Category.length && cat == Category.lengthPercentage)
					|| (category == Category.percentage && cat == Category.lengthPercentage)
					|| (category == Category.integer && cat == Category.number)
					// If the lexical unit is a calc() parameter, <number> is rounded to <integer>
					|| (category == Category.number && cat == Category.integer)) && nextDimension == null) {
				if (exponentAccuracy == 2) {
					return Match.PENDING;
				}
				if (exponent == 1 || exponent == 0 || exponentAccuracy == 1) {
					return Match.TRUE;
				}
			}
		}
		return Match.FALSE;
	}

	@Override
	public String toString() {
		return category.name() + '^' + Integer.toString(exponent);
	}

}
