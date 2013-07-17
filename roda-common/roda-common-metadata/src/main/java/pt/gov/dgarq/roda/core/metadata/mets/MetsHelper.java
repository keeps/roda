package pt.gov.dgarq.roda.core.metadata.mets;

import gov.loc.mets.FileType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.StructMapType;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MetsDocument.Mets;
import gov.loc.mets.MetsType.FileSec;
import gov.loc.mets.MetsType.FileSec.FileGrp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;

/**
 * Helper class to read a METS file.
 * 
 * @author Luís Faria
 * @author Rui Castro
 */
public class MetsHelper {
	private static final Logger logger = Logger.getLogger(MetsHelper.class);

	private final MetsDocument metsDocument;

	/**
	 * Creates a new instance of a {@link MetsHelper} for the METS file inside
	 * the given file.
	 * 
	 * @param metsFile
	 *            the METS XML file.
	 * 
	 * @return a {@link MetsHelper} for the given METS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static MetsHelper newInstance(File metsFile)
			throws MetsMetadataException, FileNotFoundException, IOException {
		FileInputStream metsInputStream = new FileInputStream(metsFile);
		MetsHelper instance = newInstance(metsInputStream);
		metsInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link MetsHelper} for the METS XML inside
	 * the given {@link InputStream}.
	 * 
	 * @param metsInputStream
	 *            the METS XML {@link InputStream}.
	 * 
	 * @return a {@link MetsHelper} for the given METS XML {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static MetsHelper newInstance(InputStream metsInputStream)
			throws MetsMetadataException, IOException {

		try {

			MetsDocument document = MetsDocument.Factory.parse(metsInputStream);
			if (document.validate()) {
				return new MetsHelper(document);
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
	 * Creates a new {@link MetsHelper} for a new METS document.
	 */
	public MetsHelper() {
		this(MetsDocument.Factory.newInstance());
	}

	/**
	 * Create a new METS helper
	 * 
	 * @param metsDocument
	 */
	protected MetsHelper(MetsDocument metsDocument) {

		this.metsDocument = metsDocument;

		if (getMets() == null) {
			getMetsDocument().addNewMets();
		}
	}

	/**
	 * Get the METS XML document.
	 * 
	 * @return the {@link MetsDocument}.
	 * 
	 */
	public MetsDocument getMetsDocument() {
		return this.metsDocument;
	}

	/**
	 * Get the METS XML bean
	 * 
	 * @return the {@link Mets} structure.
	 */
	public Mets getMets() {
		return getMetsDocument().getMets();
	}

	/**
	 * Saves the current METS documents to the specified file.
	 * 
	 * @param metsFile
	 *            the {@link File} to write the METS document to.
	 * @throws IOException
	 * 
	 * @deprecated use {@link MetsHelper#save(File)} instead.
	 */
	public void save(File metsFile) throws IOException {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions = xmlOptions.setSaveAggressiveNamespaces()
				.setSavePrettyPrint();
		getMetsDocument().save(metsFile, xmlOptions);
	}

