/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

/**
 * Media query helper.
 */
public class MediaQueryHelper {

	public static void appendFeatureText(String featureName, byte rangeType, String value1, String value2,
			StringBuilder buf) {
		buf.append('(');
		switch (rangeType) {
		case MediaFeaturePredicate.FEATURE_PLAIN:
		case MediaFeaturePredicate.FEATURE_EQ:
			appendFeatureName(featureName, buf);
			if (value1 != null) {
				buf.append(": ");
				buf.append(value1);
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT:
			appendFeatureName(featureName, buf);
			buf.append(" < ");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			appendFeatureName(featureName, buf);
			buf.append(" <= ");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			appendFeatureName(featureName, buf);
			buf.append(" > ");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			appendFeatureName(featureName, buf);
			buf.append(" >= ");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			buf.append(value1);
			buf.append(" < ");
			appendFeatureName(featureName, buf);
			buf.append(" <= ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			buf.append(value1);
			buf.append(" < ");
			appendFeatureName(featureName, buf);
			buf.append(" < ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			buf.append(value1);
			buf.append(" <= ");
			appendFeatureName(featureName, buf);
			buf.append(" < ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			buf.append(value1);
			buf.append(" <= ");
			appendFeatureName(featureName, buf);
			buf.append(" <= ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			buf.append(value1);
			buf.append(" > ");
			appendFeatureName(featureName, buf);
			buf.append(" > ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			buf.append(value1);
			buf.append(" >= ");
			appendFeatureName(featureName, buf);
			buf.append(" > ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			buf.append(value1);
			buf.append(" > ");
			appendFeatureName(featureName, buf);
			buf.append(" >= ");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			buf.append(value1);
			buf.append(" >= ");
			appendFeatureName(featureName, buf);
			buf.append(" >= ");
			buf.append(value2);
			break;
		}
		buf.append(')');
	}

	public static void appendMinifiedFeatureText(String featureName, byte rangeType, String value1, String value2,
			StringBuilder buf) {
		buf.append('(');
		switch (rangeType) {
		case MediaFeaturePredicate.FEATURE_PLAIN:
		case MediaFeaturePredicate.FEATURE_EQ:
			appendFeatureName(featureName, buf);
			if (value1 != null) {
				buf.append(':');
				buf.append(value1);
			}
			break;
		case MediaFeaturePredicate.FEATURE_LT:
			appendFeatureName(featureName, buf);
			buf.append('<');
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_LE:
			appendFeatureName(featureName, buf);
			buf.append("<=");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_GT:
			appendFeatureName(featureName, buf);
			buf.append('>');
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_GE:
			appendFeatureName(featureName, buf);
			buf.append(">=");
			buf.append(value1);
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LE:
			buf.append(value1);
			buf.append('<');
			appendFeatureName(featureName, buf);
			buf.append("<=");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LT_AND_LT:
			buf.append(value1);
			buf.append('<');
			appendFeatureName(featureName, buf);
			buf.append('<');
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LT:
			buf.append(value1);
			buf.append("<=");
			appendFeatureName(featureName, buf);
			buf.append('<');
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_LE_AND_LE:
			buf.append(value1);
			buf.append("<=");
			appendFeatureName(featureName, buf);
			buf.append("<=");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GT:
			buf.append(value1);
			buf.append('>');
			appendFeatureName(featureName, buf);
			buf.append('>');
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GT:
			buf.append(value1);
			buf.append(">=");
			appendFeatureName(featureName, buf);
			buf.append('>');
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GT_AND_GE:
			buf.append(value1);
			buf.append('>');
			appendFeatureName(featureName, buf);
			buf.append(">=");
			buf.append(value2);
			break;
		case MediaFeaturePredicate.FEATURE_GE_AND_GE:
			buf.append(value1);
			buf.append(">=");
			appendFeatureName(featureName, buf);
			buf.append(">=");
			buf.append(value2);
			break;
		}
		buf.append(')');
	}

	private static void appendFeatureName(String featureName, StringBuilder buf) {
		buf.append(ParseHelper.escape(featureName));
	}

}
