package pt.gov.dgarq.roda.services.client;

import java.net.URL;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for Browser service.
 * 
 * @author Rui Castro
 */
public class ClassificationPlanBrowserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;
			String producerUsername = null;

			if (args.length >= 6) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				if (args.length > 6) {
					producerUsername = args[6];
				}

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err
						.println(ClassificationPlanBrowserTest.class
								.getSimpleName()
								+ " roda-core-url username password casURL coreURL serviceURL [producerUserName]\n"
								+ "Ex: http://localhost:8080/roda-core demo-admin demo-admin https://localhost:8443/cas https://localhost:8443/roda-core/ https://localhost:8443/ rcastro");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Number of ClassificationPlan Fonds");
			System.out.println("**************************************");

			Filter producerFondsFilter = new Filter(
					SimpleDescriptionObject.FONDS_FILTER);
			producerFondsFilter.add(new ProducerFilterParameter(
					producerUsername));

			int collectionCount = browserService
					.getSimpleDescriptionObjectCount(producerFondsFilter);
			System.out.println(collectionCount + " fonds in the repository");

			System.out.println("\n**************************************");
			System.out.println("List of ClassificationPlan Fonds");
			System.out.println("**************************************");

			SimpleDescriptionObject[] fondsList = browserService
					.getSimpleDescriptionObjects(new ContentAdapter(
							producerFondsFilter, null, null));

			for (int i = 0; fondsList != null && i < fondsList.length; i++) {
				System.out.println(fondsList[i]);
			}

			if (fondsList != null && fondsList.length > 0) {

				System.out
						.println("\n*********************************************");
				System.out
						.println("Getting DescriptionObject of the first Fonds ("
								+ fondsList[0].getPid() + ")");
				System.out
						.println("*********************************************");

				DescriptionObject dObject = browserService
						.getDescriptionObject(fondsList[0].getPid());
				System.out.println(dObject);

			}

			System.out.println("\n**************************************");
			System.out
					.println("Inspecting children of all fonds with ContentAdapter");
			System.out.println("**************************************");

			for (int i = 0; fondsList != null && i < fondsList.length; i++) {

				SimpleDescriptionObject fonds = fondsList[i];

				Filter producerFilter = new Filter(new ProducerFilterParameter(
						producerUsername));

				SortParameter[] sorters = new SortParameter[] { new SortParameter(
						"label", true) };

				ContentAdapter contentAdapter = new ContentAdapter(
						producerFilter, new Sorter(sorters), null);

				System.out.println("\n**************************************");
				System.out.println("Number of children ODs of "
						+ fonds.getLabel() + " (" + fonds.getPid() + ")");
				System.out.println("**************************************");

				producerFilter.add(new SimpleFilterParameter("parentPID", fonds
						.getPid()));

				int childCount = browserService
						.getSimpleDescriptionObjectCount(contentAdapter
								.getFilter());
				System.out.println(childCount + " children DOs");

				int numOfChildrenForStep = 10;
				if (childCount <= numOfChildrenForStep) {

					System.out
							.println("\n**************************************");
					System.out.println("List of children ODs of "
							+ fonds.getLabel() + " (" + fonds.getPid() + ")");
					System.out
							.println("**************************************");

					SimpleDescriptionObject[] childrenOD = browserService
							.getSimpleDescriptionObjects(contentAdapter);

					for (int j = 0; childrenOD != null && j < childrenOD.length; j++) {
						SimpleDescriptionObject child = childrenOD[j];

						/*
						 * Print the child
						 */
						System.out.println("***\nChild OD: " + child);

					}

				} else {

					int maxSteps = 3;
					System.out
							.println("\n**************************************");
					System.out.println("List of children ODs of "
							+ fonds.getLabel() + " (" + fonds.getPid()
							+ ") only " + numOfChildrenForStep
							+ " at a time (3 steps max)");
					System.out
							.println("**************************************");

					for (int j = 0, step = 0; j < childCount && step < maxSteps; j += numOfChildrenForStep, step++) {

						contentAdapter.setSublist(new Sublist(j,
								numOfChildrenForStep));

						SimpleDescriptionObject[] childrenOD = browserService
								.getSimpleDescriptionObjects(contentAdapter);

						System.out.println("\n*** " + j + " to "
								+ (j + childrenOD.length - 1) + " ***");

						for (int k = 0; k < childrenOD.length; k++) {
							System.out.println(childrenOD[k]);
						}

					}
				}

			}

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				System.err.println("Cause exception:");
				e.getCause().printStackTrace();
			}
		}
	}
}
