package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.stubs.AcceptSIP;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class AcceptSIPTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RODAClient rodaClient = null;

		try {

			String sipID = null;
			boolean accept = false;

			if (args.length == 6) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				sipID = args[4];
				accept = Boolean.parseBoolean(args[5]);
				CASUtility casUtility = new CASUtility(new URL(casURL),new URL(coreURL),new URL(serviceURL));

				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length >= 7) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				sipID = args[6];
				accept = Boolean.parseBoolean(args[7]);
				CASUtility casUtility = new CASUtility(new URL(casURL),new URL(coreURL),new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);

			} else {
				System.err
						.println(IngestMonitorTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] [casURL] [coreServicesURL] [ServiceURL] SIP_ID true|false");
				System.exit(1);
			}

			AcceptSIP ingestMonitorService = rodaClient.getAcceptSIPService();

			System.out.println("\n********************************");
			System.out.println("Accept SIP " + sipID + " - " + accept);
			System.out.println("********************************");

			SIPState acceptedSIP = ingestMonitorService.acceptSIP(sipID,
					accept, "testing");

			System.out.println("Accepted SIP " + acceptedSIP);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
