/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.parser.ParseHelper;

class MediaQuery {

	public static final byte FEATURE_PLAIN = 0;
	public static final byte FEATURE_EQ = 1; // =
	public static final byte FEATURE_LT = 2; // <
	public static final byte FEATURE_LE = 3; // <=
	public static final byte FEATURE_GT = 4; // >
	public static final byte FEATURE_GE = 5; // >=
	public static final byte FEATURE_LT_AND_LE = 26; // a < foo <= b
	public static final byte FEATURE_LE_AND_LT = 19; // a <= foo < b
	public static final byte FEATURE_GE_AND_GT = 37; // a >= foo > b
	public static final byte FEATURE_GT_AND_GE = 44; // a > foo >= b

	private String mediaType = null;

	private boolean negativeQuery = false;

	private boolean onlyPrefix = false;

	private final LinkedHashMap<String, ExtendedCSSValue> featureList;

	private final HashMap<String, FeatureRange> featureRange;

	public MediaQuery() {
		super();
		featureList = new LinkedHashMap<String, ExtendedCSSValue>();
		featureRange = new HashMap<String, FeatureRange>();
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setNegative(boolean negative) {
		this.negativeQuery = negative;
	}

	public void setOnlyPrefix(boolean only) {
		this.onlyPrefix = only;
	}

	public boolean matches(String medium, CSSCanvas canvas) {
		if (mediaType != null) {
			if (mediaType.equals(medium)) {
				if(negativeQuery) {
					return false;
				}
			} else {
				if(!negativeQuery) {
					return false;
				}
			}
		}
		if (!featureList.isEmpty()) {
			if (canvas == null) {
				return false;
			}
			Iterator<Entry<String, ExtendedCSSValue>> it = featureList.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, ExtendedCSSValue> entry = it.next();
				String feature = entry.getKey();
				CSSValue value = entry.getValue();
				FeatureRange range = featureRange.get(feature);
				byte type;
				if (range == null) {
					type = FEATURE_PLAIN;
				} else {
					type = range.rangeType;
				}
				if (type == FEATURE_PLAIN || type == FEATURE_EQ) {
					if (!canvas.supports(feature, value)) {
						return false;
					}
				} else {
					CSSValue featured = canvas.getFeatureValue(feature);
					if (value == null || value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE
							|| featured.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE
							|| value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
						return false;
					}
					short primitype = ((CSSPrimitiveValue) featured).getPrimitiveType();
					float fval = ((CSSPrimitiveValue) featured).getFloatValue(primitype);
					float fval1, fval2 = 0;
					try {
						fval1 = ((CSSPrimitiveValue) value).getFloatValue(primitype);
					} catch (DOMException e) {
						return false;
					}
					if (type >=6) {
						if (range.value == null || range.value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
							return false;
						}
						try {
							fval2 = ((CSSPrimitiveValue) range.value).getFloatValue(primitype);
						} catch (DOMException e) {
							return false;
						}
					}
					switch (type) {
					case FEATURE_LT:
						if (fval1 >= fval) {
							return false;
						}
						break;
					case FEATURE_LE:
						if (fval1 > fval) {
							return false;
						}
						break;
					case FEATURE_GT:
						if (fval1 <= fval) {
							return false;
						}
						break;
					case FEATURE_GE:
						if (fval1 < fval) {
							return false;
						}
						break;
					case FEATURE_LE_AND_LT:
						if (fval1 > fval || fval >= fval2) {
							return false;
						}
						break;
					case FEATURE_LT_AND_LE:
						if (fval1 >= fval || fval > fval2) {
							return false;
						}
						break;
					case FEATURE_GE_AND_GT:
						if (fval1 < fval || fval <= fval2) {
							return false;
						}
						break;
					case FEATURE_GT_AND_GE:
						if (fval1 <= fval || fval < fval2) {
							return false;
						}
						break;
					default:
						return false;
					}
				}
			}
		}
		return true;
	}

