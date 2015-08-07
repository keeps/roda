package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.roda.common.HTMLUtils;
import org.roda.index.IndexServiceException;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DescriptiveMetadataBundle;

public class BrowserHelper {
	private static final Logger LOGGER = Logger.getLogger(BrowserHelper.class);

	protected static BrowseItemBundle getItemBundle(String aipId, String localeString) throws GenericException {
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
			Iterable<Representation> representations = RodaCoreFactory.getModelService().listRepresentations(aipId);
			for (Representation representation : representations) {
				representationList.add(representation);
			}
			itemBundle.setRepresentations(representationList);

		} catch (StorageServiceException | ModelServiceException | RODAException e) {
			throw new GenericException("Error getting item bundle " + e.getMessage());
		}

		return itemBundle;
	}

	private static void getDescriptiveMetadata(String aipId, final Locale locale,
			List<DescriptiveMetadataBundle> descriptiveMetadataList)
					throws ModelServiceException, StorageServiceException {
		ClosableIterable<DescriptiveMetadata> listDescriptiveMetadataBinaries = RodaCoreFactory.getModelService()
				.listDescriptiveMetadataBinaries(aipId);
		try {
			for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
				Binary binary = RodaCoreFactory.getStorageService().getBinary(descriptiveMetadata.getStoragePath());
				String html = HTMLUtils.descriptiveMetadataToHtml(binary, RodaCoreFactory.getModelService(), locale);

				descriptiveMetadataList
						.add(new DescriptiveMetadataBundle(descriptiveMetadata.getId(), html, binary.getSizeInBytes()));
			}
		} finally {
			try {
				listDescriptiveMetadataBinaries.close();
			} catch (IOException e) {
				LOGGER.error("Error while while freeing up resources", e);
			}
		}
	}

	protected static List<SimpleDescriptionObject> getAncestors(SimpleDescriptionObject sdo) throws GenericException {
		try {
			return RodaCoreFactory.getIndexService().getAncestors(sdo);
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting parent", e);
			throw new GenericException("Error getting parent: " + e.getMessage());
		}
	}

	protected static IndexResult<SimpleDescriptionObject> findDescriptiveMetadata(Filter filter, Sorter sorter,
			Sublist sublist) throws GenericException {
		IndexResult<SimpleDescriptionObject> sdos;
		try {
			sdos = RodaCoreFactory.getIndexService().findDescriptiveMetadata(filter, sorter, sublist);
			LOGGER.debug(String.format("findDescriptiveMetadata(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, sdos));
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting collections", e);
			throw new GenericException("Error getting collections " + e.getMessage());
		}

		return sdos;

	}

	protected static Long countDescriptiveMetadata(Filter filter) throws GenericException {
		Long count;
		try {
			count = RodaCoreFactory.getIndexService().countDescriptiveMetadata(filter);
		} catch (IndexServiceException e) {
			LOGGER.debug("Error getting sub-elements count", e);
			throw new GenericException("Error getting sub-elements count " + e.getMessage());
		}

		return count;
	}

	protected static SimpleDescriptionObject getSimpleDescriptionObject(String aipId) throws GenericException {
		try {
			return RodaCoreFactory.getIndexService().retrieveDescriptiveMetadata(aipId);
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting SDO", e);
			throw new GenericException("Error getting SDO: " + e.getMessage());
		}
	}
	
	protected static String getParent(String aipId) throws GenericException{
		try {
			return RodaCoreFactory.getIndexService().getParentId(aipId);
		} catch (IndexServiceException e) {
			LOGGER.error("Error getting parent", e);
			throw new GenericException("Error getting parent: " + e.getMessage());
		}
	}
}
