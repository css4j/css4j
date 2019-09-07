/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.sf.carte.doc.style.css.property.StyleValue;

class BorderBuilder extends BaseBoxShorthandBuilder {

	private boolean fullBorderImage = false; // All the border-image properties are available
	private boolean hasBorderImage = false; // At least one border-image property is available

	private Set<String> unusedSet = new HashSet<String>();

	BorderBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("border", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 3;
	}

	@Override
	protected void appendMinifiedIndividualProperties(StringBuilder buf) {
		super.appendMinifiedIndividualProperties(buf);
		if (isAnyBorderImagePropertySet()) {
			BorderImageBuilder builder = createBorderImageBuilder();
			builder.appendMinifiedCssText(buf);
		}
	}

	private BorderImageBuilder createBorderImageBuilder() {
		BaseCSSStyleDeclaration style = getParentStyle();
		BorderImageBuilder builder = new BorderImageBuilder(style);
		if (style.isPropertySet("border-image-source")) {
			builder.addAssignedProperty("border-image-source", style.isPropertyImportant("border-image-source"));
		}
		if (style.isPropertySet("border-image-slice")) {
			builder.addAssignedProperty("border-image-slice", style.isPropertyImportant("border-image-slice"));
		}
		if (style.isPropertySet("border-image-width")) {
			builder.addAssignedProperty("border-image-width", style.isPropertyImportant("border-image-width"));
		}
		if (style.isPropertySet("border-image-outset")) {
			builder.addAssignedProperty("border-image-outset", style.isPropertyImportant("border-image-outset"));
		}
		if (style.isPropertySet("border-image-repeat")) {
			builder.addAssignedProperty("border-image-repeat", style.isPropertyImportant("border-image-repeat"));
		}
		return builder;
	}

	/**
	 * Inefficient check for 'inherit' values.
	 * 
	 * @param declaredSet
	 *            the declared set.
	 * 
	 * @return 0 if no inherit was found within the declaredSet, 1 if all values are inherit,
	 *         2 if both inherit and non-inherit values were found mixed, 3 if one or two of
	 *         the border-[width-style-color] properties are full 'inherit'.
	 */
	@Override
	byte checkValuesForInherit(Set<String> declaredSet) {
		byte wcheck = checkValuesForInherit("border-width", declaredSet);
		byte scheck = checkValuesForInherit("border-style", declaredSet);
		byte ccheck = checkValuesForInherit("border-color", declaredSet);
		if (wcheck == 1 && scheck == 1 && ccheck == 1 && fullBorderImage) {
			// All values are inherit, and border-image is set.
			return 1;
		}
		if (wcheck == 2 || scheck == 2 || ccheck == 2) {
			return 2;
		}
		return (wcheck == 1 || scheck == 1 || ccheck == 1 ? (byte) 3 : 0);
	}

	/**
	 * Inefficient check for keyword identifiers in 'border' values.
	 * 
	 * @param keyword
	 *            the keyword.
	 * 
	 * @return 0 if no keyword was found; 1 if all values are keyword; 2 if both keyword and
	 *         non-keyword values are mixed in at least one of width, style or color; 3 if
	 *         width, style or color are keyword but at least one isn't.
	 */
	@Override
	byte checkValuesForKeyword(String keyword, Set<String> declaredSet) {
		byte wcheck = checkValuesForKeyword(keyword, "border-width", declaredSet);
		byte scheck = checkValuesForKeyword(keyword, "border-style", declaredSet);
		byte ccheck = checkValuesForKeyword(keyword, "border-color", declaredSet);
		if (wcheck == 1 && scheck == 1 && ccheck == 1 && fullBorderImage) {
			// All values are 'keyword', and border-image is set
			return 1;
		}
		if (wcheck == 2 || scheck == 2 || ccheck == 2) {
			return 2;
		}
		return (wcheck == 1 || scheck == 1 || ccheck == 1 ? (byte) 3 : 0);
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Is border-image set ?
		setBorderImageState(important);
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		if (declaredSet.size() == 12 || (!important && getTotalSetSize() == 12)) {
			// All border properties are available
			// 'inherit' check
			byte icheck = checkValuesForInherit(declaredSet);
			if (icheck == 1) {
				// All values are inherit
				buf.append("border:inherit");
				appendPriority(buf, important);
				return true;
			} else if (icheck == 2) {
				return false;
			}
			// 'unset' check
			byte ucheck = checkValuesForKeyword("unset", declaredSet);
			if (ucheck == 1) {
				// All values are unset
				buf.append("border:unset");
				appendPriority(buf, important);
				return true;
			} else if (ucheck == 2) {
				return false;
			}
			// width/style/color scores
			boolean mixedCase = false;
			PropertyValueScore score = new PropertyValueScore(declaredSet);
			score.score();
			byte live_state = bestState(score);
			int effectiveScore = score.getScore(live_state);
			if (effectiveScore == -1) { // Has mixed states
				mixedCase = true;
				score.setEquivalentScores();
				live_state = bestState(score);
				effectiveScore = score.getScore(live_state);
				buildUnusedSet(declaredSet);
			}
			if (fullBorderImage) {
				boolean ret = appendFullSet(buf, declaredSet, score, effectiveScore, live_state, important);
				if (mixedCase) {
					appendUnused(buf, important);
				}
				if (ret) {
					return true;
				}
			}
			appendBorderWidthText(buf, declaredSet, false, score, score.getSameWidthScore(), null, important);
			appendBorderStyleText(buf, declaredSet, false, score, score.getSameStyleScore(), null, important);
			appendBorderColorText(buf, declaredSet, false, score, score.getSameColorScore(), null, important);
			if (hasBorderImage) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		}
		if (hasBorderImage) {
			BorderImageBuilder builder = createBorderImageBuilder();
			builder.appendMinifiedCssText(buf);
		}
		// We could not build the full 'border' shorthand, but perhaps border-color
		// or some other could be formed.
		// Check which shorthands can be built, and build the 'unused' set
		PropertyCount counter = new PropertyCount();
		counter.count(declaredSet, important);
		if (declaredSet.size() < 3) {
			if (important || !isInShadowedSet(declaredSet, counter)) {
				return false;
			}
		}
		PropertyValueScore score = new PropertyValueScore(declaredSet);
		score.score(counter);
		return appendPartialShorthands(buf, declaredSet, score, counter, important);
	}

	private boolean isInShadowedSet(Set<String> declaredSet, PropertyCount counter) {
		for (String property : declaredSet) {
			if (!isInShadowedSet(property, counter)) {
				return false;
			}
		}
		return true;
	}

