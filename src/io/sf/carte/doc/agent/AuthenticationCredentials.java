/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.security.Principal;

/**
 * Authentication credentials for user agents.
 */
public interface AuthenticationCredentials {

	public static final byte HTTP_BASIC_AUTH = 1;

	public String getRealm();

	public Principal getLoginPrincipal();

	public void setLoginPrincipal(Principal loginPrincipal);

	public String getPassword();

	public void setPassword(String password);

	public void setAuthType(byte authtype);

	public byte getAuthType();

}