	/**
	 * Saves the current METS document to a {@link File}.
	 * 
	 * @param metsFile
	 *            the {@link File}.
	 * 
	 * @throws MetsMetadataException
	 *             if the METS document is not valid or if something goes wrong
	 *             with the serialisation.
	 * 
	 * @throws FileNotFoundException
	 *             if the specified {@link File} couldn't be opened.
	 * @throws IOException
	 *             if {@link FileOutputStream} associated with the {@link File}
	 *             couldn't be closed.
	 */
	public void saveToFile(File metsFile) throws MetsMetadataException,
			FileNotFoundException, IOException {
		try {

			MetadataHelperUtility.saveToFile(getMetsDocument(), metsFile);

		} catch (MetadataException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new MetsMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Saves the current METS document to a byte array.
	 * 
	 * @return a <code>byte[]</code> with the contents of the METS XML file.
	 * 
	 * @throws MetsMetadataException
	 *             if the METS document is not valid or if something goes wrong
	 *             with the serialisation.
	 */
	public byte[] saveToByteArray() throws MetsMetadataException {

		try {

			return MetadataHelperUtility.saveToByteArray(getMetsDocument());

		} catch (MetadataException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new MetsMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the first &lt;dmdSec&gt; section or <code>null</code> if it doesn't
	 * exist.
	 * 
	 * @return a {@link MdSecType} corresponding to the first &lt;dmdSec&gt;
	 *         section or <code>null</code> if it doesn't exist.
	 */
	public MdSecType getFirstDmdSec() {

		MdSecType dmdSec;

		List<MdSecType> dmdSecList = getMets().getDmdSecList();
		if (dmdSecList != null && dmdSecList.size() > 0) {
			dmdSec = dmdSecList.get(0);
		} else {
			dmdSec = null;
		}

		return dmdSec;
	}

	/**
	 * Gets the first &lt;structMap&gt; section or <code>null</code> if it
	 * doesn't exist.
	 * 
	 * @return a {@link StructMapType} corresponding to the first
	 *         &lt;structMap&gt; section or <code>null</code> if it doesn't
	 *         exist.
	 */
	public StructMapType getFirstStructMap() {

		StructMapType structMap = null;

		List<StructMapType> structMapList = getMets().getStructMapList();
		if (structMapList != null && structMapList.size() > 0) {
			structMap = structMapList.get(0);
		} else {
			structMap = null;
		}

		return structMap;
	}

	/**
	 * Get all files in the file section (&lt;fileSec&gt;) of the METS. All file
	 * groups and all files types with sub files are traversed. The files types
	 * with sub files are not added to the list.
	 * 
	 * @return a {@link List} of {@link FileType}.
	 */
	public List<FileType> getFiles() {
		List<FileType> files = new Vector<FileType>();

		FileSec fileSec = getMets().getFileSec();
		if (fileSec != null) {
			for (FileGrp fileGrp : fileSec.getFileGrpList()) {
				files.addAll(getFiles(fileGrp));
			}
		}

		return files;
	}

	/**
	 * Lookup a file in the file section with the specified ID.
	 * 
	 * @param fileID
	 *            the file id
	 * 
	 * @return The {@link FileType} if found, otherwise <code>null</code>.
	 */
	public FileType getFile(String fileID) {

		FileType file;

		XmlObject[] xmlObjects = getMets()
				.selectPath(
						"declare namespace mets='http://www.loc.gov/METS/'; "
								+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
								+ "/mets:mets/mets:fileSec/mets:fileGrp/mets:file[@ID='"
								+ fileID + "']");

		// logger.trace("XPath result " + Arrays.toString(xmlObjects));

		if (xmlObjects == null || xmlObjects.length == 0) {
			file = null;
		} else {
			file = (FileType) xmlObjects[0];

			if (xmlObjects.length > 1) {
				logger.warn("mets:file ID " + fileID + " is not unique!");
			}
		}

		return file;
	}

	/**
	 * Lookup a file in the file section by its URL
	 * 
	 * @param url
	 *            the file URL
	 * @return the file or null if not found
	 */
	public FileType getFileByUrl(String url) {
		FileType ret = null;
		for (FileType file : getFiles()) {
			for (FLocat locat : file.getFLocatList()) {
				if (locat.getLOCTYPE().equals(FLocat.LOCTYPE.URL)
						&& locat.getHref().equals(url)) {
					ret = file;
					break;
				}
			}
			if (ret != null) {
				break;
			}
		}
		return ret;
	}

	protected List<FileType> getFiles(FileGrp fileGrp) {
		List<FileType> files = new Vector<FileType>();
		for (FileType fileType : fileGrp.getFileList()) {
			files.addAll(getFiles(fileType));
		}
		return files;
	}

	protected List<FileType> getFiles(FileType fileType) {
		List<FileType> files = new Vector<FileType>();
		if (fileType.getFileList().size() == 0) {
			files.add(fileType);
		} else {
			for (FileType file : fileType.getFileList()) {

				files.addAll(getFiles(file));
			}
		}

		return files;
	}

}
