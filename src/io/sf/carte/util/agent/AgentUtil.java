/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.util.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * User agent utility methods.
 * 
 * @author Carlos Amengual
 *
 */
public class AgentUtil {

	/**
	 * Find the character encoding in a content-type string.
	 * 
	 * @param conType         the content-type string.
	 * @param afterCommaIndex the index of the first comma in <code>conType</code>,
	 *                        plus one.
	 * @return the character encoding, or null if could not be found.
	 */
	public static String findCharset(String conType, int afterCommaIndex) {
		int idx = conType.indexOf("charset", afterCommaIndex);
		if (idx != -1) {
			idx += 7;
			int lenm1 = conType.length() - 1;
			char c = '\0';
			while (idx < lenm1 && (c = conType.charAt(idx)) == ' ') {
				idx++;
			}
			if (idx < lenm1 && c == '=') {
				conType = conType.substring(idx + 1).trim();
				lenm1 = conType.length() - 1;
				if (lenm1 > 1) {
					char c0 = conType.charAt(0);
					char c1 = conType.charAt(lenm1);
					if ((c0 == '"' && c1 == '"') || (c0 == '\'' && c1 == '\'')) {
						conType = conType.substring(1, lenm1);
					}
				}
				return conType;
			}
		}
		return null;
	}

	/**
	 * Convert an {@code InputStream} to a {@code Reader}.
	 * 
	 * @param is              the {@code InputStream} to convert.
	 * @param conType         the content type, or {@code null} if not available.
	 * @param contentEncoding the content encoding, or {@code null} if not
	 *                        available.
	 * @param defaultCharset  a default charset to use, if it could not be
	 *                        determined.
	 * @return the {@code Reader}.
	 * @throws IOException if an I/O problem occurs while processing the
	 *                     {@code InputStream}.
	 */
	public static Reader inputStreamToReader(InputStream is, String conType, String contentEncoding,
			Charset defaultCharset) throws IOException {
		if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
			is = new GZIPInputStream(is);
		}
		String charset = null;
		if (conType != null) {
			int sepidx = conType.indexOf(';');
			if (sepidx != -1 && sepidx < conType.length()) {
				conType = conType.substring(0, sepidx);
				charset = AgentUtil.findCharset(conType, sepidx + 1);
			}
		}
		InputStreamReader isre;
		if (charset == null) {
			isre = new InputStreamReader(is, defaultCharset);
		} else {
			isre = new InputStreamReader(is, charset);
		}
		// Handle UTF-8 BOM
		PushbackReader re = new PushbackReader(isre, 1);
		int iread = re.read();
		if (iread == -1 || iread != 0xefbbbf) {
			re.unread(iread);
		}
		return re;
	}

}
