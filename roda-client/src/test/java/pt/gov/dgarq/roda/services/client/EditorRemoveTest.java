package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.data.eadc.Text;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class EditorRemoveTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;
			String doPID = null;

			if (args.length == 5) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				doPID = args[4];

				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length == 7) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				doPID = args[6];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err
						.println(EditorRemoveTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL doPID");
				System.exit(1);
			}

			Editor editorService = rodaClient.getEditorService();

			System.out.println("\n**************************************");
			System.out.println("Remove DO " + doPID);
			System.out.println("**************************************");

			editorService.removeDescriptionObject(doPID);
			System.out.println("Remove OK");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
