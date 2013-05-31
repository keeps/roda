package pt.gov.dgarq.roda.migrator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.migrator.services.ConverterException;
import pt.gov.dgarq.roda.migrator.services.SynchronousConverter;
import pt.gov.dgarq.roda.services.client.BrowserTest;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class SynchronousMigratorTest {

	private static void testService(String coreUrl, String username,
			String password, String migratorUrl, String roPID) {
		try {
			RODAClient rodaClient = new RODAClient(new URL(coreUrl), username,
					password);
			MigratorClient migratorClient = new MigratorClient();
			Browser browserService = rodaClient.getBrowserService();
			SynchronousConverter converterService = migratorClient
					.getSynchronousConverterService(migratorUrl, username,
							password);

			System.out.println("\n**************************************");
			System.out.println("Get representation " + roPID);
			System.out.println("**************************************");

			RepresentationObject representationObject = browserService
					.getRepresentationObject(roPID);
			System.out.println(representationObject);

			System.out.println("\n**************************************");
			System.out.println("Converter Agent");
			System.out.println("**************************************");

			System.out.println(converterService.getAgent());

			System.out.println("\n**************************************");
			System.out.println("Converting representation " + roPID);
			System.out.println("**************************************");

			RepresentationObject convertedRepresentation = converterService
					.convert(representationObject);

			System.out.println("Converted representation "
					+ convertedRepresentation);
			if (convertedRepresentation != null) {
				System.out.println("\n**************************************");
				System.out.println("Getting converted representation");
				System.out.println("**************************************");

				String migratorName = migratorUrl.substring(migratorUrl
						.lastIndexOf('/'));

				File rodaTests = new File(System.getProperty("user.home"),
						"RODA-tests");
				if (!rodaTests.exists()) {
					rodaTests.mkdir();
				}

				File roTests = new File(rodaTests, roPID.replace(':', '_'));
				if (!roTests.exists()) {
					roTests.mkdir();
				}

				File migratorHome = new File(roTests, migratorName);

				migratorClient.saveRepresentation(convertedRepresentation,
						migratorHome);

				System.out.println("Representation downloaded to "
						+ migratorHome);

				System.out.println("\n**************************************");
				System.out.println("Delete cached converted representation");
				System.out.println("**************************************");

				migratorClient
						.deleteCachedRepresentation(convertedRepresentation);
			}
		} catch (RODAClientException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (NoSuchRODAObjectException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (BrowserException e) {
			e.printStackTrace();
		} catch (MigratorClientException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String... args) {
		if (args.length >= 5) {

			// http://localhost:8180/ user pass
			// http://localhost:8180/roda-migrator/services/DW2Tiff roPid:1
			// http://localhost:8180/roda-migrator/services/ST2Pdf roPid:2
			String coreUrl = args[0];
			String username = args[1];
			String password = args[2];

			for (int i = 3; i < args.length - 1; i += 2) {
				String migratorUrl = args[i];
				String roPID = args[i + 1];

				System.out.println("Testing migrator " + migratorUrl + " with "
						+ roPID);
				testService(coreUrl, username, password, migratorUrl, roPID);
			}

			System.out.println("\n**************************************");
			System.out.println("FINISHED TESTS");
			System.out.println("**************************************");
		} else {
			System.err
					.println(BrowserTest.class.getSimpleName()
							+ " rodaCoreHostURL(protocol://hostname:port/)  serviceName [username password] [rodaMigratorHostURL(protocol://hostname:port/) roPID]+");
			System.exit(1);
		}

	}

}
