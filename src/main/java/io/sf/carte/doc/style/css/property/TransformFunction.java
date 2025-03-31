/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;
import io.sf.carte.doc.style.css.CSSTransformFunction;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.TransformFunctions;

/**
 * Transform function.
 */
public class TransformFunction extends FunctionValue implements CSSTransformFunction {

	private static final long serialVersionUID = 1L;

	private TransformFunctions functionId;

	public TransformFunction(TransformFunctions functionId) {
		super(Type.TRANSFORM_FUNCTION);
		this.functionId = functionId;
	}

	protected TransformFunction(TransformFunction copied) {
		super(copied);
	}

	@Override
	public TransformFunctions getFunction() {
		return functionId;
	}

	/**
	 * Gives the index of this transform function.
	 * 
	 * @return the function index.
	 */
	@Override
	public int getFunctionIndex() {
		return functionId.ordinal();
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case transformFunction:
		case transformList:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	public TransformFunction clone() {
		return new TransformFunction(this);
	}

}
