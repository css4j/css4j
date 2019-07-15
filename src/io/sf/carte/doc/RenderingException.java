/*

 Copyright (c) 1998-2017, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc;

/**
 * A problem was found while rendering a document.
 * 
 * @author Carlos Amengual
 * @version 1.01
 */

public class RenderingException extends DocumentException {

	private static final long serialVersionUID = 2L;

	public RenderingException() {
		super();
	}

	public RenderingException(String message) {
		super(message);
	}

	public RenderingException(String message, Throwable cause) {
		super(message, cause);
	}

	public RenderingException(Throwable cause) {
		super(cause);
	}

}