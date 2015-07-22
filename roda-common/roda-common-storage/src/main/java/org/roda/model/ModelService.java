package org.roda.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.Sets;
import pt.gov.dgarq.roda.core.common.RodaConstants;

import org.roda.common.RodaUtils;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.Directory;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;

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
 * */
public class ModelService extends ModelObservable {

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
			final Iterator<Resource> iterator = storage
					.listResourcesUnderContainer(
							ModelUtils.getAIPcontainerPath()).iterator();

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
			throw new ModelServiceException(
					"Error while obtaining AIP list from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return it;
	}

	public AIP retrieveAIP(String aipId) throws ModelServiceException {
		AIP aip;
		try {
			Directory directory = storage.getDirectory(ModelUtils
					.getAIPpath(aipId));
			aip = convertResourceToAIP(directory);
		} catch (StorageActionException e) {
			if (e.getCode() == StorageActionException.NOT_FOUND) {
				throw new ModelServiceException("AIP not found: " + aipId,
						ModelServiceException.NOT_FOUND, e);
			} else if (e.getCode() == StorageActionException.FORBIDDEN) {
				throw new ModelServiceException(
						"You do not have permission to access AIP: " + aipId,
						ModelServiceException.FORBIDDEN, e);
			} else {
				throw new ModelServiceException(
						"Unexpected error while retrieving AIP",
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
	public AIP createAIP(String aipId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
		// TODO verify structure of source AIP and copy it to the storage
		// XXX possible optimization would be to allow move between storage
		// TODO support asReference

		AIP aip;
		try {
			Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (isAIPvalid(sourceDirectory)) {

				storage.copy(sourceStorage, sourcePath,
						ModelUtils.getAIPpath(aipId));
				Directory newDirectory = storage.getDirectory(ModelUtils
						.getAIPpath(aipId));

				aip = convertResourceToAIP(newDirectory);
				notifyAipCreated(aip);
			} else {
				throw new ModelServiceException(
						"Error while creating AIP, reason: AIP is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error creating AIP in storage",
					e.getCode(), e);
		}

		return aip;
	}

	// TODO support asReference
	public AIP updateAIP(String aipId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
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
				throw new ModelServiceException(
						"Error while creating AIP, reason: AIP is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error creating AIP in storage, reason: " + e.getMessage(),
					e.getCode());
		}

		return aip;
	}

	public void deleteAIP(String aipId) throws ModelServiceException {
		try {
			StoragePath aipPath = ModelUtils.getAIPpath(aipId);

			storage.deleteResource(aipPath);
			notifyAipDeleted(aipId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting AIP from storage, reason: "
							+ e.getMessage(), e.getCode());
		}
	}

	public Iterable<DescriptiveMetadata> listDescriptiveMetadataBinaries(
			String aipId) throws ModelServiceException {

		Iterable<DescriptiveMetadata> it;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(
							ModelUtils.getDescriptiveMetadataPath(aipId))
					.iterator();

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
								return convertResourceToDescriptiveMetadata(iterator
										.next());
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
					"Error while obtaining descriptive metadata binary list from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return it;
	}

	public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId,
			String descriptiveMetadataId) throws ModelServiceException {
		DescriptiveMetadata descriptiveMetadataBinary;

		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(
					aipId, descriptiveMetadataId);

			Binary binary = storage.getBinary(binaryPath);
			descriptiveMetadataBinary = convertResourceToDescriptiveMetadata(binary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining descriptive metadata binary from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return descriptiveMetadataBinary;
	}

	// FIXME descriptiveMetadataType shouldn't be a parameter but instead be
	// already present in the Binary metadata
	public DescriptiveMetadata createDescriptiveMetadata(String aipId,
			String descriptiveMetadataId, Binary binary,
			String descriptiveMetadataType) throws ModelServiceException {
		DescriptiveMetadata descriptiveMetadataBinary;
		try {
			// StoragePath binaryPath = binary.getStoragePath();
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(
					aipId, descriptiveMetadataId);
			boolean asReference = false;
			Map<String, Set<String>> binaryMetadata = binary.getMetadata();
			binaryMetadata.put(RodaConstants.STORAGE_META_TYPE,
					Sets.newHashSet(descriptiveMetadataType));

			storage.createBinary(binaryPath, binaryMetadata,
					binary.getContent(), asReference);
			descriptiveMetadataBinary = new DescriptiveMetadata(
					descriptiveMetadataId, aipId, descriptiveMetadataType,
					binaryPath);
			notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error creating descriptive metadata binary in storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return descriptiveMetadataBinary;
	}

	// FIXME descriptiveMetadataType shouldn't be a parameter but instead be
	// already present in the Binary metadata (and therefore to be changed
	// appropriated method should be called)
	public DescriptiveMetadata updateDescriptiveMetadata(String aipId,
			String descriptiveMetadataId, Binary binary,
			String descriptiveMetadataType) throws ModelServiceException {

		DescriptiveMetadata descriptiveMetadataBinary;
		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(
					aipId, descriptiveMetadataId);
			boolean asReference = false;
			boolean createIfNotExists = false;
			storage.updateBinaryContent(binaryPath, binary.getContent(),
					asReference, createIfNotExists);

			Map<String, Set<String>> binaryMetadata = binary.getMetadata();
			binaryMetadata.put(RodaConstants.STORAGE_META_TYPE,
					Sets.newHashSet(descriptiveMetadataType));
			storage.updateMetadata(binaryPath, binaryMetadata, true);

			descriptiveMetadataBinary = new DescriptiveMetadata(
					descriptiveMetadataId, aipId, descriptiveMetadataType,
					binaryPath);
			notifyDescriptiveMetadataUpdated(descriptiveMetadataBinary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error updating descriptive metadata binary in the storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return descriptiveMetadataBinary;
	}

	public void deleteDescriptiveMetadata(String aipId,
			String descriptiveMetadataId) throws ModelServiceException {
		try {
			StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(
					aipId, descriptiveMetadataId);

			storage.deleteResource(binaryPath);
			notifyDescriptiveMetadataDeleted(aipId, descriptiveMetadataId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting descriptive metadata binary from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

	}

	public Iterable<Representation> listRepresentations(String aipId)
			throws ModelServiceException {
		Iterable<Representation> it = null;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(
							ModelUtils.getRepresentationsPath(aipId))
					.iterator();

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
								return convertResourceToRepresentation(iterator
										.next());
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
					"Error while obtaining Representation list from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return it;
	}

	public Representation retrieveRepresentation(String aipId,
			String representationId) throws ModelServiceException {
		Representation representation;

		try {
			Directory directory = storage.getDirectory(ModelUtils
					.getRepresentationPath(aipId, representationId));
			representation = convertResourceToRepresentation(directory);

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining Representation from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return representation;
	}

	// TODO support asReference
	public Representation createRepresentation(String aipId,
			String representationId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
		Representation representation;

		try {
			StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId,
					representationId);

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
			throw new ModelServiceException(
					"Error while creating representation in storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return representation;
	}

	public Representation updateRepresentation(String aipId,
			String representationId, StorageService sourceStorage,
			StoragePath sourcePath) throws ModelServiceException {
		Representation representation;
		Directory sourceDirectory;

		// verify structure of source representation
		try {
			sourceDirectory = sourceStorage.getDirectory(sourcePath);
			if (!isRepresentationValid(sourceDirectory)) {
				throw new ModelServiceException(
						"Error while updating AIP, reason: representation is not valid",
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while updating representation in storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		// update each representation file (from source representation)
		final List<String> fileIDsToUpdate = new ArrayList<String>();
		try {
			Iterable<Resource> files = sourceStorage
					.listResourcesUnderDirectory(sourcePath);
			for (Resource file : files) {
				if (file instanceof DefaultBinary) {
					boolean createIfNotExists = true;
					boolean notify = false;
					File fileUpdated = updateFile(aipId, representationId, file
							.getStoragePath().getName(), (Binary) file,
							createIfNotExists, notify);

					fileIDsToUpdate.add(fileUpdated.getStoragePath().getName());
				} else {
					// FIXME log error and continue???
				}
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while updating representation files",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		// delete files that were removed on representation update
		try {
			Iterable<Resource> filesToRemove = storage
					.listResourcesUnderDirectory(ModelUtils
							.getRepresentationPath(aipId, representationId));
			for (Resource fileToRemove : filesToRemove) {
				StoragePath fileToRemovePath = fileToRemove.getStoragePath();
				if (!fileIDsToUpdate.contains(fileToRemovePath.getName())) {
					storage.deleteResource(fileToRemovePath);
				}
			}

		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while delete removed representation files",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		// get representation metadata (from source representation)
		Map<String, Set<String>> representationMetadata = sourceDirectory
				.getMetadata();

		// obtain information (from metadata) to build representation object
		boolean active = ModelUtils.getBoolean(representationMetadata,
				RodaConstants.STORAGE_META_ACTIVE);
		Date dateCreated = ModelUtils.getDate(representationMetadata,
				RodaConstants.STORAGE_META_DATE_CREATED);
		Date dateModified = new Date();
		representationMetadata.put(RodaConstants.STORAGE_META_DATE_MODIFIED,
				Sets.newHashSet(RodaUtils.dateToString(dateModified)));
		String type = ModelUtils.getString(representationMetadata,
				RodaConstants.STORAGE_META_TYPE);
		Set<RepresentationState> statuses = ModelUtils
				.getStatuses(representationMetadata);

		// update representation metadata (essentially date.modified)
		try {
			storage.updateMetadata(
					ModelUtils.getRepresentationPath(aipId, representationId),
					representationMetadata, true);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while updating representation metadata",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		representation = new Representation(representationId, aipId, active,
				dateCreated, dateModified, statuses, type, fileIDsToUpdate);
		notifyRepresentationUpdated(representation);
		return representation;
	}

	public void deleteRepresentation(String aipId, String representationId)
			throws ModelServiceException {
		try {
			StoragePath representationPath = ModelUtils.getRepresentationPath(
					aipId, representationId);

			storage.deleteResource(representationPath);
			notifyRepresentationDeleted(aipId, representationId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting representation from storage, reason: "
							+ e.getMessage(), e.getCode());
		}
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public Iterable<File> listFiles(String aipId, String representationId)
			throws ModelServiceException {
		Iterable<File> it = null;

		try {
			final Iterator<Resource> iterator = storage
					.listResourcesUnderDirectory(
							ModelUtils.getRepresentationsPath(aipId))
					.iterator();

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
								return convertResourceToRepresentationFile(iterator
										.next());
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
					"Error while obtaining representation files from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return it;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File retrieveFile(String aipId, String representationId,
			String fileId) throws ModelServiceException {
		File file;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId,
					representationId, fileId);

			Binary binary = storage.getBinary(filePath);
			file = convertResourceToRepresentationFile(binary);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while obtaining representation file from storage, reason: "
							+ e.getMessage(), e.getCode());
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File createFile(String aipId, String representationId,
			String fileId, Binary binary) throws ModelServiceException {
		File file;
		// FIXME how to set this?
		boolean asReference = false;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId,
					representationId, fileId);

			final Binary createdBinary = storage.createBinary(filePath,
					binary.getMetadata(), binary.getContent(), asReference);
			file = convertResourceToRepresentationFile(createdBinary);
			notifyFileCreated(file);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error creating representation file in storage",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public File updateFile(String aipId, String representationId,
			String fileId, Binary binary, boolean createIfNotExists,
			boolean notify) throws ModelServiceException {
		File file = null;
		// FIXME how to set this?
		boolean asReference = false;

		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId,
					representationId, fileId);

			storage.updateBinaryContent(filePath, binary.getContent(),
					asReference, createIfNotExists);
			storage.updateMetadata(filePath, binary.getMetadata(), true);
			Binary binaryUpdated = storage.getBinary(filePath);
			file = convertResourceToRepresentationFile(binaryUpdated);
			if (notify) {
				notifyFileUpdated(file);
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error while updating representation file",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}

		return file;
	}

	// FIXME under a certain representation may exist files but also folders.
	// how to handle that in this method?
	public void deleteFile(String aipId, String representationId, String fileId)
			throws ModelServiceException {
		try {
			StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId,
					representationId, fileId);

			storage.deleteResource(filePath);
			notifyFileDeleted(aipId, representationId, fileId);
		} catch (StorageActionException e) {
			throw new ModelServiceException(
					"Error deleting representation file from storage, reason: "
							+ e.getMessage(), e.getCode());
		}
	}

	// TODO add methods for preservation metadata

	private boolean isAIPvalid(Directory directory) {
		// FIXME implement this
		return true;
	}

	private boolean isRepresentationValid(Directory directory) {
		// FIXME implement this
		return true;
	}

	private AIP convertResourceToAIP(Resource resource)
			throws ModelServiceException {
		AIP aip;
		if (resource instanceof DefaultDirectory) {
			StoragePath storagePath = resource.getStoragePath();
			Map<String, Set<String>> metadata = resource.getMetadata();
			String parentId = ModelUtils.getString(metadata,
					RodaConstants.STORAGE_META_PARENT_ID);
			Boolean active = ModelUtils.getBoolean(metadata,
					RodaConstants.STORAGE_META_ACTIVE);
			Date dateCreated = ModelUtils.getDate(metadata,
					RodaConstants.STORAGE_META_DATE_CREATED);
			Date dateModified = ModelUtils.getDate(metadata,
					RodaConstants.STORAGE_META_DATE_MODIFIED);

			if (active == null) {
				// when not stated, consider active=false
				active = false;
			}

			try {
				List<String> descriptiveMetadataBinaryIds = ModelUtils.getIds(
						storage, ModelUtils
								.getDescriptiveMetadataPath(storagePath
										.getName()));
				List<String> representationIds = ModelUtils
						.getIds(storage, ModelUtils
								.getRepresentationsPath(storagePath.getName()));

				aip = new AIP(storagePath.getName(), parentId, active,
						dateCreated, dateModified,
						descriptiveMetadataBinaryIds, representationIds);
			} catch (StorageActionException e) {
				throw new ModelServiceException(
						"Error while obtaining information to instantiate an AIP, reason: "
								+ e.getMessage(), e.getCode());
			}
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Directory into an AIP",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
		return aip;
	}

	private DescriptiveMetadata convertResourceToDescriptiveMetadata(
			Resource resource) throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			// retrieve needed information to instantiate DescriptiveMetadata
			String type = ModelUtils.getString(resource.getMetadata(),
					RodaConstants.STORAGE_META_TYPE);

			return new DescriptiveMetadata(resource.getStoragePath().getName(),
					ModelUtils.getAIPidFromStoragePath(resource
							.getStoragePath()), type, resource.getStoragePath());
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Binary into a descriptive metadata binary",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private Representation convertResourceToRepresentation(Resource resource)
			throws ModelServiceException {
		if (resource instanceof DefaultDirectory) {
			StoragePath directoryPath = resource.getStoragePath();
			Map<String, Set<String>> directoryMetadata = resource.getMetadata();

			// retrieve needed information to instantiate Representation
			Boolean active = ModelUtils.getBoolean(directoryMetadata,
					RodaConstants.STORAGE_META_ACTIVE);
			Date dateCreated = ModelUtils.getDate(directoryMetadata,
					RodaConstants.STORAGE_META_DATE_CREATED);
			Date dateModified = ModelUtils.getDate(directoryMetadata,
					RodaConstants.STORAGE_META_DATE_MODIFIED);
			Set<RepresentationState> statuses = ModelUtils
					.getStatuses(directoryMetadata);
			String type = ModelUtils.getString(directoryMetadata,
					RodaConstants.STORAGE_META_TYPE);
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

			return new Representation(directoryPath.getName(),
					ModelUtils.getAIPidFromStoragePath(directoryPath), active,
					dateCreated, dateModified, statuses, type, fileIds);

		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Directory into a representation",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private File convertResourceToRepresentationFile(Resource resource)
			throws ModelServiceException {
		if (resource instanceof DefaultBinary) {
			StoragePath binaryPath = resource.getStoragePath();
			Map<String, Set<String>> metadata = resource.getMetadata();

			// retrieve needed information to instantiate File
			Boolean entryPoint = ModelUtils.getBoolean(metadata,
					RodaConstants.STORAGE_META_ENTRYPOINT);
			FileFormat fileFormat = ModelUtils.getFileFormat(metadata);

			if (entryPoint == null) {
				// if entry point not defined, considering false
				entryPoint = false;
			}

			return new File(binaryPath.getName(),
					ModelUtils.getAIPidFromStoragePath(binaryPath),
					ModelUtils.getRepresentationIdFromStoragePath(binaryPath),
					entryPoint, fileFormat, binaryPath);
		} else {
			throw new ModelServiceException(
					"Error while trying to convert something that it isn't a Binary into a representation file",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}
}
