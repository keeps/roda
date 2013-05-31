/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.common.FileFormat;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;

/**
 * @author Luis Faria
 * 
 */
public class BinaryCell extends Cell {

	private FileItem fileItem;

	private List<FileFormat> formatHits;

	/**
	 * Binary cell constructor
	 * 
	 * @param id
	 *            the cell id, equal to 'tableId.columnId.rowIndex'
	 * @param fileItem
	 *            the fileItem relative to the binary data
	 * @throws ModuleException
	 */
	public BinaryCell(String id, FileItem fileItem) throws ModuleException {
		super(id);
		this.fileItem = fileItem;
		this.formatHits = new ArrayList<FileFormat>();
	}

	/**
	 * Binary cell constructor, with optional mimetype attribute
	 * 
	 * @param id
	 *            the cell id, equal to 'tableId.columnId.rowIndex'
	 * @param fileItem
	 *            the fileItem relative to the binary data
	 * @param formatHits
	 *            the possible formats of this binary
	 * @throws ModuleException
	 * 
	 */
	public BinaryCell(String id, FileItem fileItem, List<FileFormat> formatHits)
			throws ModuleException {
		super(id);
		this.fileItem = fileItem;
		this.formatHits = formatHits;
	}

	/**
	 * @return the inputstream to fetch the binary data
	 * @throws ModuleException
	 */
	public InputStream getInputstream() throws ModuleException {
		return fileItem!=null ? fileItem.getInputStream() : null;
	}

	/**
	 * @param inputstream
	 *            the inputstream to fetch the binary data
	 * @throws ModuleException
	 */
	public void setInputstream(InputStream inputstream) throws ModuleException {
		this.fileItem = new FileItem(inputstream);
	}
	
	/**
	 * @return the possible formats of this binary
	 */
	public List<FileFormat> getFormatHits() {
		return formatHits;
	}

	/**
	 * @param formatHits
	 *            the possible formats of this binary
	 */
	public void setFormatHits(List<FileFormat> formatHits) {
		this.formatHits = formatHits;
	}

	/**
	 * Get the binary stream length in bytes
	 * 
	 * @return the binary stream length
	 */
	public long getLength() {
		return fileItem != null ? fileItem.size() : 0;
	}
	
	/**
	 * Clear resources used by binary cell
	 * @return true if successfuly cleared all resources
	 */
	public boolean cleanResources() {
		return fileItem.delete();
	}

}