	private boolean isInShadowedSet(String property, PropertyCount counter) {
		BaseCSSStyleDeclaration style = getParentStyle();
		if ("border-top-width".equals(property)) {
			if (counter.countBWidth == 4
					|| (style.isPropertySet("border-top-style") && style.isPropertySet("border-top-color"))) {
				return true;
			}
		} else if ("border-top-style".equals(property)) {
			if (counter.countBStyle == 4
					|| (style.isPropertySet("border-top-width") && style.isPropertySet("border-top-color"))) {
				return true;
			}
		} else if ("border-top-color".equals(property)) {
			if (counter.countBColor == 4
					|| (style.isPropertySet("border-top-width") && style.isPropertySet("border-top-style"))) {
				return true;
			}
		} else if ("border-right-width".equals(property)) {
			if (counter.countBWidth == 4
					|| (style.isPropertySet("border-right-style") && style.isPropertySet("border-right-color"))) {
				return true;
			}
		} else if ("border-right-style".equals(property)) {
			if (counter.countBStyle == 4
					|| (style.isPropertySet("border-right-width") && style.isPropertySet("border-right-color"))) {
				return true;
			}
		} else if ("border-right-color".equals(property)) {
			if (counter.countBColor == 4
					|| (style.isPropertySet("border-right-width") && style.isPropertySet("border-right-style"))) {
				return true;
			}
		} else if ("border-bottom-width".equals(property)) {
			if (counter.countBWidth == 4
					|| (style.isPropertySet("border-bottom-style") && style.isPropertySet("border-bottom-color"))) {
				return true;
			}
		} else if ("border-bottom-style".equals(property)) {
			if (counter.countBStyle == 4
					|| (style.isPropertySet("border-bottom-width") && style.isPropertySet("border-bottom-color"))) {
				return true;
			}
		} else if ("border-bottom-color".equals(property)) {
			if (counter.countBColor == 4
					|| (style.isPropertySet("border-bottom-width") && style.isPropertySet("border-bottom-style"))) {
				return true;
			}
		} else if ("border-left-width".equals(property)) {
			if (counter.countBWidth == 4
					|| (style.isPropertySet("border-left-style") && style.isPropertySet("border-left-color"))) {
				return true;
			}
		} else if ("border-left-style".equals(property)) {
			if (counter.countBStyle == 4
					|| (style.isPropertySet("border-left-width") && style.isPropertySet("border-left-color"))) {
				return true;
			}
		} else if ("border-left-color".equals(property)) {
			if (counter.countBColor == 4
					|| (style.isPropertySet("border-left-width") && style.isPropertySet("border-left-style"))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find the best state (to build the largest shorthand) for the given score object.
	 * 
	 * @param score
	 *            the score.
	 * @return 0 if the best state are normal values, 1 if inherit, 5 if unset.
	 */
	private byte bestState(PropertyValueScore score) {
		byte best_state = 0;
		int keyword_state = score.getWidthState().getBestState() + score.getStyleState().getBestState()
				+ score.getColorState().getBestState();
		int inherit_count = keyword_state % 5;
		if (inherit_count > 1) {
			best_state = 1;
		} else if (keyword_state > 7) {
			best_state = 5;
		}
		return best_state;
	}

	/* @formatter:off
	 * 
	 *  ------ Score -----
	 *   A   B   C  Total
	 *  21  21  21    63   border: <A> <B> <C>;
	 *  21  21  20    62   border: <A> <B>; border-side-C: <c>;
	 *  21  21  17    59   border: <A> <B>; border-side-C: <c>;
	 *  21  21  16    58   border: <A> <B>; border-side-C: <c>;
	 *  21  21   5    47   border: <A> <B>; border-side-C: <c>;
	 *  21  21   4    46   border: <A> <B>; border-side-C: <c>;
	 *  21  21   0    42   border: <A> <B>; border-side-C: <c>;
	 *   n   n   n    3n   border: <A> <B> <C>; border-side1: <s1>; [border-side2: <s2>;...]
	 *  21  20  20    61   border: <A>; border-B: <b>; border-C: <c>;
	 *  21  20   0    41   border: <A>; border-B: <b>; border-C: <c>;
	 *  21   0   0    21   border: <A>; border-B: <b>; border-C: <c>;
	 * Other combinations:
	 *  21  Sb  Sc   >21   border: <A>; border-B: <b>; border-C: <c>;
	 *  Sa  Sb  Sc     ?   border-A: <A>; border-B: <b>; border-C: <c>; border-image:<...>;
	 * 
	 * @formatter:on
	 */
	private boolean appendFullSet(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			int effectiveScore, byte live_state, boolean important) {
		if (effectiveScore == 63) {
			// Full 'border' shorthand
			// All values are equal
			Set<String> equivWidthSet = score.getEquivWidthSet(score.getSameWidthScore());
			Set<String> equivStyleSet = score.getEquivStyleSet(score.getSameStyleScore());
			Set<String> equivColorSet = score.getEquivColorSet(score.getSameColorScore());
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		}
		if (score.getSameStyleScore() == score.getSameWidthScore()
				&& score.getSameColorScore() == score.getSameWidthScore()) {
			// Here, we can possibly set a full 'border:' with additional border-side properties
			if (score.getSameWidthScore() == 21) {
				// Full 'border:' with border-width/style/color
				// We could reach this if there are values with different live states
				appendFullBorderWithWidthStyleColor(buf, declaredSet, score, live_state, important);
				return true;
			} else if (appendFullBorderPlusSide(buf, declaredSet, score, live_state, important)) {
				return true;
			}
		}
		appendFullBorderPlusMore(buf, declaredSet, score, live_state, important);
		if (!isBorderImageSetToInitial()) {
			BorderImageBuilder builder = createBorderImageBuilder();
			builder.appendMinifiedCssText(buf);
		}
		return true;
	}

	private void appendFullBorderWithWidthStyleColor(StringBuilder buf, Set<String> declaredSet,
			PropertyValueScore score, byte live_state, boolean important) {
		Set<String> equivWidthSet = score.getEquivWidthSet(score.getSameWidthScore());
		Set<String> equivStyleSet = score.getEquivStyleSet(score.getSameStyleScore());
		Set<String> equivColorSet = score.getEquivColorSet(score.getSameColorScore());
		int size = equivWidthSet.size();
		if (size == 3 || size == 2) {
			equivWidthSet = null;
		}
		size = equivStyleSet.size();
		if (size == 3 || size == 2) {
			equivStyleSet = null;
		}
		size = equivColorSet.size();
		if (size == 3 || size == 2) {
			equivColorSet = null;
		}
		appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
				important);
		if (score.getWidthState().getBestState() != live_state || score.getSameWidthScore() != 21) {
			appendBorderWidthText(buf, declaredSet, false, score, score.getSameWidthScore(), null, important);
		}
		if (score.getStyleState().getBestState() != live_state || score.getSameStyleScore() != 21) {
			appendBorderStyleText(buf, declaredSet, false, score, score.getSameStyleScore(), null, important);
		}
		if (score.getColorState().getBestState() != live_state || score.getSameColorScore() != 21) {
			appendBorderColorText(buf, declaredSet, false, score, score.getSameColorScore(), null, important);
		}
		if (!isBorderImageSetToInitial()) {
			BorderImageBuilder builder = createBorderImageBuilder();
			builder.appendMinifiedCssText(buf);
		}
	}

	private boolean appendFullBorderPlusSide(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		int samescore = score.getSameWidthScore(); // Same for width, style, color
		if (samescore == 17) {
			// left = right = top
			Set<String> equivWidthSet = score.getEquivWidthSet(samescore);
			Set<String> equivStyleSet = score.getEquivStyleSet(samescore);
			Set<String> equivColorSet = score.getEquivColorSet(samescore);
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			appendBorderBottomText(buf, declaredSet, score, live_state, important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (samescore == 5) {
			// top = bottom = left
			Set<String> equivWidthSet = score.getEquivWidthSet(samescore);
			Set<String> equivStyleSet = score.getEquivStyleSet(samescore);
			Set<String> equivColorSet = score.getEquivColorSet(samescore);
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			appendBorderRightText(buf, declaredSet, score, live_state, important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (samescore == 16) {
			if (equivalentProperties("border-right-width", "border-bottom-width", score.getWidthState().getBestState())
					&& equivalentProperties("border-right-style", "border-bottom-style",
							score.getStyleState().getBestState())
					&& equivalentProperties("border-right-color", "border-bottom-color",
							score.getColorState().getBestState())) {
				// right = left, plus right = bottom
				Set<String> equivWidthSet = new HashSet<String>();
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-left-width");
				Set<String> equivStyleSet = new HashSet<String>();
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-left-style");
				Set<String> equivColorSet = new HashSet<String>();
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-left-color");
				appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
						important);
				appendBorderTopText(buf, declaredSet, score, live_state, important);
			} else {
				// right = left
				Set<String> equivWidthSet = score.getEquivWidthSet(samescore);
				Set<String> equivStyleSet = score.getEquivStyleSet(samescore);
				Set<String> equivColorSet = score.getEquivColorSet(samescore);
				appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
						important);
				appendBorderTopText(buf, declaredSet, score, live_state, important);
				appendBorderBottomText(buf, declaredSet, score, live_state, important);
			}
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (samescore == 4) {
			if (equivalentProperties("border-right-width", "border-bottom-width", score.getWidthState().getBestState())
					&& equivalentProperties("border-right-style", "border-bottom-style",
							score.getStyleState().getBestState())
					&& equivalentProperties("border-right-color", "border-bottom-color",
							score.getColorState().getBestState())) {
				// top = bottom, plus right = bottom
				Set<String> equivWidthSet = new HashSet<String>();
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-bottom-width");
				Set<String> equivStyleSet = new HashSet<String>();
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-bottom-style");
				Set<String> equivColorSet = new HashSet<String>();
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-bottom-color");
				appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
						important);
				appendBorderLeftText(buf, declaredSet, score, live_state, important);
			} else {
				// top = bottom
				Set<String> equivWidthSet = score.getEquivWidthSet(samescore);
				Set<String> equivStyleSet = score.getEquivStyleSet(samescore);
				Set<String> equivColorSet = score.getEquivColorSet(samescore);
				appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
						important);
				appendBorderRightText(buf, declaredSet, score, live_state, important);
				appendBorderLeftText(buf, declaredSet, score, live_state, important);
			}
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (samescore == 1) {
			// top = left
			Set<String> equivWidthSet = score.getEquivWidthSet(samescore);
			Set<String> equivStyleSet = score.getEquivStyleSet(samescore);
			Set<String> equivColorSet = score.getEquivColorSet(samescore);
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			appendBorderRightText(buf, declaredSet, score, live_state, important);
			appendBorderBottomText(buf, declaredSet, score, live_state, important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (equivalentProperties("border-right-width", "border-top-width", score.getWidthState().getBestState())
				&& equivalentProperties("border-right-style", "border-top-style", score.getStyleState().getBestState())
				&& equivalentProperties("border-right-color", "border-top-color",
						score.getColorState().getBestState())) {
			// top = right
			Set<String> equivWidthSet = new HashSet<String>();
			equivWidthSet.add("border-top-width");
			equivWidthSet.add("border-right-width");
			Set<String> equivStyleSet = new HashSet<String>();
			equivStyleSet.add("border-top-style");
			equivStyleSet.add("border-right-style");
			Set<String> equivColorSet = new HashSet<String>();
			equivColorSet.add("border-top-color");
			equivColorSet.add("border-right-color");
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			appendBorderBottomText(buf, declaredSet, score, live_state, important);
			appendBorderLeftText(buf, declaredSet, score, live_state, important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		} else if (equivalentProperties("border-left-width", "border-bottom-width",
				score.getWidthState().getBestState())
				&& equivalentProperties("border-left-style", "border-bottom-style",
						score.getStyleState().getBestState())
				&& equivalentProperties("border-left-color", "border-bottom-color",
						score.getColorState().getBestState())) {
			// bottom = left
			Set<String> equivWidthSet = new HashSet<String>();
			equivWidthSet.add("border-bottom-width");
			equivWidthSet.add("border-left-width");
			Set<String> equivStyleSet = new HashSet<String>();
			equivStyleSet.add("border-bottom-style");
			equivStyleSet.add("border-left-style");
			Set<String> equivColorSet = new HashSet<String>();
			equivColorSet.add("border-bottom-color");
			equivColorSet.add("border-left-color");
			appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
					important);
			appendBorderTopText(buf, declaredSet, score, live_state, important);
			appendBorderRightText(buf, declaredSet, score, live_state, important);
			if (!isBorderImageSetToInitial()) {
				BorderImageBuilder builder = createBorderImageBuilder();
				builder.appendMinifiedCssText(buf);
			}
			return true;
		}
		return false;
	}

	private void appendBorderTopText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		if (declaredSet.contains("border-top-width") && declaredSet.contains("border-top-style")
				&& declaredSet.contains("border-top-color")) {
			buf.append("border-top:");
			appendBorderSideText(buf, declaredSet, score, live_state, "border-top-width", "border-top-style",
					"border-top-color", important);
		}
	}

	private void appendBorderRightText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		if (declaredSet.contains("border-right-width") && declaredSet.contains("border-right-style")
				&& declaredSet.contains("border-right-color")) {
			buf.append("border-right:");
			appendBorderSideText(buf, declaredSet, score, live_state, "border-right-width", "border-right-style",
					"border-right-color", important);
		}
	}

	private void appendBorderBottomText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		if (declaredSet.contains("border-bottom-width") && declaredSet.contains("border-bottom-style")
				&& declaredSet.contains("border-bottom-color")) {
			buf.append("border-bottom:");
			appendBorderSideText(buf, declaredSet, score, live_state, "border-bottom-width", "border-bottom-style",
					"border-bottom-color", important);
		}
	}

	private void appendBorderLeftText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		if (declaredSet.contains("border-left-width") && declaredSet.contains("border-left-style")
				&& declaredSet.contains("border-left-color")) {
			buf.append("border-left:");
			appendBorderSideText(buf, declaredSet, score, live_state, "border-left-width", "border-left-style",
					"border-left-color", important);
		}
	}

	private void buildUnusedSet(Set<String> declaredSet) {
		unusedSet.clear();
		Iterator<String> it = declaredSet.iterator();
		while (it.hasNext()) {
			String property = it.next();
			unusedSet.add(property);
		}
	}

	private boolean isAnyBorderImagePropertySet() {
		BaseCSSStyleDeclaration style = getParentStyle();
		return style.isPropertySet("border-image-source") || style.isPropertySet("border-image-slice")
				|| style.isPropertySet("border-image-width") || style.isPropertySet("border-image-outset")
				|| style.isPropertySet("border-image-repeat");
	}

	/**
	 * Are any of the border-image properties set ?
	 * <p>
	 * Even if only one of those properties are set, its shorthand builder is responsible for
	 * serializing them.
	 * 
	 * @param important
	 *            true if the priority is important.
	 * @return <code>true</code> if at least one border-image property is set.
	 */
	private void setBorderImageState(boolean important) {
		boolean bisource = isPropertyAssigned("border-image-source", important);
		boolean bislice = isPropertyAssigned("border-image-slice", important);
		boolean biwidth = isPropertyAssigned("border-image-width", important);
		boolean bioutset = isPropertyAssigned("border-image-outset", important);
		boolean birepeat = isPropertyAssigned("border-image-repeat", important);
		this.fullBorderImage = bisource && bislice && biwidth && bioutset && birepeat;
		this.hasBorderImage = bisource || bislice || biwidth || bioutset || birepeat;
	}

	private boolean isBorderImageSetToInitial() {
		return isInitialValue("border-image-source") && isInitialValue("border-image-slice")
				&& isInitialValue("border-image-width") && isInitialValue("border-image-outset")
				&& isInitialValue("border-image-repeat");
	}

	/**
	 * Append text for shorthand values when not all 'border' subproperties are available.
	 * 
	 * @param buf
	 * @param declaredSet
	 * @param score
	 * @param counter
	 * @param important
	 *            true if the properties are of important priority
	 * @return <code>true</code> if at least one shorthand (and the eventual remaining properties) was
	 *         appended, <code>false</code> otherwise.
	 */
	private boolean appendPartialShorthands(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			PropertyCount counter, boolean important) {
		if (score.hasMixedStates()) {
			score.setEquivalentScores();
		}
		boolean ret = appendPartialProperties(buf, declaredSet, score, false, counter, important);
		appendSideProperties(buf, !ret, counter, important);
		appendUnused(buf, important);
		return true;
	}

	/**
	 * Append text for border-color, border-style and border-width.
	 * 
	 * @param buf
	 * @param declaredSet
	 * @param score
	 * @param appendOnlyUnused
	 * @param counter
	 * @param important
	 *            true if the properties are of important priority
	 * @return <code>true</code> if at least one shorthand was appended, <code>false</code> otherwise.
	 */
	private boolean appendPartialProperties(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			boolean appendOnlyUnused, PropertyCount counter, boolean important) {
		boolean ret = false;
		if (counter.countBColor == 4) {
			appendBorderColorText(buf, declaredSet, appendOnlyUnused, score, score.getSameColorScore(), counter,
					important);
			ret = true;
		}
		if (counter.countBStyle == 4) {
			appendBorderStyleText(buf, declaredSet, appendOnlyUnused, score, score.getSameStyleScore(), counter,
					important);
			ret = true;
		}
		if (counter.countBWidth == 4) {
			appendBorderWidthText(buf, declaredSet, appendOnlyUnused, score, score.getSameWidthScore(), counter,
					important);
			ret = true;
		}
		return ret;
	}

	private void appendSideProperties(StringBuilder buf, boolean appendOnlyUnused, PropertyCount counter,
			boolean important) {
		if (counter.countBTop == 3 && appendBorderTopText(buf, appendOnlyUnused)) {
			appendPriority(buf, important);
		}
		if (counter.countBRight == 3 && appendBorderRightText(buf, appendOnlyUnused)) {
			appendPriority(buf, important);
		}
		if (counter.countBBottom == 3 && appendBorderBottomText(buf, appendOnlyUnused)) {
			appendPriority(buf, important);
		}
		if (counter.countBLeft == 3 && appendBorderLeftText(buf, appendOnlyUnused)) {
			appendPriority(buf, important);
		}
	}

	private void appendUnused(StringBuilder buf, boolean important) {
		if (!unusedSet.isEmpty()) {
			Iterator<String> it = unusedSet.iterator();
			while (it.hasNext()) {
				String unusedPty = it.next();
				buf.append(unusedPty).append(':').append(getCSSValue(unusedPty).getMinifiedCssText(unusedPty));
				appendPriority(buf, important);
			}
		}
	}

	private void appendFullBorderPlusMore(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, boolean important) {
		boolean bWidthNotLive = score.getWidthState().getBestState() != live_state;
		boolean bStyleNotLive = score.getStyleState().getBestState() != live_state;
		boolean bColorNotLive = score.getColorState().getBestState() != live_state;
		final int wscore = score.getSameWidthScore();
		final int sscore = score.getSameStyleScore();
		final int cscore = score.getSameColorScore();
		boolean appendBWidth = wscore != 21;
		boolean appendBStyle = sscore != 21;
		boolean appendBColor = cscore != 21;
		Set<String> equivWidthSet;
		if (appendBWidth) {
			equivWidthSet = null;
		} else {
			equivWidthSet = score.getEquivWidthSet(wscore);
		}
		Set<String> equivStyleSet;
		if (appendBStyle) {
			equivStyleSet = null;
		} else {
			equivStyleSet = score.getEquivStyleSet(sscore);
		}
		Set<String> equivColorSet;
		if (appendBColor) {
			equivColorSet = null;
		} else {
			equivColorSet = score.getEquivColorSet(cscore);
		}
		StringBuilder trailbuf = new StringBuilder(80);
		if (bWidthNotLive) {
			appendBorderWidthText(trailbuf, declaredSet, false, score, wscore, null, important);
		} else if (appendBWidth) {
			if (wscore == 17) {
				// left = right = top
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-bottom-width");
				equivWidthSet = score.getEquivWidthSet(17);
			} else if (wscore == 16 && equivalentProperties("border-right-width", "border-bottom-width",
					score.getWidthState().getBestState())) {
				// right = left + right is equal to left and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-top-width");
				equivWidthSet = score.getEquivWidthSet(15);
			} else if (wscore == 5) {
				// top = bottom = left
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-right-width");
				equivWidthSet = score.getEquivWidthSet(5);
			} else if (wscore == 4 && equivalentProperties("border-top-width", "border-right-width",
					score.getWidthState().getBestState())) {
				// top = bottom + right is equal to top and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-left-width");
				equivWidthSet = score.getEquivWidthSet(4);
			} else { // 21, 20, plain 16, plain 4
				appendBorderWidthText(trailbuf, declaredSet, false, score, wscore, null, important);
			}
		}
		if (bStyleNotLive) {
			appendBorderStyleText(trailbuf, declaredSet, false, score, sscore, null, important);
		} else if (appendBStyle) {
			if (sscore == 17) {
				// left = right = top
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-bottom-style");
				equivStyleSet = score.getEquivStyleSet(17);
			} else if (sscore == 16 && equivalentProperties("border-right-style", "border-bottom-style",
					score.getStyleState().getBestState())) {
				// right = left + right is equal to left and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-top-style");
				equivStyleSet = score.getEquivStyleSet(15);
			} else if (sscore == 5) {
				// top = bottom = left
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-right-style");
				equivStyleSet = score.getEquivStyleSet(5);
			} else if (sscore == 4 && equivalentProperties("border-top-style", "border-right-style",
					score.getStyleState().getBestState())) {
				// top = bottom + right is equal to top and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-left-style");
				equivStyleSet = score.getEquivStyleSet(4);
			} else { // 21, 20, plain 16, plain 4
				appendBorderStyleText(trailbuf, declaredSet, false, score, sscore, null, important);
			}
		}
		if (bColorNotLive) {
			appendBorderColorText(trailbuf, declaredSet, false, score, cscore, null, important);
		} else if (appendBColor) {
			if (cscore == 17) {
				// left = right = top
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-bottom-color");
				equivColorSet = score.getEquivColorSet(17);
			} else if (cscore == 16 && equivalentProperties("border-right-color", "border-bottom-color",
					score.getColorState().getBestState())) {
				// right = left + right is equal to left and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-top-color");
				equivColorSet = score.getEquivColorSet(15);
			} else if (cscore == 5) {
				// top = bottom = left
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-right-color");
				equivColorSet = score.getEquivColorSet(5);
			} else if (cscore == 4 && equivalentProperties("border-top-color", "border-right-color",
					score.getColorState().getBestState())) {
				// top = bottom + right is equal to top and bottom
				appendStandAlonePropertyAndPriority(trailbuf, declaredSet, important, "border-left-color");
				equivColorSet = score.getEquivColorSet(4);
			} else { // 21, 20, plain 16, plain 4
				appendBorderColorText(trailbuf, declaredSet, false, score, cscore, null, important);
			}
		}
		appendFullBorderText(buf, declaredSet, score, live_state, equivWidthSet, equivStyleSet, equivColorSet,
				important);
		buf.append(trailbuf);
	}

	private void appendFullBorderText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, Set<String> equivWidthSet, Set<String> equivStyleSet, Set<String> equivColorSet,
			boolean important) {
		StyleValue widthVal;
		String widthProperty;
		if (equivWidthSet != null
				&& (widthProperty = liveMember(declaredSet, equivWidthSet, score, live_state)) != null) {
			widthVal = getCSSValue(widthProperty);
		} else {
			widthProperty = "border-top-width";
			widthVal = null;
		}
		StyleValue styleVal;
		String styleProperty;
		if (equivStyleSet != null
				&& (styleProperty = liveMember(declaredSet, equivStyleSet, score, live_state)) != null) {
			styleVal = getCSSValue(styleProperty);
		} else {
			styleProperty = "border-top-style";
			styleVal = null;
		}
		StyleValue colorVal;
		String colorProperty;
		if (equivColorSet != null
				&& (colorProperty = liveMember(declaredSet, equivColorSet, score, live_state)) != null) {
			colorVal = getCSSValue(colorProperty);
		} else {
			colorProperty = "border-top-color";
			colorVal = null;
		}
		buf.append("border:");
		appendBorderText(buf, declaredSet, score, live_state, widthProperty, widthVal, styleProperty, styleVal,
				colorProperty, colorVal, important);
		removeFromUnused(equivWidthSet);
		removeFromUnused(equivStyleSet);
		removeFromUnused(equivColorSet);
	}

	private void removeFromUnused(Set<String> equivSet) {
		if (equivSet != null) {
			Iterator<String> it = equivSet.iterator();
			while (it.hasNext()) {
				unusedSet.remove(it.next());
			}
		}
	}

	private String liveMember(Set<String> declaredSet, Set<String> equivSet, PropertyValueScore score,
			byte live_state) {
		Iterator<String> it = equivSet.iterator();
		while (it.hasNext()) {
			String property = it.next();
			if (declaredSet.contains(property) && isLiveProperty(property, live_state)) {
				return property;
			}
		}
		return null;
	}

	private void appendBorderSideText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, String widthProperty, String styleProperty, String colorProperty, boolean important) {
		Set<String> localSet = new HashSet<String>(3);
		if (declaredSet.contains(widthProperty)) {
			localSet.add(widthProperty);
		}
		if (declaredSet.contains(styleProperty)) {
			localSet.add(styleProperty);
		}
		if (declaredSet.contains(colorProperty)) {
			localSet.add(colorProperty);
		}
		StyleValue widthVal = getCSSValue(widthProperty);
		StyleValue styleVal = getCSSValue(styleProperty);
		StyleValue colorVal = getCSSValue(colorProperty);
		appendBorderText(buf, localSet, score, live_state, widthProperty, widthVal, styleProperty, styleVal,
				colorProperty, colorVal, important);
	}

