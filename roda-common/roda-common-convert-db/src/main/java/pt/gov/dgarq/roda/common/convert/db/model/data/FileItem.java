/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.transaction.util.FileHelper;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;

/**
 * @author Luis Faria
 * 
 */
public class FileItem {

	// private final Logger logger = Logger.getLogger(FileItem.class);

	private File file;

	/**
	 * Empty file item constructor
	 * 
	 * @throws ModuleException
	 */
	public FileItem() throws ModuleException {
		try {
			file = File.createTempFile("roda", null);
			file.deleteOnExit();
		} catch (IOException e) {
			throw new ModuleException("Error creating temporary file");
		}
	}

	/**
	 * File item constructor, copying inputstream to item
	 * 
	 * @param inputStream
	 *            the source input stream
	 * @throws ModuleException
	 */
	public FileItem(InputStream inputStream) throws ModuleException {
		this();
		try {
			// TODO if input stream is not too big, keep it in memory
			FileHelper.copy(inputStream, file);
		} catch (IOException e) {
			throw new ModuleException("Error copying stream to temp file", e);
		}

	}

	/**
	 * Get an output stream to insert content to file item
	 * 
	 * @return an output stream
	 * @throws ModuleException
	 */
	public FileOutputStream getOutputStream() throws ModuleException {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new ModuleException(
					"Error getting output stream from temp file", e);
		}
	}

	/**
	 * Get an input stream to retrieve the content from the file item
	 * 
	 * @return an input stream
	 * @throws ModuleException
	 */
	public FileInputStream getInputStream() throws ModuleException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new ModuleException(
					"Error getting input stream from temp file", e);
		}
	}

	/**
	 * The a file with the contents of the file item
	 * 
	 * @return a file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Get the size of the contents of the file item
	 * 
	 * @return the size
	 */
	public long size() {
		return file.length();
	}

	/**
	 * Delete file item
	 * 
	 * @return true if file item successfully deleted, false otherwise
	 */
	public boolean delete() {
		return file.delete();
	}
}
