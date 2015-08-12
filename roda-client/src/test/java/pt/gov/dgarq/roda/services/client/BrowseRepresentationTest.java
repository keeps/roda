package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for Browser service.
 * 
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class BrowseRepresentationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;

			if (args.length == 4) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length == 5) {

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
				System.err.println(BrowseRepresentationTest.class
						.getSimpleName()
						+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println(" getSimpleRepresentationObjectCount(null filter)");
			System.out.println("**************************************");

			int repCount = browserService.getSimpleRepresentationObjectCount(null);
			System.out.println(repCount + " representations.");

			System.out.println("\n**************************************");
			System.out.println(" getSimpleRepresentationObjects(type:"
					+ SimpleRepresentationObject.DIGITALIZED_WORK + ")");
			System.out.println("**************************************");

//			ContentAdapter contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.DIGITALIZED_WORK) }),
//					null, null);
//
//			SimpleRepresentationObject[] srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//
//			System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.STRUCTURED_TEXT + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.STRUCTURED_TEXT) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//
//			System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.RELATIONAL_DATABASE + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.RELATIONAL_DATABASE) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//                        
//                        System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.EMAIL + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.EMAIL) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//                        
//                        System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.PRESENTATION + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.PRESENTATION) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//                        
//                        System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.SPREADSHEET + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.SPREADSHEET) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//                        
//                        System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.VECTOR_GRAPHIC + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.VECTOR_GRAPHIC) }),
//					null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}
//
//			System.out.println("\n**************************************");
//			System.out.println(" getSimpleRepresentationObjects(type:"
//					+ SimpleRepresentationObject.UNKNOWN + ")");
//			System.out.println("**************************************");
//
//			contentAdapter = new ContentAdapter(new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("type",
//							SimpleRepresentationObject.UNKNOWN) }), null, null);
//
//			srObjects = browserService
//					.getSimpleRepresentationObjects(contentAdapter);
//			if (srObjects != null) {
//				for (SimpleRepresentationObject sro : srObjects) {
//					System.out.println(sro);
//				}
//			}

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				System.err.println("Cause exception:");
				e.getCause().printStackTrace();
			}
		}
	}
}
