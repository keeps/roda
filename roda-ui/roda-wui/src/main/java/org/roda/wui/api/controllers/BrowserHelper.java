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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ClassificationPlanUtils;
import org.roda.core.common.ConsumesOutputStream;
import org.roda.core.common.DownloadUtils;
import org.roda.core.common.EntityResponse;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.StreamResponse;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.common.tools.ZipEntryInfo;
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
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadataList;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.ip.metadata.PreservationMetadataList;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
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
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.plugins.internal.DeleteRODAObjectPlugin;
import org.roda.core.plugins.plugins.internal.MovePlugin;
import org.roda.core.plugins.plugins.internal.UpdateAIPPermissionsPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
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
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RelationTypeTranslationsBundle;
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
  private static final String HTML_EXT = ".html";
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserHelper.class);
  private static final List<String> aipAncestorsFieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.AIP_GHOST, RodaConstants.AIP_LEVEL, RodaConstants.AIP_TITLE, RodaConstants.AIP_PARENT_ID);

  private BrowserHelper() {
    // do nothing
  }

  protected static BrowseAIPBundle retrieveBrowseAipBundle(User user, IndexedAIP aip, Locale locale)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    BrowseAIPBundle bundle = new BrowseAIPBundle();

    // set aip
    bundle.setAIP(aip);
    String aipId = aip.getId();
    boolean justActive = aip.getState().equals(AIPState.ACTIVE);

    // set aip ancestors
    try {
      List<IndexedAIP> ancestors = retrieveAncestors(aip, aipAncestorsFieldsToReturn);
      bundle.setAIPAncestors(ancestors);
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
    Long childAIPCount = RodaCoreFactory.getIndexService().count(IndexedAIP.class, childAIPfilter, user, justActive);
    bundle.setChildAIPCount(childAIPCount);

    // Count representations
    Filter repFilter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    Long repCount = RodaCoreFactory.getIndexService().count(IndexedRepresentation.class, repFilter, user, justActive);
    bundle.setRepresentationCount(repCount);

    // Count DIPs
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
    Long dipCount = RodaCoreFactory.getIndexService().count(IndexedDIP.class, dipsFilter, user, justActive);
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
      List<IndexedAIP> ancestors = retrieveAncestors(aip, aipAncestorsFieldsToReturn);
      bundle.setAipAncestors(ancestors);
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

  public static BrowseFileBundle retrieveBrowseFileBundle(IndexedAIP aip, IndexedRepresentation representation,
    IndexedFile file, User user) throws NotFoundException, GenericException, RequestNotValidException {
    BrowseFileBundle bundle = new BrowseFileBundle();

    bundle.setAip(aip);
    bundle.setRepresentation(representation);
    bundle.setFile(file);

    // set aip ancestors
    try {
      List<IndexedAIP> ancestors = retrieveAncestors(aip, aipAncestorsFieldsToReturn);
      bundle.setAipAncestors(ancestors);
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

    boolean justActive = AIPState.ACTIVE.equals(aip.getState());
    bundle.setTotalSiblingCount(count(IndexedFile.class, siblingFilter, justActive, user));

    // Count DIPs
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, file.getUUID()));
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

    // Can be null when the AIP is a ghost
    if (listDescriptiveMetadataBinaries != null) {
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
          if ((typeBundle.getVersion() == null && version == null)
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

          if (StringUtils.isNotBlank(templateWithValues)) {
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
      }

      ret = new DescriptiveMetadataEditBundle(descriptiveMetadataId, type, version, xml, template, values, similar);
    } catch (IOException e) {
      throw new GenericException("Error getting descriptive metadata edit bundle: " + e.getMessage());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    return ret;
  }

  public static DipBundle retrieveDipBundle(String dipUUID, String dipFileUUID, User user)
    throws GenericException, NotFoundException, RequestNotValidException {
    DipBundle bundle = new DipBundle();

    bundle.setDip(retrieve(IndexedDIP.class, dipUUID,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID, RodaConstants.DIP_TITLE, RodaConstants.DIP_AIP_IDS,
        RodaConstants.DIP_AIP_UUIDS, RodaConstants.DIP_FILE_IDS, RodaConstants.DIP_REPRESENTATION_IDS)));

    List<String> dipFileFields = new ArrayList<>();

    if (dipFileUUID != null) {
      DIPFile dipFile = retrieve(DIPFile.class, dipFileUUID, dipFileFields);
      bundle.setDipFile(dipFile);

      List<DIPFile> dipFileAncestors = new ArrayList<>();
      for (String dipFileAncestor : dipFile.getAncestorsUUIDs()) {
        try {
          dipFileAncestors.add(retrieve(DIPFile.class, dipFileAncestor,
            Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIPFILE_DIP_ID, RodaConstants.DIPFILE_ID)));
        } catch (NotFoundException e) {
          // ignore
        }
      }
      bundle.setDipFileAncestors(dipFileAncestors);
    } else {
      // if there is only one DIPFile in the DIP and it is NOT a directory
      // then select it
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dipUUID));
      Sublist sublist = new Sublist(0, 1);
      IndexResult<DIPFile> dipFiles = find(DIPFile.class, filter, Sorter.NONE, sublist, Facets.NONE, user, false,
        dipFileFields);
      if (dipFiles.getTotalCount() == 1 && !dipFiles.getResults().get(0).isDirectory()) {
        bundle.setDipFile(dipFiles.getResults().get(0));
      }
    }

    List<String> aipFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL,
      RodaConstants.AIP_DATE_FINAL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_GHOST);
    List<String> representationFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_TYPE,
      RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, RodaConstants.REPRESENTATION_ORIGINAL,
      RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID);
    List<String> fileFields = new ArrayList<>();

    // infer from DIP
    IndexedDIP dip = bundle.getDip();
    if (!dip.getFileIds().isEmpty()) {
      IndexedFile file = BrowserHelper.retrieve(IndexedFile.class, IdUtils.getFileId(dip.getFileIds().get(0)),
        fileFields);
      bundle.setFile(file);
      bundle.setRepresentation(
        BrowserHelper.retrieve(IndexedRepresentation.class, file.getRepresentationUUID(), representationFields));
      bundle.setAip(BrowserHelper.retrieve(IndexedAIP.class, file.getAipId(), aipFields));
    } else if (!dip.getRepresentationIds().isEmpty()) {
      IndexedRepresentation representation = BrowserHelper.retrieve(IndexedRepresentation.class,
        IdUtils.getRepresentationId(dip.getRepresentationIds().get(0)), representationFields);
      bundle.setRepresentation(representation);
      bundle.setAip(BrowserHelper.retrieve(IndexedAIP.class, representation.getAipId(), aipFields));
    } else if (!dip.getAipIds().isEmpty()) {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, dip.getAipIds().get(0).getAipId(), aipFields);
      bundle.setAip(aip);
    }

    return bundle;
  }

  protected static List<IndexedAIP> retrieveAncestors(IndexedAIP aip, List<String> fieldsToReturn)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(aip, fieldsToReturn);
  }

  protected static <T extends IsIndexed> IndexResult<T> find(Class<T> returnClass, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, User user, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(returnClass, filter, sorter, sublist, facets, user, justActive,
      fieldsToReturn);
  }

  protected static <T extends IsIndexed> IterableIndexResult<T> findAll(final Class<T> returnClass, final Filter filter,
    final Sorter sorter, final Facets facets, final User user, final boolean justActive, List<String> fieldsToReturn) {
    return RodaCoreFactory.getIndexService().findAll(returnClass, filter, sorter, facets, user, justActive,
      fieldsToReturn);
  }

  protected static <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, boolean justActive, User user)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(returnClass, filter, user, justActive);
  }

  protected static <T extends IsIndexed> T retrieve(Class<T> returnClass, String id, List<String> fieldsToReturn)
    throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(returnClass, id, fieldsToReturn);
  }

  protected static <T extends IsIndexed> void commit(Class<T> returnClass) throws GenericException, NotFoundException {
    RodaCoreFactory.getIndexService().commit(returnClass);
  }

  protected static <T extends IsIndexed> List<T> retrieve(Class<T> returnClass, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn) throws GenericException, NotFoundException, RequestNotValidException {
    List<T> ret;

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<T> selectedList = (SelectedItemsList<T>) selectedItems;
      ret = RodaCoreFactory.getIndexService().retrieve(returnClass, selectedList.getIds(), fieldsToReturn);
    } else if (selectedItems instanceof SelectedItemsFilter) {
      SelectedItemsFilter<T> selectedFilter = (SelectedItemsFilter<T>) selectedItems;
      int counter = RodaCoreFactory.getIndexService().count(returnClass, selectedFilter.getFilter()).intValue();
      ret = RodaCoreFactory.getIndexService()
        .find(returnClass, selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter), fieldsToReturn)
        .getResults();
    } else {
      throw new RequestNotValidException(
        "Unsupported SelectedItems implementation: " + selectedItems.getClass().getName());
    }

    return ret;
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

  protected static void validateGetDIPParams(String acceptFormat) throws RequestNotValidException {
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
      return ApiUtils.download(directory, representationId);
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
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_METADATA.equals(part)) {
      StoragePath storagePath = ModelUtils.getRepresentationMetadataStoragePath(aipId, representationId);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId, representationId);
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId, representationId);
      return ApiUtils.download(directory, part);
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

  public static void validateGetOtherMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
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
      List<ZipEntryInfo> zipEntries = new ArrayList<>();
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

      return DownloadUtils.createZipStreamResponse(zipEntries, aipId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      int endInt = limitInt == -1 ? metadata.size() : (limitInt > metadata.size() ? metadata.size() : limitInt);
      DescriptiveMetadataList list = new DescriptiveMetadataList(metadata.subList(startInt, endInt));
      return new ObjectResponse<DescriptiveMetadataList>(acceptFormat, list);
    }

    return null;
  }

  protected static void validateGetPreservationMetadataParams(String acceptFormat) throws RequestNotValidException {
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
    StreamResponse ret;
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
      filename = descriptiveMetadataBinary.getStoragePath().getName() + HTML_EXT;
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
    StreamResponse ret;
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
      filename = descriptiveMetadataBinary.getStoragePath().getName() + HTML_EXT;
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
      filename = fileName + HTML_EXT;
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
      filename = fileName + HTML_EXT;
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
          return fileName + HTML_EXT;
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

  protected static void validateListAIPMetadataParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
    }
  }

  public static EntityResponse listAIPPreservationMetadata(String aipId, String acceptFormat)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory.getModelService()
        .listPreservationMetadata(aipId, true);
      StorageService storage = RodaCoreFactory.getStorageService();
      List<ZipEntryInfo> zipEntries = new ArrayList<>();
      Map<String, ZipEntryInfo> agents = new HashMap<>();

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
      return DownloadUtils.createZipStreamResponse(zipEntries, aipId);
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

  protected static void validateGetAIPRepresentationPreservationMetadataParams(String acceptFormat)
    throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      && !RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP,
          RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML));
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

    List<ZipEntryInfo> zipEntries = new ArrayList<>();
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
      return DownloadUtils.createZipStreamResponse(zipEntries, aipId + "_" + representationId);
    } else {
      return new ObjectResponse<PreservationMetadataList>(acceptFormat, pms);
    }
  }

  public static EntityResponse retrieveAIPRepresentationPreservationMetadata(String aipId, String representationId,
    String startAgent, String limitAgent, String startEvent, String limitEvent, String startFile, String limitFile,
    String acceptFormat)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException, IOException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory.getModelService()
        .listPreservationMetadata(aipId, representationId);
      return getAIPRepresentationPreservationMetadataEntityResponse(aipId, representationId, startAgent, limitAgent,
        startEvent, limitEvent, startFile, limitFile, acceptFormat, preservationFiles);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static EntityResponse retrieveAIPRepresentationPreservationMetadataFile(String aipId, String representationId,
    List<String> filePath, String fileId, String acceptFormat)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      Binary binary = model.retrievePreservationFile(aipId, representationId, filePath, fileId);
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
      PreservationMetadataType type = PreservationMetadataType.REPRESENTATION;
      if (fileId != null) {
        type = PreservationMetadataType.FILE;
      }
      PreservationMetadata pm = model.retrievePreservationMetadata(aipId, representationId, filePath, fileId, type);
      return new ObjectResponse<PreservationMetadata>(acceptFormat, pm);
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
        String id = IdUtils.getPreservationFileId(fileId);
        model.updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, payload, notify);
      }
    } catch (IOException e) {
      throw new GenericException("Error creating or updating AIP representation preservation metadata file", e);
    } finally {
      if (FSUtils.exists(file)) {
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

  public static EntityResponse retrievePreservationMetadataEvent(String id, String aipId, String representationUUID,
    String fileUUID, boolean onlyDetails, String acceptFormat, String language)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    String representationId = null;
    List<String> filePath = null;
    String fileId = null;

    if (fileUUID != null) {
      IndexedFile file = index.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      representationId = file.getRepresentationId();
      filePath = file.getPath();
      fileId = file.getId();
    } else if (representationUUID != null) {
      IndexedRepresentation rep = index.retrieve(IndexedRepresentation.class, representationUUID,
        Arrays.asList(RodaConstants.REPRESENTATION_ID, RodaConstants.INDEX_UUID));
      representationId = rep.getId();
    }

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final Binary binary = model.retrievePreservationEvent(aipId, representationId, filePath, fileId, id);
      final String mediaType = RodaConstants.MEDIA_TYPE_APPLICATION_XML;

      final ConsumesOutputStream stream = new ConsumesOutputStream() {

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

      return new StreamResponse(stream.getFileName(), mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      PreservationMetadata pm = model.retrievePreservationMetadata(aipId, representationId, filePath, fileId,
        PreservationMetadataType.EVENT);
      return new ObjectResponse<PreservationMetadata>(acceptFormat, pm);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML.equals(acceptFormat)) {
      final Binary binary = model.retrievePreservationEvent(aipId, representationId, filePath, fileId, id);
      final String filename = binary.getStoragePath().getName() + HTML_EXT;
      final String mediaType = RodaConstants.MEDIA_TYPE_TEXT_HTML;
      final String htmlEvent = HTMLUtils.preservationMetadataEventToHtml(binary, onlyDetails,
        ServerTools.parseLocale(language));
      final ConsumesOutputStream stream = new ConsumesOutputStream() {

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
          printStream.print(htmlEvent);
          printStream.close();
        }
      };
      return new StreamResponse(filename, mediaType, stream);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static EntityResponse retrievePreservationMetadataAgent(String id, String acceptFormat)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final Binary binary = model.retrievePreservationAgent(id);
      final String mediaType = RodaConstants.MEDIA_TYPE_APPLICATION_XML;

      final ConsumesOutputStream stream = new ConsumesOutputStream() {

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

      return new StreamResponse(stream.getFileName(), mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      PreservationMetadata pm = model.retrievePreservationMetadata(null, null, null, null,
        PreservationMetadataType.AGENT);
      return new ObjectResponse<PreservationMetadata>(acceptFormat, pm);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static EntityResponse listOtherMetadata(String aipId, String representationId, List<String> filePath,
    String fileId, String type, String acceptFormat)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    CloseableIterable<OptionalWithCause<OtherMetadata>> otherFiles = RodaCoreFactory.getModelService()
      .listOtherMetadata(aipId, representationId, filePath, fileId, type);

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      StorageService storage = RodaCoreFactory.getStorageService();
      List<ZipEntryInfo> zipEntries = new ArrayList<>();

      for (OptionalWithCause<OtherMetadata> oFile : otherFiles) {
        if (oFile.isPresent()) {
          OtherMetadata file = oFile.get();
          StoragePath storagePath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, filePath, fileId,
            file.getFileSuffix(), file.getType());
          Binary binary = storage.getBinary(storagePath);

          ZipEntryInfo info = new ZipEntryInfo(FSUtils.getStoragePathAsString(storagePath, true), binary.getContent());
          zipEntries.add(info);
        } else {
          LOGGER.error("Cannot get list other metadata", oFile.getCause());
        }
      }

      IOUtils.closeQuietly(otherFiles);
      return DownloadUtils.createZipStreamResponse(zipEntries, aipId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      OtherMetadataList metadataList = new OtherMetadataList();

      for (OptionalWithCause<OtherMetadata> oFile : otherFiles) {
        if (oFile.isPresent()) {
          metadataList.addObject(oFile.get());
        }
      }

      IOUtils.closeQuietly(otherFiles);
      return new ObjectResponse<OtherMetadataList>(acceptFormat, metadataList);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static EntityResponse retrieveOtherMetadata(String aipId, String representationId, List<String> filePath,
    String fileId, String type, String suffix, String acceptFormat)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final ConsumesOutputStream stream;
      Binary otherMetadataBinary = RodaCoreFactory.getModelService().retrieveOtherMetadataBinary(aipId,
        representationId, filePath, fileId, suffix, type);
      String filename = otherMetadataBinary.getStoragePath().getName();
      String mediaType = RodaConstants.MEDIA_TYPE_WILDCARD;

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
            fileInputStream = otherMetadataBinary.getContent().createInputStream();
            IOUtils.copy(fileInputStream, out);
          } finally {
            IOUtils.closeQuietly(fileInputStream);
          }
        }
      };
      return new StreamResponse(filename, mediaType, stream);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      OtherMetadata other = RodaCoreFactory.getModelService().retrieveOtherMetadata(aipId, representationId, filePath,
        fileId, suffix, type);
      return new ObjectResponse<OtherMetadata>(acceptFormat, other);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static OtherMetadata createOrUpdateOtherMetadataFile(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String type, String fileSuffix, InputStream is)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      Path tempFile = Files.createTempFile("descriptive", ".tmp");
      Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
      ContentPayload payload = new FSPathContentPayload(tempFile);

      return RodaCoreFactory.getModelService().createOrUpdateOtherMetadata(aipId, representationId, fileDirectoryPath,
        fileId, fileSuffix, type, payload, false);
    } catch (IOException e) {
      throw new GenericException("Error creating or updating other metadata");
    }
  }

  public static void deleteOtherMetadataFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteOtherMetadata(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
  }

  public static IndexedAIP moveAIPInHierarchy(User user, SelectedItems<IndexedAIP> selected, String parentId,
    String details) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {

    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Move AIP in hierarchy");
    job.setSourceObjects(selected);
    job.setPlugin(MovePlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, parentId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    job.setPluginParameters(pluginParameters);

    RodaCoreFactory.getModelService().createJob(job);
    try {
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute move job", e);
    }

    IndexService index = RodaCoreFactory.getIndexService();
    index.commit(IndexedAIP.class);
    index.commit(IndexedRepresentation.class);
    index.commit(IndexedFile.class);

    return (parentId != null) ? index.retrieve(IndexedAIP.class, parentId, Arrays.asList(RodaConstants.INDEX_UUID))
      : null;
  }

  public static AIP createAIP(User user, String parentAipId, String type, Permissions permissions)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();
    return model.createAIP(parentAipId, type, permissions, user.getName());
  }

  public static AIP updateAIP(User user, AIP aip) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();
    return model.updateAIP(aip, user.getName());
  }

  public static void deleteAIP(User user, SelectedItems<IndexedAIP> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete AIP");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    job.setPluginParameters(pluginParameters);

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute AIP delete action", e);
    }
  }

  public static void deleteRepresentation(User user, SelectedItems<IndexedRepresentation> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete representations");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    job.setPluginParameters(pluginParameters);

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute representations delete action", e);
    }
  }

  public static void deleteFile(User user, SelectedItems<IndexedFile> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete files");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    job.setPluginParameters(pluginParameters);

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute file delete action", e);
    }
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

  public static Representation createRepresentation(User user, String aipId, String representationId, String type,
    String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();

    try {
      Representation representation = model.createRepresentation(aipId, representationId, true, type, true,
        user.getName());

      List<LinkingIdentifier> targets = new ArrayList<>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, representation.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The representation '" + representationId + "' has been manually created";
      model.createEvent(aipId, null, null, null, PreservationEventType.CREATION, eventDescription, null, targets,
        PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      RodaCoreFactory.getIndexService().commit(IndexedRepresentation.class);
      return representation;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "The representation '" + representationId + "' has not been manually created";
      model.createUpdateAIPEvent(aipId, null, null, null, PreservationEventType.CREATION, eventDescription,
        PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public static Representation updateRepresentation(Representation representation) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateRepresentationInfo(representation);
  }

  public static File createFile(User user, String aipId, String representationId, List<String> directoryPath,
    String fileId, ContentPayload content, String details) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();

    try {
      File file = model.createFile(aipId, representationId, directoryPath, fileId, content);

      List<LinkingIdentifier> targets = new ArrayList<>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(), file.getPath(), file.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The file '" + file.getId() + "' has been manually created.";
      model.createEvent(aipId, representationId, null, null, PreservationEventType.CREATION, eventDescription, null,
        targets, PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      return file;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "The file '" + fileId + "' has not been manually created.";
      model.createUpdateAIPEvent(aipId, representationId, null, null, PreservationEventType.CREATION, eventDescription,
        PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public static File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, boolean notify)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    return RodaCoreFactory.getModelService().updateFile(file, contentPayload, createIfNotExists, notify);
  }

  public static EntityResponse retrieveAIPRepresentationFile(IndexedFile iFile, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(iFile.getAipId(), iFile.getRepresentationId(), iFile.getPath(),
      iFile.getId());

    if (!iFile.isDirectory() && RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      final String filename;
      final String mediaType;
      final ConsumesOutputStream stream;

      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(filePath);
      filename = representationFileBinary.getStoragePath().getName();
      mediaType = MimeTypeHelper.getContentType(filename, RodaConstants.MEDIA_TYPE_WILDCARD);

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
    } else if (iFile.isDirectory() && (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat))) {
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(filePath);
      return ApiUtils.download(directory);
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
      boolean justActive = selectedItemsFilter.justActive();
      Long count = count(classToReturn, filter, justActive, user);
      IndexResult<T> find = find(classToReturn, filter, Sorter.NONE, new Sublist(0, count.intValue()), Facets.NONE,
        user, justActive, Arrays.asList(RodaConstants.INDEX_UUID));
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

        String template = null;
        Set<MetadataValue> values = new HashSet<>();
        try (InputStream templateStream = RodaCoreFactory
          .getConfigurationFileAsStream(RodaConstants.METADATA_TEMPLATE_FOLDER + "/"
            + ((version != null) ? type + RodaConstants.METADATA_VERSION_SEPARATOR + version : type)
            + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

          if (templateStream != null) {
            template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
            values = ServerTools.transform(template);
            for (MetadataValue mv : values) {
              String generator = mv.get("auto-generate");
              if (generator != null && generator.length() > 0) {
                String value;
                if (representation != null) {
                  value = ServerTools.autoGenerateRepresentationValue(representation, generator);
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
                      Map<String, Map<String, String>> i18nMap = new HashMap<>();
                      for (int i = 0; i < optionsList.size(); i++) {
                        String value = optionsList.get(i);
                        String translation = terms.get(i18nPrefix + "." + value);
                        if (translation == null) {
                          translation = value;
                        }
                        Map<String, String> term = new HashMap<>();
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
          }
        } catch (IOException e) {
          LOGGER.error("Error getting the template from the stream", e);
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
    Map<String, IndexedAIP> aips = new HashMap<>();
    Map<String, IndexedRepresentation> representations = new HashMap<>();
    Map<String, IndexedFile> files = new HashMap<>();
    Map<String, TransferredResource> transferredResources = new HashMap<>();

    List<String> eventFields = new ArrayList<>();
    IndexedPreservationEvent ipe = retrieve(IndexedPreservationEvent.class, eventId, eventFields);
    eventBundle.setEvent(ipe);
    if (ipe.getLinkingAgentIds() != null && !ipe.getLinkingAgentIds().isEmpty()) {
      Map<String, IndexedPreservationAgent> agents = new HashMap<>();
      for (LinkingIdentifier agentID : ipe.getLinkingAgentIds()) {
        try {
          List<String> agentFields = Arrays.asList(RodaConstants.PRESERVATION_AGENT_ID,
            RodaConstants.PRESERVATION_AGENT_NAME, RodaConstants.PRESERVATION_AGENT_TYPE,
            RodaConstants.PRESERVATION_AGENT_ROLES, RodaConstants.PRESERVATION_AGENT_VERSION,
            RodaConstants.PRESERVATION_AGENT_NOTE, RodaConstants.PRESERVATION_AGENT_EXTENSION);
          IndexedPreservationAgent agent = retrieve(IndexedPreservationAgent.class, agentID.getValue(), agentFields);
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
          List<String> aipFields = Arrays.asList(RodaConstants.AIP_TITLE, RodaConstants.INDEX_UUID);
          IndexedAIP aip = retrieve(IndexedAIP.class, uuid, aipFields);
          aips.put(idValue, aip);
        } else if (RODA_TYPE.REPRESENTATION.equals(linkingType)) {
          String uuid = LinkingObjectUtils.getRepresentationIdFromLinkingId(idValue);
          List<String> representationFields = Arrays.asList(RodaConstants.REPRESENTATION_ID,
            RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ORIGINAL);
          IndexedRepresentation rep = retrieve(IndexedRepresentation.class, uuid, representationFields);
          representations.put(idValue, rep);
        } else if (RODA_TYPE.FILE.equals(linkingType)) {
          List<String> fileFields = new ArrayList<>(RodaConstants.FILE_FIELDS_TO_RETURN);
          fileFields.addAll(RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
          fileFields.addAll(Arrays.asList(RodaConstants.FILE_ORIGINALNAME, RodaConstants.FILE_SIZE,
            RodaConstants.FILE_FILEFORMAT, RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FORMAT_DESIGNATION));
          IndexedFile file = retrieve(IndexedFile.class, LinkingObjectUtils.getFileIdFromLinkingId(idValue),
            fileFields);
          files.put(idValue, file);
        } else if (RODA_TYPE.TRANSFERRED_RESOURCE.equals(linkingType)) {
          String id = LinkingObjectUtils.getTransferredResourceIdFromLinkingId(idValue);
          if (id != null) {
            List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID,
              RodaConstants.TRANSFERRED_RESOURCE_NAME, RodaConstants.TRANSFERRED_RESOURCE_FULLPATH);
            TransferredResource tr = retrieve(TransferredResource.class, IdUtils.createUUID(id), resourceFields);
            transferredResources.put(idValue, tr);
          }
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
    boolean recursive) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, JobAlreadyStartedException {
    final String eventDescription = "The process of updating an object of the repository.";

    final ModelService model = RodaCoreFactory.getModelService();
    AIP aip = model.retrieveAIP(indexedAIP.getId());
    aip.setPermissions(permissions);
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aip.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

    try {
      model.updateAIPPermissions(aip, user.getName());
      String outcomeText = PluginHelper.createOutcomeTextForAIP(indexedAIP, " permissions has been manually updated");
      model.createEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, sources, null,
        PluginState.SUCCESS, outcomeText, details, user.getName(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = PluginHelper.createOutcomeTextForAIP(indexedAIP,
        " permissions has not been manually updated");
      model.createEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, sources, null,
        PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }

    if (recursive) {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, indexedAIP.getId()));
      SelectedItemsFilter<IndexedAIP> selectedItems = new SelectedItemsFilter<>(filter, IndexedAIP.class.getName(),
        Boolean.FALSE);

      Job job = new Job();
      job.setId(IdUtils.createUUID());
      job.setName("Update AIP permissions recursively");
      job.setSourceObjects(selectedItems);
      job.setPlugin(UpdateAIPPermissionsPlugin.class.getCanonicalName());
      job.setPluginType(PluginType.INTERNAL);
      job.setUsername(user.getName());

      Map<String, String> pluginParameters = new HashMap<>();
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_AIP_ID, aip.getId());
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION, eventDescription);
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT,
        "AIP " + indexedAIP.getId() + " permissions were updated and all sublevels will be too");
      job.setPluginParameters(pluginParameters);

      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    }
  }

  public static void updateDIPPermissions(IndexedDIP indexedDIP, Permissions permissions, String details)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    // TODO 20170222 nvieira it should create an event associated with DIP
    ModelService model = RodaCoreFactory.getModelService();
    DIP dip = model.retrieveDIP(indexedDIP.getId());
    dip.setPermissions(permissions);
    model.updateDIPPermissions(dip);
  }

  public static Risk createRisk(Risk risk, User user, boolean commit)
    throws GenericException, RequestNotValidException {
    risk.setCreatedBy(user.getName());
    risk.setUpdatedBy(user.getName());
    return RodaCoreFactory.getModelService().createRisk(risk, commit);
  }

  public static void updateRisk(Risk risk, User user, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, RequestNotValidException {
    risk.setUpdatedBy(user.getName());
    RodaCoreFactory.getModelService().updateRisk(risk, properties, commit, incidences);
  }

  public static Risk createRisk(Risk risk, boolean commit) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createRisk(risk, commit);
  }

  public static Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().updateRisk(risk, properties, commit, incidences);
  }

  public static void deleteRisk(String riskId, boolean commit)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRisk(riskId, commit);
  }

  public static RiskIncidence createRiskIncidence(RiskIncidence incidence, boolean commit)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createRiskIncidence(incidence, commit);
  }

  public static RiskIncidence updateRiskIncidence(RiskIncidence incidence, boolean commit)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().updateRiskIncidence(incidence, commit);
  }

  public static void deleteRiskIncidence(String incidenceId, boolean commit)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRiskIncidence(incidenceId, commit);
  }

  public static RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);
    List<BinaryVersionBundle> versionList = new ArrayList<>();
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

    if (lastRiskBinary != null) {
      Risk lastRisk = JsonUtils.getObjectFromJson(lastRiskBinary.getContent().createInputStream(), Risk.class);
      return new RiskVersionsBundle(lastRisk, versionList);
    } else {
      return new RiskVersionsBundle();
    }
  }

  public static boolean hasRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getStorageService().listBinaryVersions(storagePath);
    boolean hasRiskVersion = iterable.iterator().hasNext();
    IOUtils.closeQuietly(iterable);
    return hasRiskVersion;
  }

  public static void revertRiskVersion(String riskId, String versionId, Map<String, String> properties, int incidences)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertRiskVersion(riskId, versionId, properties, false, incidences);
  }

  public static void deleteRiskVersion(String riskId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }

  public static Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    BinaryVersion bv = RodaCoreFactory.getModelService().retrieveVersion(riskId, selectedVersion);
    return JsonUtils.getObjectFromJson(bv.getBinary().getContent().createInputStream(), Risk.class);
  }

  public static void validateExportAIPParams(String acceptFormat) throws RequestNotValidException {
    if (!RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      throw new RequestNotValidException("Invalid '" + RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT
        + "' value. Expected values: " + Arrays.asList(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN));
    }
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
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId);
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId);
      return ApiUtils.download(directory, part);
    } else {
      throw new GenericException("Unsupported part: " + part);
    }
  }

  public static StreamResponse retrieveAIPs(SelectedItems<IndexedAIP> selected, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, IOException {
    IndexService index = RodaCoreFactory.getIndexService();
    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
      List<ZipEntryInfo> zipEntries = new ArrayList<>();
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
      return DownloadUtils.createZipStreamResponse(zipEntries, "export");
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

    return new RiskMitigationBundle(lowLimit, highLimit, preProbability, preImpact, posProbability, posImpact);
  }

  public static List<String> retrieveShowMitigationTerms() {
    List<String> terms = new ArrayList<>();
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
    List<String> probabilities = new ArrayList<>();
    for (int i = 0; i <= probabilityLimit; i++) {
      String value = Integer.toString(i);
      probabilities.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability", value));
    }

    // third list contains impact content
    List<String> impacts = new ArrayList<>();
    for (int i = 0; i <= impactLimit; i++) {
      String value = Integer.toString(i);
      impacts.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact", value));
    }

    return new MitigationPropertiesBundle(lowLimit, highLimit, probabilities, impacts);
  }

  public static void deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    InvalidParameterException, JobAlreadyStartedException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete risks");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute risk delete action", e);
    }
  }

  public static void updateRiskCounters() throws GenericException, RequestNotValidException, NotFoundException {
    IndexService index = RodaCoreFactory.getIndexService();

    IndexResult<RiskIncidence> findAllRiskIncidences = index.find(RiskIncidence.class, Filter.ALL, Sorter.NONE,
      new Sublist(0, 0), new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)),
      Arrays.asList(RodaConstants.INDEX_UUID));

    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_STATUS, INCIDENCE_STATUS.UNMITIGATED.toString()));
    IndexResult<RiskIncidence> findNotMitigatedRiskIncidences = index.find(RiskIncidence.class, filter, Sorter.NONE,
      new Sublist(0, 0), new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)),
      Arrays.asList(RodaConstants.INDEX_UUID));

    Map<String, IndexedRisk> allRisks = new HashMap<>();

    // retrieve risks and set default object count to zero
    IterableIndexResult<IndexedRisk> risks = index.findAll(IndexedRisk.class, Filter.ALL, new ArrayList<>());
    for (IndexedRisk indexedRisk : risks) {
      indexedRisk.setIncidencesCount(0);
      indexedRisk.setUnmitigatedIncidencesCount(0);
      allRisks.put(indexedRisk.getId(), indexedRisk);
    }

    // update risks from facets (all incidences)
    for (FacetFieldResult fieldResult : findAllRiskIncidences.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = allRisks.get(riskId);
        if (risk != null) {
          risk.setIncidencesCount((int) counter);
        } else {
          LOGGER.warn("Updating risk counters found incidences pointing to non-existing risk: {}", riskId);
        }
      }
    }

    // update risks from facets (not mitigated incidences)
    for (FacetFieldResult fieldResult : findNotMitigatedRiskIncidences.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = allRisks.get(riskId);
        if (risk != null) {
          risk.setUnmitigatedIncidencesCount((int) counter);
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
      reportItem.setPlugin(messages.getTranslation(RodaConstants.I18N_UI_APPRAISAL));
      reportItem.setPluginDetails(rejectReason);
      reportItem.setPluginState(accept ? PluginState.SUCCESS : PluginState.FAILURE);
      reportItem.setOutcomeObjectState(accept ? AIPState.ACTIVE : AIPState.DELETED);
      reportItem.setDateCreated(now);
      report.addReport(reportItem);

      model.createOrUpdateJobReport(report, job);

      // save job state
      Pair<Integer, Integer> pair = jobState.get(jobId);
      if (pair == null) {
        jobState.put(jobId, Pair.of(1, accept ? 1 : 0));
      } else {
        jobState.put(jobId, Pair.of(pair.getFirst() + 1, pair.getSecond() + (accept ? 1 : 0)));
      }
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
    List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_FULLPATH,
      RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID);

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, transferredResourceId));
    IndexResult<TransferredResource> resources = RodaCoreFactory.getIndexService().find(TransferredResource.class,
      filter, Sorter.NONE, new Sublist(0, 1), resourceFields);

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
    String eventDescription = "The process of updating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    IndexedFile ifolder = index.retrieve(IndexedFile.class, folderUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
    String oldName = ifolder.getId();

    try {
      File folder = model.retrieveFile(ifolder.getAipId(), ifolder.getRepresentationId(), ifolder.getPath(),
        ifolder.getId());
      File newFolder = model.renameFolder(folder, newName, true, true);
      String outcomeText = "The folder '" + oldName + "' has been manually renamed to '" + newName + "'.";
      model.createUpdateAIPEvent(ifolder.getAipId(), ifolder.getRepresentationId(), null, null,
        PreservationEventType.UPDATE, eventDescription, PluginState.SUCCESS, outcomeText, details, user.getName(),
        true);

      index.commitAIPs();
      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder), RodaConstants.FILE_FIELDS_TO_RETURN);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + oldName + "' has not been manually renamed to '" + newName + "'.";

      model.createUpdateAIPEvent(ifolder.getAipId(), ifolder.getRepresentationId(), null, null,
        PreservationEventType.UPDATE, eventDescription, PluginState.FAILURE, outcomeText, details, user.getName(),
        true);

      throw e;
    }
  }

  public static void moveFiles(User user, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder, String details) throws GenericException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {

    if (toFolder != null
      && (!toFolder.getAipId().equals(aipId) || !toFolder.getRepresentationId().equals(representationId))) {
      throw new RequestNotValidException("Cannot move to a file outside defined representation");
    }

    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Move files");
    job.setSourceObjects(selectedFiles);
    job.setPlugin(MovePlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    if (toFolder != null) {
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, toFolder.getUUID());
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    job.setPluginParameters(pluginParameters);

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute move job", e);
    }
  }

  public static void moveTransferredResource(User user, SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource) throws GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {

    String resourceRelativePath = "";
    if (transferredResource != null) {
      resourceRelativePath = transferredResource.getRelativePath();
    }

    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Move transferred resources");
    job.setSourceObjects(selected);
    job.setPlugin(MovePlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, resourceRelativePath);
    job.setPluginParameters(pluginParameters);

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException | AuthorizationDeniedException e) {
      LOGGER.error("Could not execute move job", e);
    }
  }

  public static IndexedFile createFolder(User user, String aipId, String representationId, String folderUUID,
    String newName, String details) throws GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, AuthorizationDeniedException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    File newFolder;
    IndexedRepresentation irep = index.retrieve(IndexedRepresentation.class,
      IdUtils.getRepresentationId(aipId, representationId), RodaConstants.REPRESENTATION_FIELDS_TO_RETURN);

    try {
      if (folderUUID != null) {
        IndexedFile ifolder = index.retrieve(IndexedFile.class, folderUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
        newFolder = model.createFile(ifolder.getAipId(), ifolder.getRepresentationId(), ifolder.getPath(),
          ifolder.getId(), newName, true);
      } else {
        newFolder = model.createFile(irep.getAipId(), irep.getId(), null, null, newName, true);
      }

      String outcomeText = "The folder '" + newName + "' has been manually created.";
      model.createUpdateAIPEvent(aipId, irep.getId(), null, null, PreservationEventType.CREATION, eventDescription,
        PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      index.commit(IndexedFile.class);
      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder), new ArrayList<>());
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + newName + "' has not been manually created.";
      model.createUpdateAIPEvent(aipId, irep.getId(), null, null, PreservationEventType.CREATION, eventDescription,
        PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(
    SelectedItems<TransferredResource> selected) throws GenericException, RequestNotValidException {
    if (selected instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> selectedList = (SelectedItemsList<TransferredResource>) selected;
      Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, selectedList.getIds()));
      IndexResult<TransferredResource> iresults = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        filter, Sorter.NONE, new Sublist(0, selectedList.getIds().size()), new ArrayList<>());
      return iresults.getResults();
    } else if (selected instanceof SelectedItemsFilter) {
      SelectedItemsFilter<TransferredResource> selectedFilter = (SelectedItemsFilter<TransferredResource>) selected;
      Long counter = RodaCoreFactory.getIndexService().count(TransferredResource.class, selectedFilter.getFilter());
      IndexResult<TransferredResource> iresults = RodaCoreFactory.getIndexService().find(TransferredResource.class,
        selectedFilter.getFilter(), Sorter.NONE, new Sublist(0, counter.intValue()), new ArrayList<>());
      return iresults.getResults();
    } else {
      return new ArrayList<>();
    }
  }

  public static void updateRiskIncidence(RiskIncidence incidence) throws GenericException {
    RodaCoreFactory.getModelService().updateRiskIncidence(incidence, true);
  }

  protected static Reports listReports(int start, int limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATED, true));
    IndexResult<IndexedReport> indexReports = RodaCoreFactory.getIndexService().find(IndexedReport.class, Filter.ALL,
      sorter, new Sublist(start, limit), new ArrayList<>());
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

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATED, true));
    IndexResult<IndexedReport> reports = RodaCoreFactory.getIndexService().find(IndexedReport.class, filter, sorter,
      new Sublist(start, limit), new ArrayList<>());
    List<Report> results = reports.getResults().stream().map(ireport -> (Report) ireport).collect(Collectors.toList());
    return new Reports(results);
  }

  protected static Reports listTransferredResourcesReportsWithSIP(String sipId, int start, int limit)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS,
      Arrays.asList(AIP.class.getName(), IndexedAIP.class.getName())));
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, sipId));

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATED, true));
    IndexResult<IndexedReport> indexReports = RodaCoreFactory.getIndexService().find(IndexedReport.class, filter,
      sorter, new Sublist(start, limit), new ArrayList<>());
    List<Report> results = indexReports.getResults().stream().map(ireport -> (Report) ireport)
      .collect(Collectors.toList());
    return new Reports(results);
  }

  protected static Reports listTransferredResourcesReportsWithSourceOriginalName(String sourceName, int start,
    int limit) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS,
      Arrays.asList(AIP.class.getName(), IndexedAIP.class.getName())));
    filter.add(new BasicSearchFilterParameter(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME, sourceName));

    Sorter sorter = new Sorter(new SortParameter(RodaConstants.JOB_REPORT_DATE_UPDATED, true));
    IndexResult<IndexedReport> indexReports = RodaCoreFactory.getIndexService().find(IndexedReport.class, filter,
      sorter, new Sublist(start, limit), new ArrayList<>());
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

  public static void updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity,
    Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
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
        new Sublist(0, counter), new ArrayList<>());

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
      IdUtils.getTransferredResourceUUID(path), new ArrayList<>());
  }

  public static DIP createDIP(DIP dip) throws GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().createDIP(dip, true);
  }

  public static DIP updateDIP(DIP dip) throws GenericException, AuthorizationDeniedException, NotFoundException {
    return RodaCoreFactory.getModelService().updateDIP(dip);
  }

  public static void deleteDIPs(SelectedItems<IndexedDIP> selected, User user)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    for (String dipId : consolidate(user, IndexedDIP.class, selected)) {
      RodaCoreFactory.getModelService().deleteDIP(dipId);
    }
    RodaCoreFactory.getIndexService().commit(IndexedDIP.class);
  }

  protected static EntityResponse retrieveDIP(String dipId, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)) {
      StorageService storage = RodaCoreFactory.getStorageService();
      StoragePath storagePath = ModelUtils.getDIPDataStoragePath(dipId);

      if (!storage.hasDirectory(storagePath)) {
        storagePath = ModelUtils.getDIPStoragePath(dipId);
      }

      return ApiUtils.download(storage.getDirectory(storagePath), dipId);
    } else if (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_XML.equals(acceptFormat)) {
      DIP dip = RodaCoreFactory.getModelService().retrieveDIP(dipId);
      return new ObjectResponse<DIP>(acceptFormat, dip);
    } else {
      throw new GenericException("Unsupported accept format: " + acceptFormat);
    }
  }

  public static EntityResponse retrieveDIPFile(String fileUuid, String acceptFormat)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    DIPFile iFile = RodaCoreFactory.getIndexService().retrieve(DIPFile.class, fileUuid,
      RodaConstants.DIPFILE_FIELDS_TO_RETURN);

    if (!iFile.isDirectory() && RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat)) {
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
    } else if (iFile.isDirectory() && (RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP.equals(acceptFormat)
      || RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN.equals(acceptFormat))) {
      StoragePath filePath = ModelUtils.getDIPFileStoragePath(iFile);
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(filePath);
      return ApiUtils.download(directory);
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
      DIPFile ifolder = index.retrieve(DIPFile.class, folderUUID, RodaConstants.DIPFILE_FIELDS_TO_RETURN);
      DIPFile newFolder = model.createDIPFile(ifolder.getDipId(), ifolder.getPath(), ifolder.getId(), newName, true);
      index.commit(DIPFile.class);
      return IdUtils.getDIPFileId(newFolder);
    } else {
      DIPFile newFolder = model.createDIPFile(dipId, null, null, newName, true);
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
      new Sublist(0, fileIds.size()), RodaConstants.DIPFILE_FIELDS_TO_RETURN);

    for (DIPFile file : files.getResults()) {
      RodaCoreFactory.getModelService().deleteDIPFile(file.getDipId(), file.getPath(), file.getId(), true);
    }

    RodaCoreFactory.getIndexService().commit(DIPFile.class);
  }

  public static void createFormatIdentificationJob(User user, SelectedItems<?> selected) throws GenericException,
    JobAlreadyStartedException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Format identification using Siegfried");
    job.setSourceObjects(selected);
    job.setPlugin(SiegfriedPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.MISC);
    job.setUsername(user.getName());
    RodaCoreFactory.getModelService().createJob(job);
    RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
  }

  public static void changeRepresentationType(User user, SelectedItems<IndexedRepresentation> selected, String newType,
    String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    String eventDescription = "The process of updating an object of the repository.";

    List<String> representationIds = consolidate(user, IndexedRepresentation.class, selected);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, representationIds));
    IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, filter, Sorter.NONE,
      new Sublist(0, representationIds.size()), Arrays.asList(RodaConstants.REPRESENTATION_ID,
        RodaConstants.REPRESENTATION_TYPE, RodaConstants.REPRESENTATION_AIP_ID));

    for (IndexedRepresentation irep : reps.getResults()) {
      String oldType = irep.getType();
      List<LinkingIdentifier> sources = new ArrayList<>();
      sources.add(PluginHelper.getLinkingIdentifier(irep.getAipId(), irep.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

      try {
        model.changeRepresentationType(irep.getAipId(), irep.getId(), newType, user.getName());
        index.commit(IndexedRepresentation.class);
        StringBuilder outcomeText = new StringBuilder().append("The representation '").append(irep.getId())
          .append("' changed its type from '").append(oldType).append("' to '").append(newType).append("'.");

        model.createEvent(irep.getAipId(), irep.getId(), null, null, PreservationEventType.UPDATE, eventDescription,
          sources, null, PluginState.SUCCESS, outcomeText.toString(), details, user.getName(), true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        StringBuilder outcomeText = new StringBuilder().append("The representation '").append(irep.getId())
          .append("' did not change its type from '").append(oldType).append("' to '").append(newType).append("'.");

        model.createEvent(irep.getAipId(), irep.getId(), null, null, PreservationEventType.UPDATE, eventDescription,
          sources, null, PluginState.FAILURE, outcomeText.toString(), details, user.getName(), true);
        throw e;
      }
    }
  }

  public static void changeAIPType(User user, SelectedItems<IndexedAIP> selected, String newType, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    String eventDescription = "The process of updating an object of the repository.";

    List<String> aipIds = consolidate(user, IndexedAIP.class, selected);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, aipIds));
    IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, filter, Sorter.NONE, new Sublist(0, aipIds.size()),
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID, RodaConstants.AIP_TYPE));

    for (IndexedAIP iaip : aips.getResults()) {
      String oldType = iaip.getType();
      List<LinkingIdentifier> sources = new ArrayList<>();
      sources.add(PluginHelper.getLinkingIdentifier(iaip.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

      try {
        model.changeAIPType(iaip.getId(), newType, user.getName());
        index.commit(IndexedAIP.class);
        StringBuilder outcomeText = new StringBuilder().append("The AIP '").append(iaip.getId())
          .append("' changed its type from '").append(oldType).append("' to '").append(newType).append("'.");

        model.createEvent(iaip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, sources, null,
          PluginState.SUCCESS, outcomeText.toString(), details, user.getName(), true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        StringBuilder outcomeText = new StringBuilder().append("The AIP '").append(iaip.getId())
          .append("' did not change its type from '").append(oldType).append("' to '").append(newType).append("'.");

        model.createEvent(iaip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, sources, null,
          PluginState.FAILURE, outcomeText.toString(), details, user.getName(), true);
        throw e;
      }
    }
  }

  public static void changeRepresentationStates(User user, IndexedRepresentation representation, List<String> newStates,
    String details) throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    String eventDescription = "The process of updating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(representation.getAipId(), representation.getId(),
      RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    try {
      model.changeRepresentationStates(representation.getAipId(), representation.getId(), newStates, user.getName());
      index.commit(IndexedRepresentation.class);
      StringBuilder outcomeText = new StringBuilder().append("The states of the representation '")
        .append(representation.getId()).append("' were updated.");

      model.createEvent(representation.getAipId(), representation.getId(), null, null, PreservationEventType.UPDATE,
        eventDescription, sources, null, PluginState.SUCCESS, outcomeText.toString(), details, user.getName(), true);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      StringBuilder outcomeText = new StringBuilder().append("The states of the representation '")
        .append(representation.getId()).append("' were not updated.");

      model.createEvent(representation.getAipId(), representation.getId(), null, null, PreservationEventType.UPDATE,
        eventDescription, sources, null, PluginState.FAILURE, outcomeText.toString(), details, user.getName(), true);
      throw e;
    }
  }

  public static ObjectPermissionResult verifyPermissions(String username, String permissionType,
    MultivaluedMap<String, String> queryParams)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    ObjectPermissionResult result = new ObjectPermissionResult();
    for (Entry<String, List<String>> entry : queryParams.entrySet()) {
      String queryKey = entry.getKey();
      try {
        Class.forName(queryKey);
        for (String queryValues : entry.getValue()) {
          boolean hasPermission = RodaCoreFactory.getModelService().checkObjectPermission(username, permissionType,
            queryKey, queryValues);
          result.addObject(new ObjectPermission(queryKey, queryValues, hasPermission));
        }
      } catch (ClassNotFoundException e) {
        // do nothing
      }
    }
    return result;
  }

  public static boolean hasDocumentation(String aipId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    StoragePath documentationPath = DefaultStoragePath.parse(aipPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    try {
      Long counter = RodaCoreFactory.getStorageService().countResourcesUnderContainer(documentationPath, false);
      return counter > 0;
    } catch (NotFoundException e) {
      return false;
    }
  }

  public static Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().createNotification(notification, processor);
  }

  public static Notification updateNotification(Notification notification)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().updateNotification(notification);
  }

  public static void deleteNotification(String notificationId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteNotification(notificationId);
  }

  public static Notification acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, ackToken);
  }

  public static RepresentationInformation createRepresentationInformation(RepresentationInformation ri,
    String createdBy, boolean commit) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createRepresentationInformation(ri, createdBy, commit);
  }

  public static RepresentationInformation updateRepresentationInformation(RepresentationInformation ri,
    String updatedBy, boolean commit) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().updateRepresentationInformation(ri, updatedBy, commit);
  }

  public static void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteRepresentationInformation(representationInformationId, commit);
  }

  public static void deleteRepresentationInformation(User user, SelectedItems<RepresentationInformation> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete representation information");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute representation information delete action", e);
    }
  }

  public static Pair<String, Integer> retrieveRepresentationInformationWithFilter(String riFilter)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_FILTERS, riFilter));
    IndexResult<RepresentationInformation> result = RodaCoreFactory.getIndexService().find(
      RepresentationInformation.class, filter, Sorter.NONE, new Sublist(0, 3),
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_INFORMATION_ID));

    if (result.getTotalCount() == 1) {
      return Pair.of(result.getResults().get(0).getId(), 1);
    } else {
      return Pair.of("", (int) result.getTotalCount());
    }
  }

  public static RepresentationInformationFilterBundle retrieveObjectClassFields(Messages messages) {
    RepresentationInformationFilterBundle newBundle = new RepresentationInformationFilterBundle();
    Iterator<String> keys = RodaCoreFactory.getRodaConfiguration().getKeys("core.ri.rule");
    Map<String, List<String>> fieldsResult = new HashMap<>();
    Map<String, String> translationsResult = new HashMap<>();

    while (keys.hasNext()) {
      String key = keys.next();
      String[] splittedKey = key.split("\\.");
      List<String> fields = RodaCoreFactory.getRodaConfigurationAsList(key);
      List<String> fieldsAndTranslations = new ArrayList<>();

      for (String field : fields) {
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString(field, RodaConstants.SEARCH_FIELD_FIELDS);
        fieldsAndTranslations.add(fieldName);

        String translation = messages
          .getTranslation(RodaCoreFactory.getRodaConfigurationAsString(field, RodaConstants.SEARCH_FIELD_I18N));
        translationsResult.put(splittedKey[3] + ":" + fieldName, translation);
      }

      fieldsResult.put(splittedKey[3], fieldsAndTranslations);
    }

    newBundle.setObjectClassFields(fieldsResult);
    newBundle.setTranslations(translationsResult);
    return newBundle;
  }

  public static Format createFormat(Format format, boolean commit) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createFormat(format, commit);
  }

  public static Format updateFormat(Format format, boolean commit) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getModelService().updateFormat(format, commit);
  }

  public static void deleteFormat(String formatId, boolean commit)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteFormat(formatId, commit);
  }

  public static void deleteFormat(User user, SelectedItems<Format> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Delete formats");
    job.setSourceObjects(selected);
    job.setPlugin(DeleteRODAObjectPlugin.class.getCanonicalName());
    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(user.getName());

    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error("Could not execute format delete action", e);
    }
  }

  public static RelationTypeTranslationsBundle retrieveRelationTypeTranslations(Messages messages) {
    RelationTypeTranslationsBundle bundle = new RelationTypeTranslationsBundle();
    Map<String, String> translations = new HashMap<>();
    Map<String, String> inverseMap = new HashMap<>();
    Map<String, String> inverseTranslations = new HashMap<>();

    List<String> configs = RodaCoreFactory.getRodaConfigurationAsList("core.ri.relation");

    for (String config : configs) {
      String fieldName = RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_FIELDS);
      String translation = messages
        .getTranslation(RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_I18N));
      translations.put(fieldName, translation);

      String inverse = RodaCoreFactory.getRodaConfigurationAsString(config, RodaConstants.SEARCH_FIELD_INVERSE,
        RodaConstants.SEARCH_FIELD_FIELDS);

      if (StringUtils.isNotBlank(inverse)) {
        String inverseTranslation = messages.getTranslation(RodaCoreFactory.getRodaConfigurationAsString(config,
          RodaConstants.SEARCH_FIELD_INVERSE, RodaConstants.SEARCH_FIELD_I18N));
        inverseTranslations.put(inverse, inverseTranslation);
        inverseMap.put(fieldName, inverse);
      }
    }

    bundle.setTranslations(translations);
    bundle.setInverses(inverseMap);
    bundle.setInverseTranslations(inverseTranslations);
    return bundle;
  }

  public static boolean updateRepresentationInformationListWithFilter(
    SelectedItemsList<RepresentationInformation> representationInformationIds, String filterToAdd, String username) {
    ModelService model = RodaCoreFactory.getModelService();
    boolean success = true;
    for (String id : representationInformationIds.getIds()) {
      try {
        RepresentationInformation representationInformation = model.retrieveRepresentationInformation(id);
        if(!representationInformation.getFilters().contains(filterToAdd)){
          representationInformation.getFilters().add(filterToAdd);
        }
        model.updateRepresentationInformation(representationInformation, username, false);
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        success = false;
        LOGGER.error("Could not update filter for representation information id: {}", id, e);
      }
    }
    return success;
  }
}
