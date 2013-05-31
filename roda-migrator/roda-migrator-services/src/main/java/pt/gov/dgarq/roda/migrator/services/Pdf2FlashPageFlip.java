/**
 * 
 */
package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.lf5.util.StreamUtils;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.util.CommandException;

/**
 * @author Luis Faria
 * 
 */
public class Pdf2FlashPageFlip extends GhostScriptConverter {

	private static final Comparator<File> IMAGE_FILE_COMPARATOR = new Comparator<File>() {

		public int compare(File file1, File file2) {
			int index1 = Integer.parseInt(file1.getName().substring(1,
					file1.getName().indexOf('.')));
			int index2 = Integer.parseInt(file2.getName().substring(1,
					file2.getName().indexOf('.')));

			return index1 - index2;
		}

	};

	/**
	 * @throws RODAServiceException
	 */
	public Pdf2FlashPageFlip() throws RODAServiceException {
		super();
		formatExtension = ".jpg";
		device = "jpeg";
		options = new String[] { "-q", "-dJPEGQ=95", "-dGraphicsAlphaBits=4",
				"-dTextAlphaBits=4", "-dDOINTERPOLATE", "-dSAFER", "-dBATCH",
				"-dNOPAUSE", "-r120x120" };
	}

	@Override
	protected void createRootFile(File root, File baseDir)
			throws ConverterException {
		try {
			String pagesTemplate = new String(StreamUtils
					.getBytes(DW2FlashPageFlip.class.getClassLoader()
							.getResourceAsStream("/Pages.xml")));

			String pagesXML = "";
			int width = 0;
			int height = 0;
			boolean first = true;
			List<File> images = Arrays.asList(baseDir.listFiles());
			Collections.sort(images, IMAGE_FILE_COMPARATOR);

			for (File image : images) {
				if (first) {
					width = ImageMagickConverter.getImageWidth(image);
					height = ImageMagickConverter.getImageHeight(image);
					first = false;
				}

				if (image != null) {
					pagesXML += "\t<page src=\"" + image.getName() + "\" />\n";
				}
			}

			pagesTemplate = pagesTemplate.replaceAll("\\@WIDTH", "" + width);
			pagesTemplate = pagesTemplate.replaceAll("\\@HEIGHT", "" + height);
			pagesTemplate = pagesTemplate.replaceAll("\\@PAGES", Matcher
					.quoteReplacement(pagesXML));

			root.createNewFile();
			PrintWriter printer = new PrintWriter(new FileOutputStream(root));
			printer.write(pagesTemplate);
			printer.flush();
			printer.close();
		} catch (IOException e) {
			throw new ConverterException(e);
		} catch (CommandException e) {
			throw new ConverterException(e);
		}

	}

}
