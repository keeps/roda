package org.roda.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
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
import org.roda.storage.fs.FSUtils;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.Pair;
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
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.disseminators.common.tools.ZipEntryInfo;
import pt.gov.dgarq.roda.disseminators.common.tools.ZipTools;
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

  protected static Pair<String, StreamingOutput> getAipRepresentation(String aipId, String representationId)
    throws ModelServiceException, StorageServiceException, GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      Representation representation = model.retrieveRepresentation(aipId, representationId);

      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      List<String> fileIds = representation.getFileIds();
      for (String fileId : fileIds) {
        StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);
        Binary binary = storage.getBinary(filePath);
        ZipEntryInfo info = new ZipEntryInfo(filePath.getName(), binary.getContent().createInputStream());
        zipEntries.add(info);
      }

      final String filename;
      final StreamingOutput stream;
      if (zipEntries.size() == 1) {
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            IOUtils.copy(zipEntries.get(0).getInputStream(), os);
          }
        };
        filename = zipEntries.get(0).getName();
      } else {
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            ZipTools.zip(zipEntries, os);
          }
        };
        filename = aipId + "_" + representationId + ".zip";
      }

      return new Pair<String, StreamingOutput>(filename, stream);

    } catch (IOException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException("");
    }
  }

  /**
   * Returns valid start (pair first elem) and limit (pair second elem) paging
   * parameters defaulting to start = 0 and limit = 100 if none or invalid
   * values are provided.
   */
  private static Pair<Integer, Integer> processPagingParams(String start, String limit) {
    Integer startInteger, limitInteger;
    try {
      startInteger = Integer.parseInt(start);
      if (startInteger < 0) {
        startInteger = 0;
      }
    } catch (NumberFormatException e) {
      startInteger = 0;
    }
    try {
      limitInteger = Integer.parseInt(limit);
      if (limitInteger < 0) {
        limitInteger = 100;
      }
    } catch (NumberFormatException e) {
      limitInteger = 100;
    }

    return new Pair<Integer, Integer>(startInteger, limitInteger);
  }

  protected static Pair<String, StreamingOutput> listAipDescriptiveMetadata(String aipId, String start, String limit)
    throws ModelServiceException, StorageServiceException, GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      ClosableIterable<DescriptiveMetadata> metadata = model.listDescriptiveMetadataBinaries(aipId);
      Pair<Integer, Integer> pagingParams = processPagingParams(start, limit);
      int startInt = pagingParams.getFirst();
      int limitInt = pagingParams.getSecond();
      int counter = 0;
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      for (DescriptiveMetadata dm : metadata) {
        if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
          Binary binary = storage.getBinary(dm.getStoragePath());
          ZipEntryInfo info = new ZipEntryInfo(dm.getStoragePath().getName(), binary.getContent().createInputStream());
          zipEntries.add(info);
        } else {
          break;
        }
        counter++;
      }

      final String filename;
      final StreamingOutput stream;
      if (zipEntries.size() == 1) {
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            IOUtils.copy(zipEntries.get(0).getInputStream(), os);
          }
        };
        filename = zipEntries.get(0).getName();
      } else {
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            ZipTools.zip(zipEntries, os);
          }
        };
        filename = aipId + ".zip";
      }

      return new Pair<String, StreamingOutput>(filename, stream);
    } catch (IOException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException("");
    }
  }

  public static Pair<String, StreamingOutput> getAipDescritiveMetadata(String aipId, String metadataId)
    throws ModelServiceException, StorageServiceException, GenericException {

    ModelService model = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        IOUtils.copy(descriptiveMetadataBinary.getContent().createInputStream(), os);
      }
    };

    return new Pair<String, StreamingOutput>(descriptiveMetadataBinary.getStoragePath().getName(), stream);
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
        throw new GenericException("Error moving in hierarchy: " + e.getMessage());
      }

    }

  }

  public static SimpleDescriptionObject createNewItem(RodaUser user, String aipId, String parentAipId)
    throws GenericException {
    try {
      StorageService storage = RodaCoreFactory.getStorageService();
      ModelService model = RodaCoreFactory.getModelService();
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);
      Map<String, Set<String>> itemMetadata = new HashMap<String, Set<String>>();
      if (parentAipId != null) {
        itemMetadata.put(RodaConstants.STORAGE_META_PARENT_ID, new HashSet<String>(Arrays.asList(parentAipId)));
      }
      storage.createContainer(aipPath, itemMetadata);
      model.updateAIP(aipId, storage, aipPath);
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (StorageServiceException | ModelServiceException | IndexServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }

    }
  }

  public static SimpleDescriptionObject addNewMetadataFile(String aipId, InputStream metadataStream,
    String descriptiveMetadataId) throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      Path p = Files.createTempFile("preservation", ".tmp");
      Files.copy(metadataStream, p, StandardCopyOption.REPLACE_EXISTING);
      Binary binary = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
      model.createDescriptiveMetadata(aipId, descriptiveMetadataId, binary, descriptiveMetadataId);
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (StorageServiceException | ModelServiceException | IndexServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }

    } catch (IOException e) {
      throw new GenericException("Internal error: " + e.getMessage());
    }
  }

  public static SimpleDescriptionObject editMetadataFile(String aipId, InputStream metadataStream,
    String descriptiveMetadataId) throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      Path p = Files.createTempFile("preservation", ".tmp");
      Files.copy(metadataStream, p, StandardCopyOption.REPLACE_EXISTING);
      Binary binary = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
      model.updateDescriptiveMetadata(aipId, descriptiveMetadataId, binary, descriptiveMetadataId);
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (StorageServiceException | ModelServiceException | IndexServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    } catch (IOException e) {
      throw new GenericException("Internal error: " + e.getMessage());
    }
  }

  public static SimpleDescriptionObject removeMetadataFile(String aipId, String descriptiveMetadataId)
    throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deleteDescriptiveMetadata(aipId, descriptiveMetadataId);
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (ModelServiceException | IndexServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }
  }

  public static DescriptiveMetadata retrieveMetadataFile(String aipId, String descriptiveMetadataId)
    throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      return model.retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }
  }

}
