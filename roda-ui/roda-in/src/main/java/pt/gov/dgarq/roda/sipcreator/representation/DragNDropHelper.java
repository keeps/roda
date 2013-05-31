/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Luis Faria
 * 
 */
public class DragNDropHelper {

	private static final Set<String> FILES_TO_FILTER = new HashSet<String>(
			Arrays.asList(new String[] { ".DS_Store", "Thumbs.db" }));

	/**
	 * Filter files that could be dropped by mistake, like hidden files
	 * 
	 * @param files
	 * @return the list of allowed dropped files
	 */
	public static List<File> filterFiles(List<File> files) {
		List<File> ret = new ArrayList<File>();
		for (File file : files) {
			System.out.println("DragNDropHelper.filterFiles() "
					+ file.getName());
			if (isFileAllowed(file)) {
				ret.add(file);
			}
		}
		return ret;
	}

	/**
	 * Check if file is allowed by dropped, i.e. could not be dropped by
	 * mistake, like hidden files
	 * 
	 * @param file
	 * @return true if allowed
	 */
	public static boolean isFileAllowed(File file) {
		return !file.isHidden() || !FILES_TO_FILTER.contains(file.getName());
	}
}
