/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

/**
 * Origin policy.
 */
public interface OriginPolicy {

	boolean isTopLevelSuffix(String possibleTld);

	/**
	 * Obtain the domain from the host name.
	 * 
	 * @param host the lower case host name.
	 * @return the domain.
	 */
	String domainFromHostname(String host);

}
