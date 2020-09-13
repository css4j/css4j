/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * User agent utility methods.
 * <p>
 * This functionality was moved to {@link io.sf.carte.util.agent.AgentUtil}.
 * 
 * @author Carlos Amengual
 * @deprecated
 */
@Deprecated
public class AgentUtil {

	/**
	 * Find the character encoding in a content-type string.
	 * 
	 * @deprecated
	 * @see io.sf.carte.util.agent.AgentUtil#findCharset(String, int)
	 * @param conType         the content-type string.
	 * @param afterCommaIndex the index of the first comma in <code>conType</code>,
	 *                        plus one.
	 * @return the character encoding, or null if could not be found.
	 */
	@Deprecated
	public static String findCharset(String conType, int afterCommaIndex) {
		return io.sf.carte.util.agent.AgentUtil.findCharset(conType, afterCommaIndex);
	}

	/**
	 * Convert an InputStream to a Reader.
	 * 
	 * @deprecated
	 * @see io.sf.carte.util.agent.AgentUtil#inputStreamToReader(InputStream, String, String, Charset)
	 * @param is
	 * @param conType
	 * @param contentEncoding
	 * @param defaultCharset
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static Reader inputStreamToReader(InputStream is, String conType, String contentEncoding,
			Charset defaultCharset) throws IOException {
		return io.sf.carte.util.agent.AgentUtil.inputStreamToReader(is, conType, contentEncoding, defaultCharset);
	}

}
