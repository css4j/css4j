/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock origin policy for tests.
 * <p>
 * 
 * @author Carlos Amengual
 */
public class MockOriginPolicy implements OriginPolicy {

	private static final List<String> topLevelSuffixes;

	static {
		topLevelSuffixes = new ArrayList<String>();
		topLevelSuffixes.add("com");
		topLevelSuffixes.add("net");
		topLevelSuffixes.add("org");
	}

	private MockOriginPolicy() {
		super();
	}

	@Override
	public boolean isTopLevelSuffix(String possibleTld) {
		return topLevelSuffixes.contains(possibleTld);
	}

	@Override
	public String domainFromHostname(String host) {
		CharSequence domain = host;
		int count = host.length();
		int idx = count - 1;
		while (idx >= 0) {
			if (domain.charAt(idx) == '.') {
				String possibleTld = domain.subSequence(idx + 1, count).toString();
				if (!isTopLevelSuffix(possibleTld)) {
					return possibleTld;
				}
			}
			idx--;
		}
		return host;
	}

	private static class MockOriginPolicyHolder {
		static final MockOriginPolicy HOLDER = new MockOriginPolicy();
	}

	public static MockOriginPolicy getInstance() {
		return MockOriginPolicyHolder.HOLDER;
	}

}