package io.sf.carte.doc.style.css;

/**
 * {@code color-mix()} function.
 */
public interface CSSColorMixFunction extends CSSColorValue {

	/**
	 * Gets the interpolation color space, as defined by CSS.
	 * <p>
	 * Beware that this color space may be different from the one reported by the
	 * interpolated color, given that CSS considers {@code hsl} and {@code hwb} as
	 * color spaces despite the actual color space being {@code sRGB}.
	 * </p>
	 * 
	 * @return the interpolation color space.
	 */
	String getCSSColorSpace();

	/**
	 * The first color value.
	 * 
	 * @return the first color value.
	 */
	CSSPrimitiveValue getColorValue1();

	/**
	 * The second color value.
	 * 
	 * @return the second color value.
	 */
	CSSPrimitiveValue getColorValue2();

	/**
	 * Gives the percentage that applies to the first color.
	 * 
	 * @return the percentage or {@code null} if no percentage was explicitly set.
	 */
	CSSPrimitiveValue getPercentage1();

	/**
	 * Gives the percentage that applies to the second color.
	 * 
	 * @return the percentage or {@code null} if no percentage was explicitly set.
	 */
	CSSPrimitiveValue getPercentage2();

	@Override
	CSSColorMixFunction clone();

}
