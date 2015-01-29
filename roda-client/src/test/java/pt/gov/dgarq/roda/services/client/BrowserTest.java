package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
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
public class BrowserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;
			String doPIDWithReps = null;
			String roPID = null;

			if (args.length == 6) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				doPIDWithReps = args[4];
				roPID = args[5];

				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length == 8) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				doPIDWithReps = args[6];
				roPID = args[7];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err
						.println(BrowserTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL doPID roPID");
				System.exit(1);
			}

			Browser browserService = rodaClient.getBrowserService();

			System.out.println("\n**************************************");
			System.out.println("Number of Fonds");
			System.out.println("**************************************");

			int collectionCount = browserService
					.getSimpleDescriptionObjectCount(SimpleDescriptionObject.FONDS_FILTER);
			System.out.println(collectionCount + " fonds in the repository");

			SimpleDescriptionObject[] fondsList = browserService
					.getSimpleDescriptionObjects(new ContentAdapter(
							SimpleDescriptionObject.FONDS_FILTER, null, null));

			System.out.println("\n**************************************");
			System.out.println("List of Fonds");
			System.out.println("**************************************");

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

				int indexToTest = (int) (Math.random() * fondsList.length);

				System.out.println("\n**************************************");
				System.out.println("Fonds index for fonds "
						+ fondsList[indexToTest].getLabel() + " ("
						+ indexToTest + ")");
				System.out.println("**************************************");

				int collectionIndex = browserService
						.getSimpleDescriptionObjectIndex(fondsList[indexToTest]
								.getPid(), new ContentAdapter(
								SimpleDescriptionObject.FONDS_FILTER, null,
								null));

				System.out.print("Index of fonds(" + indexToTest + ") is "
						+ collectionIndex + ". ");
				if (indexToTest == collectionIndex) {
					System.out.println("OK");
				} else {
					System.out.println("FAILED");
				}

				System.out.println("\n**************************************");
				System.out.println("Permissions of the first fonds ("
						+ fondsList[0].getPid() + ")");
				System.out.println("**************************************");

				System.out.println(browserService
						.getRODAObjectPermissions(fondsList[0].getPid()));

				System.out.println("\n**************************************");
				System.out.println("User permissions of the first fonds ("
						+ fondsList[0].getPid() + ")");
				System.out.println("**************************************");

				System.out.println(browserService
						.getRODAObjectUserPermissions(fondsList[0].getPid()));

				System.out.println("\n**************************************");
				System.out.println("Producers of the first fonds ("
						+ fondsList[0].getPid() + ")");
				System.out.println("**************************************");

				System.out.println(browserService.getProducers(fondsList[0]
						.getPid()));

			}

			System.out
					.println("\n**************************************************");
			System.out
					.println(" Asking for ascentors of inexistent PID (fake:pid) ");
			System.out
					.println("**************************************************");

			try {

				browserService.getDOAncestorPIDs("fake:pid");

				System.err
						.println("object with PID 'fake:pid' shouldn't exist. "
								+ "A NoSuchRODAObjectException was expected");

			} catch (NoSuchRODAObjectException e) {
				System.out.println("NoSuchRODAObjectException thrown. OK");
				e.printStackTrace(System.out);
			}

			System.out.println("\n**************************************");
			System.out
					.println("Inspecting children of all fonds with ContentAdapter");
			System.out.println("**************************************");

			for (int i = 0; fondsList != null && i < fondsList.length; i++) {

				SimpleDescriptionObject fonds = fondsList[i];

				SortParameter[] sorters = new SortParameter[] { new SortParameter(
						"label", true) };

				ContentAdapter contentAdapter = new ContentAdapter(
						new Filter(), new Sorter(sorters), null);

				System.out.println("\n**************************************");
				System.out.println("Number of children ODs of "
						+ fonds.getLabel() + " (" + fonds.getPid() + ")");
				System.out.println("**************************************");

				contentAdapter.getFilter().add(
						new SimpleFilterParameter("parentPID", fonds.getPid()));
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

						/*
						 * Check child index
						 */
						int subElementRetunedIndex = browserService
								.getSimpleDescriptionObjectIndex(
										child.getPid(), contentAdapter);
						System.out.print("Child index is "
								+ subElementRetunedIndex + ". ");
						if (subElementRetunedIndex == j) {
							System.out.println("OK");
						} else {
							System.out.println("FAILED");
						}

						/*
						 * Check child ancestors
						 */
						String[] ancestorPIDs = browserService
								.getDOAncestorPIDs(child.getPid());
						System.out.println("Child ancestor PIDs: "
								+ Arrays.asList(ancestorPIDs));

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

			/*
			 * Check child ancestors
			 */
			String[] ancestorPIDs = browserService
					.getDOAncestorPIDs(doPIDWithReps);
			System.out.println("\n**************************************");
			System.out.println("getDOAncestorPIDs(" + doPIDWithReps + ") "
					+ Arrays.asList(ancestorPIDs));
			System.out.println("\n**************************************");

			System.out.println("\n**************************************");
			System.out.println(" getOriginalRepresentation(" + doPIDWithReps
					+ ") ");
			System.out.println("**************************************");

			RepresentationObject representationObject = browserService
					.getDOOriginalRepresentation(doPIDWithReps);
			System.out.println("Original representation: "
					+ representationObject);

			System.out.println("\n**************************************");
			System.out.println(" getNormalizedRepresentation(" + doPIDWithReps
					+ ") ");
			System.out.println("**************************************");

			representationObject = browserService
					.getDONormalizedRepresentation(doPIDWithReps);
			System.out.println("Normalized representation: "
					+ representationObject);

			System.out.println("\n**************************************");
			System.out.println(" getRepresentations(" + doPIDWithReps
					+ ") DO PID");
			System.out.println("**************************************");

			RepresentationObject[] representationObjects = browserService
					.getDORepresentations(doPIDWithReps);

			if (representationObjects != null) {
				System.out.println("Representations: "
						+ Arrays.asList(representationObjects));
			} else {
				System.out.println("NO Representations");
			}

			System.out.println("\n**************************************");
			System.out.println(" getPreservationObjects(" + doPIDWithReps
					+ ") DO PID");
			System.out.println("**************************************");

			RepresentationPreservationObject[] preservationObjects = browserService
					.getDOPreservationObjects(doPIDWithReps);
			System.out.println(Arrays.toString(preservationObjects));

			System.out.println("\n**************************************");
			System.out.println(" getRepresentationObject(" + roPID
					+ ") by RO PID");
			System.out.println("**************************************");

			representationObject = browserService
					.getRepresentationObject(roPID);
			System.out.println("Representation: " + representationObject);

			System.out.println("\n**************************************");
			System.out.println(" getPreservationObjectForRepresentation("
					+ roPID + ") RO PID");
			System.out.println("**************************************");

			RepresentationPreservationObject repPO = browserService
					.getROPreservationObject(roPID);
			System.out.println(repPO);

			System.out.println("\n**************************************");
			System.out.println(" getRepresentationPreservationObject("
					+ repPO.getPid() + ") Representation PO PID");
			System.out.println("**************************************");

			repPO = browserService.getRepresentationPreservationObject(repPO
					.getPid());
			System.out.println(repPO);

			System.out.println("\n**************************************");
			System.out.println(" getPreservationEvents(" + repPO.getPid()
					+ ") Representation PO PID");
			System.out.println("**************************************");

			EventPreservationObject[] preservationEvents = browserService
					.getPreservationEvents(repPO.getPid());
			System.out.println(Arrays.toString(preservationEvents));

			if (repPO.getPreservationEventIDs() != null
					&& repPO.getPreservationEventIDs().length > 0) {

				String eventPOPID = repPO.getPreservationEventIDs()[0];

				System.out.println("\n**************************************");
				System.out.println(" getEventPreservationObject(" + eventPOPID
						+ ") Event PO PID");
				System.out.println("**************************************");

				EventPreservationObject eventPreservationObject = browserService
						.getEventPreservationObject(eventPOPID);
				System.out.println(eventPreservationObject);

				if (eventPreservationObject.getAgentID() != null) {

					System.out
							.println("\n**************************************");
					System.out.println(" getAgentPreservationObject("
							+ eventPreservationObject.getAgentID()
							+ ") Agent PO PID");
					System.out
							.println("**************************************");

					AgentPreservationObject agentPreservationObject = browserService
							.getAgentPreservationObject(eventPreservationObject
									.getAgentID());
					System.out.println(agentPreservationObject);
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
