/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.common.tools.ZipTools;
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
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.LinkingObjectUtils.LinkingObjectType;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.JsonUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.StreamResponse;
import org.roda.wui.client.browse.BinaryVersionBundle;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.DescriptiveMetadataViewBundle;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.RiskVersionsBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
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

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class BrowserHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);

  private static final int BUNDLE_MAX_REPRESENTATION_COUNT = 10;
  private static final int BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT = 1;

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
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.REPRESENTATION_ORIGINAL, true));
    IndexResult<IndexedRepresentation> findRepresentations = findRepresentations(aipId, sorter,
      new Sublist(0, BUNDLE_MAX_REPRESENTATION_COUNT));
    List<IndexedRepresentation> representations = findRepresentations.getResults();

    // if there are more representations ensure one original is there
    if (findRepresentations.getTotalCount() > findRepresentations.getLimit()) {
      boolean hasOriginals = findRepresentations.getResults().stream().anyMatch(x -> x.isOriginal());
      if (!hasOriginals) {
        boolean onlyOriginals = true;
        IndexResult<IndexedRepresentation> findOriginalRepresentations = findRepresentations(aipId, onlyOriginals,
          sorter, new Sublist(0, BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT));
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
        if (descriptiveMetadata.getVersion() != null) {
          bundle.setLabel(messages.getTranslation(RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX
            + descriptiveMetadata.getType().toLowerCase() + "." + descriptiveMetadata.getVersion().toLowerCase()));
        } else {
          bundle.setLabel(messages.getTranslation(RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX
            + descriptiveMetadata.getType().toLowerCase()));
        }

      } catch (MissingResourceException e) {
        bundle.setLabel(descriptiveMetadata.getId());
      }
    }

    try {
      bundle.setHasHistory(!CloseableIterables.isEmpty(model.getStorage()
        .listBinaryVersions(ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadata.getId()))));
    } catch (Throwable t) {
      bundle.setHasHistory(false);
    }
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
      ret = new DescriptiveMetadataEditBundle(descriptiveMetadataId, metadata.getType(), metadata.getVersion(), xml);
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

  protected static <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, RodaUser user) throws GenericException, RequestNotValidException {
    boolean showInactive = true;
    return RodaCoreFactory.getIndexService().find(returnClass, filter, sorter, sublist, facets, user, showInactive);
  }

  protected static <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, RodaUser user)
    throws GenericException, RequestNotValidException {
    boolean showInactive = true;
    return RodaCoreFactory.getIndexService().count(returnClass, filter, user, showInactive);
  }

  protected static <T extends IsIndexed> T retrieve(Class<T> returnClass, String id)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(returnClass, id);
  }

  protected static <T extends IsIndexed> List<String> suggest(Class<T> returnClass, String field, String query)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().suggest(returnClass, field, query);
  }

  private static IndexResult<IndexedRepresentation> findRepresentations(String aipId, Sorter sorter, Sublist sublist)
    throws GenericException, RequestNotValidException {
    return findRepresentations(aipId, false, sorter, sublist);
  }

  private static IndexResult<IndexedRepresentation> findRepresentations(String aipId, boolean onlyOriginals,
    Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    if (onlyOriginals) {
      filter.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_ORIGINAL, Boolean.TRUE.toString()));
    }
    Facets facets = null;

    return RodaCoreFactory.getIndexService().find(IndexedRepresentation.class, filter, sorter, sublist, facets);

  }

  public static void validateGetAipRepresentationFileParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static void validateGetAipRepresentationParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static StreamResponse getAipRepresentation(String aipId, String representationId, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      ModelService model = RodaCoreFactory.getModelService();
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      boolean recursive = true;
      CloseableIterable<OptionalWithCause<org.roda.core.data.v2.ip.File>> allFiles = model.listFilesUnder(aipId,
        representationId, recursive);
      for (OptionalWithCause<org.roda.core.data.v2.ip.File> file : allFiles) {
        if (file.isPresent()) {
          ModelUtils.addToZip(zipEntries, file.get(), false);
        } else {
          LOGGER.error("Cannot get AIP representation file", file.getCause());
        }
      }
      IOUtils.closeQuietly(allFiles);

      return createZipStreamResponse(zipEntries, aipId + "_" + representationId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)) {
      ModelService model = RodaCoreFactory.getModelService();
      Representation rep = model.retrieveRepresentation(aipId, representationId);
      final StreamingOutput stream;
      String filename = rep.getId() + "." + RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON;
      String mediaType = MediaType.APPLICATION_JSON;
      stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.write(JsonUtils.getJsonFromObject(rep), os, "UTF-8");
        }
      };
      return new StreamResponse(filename, mediaType, stream);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
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
        descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));
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
        descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));
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

  // FIXME 20160406 hsilva: representation preservation metadata is not being
  // included in the response "package"
  public static StreamResponse aipsAipIdPreservationMetadataGet(String aipId, String start, String limit)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    List<Representation> representations = null;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = null;
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
      Map<String, ZipEntryInfo> agents = new HashMap<String, ZipEntryInfo>();
      for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
        if (oPreservationFile.isPresent()) {
          PreservationMetadata preservationFile = oPreservationFile.get();
          if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
            StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);

            Binary binary = storage.getBinary(storagePath);

            ZipEntryInfo info = new ZipEntryInfo(FSUtils.getStoragePathAsString(storagePath, true),
              binary.getContent());
            zipEntries.add(info);

            if (preservationFile.getType() == PreservationMetadataType.EVENT) {
              try {
                List<LinkingIdentifier> agentIDS = PremisV3Utils.extractAgentsFromEvent(binary);
                if (!agentIDS.isEmpty()) {
                  for (LinkingIdentifier li : agentIDS) {
                    String agentID = li.getValue();
                    if (!agents.containsKey(agentID)) {
                      StoragePath agentPath = ModelUtils.getPreservationMetadataStoragePath(agentID,
                        PreservationMetadataType.AGENT);
                      Binary agentBinary = storage.getBinary(agentPath);
                      info = new ZipEntryInfo(
                        FSUtils.getStoragePathAsString(DefaultStoragePath.parse(preservationFile.getAipId()), false,
                          agentPath, true),
                        agentBinary.getContent());
                      agents.put(agentID, info);
                    }
                  }
                }
              } catch (ValidationException | GenericException e) {

              }
            }
          } else {
            break;
          }

          counter++;
        } else {
          LOGGER.error("Cannot get AIP preservation metadata", oPreservationFile.getCause());
        }
      }
      if (agents.size() > 0) {
        for (Map.Entry<String, ZipEntryInfo> entry : agents.entrySet()) {
          zipEntries.add(entry.getValue());
        }
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
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = null;
      try {
        Pair<Integer, Integer> pagingParamsAgent = ApiUtils.processPagingParams(startAgent, limitAgent);
        int counterAgent = 0;
        Pair<Integer, Integer> pagingParamsEvent = ApiUtils.processPagingParams(startEvent, limitEvent);
        int counterEvent = 0;
        Pair<Integer, Integer> pagingParamsFile = ApiUtils.processPagingParams(startFile, limitFile);
        int counterFile = 0;
        List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
        preservationFiles = model.listPreservationMetadata(aipId, representationId);

        for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
          if (oPreservationFile.isPresent()) {
            PreservationMetadata preservationFile = oPreservationFile.get();
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
          } else {
            LOGGER.error("Cannot get AIP preservation metadata", oPreservationFile.getCause());
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

  public static AIP moveInHierarchy(String aipId, String parentId) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    ModelService model = RodaCoreFactory.getModelService();

    return model.moveAIP(aipId, parentId);
  }

  public static AIP createAIP(String parentAipId, String type, Permissions permissions) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    AIP aip = model.createAIP(parentAipId, type, permissions);
    return aip;
  }

  public static String removeAIP(SelectedItems selected, RodaUser user, boolean deleteOnlyRepresentations)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);

    String parentId = null;

    for (final String aipId : aipIds) {
      try {
        AIP aip = RodaCoreFactory.getModelService().retrieveAIP(aipId);
        parentId = aip.getParentId();
        RodaCoreFactory.getModelService().deleteAIP(aipId);

        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId));

        RodaCoreFactory.getIndexService().execute(IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

          @Override
          public void run(IndexedAIP item)
            throws GenericException, RequestNotValidException, AuthorizationDeniedException {
            try {
              UserUtility.checkObjectPermissions(user, item, PermissionType.DELETE);
              if (!deleteOnlyRepresentations) {
                RodaCoreFactory.getModelService().deleteAIP(item.getId());
              } else {
                for (Representation rep : aip.getRepresentations()) {
                  RodaCoreFactory.getModelService().deleteRepresentation(aipId, rep.getId());
                }
              }
            } catch (NotFoundException e) {
              // already deleted, ignore
            }
          }
        });

      } catch (NotFoundException e) {
        // already deleted
      }
    }

    RodaCoreFactory.getIndexService().commitAIPs();

    return parentId;
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, String descriptiveMetadataVersion, ContentPayload descriptiveMetadataPayload)
    throws GenericException, ValidationException, AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, NotFoundException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload, descriptiveMetadataType,
      descriptiveMetadataVersion, false);

    return RodaCoreFactory.getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion);
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, String descriptiveMetadataVersion, ContentPayload descriptiveMetadataPayload,
    String message) throws GenericException, AuthorizationDeniedException, ValidationException,
    RequestNotValidException, NotFoundException {

    ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload, descriptiveMetadataType,
      descriptiveMetadataVersion, false);

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, message);

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

  public static void removeRepresentationFile(String fileUUID)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUUID);
    RodaCoreFactory.getModelService().deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), true);
  }

  public static StreamResponse getAipRepresentationFile(String fileUuid, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    IndexedFile indexedFile = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);

    final String filename;
    final String mediaType;
    final StreamingOutput stream;

    StorageService storage = RodaCoreFactory.getStorageService();
    Binary representationFileBinary = storage.getBinary(ModelUtils.getFileStoragePath(indexedFile.getAipId(),
      indexedFile.getRepresentationId(), indexedFile.getPath(), indexedFile.getId()));
    filename = representationFileBinary.getStoragePath().getName();
    mediaType = MediaType.WILDCARD;
    stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        InputStream fileInputStream = null;
        try {
          fileInputStream = representationFileBinary.getContent().createInputStream();
          IOUtils.copy(fileInputStream, os);
        } finally {
          IOUtils.closeQuietly(fileInputStream);
        }
      }
    };

    return new StreamResponse(filename, mediaType, stream);
  }

  public static void createOrUpdateAipDescriptiveMetadataFile(String aipId, String metadataId, String metadataType,
    String metadataVersion, String updateMessage, InputStream is, FormDataContentDisposition fileDetail, boolean create)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    Path file = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      if (create) {
        model.createDescriptiveMetadata(aipId, metadataId, payload, metadataType, metadataVersion);
      } else {
        model.updateDescriptiveMetadata(aipId, metadataId, payload, metadataType, metadataVersion, updateMessage);
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

  public static String createTransferredResourcesFolder(String parentUUID, String folderName, boolean forceCommit)
    throws GenericException, RequestNotValidException, NotFoundException {
    String uuid = RodaCoreFactory.getTransferredResourcesScanner().createFolder(parentUUID, folderName);
    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }
    return uuid;
  }

  public static <T extends IsIndexed> List<String> consolidate(RodaUser user, Class<T> classToReturn,
    SelectedItems selected) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<String> ret;

    if (selected instanceof SelectedItemsList) {
      ret = ((SelectedItemsList) selected).getIds();
    } else if (selected instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) selected).getFilter();
      Long count = count(classToReturn, filter, user);
      IndexResult<T> find = find(classToReturn, filter, null, new Sublist(0, count.intValue()), null, user);
      ret = find.getResults().stream().map(i -> i.getUUID()).collect(Collectors.toList());
    } else {
      throw new RequestNotValidException("Class not supported: " + selected.getClass().getName());
    }

    return ret;
  }

  public static void removeTransferredResources(SelectedItems selected, RodaUser user)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    List<String> ids = consolidate(user, TransferredResource.class, selected);

    // check permissions
    UserUtility.checkTransferredResourceAccess(user, ids);

    RodaCoreFactory.getTransferredResourcesScanner().removeTransferredResource(ids);
  }

  public static void createTransferredResourceFile(String parentUUID, String fileName, InputStream inputStream,
    boolean forceCommit) throws GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    LOGGER.debug("createTransferredResourceFile(path={}, name={})", parentUUID, fileName);
    RodaCoreFactory.getTransferredResourcesScanner().createFile(parentUUID, fileName, inputStream);
    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }

  }

  protected static <T extends IsIndexed> void delete(RodaUser user, Class<T> returnClass, SelectedItems ids)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    List<String> idList = consolidate(user, returnClass, ids);
    RodaCoreFactory.getIndexService().delete(returnClass, idList);
  }

  public static boolean getScanUpdateStatus() {
    return RodaCoreFactory.getTransferredResourcesScannerUpdateStatus();
  }

  public static void runTransferredResourceScan(String subFolderUUID, boolean waitToFinish)
    throws IsStillUpdatingException {
    RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(subFolderUUID, waitToFinish);
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
      for (int i = 0; i < collectionsCount; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        IndexResult<IndexedAIP> collections = index.find(IndexedAIP.class, allButRepresentationsFilter, null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE));
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

  public static List<SupportedMetadataTypeBundle> getSupportedMetadata(RodaUser user, Locale locale)
    throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    String[] types = RodaCoreFactory.getRodaConfiguration()
      .getString(RodaConstants.UI_BROWSER_METADATA_DESCRIPTIVE_TYPES).split(", ?");

    List<SupportedMetadataTypeBundle> supportedMetadata = new ArrayList<>();

    if (types != null) {
      for (String type : types) {
        String version = null;
        if (type.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
          version = type.substring(type.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1, type.length());
          type = type.substring(0, type.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
        }

        String key = RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + type;
        if (version != null) {
          key += "." + version;
        }
        String label = messages.getTranslation(key, type);
        String template = null;
        InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream("templates/"
          + ((version != null) ? type + RodaConstants.METADATA_VERSION_SEPARATOR + version : type) + ".xml");

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

        SupportedMetadataTypeBundle b = new SupportedMetadataTypeBundle(type, version, label, template);
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
          retrieveFile = RodaCoreFactory.getTransferredResourcesScanner()
            .retrieveFile(transferredResource.getFullPath());
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
    Map<String, IndexedAIP> aips = new HashMap<String, IndexedAIP>();
    Map<String, IndexedRepresentation> representations = new HashMap<String, IndexedRepresentation>();
    Map<String, IndexedFile> files = new HashMap<String, IndexedFile>();
    Map<String, TransferredResource> transferredResources = new HashMap<String, TransferredResource>();
    IndexedPreservationEvent ipe = RodaCoreFactory.getIndexService().retrieve(IndexedPreservationEvent.class, eventId);
    eventBundle.setEvent(ipe);
    if (ipe.getLinkingAgentIds() != null && !ipe.getLinkingAgentIds().isEmpty()) {
      Map<String, IndexedPreservationAgent> agents = new HashMap<String, IndexedPreservationAgent>();
      for (LinkingIdentifier agentID : ipe.getLinkingAgentIds()) {
        try {
          IndexedPreservationAgent agent = RodaCoreFactory.getIndexService().retrieve(IndexedPreservationAgent.class,
            agentID.getValue());
          agents.put(agentID.getValue(), agent);
        } catch (NotFoundException | GenericException e) {
          LOGGER.error("Error getting agent " + agentID + ": " + e.getMessage());
        }
      }
      eventBundle.setAgents(agents);
    }

    List<LinkingIdentifier> allLinkingIdentifiers = new ArrayList<>();

    if (ipe.getSourcesObjectIds() != null) {
      allLinkingIdentifiers.addAll(ipe.getSourcesObjectIds());
    }

    if (ipe.getOutcomeObjectIds() != null) {
      allLinkingIdentifiers.addAll(ipe.getOutcomeObjectIds());
    }

    for (LinkingIdentifier identifier : allLinkingIdentifiers) {
      String idValue = identifier.getValue();
      LinkingObjectType linkingType = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      try {
        if (LinkingObjectType.AIP.equals(linkingType)) {
          String uuid = LinkingObjectUtils.getAipIdFromLinkingId(idValue);
          IndexedAIP aip = retrieve(IndexedAIP.class, uuid);
          aips.put(idValue, aip);
        } else if (LinkingObjectType.REPRESENTATION.equals(linkingType)) {
          String uuid = LinkingObjectUtils.getRepresentationIdFromLinkingId(idValue);
          IndexedRepresentation rep = retrieve(IndexedRepresentation.class, uuid);
          representations.put(idValue, rep);
        } else if (LinkingObjectType.FILE.equals(linkingType)) {
          IndexedFile file = retrieve(IndexedFile.class, LinkingObjectUtils.getFileIdFromLinkingId(idValue));
          files.put(idValue, file);
        } else if (LinkingObjectType.TRANSFERRED_RESOURCE.equals(linkingType)) {
          String id = LinkingObjectUtils.getTransferredResourceIdFromLinkingId(idValue);
          String uuid = UUID.nameUUIDFromBytes(id.getBytes()).toString();
          TransferredResource tr = retrieve(TransferredResource.class, uuid);
          transferredResources.put(idValue, tr);
        } else {
          LOGGER.warn("No support for linking object type: " + linkingType);
        }
      } catch (NotFoundException e) {
        // nothing to do
      }
    }

    eventBundle.setAips(aips);
    eventBundle.setRepresentations(representations);
    eventBundle.setFiles(files);
    eventBundle.setTransferredResources(transferredResources);

    return eventBundle;
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

    CloseableIterable<BinaryVersion> it = listDescriptiveMetadataVersions(aipId, metadataId);
    for (BinaryVersion v : it) {
      versionBundles.add(new BinaryVersionBundle(v.getId(), v.getMessage(), v.getCreatedDate()));
    }
    IOUtils.closeQuietly(it);

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

  public static void updateAIPPermissions(IndexedAIP indexedAIP, Permissions permissions)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    AIP aip = RodaCoreFactory.getModelService().retrieveAIP(indexedAIP.getId());
    aip.setPermissions(permissions);
    RodaCoreFactory.getModelService().updateAIPPermissions(aip);
  }

  public static Risk addRisk(Risk risk) throws GenericException, RequestNotValidException {
    Risk createdRisk = RodaCoreFactory.getModelService().createRisk(risk);
    RodaCoreFactory.getIndexService().create(Risk.class, createdRisk);
    return createdRisk;
  }

  public static void modifyRisk(Risk risk, String message) throws GenericException, RequestNotValidException {
    RodaCoreFactory.getModelService().updateRisk(risk, message);
    RodaCoreFactory.getIndexService().delete(Risk.class, Arrays.asList(risk.getId()));
    RodaCoreFactory.getIndexService().create(Risk.class, risk);
  }

  public static Agent addAgent(Agent agent) throws GenericException, RequestNotValidException {
    Agent createdAgent = RodaCoreFactory.getModelService().createAgent(agent);
    RodaCoreFactory.getIndexService().create(Agent.class, createdAgent);
    return createdAgent;
  }

  public static void modifyAgent(Agent agent) throws GenericException, RequestNotValidException {
    RodaCoreFactory.getModelService().updateAgent(agent);
    RodaCoreFactory.getIndexService().delete(Agent.class, Arrays.asList(agent.getId()));
    RodaCoreFactory.getIndexService().create(Agent.class, agent);
  }

  public static Format addFormat(Format format) throws GenericException, RequestNotValidException {
    Format createdFormat = RodaCoreFactory.getModelService().createFormat(format);
    RodaCoreFactory.getIndexService().create(Format.class, createdFormat);
    return createdFormat;
  }

  public static void modifyFormat(Format format) throws GenericException, RequestNotValidException {
    RodaCoreFactory.getModelService().updateFormat(format);
    RodaCoreFactory.getIndexService().delete(Format.class, Arrays.asList(format.getId()));
    RodaCoreFactory.getIndexService().create(Format.class, format);
  }

  public static List<Format> retrieveFormats(String agentId) throws NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().retrieveFormatsFromAgent(agentId);
  }

  public static List<Agent> retrieveRequiredAgents(String agentId) throws NotFoundException, GenericException {
    Agent agent = RodaCoreFactory.getIndexService().retrieve(Agent.class, agentId);
    List<Agent> agentList = new ArrayList<Agent>();

    for (String otherAgentId : agent.getAgentsRequired()) {
      agentList.add(RodaCoreFactory.getIndexService().retrieve(Agent.class, otherAgentId));
    }

    return agentList;
  }

  public static RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);
    List<BinaryVersionBundle> versionList = new ArrayList<BinaryVersionBundle>();
    boolean versionFlag = false;
    Date newestDate = new Date();
    Binary lastRiskBinary = null;

    for (BinaryVersion bv : iterable) {
      versionList.add(new BinaryVersionBundle(bv.getId(), bv.getMessage(), bv.getCreatedDate()));

      if (!versionFlag) {
        lastRiskBinary = bv.getBinary();
        newestDate = bv.getCreatedDate();
        versionFlag = true;
      } else if (newestDate.before(bv.getCreatedDate())) {
        lastRiskBinary = bv.getBinary();
        newestDate = bv.getCreatedDate();
      }
    }

    iterable.close();
    Risk lastRisk = JsonUtils.getObjectFromJson(lastRiskBinary.getContent().createInputStream(), Risk.class);
    RiskVersionsBundle riskBundle = new RiskVersionsBundle(lastRisk, versionList);
    return riskBundle;
  }

  public static boolean hasRiskVersions(String id)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(id);
    CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);
    boolean hasRiskVersion = iterable.iterator().hasNext();
    IOUtils.closeQuietly(iterable);
    return hasRiskVersion;
  }

  public static void revertRiskVersion(String riskId, String versionId, String message)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertRiskVersion(riskId, versionId, message);
  }

  public static void removeRiskVersion(String riskId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }

  public static Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    BinaryVersion bv = RodaCoreFactory.getModelService().retrieveVersion(riskId, selectedVersion);
    Risk oldRisk = JsonUtils.getObjectFromJson(bv.getBinary().getContent().createInputStream(), Risk.class);
    return oldRisk;
  }

  public static void validateExportAipParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  public static List<IndexedAIP> matchAIP(Filter filter, RodaUser user)
    throws GenericException, RequestNotValidException {
    List<IndexedAIP> aips = new ArrayList<IndexedAIP>();
    long count = count(IndexedAIP.class, filter, user);
    for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
      Sorter sorter = new Sorter(new SortParameter(RodaConstants.AIP_ID, true));
      IndexResult<IndexedAIP> res = find(IndexedAIP.class, filter, sorter,
        new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null, user);
      aips.addAll(res.getResults());
    }
    return aips;
  }

  public static void deleteAIPs(List<IndexedAIP> aips, boolean deleteOnlyRepresentation)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    for (IndexedAIP aip : aips) {
      if (deleteOnlyRepresentation) {
        AIP fullAIP = model.retrieveAIP(aip.getId());
        for (Representation r : fullAIP.getRepresentations()) {
          model.deleteRepresentation(aip.getId(), r.getId());
        }
      } else {
        model.deleteAIP(aip.getId());
      }

    }
  }

  public static void validateGetAipParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: " + Arrays
          .asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }

  }

  public static StreamResponse getAIP(IndexedAIP indexedAIP, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      List<ZipEntryInfo> zipEntries = ModelUtils.zipIndexedAIP(Arrays.asList(indexedAIP));
      return createZipStreamResponse(zipEntries, "export");
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)) {
      StoragePath aipJsonPath = DefaultStoragePath.parse(ModelUtils.getAIPStoragePath(indexedAIP.getId()),
        RodaConstants.STORAGE_AIP_METADATA_FILENAME);

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary aipJSONBinary = storage.getBinary(aipJsonPath);
      String filename = aipJsonPath.getName();
      String mediaType = MediaType.APPLICATION_JSON;
      StreamingOutput stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          InputStream inputStream = aipJSONBinary.getContent().createInputStream();
          try {
            IOUtils.copy(inputStream, os);
          } finally {
            IOUtils.closeQuietly(inputStream);
          }
        }
      };
      return new StreamResponse(filename, mediaType, stream);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static StreamResponse getAIPs(SelectedItems selected, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, IOException {
    IndexService index = RodaCoreFactory.getIndexService();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      if (selected instanceof SelectedItemsFilter) {
        SelectedItemsFilter selectedItems = (SelectedItemsFilter) selected;
        long count = index.count(IndexedAIP.class, selectedItems.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItems.getFilter(), null,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          zipEntries.addAll(ModelUtils.zipIndexedAIP(aips));
        }
      } else {
        SelectedItemsList selectedItems = (SelectedItemsList) selected;
        zipEntries.addAll(ModelUtils.zipIndexedAIP(ModelUtils.getIndexedAIPsFromObjectIds(selectedItems)));
      }
      return createZipStreamResponse(zipEntries, "export");
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)) {
      throw new GenericException("Not yet supported: " + acceptFormat);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) {

    int lowLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "lowLimit");
    int highLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "highLimit");

    String preProbability = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability",
      Integer.toString(preMitigationProbability));
    String preImpact = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact",
      Integer.toString(preMitigationImpact));
    String posProbability = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability",
      Integer.toString(posMitigationProbability));
    String posImpact = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact",
      Integer.toString(posMitigationImpact));

    RiskMitigationBundle terms = new RiskMitigationBundle(lowLimit, highLimit, preProbability, preImpact,
      posProbability, posImpact);
    return terms;
  }

  public static List<String> retrieveShowMitigationTerms() {
    List<String> terms = new ArrayList<String>();
    terms.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationSeverity", "lowLimit"));
    terms.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationSeverity", "highLimit"));
    return terms;
  }

  public static MitigationPropertiesBundle retrieveAllMitigationProperties() {
    int lowLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "lowLimit");
    int highLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "highLimit");

    int probabilityLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationProbability", "limit");
    int impactLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationImpact", "limit");

    // second list contains probability content
    List<String> probabilities = new ArrayList<String>();
    for (int i = 0; i <= probabilityLimit; i++) {
      String value = Integer.toString(i);
      probabilities.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability", value));
    }

    // third list contains impact content
    List<String> impacts = new ArrayList<String>();
    for (int i = 0; i <= impactLimit; i++) {
      String value = Integer.toString(i);
      impacts.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact", value));
    }

    MitigationPropertiesBundle properties = new MitigationPropertiesBundle(lowLimit, highLimit, probabilities, impacts);
    return properties;
  }
}
