package pt.gov.dgarq.roda.ingest.siputility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.ingest.siputility.builders.DigitalizedWorkRepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * Test class for {@link SIPUtility#createSIP(String, DescriptionObject, List)}
 * and {@link SIPUtility#writeSIPPackage(SIP, OutputStream)}.
 * 
 * @author Rui Castro
 */
public class CreateSIPWithDWTest {
	private static final Logger logger = Logger
			.getLogger(CreateSIPWithDWTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 3) {

			System.err.println("Wrong number of arguments!");
			System.err.println("Use: java " + CreateSIPWithDWTest.class
					+ " SIP_file parent_PID images_dir ");

			System.exit(1);

		} else {
			// FIXME commented because static description levels doesn't
			// exist anymore - START
			// try {
			//
			// String sipFile = args[0];
			// String parentPID = args[1];
			// File imagesDirectory = new File(args[2]);
			//
			// List<String> filenames = new ArrayList<String>();
			// List<InputStream> streams = new ArrayList<InputStream>();
			//
			// for (File imageFile : imagesDirectory.listFiles()) {
			// filenames.add(imageFile.getName());
			// streams.add(new FileInputStream(imageFile));
			// }
			//
			// SIPDescriptionObject sipDO = new SIPDescriptionObject();
			// sipDO.setLevel(DescriptionLevel.ITEM);
			// sipDO.setCountryCode("PT");
			// sipDO.setRepositoryCode("DGARQ");
			// // sipDO.setId(imagesDirectory.getName().replace(" ", "_"));
			// sipDO.setId(imagesDirectory.getName());
			// sipDO.setTitle("Images from " + imagesDirectory.getName());
			// sipDO.setOrigination(CreateSIPWithDWTest.class.getSimpleName());
			// sipDO
			// .setScopecontent("Test DigitalizedWork document created by "
			// + sipDO.getOrigination());
			//
			// StreamRepresentationObject streamRO = new
			// DigitalizedWorkRepresentationBuilder()
			// .createRepresentation(filenames, streams);
			//
			// SIPRepresentationObject sipRO = SIPUtility
			// .saveStreamRepresentationToDirectory(streamRO, TempDir
			// .createUniqueDirectory("rep1"));
			// sipDO.addRepresentation(sipRO);
			//
			// SIP sip = new SIP();
			// sip.setDescriptionObject(sipDO);
			// sip.setParentPID(parentPID);
			//
			// logger.debug("Created SIP " + sip);
			//
			// FileOutputStream sipFileOutputStream = new FileOutputStream(
			// sipFile);
			// SIPUtility.writeSIPPackage(sip, sipFileOutputStream);
			// sipFileOutputStream.close();
			//
			// System.out.println("SIP created in " + sipFile);
			//
			// } catch (Exception e) {
			// e.printStackTrace();
			// System.exit(1);
			// }
			// FIXME commented because static description levels doesn't
			// exist anymore - END

		}
	}
}
