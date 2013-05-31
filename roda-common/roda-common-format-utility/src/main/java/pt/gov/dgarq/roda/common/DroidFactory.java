package pt.gov.dgarq.roda.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBException;

import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Class for detecting formats by DROID libraries
 * 
 * @author Ing. Vladislav Korecký [vladislav_korecky@gordic.cz] - GORDIC spol. s
 *         r.o.
 * 
 */
public class DroidFactory {
	private File fileSignaturesFile;
	private File sourceFile;
	private int maxBytesToScan = -1;

	/**
	 * Constructor
	 * 
	 * @throws URISyntaxException
	 */
	public DroidFactory(File sourceFile) throws URISyntaxException {
		this.sourceFile = sourceFile;
		// TODO: DROID signature file is now in resources, auto update from URL
		// will be good option. URL:
		// http://www.nationalarchives.gov.uk/aboutapps/pronom/droid-signature-files.htm
		this.fileSignaturesFile = new File(FormatUtility.class.getResource(
				"DROID_SignatureFile_V65.xml").toURI());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 */
	public FileFormat execute() throws FormatDetectionException, IOException {
		if (!sourceFile.exists())
			throw new IOException("Source file doesn't exist.");
		FileFormat fileFormat = null;
		BinarySignatureIdentifier binarySignatureIdentifier = new BinarySignatureIdentifier();
		if (!fileSignaturesFile.exists()) {
			throw new FormatDetectionException("Signature file not found");
		}
		binarySignatureIdentifier.setSignatureFile(fileSignaturesFile.getAbsolutePath());
		try {
			binarySignatureIdentifier.init();
		} catch (SignatureParseException e) {
			throw new FormatDetectionException("Can't parse signature file");
		}
		binarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);

		String fileName = sourceFile.getCanonicalPath();
		URI uri = sourceFile.toURI();
		RequestMetaData metaData = new RequestMetaData(sourceFile.length(),
				sourceFile.lastModified(), fileName);
		RequestIdentifier identifier = new RequestIdentifier(uri);
		identifier.setParentId(1L);

		InputStream in = null;
		IdentificationRequest request = new FileSystemIdentificationRequest(
				metaData, identifier);
		try {
			in = new FileInputStream(sourceFile);
			request.open(in);
			IdentificationResultCollection results = binarySignatureIdentifier
					.matchBinarySignatures(request);

			// Remove format duplicates, format with lower priority is removed
			binarySignatureIdentifier.removeLowerPriorityHits(results);

			if (results.getResults().size() < 1)
				throw new FormatDetectionException("Unknown format.");
			else if (results.getResults().size() > 1) {
				StringBuilder strBuilder = new StringBuilder();
				for (IdentificationResult result : results.getResults())
					strBuilder.append(result.getPuid());
				strBuilder.append(", ");
				throw new FormatDetectionException(
						String.format(
								"More then one format detected, unknown format. Detected PRONOM PUIDs: %s",
								strBuilder.toString()));
			} else {
				// One format detected
				// TODO: check that all fileFormat data are fill properly
				IdentificationResult result = results.getResults().get(0);
				fileFormat = new FileFormat();
				fileFormat.setName(result.getMimeType());
				fileFormat.setMimetype(result.getMimeType());
				fileFormat.setPuid(result.getPuid());
				fileFormat.setVersion(result.getVersion());
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return fileFormat;
	}
}
