/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.ArrayList;
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

    // api/v1/representations/{aip_id}/{representation_id}/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationOtherMetadataDownloadUri(String aipId, String representationId) {

    // api/v1/representations/{aip_id}/{representation_id}/otherMetadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_REST_V1_REPRESENTATION_OTHER_METADATA).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

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

    // api/v1/files/{file_uuid}?acceptFormat=bin&inline={inline}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_FILES).append(URL.encodeQueryString(fileUuid));
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_INLINE)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(contentDispositionInline);

    return UriUtils.fromSafeConstant(b.toString());
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
    return createRepresentationDescriptiveMetadataHTMLUri(aipId, representationId, descId, null);
  }

  public static SafeUri createRepresentationDescriptiveMetadataHTMLUri(String aipId, String representationId,
    String descId, String versionId) {
    // api/v1/representations/{aip_id}/{representation_id}/descriptive_metadata/{descId}?acceptFormat=html&version_id={versionId}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(URL.encodeQueryString(aipId))
      .append(RodaConstants.API_SEP).append(URL.encodeQueryString(representationId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_DESCRIPTIVE_METADATA).append(RodaConstants.API_SEP).append(descId);

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

  public static SafeUri createPreservationEventDetailsUri(String eventId, String aipId, String representationUUID,
    String fileUUID, boolean onlyDetails, String acceptFormat) {
    // api/v1/events?id={event_id}&aipId={aip_id}&representationUUID={representationUUID}&
    // fileUUID={fileUUID}&onlyDetails={onlyDetails}&acceptFormat={format}&lang={lang}
    StringBuilder b = new StringBuilder();

    b.append(RodaConstants.API_REST_V1_EVENTS).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_PARAM_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encodeQueryString(eventId));

    if (aipId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_PATH_PARAM_AIP_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(aipId));
    }

    if (representationUUID != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_PATH_PARAM_REPRESENTATION_UUID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(representationUUID));
    }

    if (fileUUID != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_PATH_PARAM_FILE_UUID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(fileUUID));
    }

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_ONLY_DETAILS)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(Boolean.toString(onlyDetails)));

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(acceptFormat));

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_LANG)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createPreservationAgentUri(String agentId, String acceptFormat) {
    // api/v1/agents?id={agent_id}&acceptFormat={format}
    StringBuilder b = new StringBuilder();

    b.append(RodaConstants.API_REST_V1_AGENTS).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_PARAM_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encodeQueryString(agentId));

    b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(URL.encodeQueryString(acceptFormat));

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static String createTransferredResourceUploadUri(String parentUUID, String locale) {
    // api/v1/transfers/?parentUUID={parentUUID}&locale={locale}&commit=true
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_RESOURCES);

    if (parentUUID != null || locale != null) {
      b.append(RodaConstants.API_QUERY_START);
    }

    if (parentUUID != null) {
      b.append(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
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
    // api/v1/files?aipId={aipId}&representationId={representationdId}&folder={folder[0]}&folder={folder[1]}&details={details}

    StringBuilder b = new StringBuilder();

    // base uri
    b.append(RodaConstants.API_REST_V1_FILES).append(RodaConstants.API_QUERY_START);
    b.append(RodaConstants.API_PATH_PARAM_AIP_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encodeQueryString(aipId));
    b.append(RodaConstants.API_QUERY_SEP);
    b.append(RodaConstants.API_PATH_PARAM_REPRESENTATION_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
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
    // api/v1/transferred/{transferred_resource_uuid}?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_RESOURCES).append(resourceId).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationInformationDownloadUri(String riId) {
    // api/v1/representation_information/{representation_information_id}?acceptFormat=xml
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATION_INFORMATION).append(riId).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
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
    // api/v1/theme/?resource_id={resourceId}&default_resource_od={defaultResourceId}&resource_type={resourceType}
    StringBuilder b = new StringBuilder();

    b.append(RodaConstants.API_REST_V1_THEME).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_PARAM_RESOURCE_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(URL.encode(resourceId));

    if (defaultResourceId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(defaultResourceId);
    }

    if (resourceType != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_RESOURCE_TYPE)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(resourceType);
    }

    if (inline) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_INLINE)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(inline);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createJobAttachmentDownloadUri(String jobId, String attachmentId) {
    StringBuilder b = new StringBuilder();
    // api/v1/jobs/{jobId}/attachment/{attachmentId}
    b.append(RodaConstants.API_REST_V1_JOBS).append(jobId).append("/attachment/").append(attachmentId);
    return UriUtils.fromSafeConstant(b.toString());
  }

  public static interface FindRequestMapper extends ObjectMapper<FindRequest> {
  }

  private static FindRequestMapper FIND_REQUEST_MAPPER = GWT.create(FindRequestMapper.class);

  public static <T extends IsIndexed> void requestCSVExport(Class<T> classToReturnName, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, boolean onlyActive, boolean exportFacets, String filename) {
    requestCSVExport(classToReturnName.getName(), filter, sorter, sublist, facets, onlyActive, exportFacets, filename);
  }

  public static <T extends IsIndexed> void requestCSVExport(String classToReturnName, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, boolean onlyActive, boolean exportFacets, String filename) {
    // api/v1/index/findFORM?type=csv

    String url = RodaConstants.API_REST_V1_INDEX + "findFORM";
    FindRequest request = new FindRequest(classToReturnName, filter, sorter, sublist, facets, onlyActive, exportFacets,
      filename, new ArrayList<>());

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

}
