package pt.gov.dgarq.roda.core.metadata.mets;

import gov.loc.mets.AmdSecType;
import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MetsDocument;
import gov.loc.mets.StructMapType;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.FileType.CHECKSUMTYPE;
import gov.loc.mets.FileType.FLocat;
import gov.loc.mets.MdSecType.MdRef;
import gov.loc.mets.MdSecType.MdRef.LOCTYPE;
import gov.loc.mets.MdSecType.MdRef.MDTYPE;
import gov.loc.mets.MetsType.FileSec;
import gov.loc.mets.MetsType.MetsHdr;
import gov.loc.mets.MetsType.FileSec.FileGrp;
import gov.loc.mets.MetsType.MetsHdr.Agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.metadata.premis.PremisMetadataException;
import pt.gov.dgarq.roda.util.FileUtility;
import pt.gov.dgarq.roda.util.StringUtility;

/**
 * Helper class to read and write a SIP METS file.
 * 
 * @author Rui Castro
 */
public class SIPMetsHelper extends MetsHelper {
	private static final Logger logger = Logger.getLogger(SIPMetsHelper.class);

	private static final String AMDSEC_ID_OBJECT = "OBJECT";
	private static final String AMDSEC_ID_EVENT = "EVENT";
	private static final String AMDSEC_ID_AGENT = "AGENT";

	private Random descriptionObjectRandomIDs = new Random();

