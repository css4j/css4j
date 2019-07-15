/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

/**
 * Origin policy.
 * <p>
 * 
 * @author Carlos Amengual
 */
public interface OriginPolicy {

	public boolean isTopLevelSuffix(String possibleTld);

	public String domainFromHostname(String host);

}
