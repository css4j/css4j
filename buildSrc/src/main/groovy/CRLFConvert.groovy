import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Converts line endings to CRLF (Windows)
 * <p>
 * Usage:
 * </p>
 * <code>
 * tasks.register('lineEndingConversion', CRLFConvert) {
 * 	file "path/to/file1.txt"
 * 	file "path/to/fileN.txt"
 * }
 * </code>
 */
class CRLFConvert extends DefaultTask {

	private static final String CRLF = "\r\n"
	private static final String LF = "\n"

	private files = []

	@TaskAction
	def action() {
		files.each { path ->
			File file = new File(path)
			String content = file.text
			content = content.replaceAll(/\r\n/, LF)
			file.write(content.replaceAll(/\n|\r/, CRLF))
		}
	}

	def file(String path) {
		this.files << path
	}
}