	/**
	 * Creates a new instance of a {@link SIPMetsHelper} for the METS file
	 * inside the given file.
	 * 
	 * @param metsFile
	 *            the METS XML file.
	 * 
	 * @return a {@link SIPMetsHelper} for the given METS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static SIPMetsHelper newInstance(File metsFile)
			throws MetsMetadataException, FileNotFoundException, IOException {
		FileInputStream metsInputStream = new FileInputStream(metsFile);
		SIPMetsHelper instance = newInstance(metsInputStream);
		metsInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link SIPMetsHelper} for the METS XML inside
	 * the given {@link InputStream}.
	 * 
	 * @param metsInputStream
	 *            the METS XML {@link InputStream}.
	 * 
	 * @return a {@link SIPMetsHelper} for the given METS XML
	 *         {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws MetsMetadataException
	 *             if the METS XML document is invalid.
	 */
	public static SIPMetsHelper newInstance(InputStream metsInputStream)
			throws MetsMetadataException, IOException {

		try {

			MetsDocument document = MetsDocument.Factory.parse(metsInputStream);
			if (document.validate()) {
				return new SIPMetsHelper(document);
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
	 * Constructs a new {@link SIPMetsHelper} with a new SIP METS.
	 */
	public SIPMetsHelper() {
		super();
		getMets().setPROFILE("RODA_SIP");
	}

	/**
	 * Create a new METS helper
	 * 
	 * @param metsDocument
	 */
	protected SIPMetsHelper(MetsDocument metsDocument) {
		super(metsDocument);
	}

	/**
	 * Sets the creator role in the METS header section.
	 * 
	 * @param creator
	 *            the name of the creator.
	 */
	public void setCreator(String creator) {
		MetsHdr metsHdr = getMets().getMetsHdr();
		if (metsHdr == null) {
			metsHdr = getMets().addNewMetsHdr();
		}

		List<Agent> agentList = metsHdr.getAgentList();
		Agent agent = null;
		if (agentList == null || agentList.size() == 0) {
			agent = metsHdr.addNewAgent();
		} else {
			agent = agentList.get(0);
		}
		agent.setROLE(Agent.ROLE.CREATOR);
		agent.setName(creator);
	}

	/**
	 * Adds a new descriptive metadata section (&lt;dmdSec&gt;), child of
	 * <code>parent</code>, to point to the given file.
	 * 
	 * @param parent
	 *            the parent description object section or <code>null</code> if
	 *            it has no parent.
	 * @param unitid
	 * @param contentModel
	 * @param file
	 * 
	 * @return the ID of the &lt;dmdSec&gt; element.
	 * 
	 * @throws MetsDescriptionObjectAlreadyExists
	 */
	public MdSecType addDescriptionObjectFile(MdSecType parent, String unitid,
			String contentModel, File file)
			throws MetsDescriptionObjectAlreadyExists {

		MdSecType existingDmdSec = getDmdSec(parent, unitid);

		if (existingDmdSec != null) {
			throw new MetsDescriptionObjectAlreadyExists(
					"The description object file " + unitid
							+ " already exists.");
		} else {
			MdSecType dmdSec = getMets().addNewDmdSec();

			String id = "EADC-" + unitid + "-"
					+ descriptionObjectRandomIDs.nextInt();

			String fixedID = id.replaceAll("[^\\w-]+", "_");

			dmdSec.setID(fixedID);
			if (parent != null) {
				dmdSec.setGROUPID(parent.getID());
			}

			MdRef mdRef = dmdSec.getMdRef();
			if (mdRef == null) {
				mdRef = dmdSec.addNewMdRef();
			}
			mdRef.setLOCTYPE(MdRef.LOCTYPE.URL);
			mdRef.setMDTYPE(MdRef.MDTYPE.OTHER);
			mdRef.setHref(file.getName());
			mdRef.setLABEL(contentModel);

			try {

				mdRef.setCHECKSUMTYPE(MdRef.CHECKSUMTYPE.MD_5);
				mdRef.setCHECKSUM(FileUtility.calculateChecksumInHex(file,
						"MD5"));

			} catch (Exception e) {
				mdRef.unsetCHECKSUMTYPE();
				mdRef.unsetCHECKSUM();
				logger.error("Error calculating checksum", e);
			}

			return dmdSec;
		}

	}

	/**
	 * Gets the parent &lt;dmdSec&gt;.
	 * 
	 * @return a {@link MdSecType} or <code>null</code> if it doesn't exist.
	 */
	public MdSecType getParentDmdSec() {

		String xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; "
				+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
				+ "/mets:mets/mets:dmdSec[not(@GROUPID)]";

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);

		MdSecType dmdSec;
		if (xmlObjects == null || xmlObjects.length == 0) {
			dmdSec = null;
		} else {
			dmdSec = (MdSecType) xmlObjects[0];

			if (xmlObjects.length > 1) {
				logger.warn("Parent mets:dmdSec (without @GROUPID) is not unique!");
			}
		}

		return dmdSec;
	}

	/**
	 * Gets the relative location of the file containing the parent description
	 * object.
	 * 
	 * @return a {@link String} with the relative location for the parent
	 *         descriptive metadata (EAD-C file) or <code>null</code> if it
	 *         doesn't exist.
	 */
	public String getParentDescriptionObjectHref() {
		return getDmdSecHref(getParentDmdSec());
	}

	/**
	 * Gets the children &lt;dmdSec&gt;s for the given parent.
	 * 
	 * @param parent
	 *            the parent &lt;dmdSec&gt;
	 * 
	 * @return a {@link List} of {@link MdSecType}.
	 */
	public List<MdSecType> getChildrenDmdSecs(MdSecType parent) {

		String xpathQuery;
		if (parent == null) {
			xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; "
					+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
					+ "/mets:mets/mets:dmdSec[not(@GROUPID)]";
		} else {
			xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; "
					+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
					+ "/mets:mets/mets:dmdSec[@GROUPID='" + parent.getID()
					+ "']";
		}

		List<MdSecType> dmdSecs = new ArrayList<MdSecType>();

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);
		if (xmlObjects != null && xmlObjects.length > 0) {
			dmdSecs.addAll(Arrays.asList((MdSecType[]) xmlObjects));
		}

		return dmdSecs;
	}

	/**
	 * Gets the relative locations of the files containing the description
	 * objects children of the given parent.
	 * 
	 * @param parent
	 *            the parent &lt;dmdSec&gt;
	 * 
	 * @return a {@link List} of {@link String} with the relative locations for
	 *         the descriptive metadata (EAD-C files).
	 */
	public List<String> getChildrenDescriptionObjectHrefs(MdSecType parent) {

		List<String> childHrefs = new ArrayList<String>();

		for (MdSecType child : getChildrenDmdSecs(parent)) {
			childHrefs.add(getDmdSecHref(child));
		}

		return childHrefs;
	}

	/**
	 * Gets the HREF of a given &lt;dmdSec&gt;.
	 * 
	 * @param dmdSec
	 *            the &lt;dmdSec&gt;
	 * 
	 * @return a {@link String} tithe the HREF.
	 */
	public String getDmdSecHref(MdSecType dmdSec) {
		String doHref;

		if (dmdSec != null) {
			if (dmdSec.getMdRef() != null) {
				doHref = dmdSec.getMdRef().getHref();
			} else {
				doHref = null;
			}
		} else {
			doHref = null;
		}

		return doHref;
	}

	/**
	 * Returns the &lt;div&gt; that contains the representations. It's the
	 * &lt;div&gt; of the first &lt;structMap&gt;.
	 * 
	 * @return a {@link DivType} or <code>null</code> if it doesn't exist.
	 */
	public DivType getRepresentationsDiv() {

		DivType rDiv = null;
		if (getFirstStructMap() != null) {
			rDiv = getFirstStructMap().getDiv();
		} else {
			rDiv = null;
		}

		return rDiv;
	}

	/**
	 * Returns the &lt;div&gt; that contains the representation with the
	 * specified <code>representationID</code>.
	 * 
	 * @param representationID
	 *            the ID of the representation (&lt;div&gt;).
	 * 
	 * @return a {@link DivType} or <code>null</code> if it doesn't exist.
	 */
	public DivType getRepresentationDiv(String representationID) {

		DivType representationDiv = null;

		DivType representationsDiv = getRepresentationsDiv();
		if (representationsDiv != null
				&& representationsDiv.getDivList() != null) {
			for (DivType repDiv : representationsDiv.getDivList()) {
				if (repDiv.getID().equals(representationID)) {
					representationDiv = repDiv;
				}
			}
		}

		return representationDiv;
	}

	/**
	 * Returns a {@link List} with all the representations inside a SIP.
	 * 
	 * @return a {@link List} of {@link RepresentationObject}s.
	 * 
	 * @throws MetsMetadataException
	 *             if the SIP METS is not valid.
	 */
	public List<RepresentationObject> getRepresentations()
			throws MetsMetadataException {

		List<RepresentationObject> representations = new ArrayList<RepresentationObject>();

		DivType representationsDiv = getRepresentationsDiv();

		if (representationsDiv != null) {

			Map<String, FileType> metsFileMap = getFileMap();

			logger.trace("METS has " + metsFileMap.size() + " files");

			for (DivType repDiv : representationsDiv.getDivList()) {

				logger.trace("Parsing Representation <div ID=" + repDiv.getID()
						+ " TYPE=" + repDiv.getTYPE() + ">");

				representations
						.add(getRepresentationObject(repDiv, metsFileMap));
			}
		}

		return representations;
	}

	/**
	 * Returns a {@link List} with the representations of the given dmdSec/@ID.
	 * 
	 * @param dmdSecID
	 * 
	 * @return a {@link List} of {@link RepresentationObject}s.
	 * 
	 * @throws MetsMetadataException
	 *             if the SIP METS is not valid.
	 */
	public List<RepresentationObject> getRepresentations(String dmdSecID)
			throws MetsMetadataException {

		List<RepresentationObject> representations = new ArrayList<RepresentationObject>();

		DivType representationsDiv = getRepresentationsDiv();

		if (representationsDiv != null) {

			Map<String, FileType> metsFileMap = getFileMap();

			if (representationsDiv.getDivList() != null) {

				for (DivType repDiv : representationsDiv.getDivList()) {

					// @DMDID is a list of IDs. We want just the first one
					String dmdid = null;
					if (repDiv.getDMDID() != null
							&& repDiv.getDMDID().size() > 0) {

						dmdid = repDiv.getDMDID().get(0).toString();

						if (repDiv.getDMDID().size() > 1) {
							logger.warn("Representation <div ID=" //$NON-NLS-1$
									+ repDiv.getID()
									+ "> @DMDID attribute has " //$NON-NLS-1$
									+ repDiv.getDMDID().size()
									+ " values, but only 1 is being considered."); //$NON-NLS-1$
						}

					} else {
						throw new MetsMetadataException(
								"Representation <div ID=" + repDiv.getID() //$NON-NLS-1$
										+ "> @DMDID attribute not set."); //$NON-NLS-1$
					}

					if (dmdid.equals(dmdSecID)) {

						logger.trace("Parsing representation <div ID=" //$NON-NLS-1$
								+ repDiv.getID() + " TYPE=" + repDiv.getTYPE() //$NON-NLS-1$
								+ ">"); //$NON-NLS-1$

						representations.add(getRepresentationObject(repDiv,
								metsFileMap));

					}
				}
			}
		}

		return representations;
	}

	/**
	 * @param dmdSec
	 * @param representations
	 */
	public void addRepresentations(MdSecType dmdSec,
			List<RepresentationObject> representations) {

		// Add the files to <fileSec>
		addRepresentationFiles(representations);

		// Get the top "representations" <div> inside structMap
		DivType representationsDiv = getRepresentationsDiv();

		if (representationsDiv == null) {

			StructMapType structMap = getFirstStructMap();
			if (structMap == null) {
				structMap = getMets().addNewStructMap();
			}

			representationsDiv = structMap.addNewDiv();
			representationsDiv.setID("Representations");
		}

		for (RepresentationObject rObject : representations) {
			createRepresentationDiv(dmdSec, representationsDiv, rObject);
		}

	}

	/**
	 * @param dmdSec
	 * @param rObjects
	 */
	public void setRepresentations(MdSecType dmdSec,
			List<RepresentationObject> rObjects) {

		removeAllFiles(getFileGrp());
		removeAllRepresentations(getRepresentationsDiv());

		addRepresentations(dmdSec, rObjects);
	}

	/**
	 * Set the files under the file section (&lt;fileSec&gt;) to the files of
	 * the given {@link RepresentationObject}s.
	 * 
	 * @param rObjects
	 *            the {@link List} of {@link RepresentationObject}s.
	 */
	public void addRepresentationFiles(List<RepresentationObject> rObjects) {

		FileGrp fileGrp = getFileGrp();

		for (RepresentationObject rObject : rObjects) {

			RepresentationFile rootFile = rObject.getRootFile();
			createFile(fileGrp, rObject, rootFile);

			for (RepresentationFile partFile : rObject.getPartFiles()) {
				createFile(fileGrp, rObject, partFile);
			}
		}

	}

	/**
	 * Set the files under the file section (&lt;fileSec&lt;) to the files of
	 * the given {@link RepresentationObject}s.
	 * 
	 * @param rObjects
	 *            the {@link List} of {@link RepresentationObject}s.
	 */
	public void setRepresentationFiles(List<RepresentationObject> rObjects) {

		removeAllFiles(getFileGrp());

		addRepresentationFiles(rObjects);
	}

	/**
	 * Gets a {@link FileType} given the representation object ID and the
	 * representation file ID.
	 * 
	 * @param representationID
	 * @param rFileID
	 * 
	 * @return {@link FileType} or <code>null</code> if it doesn't exist.
	 */
	public FileType getFile(String representationID, String rFileID) {
		return getFile(representationID + "-" + rFileID); //$NON-NLS-1$
	}

	/**
	 * Gets the ID of the digiprovMD section of a representation preservation
	 * object for the specified <code>representationID</code>.
	 * 
	 * @param representationID
	 *            the representation ID.
	 * 
	 * @return a {@link String} with the ID of the digiprovMD section that
	 *         corresponds to the given representation ID or <code>null</code>
	 *         if the representation doesn't have a digiprovMD section.
	 */
	public String getRepresentationPreservationObjectMdSecID(
			String representationID) {
		String mdSecID = null;
		DivType representationDiv = getRepresentationDiv(representationID);

		if (representationDiv != null) {

			List<String> admIDList = representationDiv.getADMID();

			if (admIDList != null && admIDList.size() > 0) {

				String admID = admIDList.get(0);

				MdSecType digiprovMdSec = getDigiprovMD(admID);
				mdSecID = digiprovMdSec.getID();

				if (admIDList.size() > 1) {
					logger.warn("Representation " //$NON-NLS-1$
							+ representationID
							+ " has " //$NON-NLS-1$
							+ admIDList.size()
							+ " ADMID values. Only the first one will be used."); //$NON-NLS-1$
				}
			}

		}

		return mdSecID;
	}

	/**
	 * Gets the {@link MdSecType} of the representation preservation object with
	 * the specified ID.
	 * 
	 * @param rpoMdSecID
	 *            the {@link MdSecType} ID.
	 * 
	 * @return a {@link of the representation.} of a PREMIS file with the
	 *         representation preservation object or
	 *         <code>null<code> if the representation doesn't exist or if it doesn't have preservation metadata.
	 */
	public MdSecType getRepresentationPreservationObjectMdSec(String rpoMdSecID) {
		return getDigiprovMD(rpoMdSecID);
	}

	/**
	 * Gets the {@link MdRef}s of the representation file preservation objects
	 * of the specified representation preservation object.
	 * 
	 * @param rpoID
	 *            the ID of the representation preservation object.
	 * 
	 * @return a {@link List} of {@link MdRef} of the representation file
	 *         preservation objects.
	 */
	public List<MdRef> getRepresentationFilePreservationObjectMdRefs(
			String rpoID) {

		List<MdRef> mdRefs = new ArrayList<MdRef>();

		String xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; " //$NON-NLS-1$
				+ "declare namespace xlink='http://www.w3.org/1999/xlink';" //$NON-NLS-1$
				+ "/mets:mets/mets:amdSec/mets:digiprovMD[@GROUPID='" //$NON-NLS-1$
				+ rpoID + "']"; //$NON-NLS-1$

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);

		if (xmlObjects == null || xmlObjects.length == 0) {
			// no digiprovMD elements with GROUPID 'rpoID'
		} else {

			for (int index = 0; index < xmlObjects.length; index++) {

				MdSecType digiprovSec = (MdSecType) xmlObjects[index];

				if (digiprovSec.getMdRef() != null) {

					mdRefs.add(digiprovSec.getMdRef());

				} else {
					logger.warn("digiprovMD with ID='" //$NON-NLS-1$
							+ digiprovSec.getID()
							+ "' doesn't have a mdRef element"); //$NON-NLS-1$
				}

			}

		}

		return mdRefs;
	}

