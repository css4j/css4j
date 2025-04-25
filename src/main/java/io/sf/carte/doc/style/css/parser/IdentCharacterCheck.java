/*

 Copyright (c) 2017-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.uparser.TokenProducer;
import io.sf.carte.uparser.TokenProducer3.CharacterCheck;
import io.sf.carte.uparser.TokenProducer3.SequenceParser;

/**
 * A character check that whitelists characters allowed in identifiers.
 */
class IdentCharacterCheck implements CharacterCheck {

	/**
	 * Constructor.
	 */
	public IdentCharacterCheck() {
		super();
	}

	@Override
	public boolean isAllowedCharacter(int codePoint, SequenceParser<? extends Exception> parser) {
		return codePoint == TokenProducer.CHAR_HYPHEN_MINUS
				|| codePoint == TokenProducer.CHAR_LOW_LINE;
	}

}