	private void appendBorderText(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score, byte live_state,
			String widthProperty, StyleValue widthVal, String styleProperty, StyleValue styleVal,
			String colorProperty, StyleValue colorVal, boolean important) {
		appendBorderTextTriplet(buf, declaredSet, score, live_state, widthProperty, widthVal, styleProperty, styleVal,
				colorProperty, colorVal);
		appendPriority(buf, important);
	}

	private void appendBorderTextTriplet(StringBuilder buf, Set<String> declaredSet, PropertyValueScore score,
			byte live_state, String widthProperty, StyleValue widthVal, String styleProperty,
			StyleValue styleVal, String colorProperty, StyleValue colorVal) {
		boolean appended = false;
		if (widthProperty != null && declaredSet.contains(widthProperty) && isNotInitialValue(widthVal, widthProperty)
				&& isLiveState(widthVal, live_state)) {
			String widthTxt = widthVal.getMinifiedCssText(widthProperty);
			buf.append(widthTxt);
			appended = true;
		}
		if (styleProperty != null && declaredSet.contains(styleProperty) && isNotInitialValue(styleVal, styleProperty)
				&& isLiveState(styleVal, live_state)) {
			if (appended) {
				if (live_state != 0) {
					return;
				}
				buf.append(' ');
			}
			buf.append(styleVal.getMinifiedCssText(styleProperty));
			appended = true;
		}
		if (colorProperty != null && declaredSet.contains(colorProperty) && isNotInitialValue(colorVal, colorProperty)
				&& isLiveState(colorVal, live_state)) {
			if (appended) {
				if (live_state != 0) {
					return;
				}
				buf.append(' ');
			}
			buf.append(colorVal.getMinifiedCssText(colorProperty));
			appended = true;
		}
		if (!appended) {
			buf.append("none");
		}
	}