	/**
	 * Gets the HREFs of the representation file preservation objects of the
	 * specified representation preservation object.
	 * 
	 * @param rpoID
	 *            the ID of the representation preservation object.
	 * 
	 * @return a {@link List} of {@link String} with the HREFs of the
	 *         representation file preservation objects.
	 */
	public List<String> getRepresentationFilePreservationObjectHrefs(
			String rpoID) {

		List<MdRef> fpoMdRefs = getRepresentationFilePreservationObjectMdRefs(rpoID);
		List<String> hrefs = new ArrayList<String>();

		for (MdRef fpoMdRef : fpoMdRefs) {

			if (fpoMdRef.getHref() != null) {
				hrefs.add(fpoMdRef.getHref());
			} else {
				logger.warn("mdRef " + fpoMdRef //$NON-NLS-1$
						+ "' doesn't have a href attribute"); //$NON-NLS-1$
			}
		}

		return hrefs;
	}

	public MdSecType getRepresentationDerivationEventMdSec(String rpoMdSecID) {

		MdSecType derivationEpoMdSec = null;

		MdSecType rpoMdSec = getRepresentationPreservationObjectMdSec(rpoMdSecID);

		// Get the ID of the derivation event
		if (rpoMdSec.getADMID() != null && rpoMdSec.getADMID().size() == 1) {

			List<String> admIDList = rpoMdSec.getADMID();

			derivationEpoMdSec = getDigiprovMD(admIDList.get(0));
		}

		return derivationEpoMdSec;
	}

