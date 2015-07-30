package org.roda.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.roda.common.RodaUtils;
import org.roda.index.IndexService;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Directory;
import org.roda.storage.JsonContentPayload;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import jersey.repackaged.com.google.common.collect.Sets;
import lc.xmlns.premisV2.EventOutcomeDetailComplexType;
import lc.xmlns.premisV2.EventOutcomeInformationComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.data.v2.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisAgentHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisEventHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisMetadataException;
import pt.gov.dgarq.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;

/**
 * Class that "relates" Model & Storage
 * 
 * XXX assumptions:
 * 
 * 1) when creating or updating stuff, metadata will be already set and
 * therefore to instantiate {@link DescriptiveMetadata}, {@link File} and
 * {@link Representation} one just need to read those values from
 * object.getMetadata()
 * 
 * 2) ATM, files beneath a certain representation can be represented as a flat
 * list and therefore no folders are supported. to support folders, we need to
 * re-think how to represent files in a representation (ATM those are
 * represented by a list of strings<=>name) and change all methods that deal
 * with representation
 * 
 * FIXME questions:
 * 
 * 1) how to undo things created/changed upon exceptions??? if using fedora
 * perhaps with transactions
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ModelService extends ModelObservable {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final StorageService storage;

	public ModelService(StorageService storage) {
		super();
		this.storage = storage;
	}

	public StorageService getStorage() {
		return storage;
	}

	public Iterable<AIP> listAIPs() throws ModelServiceException {
		Iterable<AIP> it;

		try {
			final Iterator<Resource> iterator = storage.listResourcesUnderContainer(ModelUtils.getAIPcontainerPath())
					.iterator();

			it = new Iterable<AIP>() {

				@Override
				public Iterator<AIP> iterator() {
					return new Iterator<AIP>() {

						@Override
						public boolean hasNext() {
							if (iterator == null) {
								return false;
							}
							return iterator.hasNext();
						}

						@Override
						public AIP next() {
							try {
								return convertResourceToAIP(iterator.next());
							} catch (ModelServiceException e) {
								// FIXME is this the best way to deal with the
								// ModelServiceException???
								throw new RuntimeException(e.getMessage());
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while obtaining AIP list from storage, reason: " + e.getMessage(),
					e.getCode());
		}

		return it;
	}

	public AIP retrieveAIP(String aipId) throws ModelServiceException {
		AIP aip;
		try {
			Directory directory = storage.getDirectory(ModelUtils.getAIPpath(aipId));
			aip = convertResourceToAIP(directory);
		} catch (StorageActionException e) {
			if (e.getCode() == StorageActionException.NOT_FOUND) {
				throw new ModelServiceException("AIP not found: " + aipId, ModelServiceException.NOT_FOUND, e);
			} else if (e.getCode() == StorageActionException.FORBIDDEN) {
				throw new ModelServiceException("You do not have permission to access AIP: " + aipId,
						ModelServiceException.FORBIDDEN, e);
			} else {
				throw new ModelServiceException("Unexpected error while retrieving AIP",
						ModelServiceException.INTERNAL_SERVER_ERROR, e);
			}

		}
		return aip;
	}

	/**
	 * Create a new AIP
	 * 
	 * @param aipId
	 *            Suggested ID for the AIP, if <code>null</code> then an ID will
	 *            be automatically generated. If ID cannot be allowed because it
	 *            already exists or is not valid, another ID will be provided.
	 * @param sourceStorage
	 * @param sourceContainer
	 * @param sourcePath
	 * @param sourceName
	 * @return
	 * @throws ModelServiceException
	 */
	public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify)
			throws ModelServiceException {
		// TODO verify structure of source AIP and copy it to the storage
		// XXX possible optimization would be to allow move between storage
		// TODO support asReference

		AIP aip;
		try {
			Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (isAIPvalid(sourceDirectory)) {

				storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPpath(aipId));
				Directory newDirectory = storage.getDirectory(ModelUtils.getAIPpath(aipId));

				aip = convertResourceToAIP(newDirectory);
				if (notify) {
					notifyAipCreated(aip);
				}
			} else {
				throw new ModelServiceException("Error while creating AIP, reason: AIP is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error creating AIP in storage", e.getCode(), e);
		}

		return aip;
	}

	public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath)
			throws ModelServiceException {
		return createAIP(aipId, sourceStorage, sourcePath, true);
	}

	// TODO support asReference
	public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath)
			throws ModelServiceException {
		// TODO verify structure of source AIP and update it in the storage
		AIP aip;
		try {
			Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (isAIPvalid(sourceDirectory)) {
				StoragePath aipPath = ModelUtils.getAIPpath(aipId);

				// FIXME is this the best way?
				storage.deleteResource(aipPath);

				storage.copy(sourceStorage, sourcePath, aipPath);
				Directory directoryUpdated = storage.getDirectory(aipPath);

				aip = convertResourceToAIP(directoryUpdated);
				notifyAipUpdated(aip);
			} else {
				throw new ModelServiceException("Error while creating AIP, reason: AIP is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error creating AIP in storage, reason: " + e.getMessage(), e.getCode());
		}

		return aip;
	}

	public void deleteAIP(String aipId) throws ModelServiceException {
		try {
			StoragePath aipPath = ModelUtils.getAIPpath(aipId);

			storage.deleteResource(aipPath);
			notifyAipDeleted(aipId);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error deleting AIP from storage, reason: " + e.getMessage(), e.getCode());
		}
	}

	public Iterable<DescriptiveMetadata> listDescriptiveMetadataBinaries(String aipId) throws ModelServiceException {

		Iterable<DescriptiveMetadata> it;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(ModelUtils.getDescriptiveMetadataPath(aipId)).iterator();

			it = new Iterable<DescriptiveMetadata>() {

				@Override
				public Iterator<DescriptiveMetadata> iterator() {
					return new Iterator<DescriptiveMetadata>() {

						@Override
						public boolean hasNext() {
							if (iterator == null) {
								return false;
							}
							return iterator.hasNext();
						}

						@Override
						public DescriptiveMetadata next() {
							try {
								return convertResourceToDescriptiveMetadata(iterator.next());
							} catch (ModelServiceException e) {
								// FIXME is this the best way to deal with the
								// ModelServiceException???
								throw new RuntimeException(e.getMessage());
							}

						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining descriptive metadata binary list from storage, reason: " + e.getMessage(),
					e.getCode());
		}

		return it;
	}

	public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
			throws ModelServiceException {
		DescriptiveMetadata descriptiveMetadataBinary;

		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);

			Binary binary = storage.getBinary(binaryPath);
			descriptiveMetadataBinary = convertResourceToDescriptiveMetadata(binary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining descriptive metadata binary from storage, reason: " + e.getMessage(),
					e.getCode());
		}

		return descriptiveMetadataBinary;
	}

	// FIXME descriptiveMetadataType shouldn't be a parameter but instead be
	// already present in the Binary metadata
	public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
			String descriptiveMetadataType) throws ModelServiceException {
		DescriptiveMetadata descriptiveMetadataBinary;
		try {
			// StoragePath binaryPath = binary.getStoragePath();
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
			boolean asReference = false;
			Map<String, Set<String>> binaryMetadata = binary.getMetadata();
			binaryMetadata.put(RodaConstants.STORAGE_META_TYPE, Sets.newHashSet(descriptiveMetadataType));

			storage.createBinary(binaryPath, binaryMetadata, binary.getContent(), asReference);
			descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType,
					binaryPath);
			notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error creating descriptive metadata binary in storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return descriptiveMetadataBinary;
	}

	// FIXME descriptiveMetadataType shouldn't be a parameter but instead be
	// already present in the Binary metadata (and therefore to be changed
	// appropriated method should be called)
	public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
			String descriptiveMetadataType) throws ModelServiceException {

		DescriptiveMetadata descriptiveMetadataBinary;
		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
			boolean asReference = false;
			boolean createIfNotExists = false;
			storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);

			Map<String, Set<String>> binaryMetadata = binary.getMetadata();
			binaryMetadata.put(RodaConstants.STORAGE_META_TYPE, Sets.newHashSet(descriptiveMetadataType));
			storage.updateMetadata(binaryPath, binaryMetadata, true);

			descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType,
					binaryPath);
			notifyDescriptiveMetadataUpdated(descriptiveMetadataBinary);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error updating descriptive metadata binary in the storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return descriptiveMetadataBinary;
	}

	public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId) throws ModelServiceException {
		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);

			storage.deleteResource(binaryPath);
			notifyDescriptiveMetadataDeleted(aipId, descriptiveMetadataId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting descriptive metadata binary from storage, reason: " + e.getMessage(), e.getCode());
		}

	}

	public Iterable<Representation> listRepresentations(String aipId) throws ModelServiceException {
		Iterable<Representation> it = null;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId)).iterator();

			it = new Iterable<Representation>() {

				@Override
				public Iterator<Representation> iterator() {
					return new Iterator<Representation>() {

						@Override
						public boolean hasNext() {
							if (iterator == null) {
								return true;
							}
							return iterator.hasNext();
						}

						@Override
						public Representation next() {
							try {
								return convertResourceToRepresentation(iterator.next());
							} catch (ModelServiceException e) {
								// FIXME is this the best way to deal with the
								// ModelServiceException???
								throw new RuntimeException(e.getMessage());
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation list from storage, reason: " + e.getMessage(), e.getCode());
		}

		return it;
	}

	public Representation retrieveRepresentation(String aipId, String representationId) throws ModelServiceException {
		Representation representation;

		try {
			Directory directory = storage.getDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
			representation = convertResourceToRepresentation(directory);

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation from storage, reason: " + e.getMessage(), e.getCode());
		}

		return representation;
	}

	// TODO support asReference
	public Representation createRepresentation(String aipId, String representationId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
		Representation representation;

		try {
			StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId, representationId);

			// verify structure of source representation
			Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (isRepresentationValid(sourceDirectory)) {
				storage.copy(sourceStorage, sourcePath, directoryPath);
				Directory directory = storage.getDirectory(directoryPath);

				representation = convertResourceToRepresentation(directory);
				notifyRepresentationCreated(representation);
			} else {
				throw new ModelServiceException(
						"Error while creating representation, reason: representation is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while creating representation in storage, reason: " + e.getMessage(),
					e.getCode());
		}

		return representation;
	}

	public Representation updateRepresentation(String aipId, String representationId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
		Representation representation;
		Directory sourceDirectory;

		// verify structure of source representation
		try {
			sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (!isRepresentationValid(sourceDirectory)) {
				throw new ModelServiceException("Error while updating AIP, reason: representation is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while updating representation in storage, reason: " + e.getMessage(),
					e.getCode());
		}

		// update each representation file (from source representation)
		final List<String> fileIDsToUpdate = new ArrayList<String>();
		try {
			Iterable<Resource> files = sourceStorage.listResourcesUnderDirectory(sourcePath);
			for (Resource file : files) {
				if (file instanceof DefaultBinary) {
					boolean createIfNotExists = true;
					boolean notify = false;
					File fileUpdated = updateFile(aipId, representationId, file.getStoragePath().getName(),
							(Binary) file, createIfNotExists, notify);

					fileIDsToUpdate.add(fileUpdated.getStoragePath().getName());
				} else {
					// FIXME log error and continue???
				}
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while updating representation files",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		// delete files that were removed on representation update
		try {
			Iterable<Resource> filesToRemove = storage
					.listResourcesUnderDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
			for (Resource fileToRemove : filesToRemove) {
				StoragePath fileToRemovePath = fileToRemove.getStoragePath();
				if (!fileIDsToUpdate.contains(fileToRemovePath.getName())) {
					storage.deleteResource(fileToRemovePath);
				}
			}

		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while delete removed representation files",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		// get representation metadata (from source representation)
		Map<String, Set<String>> representationMetadata = sourceDirectory.getMetadata();

		// obtain information (from metadata) to build representation object
		boolean active = ModelUtils.getBoolean(representationMetadata, RodaConstants.STORAGE_META_ACTIVE);
		Date dateCreated = ModelUtils.getDate(representationMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
		Date dateModified = new Date();
		representationMetadata.put(RodaConstants.STORAGE_META_DATE_MODIFIED,
				Sets.newHashSet(RodaUtils.dateToString(dateModified)));
		String type = ModelUtils.getString(representationMetadata, RodaConstants.STORAGE_META_TYPE);
		Set<RepresentationState> statuses = ModelUtils.getStatuses(representationMetadata);

		// update representation metadata (essentially date.modified)
		try {
			storage.updateMetadata(ModelUtils.getRepresentationPath(aipId, representationId), representationMetadata,
					true);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while updating representation metadata",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		representation = new Representation(representationId, aipId, active, dateCreated, dateModified, statuses, type,
				fileIDsToUpdate);
		notifyRepresentationUpdated(representation);
		return representation;
	}

	public void deleteRepresentation(String aipId, String representationId) throws ModelServiceException {
		try {
			StoragePath representationPath = ModelUtils.getRepresentationPath(aipId, representationId);

			storage.deleteResource(representationPath);
			notifyRepresentationDeleted(aipId, representationId);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error deleting representation from storage, reason: " + e.getMessage(),
					e.getCode());
		}
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public Iterable<File> listFiles(String aipId, String representationId) throws ModelServiceException {
		Iterable<File> it = null;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId)).iterator();

			it = new Iterable<File>() {

				@Override
				public Iterator<File> iterator() {
					return new Iterator<File>() {

						@Override
						public boolean hasNext() {
							if (iterator == null) {
								return true;
							}
							return iterator.hasNext();
						}

						@Override
						public File next() {
							try {
								return convertResourceToRepresentationFile(iterator.next());
							} catch (ModelServiceException e) {
								// FIXME is this the best way to deal with the
								// ModelServiceException???
								throw new RuntimeException(e.getMessage());
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining representation files from storage, reason: " + e.getMessage(), e.getCode());
		}

		return it;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File retrieveFile(String aipId, String representationId, String fileId) throws ModelServiceException {
		File file;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

			Binary binary = storage.getBinary(filePath);
			file = convertResourceToRepresentationFile(binary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining representation file from storage, reason: " + e.getMessage(), e.getCode());
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File createFile(String aipId, String representationId, String fileId, Binary binary)
			throws ModelServiceException {
		File file;
		// FIXME how to set this?
		boolean asReference = false;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

			final Binary createdBinary = storage.createBinary(filePath, binary.getMetadata(), binary.getContent(),
					asReference);
			file = convertResourceToRepresentationFile(createdBinary);
			notifyFileCreated(file);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error creating representation file in storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File updateFile(String aipId, String representationId, String fileId, Binary binary,
			boolean createIfNotExists, boolean notify) throws ModelServiceException {
		File file = null;
		// FIXME how to set this?
		boolean asReference = false;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

			storage.updateBinaryContent(filePath, binary.getContent(), asReference, createIfNotExists);
			storage.updateMetadata(filePath, binary.getMetadata(), true);
			Binary binaryUpdated = storage.getBinary(filePath);
			file = convertResourceToRepresentationFile(binaryUpdated);
			if (notify) {
				notifyFileUpdated(file);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while updating representation file",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public void deleteFile(String aipId, String representationId, String fileId) throws ModelServiceException {
		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

			storage.deleteResource(filePath);
			notifyFileDeleted(aipId, representationId, fileId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting representation file from storage, reason: " + e.getMessage(), e.getCode());
		}
	}

	// TODO to improve...
	public Iterable<RepresentationPreservationObject> getAipPreservationObjects(String aipId)
			throws ModelServiceException {
		Iterable<RepresentationPreservationObject> it = null;
		final List<RepresentationPreservationObject> rpos = new ArrayList<RepresentationPreservationObject>();
		try {
			final Iterator<Resource> resourceIterator = storage
					.listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId)).iterator();
			while (resourceIterator.hasNext()) {
				Resource resource = resourceIterator.next();
				Iterator<Resource> preservationIterator = storage.listResourcesUnderDirectory(
						ModelUtils.getPreservationPath(aipId, resource.getStoragePath().getName())).iterator();
				while (preservationIterator.hasNext()) {
					Resource preservationObject = preservationIterator.next();
					Binary preservationBinary = storage.getBinary(preservationObject.getStoragePath());
					if (ModelUtils.isPreservationRepresentationObject(preservationBinary)) {
						rpos.add(convertResourceToRepresentationPreservationObject(aipId,
								resource.getStoragePath().getName(), preservationObject.getStoragePath().getName(),
								preservationBinary));
					}

				}
			}
			it = new Iterable<RepresentationPreservationObject>() {
				@Override
				public Iterator<RepresentationPreservationObject> iterator() {
					return rpos.iterator();
				}
			};
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while obtaining AIP preservation objects, reason: " + e.getMessage(),
					e.getCode());
		}
		return it;
	}

	public RepresentationPreservationObject getRepresentationPreservationObject(String aipId, String representationId,
			String fileId) throws ModelServiceException {
		RepresentationPreservationObject obj = null;
		try {
			StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
			Binary b = storage.getBinary(sp);
			obj = convertResourceToRepresentationPreservationObject(aipId, representationId, fileId, b);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while getting representation preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}
		return obj;
	}

	public EventPreservationObject getEventPreservationObject(String aipId, String representationId,
			String preservationObjectID) throws ModelServiceException {
		EventPreservationObject obj = null;
		try {
			StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, preservationObjectID);
			Binary b = storage.getBinary(sp);
			obj = convertResourceToEventPreservationObject(aipId, representationId, preservationObjectID, b);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while getting event preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return obj;
	}

	public AgentPreservationObject getAgentPreservationObject(String agentID) throws ModelServiceException {
		AgentPreservationObject apo = null;
		try {
			StoragePath sp = ModelUtils.getPreservationAgentPath(agentID);
			Binary b = storage.getBinary(sp);
			apo = convertResourceToAgentPreservationObject(agentID, b);
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error while getting agent preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}
		return apo;
	}

	private AgentPreservationObject convertResourceToAgentPreservationObject(String agentID, Binary resource)
			throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			try {
				Map<String, Set<String>> directoryMetadata = resource.getMetadata();

				// retrieve needed information to instantiate Representation
				Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
				Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
				Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

				if (active == null) {
					// when not stated, considering active=false
					active = false;
				}
				PremisAgentHelper pah = PremisAgentHelper.newInstance(resource.getContent().createInputStream());
				AgentPreservationObject apo = new AgentPreservationObject();
				apo.setAgentName(
						(pah.getAgent().getAgentNameList() != null && pah.getAgent().getAgentNameList().size() > 0)
								? pah.getAgent().getAgentNameList().get(0) : "");
				apo.setAgentType(pah.getAgent().getAgentType());
				apo.setFileID(agentID);
				apo.setID((pah.getAgent().getAgentIdentifierList() != null
						&& pah.getAgent().getAgentIdentifierList().size() > 0)
								? pah.getAgent().getAgentIdentifierList().get(0).getAgentIdentifierValue() : "");
				apo.setType(""); // TODO: ??????????
				return apo;
			} catch (PremisMetadataException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new ModelServiceException(
					"Error while trying to convert a binary into a representation preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private boolean isAIPvalid(Directory directory) {
		// FIXME implement this
		return true;
	}

	private boolean isRepresentationValid(Directory directory) {
		// FIXME implement this
		return true;
	}

	private AIP convertResourceToAIP(Resource resource) throws ModelServiceException {
		AIP aip;
		if (resource instanceof DefaultDirectory) {
			StoragePath storagePath = resource.getStoragePath();
			Map<String, Set<String>> metadata = resource.getMetadata();

			// obtain basic AIP information
			String parentId = ModelUtils.getString(metadata, RodaConstants.STORAGE_META_PARENT_ID);
			Boolean active = ModelUtils.getBoolean(metadata, RodaConstants.STORAGE_META_ACTIVE);
			Date dateCreated = ModelUtils.getDate(metadata, RodaConstants.STORAGE_META_DATE_CREATED);
			Date dateModified = ModelUtils.getDate(metadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

			// TODO retrieve permissions

			if (active == null) {
				// when not stated, consider active=false
				active = false;
			}

			try {
				// obtain descriptive metadata information
				List<String> descriptiveMetadataBinaryIds = ModelUtils.getIds(storage,
						ModelUtils.getDescriptiveMetadataPath(storagePath.getName()));

				// obtain representations information
				List<String> representationIds = ModelUtils.getIds(storage,
						ModelUtils.getRepresentationsPath(storagePath.getName()));

				// obtain preservation information
				final Map<String, List<String>> preservationRepresentationObjects = new HashMap<String, List<String>>();
				final Map<String, List<String>> preservationFileObjects = new HashMap<String, List<String>>();
				final Map<String, List<String>> preservationEvents = new HashMap<String, List<String>>();
				retrieveAIPPreservationInformation(storagePath, representationIds, preservationRepresentationObjects,
						preservationFileObjects, preservationEvents);

				aip = new AIP(storagePath.getName(), parentId, active, dateCreated, dateModified,
						descriptiveMetadataBinaryIds, representationIds, preservationRepresentationObjects,
						preservationEvents, preservationFileObjects);
			} catch (StorageActionException e) {
				throw new ModelServiceException(
						"Error while obtaining information to instantiate an AIP, reason: " + e.getMessage(),
						e.getCode());
			}
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Directory into an AIP",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
		return aip;
	}

	private void retrieveAIPPreservationInformation(StoragePath storagePath, List<String> representationIds,
			final Map<String, List<String>> preservationRepresentationObjects,
			final Map<String, List<String>> preservationFileObjects,
			final Map<String, List<String>> preservationEvents) {
		for (String representationID : representationIds) {
			try {
				StoragePath representationPreservationPath = ModelUtils.getPreservationPath(storagePath.getName(),
						representationID);
				// obtain list of preservation related files
				List<String> preservationFileIds = ModelUtils.getIds(storage, representationPreservationPath);

				final List<String> preservationRepresentationObjectFileIds = new ArrayList<String>();
				final List<String> preservationFileObjectFileIds = new ArrayList<String>();
				final List<String> preservationEventFileIds = new ArrayList<String>();

				for (String preservationFileId : preservationFileIds) {
					StoragePath binaryPath = ModelUtils.getPreservationFilePath(storagePath.getName(), representationID,
							preservationFileId);
					Binary preservationBinary = storage.getBinary(binaryPath);

					if (ModelUtils.isPreservationRepresentationObject(preservationBinary)) {
						preservationRepresentationObjectFileIds.add(preservationFileId);
					} else if (ModelUtils.isPreservationEvent(preservationBinary)) {
						preservationEventFileIds.add(preservationFileId);
					} else if (ModelUtils.isPreservationFileObject(preservationBinary)) {
						preservationFileObjectFileIds.add(preservationFileId);
					} else {
						logger.warn(
								"The binary {} is neither a PreservationRepresentationObject or PreservationEvent or PreservationFileObject...Moving on...",
								binaryPath.asString());
					}
				}
				preservationRepresentationObjects.put(representationID, preservationRepresentationObjectFileIds);
				preservationFileObjects.put(representationID, preservationFileObjectFileIds);
				preservationEvents.put(representationID, preservationEventFileIds);
			} catch (StorageActionException e) {
				logger.error("Error while obtaining preservation related binaries", e);
			}
		}
	}

	private DescriptiveMetadata convertResourceToDescriptiveMetadata(Resource resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			// retrieve needed information to instantiate DescriptiveMetadata
			String type = ModelUtils.getString(resource.getMetadata(), RodaConstants.STORAGE_META_TYPE);

			return new DescriptiveMetadata(resource.getStoragePath().getName(),
					ModelUtils.getAIPidFromStoragePath(resource.getStoragePath()), type, resource.getStoragePath());
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Binary into a descriptive metadata binary",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private Representation convertResourceToRepresentation(Resource resource) throws ModelServiceException {
		if (resource instanceof DefaultDirectory) {
			StoragePath directoryPath = resource.getStoragePath();
			Map<String, Set<String>> directoryMetadata = resource.getMetadata();

			// retrieve needed information to instantiate Representation
			Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
			Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
			Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);
			Set<RepresentationState> statuses = ModelUtils.getStatuses(directoryMetadata);
			String type = ModelUtils.getString(directoryMetadata, RodaConstants.STORAGE_META_TYPE);
			List<String> fileIds = new ArrayList<String>();
			try {
				fileIds = ModelUtils.getIds(storage, resource.getStoragePath());
			} catch (StorageActionException e) {
				// FIXME log error but continue???
			}

			if (active == null) {
				// when not stated, considering active=false
				active = false;
			}

			return new Representation(directoryPath.getName(), ModelUtils.getAIPidFromStoragePath(directoryPath),
					active, dateCreated, dateModified, statuses, type, fileIds);

		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Directory into a representation",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private File convertResourceToRepresentationFile(Resource resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			StoragePath binaryPath = resource.getStoragePath();
			Map<String, Set<String>> metadata = resource.getMetadata();

			// retrieve needed information to instantiate File
			Boolean entryPoint = ModelUtils.getBoolean(metadata, RodaConstants.STORAGE_META_ENTRYPOINT);
			FileFormat fileFormat = ModelUtils.getFileFormat(metadata);

			if (entryPoint == null) {
				// if entry point not defined, considering false
				entryPoint = false;
			}

			return new File(binaryPath.getName(), ModelUtils.getAIPidFromStoragePath(binaryPath),
					ModelUtils.getRepresentationIdFromStoragePath(binaryPath), entryPoint, fileFormat, binaryPath);
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Binary into a representation file",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	public RepresentationPreservationObject retrieveRepresentationPreservationObject(String aipId,
			String representationId, String fileId) throws ModelServiceException {
		RepresentationPreservationObject representationPreservationObject = null;
		try {
			StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
			Binary binary = storage.getBinary(filePath);
			representationPreservationObject = convertResourceToRepresentationPreservationObject(aipId,
					representationId, fileId, binary);
			representationPreservationObject.setId(fileId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation from storage, reason: " + e.getMessage(), e.getCode());
		}
		return representationPreservationObject;
	}

	public RepresentationFilePreservationObject retrieveRepresentationFileObject(String aipId, String representationId,
			String fileId) throws ModelServiceException {
		RepresentationFilePreservationObject representationPreservationObject = null;
		try {
			StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
			Binary binary = storage.getBinary(filePath);
			representationPreservationObject = convertResourceToRepresentationFilePreservationObject(aipId,
					representationId, fileId, binary);
			representationPreservationObject.setId(fileId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation File from storage, reason: " + e.getMessage(), e.getCode());
		}
		return representationPreservationObject;
	}

	private RepresentationFilePreservationObject convertResourceToRepresentationFilePreservationObject(String aipId,
			String representationId, String fileId, Binary resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			try {
				Map<String, Set<String>> directoryMetadata = resource.getMetadata();

				// retrieve needed information to instantiate Representation
				Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
				Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
				Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

				if (active == null) {
					// when not stated, considering active=false
					active = false;
				}

				PremisFileObjectHelper pfoh = PremisFileObjectHelper
						.newInstance(resource.getContent().createInputStream());

				RepresentationFilePreservationObject rfpo = new RepresentationFilePreservationObject();
				rfpo.setAipId(aipId);
				rfpo.setRepresentationId(representationId);
				rfpo.setFileId(fileId);
				rfpo.setCompositionLevel(pfoh.getRepresentationFilePreservationObject().getCompositionLevel());
				rfpo.setContentLocationType(pfoh.getRepresentationFilePreservationObject().getContentLocationType());
				rfpo.setContentLocationValue(pfoh.getRepresentationFilePreservationObject().getContentLocationValue());
				rfpo.setCreatedDate(pfoh.getRepresentationFilePreservationObject().getCreatedDate());
				rfpo.setCreatingApplicationName(
						pfoh.getRepresentationFilePreservationObject().getCreatingApplicationName());
				rfpo.setCreatingApplicationVersion(
						pfoh.getRepresentationFilePreservationObject().getCreatingApplicationVersion());
				rfpo.setDateCreatedByApplication(
						pfoh.getRepresentationFilePreservationObject().getDateCreatedByApplication());
				rfpo.setFileId(fileId);
				rfpo.setFixities(pfoh.getRepresentationFilePreservationObject().getFixities());
				rfpo.setFormatDesignationName(
						pfoh.getRepresentationFilePreservationObject().getFormatDesignationName());
				rfpo.setFormatDesignationVersion(
						pfoh.getRepresentationFilePreservationObject().getFormatDesignationVersion());
				rfpo.setFormatRegistryKey(pfoh.getRepresentationFilePreservationObject().getFormatRegistryKey());
				rfpo.setFormatRegistryName(pfoh.getRepresentationFilePreservationObject().getFormatRegistryName());
				rfpo.setFormatRegistryRole(pfoh.getRepresentationFilePreservationObject().getFormatRegistryRole());
				rfpo.setHash(pfoh.getRepresentationFilePreservationObject().getHash());
				rfpo.setID(pfoh.getRepresentationFilePreservationObject().getID());
				rfpo.setId(pfoh.getRepresentationFilePreservationObject().getId());
				rfpo.setLabel(pfoh.getRepresentationFilePreservationObject().getLabel());
				rfpo.setLastModifiedDate(pfoh.getRepresentationFilePreservationObject().getLastModifiedDate());
				rfpo.setMimetype(pfoh.getRepresentationFilePreservationObject().getMimetype());
				rfpo.setModel(pfoh.getRepresentationFilePreservationObject().getModel());
				rfpo.setObjectCharacteristicsExtension(
						pfoh.getRepresentationFilePreservationObject().getObjectCharacteristicsExtension());
				rfpo.setOriginalName(pfoh.getRepresentationFilePreservationObject().getOriginalName());
				rfpo.setPreservationLevel(pfoh.getRepresentationFilePreservationObject().getPreservationLevel());
				rfpo.setPronomId(pfoh.getRepresentationFilePreservationObject().getPronomId());
				rfpo.setRepresentationObjectId(
						pfoh.getRepresentationFilePreservationObject().getRepresentationObjectId());
				rfpo.setSize(pfoh.getRepresentationFilePreservationObject().getSize());
				rfpo.setState(pfoh.getRepresentationFilePreservationObject().getState());
				rfpo.setType(pfoh.getRepresentationFilePreservationObject().getType());
				rfpo.setRepresentationId(representationId);
				return rfpo;

			} catch (PremisMetadataException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new ModelServiceException(
					"Error while trying to convert a binary into a representation preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private RepresentationPreservationObject convertResourceToRepresentationPreservationObject(String aipId,
			String representationId, String fileId, Binary resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			try {
				Map<String, Set<String>> directoryMetadata = resource.getMetadata();

				// retrieve needed information to instantiate Representation
				Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
				Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
				Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

				if (active == null) {
					// when not stated, considering active=false
					active = false;
				}

				PremisRepresentationObjectHelper proh = PremisRepresentationObjectHelper
						.newInstance(resource.getContent().createInputStream());
				RepresentationPreservationObject rpo = new RepresentationPreservationObject();
				rpo.setAipId(aipId);
				rpo.setRepresentationId(representationId);
				rpo.setFileId(fileId);
				rpo.setCreatedDate(proh.getRepresentationPreservationObject().getCreatedDate());
				rpo.setDerivationEventID(proh.getRepresentationPreservationObject().getDerivationEventID());
				rpo.setDerivedFromRepresentationObjectID(
						proh.getRepresentationPreservationObject().getDerivedFromRepresentationObjectID());
				rpo.setId(proh.getRepresentationPreservationObject().getId());
				rpo.setID(proh.getRepresentationPreservationObject().getID());
				rpo.setLabel(proh.getRepresentationPreservationObject().getLabel());
				rpo.setLastModifiedDate(proh.getRepresentationPreservationObject().getLastModifiedDate());
				rpo.setModel(proh.getRepresentationPreservationObject().getModel());
				rpo.setPartFiles(proh.getRepresentationPreservationObject().getPartFiles());
				rpo.setPreservationEventIDs(proh.getRepresentationPreservationObject().getPreservationEventIDs());
				rpo.setPreservationLevel(proh.getRepresentationPreservationObject().getPreservationLevel());
				rpo.setRepresentationObjectID(proh.getRepresentationPreservationObject().getRepresentationObjectID());
				rpo.setRootFile(proh.getRepresentationPreservationObject().getRootFile());
				rpo.setState(proh.getRepresentationPreservationObject().getState());
				rpo.setType(proh.getRepresentationPreservationObject().getType());
				return rpo;
			} catch (PremisMetadataException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a representation preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new ModelServiceException(
					"Error while trying to convert a binary into a representation preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	public EventPreservationObject retrieveEventPreservationObject(String aipId, String representationId, String fileId)
			throws ModelServiceException {
		EventPreservationObject eventPreservationObject = null;
		try {
			StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
			Binary binary = storage.getBinary(filePath);
			eventPreservationObject = convertResourceToEventPreservationObject(aipId, representationId, fileId, binary);
			eventPreservationObject.setId(fileId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation from storage, reason: " + e.getMessage(), e.getCode());
		}
		return eventPreservationObject;
	}

	private EventPreservationObject convertResourceToEventPreservationObject(String aipId, String representationId,
			String fileId, Binary resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			try {
				Map<String, Set<String>> directoryMetadata = resource.getMetadata();
				// retrieve needed information to instantiate Representation
				Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
				Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
				Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

				if (active == null) {
					active = false;
				}

				PremisEventHelper peh = PremisEventHelper.newInstance(resource.getContent().createInputStream());

				// TODO premisevent to EventPreservationObject
				EventPreservationObject epo = new EventPreservationObject();
				epo.setAipId(aipId);
				epo.setRepresentationId(representationId);
				epo.setFileId(fileId);
				epo.setLastModifiedDate(dateModified);
				epo.setCreatedDate(dateCreated);
				epo.setAgentID((peh.getEvent().getLinkingAgentIdentifierList() != null
						&& peh.getEvent().getLinkingAgentIdentifierList().size() > 0)
								? peh.getEvent().getLinkingAgentIdentifierList().get(0).getLinkingAgentIdentifierValue()
								: null);
				epo.setAgentRole((peh.getEvent().getLinkingAgentIdentifierList() != null
						&& peh.getEvent().getLinkingAgentIdentifierList().size() > 0)
								? peh.getEvent().getLinkingAgentIdentifierList().get(0).getRole() : null);
				epo.setDate(new Date()); // TODO: ????
				try {
					epo.setDatetime(DateParser.parse(peh.getEvent().getEventDateTime().toString()));
				} catch (InvalidDateException ide) {
					epo.setDatetime(new Date());
				}
				epo.setDescription(""); // TODO: ????
				epo.setEventDetail(peh.getEvent().getEventDetail());
				epo.setEventType(peh.getEvent().getEventType());
				epo.setFileId(fileId);
				epo.setId(fileId);
				epo.setID(""); // TODO: ???
				epo.setLabel(""); // TODO: ???
				epo.setModel(""); // TODO: ???
				epo.setName(""); // TODO: ???
				List<LinkingObjectIdentifierComplexType> linkingObjects = peh.getEvent()
						.getLinkingObjectIdentifierList();
				if (linkingObjects != null && linkingObjects.size() > 0) {
					List<String> objectIds = new ArrayList<String>();
					for (LinkingObjectIdentifierComplexType loi : linkingObjects) {
						objectIds.add(loi.getTitle());
					}
					epo.setObjectIDs(objectIds.toArray(new String[objectIds.size()]));
				} else {
					epo.setObjectIDs(null);
				}
				epo.setOutcome(peh.getEvent().getEventOutcomeInformationList().get(0).getEventOutcome());
				if (peh.getEvent().getEventOutcomeInformationList() != null
						&& peh.getEvent().getEventOutcomeInformationList().size() > 0) {
					EventOutcomeInformationComplexType eoict = peh.getEvent().getEventOutcomeInformationList().get(0);
					epo.setOutcome(eoict.getEventOutcome());
					if (eoict.getEventOutcomeDetailList() != null && eoict.getEventOutcomeDetailList().size() > 0) {
						EventOutcomeDetailComplexType eodc = eoict.getEventOutcomeDetailList().get(0);
						epo.setOutcomeDetailExtension(eodc.getEventOutcomeDetailExtension().toString());
						epo.setOutcomeDetailNote(eodc.getEventOutcomeDetailNote());
					} else {
						epo.setOutcomeDetailExtension("");
						epo.setOutcomeDetailNote("");
					}
				} else {
					epo.setOutcome("");
					epo.setOutcomeDetailExtension("");
					epo.setOutcomeDetailNote("");
				}
				epo.setOutcomeDetails(""); // TODO: ???
				epo.setOutcomeResult(""); // TODO: ???
				epo.setState(""); // TODO: ???
				epo.setTargetID(""); // TODO: ???
				epo.setType(peh.getEvent().getEventType());
				return epo;
			} catch (PremisMetadataException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a event preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				throw new ModelServiceException(
						"Error while trying to convert a binary into a event preservation object",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new ModelServiceException("Error while trying to convert a binary into a event preservation object",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	// FIXME all the initialization, if needed, should be done only once
	public void addLogEntry(LogEntry logEntry, boolean notify) throws StorageActionException {
		Binary dailyLog;

		try {
			storage.createContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG),
					new HashMap<String, Set<String>>());
		} catch (StorageActionException sae) {
			// container already exists...
		}
		StoragePath logPath = ModelUtils.getLogPath(new Date());
		logEntry.setFileID(logPath.getName());
		try {
			dailyLog = storage.getBinary(logPath);
		} catch (StorageActionException sae) {
			dailyLog = storage.createBinary(logPath, new HashMap<String, Set<String>>(), new JsonContentPayload(""),
					false);
		}
		try {

			String entryJSON = ModelUtils.getJsonLogEntry(logEntry);
			java.io.File f = new java.io.File(dailyLog.getContent().getURI().getPath());
			FileUtils.writeStringToFile(f, entryJSON, true);
			FileUtils.writeStringToFile(f, "\n", true);
		} catch (IOException e) {

		}
		if (notify) {
			notifyLogEntryCreated(logEntry);
		}
	}

	public void addLogEntry(LogEntry logEntry) throws StorageActionException {
		addLogEntry(logEntry, true);
	}

	public void reindexAIPs() throws ModelServiceException {
		System.out.println("Listing AIPs");
		Iterable<AIP> aips = listAIPs();
		for (AIP aip : aips) {
			System.out.println("Reindexing AIP " + aip.getId());
			reindexAIP(aip);
		}
		System.out.println("Done");
	}

	private void reindexAIP(AIP aip) {
		notifyAipCreated(aip);
	}

	public void reindexActionLogs() throws StorageActionException, ModelServiceException {
		Iterable<Resource> actionLogs = getStorage()
				.listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG));
		Iterator<Resource> it = actionLogs.iterator();
		while (it.hasNext()) {
			Resource r = it.next();
			try {
				Binary b = getStorage().getBinary(r.getStoragePath());
				java.io.File f = new java.io.File(b.getContent().getURI().getPath());
				try (BufferedReader br = new BufferedReader(new FileReader(f))) {
					String line;
					while ((line = br.readLine()) != null) {
						LogEntry entry = ModelUtils.getLogEntry(line);
						reindexActionLog(entry);
					}
				}
			} catch (IOException e) {
				throw new ModelServiceException("Error parsing log file: " + e.getMessage(), 100);
			}
		}
	}

	private void reindexActionLog(LogEntry entry) {
		notifyLogEntryCreated(entry);
	}

}
