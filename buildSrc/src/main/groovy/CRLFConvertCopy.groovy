/* -*- Mode: groovy; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * This file is dual licensed under the BSD-3-Clause and the MPL.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Converts line endings to CRLF (Windows), copy to build/tmp/crlf
 * <p>
 * Usage:
 * </p>
 * <code>
 * tasks.register('lineEndingConvCopy', CRLFConvertCopy) {
 *   from "path/to/file1.txt"
 *   from "path/to/fileN.txt"
 * }
 * </code>
 * <p>
 * You can set a prefix for the files (or a parent directory under
 * <code>tmp/crlf</code>), if you end the prefix with a slash <code>'/'</code>.
 * For example:</p>
 * <code>
 * tasks.register('prefixedLineEndingConvCopy', CRLFConvertCopy) {
 *   from "path/to/file1.txt"
 *   from "path/to/fileN.txt"
 *   prefix = 'pre/'
 * }
 * </code>
 */
class CRLFConvertCopy extends DefaultTask {

	private static final String CRLF = "\r\n"
	private static final String LF = "\n"

	private files = []
	private String pref = '';

	@TaskAction
	def action() {
		def tmpDir = new File(project.buildDir, 'tmp');
		def destDir = new File(tmpDir, 'crlf');
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		def myPref = '';
		if (pref) {
			if (pref.endsWith('/')) {
				destDir = new File(destDir, pref);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
			} else {
				myPref = pref;
			}
		}
		files.each { path ->
			def file = new File(path)
			if (file.exists()) {
				String content = file.text
				String newContent = content.replaceAll(/\r\n/, LF)
				newContent = newContent.replaceAll(/\n|\r/, CRLF)
				def dest = new File(destDir, myPref + file.name);
				dest.write(newContent, 'utf-8')
			} else {
				logger.warn('File ' + path + ' does not exist.')
			}
		}
	}

	def from(String path) {
		this.files << path
	}

	def setPrefix(String prefix) {
		this.pref = prefix;
	}
}
