package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.roda.model.ModelServiceException;
import org.roda.storage.StorageActionException;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DescriptiveMetadataBundle;

public class BrowserHelper {
	public static BrowseItemBundle getItemBundle(String aipId, String localeString) {
		final Locale locale = ServerTools.parseLocale(localeString);
		BrowseItemBundle itemBundle = new BrowseItemBundle();
		try {
			// set sdo
			SimpleDescriptionObject sdo = getSimpleDescriptionObject(aipId);
			itemBundle.setSdo(sdo);

			// set sdo ancestors
			itemBundle.setSdoAncestors(getAncestors(sdo));

			// set descriptive metadata
			List<DescriptiveMetadataBundle> descriptiveMetadataList = new ArrayList<DescriptiveMetadataBundle>();
			getDescriptiveMetadata(aipId, locale, descriptiveMetadataList);
			itemBundle.setDescriptiveMetadata(descriptiveMetadataList);

			// set representations
			// FIXME perhaps this information should be indexed as well
			List<Representation> representationList = new ArrayList<Representation>();
			Iterable<Representation> representations = model.listRepresentations(aipId);
			for (Representation representation : representations) {
				representationList.add(representation);
			}
			itemBundle.setRepresentations(representationList);

		} catch (StorageActionException | ModelServiceException | RODAException e) {
			throw new GenericException("Error getting item bundle " + e.getMessage());
		}

		return itemBundle;
	}
}
