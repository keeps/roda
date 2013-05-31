package pt.gov.dgarq.roda.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 * 
 */
public class ZipUtility {

	final private static Logger logger = Logger.getLogger(ZipUtility.class);

	final private static int BUFFER_SIZE = 1024;

	/**
	 * Extract files in zipFilename to outputDir.
	 * 
	 * @param zipFilename
	 *            the zip file to extract files from.
	 * @param outputDir
	 *            the output directory to extract files to.
	 * @throws IOException
	 *             if a input/output operation fails, like opening a file or
	 *             reading/writing from/to a stream.
	 */
	public static void extractZIPFiles(File zipFilename, File outputDir)
			throws IOException {
		extractFilesFromZIP(zipFilename, outputDir);
	}

	/**
	 * Extract files in zipFilename to outputDir.
	 * 
	 * @param zipFilename
	 *            the zip file to extract files from.
	 * @param outputDir
	 *            the output directory to extract files to.
	 * 
	 * @return a {@link List} of with all the extracted {@link File}s.
	 * 
	 * @throws IOException
	 *             if a input/output operation fails, like opening a file or
	 *             reading/writing from/to a stream.
	 */
	public static List<File> extractFilesFromZIP(File zipFilename,
			File outputDir) throws IOException {

		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(
				zipFilename));
		// JarInputStream jarInputStream = new JarInputStream(new
		// FileInputStream(
		// zipFilename));

		ZipEntry zipEntry = zipInputStream.getNextEntry();
		// JarEntry jarEntry = jarInputStream.getNextJarEntry();

		if (zipEntry == null) {
			// if (jarEntry == null) {
			// No entries in ZIP

			zipInputStream.close();
			// jarInputStream.close();

			throw new IOException("No files inside ZIP");

		} else {

			List<File> extractedFiles = new ArrayList<File>();

			while (zipEntry != null) {
				// while (jarEntry != null) {

				// for each entry to be extracted
				String entryName = zipEntry.getName();
				// String entryName = jarEntry.getName();

				logger.debug("Extracting " + entryName);

				File newFile = new File(outputDir, entryName);

				extractedFiles.add(new File(entryName));

				if (zipEntry.isDirectory()) {
					// if (jarEntry.isDirectory()) {

					newFile.mkdirs();

				} else {

					if (newFile.getParentFile() != null
							&& (!newFile.getParentFile().exists())) {
						newFile.getParentFile().mkdirs();
					}

					FileOutputStream newFileOutputStream = new FileOutputStream(
							newFile);

					// IOUtils.copy(zipInputStream, newFileOutputStream);
					// copyLarge returns a long instead of int
					IOUtils.copyLarge(zipInputStream, newFileOutputStream);
					// IOUtils.copyLarge(jarInputStream, newFileOutputStream);

					// int n;
					// while ((n = zipInputStream.read(buf, 0, BUFFER_SIZE)) >
					// -1) {
					// newFileOutputStream.write(buf, 0, n);
					// }

					newFileOutputStream.close();
					zipInputStream.closeEntry();
					// jarInputStream.closeEntry();

				}

				zipEntry = zipInputStream.getNextEntry();
				// jarEntry = jarInputStream.getNextJarEntry();

			} // end while

			zipInputStream.close();
			// jarInputStream.close();

			return extractedFiles;
		}

	}

	/**
	 * Creates ZIP file with the files inside directory <code>contentsDir</code>
	 * .
	 * 
	 * @param newZipFile
	 *            the ZIP file to create
	 * @param contentsDir
	 *            the directory containing the files to compress.
	 * @return the created ZIP file.
	 * @throws IOException
	 *             if something goes wrong with creation of the ZIP file or the
	 *             reading of the files to compress.
	 */
	public static File createZIPFile(File newZipFile, File contentsDir)
			throws IOException {

		List<File> contentAbsoluteFiles = FileUtility
				.listFilesRecursively(contentsDir);

		JarOutputStream jarOutputStream = new JarOutputStream(
				new BufferedOutputStream(new FileOutputStream(newZipFile)));
		// ZipOutputStream zipOutputStream = new ZipOutputStream(
		// new BufferedOutputStream(new FileOutputStream(newZipFile)));

		// Create a buffer for reading the files
		byte[] buffer = new byte[BUFFER_SIZE];

		Iterator<File> iterator = contentAbsoluteFiles.iterator();
		while (iterator.hasNext()) {
			File absoluteFile = iterator.next();
			String relativeFile = getFilePathRelativeTo(absoluteFile,
					contentsDir);

			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(absoluteFile));

			// Add ZIP entry to output stream.
			// zipOutputStream.putNextEntry(new
			// ZipEntry(relativeFile.toString()));
			jarOutputStream.putNextEntry(new JarEntry(relativeFile));

			logger.trace("Adding " + relativeFile);

			int length;
			while ((length = in.read(buffer)) > 0) {
				// zipOutputStream.write(buffer, 0, length);
				jarOutputStream.write(buffer, 0, length);
			}

			// Complete the entry
			// zipOutputStream.closeEntry();
			jarOutputStream.closeEntry();
			in.close();
		}

		// Complete the ZIP file
		// zipOutputStream.close();
		jarOutputStream.close();

		return newZipFile;
	}

	/**
	 * @param file
	 *            the {@link File} to make relative
	 * @param relativeTo
	 *            the {@link File} (or directory) that file should be made
	 *            relative to.
	 * @return a {@link String} with the relative file path.
	 */
	public static String getFilePathRelativeTo(File file, File relativeTo) {
		return relativeTo.getAbsoluteFile().toURI().relativize(
				file.getAbsoluteFile().toURI()).toString();
	}

}
