package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class IngestMonitorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RODAClient rodaClient = null;

		try {

			if (args.length == 4) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length == 6) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err.println(IngestMonitorTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			IngestMonitor ingestMonitorService = rodaClient
					.getIngestMonitorService();

			System.out.println("\n********************************");
			System.out.println("Get all possible states");
			System.out.println("********************************");

			String[] states = ingestMonitorService.getPossibleStates();

			System.out.println("Possible states: " + Arrays.asList(states));

			System.out.println("\n********************************");
			System.out.println("Get ALL SIPs Count");
			System.out.println("********************************");

			int sipsCount = ingestMonitorService.getSIPsCount(null);

			System.out.println(sipsCount + " SIPs");

			System.out.println("\n****************************************");
			System.out.println("Get SIPs not complete count ");
			System.out.println("****************************************");

			Filter filterNotComplete = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("complete", "false"),
					new OneOfManyFilterParameter("state", new String[] {
							"DROPED_FTP", "DROPED_UPLOAD_SERVICE", "UNPACKED",
							"VIRUS_FREE", "SIP_VALID", "AUTHORIZED",
							"SIP_INGESTED" }) });

			sipsCount = ingestMonitorService.getSIPsCount(filterNotComplete);

			System.out.println(sipsCount + " SIPs");

			System.out.println("\n****************************************");
			System.out.println("Get all SIPs not complete ");
			System.out.println("****************************************");

			SIPState[] sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterNotComplete, null, null));

			if (sips == null) {
				System.out.println("No SIPs\n");
			} else {

				System.out.println(sips.length + " SIPs:\n");

				for (int i = 0; i < sips.length; i++) {
					System.out.println(sips[i]);
				}
			}

			System.out.println("\n********************************");
			System.out.println("Get only the 3 first SIPs");
			System.out.println("********************************");

			sips = ingestMonitorService.getSIPs(new ContentAdapter(null, null,
					new Sublist(0, 3)));

			if (sips == null) {
				System.out.println("No SIPs\n");
			} else {

				System.out.println(sips.length + " SIPs:\n");

				for (int i = 0; i < sips.length; i++) {
					System.out.println(sips[i] + "\n\n");
				}
			}

			// System.out.println("\n********************************");
			// System.out.println("Get all SIPs");
			// System.out.println("********************************");
			//
			// sips = null;
			// sips = ingestMonitorService.getSIPs(null);
			//
			// if (sips == null) {
			// System.out.println("No SIPs\n");
			// } else {
			//
			// System.out.println(sips.length + " SIPs:\n");
			//
			// for (int i = 0; i < sips.length; i++) {
			// System.out.println(sips[i]);
			// }
			// }

			System.out.println("\n****************************************");
			System.out.println("Get all SIPs complete in state QUARANTINE");
			System.out.println("****************************************");

			Filter filterCompleteQuarantine = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("state", "QUARANTINE"),
					new SimpleFilterParameter("complete", "TRUE") });

			sips = null;
			sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterCompleteQuarantine, null, null));

			if (sips == null) {
				System.out.println("No SIPs\n");
			} else {

				System.out.println(sips.length + " SIPs:\n");

				for (int i = 0; i < sips.length; i++) {
					System.out.println(sips[i]);
				}
			}

			System.out.println("\n********************************");
			System.out.println("Get all SIPs not complete from user "
					+ rodaClient.getUsername());
			System.out.println("********************************");

			Filter filterSIPsNotComplete = new Filter(new FilterParameter[] {
					new SimpleFilterParameter("username", rodaClient
							.getUsername()),
					new SimpleFilterParameter("complete", "false") });

			sips = null;
			sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterSIPsNotComplete, null, null));

			if (sips == null) {
				System.out.println("No SIPs\n");
			} else {

				System.out.println(sips.length + " SIPs:\n");

				for (int i = 0; i < sips.length; i++) {
					System.out.println(sips[i]);
				}
			}

			System.out.println("\n********************************");
			System.out.println("Get all SIPs in state DROPED_FTP");
			System.out.println("********************************");

			Filter filterSIPsDropedFtp = new Filter(
					new FilterParameter[] { new SimpleFilterParameter("state",
							"DROPED_FTP") });

			sips = null;
			sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterSIPsDropedFtp, null, null));

			if (sips == null) {
				System.out.println("No SIPs\n");
			} else {

				System.out.println(sips.length + " SIPs:\n");

				for (int i = 0; i < sips.length; i++) {
					System.out.println(sips[i]);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
