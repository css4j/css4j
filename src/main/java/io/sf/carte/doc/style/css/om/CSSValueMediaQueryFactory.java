/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.CSSValueFactory;
import io.sf.carte.doc.style.css.MediaFeaturePredicate;
import io.sf.carte.doc.style.css.MediaQueryFactory;
import io.sf.carte.doc.style.css.MediaQueryHandler;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryPredicate;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Contains methods related to media query conditions.
 */
public class CSSValueMediaQueryFactory implements MediaQueryFactory {

	private static final MediaQueryList allMediaSingleton = new MediaQueryListImpl().unmodifiable();

	/**
	 * Gets an unmodifiable media list for all media.
	 * 
	 * @return the unmodifiable media list for all media.
	 */
	public static MediaQueryList getAllMediaInstance() {
		return allMediaSingleton;
	}

	/**
	 * Create a boolean condition of the <code>and</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createAndCondition() {
		return new AndCondition();
	}

	/**
	 * Create a boolean condition of the given type (<code>and</code>,
	 * <code>or</code>, <code>not</code>).
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createOrCondition() {
		return new OrCondition();
	}

	/**
	 * Create a boolean condition of the <code>not</code> type.
	 * 
	 * @return the condition.
	 */
	@Override
	public BooleanCondition createNotCondition() {
		return new NotCondition();
	}

	/**
	 * Create a media-feature operand condition.
	 * 
	 * @param featureName the name of the media feature.
	 * 
	 * @return the condition.
	 */
	@Override
	public MediaFeaturePredicate createPredicate(String featureName) {
		return new AbstractMediaFeaturePredicate(featureName) {

			private static final long serialVersionUID = 1L;

			@Override
			protected CSSValueFactory getValueFactory() {
				return CSSValueMediaQueryFactory.this.getValueFactory(getName());
			}

		};
	}

	@Override
	public MediaQueryPredicate createMediaTypePredicate(String medium) {
		return new MediaTypePredicate(medium);
	}

	@Override
	public MediaQueryHandler createMediaQueryHandler(Node owner) {
		MediaQueryListImpl list = new MyMediaQueryListImpl();
		return list.new MyMediaQueryHandler(owner);
	}

	@Override
	public MediaQueryList createAllMedia() {
		return new MyMediaQueryListImpl();
	}

	/**
	 * Create a {@code CSSNumberValue} in the desired implementation.
	 * 
	 * @param unit                 the unit.
	 * @param valueInSpecifiedUnit the value in the given unit.
	 * @param calculated           whether the value was calculated. Implementations
	 *                             may ignore this parameter.
	 * @return the number value.
	 */
	protected CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit,
			boolean calculated) {
		NumberValue value = NumberValue.createCSSNumberValue(unit, valueInSpecifiedUnit);
		value.setCalculatedNumber(calculated);
		return value;
	}

	/**
	 * Create a {@code Parser} compatible with this media query factory.
	 * <p>
	 * The parser shall be used to append media queries to a given list.
	 * </p>
	 * 
	 * @return the parser.
	 */
	protected Parser createParser() {
		return new CSSOMParser();
	}

	/**
	 * Create a {@code CSSValueFactory} with the desired OM value implementation.
	 * 
	 * @param featureName the name of the media feature.
	 * @return the object-model value factory.
	 */
	protected CSSValueFactory getValueFactory(String featureName) {
		return new ValueFactory();
	}

	private class MyMediaQueryListImpl extends MediaQueryListImpl {

		private static final long serialVersionUID = 1L;

		MyMediaQueryListImpl() {
			super();
		}

		protected MediaQueryImpl createMediaQuery() {
			return new MediaQueryImpl() {

				private static final long serialVersionUID = 1L;

				@Override
				protected CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit,
						boolean calculated) {
					return CSSValueMediaQueryFactory.this.createNumberValue(unit,
							valueInSpecifiedUnit, calculated);
				}

			};
		}

		@Override
		protected CSSValueMediaQueryFactory getMediaQueryFactory() {
			return CSSValueMediaQueryFactory.this;
		}

	}

}
