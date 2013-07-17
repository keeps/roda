package pt.gov.dgarq.roda.core.metadata.premis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lc.xmlns.premisV2.AgentComplexType;
import lc.xmlns.premisV2.AgentDocument;
import lc.xmlns.premisV2.AgentIdentifierComplexType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;

/**
 * @author Rui Castro
 */
public class PremisAgentHelper {

	private static final Logger logger = Logger
			.getLogger(PremisAgentHelper.class);

	private final AgentDocument agentDocument;

	/**
	 * Creates a new instance of a {@link PremisAgentHelper} for the PREMIS file
	 * inside the given file.
	 * 
	 * @param premisFile
	 *            the PREMIS XML file.
	 * 
	 * @return a {@link PremisAgentHelper} for the given PREMIS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisAgentHelper newInstance(File premisFile)
			throws PremisMetadataException, FileNotFoundException, IOException {
		FileInputStream premisInputStream = new FileInputStream(premisFile);
		PremisAgentHelper instance = newInstance(premisInputStream);
		premisInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link PremisAgentHelper} for the PREMIS XML
	 * inside the given {@link InputStream}.
	 * 
	 * @param premisInputStream
	 *            the PREMIS XML {@link InputStream}.
	 * 
	 * @return a {@link PremisAgentHelper} for the given PREMIS XML
	 *         {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisAgentHelper newInstance(InputStream premisInputStream)
			throws PremisMetadataException, IOException {

		try {

			AgentDocument document = AgentDocument.Factory
					.parse(premisInputStream);
			if (document.validate()) {
				return new PremisAgentHelper(document);
			} else {
				throw new PremisMetadataException(
						"Error validating XML document");
			}

		} catch (XmlException e) {
			logger.debug("Error parsing PREMIS - " + e.getMessage(), e);
			throw new PremisMetadataException("Error parsing PREMIS - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Constructs a new {@link PremisAgentHelper} with a new
	 * {@link AgentDocument}.
	 */
	public PremisAgentHelper() {
		this(AgentDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link PremisAgentHelper} with an empty PREMIS document
	 * and sets the information inside the given {@link AgentPreservationObject}
	 * .
	 * 
	 * @param agentPObject
	 *            the {@link AgentPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisAgentHelper(AgentPreservationObject agentPObject)
			throws PremisMetadataException {
		this();
		setAgentPreservationObject(agentPObject);
	}

	/**
	 * Create a new PREMIS helper
	 * 
	 * @param agentDocument
	 *            the PREMIS {@link AgentDocument}.
	 */
	private PremisAgentHelper(AgentDocument agentDocument) {

		this.agentDocument = agentDocument;

		if (getAgent() == null) {
			setAgent(AgentComplexType.Factory.newInstance());
		}

	}

	/**
	 * @return the current {@link AgentDocument}.
	 */
	public AgentDocument getAgentDocument() {
		return agentDocument;
	}

	/**
	 * @return the current {@link AgentComplexType}.
	 */
	public AgentComplexType getAgent() {
		return getAgentDocument().getAgent();
	}

	/**
	 * Returns the information about a {@link AgentPreservationObject} from a
	 * PREMIS XML file.
	 * 
	 * @return a {@link AgentPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public AgentPreservationObject getAgentPreservationObject()
			throws PremisMetadataException {

		AgentPreservationObject pObject = new AgentPreservationObject();

		// <agentIdentifier>
		if (getAgent().getAgentIdentifierList() != null
				&& getAgent().getAgentIdentifierList().size() > 0) {

			AgentIdentifierComplexType eventIdentifier = getAgent()
					.getAgentIdentifierArray(0);
			pObject.setID(eventIdentifier.getAgentIdentifierValue());

		} else {
			logger.warn("PREMIS Agent doesn't have an ID");
		}

		// <agentName>
		pObject.setAgentName(getAgent().getAgentNameList().get(0));

		// <agentType>
		if (getAgent().getAgentType() != null) {
			pObject.setAgentType(getAgent().getAgentType());
		}

		return pObject;
	}

	/**
	 * Returns the information about a {@link AgentPreservationObject} from a
	 * PREMIS XML file and copy the values from the given {@link RODAObject}.
	 * 
	 * @param agentObject
	 *            the {@link RODAObject} of the {@link AgentPreservationObject}.
	 * 
	 * @return a {@link AgentPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public AgentPreservationObject getAgentPreservationObject(
			RODAObject agentObject) throws PremisMetadataException {

		AgentPreservationObject agent = getAgentPreservationObject();

		// Copy the values from the RODAObject
		agent.setPid(agentObject.getPid());
		agent.setLabel(agentObject.getLabel());
		agent.setContentModel(agentObject.getContentModel());
		agent.setLastModifiedDate(agentObject.getLastModifiedDate());
		agent.setCreatedDate(agentObject.getCreatedDate());
		agent.setState(agentObject.getState());

		return agent;
	}

	/**
	 * Replaces the current PREMIS XML data for the data inside the given
	 * {@link AgentPreservationObject}.
	 * 
	 * @param agentPObject
	 * 
	 * @throws PremisMetadataException
	 */
	public void setAgentPreservationObject(AgentPreservationObject agentPObject)
			throws PremisMetadataException {

		// Replaces the current agent with a new empty <agent>
		setAgent(AgentComplexType.Factory.newInstance());

		// <agentIdentifier>
		AgentIdentifierComplexType agentIdentifier = getAgent()
				.addNewAgentIdentifier();
		agentIdentifier
				.setAgentIdentifierType(PremisHelper.premisIdentifierTypePID);
		agentIdentifier.setAgentIdentifierValue(agentPObject.getID());

		// <agentName>
		if (!StringUtils.isBlank(agentPObject.getAgentName())) {
			getAgent().addAgentName(agentPObject.getAgentName());
		}

		// <agentType>
		if (!StringUtils.isBlank(agentPObject.getAgentType())) {
			getAgent().setAgentType(agentPObject.getAgentType());
		}

	}

	/**
	 * Saves the current PREMIS document to a byte array.
	 * 
	 * @return a <code>byte[]</code> with the contents of the PREMIS XML file.
	 * 
	 * @throws PremisMetadataException
	 *             if the PREMIS document is not valid or if something goes
	 *             wrong with the serialisation.
	 */
	public byte[] saveToByteArray() throws PremisMetadataException {

		try {

			return MetadataHelperUtility.saveToByteArray(getAgentDocument());

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PremisMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Saves the current PREMIS document to a {@link File}.
	 * 
	 * @param premisFile
	 *            the {@link File}.
	 * 
	 * @throws PremisMetadataException
	 *             if the EAD-C document is not valid or if something goes wrong
	 *             with the serialisation.
	 * 
	 * @throws IOException
	 *             if {@link FileOutputStream} associated with the {@link File}
	 *             couldn't be closed.
	 * @throws IOException
	 */
	public void saveToFile(File premisFile) throws PremisMetadataException,
			IOException {
		try {

			MetadataHelperUtility.saveToFile(getAgentDocument(), premisFile);

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PremisMetadataException(e.getMessage(), e);
		}
	}

	private void setAgent(AgentComplexType agent) {
		getAgentDocument().setAgent(agent);
	}

}