	public String getMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		if (!featureList.isEmpty()) {
			Iterator<Entry<String, ExtendedCSSValue>> it = featureList.entrySet().iterator();
			if (mediaType != null) {
				buf.append(" and (");
			} else {
				buf.append('(');
			}
			appendFeature(buf, it.next());
			while (it.hasNext()) {
				buf.append(" and (");
				appendFeature(buf, it.next());
			}
		}
		return buf.toString();
	}

	private void appendFeature(StringBuilder buf, Entry<String, ExtendedCSSValue> entry) {
		String feature = entry.getKey();
		CSSValue value = entry.getValue();
		FeatureRange range = featureRange.get(feature);
		byte type;
		if (range == null) {
			type = FEATURE_PLAIN;
		} else {
			type = range.rangeType;
		}
		switch (type) {
		case FEATURE_PLAIN:
			appendFeatureName(buf, feature);
			if (value != null) {
				buf.append(": ");
				buf.append(value.getCssText());
			}
			break;
		case FEATURE_EQ:
			appendFeatureName(buf, feature);
			buf.append(" = ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LT:
			appendFeatureName(buf, feature);
			buf.append(" < ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LE:
			appendFeatureName(buf, feature);
			buf.append(" <= ");
			buf.append(value.getCssText());
			break;
		case FEATURE_GT:
			appendFeatureName(buf, feature);
			buf.append(" > ");
			buf.append(value.getCssText());
			break;
		case FEATURE_GE:
			appendFeatureName(buf, feature);
			buf.append(" >= ");
			buf.append(value.getCssText());
			break;
		case FEATURE_LT_AND_LE:
			buf.append(value.getCssText());
			buf.append(" < ");
			appendFeatureName(buf, feature);
			buf.append(" <= ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_LE_AND_LT:
			buf.append(value.getCssText());
			buf.append(" <= ");
			appendFeatureName(buf, feature);
			buf.append(" < ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GE_AND_GT:
			buf.append(value.getCssText());
			buf.append(" >= ");
			appendFeatureName(buf, feature);
			buf.append(" > ");
			buf.append(range.value.getCssText());
			break;
		case FEATURE_GT_AND_GE:
			buf.append(value.getCssText());
			buf.append(" > ");
			appendFeatureName(buf, feature);
			buf.append(" >= ");
			buf.append(range.value.getCssText());
			break;
		}
		buf.append(')');
	}

	public String getMinifiedMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		if (!featureList.isEmpty()) {
			Iterator<Entry<String, ExtendedCSSValue>> it = featureList.entrySet().iterator();
			if (mediaType != null) {
				buf.append(" and(");
			} else {
				buf.append('(');
			}
			appendMinifiedFeature(buf, it.next());
			while (it.hasNext()) {
				buf.append(" and(");
				appendMinifiedFeature(buf, it.next());
			}
		}
		return buf.toString();
	}

	private void appendMinifiedFeature(StringBuilder buf, Entry<String, ExtendedCSSValue> entry) {
		String feature = entry.getKey();
		ExtendedCSSValue value = entry.getValue();
		FeatureRange range = featureRange.get(feature);
		byte type;
		if (range == null) {
			type = FEATURE_PLAIN;
		} else {
			type = range.rangeType;
		}
		switch (type) {
		case FEATURE_PLAIN:
			appendFeatureName(buf, feature);
			if (value != null) {
				buf.append(':');
				buf.append(value.getMinifiedCssText(""));
			}
			break;
		case FEATURE_EQ:
			appendFeatureName(buf, feature);
			buf.append('=');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LT:
			appendFeatureName(buf, feature);
			buf.append('<');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LE:
			appendFeatureName(buf, feature);
			buf.append("<=");
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_GT:
			appendFeatureName(buf, feature);
			buf.append('>');
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_GE:
			appendFeatureName(buf, feature);
			buf.append(">=");
			buf.append(value.getMinifiedCssText(""));
			break;
		case FEATURE_LT_AND_LE:
			buf.append(value.getCssText());
			buf.append('<');
			appendFeatureName(buf, feature);
			buf.append("<=");
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_LE_AND_LT:
			buf.append(value.getCssText());
			buf.append("<=");
			appendFeatureName(buf, feature);
			buf.append('<');
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_GE_AND_GT:
			buf.append(value.getCssText());
			buf.append(">=");
			appendFeatureName(buf, feature);
			buf.append('>');
			buf.append(range.value.getMinifiedCssText(""));
			break;
		case FEATURE_GT_AND_GE:
			buf.append(value.getCssText());
			buf.append('>');
			appendFeatureName(buf, feature);
			buf.append(">=");
			buf.append(range.value.getMinifiedCssText(""));
			break;
		}
		buf.append(')');
	}

	private void appendFeatureName(StringBuilder buf, String feature) {
		buf.append(ParseHelper.escape(feature));
	}

	static String escapeIdentifier(String medium) {
		return ParseHelper.escape(medium);
	}

	@Override
	public String toString() {
		return getMedia();
	}

	public void addFeature(String featureName, byte rangeType, ExtendedCSSValue value, ExtendedCSSValue rangevalue) {
		if (featureName == null) {
			throw new IllegalArgumentException("Null feature name");
		}
		featureList.put(featureName, value);
		if (rangeType != FEATURE_PLAIN && rangeType != FEATURE_EQ) {
			FeatureRange range = new FeatureRange(rangeType, rangevalue);
			featureRange.put(featureName, range);
		}
	}

	static class FeatureRange {
		ExtendedCSSValue value;
		byte rangeType;

		FeatureRange(byte rangeType, ExtendedCSSValue value) {
			super();
			this.rangeType = rangeType;
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result;
			if (rangeType == FEATURE_PLAIN) {
				// We handle 'feature: value' effectively as 'feature = value'
				result = FEATURE_EQ;
			} else {
				result = rangeType;
			}
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FeatureRange other = (FeatureRange) obj;
			byte efftype, otherefftype;
			if (rangeType == FEATURE_PLAIN) {
				// We handle 'feature: value' effectively as 'feature = value'
				efftype = FEATURE_EQ;
			} else {
				efftype = rangeType;
			}
			if (other.rangeType == FEATURE_PLAIN) {
				otherefftype = FEATURE_EQ;
			} else {
				otherefftype = other.rangeType;
			}
			if (efftype != otherefftype)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + (negativeQuery ? 1231 : 1237);
		result = prime * result + (onlyPrefix ? 1231 : 1237);
		if (featureList != null) {
			TreeSet<String> sorted = new TreeSet<String>(featureList.keySet());
			Iterator<String> it = sorted.iterator();
			while (it.hasNext()) {
				String feature = it.next();
				result = prime * result + feature.hashCode();
				CSSValue value = featureList.get(feature);
				if (value != null) {
					result = prime * result + value.hashCode();
					FeatureRange range;
					if (featureRange != null && (range = featureRange.get(feature)) != null) {
						result = prime * result + range.hashCode();
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaQuery other = (MediaQuery) obj;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (negativeQuery != other.negativeQuery)
			return false;
		if (onlyPrefix != other.onlyPrefix) // should the 'only' prefix be ignored?
			return false;
		if (featureList == null) {
			if (other.featureList != null)
				return false;
		} else if (other.featureList == null) {
			return false;
		} else if (featureList.size() != other.featureList.size()) {
			return false;
		} else {
			if (featureRange == null) {
				if (other.featureRange != null)
					return false;
			} else if (other.featureRange == null) {
				return false;
			}
			TreeSet<String> sorted = new TreeSet<String>(featureList.keySet());
			Iterator<String> it = sorted.iterator();
			while (it.hasNext()) {
				String feature = it.next();
				if (!other.featureList.containsKey(feature)) {
					return false;
				}
				CSSValue value = featureList.get(feature);
				CSSValue othervalue = other.featureList.get(feature);
				if (value != null) {
					if (othervalue == null)
						return false;
					if (!value.equals(othervalue))
						return false;
					FeatureRange range;
					if (featureRange != null && (range = featureRange.get(feature)) != null) {
						// We already took care that if featureRange is not null,
						// neither is other.featureRange
						if (!range.equals(other.featureRange.get(feature))) {
							return false;
						}
					}
				} else if (othervalue != null) {
					return false;
				}
			}
		}
		return true;
	}

}
