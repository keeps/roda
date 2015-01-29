/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.util.NaturalOrderComparator;

/**
 * @author Luis Faria
 * 
 */
public class Tools {

	private static final Logger logger = Logger.getLogger(Tools.class);

	/**
	 * Create a Image Icon
	 * 
	 * @param path
	 * @return the ImageIcon, or null if the path was invalid.
	 */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = FondsPanel.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			logger.error("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Get the file contents as a string
	 * 
	 * @param file
	 *            the file to open
	 * @return a string with the file contents
	 * @throws IOException
	 */
	public static String readFileAsString(File file) throws IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Get the inputstream contents as a string
	 * 
	 * @param inputstream
	 *            the inputstream to read
	 * @param encoding
	 *            the encoding to use, e.g. UTF-8
	 * @return a string with the inputstream contents
	 * @throws IOException
	 */
	public static String readInputStreamAsString(InputStream inputstream,
			String encoding) throws IOException {

		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputstream, encoding));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * This will allow selection and copy to work but still retain the label
	 * look
	 * 
	 * @param textArea
	 */
	public static void makeTextAreaLookLikeLable(JTextArea textArea) {

		// Turn on word wrap
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		// Perform the other changes to complete the look
		makeTextComponentLookLikeLable(textArea);
	}

	/**
	 * This will allow selection and copy to work but still retain the label
	 * look
	 */
	private static void makeTextComponentLookLikeLable(
			JTextComponent textcomponent) {

		// Make the text component non editable
		textcomponent.setEditable(false);

		// Make the text area look like a label
		textcomponent.setBackground((Color) UIManager.get("Label.background"));
		textcomponent.setForeground((Color) UIManager.get("Label.foreground"));
		textcomponent.setBorder(null);
	}

	private static String ZERO_CHAR_STRING = "" + (char) 0;

	/**
	 * Create a file array from a buffered reader
	 * 
	 * @param bReader
	 * @return an array of files
	 */
	public static File[] createFileArray(BufferedReader bReader) {
		try {
			List<File> list = new ArrayList<File>();
			String line = null;
			while ((line = bReader.readLine()) != null) {
				try {
					// KDE seems to append a 0 char to the end of the reader
					if (ZERO_CHAR_STRING.equals(line))
						continue;

					File file = new File(new URI(line));
					list.add(file);
				} catch (URISyntaxException ex) {
					logger.error("Error creating file array", ex);
				}
			}

			return (File[]) list.toArray(new File[list.size()]);
		} catch (IOException ex) {
			logger.error("Error creating file array", ex);
		}
		return new File[0];
	}

	/**
	 * Compare SIPs by the id, respecting natural order
	 * 
	 */
	private static class SipNaturalOrderComparator implements Comparator<SIP> {

		private final NaturalOrderComparator naturalComparator;

		public SipNaturalOrderComparator() {
			naturalComparator = new NaturalOrderComparator();
		}

		public int compare(SIP sip1, SIP sip2) {
			return naturalComparator.compare(sip1.getDescriptionObject()
					.getId(), sip2.getDescriptionObject().getId());
		}

	}

	private static final SipNaturalOrderComparator sipNaturalCompator = new SipNaturalOrderComparator();

	/**
	 * Sort a SIP list by the description id, respecting natural order
	 * 
	 * @param sipList
	 */
	public static void sortSipList(List<SIP> sipList) {
		Collections.sort(sipList, sipNaturalCompator);
	}

	/**
	 * Compare RepresentationFiles by the id, respecting natural order
	 * 
	 */
	private static class RepresentationFileNaturalOrderComparator implements
			Comparator<RepresentationFile> {

		private final NaturalOrderComparator naturalComparator;

		public RepresentationFileNaturalOrderComparator() {
			naturalComparator = new NaturalOrderComparator();
		}

		public int compare(RepresentationFile part1, RepresentationFile part2) {
			return naturalComparator.compare(part1.getId(), part2.getId());
		}

	}

	private static RepresentationFileNaturalOrderComparator representationFileNaturalOrderComparator = new RepresentationFileNaturalOrderComparator();

	/**
	 * Sort a representation files list by the id, respecting natural order
	 * 
	 * @param partFiles
	 */
	public static void sortPartFiles(List<RepresentationFile> partFiles) {
		Collections.sort(partFiles, representationFileNaturalOrderComparator);
	}
}
