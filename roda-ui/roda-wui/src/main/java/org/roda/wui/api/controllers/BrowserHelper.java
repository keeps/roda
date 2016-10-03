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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.DownloadUtils;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.IdUtils;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.common.tools.ZipTools;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.common.RODAObjectList;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.AIPs;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.Representations;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.ip.metadata.PreservationMetadataList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.plugins.risks.RiskIncidenceRemoverPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.roda.wui.client.browse.BinaryVersionBundle;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.DescriptiveMetadataViewBundle;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.server.RodaStreamingOutput;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.server.common.XMLSimilarityIgnoreElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class BrowserHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);

  private static final int BUNDLE_MAX_REPRESENTATION_COUNT = 10;
  private static final int BUNDLE_MAX_ADDED_ORIGINAL_REPRESENTATION_COUNT = 1;

  protected static BrowseItemBundle retrieveItemBundle(String aipId, Locale locale)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    BrowseItemBundle itemBundle = new BrowseItemBundle();

    // set aip
    IndexedAIP aip = retrieve(IndexedAIP.class, aipId);
    itemBundle.setAIP(aip);

    // set aip ancestors
    try {
      itemBundle.setAIPAncestors(retrieveAncestors(aip));
    } catch (NotFoundException e) {
      LOGGER.warn("Found an item with invalid ancestors: {}", aipId, e);
    }

    // set descriptive metadata
    try {
      List<DescriptiveMetadataViewBundle> descriptiveMetadataList = retrieveDescriptiveMetadataBundles(aipId, locale);
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

  private static List<DescriptiveMetadataViewBundle> retrieveDescriptiveMetadataBundles(String aipId,
    final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    ModelService model = RodaCoreFactory.getModelService();
    List<DescriptiveMetadata> listDescriptiveMetadataBinaries = model.retrieveAIP(aipId).getDescriptiveMetadata();
    List<DescriptiveMetadataViewBundle> descriptiveMetadataList = new ArrayList<>();

    if (listDescriptiveMetadataBinaries != null) { // Can be null when the AIP
                                                   // is a ghost
      for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
        DescriptiveMetadataViewBundle bundle = retrieveDescriptiveMetadataBundle(aipId, descriptiveMetadata, locale);
        descriptiveMetadataList.add(bundle);
      }
    }

    return descriptiveMetadataList;
  }

  private static DescriptiveMetadataViewBundle retrieveDescriptiveMetadataBundle(String aipId,
    DescriptiveMetadata descriptiveMetadata, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    DescriptiveMetadataViewBundle bundle = new DescriptiveMetadataViewBundle();
    bundle.setId(descriptiveMetadata.getId());

    if (descriptiveMetadata.getType() != null) {
      try {
        if (descriptiveMetadata.getVersion() != null) {
          bundle.setLabel(messages.getTranslation(
            RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase()
              + RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadata.getVersion().toLowerCase()));
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
        .listBinaryVersions(ModelUtils.getDescriptiveMetadataStoragePath(aipId, descriptiveMetadata.getId()))));
    } catch (RODAException | RuntimeException t) {
      bundle.setHasHistory(false);
    }
    return bundle;
  }

  private static DescriptiveMetadataViewBundle retrieveDescriptiveMetadataBundle(String aipId,
    String descriptiveMetadataId, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
    return retrieveDescriptiveMetadataBundle(aipId, descriptiveMetadata, locale);
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, IndexedAIP aip,
    String descriptiveMetadataId, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    DescriptiveMetadata metadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aip.getId(),
      descriptiveMetadataId);
    return retrieveDescriptiveMetadataEditBundle(user, aip, descriptiveMetadataId, metadata.getType(),
      metadata.getVersion(), locale);
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, IndexedAIP aip,
    String descriptiveMetadataId, String type, String version, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataEditBundle ret;
    InputStream inputStream = null;
    try {
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aip.getId(),
        descriptiveMetadataId);
      inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream, "UTF-8");

      // Get the supported metadata type with the same type and version
      // We need this to try to get get the values for the form
      SupportedMetadataTypeBundle metadataTypeBundle = null;
      List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = BrowserHelper.retrieveSupportedMetadata(user,
        aip, locale);
      for (SupportedMetadataTypeBundle typeBundle : supportedMetadataTypeBundles) {
        if (typeBundle.getType() != null && typeBundle.getType().equalsIgnoreCase(type)) {
          if (typeBundle.getVersion() == version
            || (typeBundle.getVersion() != null && typeBundle.getVersion().equalsIgnoreCase(version))) {
            metadataTypeBundle = typeBundle;
            break;
          }
        }
      }

      boolean similar = false;
      // Get the values using XPath
      Set<MetadataValue> values = null;
      String template = null;

      if (metadataTypeBundle != null) {
        values = metadataTypeBundle.getValues();
        template = metadataTypeBundle.getTemplate();
        if (values != null) {
          for (MetadataValue mv : values) {
            // clear the auto-generated values
            // mv.set("value", null);
            String xpathRaw = mv.get("xpath");
            if (xpathRaw != null && xpathRaw.length() > 0) {
              String[] xpaths = xpathRaw.split("##%##");
              String value;
              List<String> allValues = new ArrayList<>();
              for (String xpath : xpaths) {
                allValues.addAll(ServerTools.applyXpath(xml, xpath));
              }
              // if any of the values is different, concatenate all values in a
              // string, otherwise return the value
              boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.get(0).trim()));
              if (allEqual && !allValues.isEmpty()) {
                value = allValues.get(0);
              } else {
                value = String.join(" / ", allValues);
              }
              mv.set("value", value.trim());
            }
          }
          // Identity check. Test if the original XML is equal to the result of
          // applying the extracted values to the template
          metadataTypeBundle.setValues(values);
          String templateWithValues = retrieveDescriptiveMetadataPreview(metadataTypeBundle);
          try {
            XMLUnit.setIgnoreComments(true);
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreAttributeOrder(true);
            XMLUnit.setCompareUnmatched(false);

            Diff xmlDiff = new Diff(xml, templateWithValues);
            xmlDiff.overrideDifferenceListener(new XMLSimilarityIgnoreElements("schemaLocation"));
            similar = xmlDiff.identical() || xmlDiff.similar();
          } catch (SAXException e) {
          }
        }
      }

      ret = new DescriptiveMetadataEditBundle(descriptiveMetadataId, type, version, xml, template, values, similar);
    } catch (IOException e) {
      throw new GenericException("Error getting descriptive metadata edit bundle: " + e.getMessage());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    return ret;
  }

  protected static List<IndexedAIP> retrieveAncestors(IndexedAIP aip) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(aip);
  }

  protected static <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, User user, boolean justActive) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(returnClass, filter, sorter, sublist, facets, user, justActive);
  }

  protected static <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Sublist sublist, final User user, final boolean justActive) {
    return RodaCoreFactory.getIndexService().findAll(returnClass, filter, sorter, sublist, user, justActive, true);
  }

  protected static <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, User user)
    throws GenericException, RequestNotValidException {
    boolean justActive = false;
    return RodaCoreFactory.getIndexService().count(returnClass, filter, user, justActive);
  }

  protected static <T extends IsIndexed> T retrieve(Class<T> returnClass, String id)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(returnClass, id);
  }

  protected static <T extends IsIndexed> List<T> retrieve(Class<T> returnClass, SelectedItems<T> selectedItems)
    throws GenericException, NotFoundException, RequestNotValidException {
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<T> selectedList = (SelectedItemsList<T>) selectedItems;
      return RodaCoreFactory.getIndexService().retrieve(returnClass, selectedList.getIds());
    } else if (selectedItems instanceof SelectedItemsFilter) {
      SelectedItemsFilter<T> selectedFilter = (SelectedItemsFilter<T>) selectedItems;
      int counter = RodaCoreFactory.getIndexService().count(returnClass, selectedFilter.getFilter()).intValue();
      return RodaCoreFactory.getIndexService()
        .find(returnClass, selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter)).getResults();
    }

    return null;
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

  public static void validateGetAIPRepresentationFileParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  protected static void validateGetAIPRepresentationParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP));
    }
  }

  protected static EntityResponse retrieveAIPRepresentation(IndexedRepresentation representation, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    String aipId = representation.getAipId();
    String representationId = representation.getId();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      StoragePath storagePath = ModelUtils.getRepresentationStoragePath(representation.getAipId(),
        representation.getId());
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return download(directory);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      ModelService model = RodaCoreFactory.getModelService();
      Representation rep = model.retrieveRepresentation(aipId, representationId);
      return new ObjectResponse<Representation>(acceptFormat, rep);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  private static StreamResponse download(Resource resource) {
    ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), resource);
    StreamingOutput streamingOutput = new RodaStreamingOutput(download);
    return new StreamResponse(download.getFileName(), download.getMediaType(), streamingOutput);
  }

  public static StreamResponse retrieveAIPRepresentationPart(IndexedRepresentation representation, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();

    if (RodaConstants.STORAGE_DIRECTORY_DATA.equals(part)) {
      StoragePath storagePath = ModelUtils.getRepresentationDataStoragePath(aipId, representationId);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_METADATA.equals(part)) {
      StoragePath storagePath = ModelUtils.getRepresentationMetadataStoragePath(aipId, representationId);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId, representationId);
      return download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId, representationId);
      return download(directory);
    } else {
      throw new GenericException("Unsupported part: " + part);
    }
  }

  protected static void validateListAIPDescriptiveMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }
  }

  protected static EntityResponse listAIPDescriptiveMetadata(String aipId, String start, String limit,
    String acceptFormat)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    AIP aip = model.retrieveAIP(aipId);
    List<DescriptiveMetadata> metadata = aip.getDescriptiveMetadata();
    return listDescriptiveMetadata(metadata, aipId, start, limit, acceptFormat);
  }

  protected static EntityResponse listRepresentationDescriptiveMetadata(String aipId, String representationId,
    String start, String limit, String acceptFormat)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    Representation representation = model.retrieveRepresentation(aipId, representationId);
    List<DescriptiveMetadata> metadata = representation.getDescriptiveMetadata();
    return listDescriptiveMetadata(metadata, aipId, start, limit, acceptFormat);
  }

  private static EntityResponse listDescriptiveMetadata(List<DescriptiveMetadata> metadata, String aipId, String start,
    String limit, String acceptFormat)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StorageService storage = RodaCoreFactory.getStorageService();
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();
    int counter = 0;

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      for (DescriptiveMetadata dm : metadata) {
        if (counter >= startInt && (counter <= limitInt || limitInt == -1)) {
          StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, dm.getId());
          Binary binary = storage.getBinary(storagePath);
          ZipEntryInfo info = new ZipEntryInfo(storagePath.getName(), binary.getContent());
          zipEntries.add(info);
        } else {
          break;
        }
        counter++;
      }

      return createZipStreamResponse(zipEntries, aipId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      int endInt = limitInt == -1 ? metadata.size() : (limitInt > metadata.size() ? metadata.size() : limitInt);
      DescriptiveMetadataList list = new DescriptiveMetadataList(metadata.subList(startInt, endInt));
      return new ObjectResponse(acceptFormat, list);
    }

    return null;
  }

  protected static void validateGetAIPDescriptiveMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: "
          + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML,
            RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON,
            RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  public static EntityResponse retrieveAIPDescritiveMetadata(String aipId, String metadataId, String acceptFormat,
    String language)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;
    StreamResponse ret = null;
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
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
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
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

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {

      AIP aip = model.retrieveAIP(aipId);
      List<DescriptiveMetadata> resultList = aip.getDescriptiveMetadata().stream()
        .filter(dm -> dm.getId().equals(metadataId)).collect(Collectors.toList());

      return new ObjectResponse<DescriptiveMetadata>(acceptFormat, resultList.get(0));
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }

    return ret;
  }

  public static EntityResponse retrieveRepresentationDescriptiveMetadata(String aipId, String representationId,
    String metadataId, String acceptFormat, String language)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;
    StreamResponse ret = null;
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, representationId, metadataId);
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
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, representationId, metadataId);
      filename = descriptiveMetadataBinary.getStoragePath().getName() + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, representationId, metadataId);
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

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {

      Representation representation = model.retrieveRepresentation(aipId, representationId);
      List<DescriptiveMetadata> resultList = representation.getDescriptiveMetadata().stream()
        .filter(dm -> dm.getId().equals(metadataId)).collect(Collectors.toList());

      return new ObjectResponse<DescriptiveMetadata>(acceptFormat, resultList.get(0));
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }

    return ret;
  }

  public static EntityResponse retrieveAIPDescritiveMetadataVersion(String aipId, String metadataId, String versionId,
    String acceptFormat, String language)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final StreamingOutput stream;

    ModelService model = RodaCoreFactory.getModelService();

    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, metadataId);
    BinaryVersion binaryVersion = model.getStorage().getBinaryVersion(storagePath, versionId);
    Binary binary = binaryVersion.getBinary();

    String fileName = binary.getStoragePath().getName();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      mediaType = MediaType.TEXT_XML;
      stream = new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(binary.getContent().createInputStream(), os);
        }
      };
      return new StreamResponse(fileName, mediaType, stream);

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
      return new StreamResponse(filename, mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {

      AIP aip = model.retrieveAIP(aipId);
      List<DescriptiveMetadata> resultList = aip.getDescriptiveMetadata().stream()
        .filter(dm -> dm.getId().equals(metadataId)).collect(Collectors.toList());

      return new ObjectResponse<DescriptiveMetadata>(acceptFormat, resultList.get(0));
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  protected static void validateListAIPPreservationMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }
  }

  // FIXME 20160406 hsilva: representation preservation metadata is not being
  // included in the response "package"
  public static EntityResponse listAIPPreservationMetadata(String aipId, String acceptFormat)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory.getModelService()
        .listPreservationMetadata(aipId, true);
      StorageService storage = RodaCoreFactory.getStorageService();
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      Map<String, ZipEntryInfo> agents = new HashMap<String, ZipEntryInfo>();

      for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
        if (oPreservationFile.isPresent()) {
          PreservationMetadata preservationFile = oPreservationFile.get();
          StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);
          Binary binary = storage.getBinary(storagePath);

          ZipEntryInfo info = new ZipEntryInfo(FSUtils.getStoragePathAsString(storagePath, true), binary.getContent());
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
              // do nothing
            }
          }

        } else {
          LOGGER.error("Cannot get AIP preservation metadata", oPreservationFile.getCause());
        }
      }

      if (agents.size() > 0) {
        for (Map.Entry<String, ZipEntryInfo> entry : agents.entrySet()) {
          zipEntries.add(entry.getValue());
        }
      }

      IOUtils.closeQuietly(preservationFiles);
      return createZipStreamResponse(zipEntries, aipId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory.getModelService()
        .listPreservationMetadata(aipId, true);
      PreservationMetadataList metadataList = new PreservationMetadataList();

      for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
        if (oPreservationFile.isPresent()) {
          metadataList.addObject(oPreservationFile.get());
        }
      }

      IOUtils.closeQuietly(preservationFiles);
      return new ObjectResponse(acceptFormat, metadataList);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  protected static void validateGetAIPRepresentationPreservationMetadataParams(String acceptFormat, String language)
    throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }

    // FIXME validate language? what exception should be thrown?
    if (!StringUtils.isNotBlank(language)) {
      throw new RequestNotValidException("Parameter '" + RodaConstants.API_QUERY_KEY_LANG + "' must have a value");
    }

  }

  private static EntityResponse getAIPRepresentationPreservationMetadataEntityResponse(String aipId,
    String representationId, String startAgent, String limitAgent, String startEvent, String limitEvent,
    String startFile, String limitFile, String acceptFormat,
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, IOException {
    Pair<Integer, Integer> pagingParamsAgent = ApiUtils.processPagingParams(startAgent, limitAgent);
    int counterAgent = 0;
    Pair<Integer, Integer> pagingParamsEvent = ApiUtils.processPagingParams(startEvent, limitEvent);
    int counterEvent = 0;
    Pair<Integer, Integer> pagingParamsFile = ApiUtils.processPagingParams(startFile, limitFile);
    int counterFile = 0;

    List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
    PreservationMetadataList pms = new PreservationMetadataList();

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
        } else if (preservationFile.getType().equals(PreservationMetadataType.FILE)) {
          if (counterFile >= pagingParamsFile.getFirst()
            && (counterFile <= pagingParamsFile.getSecond() || pagingParamsFile.getSecond() == -1)) {
            add = true;
          }
          counterFile++;
        }

        if (add) {
          if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
            StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);
            Binary binary = RodaCoreFactory.getStorageService().getBinary(storagePath);
            ZipEntryInfo info = new ZipEntryInfo(storagePath.getName(), binary.getContent());
            zipEntries.add(info);
          } else {
            pms.addObject(preservationFile);
          }
        }
      } else {
        LOGGER.error("Cannot get AIP preservation metadata", oPreservationFile.getCause());
      }
    }

    IOUtils.closeQuietly(preservationFiles);
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      return createZipStreamResponse(zipEntries, aipId + "_" + representationId);
    } else {
      return new ObjectResponse(acceptFormat, pms);
    }
  }

  public static EntityResponse retrieveAIPRepresentationPreservationMetadata(String aipId, String representationId,
    String startAgent, String limitAgent, String startEvent, String limitEvent, String startFile, String limitFile,
    String acceptFormat, String language)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException, IOException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = null;
      preservationFiles = RodaCoreFactory.getModelService().listPreservationMetadata(aipId, representationId);
      return getAIPRepresentationPreservationMetadataEntityResponse(aipId, representationId, startAgent, limitAgent,
        startEvent, limitEvent, startFile, limitFile, acceptFormat, preservationFiles);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static StreamResponse retrieveAIPRepresentationPreservationMetadataFile(String aipId, String representationId,
    String fileId, String acceptFormat)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      Binary binary = RodaCoreFactory.getModelService().retrievePreservationRepresentation(aipId, representationId);

      String filename = binary.getStoragePath().getName();
      StreamingOutput stream = new StreamingOutput() {

        public void write(OutputStream os) throws IOException, WebApplicationException {
          IOUtils.copy(binary.getContent().createInputStream(), os);
        }
      };

      return new StreamResponse(filename, MediaType.APPLICATION_OCTET_STREAM, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      // TODO
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static void createOrUpdateAIPRepresentationPreservationMetadataFile(String aipId, String representationId,
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
        model.createPreservationMetadata(PreservationMetadataType.FILE, aipId, representationId, fileDirectoryPath,
          fileId, payload, notify);
      } else {
        PreservationMetadataType type = PreservationMetadataType.FILE;
        String id = IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, fileId);
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

  public static void deletePreservationMetadataFile(PreservationMetadataType type, String aipId,
    String representationId, String id, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deletePreservationMetadata(type, aipId, representationId, id, notify);
  }

  public static IndexedAIP moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, User user)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);

    ModelService model = RodaCoreFactory.getModelService();

    for (String aipId : aipIds) {
      // XXX this method could be improved by moving all at once in the model
      if (!aipId.equals(parentId)) {
        // laxing check of ancestry so a big list can be moved to one of the
        // siblings
        LOGGER.debug("Moving AIP {} under {}", aipId, parentId);
        model.moveAIP(aipId, parentId);
      }
    }

    IndexService index = RodaCoreFactory.getIndexService();
    index.commit(IndexedAIP.class);
    index.commit(IndexedRepresentation.class);
    index.commit(IndexedFile.class);

    return index.retrieve(IndexedAIP.class, parentId);
  }

  public static AIP createAIP(User user, String parentAipId, String type, Permissions permissions)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    AIP aip = model.createAIP(parentAipId, type, permissions, user.getName());
    return aip;
  }

  public static AIP updateAIP(User user, AIP aip) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    AIP updatedAIP = model.updateAIP(aip, user.getName());
    return updatedAIP;
  }

  public static String deleteAIP(SelectedItems<IndexedAIP> selected, User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(AIP.class, aipIds));
      Jobs.createJob(user, job);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete AIP associated incidences");
    }

    String parentId = null;

    for (String aipId : aipIds) {
      AIP aip = RodaCoreFactory.getModelService().retrieveAIP(aipId);
      parentId = aip.getParentId();
      RodaCoreFactory.getModelService().deleteAIP(aip.getId());

      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      RodaCoreFactory.getIndexService().execute(IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

        @Override
        public void run(IndexedAIP item)
          throws GenericException, RequestNotValidException, AuthorizationDeniedException {
          try {
            // UserUtility.checkObjectPermissions(user, item,
            // PermissionType.DELETE);
            RodaCoreFactory.getModelService().deleteAIP(item.getId());
          } catch (NotFoundException e) {
            // already deleted, ignore
          }
        }
      });
    }

    RodaCoreFactory.getIndexService().commitAIPs();
    return parentId;
  }

  public static void deleteRepresentation(SelectedItems<IndexedRepresentation> selected, User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> representationIds = consolidate(user, IndexedRepresentation.class, selected);

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(IndexedRepresentation.class, representationIds));
      Jobs.createJob(user, job);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete representation associated incidences");
    }

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, representationIds));
    IndexResult<IndexedRepresentation> reps = RodaCoreFactory.getIndexService().find(IndexedRepresentation.class,
      filter, Sorter.NONE, new Sublist(0, representationIds.size()));

    for (IndexedRepresentation rep : reps.getResults()) {
      RodaCoreFactory.getModelService().deleteRepresentation(rep.getAipId(), rep.getId());
    }

    RodaCoreFactory.getIndexService().commit(IndexedRepresentation.class);
  }

  public static void deleteFile(SelectedItems<IndexedFile> selected, User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> fileIds = consolidate(user, IndexedFile.class, selected);

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(IndexedFile.class, fileIds));
      Jobs.createJob(user, job);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete file associated incidences");
    }

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, fileIds));
    IndexResult<IndexedFile> files = RodaCoreFactory.getIndexService().find(IndexedFile.class, filter, Sorter.NONE,
      new Sublist(0, fileIds.size()));

    for (IndexedFile file : files.getResults()) {
      RodaCoreFactory.getModelService().deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(),
        file.getId(), true);
    }

    RodaCoreFactory.getIndexService().commit(IndexedFile.class);
  }

  public static String deleteAIPRepresentations(SelectedItems<IndexedAIP> selected, User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);

    String parentId = null;

    for (final String aipId : aipIds) {
      try {
        AIP aip = RodaCoreFactory.getModelService().retrieveAIP(aipId);
        parentId = aip.getParentId();

        for (Representation rep : aip.getRepresentations()) {
          RodaCoreFactory.getModelService().deleteRepresentation(aipId, rep.getId());
        }

        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId));

        RodaCoreFactory.getIndexService().execute(IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

          @Override
          public void run(IndexedAIP item)
            throws GenericException, RequestNotValidException, AuthorizationDeniedException {
            try {
              UserUtility.checkObjectPermissions(user, item, PermissionType.DELETE);
              for (Representation rep : aip.getRepresentations()) {
                RodaCoreFactory.getModelService().deleteRepresentation(aipId, rep.getId());
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
    return createDescriptiveMetadataFile(aipId, null, descriptiveMetadataId, descriptiveMetadataType,
      descriptiveMetadataVersion, descriptiveMetadataPayload);
  }

  public static DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String representationId,
    String descriptiveMetadataId, String descriptiveMetadataType, String descriptiveMetadataVersion,
    ContentPayload descriptiveMetadataPayload) throws GenericException, ValidationException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion);
  }

  public static DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String descriptiveMetadataId,
    String descriptiveMetadataType, String descriptiveMetadataVersion, ContentPayload descriptiveMetadataPayload,
    String message) throws GenericException, AuthorizationDeniedException, ValidationException,
    RequestNotValidException, NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, message);

  }

  public static void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId);
  }

  public static DescriptiveMetadata retrieveMetadataFile(String aipId, String descriptiveMetadataId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  // FIXME allow to create a zip without files/directories???
  private static StreamResponse createZipStreamResponse(List<ZipEntryInfo> zipEntries, String zipName) {
    final StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException {
        ZipTools.zip(zipEntries, os);
      }
    };

    return new StreamResponse(zipName + ".zip", "application/zip", stream);
  }

  public static Representation createRepresentation(String aipId, String representationId, String type)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    return RodaCoreFactory.getModelService().createRepresentation(aipId, representationId, true, type, false);
  }

  public static Representation updateRepresentation(User user, Representation representation) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateRepresentationInfo(representation);
  }

  public static void deleteRepresentation(String aipId, String representationId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRepresentation(aipId, representationId);
  }

  public static File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload content) throws GenericException, AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().createFile(aipId, representationId, directoryPath, fileId, content);
  }

  public static File updateFile(User user, File file) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateFileInfo(file);
  }

  public static void deleteRepresentationFile(String fileUUID)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUUID);
    RodaCoreFactory.getModelService().deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), true);
  }

  public static EntityResponse retrieveAIPRepresentationFile(String fileUuid, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    IndexedFile iFile = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final String filename;
      final String mediaType;
      final StreamingOutput stream;

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(
        ModelUtils.getFileStoragePath(iFile.getAipId(), iFile.getRepresentationId(), iFile.getPath(), iFile.getId()));
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
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      File file = RodaCoreFactory.getModelService().retrieveFile(iFile.getAipId(), iFile.getRepresentationId(),
        iFile.getPath(), iFile.getId());
      return new ObjectResponse(acceptFormat, file);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static DescriptiveMetadata createOrUpdateAIPDescriptiveMetadataFile(String aipId, String representationId,
    String metadataId, String metadataType, String metadataVersion, String updateMessage, InputStream is,
    FormDataContentDisposition fileDetail, boolean create) throws GenericException, RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    Path file = null;
    DescriptiveMetadata dm = null;
    try {
      ModelService model = RodaCoreFactory.getModelService();
      file = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(file);

      if (create) {
        dm = model.createDescriptiveMetadata(aipId, representationId, metadataId, payload, metadataType,
          metadataVersion);
      } else {
        dm = model.updateDescriptiveMetadata(aipId, representationId, metadataId, payload, metadataType,
          metadataVersion, updateMessage);
      }

    } catch (IOException e) {
      throw new GenericException("Error creating or updating AIP descriptive metadata file", e);
    } finally {
      FSUtils.deletePathQuietly(file);
    }

    return dm;
  }

  public static TransferredResource createTransferredResourcesFolder(String parentUUID, String folderName,
    boolean forceCommit) throws GenericException, RequestNotValidException, NotFoundException {
    TransferredResource transferredResource = RodaCoreFactory.getTransferredResourcesScanner().createFolder(parentUUID,
      folderName);
    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }
    return transferredResource;
  }

  private static <T extends IsIndexed> List<String> consolidate(User user, Class<T> classToReturn,
    SelectedItems<T> selected) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<String> ret;

    if (selected instanceof SelectedItemsList) {
      ret = ((SelectedItemsList<T>) selected).getIds();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<T> selectedItemsFilter = (SelectedItemsFilter<T>) selected;
      Filter filter = selectedItemsFilter.getFilter();
      Long count = count(classToReturn, filter, user);
      IndexResult<T> find = find(classToReturn, filter, Sorter.NONE, new Sublist(0, count.intValue()), Facets.NONE,
        user, selectedItemsFilter.justActive());
      ret = find.getResults().stream().map(i -> i.getUUID()).collect(Collectors.toList());
    } else {
      throw new RequestNotValidException("Class not supported: " + selected.getClass().getName());
    }

    return ret;
  }

  public static void deleteTransferredResources(SelectedItems<TransferredResource> selected, User user)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    List<String> ids = consolidate(user, TransferredResource.class, selected);

    // check permissions
    UserUtility.checkTransferredResourceAccess(user, ids);

    RodaCoreFactory.getTransferredResourcesScanner().deleteTransferredResource(ids);
  }

  public static TransferredResource createTransferredResourceFile(String parentUUID, String fileName,
    InputStream inputStream, boolean forceCommit)
    throws GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    LOGGER.debug("createTransferredResourceFile(path={}, name={})", parentUUID, fileName);
    TransferredResource transferredResource = RodaCoreFactory.getTransferredResourcesScanner().createFile(parentUUID,
      fileName, inputStream);
    if (forceCommit) {
      RodaCoreFactory.getTransferredResourcesScanner().commit();
    }

    return transferredResource;
  }

  protected static <T extends IsIndexed> void delete(User user, Class<T> returnClass, SelectedItems<T> ids)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    List<String> idList = consolidate(user, returnClass, ids);
    RodaCoreFactory.getIndexService().delete(returnClass, idList);
    RodaCoreFactory.getIndexService().commit(returnClass);
  }

  public static boolean retrieveScanUpdateStatus() {
    return RodaCoreFactory.getTransferredResourcesScannerUpdateStatus();
  }

  public static void runTransferredResourceScan(String subFolderUUID, boolean waitToFinish)
    throws IsStillUpdatingException {
    RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(subFolderUUID, waitToFinish);
  }

  // TODO Limit access to SDO accessible by user
  public static StreamResponse retrieveClassificationPlan(User user)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ObjectNode root = mapper.createObjectNode();

      ArrayNode array = mapper.createArrayNode();
      List<String> descriptionsLevels = RodaUtils
        .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.LEVELS_CLASSIFICATION_PLAN));

      Filter allButRepresentationsFilter = new Filter(
        new OneOfManyFilterParameter(RodaConstants.AIP_LEVEL, descriptionsLevels));

      IndexService index = RodaCoreFactory.getIndexService();
      long collectionsCount = index.count(IndexedAIP.class, allButRepresentationsFilter);
      for (int i = 0; i < collectionsCount; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        IndexResult<IndexedAIP> collections = index.find(IndexedAIP.class, allButRepresentationsFilter, null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE));
        for (IndexedAIP aip : collections.getResults()) {
          array.add(aipToJSON(aip));
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

  /**
   * @deprecated this method should be replaced by a specialized class to
   *             marshal and unmarshal a classification plans
   */
  @Deprecated
  public static ObjectNode aipToJSON(IndexedAIP indexedAIP)
    throws IOException, RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    ModelService model = RodaCoreFactory.getModelService();

    ObjectNode node = mapper.createObjectNode();
    if (indexedAIP.getTitle() != null) {
      node = node.put("title", indexedAIP.getTitle());
    }
    if (indexedAIP.getId() != null) {
      node = node.put("id", indexedAIP.getId());
    }
    if (indexedAIP.getParentID() != null) {
      node = node.put("parentId", indexedAIP.getParentID());
    }
    if (indexedAIP.getLevel() != null) {
      node = node.put("descriptionlevel", indexedAIP.getLevel());
    }

    AIP modelAIP = model.retrieveAIP(indexedAIP.getId());
    if (modelAIP != null) {
      List<DescriptiveMetadata> descriptiveMetadata = modelAIP.getDescriptiveMetadata();
      if (descriptiveMetadata != null && !descriptiveMetadata.isEmpty()) {
        ArrayNode metadata = mapper.createArrayNode();
        for (DescriptiveMetadata dm : descriptiveMetadata) {
          ObjectNode dmNode = mapper.createObjectNode();
          if (dm.getId() != null) {
            dmNode = dmNode.put("id", dm.getId());
          }
          if (dm.getType() != null) {
            dmNode = dmNode.put("metadataType", dm.getType());
          }
          Binary b = model.retrieveDescriptiveMetadataBinary(modelAIP.getId(), dm.getId());
          InputStream is = b.getContent().createInputStream();
          dmNode = dmNode.put("content", new String(Base64.encodeBase64(IOUtils.toByteArray(is))));
          IOUtils.closeQuietly(is);
          dmNode = dmNode.put("contentEncoding", "Base64");
          metadata = metadata.add(dmNode);
        }
        node.set("metadata", metadata);
      }
    }
    return node;
  }

  public static List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(User user, IndexedAIP aip, Locale locale)
    throws GenericException {
    LOGGER.error("retrieveSupportedMetadata");
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    List<String> types = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.UI_BROWSER_METADATA_DESCRIPTIVE_TYPES));

    List<SupportedMetadataTypeBundle> supportedMetadata = new ArrayList<>();

    if (types != null) {
      for (String id : types) {
        String type = id;
        String version = null;
        if (id.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
          version = id.substring(id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1, id.length());
          type = id.substring(0, id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
        }
        String key = RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + type;
        if (version != null) {
          key += RodaConstants.METADATA_VERSION_SEPARATOR + version;
        }
        String label = messages.getTranslation(key, type);
        InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.METADATA_TEMPLATE_FOLDER
          + "/" + ((version != null) ? type + RodaConstants.METADATA_VERSION_SEPARATOR + version : type)
          + RodaConstants.METADATA_TEMPLATE_EXTENSION);

        String template = null;
        Set<MetadataValue> values = null;
        if (templateStream != null) {
          try {
            template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
            values = ServerTools.transform(template);
            for (MetadataValue mv : values) {
              String generator = mv.get("auto-generate");
              if (generator != null && generator.length() > 0) {
                String value = ServerTools.autoGenerateValue(aip, user, generator);
                if (value != null) {
                  mv.set("value", value);
                }
              }
              String labels = mv.get("label");
              String labelI18N = mv.get("labeli18n");
              if (labels != null && labelI18N != null) {
                Map<String, String> labelsMaps = JsonUtils.getMapFromJson(labels);
                try {
                  labelsMaps.put(locale.toString(), RodaCoreFactory.getI18NMessages(locale).getTranslation(labelI18N));
                } catch (MissingResourceException e) {
                  LOGGER.debug("Missing resource: {}", labelI18N);
                }
                labels = JsonUtils.getJsonFromObject(labelsMaps);
                mv.set("label", labels);
              }

              String i18nPrefix = mv.get("optionsLabelI18nKeyPrefix");
              if (i18nPrefix != null) {
                Map<String, String> terms = messages.getTranslations(i18nPrefix, String.class, false);
                if (terms.size() > 0) {
                  try {
                    String options = mv.get("options");
                    List<String> optionsList = JsonUtils.getListFromJson(options, String.class);

                    if (optionsList != null) {
                      Map<String, Map<String, String>> i18nMap = new HashMap<String, Map<String, String>>();
                      for (int i = 0; i < optionsList.size(); i++) {
                        String value = optionsList.get(i);
                        String translation = terms.get(i18nPrefix + "." + value);
                        if (translation == null) {
                          translation = value;
                        }
                        Map<String, String> term = new HashMap<String, String>();
                        term.put(locale.toString(), translation);
                        i18nMap.put(value, term);
                      }
                      mv.set("optionsLabels", JsonUtils.getJsonFromObject(i18nMap));
                    }
                  } catch (MissingResourceException e) {
                    LOGGER.error(e.getMessage(), e);
                  }
                }
              }

            }
          } catch (IOException e) {
            LOGGER.error("Error getting the template from the stream", e);
          }
        }

        supportedMetadata.add(new SupportedMetadataTypeBundle(id, type, version, label, template, values));
      }
    }
    return supportedMetadata;
  }

  public static EntityResponse retrieveTransferredResource(final TransferredResource transferredResource,
    String acceptFormat) throws NotFoundException, RequestNotValidException, GenericException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
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
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      return new ObjectResponse(acceptFormat, transferredResource);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
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
          LOGGER.error("Error getting agent {}: {}", agentID, e.getMessage());
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
      RODA_TYPE linkingType = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      try {
        if (RODA_TYPE.AIP.equals(linkingType)) {
          String uuid = LinkingObjectUtils.getAipIdFromLinkingId(idValue);
          IndexedAIP aip = retrieve(IndexedAIP.class, uuid);
          aips.put(idValue, aip);
        } else if (RODA_TYPE.REPRESENTATION.equals(linkingType)) {
          String uuid = LinkingObjectUtils.getRepresentationIdFromLinkingId(idValue);
          IndexedRepresentation rep = retrieve(IndexedRepresentation.class, uuid);
          representations.put(idValue, rep);
        } else if (RODA_TYPE.FILE.equals(linkingType)) {
          IndexedFile file = retrieve(IndexedFile.class, LinkingObjectUtils.getFileIdFromLinkingId(idValue));
          files.put(idValue, file);
        } else if (RODA_TYPE.TRANSFERRED_RESOURCE.equals(linkingType)) {
          String id = LinkingObjectUtils.getTransferredResourceIdFromLinkingId(idValue);
          String uuid = UUID.nameUUIDFromBytes(id.getBytes()).toString();
          TransferredResource tr = retrieve(TransferredResource.class, uuid);
          transferredResources.put(idValue, tr);
        } else {
          LOGGER.warn("No support for linking object type: {}", linkingType);
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
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, descriptiveMetadataId);
    return RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);

  }

  public static DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId,
    String metadataId, Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataVersionsBundle bundle = new DescriptiveMetadataVersionsBundle();

    IndexedAIP aip = retrieve(IndexedAIP.class, aipId);
    DescriptiveMetadataViewBundle descriptiveMetadataBundle = retrieveDescriptiveMetadataBundle(aipId, metadataId,
      locale);

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

  public static void deleteDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, descriptiveMetadataId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }

  public static void updateAIPPermissions(User user, IndexedAIP indexedAIP, Permissions permissions, boolean recursive)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    final ModelService model = RodaCoreFactory.getModelService();
    AIP aip = model.retrieveAIP(indexedAIP.getId());
    aip.setPermissions(permissions);
    model.updateAIPPermissions(aip, user.getName());

    if (recursive) {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, indexedAIP.getId()));
      RodaCoreFactory.getIndexService().execute(IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

        @Override
        public void run(IndexedAIP idescendant)
          throws GenericException, RequestNotValidException, AuthorizationDeniedException {
          AIP descendant;
          try {
            descendant = model.retrieveAIP(idescendant.getId());
            descendant.setPermissions(permissions);
            model.updateAIPPermissions(descendant, user.getName());
          } catch (NotFoundException e) {
            LOGGER.warn("Got an AIP from index which was not found in the model", e);
          } catch (RuntimeException e) {
            LOGGER.error("Error applying permissions", e);
          }

        }
      });
    }

  }

  public static Risk createRisk(Risk risk, User user, boolean commit)
    throws GenericException, RequestNotValidException {
    risk.setCreatedBy(user.getName());
    risk.setUpdatedBy(user.getName());
    Risk createdRisk = RodaCoreFactory.getModelService().createRisk(risk, commit);
    RodaCoreFactory.getIndexService().commit(IndexedRisk.class);
    return createdRisk;
  }

  public static void updateRisk(Risk risk, User user, String message, boolean commit)
    throws GenericException, RequestNotValidException {
    risk.setUpdatedBy(user.getName());
    RodaCoreFactory.getModelService().updateRisk(risk, message, commit);
    RodaCoreFactory.getIndexService().commit(IndexedRisk.class);
  }

  public static Format createFormat(Format format, boolean commit) throws GenericException, RequestNotValidException {
    Format createdFormat = RodaCoreFactory.getModelService().createFormat(format, commit);
    RodaCoreFactory.getIndexService().commit(Format.class);
    return createdFormat;
  }

  public static void updateFormat(Format format, boolean commit) throws GenericException, RequestNotValidException {
    RodaCoreFactory.getModelService().updateFormat(format, commit);
    RodaCoreFactory.getIndexService().commit(Format.class);
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
    RodaCoreFactory.getModelService().revertRiskVersion(riskId, versionId, message, false);
  }

  public static void deleteRiskVersion(String riskId, String versionId)
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

  public static void validateExportAIPParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
  }

  public static List<IndexedAIP> matchAIP(Filter filter, User user) throws GenericException, RequestNotValidException {
    List<IndexedAIP> aips = new ArrayList<IndexedAIP>();
    long count = count(IndexedAIP.class, filter, user);
    boolean justActive = true;
    for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
      Sorter sorter = new Sorter(new SortParameter(RodaConstants.AIP_ID, true));
      IndexResult<IndexedAIP> res = find(IndexedAIP.class, filter, sorter,
        new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), Facets.NONE, user, justActive);
      aips.addAll(res.getResults());
    }
    return aips;
  }

  public static void validateListingParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: " + Arrays
          .asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }

  }

  public static void validateCreateAndUpdateParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException(
        "Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT + "' value. Expected values: " + Arrays
          .asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }

  }

  public static void validateGetAIPParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP));
    }

  }

  public static <T extends IsIndexed> RODAObjectList<?> retrieveObjects(Class<T> objectClass, int start, int limit,
    String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    IndexResult<T> result = RodaCoreFactory.getIndexService().find(objectClass, Filter.NULL, Sorter.NONE,
      new Sublist(start, limit));

    if (objectClass.equals(IndexedAIP.class)) {
      AIPs aips = new AIPs();
      for (T object : result.getResults()) {
        IndexedAIP aip = (IndexedAIP) object;
        aips.addObject(RodaCoreFactory.getModelService().retrieveAIP(aip.getId()));
      }
      return aips;
    } else if (objectClass.equals(IndexedRepresentation.class)) {
      Representations representations = new Representations();
      for (T object : result.getResults()) {
        IndexedRepresentation representation = (IndexedRepresentation) object;
        representations.addObject(
          RodaCoreFactory.getModelService().retrieveRepresentation(representation.getAipId(), representation.getId()));
      }
      return representations;
    } else if (objectClass.equals(IndexedFile.class)) {
      org.roda.core.data.v2.ip.Files files = new org.roda.core.data.v2.ip.Files();
      for (T object : result.getResults()) {
        IndexedFile file = (IndexedFile) object;
        files.addObject(RodaCoreFactory.getModelService().retrieveFile(file.getAipId(), file.getRepresentationId(),
          file.getPath(), file.getId()));
      }
      return files;
    } else if (objectClass.equals(IndexedRisk.class)) {
      List<Risk> risks = new ArrayList<Risk>();
      for (T res : result.getResults()) {
        IndexedRisk irisk = (IndexedRisk) res;
        risks.add(irisk);
      }
      return new org.roda.core.data.v2.risks.Risks(risks);
    } else if (objectClass.equals(TransferredResource.class)) {
      return new org.roda.core.data.v2.ip.TransferredResources((List<TransferredResource>) result.getResults());
    } else if (objectClass.equals(Format.class)) {
      return new org.roda.core.data.v2.formats.Formats((List<Format>) result.getResults());
    } else if (objectClass.equals(Notification.class)) {
      return new org.roda.core.data.v2.notifications.Notifications((List<Notification>) result.getResults());
    } else if (objectClass.equals(LogEntry.class)) {
      return new org.roda.core.data.v2.log.LogEntries((List<LogEntry>) result.getResults());
    } else if (objectClass.equals(RiskIncidence.class)) {
      return new org.roda.core.data.v2.risks.RiskIncidences((List<RiskIncidence>) result.getResults());
    } else if (objectClass.equals(RODAMember.class)) {
      return new org.roda.core.data.v2.user.RODAMembers((List<RODAMember>) result.getResults());
    } else {
      throw new GenericException("Unsupported object class: " + objectClass);
    }

  }

  public static EntityResponse retrieveAIP(IndexedAIP indexedAIP, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      StoragePath storagePath = ModelUtils.getAIPStoragePath(indexedAIP.getId());
      StorageService storage = RodaCoreFactory.getStorageService();
      Directory directory = storage.getDirectory(storagePath);
      return download(directory);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      AIP aip = RodaCoreFactory.getModelService().retrieveAIP(indexedAIP.getId());
      return new ObjectResponse<AIP>(acceptFormat, aip);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static StreamResponse retrieveAIPPart(IndexedAIP aip, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    String aipId = aip.getId();

    if (RodaConstants.STORAGE_DIRECTORY_SUBMISSION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSubmissionDirectory(aipId);
      return download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId);
      return download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId);
      return download(directory);
    } else {
      throw new GenericException("Unsupported part: " + part);
    }
  }

  public static StreamResponse retrieveAIPs(SelectedItems<IndexedAIP> selected, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, IOException {
    IndexService index = RodaCoreFactory.getIndexService();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      List<ZipEntryInfo> zipEntries = new ArrayList<ZipEntryInfo>();
      if (selected instanceof SelectedItemsFilter) {
        SelectedItemsFilter<IndexedAIP> selectedItems = (SelectedItemsFilter<IndexedAIP>) selected;
        long count = index.count(IndexedAIP.class, selectedItems.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItems.getFilter(), null,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          zipEntries.addAll(ModelUtils.zipIndexedAIP(aips));
        }
      } else {
        SelectedItemsList<IndexedAIP> selectedItems = (SelectedItemsList<IndexedAIP>) selected;
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

  public static void deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    List<String> idList = consolidate(user, IndexedRisk.class, selected);

    for (String riskId : idList) {
      RodaCoreFactory.getModelService().deleteRisk(riskId, true);
    }

    Job job = new Job();
    job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
    job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
    job.setSourceObjects(SelectedItemsList.create(Risk.class, idList));
    Jobs.createJob(user, job);
  }

  public static void deleteFormat(User user, SelectedItems<Format> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    List<String> idList = consolidate(user, Format.class, selected);
    for (String formatId : idList) {
      RodaCoreFactory.getModelService().deleteFormat(formatId, true);
    }
  }

  public static void updateRiskCounters() throws GenericException, RequestNotValidException, NotFoundException {
    IndexResult<RiskIncidence> find = RodaCoreFactory.getIndexService().find(RiskIncidence.class, Filter.ALL,
      Sorter.NONE, new Sublist(0, 0), new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)));

    boolean findFlag = true;
    int initialIndex = 0, interval = 20;
    List<String> allRisks = new ArrayList<String>();
    Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.RISK_OBJECTS_SIZE, "0"));

    while (findFlag) {
      IndexResult<IndexedRisk> findAll = RodaCoreFactory.getIndexService().find(IndexedRisk.class, filter, Sorter.NONE,
        new Sublist(initialIndex, initialIndex + interval));

      for (IndexedRisk risk : findAll.getResults()) {
        allRisks.add(risk.getId());
      }

      if (findAll.getResults().size() < interval) {
        findFlag = false;
      } else {
        initialIndex += interval;
      }
    }

    for (FacetFieldResult fieldResult : find.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = RodaCoreFactory.getIndexService().retrieve(IndexedRisk.class, riskId);
        risk.setObjectsSize((int) counter);
        RodaCoreFactory.getIndexService().reindexRisk(risk);

        allRisks.remove(risk.getId());
      }
    }

    for (String riskId : allRisks) {
      Filter riskFilter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_ID, riskId));
      IndexResult<IndexedRisk> findAll = RodaCoreFactory.getIndexService().find(IndexedRisk.class, riskFilter,
        Sorter.NONE, new Sublist(0, 1));
      IndexedRisk risk = findAll.getResults().get(0);
      risk.setObjectsSize(0);
      RodaCoreFactory.getIndexService().reindexRisk(risk);
    }

    RodaCoreFactory.getIndexService().commit(IndexedRisk.class);
  }

  public static void appraisal(User user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    List<String> listOfIds = consolidate(user, IndexedAIP.class, selected);

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    Date now = new Date();

    // map of job id -> (total, accepted)
    Map<String, Pair<Integer, Integer>> jobState = new HashMap<>();
    List<String> aipsToDelete = new ArrayList<>();

    String userAgentId;
    try {
      boolean notifyAgent = true;
      PreservationMetadata pm = PremisV3Utils.createPremisUserAgentBinary(user.getName(), model, index, notifyAgent);
      userAgentId = pm.getId();
    } catch (AlreadyExistsException e) {
      userAgentId = IdUtils.getUserAgentId(user.getName());
    } catch (ValidationException e) {
      throw new GenericException(e);
    }

    for (String aipId : listOfIds) {
      AIP aip = model.retrieveAIP(aipId);
      String jobId = aip.getIngestJobId();
      if (accept) {
        // Accept AIP
        aip.setState(AIPState.ACTIVE);
        model.updateAIPState(aip, user.getName());

        // create preservation event
        String id = IdUtils.createPreservationMetadataId(PreservationMetadataType.EVENT);
        PreservationEventType type = PreservationEventType.ACCESSION;
        String preservationEventDescription = AutoAcceptSIPPlugin.DESCRIPTION;
        List<LinkingIdentifier> sources = new ArrayList<>();
        List<LinkingIdentifier> outcomes = Arrays
          .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
        PluginState outcome = PluginState.SUCCESS;
        String outcomeDetailNote = AutoAcceptSIPPlugin.SUCCESS_MESSAGE;
        String outcomeDetailExtension = null;
        boolean notifyEvent = true;
        try {
          ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, now, type.toString(),
            preservationEventDescription, sources, outcomes, outcome.name(), outcomeDetailNote, outcomeDetailExtension,
            Arrays.asList(userAgentId));

          model.createPreservationMetadata(PreservationMetadataType.EVENT, id, aipId, null, null, null, premisEvent,
            notifyEvent);
        } catch (AlreadyExistsException | ValidationException e) {
          throw new GenericException(e);
        }

      } else {
        // Reject AIP
        model.deleteAIP(aipId);
        aipsToDelete.add(aipId);
      }

      // create job report
      Report report = model.retrieveJobReport(jobId, aipId);

      Report reportItem = new Report();
      reportItem.setTitle("Manual appraisal");
      reportItem.setPlugin(user.getName());
      reportItem.setPluginDetails(rejectReason);
      reportItem.setPluginState(accept ? PluginState.SUCCESS : PluginState.FAILURE);
      reportItem.setOutcomeObjectState(accept ? AIPState.ACTIVE : AIPState.DELETED);
      reportItem.setDateCreated(now);
      report.addReport(reportItem);

      model.createOrUpdateJobReport(report);

      // save job state
      Pair<Integer, Integer> pair = jobState.get(jobId);
      if (pair == null) {
        jobState.put(jobId, Pair.create(1, accept ? 1 : 0));
      } else {
        jobState.put(jobId, Pair.create(pair.getFirst() + 1, pair.getSecond() + (accept ? 1 : 0)));
      }

    }

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(AIP.class, aipsToDelete));
      Jobs.createJob(user, job);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete AIP assoaciated incidences");
    }

    // update job counters
    for (Entry<String, Pair<Integer, Integer>> entry : jobState.entrySet()) {
      String jobId = entry.getKey();
      int total = entry.getValue().getFirst();
      int accepted = entry.getValue().getSecond();
      int rejected = total - accepted;
      Job job = model.retrieveJob(jobId);
      if (rejected > 0) {
        // change counter to failure
        job.getJobStats()
          .setSourceObjectsProcessedWithSuccess(job.getJobStats().getSourceObjectsProcessedWithSuccess() - rejected);
        job.getJobStats()
          .setSourceObjectsProcessedWithFailure(job.getJobStats().getSourceObjectsProcessedWithFailure() + rejected);
      }

      // decrement manual interaction counter
      job.getJobStats()
        .setOutcomeObjectsWithManualIntervention(job.getJobStats().getOutcomeObjectsWithManualIntervention() - total);

      model.createOrUpdateJob(job);

    }

    RodaCoreFactory.getIndexService().commit(IndexedAIP.class, Job.class, Report.class, IndexedPreservationEvent.class);
  }

  public static IndexedRepresentation retrieveRepresentationById(User user, String representationId)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_ID, representationId));
    IndexResult<IndexedRepresentation> reps = RodaCoreFactory.getIndexService().find(IndexedRepresentation.class,
      filter, Sorter.NONE, new Sublist(0, 1));

    if (reps.getResults().isEmpty()) {
      return null;
    } else {
      return reps.getResults().get(0);
    }
  }

  public static IndexedFile retrieveFileById(User user, String fileId)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_FILE_ID, fileId));
    IndexResult<IndexedFile> files = RodaCoreFactory.getIndexService().find(IndexedFile.class, filter, Sorter.NONE,
      new Sublist(0, 1));

    if (files.getResults().isEmpty()) {
      return null;
    } else {
      return files.getResults().get(0);
    }
  }

  public static String retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle) throws GenericException {
    String rawTemplate = bundle.getTemplate();
    String result;
    if (StringUtils.isNotBlank(rawTemplate)) {
      try {
        Handlebars handlebars = new Handlebars();
        Map<String, String> data = new HashMap<>();
        handlebars.registerHelper("field", (o, options) -> {
          return options.fn();
        });
        handlebars.registerHelper("ifCond", (context, options) -> {
          // the first parameter of ifCond is placed in the context field by the
          // parser
          String condition = (context == null) ? "||" : context.toString();
          List<Object> values = Arrays.asList(options.params);
          boolean display;
          if (condition.equals("||")) {
            display = false;
            for (Object value : values) {
              if (value != null) {
                display = true;
                break;
              }
            }
          } else if (condition.equals("&&")) {
            display = true;
            for (Object value : values) {
              if (value == null) {
                display = false;
                break;
              }
            }
          } else {
            display = false;
          }
          return display ? options.fn() : options.inverse();
        });

        Template tmpl = handlebars.compileInline(rawTemplate);

        Set<MetadataValue> values = bundle.getValues();
        if (values != null) {
          values.forEach(metadataValue -> {
            String val = metadataValue.get("value");
            if (val != null) {
              val = val.replaceAll("\\s", "");
              if (!"".equals(val)) {
                data.put(metadataValue.get("name"), metadataValue.get("value"));
              }
            }
          });
        }
        result = tmpl.apply(data);
        // result = RodaUtils.indentXML(result);
      } catch (IOException e) {
        throw new GenericException(e);
      }
    } else {
      result = rawTemplate;
    }
    return result;
  }

  public static String renameTransferredResource(String transferredResourceId, String newName) throws GenericException,
    RequestNotValidException, AlreadyExistsException, IsStillUpdatingException, NotFoundException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, transferredResourceId));
    IndexResult<TransferredResource> resources = RodaCoreFactory.getIndexService().find(TransferredResource.class,
      filter, Sorter.NONE, new Sublist(0, 1));

    if (!resources.getResults().isEmpty()) {
      TransferredResource resource = (TransferredResource) resources.getResults().get(0);
      return RodaCoreFactory.getTransferredResourcesScanner().renameTransferredResource(resource, newName, true, true);
    } else {
      return transferredResourceId;
    }
  }

  public static String moveTransferredResource(SelectedItems selected, TransferredResource transferredResource)
    throws GenericException, RequestNotValidException, AlreadyExistsException, IsStillUpdatingException,
    NotFoundException {

    String resourceRelativePath = "";
    Filter filter = new Filter();
    int counter = 1;

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList selectedList = (SelectedItemsList) selected;
      filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      counter = selectedList.getIds().size();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter selectedFilter = (SelectedItemsFilter) selected;
      filter = selectedFilter.getFilter();
      counter = RodaCoreFactory.getIndexService().count(TransferredResource.class, filter).intValue();
    }

    IndexResult<TransferredResource> resources = RodaCoreFactory.getIndexService().find(TransferredResource.class,
      filter, Sorter.NONE, new Sublist(0, counter));

    if (transferredResource != null) {
      resourceRelativePath = transferredResource.getRelativePath();
    }

    Map<String, String> moveMap = RodaCoreFactory.getTransferredResourcesScanner()
      .moveTransferredResource(resources.getResults(), resourceRelativePath, true, true, true);

    if (!moveMap.isEmpty()) {
      List<String> values = new ArrayList(moveMap.values());
      return values.get(0);
    } else {
      return transferredResource.getUUID();
    }

  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(
    SelectedItems<TransferredResource> selected) throws GenericException, RequestNotValidException {
    if (selected instanceof SelectedItemsList) {
      SelectedItemsList selectedList = (SelectedItemsList) selected;

      Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      IndexResult<TransferredResource> iresults = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()));
      return iresults.getResults();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter selectedFilter = (SelectedItemsFilter) selected;

      Long counter = RodaCoreFactory.getIndexService().count(TransferredResource.class, selectedFilter.getFilter());
      IndexResult<TransferredResource> iresults = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter.intValue()));
      return iresults.getResults();
    } else {
      return new ArrayList<TransferredResource>();
    }
  }

  public static void updateRiskIncidence(RiskIncidence incidence) throws GenericException {
    RodaCoreFactory.getModelService().updateRiskIncidence(incidence, true);
  }

  protected static Reports listReports(String start, String limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();

    IndexService indexService = RodaCoreFactory.getIndexService();
    int reportCounter = indexService.count(Report.class, Filter.ALL).intValue();
    int endInt = limitInt == -1 ? reportCounter : (limitInt > reportCounter ? reportCounter : limitInt);

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<Report> indexReports = indexService.find(Report.class, Filter.ALL, sorter,
      new Sublist(startInt, endInt));
    return new Reports(indexReports.getResults());
  }

  protected static Reports listTransferredResourcesReports(String resourceId, String start, String limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();

    IndexService indexService = RodaCoreFactory.getIndexService();
    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, TransferredResource.class.getName()));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, resourceId));

    int reportCounter = indexService.count(Report.class, filter).intValue();
    int endInt = limitInt == -1 ? reportCounter : (limitInt > reportCounter ? reportCounter : limitInt);

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<Report> indexReports = indexService.find(Report.class, filter, sorter, new Sublist(startInt, endInt));
    return new Reports(indexReports.getResults());
  }

  protected static Reports listTransferredResourcesReportsWithSIP(String sipId, String start, String limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);
    int startInt = pagingParams.getFirst();
    int limitInt = pagingParams.getSecond();

    IndexService indexService = RodaCoreFactory.getIndexService();

    Filter sipFilter = new Filter();
    sipFilter.add(new SimpleFilterParameter(RodaConstants.INGEST_SIP_ID, sipId));

    int aipCounter = indexService.count(IndexedAIP.class, sipFilter).intValue();
    IndexResult<IndexedAIP> aips = indexService.find(IndexedAIP.class, sipFilter, Sorter.NONE,
      new Sublist(startInt, aipCounter));

    List<String> aipIds = aips.getResults().stream().map((IndexedAIP aip) -> aip.getId()).collect(Collectors.toList());

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS,
      Arrays.asList(AIP.class.getName(), IndexedAIP.class.getName())));
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, aipIds));

    int reportCounter = indexService.count(Report.class, filter).intValue();
    int endInt = limitInt == -1 ? reportCounter : (limitInt > reportCounter ? reportCounter : limitInt);

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<Report> indexReports = indexService.find(Report.class, filter, sorter, new Sublist(startInt, endInt));
    return new Reports(indexReports.getResults());
  }

  public static void deleteRiskIncidences(User user, SelectedItems<RiskIncidence> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    List<String> idList = consolidate(user, RiskIncidence.class, selected);
    for (String incidenceId : idList) {
      RodaCoreFactory.getModelService().deleteRiskIncidence(incidenceId, true);
    }
  }
}
