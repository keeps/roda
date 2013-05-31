package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.stubs.LogMonitor;
import pt.gov.dgarq.roda.core.stubs.Logger;
import pt.gov.dgarq.roda.core.stubs.Reports;

/**
 * Test class for {@link Logger} and {@link LogMonitor} service.
 * 
 * @author Rui Castro
 */
public class ReportsTest {

	public static void main(String[] args) {

		RODAClient rodaClient = null;

		try {

			String reportID = null;

			if (args.length == 2) {

				// http://localhost:8180/ reportID
				String hostUrl = args[0];
				reportID = args[1];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 4) {

				// http://localhost:8180/ user pass reportID
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				reportID = args[3];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err
						.println(ReportsTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] reportID");
				System.exit(1);
			}

			Reports reportsService = rodaClient.getReportsService();

			System.out.println("\n********************************");
			System.out.println("Get report " + reportID);
			System.out.println("********************************");

			long start = System.currentTimeMillis();
			Report report = reportsService.getReport(reportID);
			long duration = System.currentTimeMillis() - start;

			System.out.println("Report received in " + duration / 1000
					+ " seconds");

			if (report == null) {
				System.out.println("Report is null");
			} else {
				printReport(report);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printReport(Report report) {

		System.out.println("Report " + report.getId());
		System.out.println("Title: " + report.getTitle());
		System.out.println("Type: " + report.getType());

		System.out.println("Attributes:");
		if (report.getAttributes() == null) {
			System.out.println("0 attributes");
		} else {
			for (Attribute attribute : report.getAttributes()) {
				System.out.println("\t" + attribute.getName() + ": "
						+ attribute.getValue());
			}
			System.out.println(report.getAttributes().length + " attributes");
		}

		System.out.println("Items:");
		if (report.getItems() == null) {
			System.out.println("0 items");
		} else {
			for (ReportItem item : report.getItems()) {
				System.out.println("==============================");
				System.out.println("Item " + item.getTitle());
				System.out.println("----------------------------");

				System.out.println("Attributes:");
				if (item.getAttributes() == null) {
					System.out.println("0 attributes");
				} else {
					for (Attribute attribute : item.getAttributes()) {
						System.out.println("\t" + attribute.getName() + ": "
								+ attribute.getValue());
					}
					System.out.println(report.getAttributes().length
							+ " attributes");
				}

				System.out.println("==============================");
			}
			System.out.println(report.getAttributes().length + " items");
		}
	}
}
