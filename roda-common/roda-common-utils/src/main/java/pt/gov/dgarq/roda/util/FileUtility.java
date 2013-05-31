package pt.gov.dgarq.roda.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 * 
 */
public class FileUtility {

	static final private Logger logger = Logger.getLogger(FileUtility.class);

	/**
	 * Copy <code>sourceFile</code> to <code>destinationFile</code>.
	 * 
	 * @param sourceFile
	 *            the source file.
	 * @param destinationFile
	 *            the destination file.
	 * @throws IOException
	 *             if something goes wrong with the copy.
	 */
	public static void copyFile(File sourceFile, File destinationFile)
			throws IOException {

		FileHelper.copy(sourceFile, destinationFile);

	}

	/**
	 * Copy <code>sourceFiles</code> to <code>destinationDir</code>.
	 * 
	 * @param sourceFiles
	 *            the source files.
	 * @param destinationDir
	 *            the destination directory.
	 * @return the number of copied files.
	 */
	public static int copyFiles(List<File> sourceFiles, File destinationDir) {
		int filesCopied = 0;

		for (Iterator<File> iter = sourceFiles.iterator(); iter.hasNext();) {
			File file = iter.next();
			try {
				copyFile(file, new File(destinationDir, file.getName()));
				filesCopied++;
			} catch (IOException e) {
				logger.error("Error copying file " + file + " to "
						+ new File(destinationDir, file.getName()), e);
			}
		}

		return filesCopied;
	}

	/**
	 * 
	 * @param file
	 * @param digestAlgorithm
	 * 
	 * @return a {@link String} with calculated checksum in hexadecimal.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateChecksumInHex(File file,
			String digestAlgorithm) throws NoSuchAlgorithmException,
			IOException {
		FileInputStream fis = new FileInputStream(file);
		String checksumInHex = calculateChecksumInHex(fis, digestAlgorithm);
		fis.close();
		return checksumInHex;
	}

	/**
	 * 
	 * @param is
	 * @param digestAlgorithm
	 * @return a {@link java.lang.String} with calculated checksum in
	 *         hexadecimal.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateChecksumInHex(InputStream is,
			String digestAlgorithm) throws NoSuchAlgorithmException,
			IOException {
		return byteArrayToHexString(calculateDigest(is, digestAlgorithm));
	}

	/**
	 * 
	 * @param is
	 * @param digestAlgorithm
	 * @return a byte array with the calculated digest.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static byte[] calculateDigest(InputStream is, String digestAlgorithm)
			throws NoSuchAlgorithmException, IOException {

		MessageDigest digestor = MessageDigest.getInstance(digestAlgorithm);

		// reads file at 1Kbyte chunks (1024 bytes)
		// reads file in 1Mbyte chunks (1048576 bytes - 2^20 bytes)
		int bufSize = 1048576; // 1 MByte
		byte[] buffer = new byte[bufSize];
		int n = is.read(buffer, 0, bufSize);
		int count = 0;
		while (n != -1) {
			count += n;
			digestor.update(buffer, 0, n);
			n = is.read(buffer, 0, bufSize);
		}
		// is.close();

		byte[] digest = digestor.digest();

		return digest;
	}

	/**
	 * Convert a byte[] array to readable string format. This makes the "hex"
	 * readable!
	 * 
	 * @return result String buffer in String format
	 * @param in
	 *            byte[] buffer to convert to string format
	 */
	private static String byteArrayToHexString(byte in[]) {

		byte ch = 0x00;

		int i = 0;

		if (in == null || in.length <= 0) {
			return null;
		}

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };

		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {

			ch = (byte) (in[i] & 0xF0); // Strip off high nibble

			ch = (byte) (ch >>> 4);
			// shift the bits down

			ch = (byte) (ch & 0x0F);
			// must do this is high order bit is on!

			out.append(pseudo[(int) ch]); // convert the nibble to a String
			// Character

			ch = (byte) (in[i] & 0x0F); // Strip off low nibble

			out.append(pseudo[(int) ch]); // convert the nibble to a String
			// Character

			i++;
		}

		return new String(out);
	}

	/**
	 * Returns a list with all the files inside a given directory.
	 * 
	 * @param dir
	 *            the directory to list the contents.
	 * @return a list with all the files and directories.
	 */
	public static List<File> listFilesRecursively(File dir) {

		ArrayList<File> files = new ArrayList<File>();

		File[] list = dir.listFiles();

		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				files.addAll(listFilesRecursively(list[i]));
			} else {
				files.add(list[i]);
			}
		}

		return files;
	}

	/**
	 * Reads bytes from a file
	 * 
	 * @param file
	 * @return an array of bytes read from file
	 * @throws IOException
	 */
	public static byte[] readBytesFromFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] bytes = StreamUtility.byteArrayFromInputStream(inputStream);
		inputStream.close();
		return bytes;
	}

}
