package org.roda.model.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.roda.common.RodaConstants;
import org.roda.common.RodaUtils;
import org.roda.model.FileFormat;
import org.roda.model.ModelServiceException;
import org.roda.model.RepresentationState;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;

/**
 * Model related utility class
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class ModelUtils {

	/**
	 * Private empty constructor
	 */
	private ModelUtils() {

	}

	/**
	 * Builds, from metadata, a {@code FileFormat} object
	 * 
	 * @param metadata
	 *            metadata
	 * @throws ModelServiceException
	 */
	public static FileFormat getFileFormat(Map<String, Set<String>> metadata)
			throws ModelServiceException {
		String mimetype = getString(metadata,
				RodaConstants.STORAGE_META_FORMAT_MIME);
		String version = getString(metadata,
				RodaConstants.STORAGE_META_FORMAT_VERSION);
		// FIXME how to load format registries if any
		Map<String, String> formatRegistries = new HashMap<String, String>();

		return new FileFormat(mimetype, version, formatRegistries);
	}

	/**
	 * Builds, from metadata, a {@code Set<RepresentationState>} object
	 * 
	 * @param metadata
	 *            metadata
	 */
	public static Set<RepresentationState> getStatuses(
			Map<String, Set<String>> metadata) {
		Set<RepresentationState> statuses = new TreeSet<RepresentationState>();
		Set<String> statusesInString = metadata
				.get(RodaConstants.STORAGE_META_REPRESENTATION_STATUSES);
		for (String statusString : statusesInString) {
			statuses.add(RepresentationState.valueOf(statusString.toUpperCase()));
		}
		return statuses;
	}

	public static <T> T getAs(Map<String, Set<String>> metadata, String key,
			Class<T> type) throws ModelServiceException {
		T ret;
		Set<String> set = metadata.get(key);
		if (set == null || set.size() == 0) {
			ret = null;
		} else if (set.size() == 1) {
			String value = set.iterator().next();
			if (type.equals(Date.class)) {
				try {
					ret = type.cast(RodaUtils.parseDate(set.iterator().next()));
				} catch (ParseException e) {
					throw new ModelServiceException("Could not parse date: "
							+ value,
							ModelServiceException.INTERNAL_SERVER_ERROR, e);
				}
			} else if (type.equals(Boolean.class)) {
				ret = type.cast(Boolean.valueOf(value));
			} else {
				throw new ModelServiceException(
						"Could not parse date because metadata field has not a single value class is not supported"
								+ type,
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
		} else {
			throw new ModelServiceException(
					"Could not parse date because metadata field has not a single value, set="
							+ set, ModelServiceException.INTERNAL_SERVER_ERROR);
		}

		return ret;
	}

	/**
	 * Reads, from metadata and for a metadata key, an ISO8601 date if it exists
	 * 
	 * @param metadata
	 *            metadata
	 * @param key
	 *            metadata key
	 * @throws ModelServiceException
	 */
	public static Date getDate(Map<String, Set<String>> metadata, String key)
			throws ModelServiceException {
		return getAs(metadata, key, Date.class);
	}

	/**
	 * Reads, from metadata and for a metadata key, a boolean if it exists
	 * 
	 * @param metadata
	 *            metadata
	 * @param key
	 *            metadata key
	 * @throws ModelServiceException
	 */
	public static Boolean getBoolean(Map<String, Set<String>> metadata,
			String key) throws ModelServiceException {
		return getAs(metadata, key, Boolean.class);
	}

	/**
	 * Reads, from metadata and for a metadata key, a string if it exists
	 * 
	 * @param metadata
	 *            metadata
	 * @param key
	 *            metadata key
	 * @throws ModelServiceException
	 */
	public static String getString(Map<String, Set<String>> metadata, String key)
			throws ModelServiceException {
		String ret;
		Set<String> set = metadata.get(key);
		if (set == null || set.size() == 0) {
			ret = null;
		} else if (set.size() == 1) {
			ret = set.iterator().next();
		} else {
			throw new ModelServiceException(
					"Could not parse date because metadata field has multiple values, set="
							+ set, ModelServiceException.INTERNAL_SERVER_ERROR);
		}

		return ret;
	}

	/**
	 * Returns a list of ids from the children of a certain resource
	 * 
	 * @param storage
	 *            the storage service containing the parent resource
	 * @param path
	 *            the storage path for the parent resource
	 */
	public static List<String> getIds(StorageService storage, StoragePath path)
			throws StorageActionException {
		List<String> ids = new ArrayList<String>();
		Iterator<Resource> it = storage.listResourcesUnderDirectory(path)
				.iterator();
		while (it.hasNext()) {
			Resource next = it.next();
			StoragePath storagePath = next.getStoragePath();
			ids.add(storagePath.getName());
		}

		return ids;
	}

	public static StoragePath getAIPcontainerPath()
			throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP);
	}

	public static StoragePath getAIPpath(String aipId)
			throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId);
	}

	public static StoragePath getDescriptiveMetadataPath(String aipId)
			throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId, RodaConstants.STORAGE_DIRECTORY_METADATA,
				RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);
	}

	public static StoragePath getDescriptiveMetadataPath(String aipId,
			String descriptiveMetadataBinaryId) throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId, RodaConstants.STORAGE_DIRECTORY_METADATA,
				RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE,
				descriptiveMetadataBinaryId);
	}

	public static StoragePath getRepresentationsPath(String aipId)
			throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId, RodaConstants.STORAGE_DIRECTORY_DATA);
	}

	public static StoragePath getRepresentationPath(String aipId,
			String representationId) throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId, RodaConstants.STORAGE_DIRECTORY_DATA, representationId);
	}

	public static StoragePath getRepresentationFilePath(String aipId,
			String representationId, String fileId)
			throws StorageActionException {
		return DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP,
				aipId, RodaConstants.STORAGE_DIRECTORY_DATA, representationId,
				fileId);
	}

	public static String getAIPidFromStoragePath(StoragePath path) {
		return path.getDirectoryPath().get(0);
	}

	public static String getRepresentationIdFromStoragePath(StoragePath path)
			throws ModelServiceException {
		if (path.getDirectoryPath().size() >= 3) {
			return path.getDirectoryPath().get(2);
		} else {
			throw new ModelServiceException(
					"Error while trying to obtain representation id from storage path (length is not 3 or above)",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}
}
