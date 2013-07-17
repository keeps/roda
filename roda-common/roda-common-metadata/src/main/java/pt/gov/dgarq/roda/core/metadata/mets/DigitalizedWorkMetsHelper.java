package pt.gov.dgarq.roda.core.metadata.mets;

import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MetsType.FileSec.FileGrp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

/**
 * @author Luís Faria
 * @author Rui Castro
 */
public class DigitalizedWorkMetsHelper extends MetsHelper {
	private static final Logger logger = Logger
			.getLogger(DigitalizedWorkMetsHelper.class);

	/**
	 * Creates a new instance of a {@link DigitalizedWorkMetsHelper} for the
	 * METS file inside the given file.
	 * 
	 * @param metsFile
	 *            the METS XML file.
	 * 
	 * @return a {@link DigitalizedWorkMetsHelper} for the given METS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static DigitalizedWorkMetsHelper newInstance(File metsFile)
			throws MetsMetadataException, FileNotFoundException, IOException {
		FileInputStream metsInputStream = new FileInputStream(metsFile);
		DigitalizedWorkMetsHelper instance = newInstance(metsInputStream);
		metsInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link DigitalizedWorkMetsHelper} for the
	 * METS XML inside the given {@link InputStream}.
	 * 
	 * @param metsInputStream
	 *            the METS XML {@link InputStream}.
	 * 
	 * @return a {@link DigitalizedWorkMetsHelper} for the given METS XML
	 *         {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static DigitalizedWorkMetsHelper newInstance(
			InputStream metsInputStream) throws MetsMetadataException,
			IOException {

		try {

			MetsDocument document = MetsDocument.Factory.parse(metsInputStream);
			if (document.validate()) {
				return new DigitalizedWorkMetsHelper(document);
			} else {
				throw new MetsMetadataException("Error validating XML document");
			}

		} catch (XmlException e) {
			logger.debug("Error parsing METS - " + e.getMessage(), e);
			throw new MetsMetadataException("Error parsing METS - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Creates a new {@link DigitalizedWorkMetsHelper} for a new METS document.
	 */
	public DigitalizedWorkMetsHelper() {
		super();
	}

	/**
	 * Create a new METS helper
	 * 
	 * @param metsDocument
	 */
	protected DigitalizedWorkMetsHelper(MetsDocument metsDocument) {
		super(metsDocument);
	}

	/**
	 * Returns the {@link DivType} with the representation files structure.
	 * 
	 * @return a {@link DivType} or <code>null</code> if it doesn't exist.
	 */
	public DivType getRepresentationFileStructureDiv() {

		DivType rDiv = null;

		if (getFirstStructMap() != null) {
			rDiv = getFirstStructMap().getDiv();
		} else {
			rDiv = null;
		}

		return rDiv;
	}

	/**
	 * Create a new representation, initializing basic structures
	 * 
	 * @param id
	 *            the representation id
	 * 
	 * @return the {@link DivType} of the representation files structure.
	 */
	public DivType createRepresentation(String id) {

		setRepresentationID(id);

		if (getMets().getFileSec() == null) {
			getMets().addNewFileSec().addNewFileGrp();
		}

		if (getFirstStructMap() == null) {
			getMets().addNewStructMap();
		}

		if (getRepresentationFileStructureDiv() == null) {
			getFirstStructMap().addNewDiv();
		} else {
			clearRepresentation();
		}

		getRepresentationFileStructureDiv().setLABEL(id);

		return getRepresentationFileStructureDiv();
	}

	/**
	 * Removes all the files from the representation files structure div.
	 */
	public void clearRepresentation() {

		if (getRepresentationFileStructureDiv() != null) {
			DivType newDiv = getFirstStructMap().addNewDiv();
			newDiv.setLabel(getRepresentationID());
		}
		// if (getRepresentationFileStructureDiv() != null) {
		//
		// List<DivType> divList = getRepresentationFileStructureDiv()
		// .getDivList();
		// if (divList != null) {
		// for (int i = 0; i < divList.size(); i++) {
		// getRepresentationFileStructureDiv().removeDiv(i);
		// }
		// }
		//
		// List<Fptr> fptrList = getRepresentationFileStructureDiv()
		// .getFptrList();
		// if (fptrList != null) {
		// for (int i = 0; i < fptrList.size(); i++) {
		// getRepresentationFileStructureDiv().removeFptr(i);
		// }
		// }
		// }
	}

