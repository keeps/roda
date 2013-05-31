package pt.gov.dgarq.roda.ingest.siputility;

import java.io.File;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.ingest.siputility.data.SIP;

/**
 * Test class for {@link SIPUtility#readSIP(File)}.
 * 
 * @author Rui Castro
 */
public class ReadWriteSIP2Test {
	private static final Logger logger = Logger.getLogger(ReadWriteSIP2Test.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 2) {

			System.err.println("Wrong number of arguments!");
			System.err.println("Use: java " + ReadWriteSIP2Test.class
					+ " sip_directory sip_directory_to_write");

			System.exit(1);

		} else {
			File read_directory = new File(args[0]);
			File write_directory = new File(args[1]);

			try {

				SIP sip = SIPUtility.readSIP(read_directory, true);
				logger.info("SIP read " + sip);

				SIPUtility.writeSIP(sip, write_directory);

			} catch (SIPException e) {
				logger.debug("Error reading SIP - " + e.getMessage(), e);
				System.exit(1);
			}
		}

	}

}
