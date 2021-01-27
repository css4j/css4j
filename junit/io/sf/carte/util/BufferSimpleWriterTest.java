/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class BufferSimpleWriterTest {

	@Test
	public void testBufferSimpleWriter() throws IOException {
		BufferSimpleWriter wri = new BufferSimpleWriter();
		wri.write('a');
		wri.write("bc");
		char[] cbuf = {'d', 'e', 'f'};
		wri.write(cbuf , 0, cbuf.length);
		wri.write(1);
		wri.write(0);
		assertEquals("abcdef10", wri.toString());
		wri.unwrite();
		assertEquals("abcdef1", wri.toString());
		wri.unwrite(2);
		assertEquals("abcde", wri.toString());
		wri.unwrite(20);
		assertEquals(0, wri.length());
	}

}
