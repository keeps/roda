package pt.gov.dgarq.roda.wui.dissemination.browse.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.roda.common.HTMLUtils;
import org.roda.index.IndexServiceException;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.RepresentationState;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowseItemBundle;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.DescriptiveMetadataBundle;

public class BrowserHelper {
  private static final int BUNDLE_MAX_REPRESENTATION_COUNT = 2;
  private static final int BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT = 1;
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
      List<DescriptiveMetadataBundle> descriptiveMetadataList = getDescriptiveMetadataBundles(aipId, locale);
      itemBundle.setDescriptiveMetadata(descriptiveMetadataList);

      // set representations
      // getting the last 2 representations
      Sorter sorter = new Sorter(new SortParameter(RodaConstants.SRO_DATE_CREATION, true));
      IndexResult<Representation> findRepresentations = findRepresentations(aipId, sorter,
        new Sublist(0, BUNDLE_MAX_REPRESENTATION_COUNT));
      List<Representation> representations = findRepresentations.getResults();

      // if there are more representations ensure one original is there
      if (findRepresentations.getTotalCount() > findRepresentations.getLimit()) {
        boolean hasOriginals = findRepresentations.getResults().stream()
          .anyMatch(x -> x.getStatuses().contains(RepresentationState.ORIGINAL));
        if (!hasOriginals) {
          boolean onlyOriginals = true;
          IndexResult<Representation> findOriginalRepresentations = findRepresentations(aipId, onlyOriginals, sorter,
            new Sublist(0, BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT));
          representations.addAll(findOriginalRepresentations.getResults());
        }
      }

      itemBundle.setRepresentations(representations);

    } catch (StorageServiceException | ModelServiceException | RODAException e) {
      throw new GenericException("Error getting item bundle " + e.getMessage());
    }

    return itemBundle;
  }

  private static List<DescriptiveMetadataBundle> getDescriptiveMetadataBundles(String aipId, final Locale locale)
    throws ModelServiceException, StorageServiceException {
    ClosableIterable<DescriptiveMetadata> listDescriptiveMetadataBinaries = RodaCoreFactory.getModelService()
      .listDescriptiveMetadataBinaries(aipId);

    List<DescriptiveMetadataBundle> descriptiveMetadataList = new ArrayList<DescriptiveMetadataBundle>();
    try {
      for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
        Binary binary = RodaCoreFactory.getStorageService().getBinary(descriptiveMetadata.getStoragePath());
        String html = HTMLUtils.descriptiveMetadataToHtml(binary, locale);

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

    return descriptiveMetadataList;
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
    Sublist sublist, Facets facets) throws GenericException {
    IndexResult<SimpleDescriptionObject> sdos;
    try {
      sdos = RodaCoreFactory.getIndexService().find(SimpleDescriptionObject.class, filter, sorter, sublist, facets);
      LOGGER.debug(String.format("findDescriptiveMetadata(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, sdos));
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting collections", e);
      throw new GenericException("Error getting collections " + e.getMessage());
    }

    return sdos;
  }

  private static IndexResult<Representation> findRepresentations(String aipId, Sorter sorter, Sublist sublist)
    throws GenericException {
    return findRepresentations(aipId, false, sorter, sublist);
  }

  private static IndexResult<Representation> findRepresentations(String aipId, boolean onlyOriginals, Sorter sorter,
    Sublist sublist) throws GenericException {
    IndexResult<Representation> reps;
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.SRO_AIP_ID, aipId));
    if (onlyOriginals) {
      filter.add(new SimpleFilterParameter(RodaConstants.SRO_STATUS, RepresentationState.ORIGINAL.toString()));
    }
    Facets facets = null;
    try {
      reps = RodaCoreFactory.getIndexService().find(Representation.class, filter, sorter, sublist, facets);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting collections", e);
      throw new GenericException("Error getting collections " + e.getMessage());
    }

    return reps;
  }

  protected static Long countDescriptiveMetadata(Filter filter) throws GenericException {
    Long count;
    try {
      count = RodaCoreFactory.getIndexService().count(SimpleDescriptionObject.class, filter);
    } catch (IndexServiceException e) {
      LOGGER.debug("Error getting sub-elements count", e);
      throw new GenericException("Error getting sub-elements count " + e.getMessage());
    }

    return count;
  }

  protected static SimpleDescriptionObject getSimpleDescriptionObject(String aipId) throws GenericException {
    try {
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting SDO", e);
      throw new GenericException("Error getting SDO: " + e.getMessage());
    }
  }

  public static SimpleDescriptionObject moveInHierarchy(String aipId, String parentId) throws GenericException {
    try {
      StorageService storage = RodaCoreFactory.getStorageService();
      ModelService model = RodaCoreFactory.getModelService();
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);
      if (parentId == null || parentId.trim().equals("")) {
        StoragePath parentPath = ModelUtils.getAIPpath(parentId);
        storage.getDirectory(parentPath);
      }
      Map<String, Set<String>> metadata = storage.getMetadata(aipPath);
      if (parentId == null || parentId.trim().equalsIgnoreCase("")) {
        metadata.remove(RodaConstants.STORAGE_META_PARENT_ID);
      } else {
        metadata.put(RodaConstants.STORAGE_META_PARENT_ID, new HashSet<String>(Arrays.asList(parentId)));
      }
      storage.updateMetadata(aipPath, metadata, true);
      model.updateAIP(aipId, storage, aipPath);

      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (ModelServiceException | IndexServiceException | StorageServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error moving in hierarchy ");
      }

    }

  }

}