	/**
	 * Returns the ID of the representation.
	 * 
	 * @return a {@link String} with the ID of the representation.
	 */
	public String getRepresentationID() {
		return getMets().getID();
	}

	/**
	 * Sets the ID of the representation.
	 * 
	 * @param id
	 *            the ID to set.
	 */
	public void setRepresentationID(String id) {
		getMets().setID(id);
		if (getRepresentationFileStructureDiv() != null) {
			getRepresentationFileStructureDiv().setLabel(id);
		}
	}

	/**
	 * Generate a new file ID
	 * 
	 * @return "F" + the file index, e.g. "F0", "F1" ... "F110"
	 */
	public String getNextFileID() {
		return "F" + getFiles().size();
	}

	/**
	 * Create a new File in the File section
	 * 
	 * @param fileId
	 *            the file id
	 * @param url
	 *            the URL to the File
	 * @return the created FileType
	 * @throws MetsFileAlreadyExistsException
	 */
	public FileType createFile(String fileId, String url)
			throws MetsFileAlreadyExistsException {

		if (!isFileUrlUnique(url)) {
			throw new MetsFileAlreadyExistsException(url);
		}

		List<FileGrp> grpList = getMets().getFileSec().getFileGrpList();
		FileGrp fileGrp;
		if (grpList.size() == 0) {
			fileGrp = getMets().getFileSec().addNewFileGrp();
			logger.debug("created new file group");
		} else {
			fileGrp = grpList.get(0);
		}
		FileType newFileType = fileGrp.addNewFile();
		newFileType.setID(fileId);
		FLocat newFLocat = newFileType.addNewFLocat();
		newFLocat.setLOCTYPE(FLocat.LOCTYPE.URL);
		newFLocat.setHref(url);
		return newFileType;
	}

	/**
	 * Adds a new file to the file section. The file ID will be given by
	 * {@link #getNextFileID()}.
	 * 
	 * @param fileURL
	 *            the URL of the file to add.
	 * 
	 * @return the new {@link FileType}.
	 * 
	 * @throws MetsFileAlreadyExistsException
	 *             if the URL is equal to the URL of an existing file.
	 */
	public FileType addFile(String fileURL)
			throws MetsFileAlreadyExistsException {
		return createFile(getNextFileID(), fileURL);
	}

	/**
	 * Get HREF of a file assuming there is at least one FLocat for it.
	 * 
	 * @param fileId
	 *            the file id
	 * 
	 * @return a {@link String} with the HREF for file with the given fileId or
	 *         <code>null</code> if it doesn't exist.
	 */
	public String getFileHref(String fileId) {
		String fileHref;

		FileType fileType = getFile(fileId);
		if (fileType != null) {
			List<FLocat> fLocatList = fileType.getFLocatList();
			if (fLocatList != null && fLocatList.size() > 0) {
				fileHref = fLocatList.get(0).getHref();
			} else {
				fileHref = null;
			}
		} else {
			fileHref = null;
		}

		return fileHref;
	}

	/**
	 * Get a file with the given HREF.
	 * 
	 * @param href
	 *            the HREF
	 * 
	 * @return the file type or <code>null</code> if it doesn't exist.
	 */
	public FileType getFileByHref(String href) {

		FileType file;

		XmlObject[] xmlObjects = getMets()
				.selectPath(
						"declare namespace mets='http://www.loc.gov/METS/'; "
								+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
								+ "/mets:mets/mets:fileSec/mets:fileGrp/mets:file[mets:FLocat[@xlink:href='"
								+ href + "']]");

		if (xmlObjects == null || xmlObjects.length == 0) {
			file = null;
		} else {
			file = (FileType) xmlObjects[0];

			if (xmlObjects.length > 1) {
				logger.warn("mets:file/mets:FLocat HREF " + href
						+ " is not unique!");
			}
		}

		// FileType file = null;
		//
		// for (FileType fileType : getFiles()) {
		// if (fileType.getFLocatList().get(0).getHref().equals(href)) {
		// file = fileType;
		// break;
		// }
		// }

		return file;
	}

	/**
	 * Create a new div.
	 * 
	 * @param parent
	 *            the parent of the new div
	 * @param label
	 *            the LABEL of the new div
	 * @return the created div
	 */
	public DivType createDiv(DivType parent, String label) {
		DivType newDiv = parent.addNewDiv();
		newDiv.setLABEL(label);
		return newDiv;
	}