	public List<MdSecType> getEventMdSecs(String rpoMdSecID) {
		List<MdSecType> mdSecs = new ArrayList<MdSecType>();

		String xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; " //$NON-NLS-1$
				+ "declare namespace xlink='http://www.w3.org/1999/xlink';" //$NON-NLS-1$
				+ "/mets:mets/mets:amdSec[@ID='EVENT']/mets:digiprovMD[contains(@ADMID,'" //$NON-NLS-1$
				+ rpoMdSecID + "')]"; //$NON-NLS-1$

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);

		if (xmlObjects == null || xmlObjects.length == 0) {
			// no digiprovMD elements with ADMID 'rpoMdSecID'
		} else {

			for (int index = 0; index < xmlObjects.length; index++) {
				MdSecType digiprovSec = (MdSecType) xmlObjects[index];
				mdSecs.add(digiprovSec);
			}

		}

		return mdSecs;
	}

	public MdSecType getEventMdSec(String eventMdSecID) {
		return getDigiprovMD(eventMdSecID);
	}

	public MdSecType getAgentMdSec(String agentMdSecID) {
		return getDigiprovMD(agentMdSecID);
	}

	/**
	 * Add a new digiprovMD section for the given
	 * {@link RepresentationPreservationObject}.
	 * 
	 * @param rpo
	 *            the {@link RepresentationPreservationObject}
	 * @param rpoFile
	 *            the {@link RepresentationPreservationObject}'s file.
	 * @param ro
	 *            the {@link RepresentationObject}.
	 * @return the added digiprovMD {@link MdSecType}.
	 * 
	 * @throws MetsMetadataException
	 */
	public MdSecType addRepresentationPreservationObject(
			RepresentationPreservationObject rpo, File rpoFile,
			RepresentationObject ro) throws MetsMetadataException {

		String rpoMdSecID = "PREMIS-" + rpo.getID();

		AmdSecType objectsAmdSec = getAmdSec(AMDSEC_ID_OBJECT);
		if (objectsAmdSec == null) {
			objectsAmdSec = getMets().addNewAmdSec();
			objectsAmdSec.setID(AMDSEC_ID_OBJECT);
		}

		MdSecType newDigiprovMD = objectsAmdSec.addNewDigiprovMD();
		newDigiprovMD.setID(rpoMdSecID);

		if (rpo.getDerivationEventID() != null) {
			String derivationEventMdSecID = "PREMIS-"
					+ rpo.getDerivationEventID();
			newDigiprovMD.setADMID(Arrays.asList(derivationEventMdSecID));
		}

		MdRef newMdRef = newDigiprovMD.addNewMdRef();
		newMdRef.setLOCTYPE(LOCTYPE.URL);
		newMdRef.setMDTYPE(MDTYPE.PREMIS_OBJECT);
		newMdRef.setMIMETYPE("text/xml"); //$NON-NLS-1$

		try {
			newMdRef.setCHECKSUMTYPE(MdRef.CHECKSUMTYPE.MD_5);
			newMdRef.setCHECKSUM(FileUtility.calculateChecksumInHex(rpoFile,
					"MD5")); //$NON-NLS-1$
		} catch (Exception e) {
			newMdRef.unsetCHECKSUMTYPE();
			newMdRef.unsetCHECKSUM();
			logger.error("Error calculating checksum", e); //$NON-NLS-1$
		}

		newMdRef.setHref(rpoFile.getParentFile().getName() + File.separator
				+ rpoFile.getName());

		if (ro == null) {
			logger.debug("RPO " + rpo.getID()
					+ " doesn't have a representation div");
		} else {

			DivType representationDiv = getRepresentationDiv(ro.getId());

			if (representationDiv == null) {
				logger.error("RO " + ro.getId()
						+ " doesn't have a representation div");
				throw new MetsMetadataException("RO " + ro.getId()
						+ " doesn't have a representation div");
			} else {
				representationDiv.setADMID(Arrays.asList(rpoMdSecID));
			}
		}

		return newDigiprovMD;
	}

	/**
	 * Add a new digiprovMD section for the given
	 * {@link RepresentationFilePreservationObject}.
	 * 
	 * @param rpo
	 *            the {@link RepresentationPreservationObject}
	 * @param rfpo
	 *            the {@link RepresentationFilePreservationObject}
	 * @param rfpoFile
	 *            the {@link RepresentationFilePreservationObject}'s file.
	 * 
	 * @return the added digiprovMD {@link MdSecType}.
	 */
	public MdSecType addRepresentationFilePreservationObject(
			RepresentationPreservationObject rpo,
			RepresentationFilePreservationObject rfpo, File rfpoFile) {

		String rpoMdSecID = "PREMIS-" + rpo.getID();
		String rfpoMdSecID = rpoMdSecID + "-" + rfpo.getID();

		AmdSecType objectsAmdSec = getAmdSec(AMDSEC_ID_OBJECT);
		if (objectsAmdSec == null) {
			objectsAmdSec = getMets().addNewAmdSec();
			objectsAmdSec.setID(AMDSEC_ID_OBJECT);
		}

		MdSecType newDigiprovMD = objectsAmdSec.addNewDigiprovMD();
		newDigiprovMD.setID(rfpoMdSecID);
		newDigiprovMD.setGROUPID(rpoMdSecID);
		MdRef newMdRef = newDigiprovMD.addNewMdRef();
		newMdRef.setLOCTYPE(LOCTYPE.URL);
		newMdRef.setMDTYPE(MDTYPE.PREMIS_OBJECT);
		newMdRef.setMIMETYPE("text/xml"); //$NON-NLS-1$

		try {
			newMdRef.setCHECKSUMTYPE(MdRef.CHECKSUMTYPE.MD_5);
			newMdRef.setCHECKSUM(FileUtility.calculateChecksumInHex(rfpoFile,
					"MD5")); //$NON-NLS-1$
		} catch (Exception e) {
			newMdRef.unsetCHECKSUMTYPE();
			newMdRef.unsetCHECKSUM();
			logger.error("Error calculating checksum", e); //$NON-NLS-1$
		}

		newMdRef.setHref(rfpoFile.getParentFile().getName() + File.separator
				+ rfpoFile.getName());

		return newDigiprovMD;
	}

	/**
	 * Add a new digiprovMD section for the given
	 * {@link EventPreservationObject}.
	 * 
	 * @param rpo
	 *            the {@link RepresentationPreservationObject}
	 * @param epo
	 *            the {@link EventPreservationObject}
	 * @param epoFile
	 *            the {@link EventPreservationObject}'s file.
	 * 
	 * @return the added digiprovMD {@link MdSecType}.
	 */
	public MdSecType addEventPreservationObject(
			RepresentationPreservationObject rpo, EventPreservationObject epo,
			File epoFile) {

		String rpoMdSecID = "PREMIS-" + rpo.getID();
		String epoMdSecID = "PREMIS-" + epo.getID();
		String apoMdSecID = "PREMIS-" + epo.getAgentID();

		AmdSecType eventsAmdSec = getAmdSec(AMDSEC_ID_EVENT);
		if (eventsAmdSec == null) {
			eventsAmdSec = getMets().addNewAmdSec();
			eventsAmdSec.setID(AMDSEC_ID_EVENT);
		}

		MdSecType newDigiprovMD = eventsAmdSec.addNewDigiprovMD();
		newDigiprovMD.setID(epoMdSecID);
		newDigiprovMD.setADMID(Arrays.asList(apoMdSecID, rpoMdSecID));
		MdRef newMdRef = newDigiprovMD.addNewMdRef();
		newMdRef.setLOCTYPE(LOCTYPE.URL);
		newMdRef.setMDTYPE(MDTYPE.PREMIS_EVENT);
		newMdRef.setMIMETYPE("text/xml"); //$NON-NLS-1$

		try {
			newMdRef.setCHECKSUMTYPE(MdRef.CHECKSUMTYPE.MD_5);
			newMdRef.setCHECKSUM(FileUtility.calculateChecksumInHex(epoFile,
					"MD5")); //$NON-NLS-1$
		} catch (Exception e) {
			newMdRef.unsetCHECKSUMTYPE();
			newMdRef.unsetCHECKSUM();
			logger.error("Error calculating checksum", e); //$NON-NLS-1$
		}

		newMdRef.setHref(epoFile.getParentFile().getName() + File.separator
				+ epoFile.getName());

		return newDigiprovMD;
	}

	/**
	 * Add a new digiprovMD section for the given
	 * {@link AgentPreservationObject}.
	 * 
	 * @param apo
	 *            the {@link AgentPreservationObject}
	 * @param apoFile
	 *            the {@link AgentPreservationObject}'s file.
	 * 
	 * @return the added digiprovMD {@link MdSecType}.
	 * 
	 * @throws PremisMetadataException
	 */
	public MdSecType addAgentPreservationObject(AgentPreservationObject apo,
			File apoFile) throws PremisMetadataException {

		String apoMdSecID = "PREMIS-" + apo.getID();

		if (getAgentMdSec(apoMdSecID) != null) {
			throw new PremisMetadataException("Agent with ID " + apo.getID() //$NON-NLS-1$
					+ " already exists"); //$NON-NLS-1$
		}

		AmdSecType eventsAmdSec = getAmdSec(AMDSEC_ID_AGENT);
		if (eventsAmdSec == null) {
			eventsAmdSec = getMets().addNewAmdSec();
			eventsAmdSec.setID(AMDSEC_ID_AGENT);
		}

		MdSecType newDigiprovMD = eventsAmdSec.addNewDigiprovMD();
		newDigiprovMD.setID(apoMdSecID);
		MdRef newMdRef = newDigiprovMD.addNewMdRef();
		newMdRef.setLOCTYPE(LOCTYPE.URL);
		newMdRef.setMDTYPE(MDTYPE.PREMIS_AGENT);
		newMdRef.setMIMETYPE("text/xml"); //$NON-NLS-1$

		try {
			newMdRef.setCHECKSUMTYPE(MdRef.CHECKSUMTYPE.MD_5);
			newMdRef.setCHECKSUM(FileUtility.calculateChecksumInHex(apoFile,
					"MD5")); //$NON-NLS-1$
		} catch (Exception e) {
			newMdRef.unsetCHECKSUMTYPE();
			newMdRef.unsetCHECKSUM();
			logger.error("Error calculating checksum", e); //$NON-NLS-1$
		}

		newMdRef.setHref(apoFile.getName());

		return newDigiprovMD;
	}

	private AmdSecType getAmdSec(String amdSecID) {

		AmdSecType resultAmdSec = null;

		if (getMets().getAmdSecList() != null) {
			for (AmdSecType amdSec : getMets().getAmdSecList()) {
				if (amdSecID.equals(amdSec.getID())) {
					resultAmdSec = amdSec;
					break;
				}
			}
		}

		return resultAmdSec;
	}

	private MdSecType getDmdSec(MdSecType parent, String id) {
		MdSecType dmdSec = null;

		String xpathQuery;
		if (parent == null) {
			xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; "
					+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
					+ "/mets:mets/mets:dmdSec[not(@GROUPID) and @ID='" + id
					+ "']";
		} else {
			xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; "
					+ "declare namespace xlink='http://www.w3.org/1999/xlink';"
					+ "/mets:mets/mets:dmdSec[@GROUPID='" + parent.getID()
					+ "' and @ID='" + id + "']";
		}

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);

		if (xmlObjects == null || xmlObjects.length == 0) {
			dmdSec = null;
		} else {
			dmdSec = (MdSecType) xmlObjects[0];

			if (xmlObjects.length > 1) {
				logger.warn("mets:dmdSec with ID='" + id + "' and GROUPID='" //$NON-NLS-1$ //$NON-NLS-2$
						+ parent.getID() + "' is not unique!"); //$NON-NLS-1$
			}
		}

		return dmdSec;
	}

	private MdSecType getDigiprovMD(String admID) {
		MdSecType digiprovSec = null;

		String xpathQuery = "declare namespace mets='http://www.loc.gov/METS/'; " //$NON-NLS-1$
				+ "declare namespace xlink='http://www.w3.org/1999/xlink';" //$NON-NLS-1$
				+ "/mets:mets/mets:amdSec/mets:digiprovMD[@ID='" + admID + "']"; //$NON-NLS-1$ //$NON-NLS-2$

		XmlObject[] xmlObjects = getMets().selectPath(xpathQuery);

		if (xmlObjects == null || xmlObjects.length == 0) {
			digiprovSec = null;
		} else {
			digiprovSec = (MdSecType) xmlObjects[0];

			if (xmlObjects.length > 1) {
				logger.warn("mets:digiprovMD with ID='" + admID //$NON-NLS-1$
						+ "' is not unique!"); //$NON-NLS-1$
			}
		}

		return digiprovSec;

	}

	private void createRepresentationDiv(MdSecType dmdSec,
			DivType representationsDiv, RepresentationObject rObject) {

		DivType div = representationsDiv.addNewDiv();
		div.setID(rObject.getLabel());
		div.setTYPE(rObject.getContentModel());
		div.setDMDID(Arrays.asList(new String[] { dmdSec.getID() }));
		div.setLabel(StringUtility.join(rObject.getStatuses(), ",")); //$NON-NLS-1$

		// Add reference to the root file
		Fptr fptr = div.addNewFptr();
		String rootFileID = rObject.getId() + "-" //$NON-NLS-1$
				+ rObject.getRootFile().getId();
		fptr.setFILEID(rootFileID);

		// Add references to the part files
		for (RepresentationFile rFile : rObject.getPartFiles()) {
			fptr = div.addNewFptr();

			String fFileID = rObject.getId() + "-" + rFile.getId(); //$NON-NLS-1$
			fptr.setFILEID(fFileID);
		}

	}

	private void createFile(FileGrp fileGrp, RepresentationObject rObject,
			RepresentationFile rFile) {
		FileType newFile = fileGrp.addNewFile();

		String fFileID = rObject.getId() + "-" + rFile.getId(); //$NON-NLS-1$
		newFile.setID(fFileID);
		newFile.setMIMETYPE(rFile.getMimetype());

		File file = new File(URI.create(rFile.getAccessURL()));

		try {

			newFile.setCHECKSUMTYPE(CHECKSUMTYPE.MD_5);
			newFile.setCHECKSUM(FileUtility.calculateChecksumInHex(file, "MD5")); //$NON-NLS-1$

		} catch (Exception e) {
			newFile.unsetCHECKSUMTYPE();
			newFile.unsetCHECKSUM();
			logger.error("Error calculating checksum", e); //$NON-NLS-1$
		}

		FLocat fLocat = newFile.addNewFLocat();
		fLocat.setLOCTYPE(gov.loc.mets.FileType.FLocat.LOCTYPE.URL);
		fLocat.setHref(file.getParentFile().getName() + "/" + file.getName()); //$NON-NLS-1$
		fLocat.setTitle(rFile.getOriginalName());
	}

	private FileGrp getFileGrp() {
		FileSec fileSec = getMets().getFileSec();
		if (fileSec == null) {
			fileSec = getMets().addNewFileSec();
		}

		List<FileGrp> fileGrpList = fileSec.getFileGrpList();
		FileGrp fileGrp = null;
		if (fileGrpList == null || fileGrpList.size() == 0) {
			fileGrp = fileSec.addNewFileGrp();
		} else {
			fileGrp = fileGrpList.get(0);
		}

		return fileGrp;
	}

	@SuppressWarnings("unchecked")
	private RepresentationObject getRepresentationObject(DivType repDiv,
			Map<String, FileType> metsFileMap) throws MetsMetadataException {

		RepresentationObject rObject = new RepresentationObject();

		rObject.setId(repDiv.getID());
		rObject.setContentModel(repDiv.getTYPE());

		String[] statuses;
		if (!StringUtils.isBlank(repDiv.getLabel())) {
			statuses = repDiv.getLabel().split(",\\e*"); //$NON-NLS-1$
		} else {
			statuses = new String[] { RepresentationObject.STATUS_ORIGINAL };
			logger.warn("No statuses specified. Setting status ORIGINAL"); //$NON-NLS-1$
		}
		rObject.setStatuses(statuses);

		// Put the DO's ID in the DO's PID field.
		List<String> dmdid = repDiv.getDMDID();
		if (dmdid != null && dmdid.size() > 0) {
			rObject.setDescriptionObjectPID(dmdid.get(0));
		} else {
			throw new MetsMetadataException("Representation " + repDiv.getID() //$NON-NLS-1$
					+ " doesn't have a reference to the description object"); //$NON-NLS-1$
		}

		List<RepresentationFile> repFiles = new ArrayList<RepresentationFile>();
		for (DivType.Fptr repDivFptr : repDiv.getFptrList()) {

			repFiles.add(getRepresentationFile(rObject.getId(),
					metsFileMap.get(repDivFptr.getFILEID())));

			logger.trace("Added RepresentationFile " + repDivFptr.getFILEID()); //$NON-NLS-1$
		}

		if (repFiles.size() > 0) {
			rObject.setRootFile(repFiles.get(0));
			repFiles.remove(0);
		} else {
			throw new MetsMetadataException("Representation " + repDiv.getID() //$NON-NLS-1$
					+ " doesn't have any files"); //$NON-NLS-1$
		}

		rObject.setPartFiles(repFiles.toArray(new RepresentationFile[repFiles
				.size()]));

		return rObject;
	}

	private RepresentationFile getRepresentationFile(String representationID,
			FileType repFileType) throws MetsMetadataException {

		RepresentationFile rFile = new RepresentationFile();

		String fileIDWithoutPrefix = repFileType.getID().substring(
				representationID.length() + 1);
		rFile.setId(fileIDWithoutPrefix);

		rFile.setMimetype(repFileType.getMIMETYPE());

		List<FLocat> flocatList = repFileType.getFLocatList();
		if (flocatList == null || flocatList.size() == 0) {

			throw new MetsMetadataException("Mets file " + repFileType.getID() //$NON-NLS-1$
					+ " doesn't have a FLocat element"); //$NON-NLS-1$

		} else {
			FLocat flocat = flocatList.get(0);

			rFile.setOriginalName(flocat.getTitle());
			rFile.setAccessURL(flocat.getHref());
		}

		return rFile;
	}

	private void removeAllFiles(FileGrp fileGrp) {
		List<FileType> fileList = fileGrp.getFileList();
		for (int i = 0; i < fileList.size(); i++) {
			fileGrp.removeFile(i);
		}
	}

	private void removeAllRepresentations(DivType representationsDiv) {
		List<DivType> divList = representationsDiv.getDivList();
		for (int i = 0; i < divList.size(); i++) {
			representationsDiv.removeDiv(i);
		}
	}

	private Map<String, FileType> getFileMap() {

		Map<String, FileType> fileMap = new HashMap<String, FileType>();

		for (FileType fileType : getFiles()) {
			fileMap.put(fileType.getID(), fileType);
		}

		return fileMap;
	}

}
