/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

public class RestUtils {

  private static final FindRequestMapper FIND_REQUEST_MAPPER = GWT.create(FindRequestMapper.class);

  private RestUtils() {
    // do nothing
  }

  public static SafeUri createAIPDownloadUri(String aipId) {
    // api/v2/aips/{id}/download

    String b = RodaConstants.API_REST_V2_AIPS + URL.encodeQueryString(aipId)
      + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createAIPPartDownloadUri(String aipId, String part) {
    // api/v2/aips/{aip_id}/download/{part}

    String b = RodaConstants.API_REST_V2_AIPS + URL.encodeQueryString(aipId)
      + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER + RodaConstants.API_SEP + URL.encodeQueryString(part);

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createRepresentationDownloadUri(String aipId, String representationId) {

    // api/v2/{aip_id}/representations/{representation_id}/binary
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(RodaConstants.AIP_REPRESENTATIONS).append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(representationId)).append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationOtherMetadataDownloadUri(String aipId, String representationId) {

    // api/v2/aip/{aip_id}/representations/{representation_id}/other-metadata/binary
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(RodaConstants.AIP_REPRESENTATIONS).append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(representationId)).append(RodaConstants.API_REST_V2_REPRESENTATION_OTHER_METADATA)
      .append(RodaConstants.API_REST_V2_REPRESENTATION_BINARY);

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationFileDownloadUri(String fileUuid) {
    return createRepresentationFileDownloadUri(fileUuid, false);
  }

  public static SafeUri createRepresentationFileDownloadUri(String fileUuid, boolean contentDispositionInline) {
    // api/v2/files/{file_uuid}/preview
    String b = RodaConstants.API_REST_V2_FILES + URL.encodeQueryString(fileUuid)
      + RodaConstants.API_REST_V2_PREVIEW_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createDipDownloadUri(String dipUUID) {
    // api/v2/dips/{uuid}/download
    StringBuilder b = new StringBuilder();
    b.append(RodaConstants.API_REST_V2_DIPS).append(URL.encodeQueryString(dipUUID))
      .append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDipFileDownloadUri(String dipFileUUID) {
    return createDipFileDownloadUri(dipFileUUID, false);
  }

  public static SafeUri createDipFileDownloadUri(String dipFileUUID, boolean contentDispositionInline) {

    // api/v1/dipfiles/{file_uuid}?acceptFormat=bin&inline={inline}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_DIPFILES).append(URL.encodeQueryString(dipFileUUID));
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_INLINE)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(contentDispositionInline);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId, String descId) {
    return createDescriptiveMetadataDownloadUri(aipId, descId, null);
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId, String descId, String versionId) {
    // api/v2/aips/{id}/metadata/descriptive/{descriptive-metadata-id}/download?version-id={versionId}

    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append("metadata").append(RodaConstants.API_SEP).append("descriptive").append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(descId)).append(RodaConstants.API_SEP).append("download");

    // version
    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_START).append("version-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(URL.encodeQueryString(versionId));
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataHTMLUri(String aipId, String descId) {
    return createDescriptiveMetadataHTMLUri(aipId, descId, null);
  }

  public static SafeUri createDescriptiveMetadataHTMLUri(String aipId, String descId, String versionId) {
    // api/v2/aips/{id}/metadata/descriptive/{descriptive-metadata-id}/html?lang=en&version-id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append("metadata").append(RodaConstants.API_SEP).append("descriptive").append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(descId)).append(RodaConstants.API_SEP).append("html");

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append("version-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(URL.encodeQueryString(versionId));
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createTechnicalMetadataHTMLUri(String fileId, String typeId) {
    return createTechnicalMetadataHTMLUri(fileId, typeId, null);
  }

  public static SafeUri createTechnicalMetadataHTMLUri(String fileId, String typeId, String versionId) {
    // /api/v2/files/{fileId}/metadata/technical/{typeId}/html?lang={lang}&versionId={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_FILES).append(URL.encodeQueryString(fileId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_FILES_TECHNICAL_METADATA_TYPE_HTML).append(RodaConstants.API_SEP);

    // type id
    b.append(typeId);

    // html suffix
    b.append(RodaConstants.API_REST_V2_FILES_TECHNICAL_METADATA_TYPE_HTML_SUFFIX);

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    // version id
    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(versionId);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createTechnicalMetadataHTMLUri(String fileId) {
    // /api/v2/files/{fileId}/metadata/preservation/html?lang={lang}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_FILES).append(URL.encodeQueryString(fileId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_FILES_TECHNICAL_METADATA_HTML);

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createTechnicalMetadataDownloadUri(String fileId) {
    // /api/v2/files/{fileId}/metadata/preservation/download
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_FILES).append(URL.encodeQueryString(fileId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_FILES_TECHNICAL_METADATA_DOWNLOAD);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createTechnicalMetadataDownloadUri(String fileUUID, String metadataType, String versionId) {
    // /api/v2/files/{fileUUID}/metadata/technical/{typeId}/download?versionId={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_FILES).append(URL.encodeQueryString(fileUUID)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_FILES_TECHNICAL_METADATA_TYPE_HTML).append(RodaConstants.API_SEP)
      .append((URL.encodeQueryString(metadataType))).append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);
    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(versionId));
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createPreservationMetadataDownloadUri(String aipId) {
    // api/v2/aips/{id}/download/preservation
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER).append(RodaConstants.API_SEP).append("preservation");

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationDescriptiveMetadataDownloadUri(String aipId, String representationId,
    String descId) {
    return createRepresentationDescriptiveMetadataDownloadUri(aipId, representationId, descId, null);
  }

  public static SafeUri createRepresentationDescriptiveMetadataDownloadUri(String aipId, String representationId,
    String descId, String versionId) {
    // api/v2/aips/{aip_id}/representations/{representation_id}/metadata/descriptive/{descriptive_metadata_id}/download

    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_SUB_RESOURCE_REPRESENTATIONS).append(RodaConstants.API_SEP)

      .append(UriUtils.encode(representationId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_SUB_RESOURCE_METADATA).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V2_SUB_RESOURCE_DESCRIPTIVE).append(RodaConstants.API_SEP).append(descId)
      .append(RodaConstants.API_SEP).append(RodaConstants.API_REST_V2_SUB_RESOURCE_DOWNLOAD);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationDescriptiveMetadataHTMLUri(String aipId, String representationId,
    String descId) {
    return createRepresentationDescriptiveMetadataHTMLUri(aipId, representationId, descId, null);
  }

  public static SafeUri createRepresentationDescriptiveMetadataHTMLUri(String aipId, String representationId,
    String descId, String versionId) {
    // api/v2/aips/{id}/representations/{representation-id}/metadata/descriptive/{descriptive-metadata-id}/html?lang=en&version-id={versionId}

    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append("representations").append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId))
      .append(RodaConstants.API_SEP).append("metadata").append(RodaConstants.API_SEP).append("descriptive")
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(descId)).append(RodaConstants.API_SEP).append("html");

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    // version
    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append("version-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(URL.encodeQueryString(versionId));
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createPreservationEventDownloadUri(String eventId) {
    // api/v2/preservation/events/{id}/binary
    String b = RodaConstants.API_REST_V2_PRESERVATION_EVENTS + URL.encodeQueryString(eventId)
      + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createPreservationEventDetailsUri(String eventId) {
    // api/v2/preservation/events/{id}/details/html?lang={lang}
    String b = RodaConstants.API_REST_V2_PRESERVATION_EVENTS + URL.encodeQueryString(eventId)
      + RodaConstants.API_REST_V2_PRESERVATION_EVENTS_DETAILS_HTML + RodaConstants.API_QUERY_START
      + RodaConstants.API_QUERY_KEY_LANG + RodaConstants.API_QUERY_ASSIGN_SYMBOL
      + LocaleInfo.getCurrentLocale().getLocaleName();

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createPreservationAgentDownloadUri(String agentId) {
    // api/v2/preservation/agents/{id}/binary
    String b = RodaConstants.API_REST_V2_PRESERVATION_AGENTS + URL.encodeQueryString(agentId)
      + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  public static String createTransferredResourceUploadUri(String parentUUID, String locale) {
    // api/v2/transfers/create/resource?parent-uuid={parentUUID}&locale={locale}&commit=true
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_TRANSFERRED_RESOURCES);

    b.append(RodaConstants.API_REST_V2_TRANSFERRED_RESOURCE_CREATE_RESOURCE);

    if (parentUUID != null || locale != null) {
      b.append(RodaConstants.API_QUERY_START);
    }

    if (parentUUID != null) {
      b.append("parent-uuid").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(parentUUID);
    }

    if (parentUUID != null && locale != null) {
      b.append(RodaConstants.API_QUERY_SEP);
    }

    if (locale != null) {
      b.append(RodaConstants.LOCALE).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(locale)
        .append(RodaConstants.API_QUERY_SEP);
    }

    b.append(RodaConstants.API_QUERY_PARAM_COMMIT).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append("True");
    return b.toString();
  }

  public static String createFileUploadUri(String aipId, String representationId, List<String> directory,
    String details) {

    // api/v2/files?aip-id={aipId}&representation-id={representationdId}&folder={folder[0]}&folder={folder[1]}&details={details}

    StringBuilder b = new StringBuilder();

    // base uri
    b.append(RodaConstants.API_REST_V2_FILES).append("upload").append(RodaConstants.API_QUERY_START);
    b.append("aip-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(aipId));
    b.append(RodaConstants.API_QUERY_SEP);
    b.append("representation-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encodeQueryString(representationId));
    b.append(RodaConstants.API_QUERY_SEP);

    for (String folderPath : directory) {
      b.append(RodaConstants.API_PATH_PARAM_FOLDER).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(URL.encodeQueryString(folderPath));
      b.append(RodaConstants.API_QUERY_SEP);
    }
    b.append(RodaConstants.API_QUERY_PARAM_DETAILS).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(details);

    return b.toString();
  }

  public static SafeUri createTransferredResourceDownloadUri(String resourceId) {
    // api/v2/transfers/{transferred_resource_uuid}/binary

    return UriUtils.fromSafeConstant(
      RodaConstants.API_REST_V2_TRANSFERRED_RESOURCES + resourceId + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);
  }

  public static SafeUri createRepresentationInformationDownloadUri(String riId) {
    // api/v2/representation-information/{id}/binary

    String b = RodaConstants.API_REST_V2_REPRESENTATION_INFORMATION + riId + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  /**
   * Creates a uri to
   * api/v1/distributed_instances/synchronization/{instanceIdentifier}.
   *
   * @param instanceIdentifier
   *          the instanceIdentifier.
   * @return {@link SafeUri}
   *
   */
  public static SafeUri createLastSynchronizationDownloadUri(String instanceIdentifier, String entityClass,
    String type) {

    // api/v1/distributed_instances/download_last_sync/{instanceIdentifier}
    final StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE).append(RodaConstants.API_PATH_LAST_SYNC_STATUS)
      .append(RodaConstants.API_SEP).append(instanceIdentifier).append("?class=").append(entityClass).append("&")
      .append("type=").append(type);
    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createThemeResourceUri(String resourceId, String defaultResourceId, boolean inline) {
    return createThemeResourceUri(resourceId, defaultResourceId, RodaConstants.ResourcesTypes.INTERNAL.toString(),
      inline);
  }

  public static SafeUri createThemeResourceUri(String resourceId, String defaultResourceId, String resourceType,
    boolean inline) {
    // api/v2/themes/?resource-id={resourceId}&default-resource-id={defaultResourceId}&resource-type={resourceType}
    StringBuilder b = new StringBuilder();

    b.append(RodaConstants.API_REST_V2_THEME).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_V2_QUERY_PARAM_RESOURCE_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encode(resourceId));

    if (defaultResourceId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_V2_QUERY_PARAM_DEFAULT_RESOURCE_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(defaultResourceId);
    }

    if (resourceType != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_V2_QUERY_PARAM_RESOURCE_TYPE)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(resourceType);
    }

    if (inline) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_INLINE)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(true);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createJobAttachmentDownloadUri(String jobId, String attachmentId) {
    // api/v2/jobs/{id}/attachment/{attachment-id}
    return UriUtils.fromSafeConstant(RodaConstants.API_REST_V2_JOBS + jobId + "/attachment/" + attachmentId);
  }

  public static SafeUri createJobReportsHTMLUri(String jobId) {
    // api/v2/jobs/{id}/reports
    return UriUtils.fromSafeConstant(RodaConstants.API_REST_V2_JOBS + jobId + "/reports");
  }

  public static <T extends IsIndexed> void requestCSVExport(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, boolean onlyActive, boolean exportFacets, String filename) {
    // api/v2/<resource>/export/csv?findRequest={findRequest}

    String url = retrieveRestAPIBase(classToReturn) + "export/csv";

    FindRequest request = FindRequest.getBuilder(filter, onlyActive).withSorter(sorter).withSublist(sublist)
      .withFacets(facets).withExportFacets(exportFacets).withFilename(filename).build();

    final FormPanel form = new FormPanel();
    form.setAction(URL.encode(url));
    form.setMethod(FormPanel.METHOD_POST);
    form.setEncoding(FormPanel.ENCODING_URLENCODED);
    FlowPanel layout = new FlowPanel();
    form.setWidget(layout);
    layout.add(new Hidden("findRequest", FIND_REQUEST_MAPPER.write(request)));

    form.setVisible(false);
    RootPanel.get().add(form);

    // using submit instead of submit completed because Chrome doesn't create
    // the other event
    form.addSubmitHandler(event -> {

      Timer timer = new Timer() {

        @Override
        public void run() {
          RootPanel.get().remove(form);
        }
      };

      // remove form 10 seconds in the future
      timer.schedule(10000);
    });

    form.submit();
  }

  public static SafeUri createDisposalConfirmationHTMLUri(String disposalConfirmationId, boolean toPrint) {
    // api/v2/disposal/confirmations/{id}/report/html?to-print=<true|false>

    final StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_DISPOSAL_CONFIRMATION).append(disposalConfirmationId)
      .append(RodaConstants.API_SEP).append(RodaConstants.API_REST_V2_DISPOSAL_CONFIRMATION_REPORT)
      .append(RodaConstants.API_SEP).append(RodaConstants.API_REST_V2_DISPOSAL_CONFIRMATION_REPORT_HTML);

    if (toPrint) {
      b.append(RodaConstants.API_QUERY_START)
        .append(RodaConstants.API_REST_V2_DISPOSAL_CONFIRMATION_QUERY_PARAM_TO_PRINT)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(true);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static interface FindRequestMapper extends ObjectMapper<FindRequest> {
  }

  private static <T extends IsIndexed> String retrieveRestAPIBase(Class<T> classToReturn) {
    switch (classToReturn.getName()) {
      case "org.roda.core.data.v2.ip.IndexedAIP":
        return RodaConstants.API_REST_V2_AIPS;
      case "org.roda.core.data.v2.ip.IndexedRepresentation":
        return RodaConstants.API_REST_V2_REPRESENTATIONS_ENDPOINT;
      case "org.roda.core.data.v2.jobs.IndexedJob":
        return RodaConstants.API_REST_V2_JOBS;
      case "org.roda.core.data.v2.ip.TransferredResource":
        return RodaConstants.API_REST_V2_TRANSFERRED_RESOURCES;
      case "org.roda.core.data.v2.ip.IndexedFile":
        return RodaConstants.API_REST_V2_FILES;
      case "org.roda.core.data.v2.jobs.IndexedReport":
        return RodaConstants.API_REST_V2_JOB_REPORT;
      case "org.roda.core.data.v2.log.LogEntry":
        return RodaConstants.API_REST_V2_AUDIT_LOGS;
      case "org.roda.core.data.v2.notifications":
        return RodaConstants.API_REST_V2_NOTIFICATIONS;
      case "org.roda.core.data.v2.user.RODAMember":
        return RodaConstants.API_REST_V2_MEMBERS;
      case "org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation":
        return RodaConstants.API_REST_V2_DISPOSAL_CONFIRMATION;
      case "org.roda.core.data.v2.ri.RepresentationInformation":
        return RodaConstants.API_REST_V2_REPRESENTATION_INFORMATION;
      case "org.roda.core.data.v2.risks.IndexedRisk":
        return RodaConstants.API_REST_V2_RISKS;
      case "org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent":
        return RodaConstants.API_REST_V2_PRESERVATION_EVENTS;
      case "org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent":
        return RodaConstants.API_REST_V2_PRESERVATION_AGENTS;
      default:
        return null;
    }
  }
}
