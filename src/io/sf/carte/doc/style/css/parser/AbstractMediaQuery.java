/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.MediaQuery;

abstract public class AbstractMediaQuery implements MediaQuery, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final HashSet<String> rangeFeatureSet;

	static {
		final String[] rangeFeatures = { "aspect-ratio", "color", "color-index", "height", "monochrome", "resolution",
				"width" };
		rangeFeatureSet = new HashSet<String>(rangeFeatures.length);
		Collections.addAll(rangeFeatureSet, rangeFeatures);
	}

	private String mediaType = null;

	private boolean negativeQuery = false;

	private boolean onlyPrefix = false;

	private BooleanCondition predicate = null;

	protected AbstractMediaQuery() {
		super();
	}

	private static String escapeIdentifier(String medium) {
		return ParseHelper.escape(medium);
	}

	protected static boolean isRangeFeature(String string) {
		return rangeFeatureSet.contains(string);
	}

	@Override
	public BooleanCondition getCondition() {
		return predicate;
	}

	@Override
	public String getMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (predicate != null) {
			predicate.appendText(buf);
		} else if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		return buf.toString();
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

	@Override
	public String getMinifiedMedia() {
		StringBuilder buf = new StringBuilder(32);
		if (negativeQuery) {
			buf.append("not ");
		} else if (onlyPrefix) {
			buf.append("only ");
		}
		if (predicate != null) {
			predicate.appendMinifiedText(buf);
		} else if (mediaType != null) {
			buf.append(escapeIdentifier(mediaType));
		}
		return buf.toString();
	}

	@Override
	public boolean isAllMedia() {
		return (mediaType == null || "all".equalsIgnoreCase(mediaType)) && !negativeQuery && predicate == null;
	}

	@Override
	public boolean isNegated() {
		return negativeQuery;
	}

	@Override
	public boolean isNotAllMedia() {
		return mediaType != null && "all".equalsIgnoreCase(mediaType) && negativeQuery && predicate == null;
	}

	/**
	 * Check whether the given query is partially or totaly contained by this one.
	 * <p>
	 * If query A matches B, then if a medium matches B it will also match A. The
	 * opposite may not be true.
	 * 
	 * @param other the other query to check against.
	 * @return <code>true</code> if the other query is partially or totally
	 *         contained by this one.
	 */
	protected boolean matches(AbstractMediaQuery other) {
		if (other.isNotAllMedia()) {
			return false;
		}
		boolean isAllMedium = (mediaType == null || "all".equals(mediaType));
		if (negativeQuery) {
			if (isAllMedium) {
				if (predicate == null) {
					return false;
				}
			} else if (mediaType.equals(other.mediaType)) {
				if (!other.negativeQuery) {
					return false;
				}
			} else if (other.negativeQuery) {
				return false;
			}
		} else if (!isAllMedium && (other.negativeQuery || !mediaType.equals(other.mediaType))) {
			return false;
		}
		if (predicate == null) {
			return true;
		} else if (other.predicate == null) {
			return false;
		}
		byte negatedQuery;
		if (negativeQuery) {
			if (!other.negativeQuery) {
				negatedQuery = 1;
			} else {
				negatedQuery = 0;
			}
		} else if (other.negativeQuery) {
			negatedQuery = 2;
		} else {
			negatedQuery = 0;
		}
		return matches(predicate, other.predicate, negatedQuery) != 0;
	}

	/**
	 * Determine whether the two conditions match.
	 * 
	 * @param condition      the first condition.
	 * @param otherCondition the second consdition.
	 * @param negatedQuery   <code>0</code> if it is a direct match, <code>1</code>
	 *                       if the this predicate is reverse (negated),
	 *                       <code>2</code> if the given predicate is negated,
	 *                       <code>3</code> if both are negated.
	 * @return <code>1</code> if they match, <code>0</code> if don't, <code>2</code>
	 *         if the match should not be taken into account.
	 */
	abstract protected byte matches(BooleanCondition condition, BooleanCondition otherCondition, byte negatedQuery);

	public boolean matches(String medium, CSSCanvas canvas) {
		if (mediaType != null) {
			if (mediaType.equals(medium)) {
				if (negativeQuery) {
					return false;
				}
			} else {
				if (!negativeQuery) {
					return false;
				}
			}
		}
		if (predicate != null) {
			if (canvas == null) {
				return false;
			}
			return matchesCondition(predicate, canvas);
		}
		return true;
	}

	private boolean matchesCondition(BooleanCondition condition, CSSCanvas canvas) {
		switch (condition.getType()) {
		case AND:
			Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (!matchesCondition(subcond, canvas)) {
					return false;
				}
			}
			return true;
		case NOT:
			return !matchesCondition(condition.getNestedCondition(), canvas);
		case OR:
			it = condition.getSubConditions().iterator();
			while (it.hasNext()) {
				BooleanCondition subcond = it.next();
				if (matchesCondition(subcond, canvas)) {
					return true;
				}
			}
			break;
		default:
			return matchesPredicate(condition, canvas);
		}
		return false;
	}

	protected abstract boolean matchesPredicate(BooleanCondition condition, CSSCanvas canvas);

	protected void setFeaturePredicate(BooleanCondition predicate) {
		this.predicate = predicate;
	}

	protected void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	protected void setNegative(boolean negative) {
		this.negativeQuery = negative;
	}

	protected void setOnlyPrefix(boolean only) {
		this.onlyPrefix = only;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMediaQuery other = (AbstractMediaQuery) obj;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (negativeQuery != other.negativeQuery)
			return false;
		if (onlyPrefix != other.onlyPrefix) // should the 'only' prefix be ignored?
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (other.predicate == null) {
			return false;
		} else if (!predicate.equals(other.predicate)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + (negativeQuery ? 1231 : 1237);
		result = prime * result + (onlyPrefix ? 1231 : 1237);
		if (predicate != null) {
			result = prime * result + predicate.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return getMedia();
	}

}
