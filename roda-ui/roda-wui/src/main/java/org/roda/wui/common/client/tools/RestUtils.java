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
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

public class RestUtils {

  private static final FindRequestMapper FIND_REQUEST_MAPPER = GWT.create(FindRequestMapper.class);

  private RestUtils() {
    // do nothing
  }

  public static SafeUri createAIPDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createAIPPartDownloadUri(String aipId, String part) {

    // api/v1/aips/{aip_id}/{part}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(part));

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationDownloadUri(String aipId, String representationId) {

    // api/v2/representations/{aip_id}/{representation_id}/binary
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_REPRESENTATIONS).append(RodaConstants.API_SEP).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationOtherMetadataDownloadUri(String aipId, String representationId) {

    // api/v2/representations/{aip_id}/{representation_id}/other-metadata/binary
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_REPRESENTATIONS).append(RodaConstants.API_SEP).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_REST_V2_REPRESENTATION_OTHER_METADATA)
      .append(RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationPartDownloadUri(String aipId, String representationId, String part) {

    // api/v1/representations/{aip_id}/{representation_id}/{part}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(part));

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationFileDownloadUri(String fileUuid) {
    return createRepresentationFileDownloadUri(fileUuid, false);
  }

  public static SafeUri createRepresentationFileDownloadUri(String fileUuid, boolean contentDispositionInline) {
    // api/v2/files/{file_uuid}/binary
    String b = RodaConstants.API_REST_V2_FILES + URL.encodeQueryString(fileUuid)
      + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER;

    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createDipDownloadUri(String dipUUID) {

    // api/v1/dips/{dip_uuid}?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_DIPS).append(URL.encodeQueryString(dipUUID));
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

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

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/descriptive_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_DESCRIPTIVE_METADATA).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId, String descId) {
    return createDescriptiveMetadataDownloadUri(aipId, descId, null);
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId, String descId, String versionId) {

    // api/v1/aips/{aip_id}/descriptive_metadata/{descId}?acceptFormat=xml&version_id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_DESCRIPTIVE_METADATA).append(RodaConstants.API_SEP).append(descId);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(versionId);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataHTMLUri(String aipId, String descId) {
    return createDescriptiveMetadataHTMLUri(aipId, descId, null);
  }

  public static SafeUri createDescriptiveMetadataHTMLUri(String aipId, String descId, String versionId) {
    // api/v1/aips/{aip_id}/descriptive_metadata/{descId}?acceptFormat=html&version_id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_DESCRIPTIVE_METADATA);

    if (descId != null) {
      b.append(RodaConstants.API_SEP).append(descId);
    }

    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML);

    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(versionId);
    }

    // locale
    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createTechnicalMetadataHTMLUri(String aipId, String fileId, String acceptFormat,
    String versionId) {
    // api/v1/aips/{aip_id}/preservation_metadata/{file_id}?acceptFormat={acceptFormat}&version_id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_PRESERVATION_METADATA);

    if (fileId != null) {
      b.append(RodaConstants.API_SEP).append(fileId);
    }

    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(acceptFormat);

    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(versionId);
    }

    // locale
    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createPreservationMetadataDownloadUri(String aipId) {
    // api/v1/aips/{aip_id}/preservation_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(URL.encodeQueryString(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_PRESERVATION_METADATA).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static String createRepresentationPreservationMetadataUri(String aipId, String representationId,
    int startAgent, int limitAgent, int startEvent, int limitEvent, int startFile, int limitFile) {
    // api/v1/representations/{aip_id}/{representation_id}/preservation_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_PRESERVATION_METADATA).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    // start and limits
    b.append(RodaConstants.API_QUERY_SEP).append("startAgent").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(startAgent);
    b.append(RodaConstants.API_QUERY_SEP).append("limitAgent").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(limitAgent);
    b.append(RodaConstants.API_QUERY_SEP).append("startEvent").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(startEvent);
    b.append(RodaConstants.API_QUERY_SEP).append("limitEvent").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(limitEvent);
    b.append(RodaConstants.API_QUERY_SEP).append("startFile").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(startFile);
    b.append(RodaConstants.API_QUERY_SEP).append("limitFile").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(limitFile);

    // locale
    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return b.toString();
  }

  public static SafeUri createRepresentationDescriptiveMetadataDownloadUri(String aipId, String representationId,
    String descId) {
    return createRepresentationDescriptiveMetadataDownloadUri(aipId, representationId, descId, null);
  }

  public static SafeUri createRepresentationDescriptiveMetadataDownloadUri(String aipId, String representationId,
    String descId, String versionId) {
    // api/v1/representations/{aip_id}/{representation_id}/descriptive_metadata/{descId}?acceptFormat=xml&version_id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_DESCRIPTIVE_METADATA).append(RodaConstants.API_SEP).append(descId);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    if (versionId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_VERSION_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(versionId);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationDescriptiveMetadataHTMLUri(String aipId, String representationId,
    String descId) {

    // api/v2/aips/{id}/representations/{representation-id}/metadata/descriptive/{descriptive-metadata-id}/html

    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V2_AIPS).append(RodaConstants.API_SEP).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append("representations").append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
        .append("metadata").append(RodaConstants.API_SEP).append("descriptive").append(RodaConstants.API_SEP)
      .append(URL.encodeQueryString(descId)).append(RodaConstants.API_SEP).append("html");

    // locale
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

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
      + RodaConstants.API_REST_V2_PRESERVATION_EVENTS_DETAILS_HTML
      + RodaConstants.API_QUERY_START + RodaConstants.API_QUERY_KEY_LANG + RodaConstants.API_QUERY_ASSIGN_SYMBOL
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
    b.append(RodaConstants.API_REST_V2_RESOURCES);

    b.append(RodaConstants.API_REST_V2_TRANSFERRED_RESOURCE_CREATE_RESOURCE);

    if (parentUUID != null || locale != null) {
      b.append(RodaConstants.API_QUERY_START);
    }

    if (parentUUID != null) {
      b.append("parent-uuid").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(parentUUID);
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
    b.append("aip-id").append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encodeQueryString(aipId));
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

    return UriUtils
      .fromSafeConstant(RodaConstants.API_REST_V2_RESOURCES + resourceId + RodaConstants.API_REST_V2_DOWNLOAD_HANDLER);
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
      .append(RodaConstants.API_V2_QUERY_PARAM_RESOURCE_ID)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encode(resourceId));

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
    // api/v1/jobs/{jobId}/attachment/{attachmentId}
    return UriUtils.fromSafeConstant(RodaConstants.API_REST_V2_JOBS + jobId + "/attachment/" + attachmentId);
  }

  public static <T extends IsIndexed> void requestCSVExport(Class<T> classToReturnName, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, boolean onlyActive, boolean exportFacets, String filename) {
    requestCSVExport(classToReturnName.getName(), filter, sorter, sublist, facets, onlyActive, exportFacets, filename);
  }

  public static <T extends IsIndexed> void requestCSVExport(String classToReturnName, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, boolean onlyActive, boolean exportFacets, String filename) {
    // api/v1/index/findFORM?type=csv

    String url = RodaConstants.API_REST_V1_INDEX + "findFORM";
    FindRequest request = FindRequest.getBuilder(classToReturnName, filter, onlyActive).withSorter(sorter)
      .withSublist(sublist).withFacets(facets).withExportFacets(exportFacets).withFilename(filename).build();

    final FormPanel form = new FormPanel();
    form.setAction(URL.encode(url));
    form.setMethod(FormPanel.METHOD_POST);
    form.setEncoding(FormPanel.ENCODING_URLENCODED);
    FlowPanel layout = new FlowPanel();
    form.setWidget(layout);
    layout.add(new Hidden("findRequest", FIND_REQUEST_MAPPER.write(request)));
    layout.add(new Hidden("type", "csv"));

    form.setVisible(false);
    RootPanel.get().add(form);

    // using submit instead of submit completed because Chrome doesn't created
    // the other event
    form.addSubmitHandler(new SubmitHandler() {

      @Override
      public void onSubmit(SubmitEvent event) {

        Timer timer = new Timer() {

          @Override
          public void run() {
            RootPanel.get().remove(form);
          }
        };

        // remove form 10 seconds in the future
        timer.schedule(10000);
      }
    });

    form.submit();
  }

  public static interface FindRequestMapper extends ObjectMapper<FindRequest> {
  }
}
