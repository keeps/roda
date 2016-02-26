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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.descriptionLevels.DescriptionLevel;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.IdUtils.LinkingObjectType;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.disseminators.common.tools.ZipEntryInfo;
import org.roda.disseminators.common.tools.ZipTools;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BinaryVersionBundle;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.DescriptiveMetadataViewBundle;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class BrowserHelper {
  private static final int BUNDLE_MAX_REPRESENTATION_COUNT = 10;
  private static final int BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT = 1;

  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);

  protected static BrowseItemBundle getItemBundle(String aipId, Locale locale)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    BrowseItemBundle itemBundle = new BrowseItemBundle();

    // set aip
    IndexedAIP aip = retrieve(IndexedAIP.class, aipId);
    itemBundle.setAIP(aip);

    // set aip ancestors
    try {
      itemBundle.setAIPAncestors(getAncestors(aip));
    } catch (NotFoundException e) {
      LOGGER.warn("Found an item with invalid ancestors: " + aipId, e);
    }

    // set descriptive metadata
    try {
      List<DescriptiveMetadataViewBundle> descriptiveMetadataList = getDescriptiveMetadataBundles(aipId, locale);
      itemBundle.setDescriptiveMetadata(descriptiveMetadataList);
    } catch (NotFoundException e) {
      // do nothing
    }

    // set representations
    // getting the last [BUNDLE_MAX_REPRESENTATION_COUNT] representations
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.SRO_ORIGINAL, true));
    IndexResult<Representation> findRepresentations = findRepresentations(aipId, sorter,
      new Sublist(0, BUNDLE_MAX_REPRESENTATION_COUNT));
    List<Representation> representations = findRepresentations.getResults();

    // if there are more representations ensure one original is there
    if (findRepresentations.getTotalCount() > findRepresentations.getLimit()) {
      boolean hasOriginals = findRepresentations.getResults().stream().anyMatch(x -> x.isOriginal());
      if (!hasOriginals) {
        boolean onlyOriginals = true;
        IndexResult<Representation> findOriginalRepresentations = findRepresentations(aipId, onlyOriginals, sorter,
          new Sublist(0, BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT));
        representations.addAll(findOriginalRepresentations.getResults());
      }
    }

    itemBundle.setRepresentations(representations);

    return itemBundle;
  }

  private static List<DescriptiveMetadataViewBundle> getDescriptiveMetadataBundles(String aipId, final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    ModelService model = RodaCoreFactory.getModelService();
    List<DescriptiveMetadata> listDescriptiveMetadataBinaries = model.retrieveAIP(aipId).getDescriptiveMetadata();

    List<DescriptiveMetadataViewBundle> descriptiveMetadataList = new ArrayList<DescriptiveMetadataViewBundle>();

    for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
      DescriptiveMetadataViewBundle bundle = getDescriptiveMetadataBundle(aipId, descriptiveMetadata, locale);

      descriptiveMetadataList.add(bundle);

    }

    return descriptiveMetadataList;
  }

  private static DescriptiveMetadataViewBundle getDescriptiveMetadataBundle(String aipId,
    DescriptiveMetadata descriptiveMetadata, final Locale locale)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    DescriptiveMetadataViewBundle bundle = new DescriptiveMetadataViewBundle();
    bundle.setId(descriptiveMetadata.getId());
    if (descriptiveMetadata.getType() != null) {
      try {

        bundle.setLabel(messages.getTranslation(
          RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase()));

      } catch (MissingResourceException e) {
        bundle.setLabel(descriptiveMetadata.getId());
      }
    }
    bundle.setHasHistory(!Iterables.isEmpty(model.getStorage()
      .listBinaryVersions(ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadata.getId()))));
    return bundle;
  }

  private static DescriptiveMetadataViewBundle getDescriptiveMetadataBundle(String aipId, String descriptiveMetadataId,
    final Locale locale)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
    return getDescriptiveMetadataBundle(aipId, descriptiveMetadata, locale);
  }

  public static DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId,
    String descriptiveMetadataId)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataEditBundle ret;
    InputStream inputStream = null;
    try {
      DescriptiveMetadata metadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId,
        descriptiveMetadataId);
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
      inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream);
      ret = new DescriptiveMetadataEditBundle(descriptiveMetadataId, metadata.getType(), xml);
    } catch (IOException e) {
      throw new GenericException("Error getting descriptive metadata edit bundle: " + e.getMessage());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    return ret;
  }

  protected static List<IndexedAIP> getAncestors(IndexedAIP aip) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().getAncestors(aip);
  }

  protected static <T extends Serializable> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(returnClass, filter, sorter, sublist, facets);
  }

  protected static <T extends Serializable> Long count(Class<T> returnClass, Filter filter)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(returnClass, filter);
  }

  protected static <T extends Serializable> T retrieve(Class<T> returnClass, String id)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(returnClass, id);
  }

  private static IndexResult<Representation> findRepresentations(String aipId, Sorter sorter, Sublist sublist)
    throws GenericException, RequestNotValidException {
    return findRepresentations(aipId, false, sorter, sublist);
  }

  private static IndexResult<Representation> findRepresentations(String aipId, boolean onlyOriginals, Sorter sorter,
    Sublist sublist) throws GenericException, RequestNotValidException {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.SRO_AIP_ID, aipId));
    if (onlyOriginals) {
      filter.add(new SimpleFilterParameter(RodaConstants.SRO_ORIGINAL, Boolean.TRUE.toString()));
    }
    Facets facets = null;

    return RodaCoreFactory.getIndexService().find(Representation.class, filter, sorter, sublist, facets);

  }

  public static void validateGetAipRepresentationFileParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static void validateGetAipRepresentationParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static StreamResponse getAipRepresentation(String aipId, String representationId, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    try {
      ModelService model = RodaCoreFactory.getModelService();

      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      boolean recursive = true;
      CloseableIterable<org.roda.core.data.v2.ip.File> allFiles = model.listFilesUnder(aipId, representationId,
        recursive);
      for (org.roda.core.data.v2.ip.File file : allFiles) {
        addToZip(zipEntries, file);
      }
      IOUtils.closeQuietly(allFiles);

      return createZipStreamResponse(zipEntries, aipId + "_" + representationId);

    } catch (IOException e) {
      throw new GenericException("Error getting AIP representation", e);
    }

  }

  private static void addToZip(List<ZipEntryInfo> zipEntries, org.roda.core.data.v2.ip.File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    StorageService storage = RodaCoreFactory.getStorageService();

    if (!file.isDirectory()) {
      StoragePath filePath = ModelUtils.getFileStoragePath(file);
      Binary binary = storage.getBinary(filePath);
      ZipEntryInfo info = new ZipEntryInfo(filePath.getName(), binary.getContent());
      zipEntries.add(info);
    } else {
      // TODO add directory zip entry
    }
  }

  protected static void validateListAipDescriptiveMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static StreamResponse listAipDescriptiveMetadata(String aipId, String start, String limit)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    List<DescriptiveMetadata> metadata = null;

    ModelService model = RodaCoreFactory.getModelService();
    StorageService storage = RodaCoreFactory.getStorageService();
    metadata = model.retrieveAIP(aipId).getDescriptiveMetadata();
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();
    int counter = 0;
    List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
    for (DescriptiveMetadata dm : metadata) {
      if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aipId, dm.getId());
        Binary binary = storage.getBinary(storagePath);
        ZipEntryInfo info = new ZipEntryInfo(storagePath.getName(), binary.getContent());
        zipEntries.add(info);
      } else {
        break;
      }
      counter++;
    }

    return createZipStreamResponse(zipEntries, aipId);

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
    String language)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;
    StreamResponse ret = null;

    ModelService model = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary;

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
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, metadataId);
      mediaType = MediaType.TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
        descriptiveMetadata.getType(), ServerTools.parseLocale(language));
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
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }

    return ret;
  }

  public static StreamResponse getAipDescritiveMetadataVersion(String aipId, String metadataId, String versionId,
    String acceptFormat, String language)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;
    StreamResponse ret = null;

    ModelService model = RodaCoreFactory.getModelService();

    StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aipId, metadataId);
    BinaryVersion binaryVersion = model.getStorage().getBinaryVersion(storagePath, versionId);
    Binary binary = binaryVersion.getBinary();

    String fileName = binary.getStoragePath().getName();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      mediaType = MediaType.TEXT_XML;
      stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(binary.getContent().createInputStream(), os);
        }
      };
      ret = new StreamResponse(fileName, mediaType, stream);

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      filename = fileName + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, metadataId);
      mediaType = MediaType.TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(binary, descriptiveMetadata.getType(),
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
      throw new GenericException("Unsupported accept format: " + acceptFormat);
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
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    List<Representation> representations = null;
    CloseableIterable<PreservationMetadata> preservationFiles = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      StorageService storage = RodaCoreFactory.getStorageService();
      representations = model.retrieveAIP(aipId).getRepresentations();
      Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
      int startInt = pagingParams.getFirst();
      int limitInt = pagingParams.getSecond();
      int counter = 0;
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      boolean includeRepresentations = true;
      preservationFiles = model.listPreservationMetadata(aipId, includeRepresentations);
      for (PreservationMetadata preservationFile : preservationFiles) {
        if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
          StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);
          Binary binary = storage.getBinary(storagePath);
          if (preservationFile.getRepresentationId() != null) {
            ZipEntryInfo info = new ZipEntryInfo(
              preservationFile.getRepresentationId() + File.separator + storagePath.getName(), binary.getContent());
            zipEntries.add(info);
          }
        } else {
          break;
        }

        counter++;
      }

      return createZipStreamResponse(zipEntries, aipId);

    } finally {
      IOUtils.closeQuietly(preservationFiles);
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

  public static StreamResponse getAipRepresentationPreservationMetadata(String aipId, String representationId,
    String startAgent, String limitAgent, String startEvent, String limitEvent, String startFile, String limitFile,
    String acceptFormat, String language)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    StorageService storage = RodaCoreFactory.getStorageService();
    ModelService model = RodaCoreFactory.getModelService();
    StreamResponse response = null;

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      CloseableIterable<PreservationMetadata> preservationFiles = null;
      try {
        Pair<Integer, Integer> pagingParamsAgent = ApiUtils.processPagingParams(startAgent, limitAgent);
        int counterAgent = 0;
        Pair<Integer, Integer> pagingParamsEvent = ApiUtils.processPagingParams(startEvent, limitEvent);
        int counterEvent = 0;
        Pair<Integer, Integer> pagingParamsFile = ApiUtils.processPagingParams(startFile, limitFile);
        int counterFile = 0;
        List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
        preservationFiles = model.listPreservationMetadata(aipId, representationId);

        for (PreservationMetadata preservationFile : preservationFiles) {
          boolean add = false;

          if (preservationFile.getType().equals(PreservationMetadataType.AGENT)) {
            if (counterAgent >= pagingParamsAgent.getFirst()
              && (counterAgent <= pagingParamsAgent.getSecond() || pagingParamsAgent.getSecond() == -1)) {
              add = true;
            }
            counterAgent++;
          } else if (preservationFile.getType().equals(PreservationMetadataType.EVENT)) {
            if (counterEvent >= pagingParamsEvent.getFirst()
              && (counterEvent <= pagingParamsEvent.getSecond() || pagingParamsEvent.getSecond() == -1)) {
              add = true;
            }
            counterEvent++;
          } else if (preservationFile.getType().equals(PreservationMetadataType.OBJECT_FILE)) {
            if (counterFile >= pagingParamsFile.getFirst()
              && (counterFile <= pagingParamsFile.getSecond() || pagingParamsFile.getSecond() == -1)) {
              add = true;
            }
            counterFile++;
          }

          if (add) {
            StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);
            Binary binary = storage.getBinary(storagePath);
            ZipEntryInfo info = new ZipEntryInfo(storagePath.getName(), binary.getContent());
            zipEntries.add(info);
          }
        }

        response = createZipStreamResponse(zipEntries, aipId + "_" + representationId);

      } finally {
        IOUtils.closeQuietly(preservationFiles);
      }
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }

    return response;

  }

  public static StreamResponse getAipRepresentationPreservationMetadataFile(String aipId, String representationId,
    String fileId) throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    Binary binary = RodaCoreFactory.getModelService().retrievePreservationRepresentation(aipId, representationId);

    String filename = binary.getStoragePath().getName();
    StreamingOutput stream = new StreamingOutput() {

      public void write(OutputStream os) throws IOException, WebApplicationException {
        IOUtils.copy(binary.getContent().createInputStream(), os);
      }
    };

    return new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM, stream);
  }

  public static void createOrUpdateAipRepresentationPreservationMetadataFile(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, FormDataContentDisposition fileDetail,
    boolean create) throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    Path file = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("preservation", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);
      boolean notify = true;
      if (create) {
        model.createPreservationMetadata(PreservationMetadataType.OBJECT_FILE, aipId, representationId,
          fileDirectoryPath, fileId, payload, notify);
      } else {
        PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
        String id = IdUtils.getPreservationMetadataId(type, aipId, representationId, fileDirectoryPath, fileId);
        model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, payload, notify);
      }
    } catch (IOException e) {
      throw new GenericException("Error creating or updating AIP representation preservation metadata file", e);
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
    String fileId, String preservationId)
      throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    boolean notify = true;
    RodaCoreFactory.getModelService().deletePreservationMetadata(PreservationMetadataType.OBJECT_FILE, aipId,
      representationId, preservationId, notify);
  }

  public static IndexedAIP moveInHierarchy(String aipId, String parentId) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    StorageService storage = RodaCoreFactory.getStorageService();
    ModelService model = RodaCoreFactory.getModelService();
    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    // TODO update setting AIP parent
    // Map<String, Set<String>> metadata = storage.getMetadata(aipPath);

    if (StringUtils.isBlank(parentId)) {
      StoragePath parentPath = ModelUtils.getAIPStoragePath(parentId);
      storage.getDirectory(parentPath);

      // metadata.remove(RodaConstants.STORAGE_META_PARENT_ID);
    } else {
      // metadata.put(RodaConstants.STORAGE_META_PARENT_ID, new
      // HashSet<String>(Arrays.asList(parentId)));
    }

    // storage.updateMetadata(aipPath, metadata, true);
    model.updateAIP(aipId, storage, aipPath);

    return RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId);

  }

  public static AIP createAIP(String parentAipId) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    AIP aip = model.createAIP(parentAipId);
    return aip;
  }

  public static String removeAIP(String aipId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    String parentId = RodaCoreFactory.getModelService().retrieveAIP(aipId).getParentId();
    RodaCoreFactory.getModelService().deleteAIP(aipId);
    return parentId;
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, ContentPayload descriptiveMetadataPayload)
      throws GenericException, ValidationException, AuthorizationDeniedException, RequestNotValidException,
      AlreadyExistsException, NotFoundException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload, descriptiveMetadataType, false);

    return RodaCoreFactory.getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType);
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, ContentPayload descriptiveMetadataPayload, String message) throws GenericException,
      AuthorizationDeniedException, ValidationException, RequestNotValidException, NotFoundException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload, descriptiveMetadataType, false);

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, message);

  }

  public static void removeDescriptiveMetadataFile(String aipId, String descriptiveMetadataId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  public static DescriptiveMetadata retrieveMetadataFile(String aipId, String descriptiveMetadataId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  // FIXME allow to create a zip without files/directories???
  private static StreamResponse createZipStreamResponse(List<ZipEntryInfo> zipEntries, String zipName) {
    final String filename;
    final StreamingOutput stream;
    if (zipEntries.size() == 1) {
      stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          InputStream inputStream = zipEntries.get(0).getPayload().createInputStream();
          IOUtils.copy(inputStream, os);
          IOUtils.closeQuietly(inputStream);
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

  public static void removeRepresentation(String aipId, String representationId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRepresentation(aipId, representationId);
  }

  public static void removeRepresentationFile(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteFile(aipId, representationId, directoryPath, fileId, true);
  }

  public static StreamResponse getAipRepresentationFile(String aipId, String representationId, String fileUuid,
    String acceptFormat)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;

    IndexedFile indexedFile = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);

    StorageService storage = RodaCoreFactory.getStorageService();
    Binary representationFileBinary = storage
      .getBinary(ModelUtils.getFileStoragePath(aipId, representationId, indexedFile.getPath(), indexedFile.getId()));
    filename = representationFileBinary.getStoragePath().getName();
    mediaType = MediaType.WILDCARD;
    stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        IOUtils.copy(representationFileBinary.getContent().createInputStream(), os);
      }
    };

    return new StreamResponse(filename, mediaType, stream);
  }

  public static void createOrUpdateAipDescriptiveMetadataFile(String aipId, String metadataId, String metadataType,
    String updateMessage, InputStream is, FormDataContentDisposition fileDetail, boolean create)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
      AlreadyExistsException, ValidationException {
    Path file = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      if (create) {
        model.createDescriptiveMetadata(aipId, metadataId, payload, metadataType);
      } else {
        model.updateDescriptiveMetadata(aipId, metadataId, payload, metadataType, updateMessage);
      }
    } catch (IOException e) {
      throw new GenericException("Error creating or updating AIP descriptive metadata file", e);
    } finally {
      FSUtils.deletePathQuietly(file);
    }

  }

  // public static IndexResult<TransferredResource>
  // findTransferredResources(Filter filter, Sorter sorter, Sublist sublist,
  // Facets facets) throws GenericException, RequestNotValidException {
  // return RodaCoreFactory.getIndexService().find(TransferredResource.class,
  // filter, sorter, sublist, facets);
  // }
  //
  // public static TransferredResource retrieveTransferredResource(String
  // transferredResourceId)
  // throws GenericException, NotFoundException {
  // return
  // RodaCoreFactory.getIndexService().retrieve(TransferredResource.class,
  // transferredResourceId);
  // }

  public static String createTransferredResourcesFolder(String parent, String folderName, boolean forceCommit)
    throws GenericException {
    try {
      String id = RodaCoreFactory.getFolderMonitor().createFolder(parent, folderName);
      if (forceCommit) {
        RodaCoreFactory.getFolderMonitor().commit();
      }
      return id;
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error creating transferred resource folder", e);
      throw new GenericException("Error creating transferred resource folder: " + e.getMessage());
    }
  }

  public static void removeTransferredResources(List<String> ids, boolean forceCommit)
    throws GenericException, NotFoundException {
    RodaCoreFactory.getFolderMonitor().removeSync(ids, forceCommit);
  }

  public static void createTransferredResourceFile(String path, String fileName, InputStream inputStream,
    boolean forceCommit) throws GenericException, AlreadyExistsException {
    try {
      LOGGER.debug("createTransferredResourceFile(path=" + path + ",name=" + fileName + ")");
      RodaCoreFactory.getFolderMonitor().createFile(path, fileName, inputStream);
      if (forceCommit) {
        RodaCoreFactory.getFolderMonitor().commit();
      }
    } catch (FileAlreadyExistsException e) {
      throw new AlreadyExistsException("Error creating transferred resource file", e);
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error creating transferred resource", e);
      throw new GenericException("Error creating transferred resource file: " + e.getMessage());
    }

  }

  // TODO Limit access to SDO accessible by user
  public static StreamResponse getClassificationPlan(String type, RodaUser user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ObjectNode root = mapper.createObjectNode();

      ArrayNode array = mapper.createArrayNode();
      List<DescriptionLevel> descriptionLevels = RodaCoreFactory.getDescriptionLevelManager()
        .getAllButRepresentationsDescriptionLevels();
      List<String> descriptionsLevels = descriptionLevels.stream().map(d -> d.getLevel()).collect(Collectors.toList());

      Filter allButRepresentationsFilter = new Filter(
        new OneOfManyFilterParameter(RodaConstants.AIP_LEVEL, descriptionsLevels));

      IndexService index = RodaCoreFactory.getIndexService();
      long collectionsCount = index.count(IndexedAIP.class, allButRepresentationsFilter);
      for (int i = 0; i < collectionsCount; i += 100) {
        IndexResult<IndexedAIP> collections = index.find(IndexedAIP.class, allButRepresentationsFilter, null,
          new Sublist(i, 100));
        for (IndexedAIP aip : collections.getResults()) {
          array.add(JsonUtils.aipToJSON(aip));
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
    } catch (IOException e) {
      throw new GenericException("Error creating classification plan: " + e.getMessage());
    }

  }

  public static boolean isTransferFullyInitialized() {
    return RodaCoreFactory.getFolderMonitor().isFullyInitialized();
  }

  // public static IndexResult<IndexedFile> findFiles(Filter filter, Sorter
  // sorter, Sublist sublist, Facets facets)
  // throws GenericException, RequestNotValidException {
  // return RodaCoreFactory.getIndexService().find(IndexedFile.class, filter,
  // sorter, sublist, facets);
  // }
  //
  // public static IndexedFile retrieveFile(String aipId, String
  // representationId, List<String> fileDirectoryPath,
  // String fileId) throws NotFoundException, GenericException {
  // return RodaCoreFactory.getIndexService().retrieve(IndexedFile.class,
  // IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId));
  // }

  public static List<SupportedMetadataTypeBundle> getSupportedMetadata(RodaUser user, Locale locale)
    throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    String[] types = RodaCoreFactory.getRodaConfiguration().getString("ui.browser.metadata.descriptive.types")
      .split(", ?");

    List<SupportedMetadataTypeBundle> supportedMetadata = new ArrayList<>();

    if (types != null) {
      for (String type : types) {
        String label = messages.getTranslation(RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + type,
          type);
        String template = null;
        InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream("templates/" + type + ".xml");

        //
        Map<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("id", "");
        scopes.put("title", "");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        scopes.put("date", format.format(new Date()));
        scopes.put("user", user.getFullName());

        Writer writer = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(templateStream), type);
        mustache.execute(writer, scopes);
        template = writer.toString();
        IOUtils.closeQuietly(templateStream);

        SupportedMetadataTypeBundle b = new SupportedMetadataTypeBundle(type, label, template);
        supportedMetadata.add(b);
      }
    }
    return supportedMetadata;
  }

  public static StreamResponse getTransferredResource(final TransferredResource transferredResource)
    throws NotFoundException, RequestNotValidException, GenericException {

    StreamingOutput streamingOutput = new StreamingOutput() {

      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        InputStream retrieveFile = null;
        try {
          retrieveFile = RodaCoreFactory.getFolderMonitor().retrieveFile(transferredResource.getFullPath());
          IOUtils.copy(retrieveFile, os);
        } catch (NotFoundException | RequestNotValidException | GenericException e) {
          // do nothing
        } finally {
          IOUtils.closeQuietly(retrieveFile);
        }
      }
    };

    return new StreamResponse(transferredResource.getName(), MediaType.APPLICATION_OCTET_STREAM, streamingOutput);
  }

  public static PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws NotFoundException, GenericException {
    PreservationEventViewBundle eventBundle = new PreservationEventViewBundle();
    Map<String, IndexedFile> files = new HashMap<String, IndexedFile>();
    IndexedPreservationEvent ipe = RodaCoreFactory.getIndexService().retrieve(IndexedPreservationEvent.class, eventId);
    eventBundle.setEvent(ipe);
    if (ipe.getLinkingAgentIds() != null && !ipe.getLinkingAgentIds().isEmpty()) {
      List<IndexedPreservationAgent> agents = new ArrayList<IndexedPreservationAgent>();
      for (LinkingIdentifier agentID : ipe.getLinkingAgentIds()) {
        try {
          IndexedPreservationAgent agent = RodaCoreFactory.getIndexService().retrieve(IndexedPreservationAgent.class,
            agentID.getValue());
          agents.add(agent);
        } catch (NotFoundException | GenericException e) {
          LOGGER.error("Error getting agent " + agentID + ": " + e.getMessage());
        }
      }
      eventBundle.setAgents(agents);
    }
    if ((ipe.getSourcesObjectIds() != null && !ipe.getSourcesObjectIds().isEmpty())
      || (ipe.getOutcomeObjectIds() != null && !ipe.getOutcomeObjectIds().isEmpty())) {

      if ((ipe.getSourcesObjectIds() != null && !ipe.getSourcesObjectIds().isEmpty())) {
        processLinkingIdentifiers(files, ipe.getSourcesObjectIds());
      }

      if ((ipe.getOutcomeObjectIds() != null && !ipe.getOutcomeObjectIds().isEmpty())) {
        processLinkingIdentifiers(files, ipe.getOutcomeObjectIds());
      }
      eventBundle.setFiles(files);
    }

    return eventBundle;
  }

  private static Map<String, IndexedFile> processLinkingIdentifiers(Map<String, IndexedFile> files,
    List<LinkingIdentifier> linkingIdentifiers) throws NotFoundException, GenericException {
    for (LinkingIdentifier identifier : linkingIdentifiers) {
      LinkingObjectType linkingType = IdUtils.getLinkingIdentifierType(identifier.getValue());

      if (linkingType != LinkingObjectType.TRANSFERRED_RESOURCE) {
        String[] tokens = IdUtils.splitLinkingId(IdUtils.getLinkingObjectPath(identifier.getValue()));
        if (tokens.length == 1) {
          // AIP
        } else if (tokens.length == 2) {
          // REPRESENTATION
        } else {
          // FILE
          IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class,
            IdUtils.getFileIdFromLinkingId(identifier.getValue()));
          files.put(identifier.getValue(), file);
        }
      }
    }
    return files;
  }

  public static CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId,
    String descriptiveMetadataId)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    return RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);

  }

  public static DescriptiveMetadataVersionsBundle getDescriptiveMetadataVersionsBundle(String aipId, String metadataId,
    Locale locale) throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataVersionsBundle bundle = new DescriptiveMetadataVersionsBundle();

    IndexedAIP aip = retrieve(IndexedAIP.class, aipId);
    DescriptiveMetadataViewBundle descriptiveMetadataBundle = getDescriptiveMetadataBundle(aipId, metadataId, locale);

    List<BinaryVersionBundle> versionBundles = new ArrayList<>();

    for (BinaryVersion v : listDescriptiveMetadataVersions(aipId, metadataId)) {
      versionBundles.add(new BinaryVersionBundle(v.getId(), v.getMessage(), v.getCreatedDate()));
    }

    bundle.setAip(aip);
    bundle.setDescriptiveMetadata(descriptiveMetadataBundle);
    bundle.setVersions(versionBundles);
    return bundle;
  }

  public static void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    String message) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId,
      message);
  }

  public static void removeDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }
}
