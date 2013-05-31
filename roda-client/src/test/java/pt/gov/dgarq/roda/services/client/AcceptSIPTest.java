package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.stubs.AcceptSIP;

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

			if (args.length == 3) {

				// http://localhost:8180/
				String hostUrl = args[0];

				sipID = args[1];
				accept = Boolean.parseBoolean(args[2]);

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 4) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];

				sipID = args[3];
				accept = Boolean.parseBoolean(args[4]);

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);

			} else {
				System.err
						.println(IngestMonitorTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] SIP_ID true|false");
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
