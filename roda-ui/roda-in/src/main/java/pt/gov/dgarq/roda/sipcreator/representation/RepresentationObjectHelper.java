/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationObjectHelper {

	/**
	 * Get next unique part file ID
	 * 
	 * @param repObj
	 *            the representation object where to check for IDs
	 * @return an unique file part ID, starting at "F1", and incrementing the
	 *         number until its unique
	 */
	public static String getNextFilePartId(RepresentationObject repObj) {
		RepresentationFile[] partFiles = repObj.getPartFiles();
		if (partFiles == null) {
			partFiles = new RepresentationFile[] {};
		}
		return getNextFilePartId(Arrays.asList(partFiles));
	}

	/**
	 * Get next unique part file ID
	 * 
	 * @param partFiles
	 *            the part files list where to check the IDs
	 * @return an unique file part ID, starting at "F1", and incrementing the
	 *         number until its unique
	 */
	public static String getNextFilePartId(List<RepresentationFile> partFiles) {
		int i = 1;
		String id;
		boolean unique;
		do {
			id = "F" + i;
			unique = true;
			for (RepresentationFile partFile : partFiles) {
				if (partFile.getId().equals(id)) {
					unique = false;
					break;
				}
			}
			i++;

		} while (!unique);
		return id;
	}

	/**
	 * Add a new part file to a representation object
	 * 
	 * @param partFile
	 *            the part file to add
	 * @param repObj
	 *            the representation object where it will be added
	 */
	public static void addPartFile(RepresentationFile partFile,
			RepresentationObject repObj) {
		RepresentationFile[] partFiles = repObj.getPartFiles();
		if (partFiles == null) {
			partFiles = new RepresentationFile[] {};
		}
		List<RepresentationFile> newPartFiles = new ArrayList<RepresentationFile>(
				partFiles.length + 1);
		newPartFiles.addAll(Arrays.asList(partFiles));
		newPartFiles.add(partFile);
		repObj.setPartFiles(newPartFiles
				.toArray(new RepresentationFile[partFiles.length + 1]));

	}

	/**
	 * Lookup a part file in a representation object
	 * 
	 * @param id
	 *            the part file ID
	 * @param repObj
	 *            the representation object to look into
	 * @return The representation file if found, or null otherwise
	 */
	public static RepresentationFile lookupPartFile(String id,
			RepresentationObject repObj) {
		RepresentationFile[] partFiles = repObj.getPartFiles();
		return lookupPartFile(id, Arrays.asList(partFiles));
	}

	/**
	 * Lookup a part file in a part file list
	 * 
	 * @param id
	 *            the part file ID
	 * @param partFiles
	 *            the part file list
	 * @return The representation file if found, or null otherwise
	 */
	public static RepresentationFile lookupPartFile(String id,
			List<RepresentationFile> partFiles) {
		RepresentationFile ret = null;
		for (RepresentationFile partFile : partFiles) {
			if (partFile.getId().equals(id)) {
				ret = partFile;
				break;
			}
		}
		return ret;
	}

	/**
	 * Lookup a part file in a part file list
	 * 
	 * @param accessUrl
	 *            the part file access URL
	 * @param partFiles
	 *            the part file list
	 * @return The representation file if found, or null otherwise
	 */
	public static RepresentationFile lookupPartFileAccessUrl(String accessUrl,
			List<RepresentationFile> partFiles) {
		RepresentationFile ret = null;
		for (RepresentationFile partFile : partFiles) {
			if (partFile.getAccessURL().equals(accessUrl)) {
				ret = partFile;
				break;
			}
		}
		return ret;
	}

	/**
	 * Remove a part file from a representation object
	 * 
	 * @param id
	 *            the part file id
	 * @param repObj
	 *            the representation object
	 */
	public static void removePartFile(String id, RepresentationObject repObj) {
		List<RepresentationFile> repFiles = new ArrayList<RepresentationFile>();
		repFiles.addAll(Arrays.asList(repObj.getPartFiles()));
		repFiles.remove(lookupPartFile(id, repObj));
		repObj.setPartFiles(repFiles.toArray(new RepresentationFile[repFiles
				.size()]));
	}
}
