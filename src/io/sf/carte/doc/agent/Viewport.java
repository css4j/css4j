/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import io.sf.carte.doc.style.css.StyleDatabase;

/**
 * Represents a viewport defined as per the CSS specifications.
 * <p>
 * The <code>Viewport</code>, together with the {@link CSSCanvas}, has a similar role to
 * the W3C's <code>Screen</code>, with the big difference that some of the information
 * that <code>Screen</code> provides is in fact available from the {@link StyleDatabase}.
 * </p>
 * 
 * @deprecated
 * @see io.sf.carte.doc.style.css.Viewport
 */
@Deprecated
public interface Viewport extends io.sf.carte.doc.style.css.Viewport {
}
