/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;

public class IllegalOriginException extends IOException {

	private static final long serialVersionUID = 2L;

	public IllegalOriginException() {
	}

	public IllegalOriginException(String message) {
		super(message);
	}

	public IllegalOriginException(Throwable cause) {
		super(cause);
	}

	public IllegalOriginException(String message, Throwable cause) {
		super(message, cause);
	}

}
