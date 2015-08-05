package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import org.roda.storage.StorageActionException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;

public class Browser {
	public static BrowseItemBundle getItemBundle(CASUserPrincipal user, String aipId, String localeString)
			throws AuthorizationDeniedException {

		// check user permissions
		UserUtility.checkRoles(user, "browse");

		// delegate
		BrowseItemBundle itemBundle = BrowserHelper.getItemBundle(aipId, localeString);

		// register action
		LogEntry logEntry;
		try {
			RodaCoreFactory.getModelService().addLogEntry(logEntry);
		} catch (StorageActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return itemBundle;
	}
}
