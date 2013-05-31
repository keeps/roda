package pt.gov.dgarq.roda.ingest.siputility;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPEventPreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationPreservationObject;

/**
 * Test class for {@link SIPUtility#readSIP(File)}.
 * 
 * @author Rui Castro
 */
public class ReadSIP2Test {
	private static final Logger logger = Logger.getLogger(ReadSIP2Test.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {

			System.err.println("Wrong number of arguments!");
			System.err.println("Use: java " + ReadSIP2Test.class
					+ " sip_directory");

			System.exit(1);

		} else {
			File read_directory = new File(args[0]);

			try {

				SIP sip = SIPUtility.readSIP(read_directory, true);
				logger.info("SIP read " + sip);

				printSIP(sip);

			} catch (SIPException e) {
				logger.debug("Error reading SIP - " + e.getMessage(), e);
				System.exit(1);
			}
		}

	}

	private static void printSIP(SIP sip) {
		System.out.println("SIP parentPID=" + sip.getParentPID()
				+ ", directory=" + sip.getDirectory());
		printDO(sip.getDescriptionObject(), 1);
	}

	private static void printDO(SIPDescriptionObject descriptionObject,
			int level) {

		System.out.println(StringUtils.repeat("\t", level) + "DO "
				+ descriptionObject);

		if (descriptionObject.getRepresentations() != null) {

			for (SIPRepresentationObject ro : descriptionObject
					.getRepresentations()) {
				printRO(ro, level);
			}
		}

		if (descriptionObject.getChildren() != null) {
			for (SIPDescriptionObject childDO : descriptionObject.getChildren()) {
				printDO(childDO, level + 1);
			}
		}
	}

	private static void printRO(SIPRepresentationObject ro, int level) {
		System.out.println(StringUtils.repeat("\t", level) + "RO " + ro);

		System.out.println(StringUtils.repeat("\t", level + 1) + "rootFile="
				+ ro.getRootFile());

		if (ro.getPartFiles() != null) {
			for (RepresentationFile rFile : ro.getPartFiles()) {
				System.out.println(StringUtils.repeat("\t", level + 1)
						+ "partFile=" + rFile);
			}
		}

		if (ro.getPreservationObject() != null) {
			printPO(ro.getPreservationObject(), level);
		}
	}

	private static void printPO(SIPRepresentationPreservationObject po,
			int level) {

		System.out.println(StringUtils.repeat("\t", level) + "PO " + po);

		System.out.println(StringUtils.repeat("\t", level + 1) + "rootFile="
				+ po.getRootFile());

		if (po.getPartFiles() != null) {
			for (RepresentationFilePreservationObject rfpo : po.getPartFiles()) {
				System.out.println(StringUtils.repeat("\t", level + 1)
						+ "partFile=" + rfpo);
			}
		}

		for (SIPEventPreservationObject epo : po.getPreservationEvents()) {
			System.out.println(StringUtils.repeat("\t", level + 1)
					+ "PO preservationEvent=" + epo);
			System.out.println(StringUtils.repeat("\t", level + 2)
					+ "PO preservationAgent=" + epo.getAgent());
		}

		System.out.println(StringUtils.repeat("\t", level + 1)
				+ "PO derivationEvent=" + po.getDerivationEvent());
		System.out.println(StringUtils.repeat("\t", level + 1)
				+ "PO derivedFromRepresentationObject="
				+ po.getDerivedFromRepresentationObject());

		if (po.getDerivedFromRepresentationObject() != null) {
			printPO(po.getDerivedFromRepresentationObject(), level + 1);
		}
	}
}