	/**
	 * Remove a div
	 * 
	 * @param parent
	 *            the parent of the div
	 * @param child
	 *            the div to remove
	 * 
	 * @return true if div was removed, false if it was not found under the
	 *         parent
	 */
	public boolean removeDiv(DivType parent, DivType child) {
		boolean ret;
		int childIndex = parent.getDivList().indexOf(child);
		if (childIndex >= 0) {
			parent.removeDiv(childIndex);
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	/**
	 * Rename a div
	 * 
	 * @param div
	 *            the div to rename
	 * @param label
	 *            the div label
	 */
	public void renameDiv(DivType div, String label) {
		div.setLABEL(label);
	}

	/**
	 * Copy div into parent, recursively copying div's sub-divs and fptrs into
	 * the new div
	 * 
	 * @param parent
	 * @param div
	 * @return the new div
	 */
	public DivType copyDiv(DivType div, DivType parent) {
		DivType newDiv = parent.addNewDiv();
		newDiv.setLABEL(div.getLABEL());
		for (DivType subDiv : div.getDivList()) {
			copyDiv(subDiv, newDiv);
		}
		for (Fptr fptr : div.getFptrList()) {
			copyFptr(fptr, newDiv);
		}
		return newDiv;

	}

	/**
	 * Copy div into parent, recursively copying div's sub-divs and fptrs into
	 * the new div, on a designated position
	 * 
	 * @param parent
	 * @param div
	 * @param beforeIndex
	 * @return the new div
	 */
	public DivType copyDiv(DivType div, DivType parent, int beforeIndex) {
		DivType newDiv = parent.insertNewDiv(beforeIndex);
		newDiv.setLABEL(div.getLABEL());
		for (DivType subDiv : div.getDivList()) {
			copyDiv(subDiv, newDiv);
		}
		for (Fptr fptr : div.getFptrList()) {
			copyFptr(fptr, newDiv);
		}
		return newDiv;

	}

	/**
	 * Create a new File Pointer under a div
	 * 
	 * @param parent
	 *            the parent div
	 * @param file
	 *            the file where to point
	 * @return the File Pointer
	 */
	public Fptr createFptr(DivType parent, FileType file) {
		Fptr newFptr = parent.addNewFptr();
		newFptr.setFILEID(file.getID());

		return newFptr;

	}

	/**
	 * Copy Fptr into div
	 * 
	 * @param fptr
	 * @param div
	 * @return the new fptr
	 */
	public Fptr copyFptr(Fptr fptr, DivType div) {
		Fptr newFptr = div.addNewFptr();
		newFptr.setFILEID(fptr.getFILEID());
		return newFptr;
	}

	/**
	 * Copy fptr into div into a designated position
	 * 
	 * @param fptr
	 * @param div
	 * @param beforeIndex
	 * @return the new fptr
	 */
	public Fptr copyFptr(Fptr fptr, DivType div, int beforeIndex) {
		Fptr newFptr = div.insertNewFptr(beforeIndex);
		newFptr.setFILEID(fptr.getFILEID());
		return newFptr;
	}

	/**
	 * Remove a fptr
	 * 
	 * @param parent
	 *            the parent of the fptr
	 * @param fptr
	 *            the fptr to remove
	 * @return true if the fptr was removed, false if it was not found under the
	 *         parent
	 */
	public boolean removeFptr(DivType parent, Fptr fptr) {
		boolean ret;
		int childIndex = parent.getFptrList().indexOf(fptr);
		if (childIndex >= 0) {
			parent.removeFptr(childIndex);
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	/**
	 * Remove a file
	 * 
	 * @param file
	 * @return true if file was removed, false if file orphan
	 */
	public boolean removeFile(FileType file) {
		boolean ret;
		Node fileNode = file.getDomNode();
		Node parentNode = fileNode.getParentNode();
		if (parentNode != null) {
			parentNode.removeChild(fileNode);
			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}

	protected boolean isFileUrlUnique(String url) {
		boolean ret = true;
		for (FileType file : getFiles()) {
			for (FLocat flocat : file.getFLocatList()) {
				if (flocat.getHref().equals(url)) {
					ret = false;
					break;
				}
			}

			if (!ret) {
				break;
			}
		}

		return ret;
	}

}