	private void appendStandAlonePropertyAndPriority(StringBuilder buf, Set<String> declaredSet, boolean important,
			String standAlonePty) {
		if (declaredSet.contains(standAlonePty)) {
			buf.append(standAlonePty).append(':').append(getCSSValue(standAlonePty).getCssText());
			appendPriority(buf, important);
		}
	}

	/**
	 * Append css text for the border-width shorthand property.
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param sameBWidthScore
	 *            the width same-value score.
	 * @param counter
	 * @param important
	 *            true if the properties are of important priority
	 */
	private boolean appendBorderWidthText(StringBuilder buf, Set<String> declaredSet, boolean checkUnused,
			PropertyValueScore score, int sameBWidthScore, PropertyCount counter, boolean important) {
		return appendBorderPropertyBoxText(buf, declaredSet, checkUnused, score, sameBWidthScore,
				score.getWidthState().getBestState(), counter, "border-width:", "border-top-width",
				"border-right-width", "border-bottom-width", "border-left-width", "medium", important);
	}

	/**
	 * Append css text for the border-style shorthand property.
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param sameBStyleScore
	 *            the style same-value score.
	 * @param counter
	 * @param important
	 *            true if the properties are of important priority
	 */
	private boolean appendBorderStyleText(StringBuilder buf, Set<String> declaredSet, boolean checkUnused,
			PropertyValueScore score, int sameBStyleScore, PropertyCount counter, boolean important) {
		return appendBorderPropertyBoxText(buf, declaredSet, checkUnused, score, sameBStyleScore,
				score.getStyleState().getBestState(), counter, "border-style:", "border-top-style",
				"border-right-style", "border-bottom-style", "border-left-style", "none", important);
	}

