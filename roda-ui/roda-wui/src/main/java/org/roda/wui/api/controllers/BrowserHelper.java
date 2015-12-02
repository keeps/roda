/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.ValidationUtils;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.AuthorizationDeniedException;
import org.roda.core.data.common.Pair;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationState;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.model.AIP;
import org.roda.core.model.DescriptiveMetadata;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.PreservationMetadata;
import org.roda.core.model.ValidationException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.disseminators.common.tools.ZipEntryInfo;
import org.roda.disseminators.common.tools.ZipTools;
import org.roda.wui.api.exceptions.ApiException;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataViewBundle;
import org.roda.wui.client.browse.PreservationMetadataBundle;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.client.GenericException;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BrowserHelper {
  private static final int BUNDLE_MAX_REPRESENTATION_COUNT = 2;
  private static final int BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT = 1;
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);

  protected static BrowseItemBundle getItemBundle(String aipId, Locale locale)
    throws GenericException, NotFoundException {
    BrowseItemBundle itemBundle = new BrowseItemBundle();
    try {
      // set sdo
      SimpleDescriptionObject sdo = getSimpleDescriptionObject(aipId);
      itemBundle.setSdo(sdo);

      // set sdo ancestors
      itemBundle.setSdoAncestors(getAncestors(sdo));

      // set descriptive metadata
      List<DescriptiveMetadataViewBundle> descriptiveMetadataList = getDescriptiveMetadataBundles(aipId, locale);
      itemBundle.setDescriptiveMetadata(descriptiveMetadataList);

      // set preservation metadata
      PreservationMetadataBundle preservationMetadata = getPreservationMetadataBundle(aipId);
      itemBundle.setPreservationMetadata(preservationMetadata);

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

    } catch (StorageServiceException | ModelServiceException e) {
      LOGGER.error("Error getting item bundle", e);
      throw new GenericException("Error getting item bundle " + e.getMessage());
    }

    return itemBundle;
  }

  private static List<DescriptiveMetadataViewBundle> getDescriptiveMetadataBundles(String aipId, final Locale locale)
    throws ModelServiceException, StorageServiceException {
    ClosableIterable<DescriptiveMetadata> listDescriptiveMetadataBinaries = RodaCoreFactory.getModelService()
      .listDescriptiveMetadataBinaries(aipId);

    List<DescriptiveMetadataViewBundle> descriptiveMetadataList = new ArrayList<DescriptiveMetadataViewBundle>();
    try {
      Messages messages = RodaCoreFactory.getI18NMessages(locale);
      for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
        DescriptiveMetadataViewBundle bundle = new DescriptiveMetadataViewBundle();
        bundle.setId(descriptiveMetadata.getId());
        try {
          bundle.setLabel(messages
            .getTranslation(RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + descriptiveMetadata.getId()));
        } catch (MissingResourceException e) {
          bundle.setLabel(descriptiveMetadata.getId());
        }
        descriptiveMetadataList.add(bundle);
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

  private static PreservationMetadataBundle getPreservationMetadataBundle(String aipId)
    throws ModelServiceException, StorageServiceException {
    ModelService model = RodaCoreFactory.getModelService();
    StorageService storage = RodaCoreFactory.getStorageService();
    return HTMLUtils.getPreservationMetadataBundle(aipId, model, storage);
  }

  public static DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId,
    String descriptiveMetadataId) throws GenericException {
    DescriptiveMetadataEditBundle ret;
    InputStream inputStream = null;
    try {
      DescriptiveMetadata metadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId,
        descriptiveMetadataId);
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
      inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream);
      ret = new DescriptiveMetadataEditBundle(descriptiveMetadataId, metadata.getType(), xml);
    } catch (ModelServiceException | IOException e) {
      throw new GenericException("Error getting descriptive metadata edit bundle: " + e.getMessage());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          LOGGER.warn("Error closing input stream", e);
        }
      }
    }

    return ret;
  }

  protected static List<SimpleDescriptionObject> getAncestors(SimpleDescriptionObject sdo)
    throws GenericException, NotFoundException {
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

  public static void validateGetAipRepresentationFileParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static SimpleDescriptionObject getSimpleDescriptionObject(String aipId)
    throws GenericException, NotFoundException {
    try {
      return RodaCoreFactory.getIndexService().retrieve(SimpleDescriptionObject.class, aipId);
    } catch (IndexServiceException e) {
      if (e.getCode() == IndexServiceException.NOT_FOUND) {
        throw new NotFoundException("Could not find simple description object: " + aipId);
      }
      LOGGER.error("Error getting SDO", e);
      throw new GenericException("Error getting SDO: " + e.getMessage());
    }
  }

  protected static void validateGetAipRepresentationParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static StreamResponse getAipRepresentation(String aipId, String representationId, String acceptFormat)
    throws GenericException {
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

      return createZipStreamResponse(zipEntries, aipId + "_" + representationId);

    } catch (IOException | ModelServiceException | StorageServiceException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException(e.getMessage());
    }

  }

  protected static void validateListAipDescriptiveMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static StreamResponse listAipDescriptiveMetadata(String aipId, String start, String limit)
    throws GenericException {
    ClosableIterable<DescriptiveMetadata> metadata = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      metadata = model.listDescriptiveMetadataBinaries(aipId);
      Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
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

      return createZipStreamResponse(zipEntries, aipId);

    } catch (IOException | ModelServiceException | StorageServiceException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException(e.getMessage());
    } finally {
      try {
        if (metadata != null) {
          metadata.close();
        }
      } catch (IOException e) {
        LOGGER.error("Error while while freeing up resources", e);
      }
    }
  }

  protected static void validateGetAipDescritiveMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: " + Arrays
          .asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML));
    }
  }

  public static StreamResponse getAipDescritiveMetadata(String aipId, String metadataId, String acceptFormat,
    String language) throws GenericException, TransformerException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;
    StreamResponse ret = null;

    ModelService model = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary;
    try {
      descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);

      if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
        filename = descriptiveMetadataBinary.getStoragePath().getName();
        mediaType = MediaType.TEXT_XML;
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            IOUtils.copy(descriptiveMetadataBinary.getContent().createInputStream(), os);
          }
        };
        ret = new StreamResponse(filename, mediaType, stream);

      } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
        filename = descriptiveMetadataBinary.getStoragePath().getName() + ".html";
        mediaType = MediaType.TEXT_HTML;
        String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
          ServerTools.parseLocale(language));
        stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            PrintStream printStream = new PrintStream(os);
            printStream.print(htmlDescriptive);
            printStream.close();
          }
        };
        ret = new StreamResponse(filename, mediaType, stream);

      } else {
        new GenericException("Unsupported accept format: " + acceptFormat);
      }
    } catch (ModelServiceException e) {
      String message = e.getMessage();
      if (e.getCause() != null) {
        message += ": " + e.getCause().getMessage();
      }
      throw new GenericException(message);
    }

    return ret;
  }

  protected static void validateListAipPreservationMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }

  }

  public static StreamResponse aipsAipIdPreservationMetadataGet(String aipId, String start, String limit)
    throws GenericException {

    ClosableIterable<Representation> representations = null;
    ClosableIterable<PreservationMetadata> preservationFiles = null;

    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      representations = model.listRepresentations(aipId);
      Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
      int startInt = pagingParams.getFirst();
      int limitInt = pagingParams.getSecond();
      int counter = 0;
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      for (Representation r : representations) {
        preservationFiles = model.listPreservationMetadataBinaries(aipId, r.getId());
        for (PreservationMetadata preservationFile : preservationFiles) {
          if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
            Binary binary = storage.getBinary(preservationFile.getStoragePath());
            ZipEntryInfo info = new ZipEntryInfo(
              r.getId() + File.separator + preservationFile.getStoragePath().getName(),
              binary.getContent().createInputStream());
            zipEntries.add(info);
          } else {
            break;
          }

          counter++;
        }
      }

      return createZipStreamResponse(zipEntries, aipId);

    } catch (IOException | ModelServiceException | StorageServiceException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException(e.getMessage());
    } finally {
      if (representations != null) {
        try {
          representations.close();
        } catch (IOException e) {
          LOGGER.warn("Error closing resources, possible leak", e);
        }
      }
      if (preservationFiles != null) {
        try {
          preservationFiles.close();
        } catch (IOException e) {
          LOGGER.warn("Error closing resources, possible leak", e);
        }
      }
    }

  }

  protected static void validateGetAipRepresentationPreservationMetadataParams(String acceptFormat, String language)
    throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: " + Arrays
          .asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML));
    }

    // FIXME validate language? what exception should be thrown?
    if (!StringUtils.isNotBlank(language)) {
      throw new RequestNotValidException("Parameter '" + RodaConstants.API_QUERY_KEY_LANG + "' must have a value");
    }

  }

  // FIXME 100 lines of method
  // FIXME 100 lines of method
  // FIXME 100 lines of method
  // FIXME 100 lines of method
  // FIXME 100 lines of method
  public static StreamResponse getAipRepresentationPreservationMetadata(String aipId, String representationId,
    String startAgent, String limitAgent, String startEvent, String limitEvent, String startFile, String limitFile,
    String acceptFormat, String language) throws GenericException, TransformerException {

    StorageService storage = RodaCoreFactory.getStorageService();
    ModelService model = RodaCoreFactory.getModelService();
    StreamResponse response = null;

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      ClosableIterable<PreservationMetadata> preservationFiles = null;
      try {
        Pair<Integer, Integer> pagingParamsAgent = ApiUtils.processPagingParams(startAgent, limitAgent);
        int counterAgent = 0;
        Pair<Integer, Integer> pagingParamsEvent = ApiUtils.processPagingParams(startEvent, limitEvent);
        int counterEvent = 0;
        Pair<Integer, Integer> pagingParamsFile = ApiUtils.processPagingParams(startFile, limitFile);
        int counterFile = 0;
        List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
        preservationFiles = model.listPreservationMetadataBinaries(aipId, representationId);
        for (PreservationMetadata preservationFile : preservationFiles) {
          boolean add = false;

          if (preservationFile.getType().equalsIgnoreCase("agent")) {
            if (counterAgent >= pagingParamsAgent.getFirst()
              && (counterAgent <= pagingParamsAgent.getSecond() || pagingParamsAgent.getSecond() == -1)) {
              add = true;
            }
            counterAgent++;
          } else if (preservationFile.getType().equalsIgnoreCase("event")) {
            if (counterEvent >= pagingParamsEvent.getFirst()
              && (counterEvent <= pagingParamsEvent.getSecond() || pagingParamsEvent.getSecond() == -1)) {
              add = true;
            }
            counterEvent++;
          } else if (preservationFile.getType().equalsIgnoreCase("file")) {
            if (counterFile >= pagingParamsFile.getFirst()
              && (counterFile <= pagingParamsFile.getSecond() || pagingParamsFile.getSecond() == -1)) {
              add = true;
            }
            counterFile++;
          }

          if (add) {
            Binary binary = storage.getBinary(preservationFile.getStoragePath());
            ZipEntryInfo info = new ZipEntryInfo(preservationFile.getStoragePath().getName(),
              binary.getContent().createInputStream());
            zipEntries.add(info);
          }
        }
        response = createZipStreamResponse(zipEntries, aipId + "_" + representationId);

      } catch (IOException | ModelServiceException | StorageServiceException e) {
        // FIXME see what better exception should be thrown
        throw new GenericException("");
      } finally {
        if (preservationFiles != null) {
          try {
            preservationFiles.close();
          } catch (IOException e) {
            // FIXME see what better exception should be thrown
            throw new GenericException("");
          }
        }
      }
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      try {
        String filename = aipId + "_" + representationId + ".html";

        String html = HTMLUtils.getRepresentationPreservationMetadataHtml(
          ModelUtils.getPreservationPath(aipId, representationId), storage, ServerTools.parseLocale(language),
          ApiUtils.processPagingParams(startAgent, limitAgent), ApiUtils.processPagingParams(startEvent, limitEvent),
          ApiUtils.processPagingParams(startFile, limitFile));

        StreamingOutput stream = new StreamingOutput() {
          @Override
          public void write(OutputStream os) throws IOException, WebApplicationException {
            PrintStream printStream = new PrintStream(os);
            printStream.print(html);
            printStream.close();
          }
        };
        response = new StreamResponse(filename, MediaType.TEXT_HTML, stream);

      } catch (ModelServiceException | StorageServiceException e) {
        // FIXME
        throw new GenericException(e.getMessage());
      }
    }

    return response;

  }

  public static StreamResponse getAipRepresentationPreservationMetadataFile(String aipId, String representationId,
    String fileId) throws NotFoundException, GenericException {

    StorageService storage = RodaCoreFactory.getStorageService();
    Binary binary;
    try {
      binary = storage.getBinary(ModelUtils.getPreservationFilePath(aipId, representationId, fileId));

      String filename = binary.getStoragePath().getName();
      StreamingOutput stream = new StreamingOutput() {

        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(binary.getContent().createInputStream(), os);
        }
      };

      return new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM, stream);
    } catch (StorageServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new NotFoundException();
      } else {
        throw new GenericException();
      }
    }
  }

  public static void createOrUpdateAipRepresentationPreservationMetadataFile(String aipId, String representationId,
    InputStream is, FormDataContentDisposition fileDetail, boolean create) throws GenericException {
    Path file = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("preservation", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      Binary resource = (Binary) FSUtils.convertPathToResource(file.getParent(), file);
      if (create) {
        model.createPreservationMetadata(aipId, representationId, fileDetail.getFileName(), resource);
      } else {
        model.updatePreservationMetadata(aipId, representationId, fileDetail.getFileName(), resource, false);
      }
    } catch (IOException | StorageServiceException | ModelServiceException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException(e.getMessage());
    } finally {
      if (file != null && Files.exists(file)) {
        try {
          Files.delete(file);
        } catch (IOException e) {
          LOGGER.warn("Error while deleting temporary file", e);
        }
      }
    }
  }

  public static void aipsAipIdPreservationMetadataRepresentationIdFileIdDelete(String aipId, String representationId,
    String fileId) throws NotFoundException, GenericException {
    ModelService model = RodaCoreFactory.getModelService();
    try {
      model.deletePreservationMetadata(aipId, representationId, fileId);
    } catch (ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new NotFoundException(e.getMessage());
      } else {
        throw new GenericException(e.getMessage());
      }
    }
  }

  public static SimpleDescriptionObject moveInHierarchy(String aipId, String parentId)
    throws GenericException, NotFoundException {
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
        throw new NotFoundException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error moving in hierarchy: " + e.getMessage());
      }

    }

  }

  public static AIP createAIP(String parentAipId) throws GenericException, AuthorizationDeniedException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      // IndexService index = RodaCoreFactory.getIndexService();

      Map<String, Set<String>> metadata = new HashMap<String, Set<String>>();
      if (parentAipId != null) {
        metadata.put(RodaConstants.STORAGE_META_PARENT_ID, new HashSet<String>(Arrays.asList(parentAipId)));
      }

      AIP aip = model.createAIP(metadata);
      return aip;
      // return index.retrieve(SimpleDescriptionObject.class,
      // aip.getId());
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new AuthorizationDeniedException("You do not have permission to create AIPS");
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }
  }

  public static void removeAIP(String aipId) throws AuthorizationDeniedException, GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deleteAIP(aipId);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new AuthorizationDeniedException("You do not have permission to create AIPS");
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, Binary descriptiveMetadataIdBinary)
      throws GenericException, ValidationException, AuthorizationDeniedException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataIdBinary, descriptiveMetadataId, false);

    DescriptiveMetadata ret;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      ret = model.createDescriptiveMetadata(aipId, descriptiveMetadataId, descriptiveMetadataIdBinary,
        descriptiveMetadataType);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new AuthorizationDeniedException("You do not have permission to access AIP: " + aipId);
      } else {
        LOGGER.error("Error creating new item", e);
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }

    return ret;
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, Binary descriptiveMetadataIdBinary)
      throws GenericException, AuthorizationDeniedException, ValidationException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataIdBinary, descriptiveMetadataId, false);

    try {
      ModelService model = RodaCoreFactory.getModelService();
      return model.updateDescriptiveMetadata(aipId, descriptiveMetadataId, descriptiveMetadataIdBinary,
        descriptiveMetadataType);
    } catch (ModelServiceException e) {
      if (e.getCode() == ModelServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == ModelServiceException.FORBIDDEN) {
        throw new AuthorizationDeniedException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error creating new item: " + e.getMessage());
      }
    }

  }

  public static void removeDescriptiveMetadataFile(String aipId, String descriptiveMetadataId) throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deleteDescriptiveMetadata(aipId, descriptiveMetadataId);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error removing descriptive metadata: " + e.getMessage());
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
        throw new GenericException("Error retrieving metadata file: " + e.getMessage());
      }
    }
  }

  private static StreamResponse createZipStreamResponse(List<ZipEntryInfo> zipEntries, String zipName) {
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
      filename = zipName + ".zip";
    }

    return new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM, stream);

  }

  public static void removeRepresentation(String aipId, String representationId) throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deleteRepresentation(aipId, representationId);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException(
          "Error removing representation " + representationId + " from AIP " + aipId + ": " + e.getMessage());
      }
    }

  }

  public static void removeRepresentationFile(String aipId, String representationId, String fileId)
    throws GenericException {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      model.deleteFile(aipId, representationId, fileId);
    } catch (ModelServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("AIP not found: " + aipId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error removing file " + fileId + " from representation " + representationId
          + " of AIP " + aipId + ": " + e.getMessage());
      }
    }

  }

  public static StreamResponse getAipRepresentationFile(String aipId, String representationId, String fileId,
    String acceptFormat) throws GenericException {
    try {
      final String filename;
      final String mediaType;
      final StreamingOutput stream;

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage
        .getBinary(ModelUtils.getRepresentationFilePath(aipId, representationId, fileId));
      filename = representationFileBinary.getStoragePath().getName();
      mediaType = MediaType.WILDCARD;
      stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(representationFileBinary.getContent().createInputStream(), os);
        }
      };

      return new StreamResponse(filename, mediaType, stream);

    } catch (StorageServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new GenericException("File not found: " + aipId + "/" + representationId + "/" + fileId);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new GenericException("You do not have permission to access AIP: " + aipId);
      } else {
        throw new GenericException("Error getting representation file " + fileId + " from representation "
          + representationId + " of AIP " + aipId + ": " + e.getMessage());
      }
    }
  }

  public static void createOrUpdateAipDescriptiveMetadataFile(String aipId, String metadataId, String metadataType,
    InputStream is, FormDataContentDisposition fileDetail, boolean create) throws GenericException {
    Path file = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      Binary resource = (Binary) FSUtils.convertPathToResource(file.getParent(), file);
      if (create) {
        model.createDescriptiveMetadata(aipId, metadataId, resource, metadataType);
      } else {
        model.updateDescriptiveMetadata(aipId, metadataId, resource, metadataType);
      }
    } catch (IOException | StorageServiceException | ModelServiceException e) {
      // FIXME see what better exception should be thrown
      throw new GenericException(e.getMessage());
    } finally {
      if (file != null && Files.exists(file)) {
        try {
          Files.delete(file);
        } catch (IOException e) {
          LOGGER.warn("Error while deleting temporary file", e);
        }
      }
    }

  }

  public static IndexResult<TransferredResource> findTransferredResources(Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException {

    IndexResult<TransferredResource> ret;
    try {
      ret = RodaCoreFactory.getIndexService().find(TransferredResource.class, filter, sorter, sublist, facets);
      LOGGER.debug(String.format("findTransferredResources(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, ret));
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting transferred resources", e);
      throw new GenericException("Error getting transferred resources: " + e.getMessage());
    }

    return ret;

  }

  public static TransferredResource retrieveTransferredResource(String transferredResourceId)
    throws GenericException, NotFoundException {
    try {
      return RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, transferredResourceId);
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting transferred resource", e);
      throw new GenericException("Error getting transferred resource: " + e.getMessage());
    }
  }

  public static String createTransferredResourcesFolder(String parent, String folderName) throws GenericException {
    try {
      return RodaCoreFactory.getFolderMonitor().createFolder(Paths.get(parent), folderName);
    } catch (IOException e) {
      LOGGER.error("Error creating transferred resource folder", e);
      throw new GenericException("Error creating transferred resource folder: " + e.getMessage());
    }
  }

  public static void removeTransferredResources(List<String> ids) throws GenericException, NotFoundException {
    try {
      RodaCoreFactory.getFolderMonitor().removeSync(ids);
    } catch (IOException e) {
      LOGGER.error("Error removing transferred resource", e);
      throw new GenericException("Error removing transferred resource: " + e.getMessage());
    }
  }

  public static void createTransferredResourceFile(String path, String fileName, InputStream inputStream)
    throws GenericException, FileAlreadyExistsException {
    try {
      LOGGER.debug("createTransferredResourceFile(path=" + path + ",name=" + fileName + ")");
      RodaCoreFactory.getFolderMonitor().createFile(path, fileName, inputStream);
    } catch (FileAlreadyExistsException e) {
      throw e;
    } catch (IOException e) {
      LOGGER.error("Error removing transferred resource", e);
      throw new GenericException("Error creating transferred resource file: " + e.getMessage());
    }

  }

  // TODO Limit access to SDO accessible by user
  // TODO improve descriptionlevelmanager initialization
  public static StreamResponse getClassificationPlan(String type, RodaUser user) throws GenericException {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ObjectNode root = mapper.createObjectNode();

      ArrayNode array = mapper.createArrayNode();
      List<DescriptionLevel> descriptionLevels = RodaCoreFactory.getDescriptionLevelManager()
        .getAllButRepresentationsDescriptionLevels();
      List<String> descriptionsLevels = (List<String>) CollectionUtils.collect(descriptionLevels, new Transformer() {
        @Override
        public Object transform(Object dl) {
          return ((DescriptionLevel) dl).getLevel();
        }
      });

      Filter allButRepresentationsFilter = new Filter(
        new OneOfManyFilterParameter(RodaConstants.SDO_LEVEL, descriptionsLevels));

      IndexService index = RodaCoreFactory.getIndexService();
      long collectionsCount = index.count(SimpleDescriptionObject.class, allButRepresentationsFilter);
      for (int i = 0; i < collectionsCount; i += 100) {
        IndexResult<SimpleDescriptionObject> collections = index.find(SimpleDescriptionObject.class,
          allButRepresentationsFilter, null, new Sublist(i, 100));
        for (SimpleDescriptionObject sdo : collections.getResults()) {
          array.add(ModelUtils.sdoToJSON(sdo));
        }
      }
      root.set("dos", array);
      StringWriter sw = new StringWriter();
      mapper.writeValue(sw, root);
      StreamingOutput stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.write(sw.toString().getBytes("UTF-8"), os);
        }
      };
      return new StreamResponse("plan.json", MediaType.APPLICATION_JSON, stream);
    } catch (IndexServiceException | IOException | ModelServiceException e) {
      throw new GenericException("Error creating classification plan: " + e.getMessage());
    }

  }

  public static boolean isTransferFullyInitialized() {
    return RodaCoreFactory.getFolderMonitor().isFullyInitialized();
  }

  public static IndexResult<SimpleFile> findFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException {
    IndexResult<SimpleFile> files;
    try {
      files = RodaCoreFactory.getIndexService().find(SimpleFile.class, filter, sorter, sublist, facets);
      LOGGER.debug(String.format("findFiles(%1$s,%2$s,%3$s)=%4$s", filter, sorter, sublist, files));
    } catch (IndexServiceException e) {
      LOGGER.error("Error getting collections", e);
      throw new GenericException("Error getting collections " + e.getMessage());
    }

    return files;
  }

}
