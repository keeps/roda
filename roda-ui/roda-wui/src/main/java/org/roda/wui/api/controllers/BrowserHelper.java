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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ClassificationPlanUtils;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.common.IdUtils;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.monitor.TransferredResourcesScanner;
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
import org.roda.core.data.v2.common.ObjectPermission;
import org.roda.core.data.v2.common.ObjectPermissionResult;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
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
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
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
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.risks.RiskIncidenceRemoverPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ObjectResponse;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.bundle.BinaryVersionBundle;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.server.common.XMLSimilarityIgnoreElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class BrowserHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);

  protected static BrowseAIPBundle retrieveBrowseAipBundle(IndexedAIP aip, Locale locale)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    BrowseAIPBundle bundle = new BrowseAIPBundle();

    // set aip
    bundle.setAIP(aip);
    String aipId = aip.getId();

    // set aip ancestors
    try {
      bundle.setAIPAncestors(retrieveAncestors(aip));
    } catch (NotFoundException e) {
      LOGGER.warn("Found an item with invalid ancestors: {}", aip.getId(), e);
    }

    // set descriptive metadata
    try {
      List<DescriptiveMetadataViewBundle> descriptiveMetadataList = retrieveDescriptiveMetadataBundles(aipId, locale);
      bundle.setDescriptiveMetadata(descriptiveMetadataList);
    } catch (NotFoundException e) {
      // do nothing
    }

    // Count child AIPs
    Filter childAIPfilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId()));
    Long childAIPCount = RodaCoreFactory.getIndexService().count(IndexedAIP.class, childAIPfilter);
    bundle.setChildAIPCount(childAIPCount);

    // Count representations
    Filter repFilter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    Long repCount = RodaCoreFactory.getIndexService().count(IndexedRepresentation.class, repFilter);
    bundle.setRepresentationCount(repCount);

    // Count DIPs
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
    Long dipCount = RodaCoreFactory.getIndexService().count(IndexedDIP.class, dipsFilter);
    bundle.setDipCount(dipCount);

    return bundle;
  }

  public static BrowseRepresentationBundle retrieveBrowseRepresentationBundle(IndexedAIP aip,
    IndexedRepresentation representation, Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    BrowseRepresentationBundle bundle = new BrowseRepresentationBundle();

    bundle.setAip(aip);
    bundle.setRepresentation(representation);

    // set aip ancestors
    try {
      bundle.setAipAncestors(retrieveAncestors(aip));
    } catch (NotFoundException e) {
      LOGGER.warn("Found an item with invalid ancestors: {}", aip.getId(), e);
    }

    // set representation desc. metadata
    try {
      bundle.setRepresentationDescriptiveMetadata(
        retrieveDescriptiveMetadataBundles(aip.getId(), representation.getId(), locale));
    } catch (NotFoundException e) {
      // do nothing
    }

    // Count DIPs
    Filter dipsFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
    Long dipCount = RodaCoreFactory.getIndexService().count(IndexedDIP.class, dipsFilter);
    bundle.setDipCount(dipCount);

    return bundle;
  }

  public static BrowseFileBundle retrieveBrowseFileBundle(IndexedAIP aip, String representationId,
    List<String> filePath, String fileId, Locale locale, User user)
    throws NotFoundException, GenericException, RequestNotValidException {
    BrowseFileBundle bundle = new BrowseFileBundle();

    bundle.setAip(aip);
    bundle.setRepresentation(
      retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aip.getId(), representationId)));
    String fileUUID = IdUtils.getFileId(aip.getId(), representationId, filePath, fileId);
    bundle.setFile(retrieve(IndexedFile.class, fileUUID));

    // set aip ancestors
    try {
      bundle.setAipAncestors(retrieveAncestors(aip));
    } catch (NotFoundException e) {
      LOGGER.warn("Found an item with invalid ancestors: {}", aip.getId(), e);
    }

    // set sibling count
    String parentUUID = bundle.getFile().getParentUUID();

    Filter siblingFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, bundle.getFile().getRepresentationUUID()));

    if (parentUUID != null) {
      siblingFilter.add(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, parentUUID));
    } else {
      siblingFilter.add(new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));
    }

    bundle.setTotalSiblingCount(count(IndexedFile.class, siblingFilter, user));

    // Count DIPs
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, fileUUID));
    Long dipCount = RodaCoreFactory.getIndexService().count(IndexedDIP.class, dipsFilter);
    bundle.setDipCount(dipCount);

    return bundle;
  }

  private static List<DescriptiveMetadataViewBundle> retrieveDescriptiveMetadataBundles(String aipId, Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    return retrieveDescriptiveMetadataBundles(aipId, null, locale);
  }

  private static List<DescriptiveMetadataViewBundle> retrieveDescriptiveMetadataBundles(String aipId,
    String representationId, final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    ModelService model = RodaCoreFactory.getModelService();
    List<DescriptiveMetadata> listDescriptiveMetadataBinaries;
    if (representationId != null) {
      listDescriptiveMetadataBinaries = model.retrieveRepresentation(aipId, representationId).getDescriptiveMetadata();
    } else {
      listDescriptiveMetadataBinaries = model.retrieveAIP(aipId).getDescriptiveMetadata();
    }
    List<DescriptiveMetadataViewBundle> descriptiveMetadataList = new ArrayList<>();

    if (listDescriptiveMetadataBinaries != null) { // Can be null when the AIP
                                                   // is a ghost
      for (DescriptiveMetadata descriptiveMetadata : listDescriptiveMetadataBinaries) {
        DescriptiveMetadataViewBundle bundle = retrieveDescriptiveMetadataBundle(aipId, representationId,
          descriptiveMetadata, locale);
        descriptiveMetadataList.add(bundle);
      }
    }

    return descriptiveMetadataList;
  }

  private static DescriptiveMetadataViewBundle retrieveDescriptiveMetadataBundle(String aipId, String representationId,
    DescriptiveMetadata descriptiveMetadata, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    DescriptiveMetadataViewBundle bundle = new DescriptiveMetadataViewBundle();
    bundle.setId(descriptiveMetadata.getId());

    if (descriptiveMetadata.getType() != null) {
      try {
        String labelWithoutVersion = messages.getTranslation(
          RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase());
        if (descriptiveMetadata.getVersion() != null) {
          String labelWithVersion = messages.getTranslation(
            RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase()
              + RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadata.getVersion().toLowerCase(),
            labelWithoutVersion);
          bundle.setLabel(labelWithVersion);
        } else {
          bundle.setLabel(labelWithoutVersion);
        }

      } catch (MissingResourceException e) {
        bundle.setLabel(descriptiveMetadata.getId());
      }
    }

    try {
      StoragePath storagePath;
      if (representationId != null) {
        storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
          descriptiveMetadata.getId());
      } else {
        storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, descriptiveMetadata.getId());
      }

      bundle.setHasHistory(!CloseableIterables.isEmpty(model.getStorage().listBinaryVersions(storagePath)));

    } catch (RODAException | RuntimeException t) {
      bundle.setHasHistory(false);
    }
    return bundle;
  }

  private static DescriptiveMetadataViewBundle retrieveDescriptiveMetadataBundle(String aipId, String representationId,
    String descriptiveMetadataId, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, representationId,
      descriptiveMetadataId);
    return retrieveDescriptiveMetadataBundle(aipId, representationId, descriptiveMetadata, locale);
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, IndexedAIP aip,
    IndexedRepresentation representation, String descriptiveMetadataId, final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String representationId = representation != null ? representation.getId() : null;

    DescriptiveMetadata metadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aip.getId(),
      representationId, descriptiveMetadataId);
    return retrieveDescriptiveMetadataEditBundle(user, aip, representation, descriptiveMetadataId, metadata.getType(),
      metadata.getVersion(), locale);
  }

  public static DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(User user, IndexedAIP aip,
    IndexedRepresentation representation, String descriptiveMetadataId, String type, String version,
    final Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataEditBundle ret;
    InputStream inputStream = null;
    try {
      String representationId = representation != null ? representation.getId() : null;
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aip.getId(), representationId,
        descriptiveMetadataId);
      inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream, "UTF-8");

      // Get the supported metadata type with the same type and version
      // We need this to try to get get the values for the form
      SupportedMetadataTypeBundle metadataTypeBundle = null;
      List<SupportedMetadataTypeBundle> supportedMetadataTypeBundles = BrowserHelper.retrieveSupportedMetadata(user,
        aip, representation, locale);
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
            LOGGER.warn("Could not check if template can loose info", e);
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

  public static DipBundle retrieveDipBundle(String dipUUID, String dipFileUUID, String aipId, String representationId,
    List<String> filePath, String fileId) throws GenericException, NotFoundException {
    DipBundle bundle = new DipBundle();

    bundle.setDip(BrowserHelper.retrieve(IndexedDIP.class, dipUUID));

    if (dipFileUUID != null) {
      DIPFile dipFile = BrowserHelper.retrieve(DIPFile.class, dipFileUUID);
      bundle.setDipFile(dipFile);

      List<DIPFile> dipFileAncestors = new ArrayList<>();
      for (String dipFileAncestor : dipFile.getAncestorsPath()) {
        try {
          dipFileAncestors.add(BrowserHelper.retrieve(DIPFile.class, dipFileAncestor));
        } catch (NotFoundException e) {
          // ignore
        }
      }
      bundle.setDipFileAncestors(dipFileAncestors);
    }

    if (aipId == null && representationId == null && fileId == null) {
      // infer from DIP
      IndexedDIP dip = bundle.getDip();
      if (!dip.getFileIds().isEmpty()) {
        IndexedFile file = BrowserHelper.retrieve(IndexedFile.class, IdUtils.getFileId(dip.getFileIds().get(0)));
        bundle.setFile(file);
        bundle.setRepresentation(BrowserHelper.retrieve(IndexedRepresentation.class, file.getRepresentationUUID()));
        bundle.setAip(BrowserHelper.retrieve(IndexedAIP.class, file.getAipId()));
      } else if (!dip.getRepresentationIds().isEmpty()) {
        IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
          IdUtils.getRepresentationId(dip.getRepresentationIds().get(0)));
        bundle.setRepresentation(representation);
        bundle.setAip(BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId()));
      } else if (!dip.getAipIds().isEmpty()) {
        IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, dip.getAipIds().get(0).getAipId());
        bundle.setAip(aip);
      }
    }

    if (aipId != null) {
      bundle.setAip(BrowserHelper.retrieve(IndexedAIP.class, aipId));
    }

    if (representationId != null) {
      bundle.setRepresentation(
        BrowserHelper.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, representationId)));
    }

    if (fileId != null) {
      bundle.setFile(
        BrowserHelper.retrieve(IndexedFile.class, IdUtils.getFileId(aipId, representationId, filePath, fileId)));
    }

    return bundle;
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

  protected static <T extends IsIndexed> void commit(Class<T> returnClass) throws GenericException, NotFoundException {
    RodaCoreFactory.getIndexService().commit(returnClass);
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

  protected static <T extends IsIndexed> List<String> suggest(Class<T> returnClass, String field, String query,
    User user, boolean allowPartial) throws GenericException, NotFoundException {
    boolean justActive = true;
    return RodaCoreFactory.getIndexService().suggest(returnClass, field, query, user, allowPartial, justActive);
  }

  public static void validateGetFileParams(String acceptFormat) throws RequestNotValidException {
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

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      StoragePath storagePath = ModelUtils.getRepresentationStoragePath(representation.getAipId(),
        representation.getId());
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return ApiUtils.download(directory);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      ModelService model = RodaCoreFactory.getModelService();
      Representation rep = model.retrieveRepresentation(aipId, representationId);
      return new ObjectResponse<Representation>(acceptFormat, rep);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static StreamResponse retrieveAIPRepresentationPart(IndexedRepresentation representation, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    String aipId = representation.getAipId();
    String representationId = representation.getId();

    if (RodaConstants.STORAGE_DIRECTORY_DATA.equals(part)) {
      StoragePath storagePath = ModelUtils.getRepresentationDataStoragePath(aipId, representationId);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return ApiUtils.download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_METADATA.equals(part)) {
      StoragePath storagePath = ModelUtils.getRepresentationMetadataStoragePath(aipId, representationId);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return ApiUtils.download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId, representationId);
      return ApiUtils.download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId, representationId);
      return ApiUtils.download(directory);
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
      return new ObjectResponse<DescriptiveMetadataList>(acceptFormat, list);
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
    final ConsumesOutputStream stream;
    StreamResponse ret = null;
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
      filename = descriptiveMetadataBinary.getStoragePath().getName();
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_XML;
      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          IOUtils.copy(descriptiveMetadataBinary.getContent().createInputStream(), out);
        }
      };
      ret = new StreamResponse(filename, mediaType, stream);

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
      filename = descriptiveMetadataBinary.getStoragePath().getName() + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, metadataId);
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
        descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));
      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          PrintStream printStream = new PrintStream(out);
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
    final ConsumesOutputStream stream;
    StreamResponse ret = null;
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, representationId, metadataId);
      filename = descriptiveMetadataBinary.getStoragePath().getName();
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_XML;

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          IOUtils.copy(descriptiveMetadataBinary.getContent().createInputStream(), out);
        }
      };

      ret = new StreamResponse(filename, mediaType, stream);

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, representationId, metadataId);
      filename = descriptiveMetadataBinary.getStoragePath().getName() + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, representationId, metadataId);
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
        descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          PrintStream printStream = new PrintStream(out);
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
    final ConsumesOutputStream stream;

    ModelService model = RodaCoreFactory.getModelService();

    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, metadataId);
    BinaryVersion binaryVersion = model.getStorage().getBinaryVersion(storagePath, versionId);
    Binary binary = binaryVersion.getBinary();

    String fileName = binary.getStoragePath().getName();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_XML;

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return fileName;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          IOUtils.copy(binary.getContent().createInputStream(), out);
        }
      };
      return new StreamResponse(fileName, mediaType, stream);

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      filename = fileName + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, metadataId);
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(binary, descriptiveMetadata.getType(),
        descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          PrintStream printStream = new PrintStream(out);
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

  public static EntityResponse retrieveRepresentationDescriptiveMetadataVersion(String aipId, String representationId,
    String metadataId, String versionId, String acceptFormat, String language)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final String filename;
    final String mediaType;
    final ConsumesOutputStream stream;

    ModelService model = RodaCoreFactory.getModelService();

    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId, metadataId);
    BinaryVersion binaryVersion = model.getStorage().getBinaryVersion(storagePath, versionId);
    Binary binary = binaryVersion.getBinary();

    String fileName = binary.getStoragePath().getName();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_XML;

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return binary.getStoragePath().getName();
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          IOUtils.copy(binary.getContent().createInputStream(), out);
        }
      };

      return new StreamResponse(fileName, mediaType, stream);

    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      filename = fileName + ".html";
      DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, representationId, metadataId);
      mediaType = RodaConstants.MEDIA_TYPE_TEXT_HTML;
      String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(binary, descriptiveMetadata.getType(),
        descriptiveMetadata.getVersion(), ServerTools.parseLocale(language));

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return mediaType;
        }

        @Override
        public String getFileName() {
          return fileName + ".html";
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          PrintStream printStream = new PrintStream(out);
          printStream.print(htmlDescriptive);
          printStream.close();
        }
      };

      return new StreamResponse(filename, mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {

      AIP aip = model.retrieveAIP(aipId);
      List<DescriptiveMetadata> resultList = new ArrayList<>();

      for (Representation representation : aip.getRepresentations()) {
        if (representation.getId().equals(representationId)) {
          resultList = representation.getDescriptiveMetadata().stream().filter(dm -> dm.getId().equals(metadataId))
            .collect(Collectors.toList());
        }
      }

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
      return new ObjectResponse<PreservationMetadataList>(acceptFormat, metadataList);
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
      return new ObjectResponse<PreservationMetadataList>(acceptFormat, pms);
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

      ConsumesOutputStream stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return acceptFormat;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          IOUtils.copy(binary.getContent().createInputStream(), out);
        }
      };
      return new StreamResponse(filename, RodaConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      // TODO
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static void createOrUpdateAIPRepresentationPreservationMetadataFile(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, InputStream is, boolean create)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
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

  public static IndexedAIP moveAIPInHierarchy(User user, SelectedItems<IndexedAIP> selected, String parentId,
    String details) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);

    ModelService model = RodaCoreFactory.getModelService();

    for (String aipId : aipIds) {
      // XXX this method could be improved by moving all at once in the model
      if (!aipId.equals(parentId)) {
        // laxing check of ancestry so a big list can be moved to one of the
        // siblings
        LOGGER.debug("Moving AIP {} under {}", aipId, parentId);

        try {
          model.moveAIP(aipId, parentId);

          String outcomeText = "The AIP '" + aipId + "' has been manually moved.";
          model.createUpdateAIPEvent(aipId, null, null, null, PreservationEventType.UPDATE,
            "The process of modifying an object of the repository.", PluginState.SUCCESS, outcomeText, details,
            user.getName(), true);
        } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
          String outcomeText = "The AIP '" + aipId + "' has not been manually moved.";
          model.createUpdateAIPEvent(aipId, null, null, null, PreservationEventType.UPDATE,
            "The process of modifying an object of the repository.", PluginState.FAILURE, outcomeText, details,
            user.getName(), true);

          throw e;
        }
      }
    }

    IndexService index = RodaCoreFactory.getIndexService();
    index.commit(IndexedAIP.class);
    index.commit(IndexedRepresentation.class);
    index.commit(IndexedFile.class);

    return (parentId != null) ? index.retrieve(IndexedAIP.class, parentId) : null;
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

  public static String deleteAIP(User user, SelectedItems<IndexedAIP> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);
    ModelService model = RodaCoreFactory.getModelService();
    String parentId = null;

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(AIP.class, aipIds));
      Jobs.createJob(user, job, false);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete AIP associated incidences");
    }

    for (String aipId : aipIds) {
      try {
        AIP aip = model.retrieveAIP(aipId);
        parentId = aip.getParentId();
        model.deleteAIP(aip.getId());
        deleteAIPEvent(model, user, aipId, details);

        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
        RodaCoreFactory.getIndexService().execute(IndexedAIP.class, filter, new IndexRunnable<IndexedAIP>() {

          @Override
          public void run(IndexedAIP item)
            throws GenericException, RequestNotValidException, AuthorizationDeniedException {
            try {
              model.deleteAIP(item.getId());
              deleteAIPEvent(model, user, aipId, details);
            } catch (NotFoundException e) {
              // already deleted, ignore
            }
          }
        });
      } catch (NotFoundException e) {
        // already deleted, ignore
      }
    }

    RodaCoreFactory.getIndexService().commitAIPs();
    return parentId;
  }

  private static void deleteAIPEvent(ModelService model, User user, String aipId, String details) {
    String outcomeText = "The AIP '" + aipId + "' has been manually deleted.";
    model.createRepositoryEvent(PreservationEventType.DELETION, "The process of deleting an object of the repository.",
      PluginState.SUCCESS, outcomeText, details, user.getName(), true);
  }

  public static void deleteRepresentation(User user, SelectedItems<IndexedRepresentation> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> representationIds = consolidate(user, IndexedRepresentation.class, selected);

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(IndexedRepresentation.class, representationIds));
      Jobs.createJob(user, job, false);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete representation associated incidences");
    }

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, representationIds));
    IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, filter, Sorter.NONE,
      new Sublist(0, representationIds.size()));

    for (IndexedRepresentation rep : reps.getResults()) {
      try {
        model.deleteRepresentation(rep.getAipId(), rep.getId());

        String outcomeText = "The Representation '" + rep.getId() + "' has been manually deleted.";
        model.createUpdateAIPEvent(rep.getAipId(), null, null, null, PreservationEventType.DELETION,
          "The process of deleting an object of the repository.", PluginState.SUCCESS, outcomeText, details,
          user.getName(), true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        String outcomeText = "The Representation '" + rep.getId() + "' has not been manually deleted.";
        model.createUpdateAIPEvent(rep.getAipId(), null, null, null, PreservationEventType.DELETION,
          "The process of deleting an object of the repository.", PluginState.FAILURE, outcomeText, details,
          user.getName(), true);

        throw e;
      }
    }

    index.commit(IndexedRepresentation.class);
  }

  public static void deleteFile(SelectedItems<IndexedFile> selected, User user, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> fileIds = consolidate(user, IndexedFile.class, selected);

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    try {
      Job job = new Job();
      job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
      job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(IndexedFile.class, fileIds));
      Jobs.createJob(user, job, false);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete file associated incidences");
    }

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, fileIds));
    IndexResult<IndexedFile> files = index.find(IndexedFile.class, filter, Sorter.NONE, new Sublist(0, fileIds.size()));

    for (IndexedFile file : files.getResults()) {
      try {
        model.deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), true);

        String outcomeText = "The File '" + file.getId() + "' has been manually deleted.";
        model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null,
          PreservationEventType.DELETION, "The process of deleting an object of the repository.", PluginState.SUCCESS,
          outcomeText, details, user.getName(), true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        String outcomeText = "The File '" + file.getId() + "' has not been manually deleted.";
        model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null,
          PreservationEventType.DELETION, "The process of deleting an object of the repository.", PluginState.FAILURE,
          outcomeText, details, user.getName(), true);

        throw e;
      }
    }

    index.commit(IndexedFile.class);
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
              UserUtility.checkAIPPermissions(user, item, PermissionType.DELETE);
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

  public static DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String representationId,
    String descriptiveMetadataId, String descriptiveMetadataType, String descriptiveMetadataVersion,
    ContentPayload descriptiveMetadataPayload, Map<String, String> properties) throws GenericException,
    AuthorizationDeniedException, ValidationException, RequestNotValidException, NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties);

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

    final ConsumesOutputStream stream = new ConsumesOutputStream() {

      @Override
      public String getMediaType() {
        return "application/zip";
      }

      @Override
      public String getFileName() {
        return zipName;
      }

      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {
        ZipTools.zip(zipEntries, out);
      }
    };

    return new StreamResponse(zipName + ".zip", "application/zip", stream);
  }

  public static Representation createRepresentation(User user, String aipId, String representationId, String type,
    String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    try {
      Representation representation = model.createRepresentation(aipId, representationId, true, type, true);

      List<LinkingIdentifier> targets = new ArrayList<LinkingIdentifier>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, representation.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The Representation '" + representation.getId() + "' has been manually created.";
      model.createEvent(aipId, null, null, null, PreservationEventType.CREATION,
        "The process of creating an object of the repository.", null, targets, PluginState.SUCCESS, outcomeText,
        details, user.getName(), true);

      RodaCoreFactory.getIndexService().commit(IndexedRepresentation.class);
      return representation;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "A new representation has not been manually deleted.";
      model.createUpdateAIPEvent(aipId, null, null, null, PreservationEventType.CREATION,
        "The process of creation an object of the repository.", PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }
  }

  public static Representation updateRepresentation(User user, Representation representation) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateRepresentationInfo(representation);
  }

  public static void deleteRepresentation(String aipId, String representationId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRepresentation(aipId, representationId);
  }

  public static File createFile(User user, String aipId, String representationId, List<String> directoryPath,
    String fileId, ContentPayload content, String details) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();

    try {
      File file = model.createFile(aipId, representationId, directoryPath, fileId, content);

      List<LinkingIdentifier> targets = new ArrayList<LinkingIdentifier>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(), file.getPath(), file.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The File '" + file.getId() + "' has been manually created.";
      model.createEvent(aipId, representationId, null, null, PreservationEventType.CREATION,
        "The process of creating an object of the repository.", null, targets, PluginState.SUCCESS, outcomeText,
        details, user.getName(), true);

      return file;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "A new file has not been manually created.";
      model.createUpdateAIPEvent(aipId, representationId, null, null, PreservationEventType.CREATION,
        "The process of creation an object of the repository.", PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }
  }

  public static File updateFile(User user, File file, ContentPayload contentPayload, boolean createIfNotExists,
    boolean notify) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    RodaCoreFactory.getModelService().updateFile(file, contentPayload, createIfNotExists, notify);
    return file;
  }

  public static void deleteRepresentationFile(User user, String fileUUID, String details)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    IndexedFile file = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUUID);
    ModelService model = RodaCoreFactory.getModelService();

    try {
      model.deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), true);

      String outcomeText = "The File '" + file.getId() + "' has been manually deleted.";
      model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null,
        PreservationEventType.DELETION, "The process of deleting an object of the repository.", PluginState.SUCCESS,
        outcomeText, details, user.getName(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The File '" + file.getId() + "' has not been manually deleted.";
      model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null,
        PreservationEventType.DELETION, "The process of deleting an object of the repository.", PluginState.FAILURE,
        outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public static EntityResponse retrieveAIPRepresentationFile(String fileUuid, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    IndexedFile iFile = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, fileUuid);

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final String filename;
      final String mediaType;
      final ConsumesOutputStream stream;

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(
        ModelUtils.getFileStoragePath(iFile.getAipId(), iFile.getRepresentationId(), iFile.getPath(), iFile.getId()));
      filename = representationFileBinary.getStoragePath().getName();
      mediaType = RodaConstants.MEDIA_TYPE_WILDCARD;

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return acceptFormat;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          InputStream fileInputStream = null;
          try {
            fileInputStream = representationFileBinary.getContent().createInputStream();
            IOUtils.copy(fileInputStream, out);
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
      return new ObjectResponse<File>(acceptFormat, file);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static DescriptiveMetadata createOrUpdateAIPDescriptiveMetadataFile(String aipId, String representationId,
    String metadataId, String metadataType, String metadataVersion, Map<String, String> properties, InputStream is,
    boolean create) throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
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
          metadataVersion, properties);
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

  public static boolean retrieveScanUpdateStatus(Optional<String> folderRelativePath) {
    return RodaCoreFactory.getTransferredResourcesScannerUpdateStatus(folderRelativePath);
  }

  public static void updateTransferredResources(Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, GenericException {
    RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResources(folderRelativePath, waitToFinish);
  }

  public static void updateTransferredResource(Optional<String> folderRelativePath, ContentPayload payload, String name,
    boolean waitToFinish) throws IsStillUpdatingException, GenericException, NotFoundException, IOException {
    RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResource(folderRelativePath, payload, name,
      waitToFinish);
  }

  public static ConsumesOutputStream retrieveClassificationPlan(User user, String filename)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return ClassificationPlanUtils.retrieveClassificationPlan(user, filename);

  }

  public static List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(User user, IndexedAIP aip,
    IndexedRepresentation representation, Locale locale) throws GenericException {
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
          key += RodaConstants.METADATA_VERSION_SEPARATOR + version.toLowerCase();
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
                String value = null;
                if (representation != null) {
                  value = ServerTools.autoGenerateRepresentationValue(representation, user, generator);
                } else {
                  value = ServerTools.autoGenerateAIPValue(aip, user, generator);
                }

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

      ConsumesOutputStream stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return acceptFormat;
        }

        @Override
        public String getFileName() {
          return transferredResource.getName();
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          InputStream retrieveFile = null;
          try {
            retrieveFile = RodaCoreFactory.getTransferredResourcesScanner()
              .retrieveFile(transferredResource.getFullPath());
            IOUtils.copy(retrieveFile, out);
          } catch (NotFoundException | RequestNotValidException | GenericException e) {
            // do nothing
          } finally {
            IOUtils.closeQuietly(retrieveFile);
          }
        }
      };

      return new StreamResponse(transferredResource.getName(), RodaConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM,
        stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      return new ObjectResponse<TransferredResource>(acceptFormat, transferredResource);
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

  public static CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    return RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);

  }

  public static DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId,
    String representationId, String metadataId, Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadataVersionsBundle bundle = new DescriptiveMetadataVersionsBundle();

    bundle.setAip(retrieve(IndexedAIP.class, aipId));
    if (representationId != null) {
      IndexedRepresentation representation = retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(aipId, representationId));
      bundle.setRepresentation(representation);
    }
    DescriptiveMetadataViewBundle descriptiveMetadataBundle = retrieveDescriptiveMetadataBundle(aipId, representationId,
      metadataId, locale);
    bundle.setDescriptiveMetadata(descriptiveMetadataBundle);

    List<BinaryVersionBundle> versionBundles = new ArrayList<>();

    CloseableIterable<BinaryVersion> it = listDescriptiveMetadataVersions(aipId, representationId, metadataId);
    for (BinaryVersion v : it) {
      versionBundles.add(new BinaryVersionBundle(v.getId(), v.getCreatedDate(), v.getProperties()));
    }
    IOUtils.closeQuietly(it);

    bundle.setVersions(versionBundles);
    return bundle;
  }

  public static void revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId,
      versionId, properties);
  }

  public static void deleteDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }

  public static void updateAIPPermissions(User user, IndexedAIP indexedAIP, Permissions permissions, String details,
    boolean recursive)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    final ModelService model = RodaCoreFactory.getModelService();

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    aip.setPermissions(permissions);
    try {
      model.updateAIPPermissions(aip, user.getName());

      String outcomeText = "The AIP '" + aip.getId() + "' has been manually updated.";
      model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE,
        "The process of updating an object of the repository.", PluginState.SUCCESS, outcomeText, details,
        user.getName(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The AIP '" + aip.getId() + "' has not been manually updated.";
      model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE,
        "The process of updating an object of the repository.", PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }

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
            try {
              model.updateAIPPermissions(descendant, user.getName());

              String outcomeText = "The AIP '" + descendant.getId() + "' has been manually updated.";
              model.createUpdateAIPEvent(descendant.getId(), null, null, null, PreservationEventType.UPDATE,
                "The process of updating an object of the repository.", PluginState.SUCCESS, outcomeText, details,
                user.getName(), true);
            } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
              String outcomeText = "The AIP '" + descendant.getId() + "' has not been manually updated.";
              model.createUpdateAIPEvent(descendant.getId(), null, null, null, PreservationEventType.UPDATE,
                "The process of updating an object of the repository.", PluginState.FAILURE, outcomeText, details,
                user.getName(), true);

              throw e;
            }
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

  public static void updateRisk(Risk risk, User user, Map<String, String> properties, boolean commit)
    throws GenericException, RequestNotValidException {
    risk.setUpdatedBy(user.getName());
    RodaCoreFactory.getModelService().updateRisk(risk, properties, commit);
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
      versionList.add(new BinaryVersionBundle(bv.getId(), bv.getCreatedDate(), bv.getProperties()));

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

  public static void revertRiskVersion(String riskId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertRiskVersion(riskId, versionId, properties, false);
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

  public static StreamResponse retrieveAIPPart(IndexedAIP aip, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    String aipId = aip.getId();

    if (RodaConstants.STORAGE_DIRECTORY_SUBMISSION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSubmissionDirectory(aipId);
      return ApiUtils.download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId);
      return ApiUtils.download(directory);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId);
      return ApiUtils.download(directory);
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

    Job job = new Job();
    job.setName(RiskIncidenceRemoverPlugin.class.getSimpleName() + " " + job.getStartDate());
    job.setPlugin(RiskIncidenceRemoverPlugin.class.getName());
    job.setSourceObjects(SelectedItemsList.create(Risk.class, idList));
    Jobs.createJob(user, job, false);

    for (String riskId : idList) {
      RodaCoreFactory.getModelService().deleteRisk(riskId, true);
    }
  }

  public static void deleteFormat(User user, SelectedItems<Format> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    List<String> idList = consolidate(user, Format.class, selected);
    for (String formatId : idList) {
      RodaCoreFactory.getModelService().deleteFormat(formatId, true);
    }
  }

  public static void updateRiskCounters() throws GenericException, RequestNotValidException, NotFoundException {

    // get risks incidence count using facets
    IndexService index = RodaCoreFactory.getIndexService();
    IndexResult<RiskIncidence> find = index.find(RiskIncidence.class, Filter.ALL, Sorter.NONE, new Sublist(0, 0),
      new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)));

    Map<String, IndexedRisk> allRisks = new HashMap<String, IndexedRisk>();

    // retrieve risks and set default object count to zero
    for (IndexedRisk indexedRisk : index.findAll(IndexedRisk.class, Filter.ALL)) {
      indexedRisk.setObjectsSize(0);
      allRisks.put(indexedRisk.getId(), indexedRisk);
    }

    // update risks from facets
    for (FacetFieldResult fieldResult : find.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = allRisks.get(riskId);
        if (risk != null) {
          risk.setObjectsSize((int) counter);
        } else {
          LOGGER.warn("Updating risk counters found incidences pointing to non-existing risk: {}", riskId);
        }
      }
    }

    // update all in index
    for (IndexedRisk risk : allRisks.values()) {
      index.reindexRisk(risk);
    }

    index.commit(IndexedRisk.class);
  }

  public static void appraisal(User user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason,
    Locale locale) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
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
      Job job = model.retrieveJob(jobId);
      Report report = model.retrieveJobReport(jobId, aipId, true);

      Report reportItem = new Report();
      Messages messages = RodaCoreFactory.getI18NMessages(locale);
      reportItem.setTitle(messages.getTranslation(RodaConstants.I18N_UI_APPRAISAL));
      reportItem.setPlugin(user.getName());
      reportItem.setPluginDetails(rejectReason);
      reportItem.setPluginState(accept ? PluginState.SUCCESS : PluginState.FAILURE);
      reportItem.setOutcomeObjectState(accept ? AIPState.ACTIVE : AIPState.DELETED);
      reportItem.setDateCreated(now);
      report.addReport(reportItem);

      model.createOrUpdateJobReport(report, job);

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
      Jobs.createJob(user, job, false);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not delete AIP associated incidences");
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

    RodaCoreFactory.getIndexService().commit(IndexedAIP.class, Job.class, IndexedReport.class,
      IndexedPreservationEvent.class);
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

      Map<String, String> data = new HashMap<>();
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

      result = HandlebarsUtility.executeHandlebars(rawTemplate, data);
      // result = RodaUtils.indentXML(result);

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
      TransferredResource resource = resources.getResults().get(0);
      return RodaCoreFactory.getTransferredResourcesScanner().renameTransferredResource(resource, newName, true, true);
    } else {
      return transferredResourceId;
    }
  }

  public static IndexedFile renameFolder(User user, String folderUUID, String newName, String details)
    throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    IndexedFile ifolder = index.retrieve(IndexedFile.class, folderUUID);
    String oldName = ifolder.getId();

    try {
      File folder = model.retrieveFile(ifolder.getAipId(), ifolder.getRepresentationId(), ifolder.getPath(),
        ifolder.getId());
      File newFolder = model.renameFolder(folder, newName, true, true);

      String outcomeText = "The folder '" + ifolder.getId() + "' has been manually renamed from '" + oldName + "' to '"
        + newName + "'.";

      model.createUpdateAIPEvent(ifolder.getAipId(), ifolder.getRepresentationId(), null, null,
        PreservationEventType.UPDATE, "The process of updating an object of the repository.", PluginState.SUCCESS,
        outcomeText, details, user.getName(), true);

      index.commitAIPs();

      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + ifolder.getId() + "' has not been manually renamed from '" + oldName
        + "' to '" + newName + "'.";

      model.createUpdateAIPEvent(ifolder.getAipId(), ifolder.getRepresentationId(), null, null,
        PreservationEventType.UPDATE, "The process of updating an object of the repository.", PluginState.FAILURE,
        outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public static void moveFiles(User user, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder, String details) throws GenericException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    IndexResult<IndexedFile> findResult = new IndexResult<IndexedFile>();

    if (toFolder != null && !toFolder.getAipId().equals(aipId)
      || !toFolder.getRepresentationId().equals(representationId)) {
      throw new RequestNotValidException("Cannot move to a file outside defined representation");
    }

    if (selectedFiles instanceof SelectedItemsList) {
      SelectedItemsList<IndexedFile> selectedList = (SelectedItemsList<IndexedFile>) selectedFiles;
      Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      findResult = index.find(IndexedFile.class, filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()));
    } else if (selectedFiles instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedFile> selectedFilter = (SelectedItemsFilter<IndexedFile>) selectedFiles;
      int findCounter = index.count(IndexedFile.class, selectedFilter.getFilter()).intValue();
      findResult = index.find(IndexedFile.class, selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, findCounter));
    }

    if (!findResult.getResults().isEmpty()) {
      String storagePath = toFolder != null ? toFolder.getStoragePath()
        : ModelUtils.getRepresentationDataStoragePath(aipId, representationId).toString();
      StringBuilder outcomeText = new StringBuilder();

      for (IndexedFile ifile : findResult.getResults()) {

        if (ifile != null && !ifile.getAipId().equals(aipId) || !ifile.getRepresentationId().equals(representationId)) {
          throw new RequestNotValidException("Cannot move from a file outside defined representation");
        }

        File file = model.retrieveFile(ifile.getAipId(), ifile.getRepresentationId(), ifile.getPath(), ifile.getId());
        try {
          File movedFile = model.moveFile(aipId, representationId, file, storagePath, true, true);

          outcomeText.append("The file '" + file.getPath() + "/" + file.getId() + "' has been manually moved to '"
            + movedFile.getPath() + "/" + movedFile.getId() + "'.\n");

        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {

          // failure
          outcomeText.append("The file '" + file.getId() + "' has not been manually moved: ["
            + e.getClass().getSimpleName() + "] " + e.getMessage());

          model.createUpdateAIPEvent(aipId, representationId, null, null, PreservationEventType.UPDATE,
            "The process of updating an object of the repository.", PluginState.FAILURE, outcomeText.toString(),
            details, user.getName(), true);

          index.commitAIPs();

          throw e;
        }
      }

      // success
      model.createUpdateAIPEvent(aipId, representationId, null, null, PreservationEventType.UPDATE,
        "The process of updating an object of the repository.", PluginState.SUCCESS, outcomeText.toString(), details,
        user.getName(), true);

      index.commitAIPs();

    }
  }

  public static String moveTransferredResource(SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource) throws GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {

    String resourceRelativePath = "";
    Filter filter = new Filter();
    int counter = 1;

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> selectedList = (SelectedItemsList<TransferredResource>) selected;
      filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      counter = selectedList.getIds().size();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<TransferredResource> selectedFilter = (SelectedItemsFilter<TransferredResource>) selected;
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
      List<String> values = new ArrayList<String>(moveMap.values());
      return values.get(0);
    } else {
      return transferredResource.getUUID();
    }

  }

  public static IndexedFile createFolder(User user, String aipId, String representationId, String folderUUID,
    String newName, String details) throws GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    File newFolder;
    IndexedRepresentation irep = index.retrieve(IndexedRepresentation.class,
      IdUtils.getRepresentationId(aipId, representationId));

    try {
      if (folderUUID != null) {
        IndexedFile ifolder = index.retrieve(IndexedFile.class, folderUUID);
        newFolder = model.createFile(ifolder.getAipId(), ifolder.getRepresentationId(), ifolder.getPath(),
          ifolder.getId(), newName, true);
      } else {
        newFolder = model.createFile(irep.getAipId(), irep.getId(), null, null, newName, true);
      }

      String outcomeText = "The folder '" + newName + "' has been manually created.";
      model.createUpdateAIPEvent(aipId, irep.getId(), null, null, PreservationEventType.CREATION,
        "The process of creating an object of the repository.", PluginState.SUCCESS, outcomeText, details,
        user.getName(), true);

      index.commit(IndexedFile.class);
      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder));
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + newName + "' has not been manually created.";
      model.createUpdateAIPEvent(aipId, irep.getId(), null, null, PreservationEventType.CREATION,
        "The process of creating an object of the repository.", PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }
  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(
    SelectedItems<TransferredResource> selected) throws GenericException, RequestNotValidException {
    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> selectedList = (SelectedItemsList<TransferredResource>) selected;

      Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      IndexResult<TransferredResource> iresults = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()));
      return iresults.getResults();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<TransferredResource> selectedFilter = (SelectedItemsFilter<TransferredResource>) selected;

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

  protected static Reports listReports(int start, int limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<IndexedReport> indexReports = RodaCoreFactory.getIndexService().find(IndexedReport.class, Filter.ALL,
      sorter, new Sublist(start, limit));
    List<Report> results = indexReports.getResults().stream().map(ireport -> (Report) ireport)
      .collect(Collectors.toList());
    return new Reports(results);
  }

  protected static Reports listTransferredResourcesReports(String resourceId, int start, int limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, TransferredResource.class.getName()));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, resourceId));
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS,
      Arrays.asList(AIP.class.getName(), IndexedAIP.class.getName())));

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<IndexedReport> reports = RodaCoreFactory.getIndexService().find(IndexedReport.class, filter, sorter,
      new Sublist(start, limit));
    List<Report> results = reports.getResults().stream().map(ireport -> (Report) ireport).collect(Collectors.toList());
    return new Reports(results);
  }

  protected static Reports listTransferredResourcesReportsWithSIP(String sipId, int start, int limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS,
      Arrays.asList(AIP.class.getName(), IndexedAIP.class.getName())));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, sipId));

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATE, true));
    IndexResult<IndexedReport> indexReports = RodaCoreFactory.getIndexService().find(IndexedReport.class, filter,
      sorter, new Sublist(start, limit));
    List<Report> results = indexReports.getResults().stream().map(ireport -> (Report) ireport)
      .collect(Collectors.toList());
    return new Reports(results);
  }

  public static void deleteRiskIncidences(User user, SelectedItems<RiskIncidence> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    List<String> idList = consolidate(user, RiskIncidence.class, selected);
    for (String incidenceId : idList) {
      RodaCoreFactory.getModelService().deleteRiskIncidence(incidenceId, true);
    }
  }

  public static void updateMultipleIncidences(User user, SelectedItems<RiskIncidence> selected, String status,
    String severity, Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    IndexService index = RodaCoreFactory.getIndexService();
    ModelService model = RodaCoreFactory.getModelService();

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<RiskIncidence> list = (SelectedItemsList<RiskIncidence>) selected;
      List<String> ids = list.getIds();

      for (String id : ids) {
        RiskIncidence incidence = RodaCoreFactory.getModelService().retrieveRiskIncidence(id);
        incidence.setStatus(INCIDENCE_STATUS.valueOf(status));
        incidence.setSeverity(SEVERITY_LEVEL.valueOf(severity));
        incidence.setMitigatedOn(mitigatedOn);
        incidence.setMitigatedBy(mitigatedBy);
        incidence.setMitigatedDescription(mitigatedDescription);
        model.updateRiskIncidence(incidence, false);
      }

      index.commit(RiskIncidence.class);
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<RiskIncidence> filter = (SelectedItemsFilter<RiskIncidence>) selected;

      int counter = index.count(RiskIncidence.class, filter.getFilter()).intValue();
      IndexResult<RiskIncidence> incidences = index.find(RiskIncidence.class, filter.getFilter(), Sorter.NONE,
        new Sublist(0, counter));

      for (RiskIncidence incidence : incidences.getResults()) {
        incidence.setStatus(INCIDENCE_STATUS.valueOf(status));
        incidence.setSeverity(SEVERITY_LEVEL.valueOf(severity));
        incidence.setMitigatedOn(mitigatedOn);
        incidence.setMitigatedBy(mitigatedBy);
        incidence.setMitigatedDescription(mitigatedDescription);
        model.updateRiskIncidence(incidence, false);
      }

      index.commit(RiskIncidence.class);
    }
  }

  public static TransferredResource reindexTransferredResource(String path)
    throws IsStillUpdatingException, NotFoundException, GenericException {
    TransferredResourcesScanner scanner = RodaCoreFactory.getTransferredResourcesScanner();
    scanner.updateTransferredResources(Optional.of(path), true);
    return RodaCoreFactory.getIndexService().retrieve(TransferredResource.class,
      IdUtils.getTransferredResourceUUID(path));
  }

  public static DIP createDIP(DIP dip) throws GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().createDIP(dip, true);
  }

  public static DIP updateDIP(DIP dip) throws GenericException, AuthorizationDeniedException, NotFoundException {
    return RodaCoreFactory.getModelService().updateDIP(dip);
  }

  public static void deleteDIP(String dipId) throws GenericException, AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.getModelService().deleteDIP(dipId);
    RodaCoreFactory.getIndexService().commit(IndexedDIP.class);
  }

  public static EntityResponse retrieveDIPFile(String fileUuid, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    DIPFile iFile = RodaCoreFactory.getIndexService().retrieve(DIPFile.class, fileUuid);

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final String filename;
      final String mediaType;
      final ConsumesOutputStream stream;

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(ModelUtils.getDIPFileStoragePath(iFile));
      filename = representationFileBinary.getStoragePath().getName();
      mediaType = RodaConstants.MEDIA_TYPE_WILDCARD;

      stream = new ConsumesOutputStream() {

        @Override
        public String getMediaType() {
          return acceptFormat;
        }

        @Override
        public String getFileName() {
          return filename;
        }

        @Override
        public void consumeOutputStream(OutputStream out) throws IOException {
          InputStream fileInputStream = null;
          try {
            fileInputStream = representationFileBinary.getContent().createInputStream();
            IOUtils.copy(fileInputStream, out);
          } finally {
            IOUtils.closeQuietly(fileInputStream);
          }
        }
      };
      return new StreamResponse(filename, mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      DIPFile file = RodaCoreFactory.getModelService().retrieveDIPFile(iFile.getDipId(), iFile.getPath(),
        iFile.getId());
      return new ObjectResponse<DIPFile>(acceptFormat, file);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload content, boolean notify) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().createDIPFile(dipId, directoryPath, fileId, size, content, notify);
  }

  public static String createDIPFolder(String dipId, String folderUUID, String newName) throws GenericException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    if (folderUUID != null) {
      DIPFile ifolder = index.retrieve(DIPFile.class, folderUUID);
      DIPFile newFolder = model.createDIPFile(ifolder.getDipId(), ifolder.getPath(), ifolder.getId(), newName, true);
      index.commit(DIPFile.class);
      return IdUtils.getDIPFileId(newFolder);
    } else {
      IndexedDIP dip = index.retrieve(IndexedDIP.class, folderUUID);
      DIPFile newFolder = model.createDIPFile(dip.getId(), null, null, newName, true);
      index.commit(DIPFile.class);
      return IdUtils.getDIPFileId(newFolder);
    }
  }

  public static DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId,
    long size, ContentPayload content, boolean notify) throws GenericException, AuthorizationDeniedException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateDIPFile(dipId, directoryPath, oldFileId, fileId, size, content, true,
      notify);
  }

  public static void deleteDIPFiles(SelectedItems<DIPFile> selected, User user)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    List<String> fileIds = consolidate(user, DIPFile.class, selected);

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, fileIds));
    IndexResult<DIPFile> files = RodaCoreFactory.getIndexService().find(DIPFile.class, filter, Sorter.NONE,
      new Sublist(0, fileIds.size()));

    for (DIPFile file : files.getResults()) {
      RodaCoreFactory.getModelService().deleteDIPFile(file.getDipId(), file.getPath(), file.getId(), true);
    }

    RodaCoreFactory.getIndexService().commit(DIPFile.class);
  }

  public static void createFormatIdentificationJob(User user, SelectedItems selected) throws GenericException,
    JobAlreadyStartedException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Job job = new Job();
    job.setId(UUID.randomUUID().toString());
    job.setName("Format identification using Siegfried");
    job.setSourceObjects(selected);
    job.setPlugin(SiegfriedPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.MISC);
    job.setUsername(user.getName());
    RodaCoreFactory.getModelService().createJob(job);
    RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
  }

  public static void changeRepresentationType(User user, SelectedItemsList<IndexedRepresentation> selected,
    String newType, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    List<String> representationIds = consolidate(user, IndexedRepresentation.class, selected);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, representationIds));
    IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, filter, Sorter.NONE,
      new Sublist(0, representationIds.size()));

    for (IndexedRepresentation irep : reps.getResults()) {
      String oldType = irep.getType();
      try {
        Representation rep = model.retrieveRepresentation(irep.getAipId(), irep.getId());
        rep.setType(newType);
        model.updateRepresentationInfo(rep);

        String outcomeText = "The representation '" + irep.getId() + "' changed its type from '" + oldType + "' to '"
          + newType + "'.";

        model.createUpdateAIPEvent(irep.getAipId(), irep.getId(), null, null, PreservationEventType.UPDATE,
          "The process of updating an object of the repository.", PluginState.SUCCESS, outcomeText, details,
          user.getName(), true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        String outcomeText = "The representation '" + irep.getId() + "' did not change its type from '" + oldType
          + "' to '" + newType + "'.";

        model.createUpdateAIPEvent(irep.getAipId(), irep.getId(), null, null, PreservationEventType.UPDATE,
          "The process of updating an object of the repository.", PluginState.FAILURE, outcomeText, details,
          user.getName(), true);

        throw e;
      }
    }

    index.commit(IndexedRepresentation.class);
  }

  public static ObjectPermissionResult verifyPermissions(User user, String username, String permissionType,
    MultivaluedMap<String, String> queryParams)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    ObjectPermissionResult result = new ObjectPermissionResult();
    for (String queryKey : queryParams.keySet()) {
      for (String queryValues : queryParams.get(queryKey)) {
        boolean hasPermission = checkObjectPermission(username, permissionType, queryKey, queryValues);
        result.addObject(new ObjectPermission(queryKey, queryValues, hasPermission));
      }
    }
    return result;
  }

  private static boolean checkObjectPermission(String username, String permissionType, String objectClass, String id)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    ModelService model = RodaCoreFactory.getModelService();
    boolean hasPermission = false;

    try {
      if (DIP.class.getName().equals(objectClass)) {
        DIP dip = model.retrieveDIP(id);
        Permissions permissions = dip.getPermissions();
        Set<PermissionType> userPermissions = permissions.getUserPermissions(username);
        PermissionType type = PermissionType.valueOf(permissionType.toUpperCase());
        hasPermission = userPermissions.contains(type);
      } else if (AIP.class.getName().equals(objectClass)) {
        AIP aip = model.retrieveAIP(id);
        Permissions permissions = aip.getPermissions();
        Set<PermissionType> userPermissions = permissions.getUserPermissions(username);
        PermissionType type = PermissionType.valueOf(permissionType.toUpperCase());
        hasPermission = userPermissions.contains(type);
      } else {
        throw new RequestNotValidException(objectClass + " permission verification is not supported");
      }
    } catch (IllegalArgumentException e) {
      throw new RequestNotValidException(e);
    }

    return hasPermission;
  }

}