	/**
	 * Append css text for the border-color shorthand property.
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param sameBColorScore
	 *            the color same-value score.
	 * @param counter
	 * @param important
	 *            true if the properties are of important priority
	 */
	private boolean appendBorderColorText(StringBuilder buf, Set<String> declaredSet, boolean checkUnused,
			PropertyValueScore score, int sameBColorScore, PropertyCount counter, boolean important) {
		return appendBorderPropertyBoxText(buf, declaredSet, checkUnused, score, sameBColorScore,
				score.getColorState().getBestState(), counter, "border-color:", "border-top-color",
				"border-right-color", "border-bottom-color", "border-left-color", "currentcolor", important);
	}

	private boolean appendBorderPropertyBoxText(StringBuilder buf, Set<String> declaredSet, boolean checkUnused,
			PropertyValueScore score, int sameScore, byte live_state, PropertyCount counter, String pnameStr,
			String borderTopPty, String borderRightPty, String borderBottomPty, String borderLeftPty,
			String initialString, boolean important) {
		if (!declaredSet.contains(borderTopPty) && !declaredSet.contains(borderRightPty)
				&& !declaredSet.contains(borderBottomPty) && !declaredSet.contains(borderLeftPty)) {
			return false;
		}
		if (checkUnused) {
			if (!unusedSet.contains(borderTopPty) && !unusedSet.contains(borderRightPty)
					&& !unusedSet.contains(borderBottomPty) && !unusedSet.contains(borderLeftPty)) {
				return false;
			}
		}
		// Perhaps we could omit some values that shall be printed later in border-side shorthands
		if (counter != null) {
			if (sameScore == 16) { // right = left
				if (counter.countBTop == 3 && counter.countBBottom == 3) {
					sameScore = 21;
					borderTopPty = null;
					borderBottomPty = null;
				} else {
					if (equivalentProperties(borderBottomPty, borderRightPty, live_state)) {
						// right = left = bottom
						if (counter.countBTop == 3) {
							sameScore = 21;
							borderTopPty = null;
						}
					} else if (counter.countBTop == 3) {
						sameScore = 20;
						borderTopPty = null;
					} else if (counter.countBBottom == 3) {
						sameScore = 20;
						borderBottomPty = null;
					}
				}
			} else if (sameScore == 17) { // right = left = top
				if (counter.countBBottom == 3) {
					sameScore = 21;
					borderBottomPty = null;
				}
			} else if (sameScore == 20) {
				if ((counter.countBTop == 3 && counter.countBBottom == 3)) {
					sameScore = 21;
					borderTopPty = null;
					borderBottomPty = null;
				} else if (counter.countBRight == 3 && counter.countBLeft == 3) {
					sameScore = 21;
					borderRightPty = null;
					borderLeftPty = null;
				}
			} else if (sameScore == 5) { // top = bottom = left
				if (counter.countBRight == 3) {
					sameScore = 21;
					borderRightPty = null;
				} else if (counter.countBLeft == 3) {
					sameScore = 20;
					borderLeftPty = null;
				}
			} else if (sameScore == 4) { // top = bottom
				if (counter.countBRight == 3 && counter.countBLeft == 3) {
					sameScore = 21;
					borderRightPty = null;
					borderLeftPty = null;
				} else {
					if (equivalentProperties(borderTopPty, borderRightPty, live_state)) {
						// top = bottom = right
						if (counter.countBLeft == 3) {
							sameScore = 21;
							borderLeftPty = null;
						}
					} else if (counter.countBRight == 3) {
						sameScore = 20;
						borderRightPty = null;
					} else if (counter.countBLeft == 3) {
						sameScore = 20;
						borderLeftPty = null;
					}
				}
			} else if (sameScore == 1) { // top = left
				if (equivalentProperties(borderBottomPty, borderRightPty, live_state)) {
					if (counter.countBTop == 3 && counter.countBLeft == 3) {
						sameScore = 21;
						borderTopPty = null;
						borderLeftPty = null;
					} else if (counter.countBLeft == 3) {
						sameScore = 16;
						borderLeftPty = null;
					}
				}
				if (sameScore < 21) {
					if (counter.countBRight == 3 && counter.countBBottom == 3) {
						sameScore = 21;
						borderRightPty = null;
						borderBottomPty = null;
					} else if (counter.countBRight == 3) {
						sameScore = 16;
						borderRightPty = null;
					}
				}
			}
			if (sameScore >= 0 && sameScore < 20) {
				// We missed top = right
				if (equivalentProperties(borderTopPty, borderRightPty, live_state)) {
					if (counter.countBBottom == 3 && counter.countBLeft == 3) {
						sameScore = 21;
						borderBottomPty = null;
						borderLeftPty = null;
					} else if (counter.countBTop == 3 && counter.countBRight == 3) {
						sameScore = 20;
						borderTopPty = null;
						borderRightPty = null;
					} else if (counter.countBLeft == 3) {
						sameScore = 16;
						borderLeftPty = null;
					}
				}
				if (sameScore < 21) {
					// ...and bottom = left
					if (equivalentProperties(borderBottomPty, borderLeftPty, live_state)) {
						if (counter.countBTop == 3 && counter.countBRight == 3) {
							sameScore = 21;
							borderTopPty = null;
							borderRightPty = null;
						} else if (sameScore < 20 && counter.countBBottom == 3 && counter.countBLeft == 3) {
							sameScore = 20;
							borderBottomPty = null;
							borderLeftPty = null;
						} else if (sameScore < 16 && counter.countBRight == 3) {
							sameScore = 16;
							borderRightPty = null;
						}
					}
					if (sameScore < 20) {
						if (counter.countBTop == 3 && counter.countBRight == 3) {
							sameScore = 20;
							borderTopPty = null;
							borderRightPty = null;
						} else if (counter.countBBottom == 3 && counter.countBLeft == 3) {
							sameScore = 20;
							borderBottomPty = null;
							borderLeftPty = null;
						} else if (sameScore < 16) {
							if (counter.countBRight == 3) {
								sameScore = 16;
								borderRightPty = null;
							} else if (counter.countBLeft == 3) {
								sameScore = 16;
								borderLeftPty = null;
							}
						}
					}
				}
			}
		}
		// Append
		boolean appended = false;
		buf.append(pnameStr);
		switch (sameScore) {
		case 21: // 1 value
			String property = borderTopPty;
			if (isBoxLiveProperty(score, borderTopPty, live_state)) {
				unusedSet.remove(borderTopPty);
			}
			if (isBoxLiveProperty(score, borderBottomPty, live_state)) {
				if (unusedSet.remove(borderBottomPty)) {
					property = borderBottomPty;
				}
			}
			if (isBoxLiveProperty(score, borderLeftPty, live_state)) {
				if (unusedSet.remove(borderLeftPty)) {
					property = borderLeftPty;
				}
			}
			if (isBoxLiveProperty(score, borderRightPty, live_state)) {
				if (unusedSet.remove(borderRightPty)) {
					property = borderRightPty;
				}
			}
			appended = appendValueIfNotInitial(buf, property, false);
			if (!appended) {
				buf.append(initialString);
			}
			break;
		case 20: // 2 values
			if (isBoxLiveProperty(score, borderTopPty, live_state) && unusedSet.remove(borderTopPty)) {
				property = borderTopPty;
			} else {
				property = borderBottomPty;
			}
			appended = appendValueIfNotInitial(buf, property, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			String property2;
			if (isBoxLiveProperty(score, borderLeftPty, live_state) && unusedSet.remove(borderLeftPty)) {
				property2 = borderLeftPty;
			} else {
				property2 = borderRightPty;
			}
			appended = appendValueIfNotInitial(buf, property2, false);
			if (!appended) {
				buf.append(initialString);
			}
			if (isBoxLiveProperty(score, borderBottomPty, live_state)) {
				unusedSet.remove(borderBottomPty);
			}
			if (isBoxLiveProperty(score, borderRightPty, live_state)) {
				unusedSet.remove(borderRightPty);
			}
			break;
		case 17: // 3 values
		case 16: // 3 values
			appended = appendValueIfNotInitial(buf, borderTopPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			if (isBoxLiveProperty(score, borderLeftPty, live_state) && unusedSet.remove(borderLeftPty)) {
				property = borderLeftPty;
			} else {
				property = borderRightPty;
			}
			appended = appendValueIfNotInitial(buf, property, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			appended = appendValueIfNotInitial(buf, borderBottomPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			unusedSet.remove(borderTopPty);
			unusedSet.remove(borderBottomPty);
			if (isBoxLiveProperty(score, borderRightPty, live_state)) {
				unusedSet.remove(borderRightPty);
			}
			break;
		default:
			appended = appendValueIfNotInitial(buf, borderTopPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			appended = appendValueIfNotInitial(buf, borderRightPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			appended = appendValueIfNotInitial(buf, borderBottomPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			buf.append(' ');
			appended = appendValueIfNotInitial(buf, borderLeftPty, false);
			if (!appended) {
				buf.append(initialString);
			}
			unusedSet.remove(borderTopPty);
			unusedSet.remove(borderRightPty);
			unusedSet.remove(borderBottomPty);
			unusedSet.remove(borderLeftPty);
		}
		appendPriority(buf, important);
		return true;
	}

	private boolean isBoxLiveProperty(PropertyValueScore score, String property, byte live_state) {
		return property != null && isLiveProperty(property, live_state);
	}

	/**
	 * Append css text for the border-top shorthand property.
	 * <p>
	 * No !important string is ever appended.
	 * </p>
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param onlyUnused
	 *            if true, no text will be appended unless at least one unused value will be
	 *            used.
	 * @return <code>true</code> if text was appended, meaning that at least one unused value remained,
	 *         <code>false</code> otherwise.
	 */
	private boolean appendBorderTopText(StringBuilder buf, boolean onlyUnused) {
		return appendBorderSideText(buf, "border-top:", "border-top-width", "border-top-style", "border-top-color",
				getTopKeywordState(), onlyUnused);
	}

	/**
	 * Append css text for the border-right shorthand property.
	 * <p>
	 * No !important string is ever appended.
	 * </p>
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param onlyUnused
	 *            if true, no text will be appended unless at least one unused value will be
	 *            used.
	 * @return <code>true</code> if text was appended, meaning that at least one unused value remained,
	 *         <code>false</code> otherwise.
	 */
	private boolean appendBorderRightText(StringBuilder buf, boolean onlyUnused) {
		return appendBorderSideText(buf, "border-right:", "border-right-width", "border-right-style",
				"border-right-color", getRightKeywordState(), onlyUnused);
	}

	/**
	 * Append css text for the border-bottom shorthand property.
	 * <p>
	 * No !important string is ever appended.
	 * </p>
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param onlyUnused
	 *            if true, no text will be appended unless at least one unused value will be
	 *            used.
	 * @return <code>true</code> if text was appended, meaning that at least one unused value remained,
	 *         <code>false</code> otherwise.
	 */
	private boolean appendBorderBottomText(StringBuilder buf, boolean onlyUnused) {
		return appendBorderSideText(buf, "border-bottom:", "border-bottom-width", "border-bottom-style",
				"border-bottom-color", getBottomKeywordState(), onlyUnused);
	}

	/**
	 * Append css text for the border-left shorthand property.
	 * <p>
	 * No !important string is ever appended.
	 * </p>
	 * 
	 * @param buf
	 *            the buffer to append to.
	 * @param onlyUnused
	 *            if true, no text will be appended unless at least one unused value shall be
	 *            used.
	 * @return <code>true</code> if text was appended, meaning that at least one unused value remained,
	 *         <code>false</code> otherwise.
	 */
	private boolean appendBorderLeftText(StringBuilder buf, boolean onlyUnused) {
		return appendBorderSideText(buf, "border-left:", "border-left-width", "border-left-style", "border-left-color",
				getLeftKeywordState(), onlyUnused);
	}

	private boolean appendBorderSideText(StringBuilder buf, String bSidePtyStr, String bWidthPty, String bStylePty,
			String bColorPty, byte state, boolean appendOnlyUnused) {
		if (state != -1
				&& (unusedSet.contains(bWidthPty) || unusedSet.contains(bStylePty) || unusedSet.contains(bColorPty))) {
			buf.append(bSidePtyStr);
			if (state == 0) {
				boolean appended = false;
				if (!appendOnlyUnused || unusedSet.contains(bWidthPty)) {
					appended = appendValueIfNotInitial(buf, bWidthPty, false);
				}
				if (!appendOnlyUnused || unusedSet.contains(bStylePty)) {
					appended = appendValueIfNotInitial(buf, bStylePty, appended);
				}
				if (!appendOnlyUnused || unusedSet.contains(bColorPty)) {
					appended = appendValueIfNotInitial(buf, bColorPty, appended);
				}
				if (!appended) {
					buf.append("none");
				}
			} else if (state == 1) {
				buf.append("inherit");
			} else {
				buf.append("unset");
			}
			unusedSet.remove(bWidthPty);
			unusedSet.remove(bStylePty);
			unusedSet.remove(bColorPty);
			return true;
		}
		return false;
	}

	/**
	 * Get the keyword state of top side properties.
	 * 
	 * @return 0 if all properties are non-keyword, 1 if all are inherit, 2 if unset, -1 if
	 *         mixed.
	 */
	private byte getTopKeywordState() {
		StyleValue width = getCSSValue("border-top-width");
		StyleValue style = getCSSValue("border-top-style");
		StyleValue color = getCSSValue("border-top-color");
		return getSideKeywordState(width, style, color);
	}

	/**
	 * Get the keyword state of right side properties.
	 * 
	 * @return 0 if all properties are non-keyword, 1 if all are inherit, 2 if unset, -1 if
	 *         mixed.
	 */
	private byte getRightKeywordState() {
		StyleValue width = getCSSValue("border-right-width");
		StyleValue style = getCSSValue("border-right-style");
		StyleValue color = getCSSValue("border-right-color");
		return getSideKeywordState(width, style, color);
	}

	/**
	 * Get the keyword state of bottom side properties.
	 * 
	 * @return 0 if all properties are non-keyword, 1 if all are inherit, 2 if unset, -1 if
	 *         mixed.
	 */
	private byte getBottomKeywordState() {
		StyleValue width = getCSSValue("border-bottom-width");
		StyleValue style = getCSSValue("border-bottom-style");
		StyleValue color = getCSSValue("border-bottom-color");
		return getSideKeywordState(width, style, color);
	}

	/**
	 * Get the keyword state of left side properties.
	 * 
	 * @return 0 if all properties are non-keyword, 1 if all are inherit, 2 if unset, -1 if
	 *         mixed.
	 */
	private byte getLeftKeywordState() {
		StyleValue width = getCSSValue("border-left-width");
		StyleValue style = getCSSValue("border-left-style");
		StyleValue color = getCSSValue("border-left-color");
		return getSideKeywordState(width, style, color);
	}

	/**
	 * Get the keyword state of a border side.
	 * 
	 * @param width
	 * @param style
	 * @param color
	 * @return 0 if all properties are non-keyword, 1 if all are inherit, 2 if unset, -1 if
	 *         mixed.
	 */
	private byte getSideKeywordState(StyleValue width, StyleValue style, StyleValue color) {
		byte state;
		byte keyword_state_width = keywordState(width);
		byte keyword_state_style = keywordState(style);
		byte keyword_state_color = keywordState(color);
		int keyword_state = keyword_state_width + keyword_state_style + keyword_state_color;
		if (keyword_state == 0) {
			state = 0;
		} else if (keyword_state == 3) {
			state = 1;
		} else if (keyword_state == 15) {
			state = 2;
		} else {
			state = -1;
		}
		return state;
	}

	private boolean isLiveProperty(String property, byte live_state) {
		return isLiveState(getCSSValue(property), live_state);
	}

	private boolean isLiveState(StyleValue value, byte live_state) {
		return keywordState(value) == live_state;
	}

	private boolean equivalentProperties(String property1, String property2, byte live_state) {
		StyleValue value1 = getCSSValue(property1);
		StyleValue value2 = getCSSValue(property2);
		return valueEquals(value1, value2) || keywordState(value1) != live_state || keywordState(value2) != live_state;
	}

	private class PropertyValueScore {

		final Set<String> declaredSet;

		final ShorthandPropertyState widthState;
		final ShorthandPropertyState styleState;
		final ShorthandPropertyState colorState;

		PropertyValueScore(Set<String> declaredSet) {
			super();
			widthState = new ShorthandPropertyState();
			styleState = new ShorthandPropertyState();
			colorState = new ShorthandPropertyState();
			this.declaredSet = declaredSet;
		}

		public ShorthandPropertyState getWidthState() {
			return widthState;
		}

		public ShorthandPropertyState getStyleState() {
			return styleState;
		}

		public ShorthandPropertyState getColorState() {
			return colorState;
		}

		/**
		 * Score for finding same values in the width properties.
		 * 
		 * @return 21 if all width values are equal; 20 if right equals to left, and top equals
		 *         bottom; 17 if left equals to right and top; 16 if right equals to left (bottom
		 *         may be equal to them); 5 if top equals bottom and left; 4 if top equals bottom
		 *         but other values are different (right could be equal to them), 1 if left and
		 *         top are the same but other values differ (right could be equal to bottom), 0 if
		 *         3 or 4 values are different (right could be equal to bottom or top); or -1 if
		 *         an incompatible combination of CSS keywords was found.
		 */
		public int getSameWidthScore() {
			return widthState.score;
		}

		/**
		 * Score for finding same values in the style properties.
		 * 
		 * @return 21 if all style values are equal; 20 if right equals to left, and top equals
		 *         bottom; 17 if left equals to right and top; 16 if right equals to left (bottom
		 *         may be equal to them); 5 if top equals bottom and left; 4 if top equals bottom
		 *         but other values are different (right could be equal to them), 1 if left and
		 *         top are the same but other values differ (right could be equal to bottom), 0 if
		 *         3 or 4 values are different (right could be equal to bottom or top); or -1 if
		 *         an incompatible combination of CSS keywords was found.
		 */
		public int getSameStyleScore() {
			return styleState.score;
		}

		/**
		 * Score for finding same values in the color properties.
		 * 
		 * @return 21 if all color values are equal; 20 if right equals to left, and top equals
		 *         bottom; 17 if left equals to right and top; 16 if right equals to left (bottom
		 *         may be equal to them); 5 if top equals bottom and left; 4 if top equals bottom
		 *         but other values are different (right could be equal to them), 1 if left and
		 *         top are the same but other values differ (right could be equal to bottom), 0 if
		 *         3 or 4 values are different (right could be equal to bottom or top); or -1 if
		 *         an incompatible combination of CSS keywords was found.
		 */
		public int getSameColorScore() {
			return colorState.score;
		}

		private void score() {
			widthState.sameValueScore("border-top-width", "border-right-width", "border-bottom-width",
					"border-left-width");
			styleState.sameValueScore("border-top-style", "border-right-style", "border-bottom-style",
					"border-left-style");
			colorState.sameValueScore("border-top-color", "border-right-color", "border-bottom-color",
					"border-left-color");
		}

		public void score(PropertyCount counter) {
			if (counter.countBWidth == 4) {
				widthState.sameValueScore("border-top-width", "border-right-width", "border-bottom-width",
						"border-left-width");
			}
			if (counter.countBStyle == 4) {
				styleState.sameValueScore("border-top-style", "border-right-style", "border-bottom-style",
						"border-left-style");
			}
			if (counter.countBColor == 4) {
				colorState.sameValueScore("border-top-color", "border-right-color", "border-bottom-color",
						"border-left-color");
			}
		}

		boolean hasMixedStates() {
			return getSameWidthScore() == -1 || getSameStyleScore() == -1 || getSameColorScore() == -1;
		}

		private int getScore(byte live_state) {
			int score = 0;
			if (!hasMixedStates()) {
				score = 0;
				if (widthState.keyword_state == live_state) {
					score += getSameWidthScore();
				}
				if (styleState.keyword_state == live_state) {
					score += getSameStyleScore();
				}
				if (colorState.keyword_state == live_state) {
					score += getSameColorScore();
				}
			} else {
				score = -1;
			}
			return score;
		}

		private void setEquivalentScores() {
			widthState.equivalentValueScore("border-top-width", "border-right-width", "border-bottom-width",
					"border-left-width");
			styleState.equivalentValueScore("border-top-style", "border-right-style", "border-bottom-style",
					"border-left-style");
			colorState.equivalentValueScore("border-top-color", "border-right-color", "border-bottom-color",
					"border-left-color");
		}

		private Set<String> getEquivWidthSet(int propertyScore) {
			Set<String> equivWidthSet = new HashSet<String>();
			switch (propertyScore) {
			case 21:
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-left-width");
				equivWidthSet.add("border-bottom-width");
				break;
			case 17:
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-left-width");
				break;
			case 16: // right = left
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-left-width");
				break;
			case 15:
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-left-width");
				equivWidthSet.add("border-bottom-width");
				break;
			case 5:
				equivWidthSet.add("border-left-width");
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-bottom-width");
				break;
			case 4:
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-bottom-width");
				break;
			case 3:
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-right-width");
				equivWidthSet.add("border-bottom-width");
				break;
			case 1:
				equivWidthSet.add("border-top-width");
				equivWidthSet.add("border-left-width");
				break;
			}
			return equivWidthSet;
		}

		private Set<String> getEquivStyleSet(int propertyScore) {
			Set<String> equivStyleSet = new HashSet<String>();
			switch (propertyScore) {
			case 21:
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-left-style");
				equivStyleSet.add("border-bottom-style");
				break;
			case 17:
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-left-style");
				break;
			case 16: // right = left
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-left-style");
				break;
			case 15:
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-left-style");
				equivStyleSet.add("border-bottom-style");
				break;
			case 5:
				equivStyleSet.add("border-left-style");
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-bottom-style");
				break;
			case 4:
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-bottom-style");
				break;
			case 3:
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-right-style");
				equivStyleSet.add("border-bottom-style");
				break;
			case 1:
				equivStyleSet.add("border-top-style");
				equivStyleSet.add("border-left-style");
				break;
			}
			return equivStyleSet;
		}

		private Set<String> getEquivColorSet(int propertyScore) {
			Set<String> equivColorSet = new HashSet<String>();
			switch (propertyScore) {
			case 21:
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-left-color");
				equivColorSet.add("border-bottom-color");
				break;
			case 17:
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-left-color");
				break;
			case 16: // right = left
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-left-color");
				break;
			case 15:
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-left-color");
				equivColorSet.add("border-bottom-color");
				break;
			case 5:
				equivColorSet.add("border-left-color");
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-bottom-color");
				break;
			case 4:
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-bottom-color");
				break;
			case 3:
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-right-color");
				equivColorSet.add("border-bottom-color");
				break;
			case 1:
				equivColorSet.add("border-top-color");
				equivColorSet.add("border-left-color");
				break;
			}
			return equivColorSet;
		}

		private class ShorthandPropertyState {

			private int score = 0;
			private int keyword_state = 0;
			private byte best_state = 0;

			byte getBestState() {
				return best_state;
			}

			ShorthandPropertyState() {
				super();
			}

			/**
			 * Score for finding same values in the given property set.
			 * 
			 * @return 21 if all subproperty values are equal; 20 if right equals to left, and top
			 *         equals bottom; 17 if left equals to right and top; 16 if right equals to left
			 *         (bottom may be equal to them); 5 if top equals bottom and left; 4 if top equals
			 *         bottom but other values are different (right could be equal to them), 1 if left
			 *         and top are the same but other values differ (right could be equal to bottom),
			 *         0 if 3 or 4 values are different (right could be equal to bottom or top); or -1
			 *         if an incompatible combination of CSS keywords was found.
			 */
			private int sameValueScore(String ptyTop, String ptyRight, String ptyBottom, String ptyLeft) {
				StyleValue top = getCSSValue(ptyTop);
				StyleValue bottom = getCSSValue(ptyBottom);
				StyleValue left = getCSSValue(ptyLeft);
				StyleValue right = getCSSValue(ptyRight);
				byte keyword_state_top = keywordState(top);
				byte keyword_state_bottom = keywordState(bottom);
				byte keyword_state_left = keywordState(left);
				byte keyword_state_right = keywordState(right);
				score = 0;
				if (valueEquals(left, right) || (!declaredSet.contains(ptyLeft) && !declaredSet.contains(ptyRight))) {
					score = 16;
				}
				if (valueEquals(top, bottom) || (!declaredSet.contains(ptyTop) && !declaredSet.contains(ptyBottom))) {
					score += 4;
				}
				if (valueEquals(top, left) || (!declaredSet.contains(ptyTop) && !declaredSet.contains(ptyLeft))) {
					score += 1;
				} else if (!declaredSet.contains(ptyLeft) && score == 16) {
					score += 1;
				} else if (!declaredSet.contains(ptyTop) && (score == 20 || score == 4)) {
					score += 1;
				}
				keyword_state = keyword_state_top + keyword_state_bottom + keyword_state_left + keyword_state_right;
				if (keyword_state != 0 && keyword_state != 4 && keyword_state != 20) {
					score = -1;
				} else {
					best_state = (byte) (keyword_state / 4);
				}
				return score;
			}

			/**
			 * Score for finding same values in the given property set, disregarding CSS-wide
			 * keywords.
			 * 
			 * @return 21 if all subproperty values are equal; 20 if right equals to left, and top
			 *         equals bottom; 17 if left equals to right and top; 16 if right equals to left
			 *         (bottom may be equal to them); 5 if top equals bottom and left; 4 if top equals
			 *         bottom but other values are different (right could be equal to them), 1 if left
			 *         and top are the same but other values differ (right could be equal to bottom),
			 *         0 if 3 or 4 values are different (right could be equal to bottom or top).
			 */
			private int equivalentValueScore(String ptyTop, String ptyRight, String ptyBottom, String ptyLeft) {
				StyleValue top = getCSSValue(ptyTop);
				StyleValue bottom = getCSSValue(ptyBottom);
				StyleValue left = getCSSValue(ptyLeft);
				StyleValue right = getCSSValue(ptyRight);
				byte keyword_state_top = keywordState(top);
				byte keyword_state_bottom = keywordState(bottom);
				byte keyword_state_left = keywordState(left);
				byte keyword_state_right = keywordState(right);
				keyword_state = keyword_state_top + keyword_state_bottom + keyword_state_left + keyword_state_right;
				best_state = 0;
				int inherit_count = keyword_state % 5;
				if (inherit_count == 3 || inherit_count == 4) {
					best_state = 1;
				} else if (keyword_state > 10) {
					best_state = 5;
				}
				score = 0;
				if (valueEquals(left, right) || keyword_state_left != best_state || keyword_state_right != best_state) {
					score += 16;
				}
				if (valueEquals(top, bottom) || keyword_state_top != best_state || keyword_state_bottom != best_state) {
					score += 4;
				}
				if (valueEquals(top, left) || keyword_state_top != best_state || keyword_state_left != best_state) {
					score++;
				}
				return score;
			}

		}

	}

	/**
	 * Checks which shorthands can be built, and build the 'unused' set
	 */
	private class PropertyCount {
		int countBTop = 0, countBRight = 0, countBBottom = 0, countBLeft = 0;
		int countBColor = 0, countBStyle = 0, countBWidth = 0;

		PropertyCount() {
			super();
		}

		private void count(Set<String> declaredSet, boolean important) {
			unusedSet.clear();
			unusedSet.addAll(declaredSet); // Build the 'unused' set
			if (declaredSet.contains("border-top-width")) {
				countBTop++;
				countBWidth++;
			}
			if (declaredSet.contains("border-top-style")) {
				countBTop++;
				countBStyle++;
			}
			if (declaredSet.contains("border-top-color")) {
				countBTop++;
				countBColor++;
			}
			if (declaredSet.contains("border-right-width")) {
				countBRight++;
				countBWidth++;
			}
			if (declaredSet.contains("border-right-style")) {
				countBRight++;
				countBStyle++;
			}
			if (declaredSet.contains("border-right-color")) {
				countBRight++;
				countBColor++;
			}
			if (declaredSet.contains("border-bottom-width")) {
				countBBottom++;
				countBWidth++;
			}
			if (declaredSet.contains("border-bottom-style")) {
				countBBottom++;
				countBStyle++;
			}
			if (declaredSet.contains("border-bottom-color")) {
				countBBottom++;
				countBColor++;
			}
			if (declaredSet.contains("border-left-width")) {
				countBLeft++;
				countBWidth++;
			}
			if (declaredSet.contains("border-left-style")) {
				countBLeft++;
				countBStyle++;
			}
			if (declaredSet.contains("border-left-color")) {
				countBLeft++;
				countBColor++;
			}
			// If the set is not important, we could still use some shorthands
			if (!important) {
				BaseCSSStyleDeclaration style = getParentStyle();
				if (isShadowed(style, declaredSet, "border-top-width")) {
					countBTop++;
					if (countBWidth != 0)
						countBWidth++;
				}
				if (isShadowed(style, declaredSet, "border-top-style")) {
					countBTop++;
					if (countBStyle != 0)
						countBStyle++;
				}
				if (isShadowed(style, declaredSet, "border-top-color")) {
					countBTop++;
					if (countBColor != 0)
						countBColor++;
				}
				if (isShadowed(style, declaredSet, "border-right-width")) {
					countBRight++;
					if (countBWidth != 0)
						countBWidth++;
				}
				if (isShadowed(style, declaredSet, "border-right-style")) {
					countBRight++;
					if (countBStyle != 0)
						countBStyle++;
				}
				if (isShadowed(style, declaredSet, "border-right-color")) {
					countBRight++;
					if (countBColor != 0)
						countBColor++;
				}
				if (isShadowed(style, declaredSet, "border-bottom-width")) {
					countBBottom++;
					if (countBWidth != 0)
						countBWidth++;
				}
				if (isShadowed(style, declaredSet, "border-bottom-style")) {
					countBBottom++;
					if (countBStyle != 0)
						countBStyle++;
				}
				if (isShadowed(style, declaredSet, "border-bottom-color")) {
					countBBottom++;
					if (countBColor != 0)
						countBColor++;
				}
				if (isShadowed(style, declaredSet, "border-left-width")) {
					countBLeft++;
					if (countBWidth != 0)
						countBWidth++;
				}
				if (isShadowed(style, declaredSet, "border-left-style")) {
					countBLeft++;
					if (countBStyle != 0)
						countBStyle++;
				}
				if (isShadowed(style, declaredSet, "border-left-color")) {
					countBLeft++;
					if (countBColor != 0)
						countBColor++;
				}
			}

		}

		/*
		 * Is the given property shadowed by an important one ?
		 */
		private boolean isShadowed(BaseCSSStyleDeclaration style, Set<String> declaredSet, String property) {
			return !declaredSet.contains(property) && style.isPropertySet(property);
		}
	}
}
