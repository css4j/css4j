/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

/**
 * Origin policy.
 * <p>
 *
 * @author Carlos Amengual
 */
public interface OriginPolicy {

	boolean isTopLevelSuffix(String possibleTld);

	String domainFromHostname(String host);

}
