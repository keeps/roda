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
public class CreateSIPWithMultipleDWTest {
	private static final Logger logger = Logger
			.getLogger(CreateSIPWithMultipleDWTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 3) {

			System.err.println("Wrong number of arguments!");
			System.err.println("Use: java " + CreateSIPWithMultipleDWTest.class
					+ " SIP_file parent_PID images_dir+ ");

			System.exit(1);

		} else {

			try {

				String sipFile = args[0];
				String parentPID = args[1];

				List<SIPDescriptionObject> sipDOs = new ArrayList<SIPDescriptionObject>();

				String parentDOID = "";
				String parentDOTitle = "Images from ";

				for (int i = 2; i < args.length; i++) {

					File imagesDirectory = new File(args[i]);

					List<String> filenames = new ArrayList<String>();
					List<InputStream> streams = new ArrayList<InputStream>();

					for (File imageFile : imagesDirectory.listFiles()) {
						filenames.add(imageFile.getName());
						streams.add(new FileInputStream(imageFile));
					}

					SIPDescriptionObject sipDO = new SIPDescriptionObject();
					sipDO.setLevel(DescriptionLevel.ITEM);
					sipDO.setCountryCode("PT");
					sipDO.setRepositoryCode("DGARQ");
					sipDO.setId(imagesDirectory.getName().replace(" ", "_"));
					sipDO.setTitle("Images from " + imagesDirectory.getName());
					sipDO.setOrigination(CreateSIPWithMultipleDWTest.class
							.getSimpleName());
					sipDO
							.setScopecontent("Test DigitalizedWork document created by "
									+ sipDO.getOrigination());

					StreamRepresentationObject streamRO = new DigitalizedWorkRepresentationBuilder()
							.createRepresentation(filenames, streams);

					SIPRepresentationObject sipRO = SIPUtility
							.saveStreamRepresentationToDirectory(streamRO,
									TempDir.createUniqueDirectory("rep"));
					sipDO.addRepresentation(sipRO);

					sipDOs.add(sipDO);
					parentDOID += ("".equals(parentDOID)) ? sipDO.getId() : "-"
							+ sipDO.getId();
					parentDOTitle += imagesDirectory.getName() + ", ";
				}

				SIPDescriptionObject doParent = new SIPDescriptionObject();
				doParent.setLevel(DescriptionLevel.FILE);
				doParent.setCountryCode("PT");
				doParent.setRepositoryCode("DGARQ");
				doParent.setId(parentDOID);
				doParent.setTitle(parentDOTitle);
				doParent.setOrigination(CreateSIPWithMultipleDWTest.class
						.getSimpleName());
				doParent.setScopecontent("Test DC with several DW documents");

				doParent.setChildren(sipDOs);

				SIP sip = new SIP();
				sip.setDescriptionObject(doParent);
				sip.setParentPID(parentPID);

				logger.debug("Created SIP " + sip);

				FileOutputStream sipFileOutputStream = new FileOutputStream(
						sipFile);
				SIPUtility.writeSIPPackage(sip, sipFileOutputStream);
				sipFileOutputStream.close();

				System.out.println("SIP created in " + sipFile);

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		}
	}
}
