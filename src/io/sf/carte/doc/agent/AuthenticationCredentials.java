/*

 Copyright (c) 2005-2023, Carlos Amengual.

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

	byte HTTP_BASIC_AUTH = 1;

	String getRealm();

	Principal getLoginPrincipal();

	void setLoginPrincipal(Principal loginPrincipal);

	String getPassword();

	void setPassword(String password);

	void setAuthType(byte authtype);

	byte getAuthType();

}
