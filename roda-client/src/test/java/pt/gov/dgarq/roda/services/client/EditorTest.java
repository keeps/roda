package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class EditorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		List<String> createdObjectPIDs = new ArrayList<String>();

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
				System.err.println(EditorTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			Editor editorService = rodaClient.getEditorService();
			Browser browserService = rodaClient.getBrowserService();

			String fondsPID = null;

			try {

				System.out.println("\n**************************************");
				System.out.println("Create Fonds");
				System.out.println("**************************************");

				DescriptionObject fondsDO = new DescriptionObject();
				fondsDO.setLevel(DescriptionLevel.FONDS);
				fondsDO.setCountryCode("PT");
				fondsDO.setRepositoryCode("DGARQ");
				fondsDO.setId("test fonds");
				fondsDO.setTitle("test fonds");
				fondsDO.setDateInitial("2009");
				fondsDO.setDateFinal("2009");
				fondsDO.setOrigination("test class");
				fondsDO.setScopecontent("test fonds");

				Date start = new Date();
				fondsPID = editorService.createDescriptionObject(fondsDO);
				Date stop = new Date();
				double duration = ((double) (stop.getTime() - start.getTime())) / 1000;

				System.out.println("New fonds created with pid " + fondsPID);
				System.out.println("duration: " + duration + " sec");

				System.out.println("New fonds "
						+ browserService.getDescriptionObject(fondsPID));
				createdObjectPIDs.add(fondsPID);

				System.out.println("\n**************************************");
				System.out.println("Set Fonds Producers");
				System.out.println("**************************************");

				start = new Date();
				editorService.setProducers(fondsPID, new Producers(fondsPID,
						new String[] { rodaClient.getUsername() },
						new String[0]));
				stop = new Date();
				duration = ((double) (stop.getTime() - start.getTime())) / 1000;
				System.out.println("duration: " + duration + " sec");

				System.out.println("\n**************************************");
				System.out.println("Get Fonds Producers");
				System.out.println("**************************************");

				System.out.println(browserService.getProducers(fondsPID));

				System.out.println("\n**************************************");
				System.out.println("Create Subfonds");
				System.out.println("**************************************");

				// FIXME commented because static description levels doesn't
				// exist anymore - START
				// DescriptionObject tempDObject = new DescriptionObject();
				// tempDObject.setLevel(DescriptionLevel.SUBFONDS);
				// tempDObject.setId("test subfonds");
				// tempDObject.setCountryCode("PT");
				// tempDObject.setRepositoryCode("DGARQ");
				// tempDObject.setTitle("test subfonds title");
				// tempDObject.setOrigination("Rui Castro");
				// tempDObject.setScopecontent("Âmbito e conteúdo  de teste");
				// tempDObject.setParentPID(fondsPID);
				//
				// start = new Date();
				// String subfondsPID = editorService
				// .createDescriptionObject(tempDObject);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("New subfonds created with pid "
				// + subfondsPID);
				// System.out.println("duration: " + duration + " sec");
				//
				// System.out.println("New subfonds "
				// + browserService.getDescriptionObject(subfondsPID));
				// createdObjectPIDs.add(subfondsPID);
				//
				// System.out.println("\n**************************************");
				// System.out.println("Create Series");
				// System.out.println("**************************************");
				//
				// // String seriesPID =
				// // editorService.createChildDescriptionObject(
				// // subfondsPID, DescriptionLevel.SERIES, "test series");
				// tempDObject = new DescriptionObject();
				// tempDObject.setLevel(DescriptionLevel.SERIES);
				// tempDObject.setId("test series");
				// tempDObject.setCountryCode("PT");
				// tempDObject.setRepositoryCode("DGARQ");
				// tempDObject.setTitle("test series title");
				// tempDObject.setOrigination("Rui Castro");
				// tempDObject.setScopecontent("Âmbito e conteúdo  de teste");
				// tempDObject.setParentPID(subfondsPID);
				//
				// start = new Date();
				// String seriesPID = editorService
				// .createDescriptionObject(tempDObject);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("New series created with pid " +
				// seriesPID);
				// System.out.println("duration: " + duration + " sec");
				// System.out.println("New series "
				// + browserService.getDescriptionObject(seriesPID));
				// createdObjectPIDs.add(seriesPID);
				//
				// System.out.println("\n**************************************");
				// System.out.println("Create SubSeries");
				// System.out.println("**************************************");
				//
				// // String subSeriesPID = editorService
				// // .createChildDescriptionObject(subfondsPID,
				// // DescriptionLevel.SUBSERIES, "test sub-series");
				// tempDObject = new DescriptionObject();
				// tempDObject.setLevel(DescriptionLevel.SUBSERIES);
				// tempDObject.setId("test sub-series");
				// tempDObject.setCountryCode("PT");
				// tempDObject.setRepositoryCode("DGARQ");
				// tempDObject.setTitle("test sub-series title");
				// tempDObject.setOrigination("Rui Castro");
				// tempDObject.setScopecontent("Âmbito e conteúdo  de teste");
				// tempDObject.setParentPID(subfondsPID);
				//
				// start = new Date();
				// String subSeriesPID = editorService
				// .createDescriptionObject(tempDObject);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("New sub-series created with pid "
				// + subSeriesPID);
				// System.out.println("duration: " + duration + " sec");
				// System.out.println("New sub-series "
				// + browserService.getDescriptionObject(subSeriesPID));
				// createdObjectPIDs.add(subSeriesPID);
				//
				// System.out.println("\n**************************************");
				// System.out.println("Create Document");
				// System.out.println("**************************************");
				//
				// // String documentPID = editorService
				// // .createChildDescriptionObject(subSeriesPID,
				// // DescriptionLevel.ITEM, "test document");
				// tempDObject = new DescriptionObject();
				// tempDObject.setLevel(DescriptionLevel.ITEM);
				// tempDObject.setId("test document");
				// tempDObject.setCountryCode("PT");
				// tempDObject.setRepositoryCode("DGARQ");
				// tempDObject.setTitle("test document title");
				// tempDObject.setOrigination("Rui Castro");
				// tempDObject.setScopecontent("Âmbito e conteúdo  de teste");
				// tempDObject.setParentPID(subSeriesPID);
				//
				// start = new Date();
				// String documentPID = editorService
				// .createDescriptionObject(tempDObject);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("New document created with pid "
				// + documentPID);
				// System.out.println("duration: " + duration + " sec");
				// createdObjectPIDs.add(documentPID);
				//
				// System.out.println("\n**************************************");
				// System.out.println("Edit Document");
				// System.out.println("**************************************");
				//
				// DescriptionObject descriptionObject = browserService
				// .getDescriptionObject(documentPID);
				//
				// System.out.println("Current description object: "
				// + descriptionObject);
				//
				// descriptionObject.setValue(DescriptionObject.LEVEL,
				// DescriptionLevel.FILE);
				// descriptionObject.setValue(DescriptionObject.TITLE, new Text(
				// "EDITED: test document title"));
				// descriptionObject.setValue(DescriptionObject.DATE_INITIAL,
				// new Text("2007-01-11"));
				// descriptionObject.setValue(DescriptionObject.DATE_FINAL,
				// new Text("2007-01-12"));
				// descriptionObject.setValue(
				// DescriptionObject.PHYSDESC_DIMENSIONS,
				// new PhysdescElement("tons of flax", "5"));
				// descriptionObject.setValue(DescriptionObject.SCOPECONTENT,
				// new Text("EDITED: Âmbito e conteúdo de teste"));
				// descriptionObject.setValue(DescriptionObject.NOTE, new Text(
				// "EDITED: test note"));
				//
				// start = new Date();
				// descriptionObject = editorService
				// .modifyDescriptionObject(descriptionObject);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("Edited description object: "
				// + descriptionObject);
				// System.out.println("duration: " + duration + " sec");
				//
				// System.out.println("\n**************************************");
				// System.out.println("Inspect edited object");
				// System.out.println("**************************************");
				//
				// System.out.println("\n**************************************");
				// System.out.println("getTitle() null");
				// System.out.println("**************************************");
				//
				// System.out
				// .println("isNull? " + descriptionObject.getTitle() == null);
				//
				// System.out.println("\n**************************************");
				// System.out.println("getNote()");
				// System.out.println("**************************************");
				//
				// System.out.println(descriptionObject.getNote());
				//
				// System.out.println("\n**************************************");
				// System.out.println("getPhysdescDimensions()");
				// System.out.println("**************************************");
				//
				// System.out.println(descriptionObject.getPhysdescDimensions());
				//
				// System.out.println("\n**************************************");
				// System.out.println("Possible levels for fonds");
				// System.out.println("**************************************");
				//
				// System.out.println(Arrays.asList(editorService
				// .getDOPossibleLevels(fondsPID)));
				//
				// System.out.println("\n**************************************");
				// System.out.println("Possible levels for subfonds");
				// System.out.println("**************************************");
				//
				// System.out.println(Arrays.asList(editorService
				// .getDOPossibleLevels(subfondsPID)));
				//
				// System.out.println("\n**************************************");
				// System.out.println("Possible levels for series");
				// System.out.println("**************************************");
				//
				// System.out.println(Arrays.asList(editorService
				// .getDOPossibleLevels(seriesPID)));
				//
				// System.out.println("\n**************************************");
				// System.out.println("Possible levels for subseries");
				// System.out.println("**************************************");
				//
				// System.out.println(Arrays.asList(editorService
				// .getDOPossibleLevels(subSeriesPID)));
				//
				// System.out.println("\n**************************************");
				// System.out.println("Possible levels for document");
				// System.out.println("**************************************");
				//
				// System.out.println(Arrays.asList(editorService
				// .getDOPossibleLevels(documentPID)));
				//
				// System.out.println("\n**************************************");
				// System.out.println("Move sub-serie from subfonds to series");
				// System.out.println("**************************************");
				//
				// DescriptionObject object = browserService
				// .getDescriptionObject(subSeriesPID);
				// System.out.println("SubSeries complete reference before move: "
				// + object.getCompleteReference());
				//
				// DescriptionObject subSeriesDO = browserService
				// .getDescriptionObject(subSeriesPID);
				// subSeriesDO.setParentPID(seriesPID);
				//
				// start = new Date();
				// object = editorService.modifyDescriptionObject(subSeriesDO);
				// stop = new Date();
				// duration = ((double) (stop.getTime() - start.getTime())) /
				// 1000;
				//
				// System.out.println("SubSeries complete reference after move: "
				// + object.getCompleteReference());
				// System.out.println("duration: " + duration + " sec");
				// FIXME commented because static description levels doesn't
				// exist anymore - END

				// System.out.println("\n**************************************")
				// ;
				// System.out.println("Remove created Document");
				// System.out.println("**************************************");
				//
				// editorService.removeDescriptionObject(documentPID);
				//
				// System.out.println("\n**************************************")
				// ;
				// System.out.println("Remove created SubSeries");
				// System.out.println("**************************************");
				//
				// editorService.removeDescriptionObject(subSeriesPID);
				//
				// System.out.println("\n**************************************")
				// ;
				// System.out.println("Remove created Series");
				// System.out.println("**************************************");
				//
				// editorService.removeDescriptionObject(seriesPID);

				System.out
						.println("\n*********************************************");
				System.out
						.println("Remove created Fonds (and all it's descendants)");
				System.out
						.println("**********************************************");

				start = new Date();
				editorService.removeDescriptionObject(fondsPID);
				stop = new Date();
				duration = ((double) (stop.getTime() - start.getTime())) / 1000;
				System.out.println("duration: " + duration + " sec");

			} catch (RemoteException e) {

				throw RODAClient.parseRemoteException(e);

			} catch (Exception e) {
				e.printStackTrace();

				// Remove the test fonds
				if (fondsPID != null) {
					editorService.removeDescriptionObject(fondsPID);
				}

				// for (Iterator iterator = createdObjectPIDs.iterator();
				// iterator
				// .hasNext();) {
				// String pid = (String) iterator.next();
				// try {
				//
				// editorService.removeDescriptionObject(pid);
				// System.out.println("Removed object " + pid);
				//
				// } catch (Exception e1) {
				// System.out.println("Exception (" + e1.getMessage()
				// + ") removing " + pid);
				// }
				// }

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
