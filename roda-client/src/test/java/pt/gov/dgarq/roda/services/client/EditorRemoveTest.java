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

			if (args.length == 2) {

				// http://localhost:8180/
				String hostUrl = args[0];
				doPID = args[1];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length == 4) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				doPID = args[3];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err
						.println(EditorRemoveTest.class.getSimpleName()
								+ " protocol://hostname:port/ [username password] doPID");
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
