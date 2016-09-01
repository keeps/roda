/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import org.roda.core.data.common.RodaConstants;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class RestUtils {

  public static SafeUri createAIPDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createAIPPartDownloadUri(String aipId, String part) {

    // api/v1/aips/{aip_id}/{part}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
      .append(UriUtils.encode(part));

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationDownloadUri(String representationUUID) {

    // api/v1/representations/{representation_uuid}/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(UriUtils.encode(representationUUID))
      .append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationPartDownloadUri(String representationUUID, String part) {

    // api/v1/representations/{representation_uuid}/{part}
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(UriUtils.encode(representationUUID))
      .append(RodaConstants.API_SEP).append(UriUtils.encode(part));

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createRepresentationFileDownloadUri(String fileUuid) {

    // api/v1/files/{file_uuid}?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_FILES).append(UriUtils.encode(fileUuid));
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/descriptive_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
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
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
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
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
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

  public static SafeUri createPreservationMetadataDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/preservation_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_AIPS).append(UriUtils.encode(aipId)).append(RodaConstants.API_SEP)
      .append(RodaConstants.API_PRESERVATION_METADATA).append(RodaConstants.API_SEP);
    // accept format attribute
    b.append(RodaConstants.API_QUERY_START).append(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT)
      .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_ZIP);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static String createRepresentationPreservationMetadataUri(String representationUUID, int startAgent,
    int limitAgent, int startEvent, int limitEvent, int startFile, int limitFile) {
    // api/v1/representations/{representation_uuid}/descriptive_metadata/?acceptFormat=zip
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(RodaConstants.API_REST_V1_REPRESENTATIONS).append(UriUtils.encode(representationUUID))
      .append(RodaConstants.API_SEP).append(RodaConstants.API_PRESERVATION_METADATA).append(RodaConstants.API_SEP);
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

  public static String createTransferredResourceUploadUri(String parentUUID, String locale) {
    // api/v1/transferred/?parentUUID={parentUUID}&locale={locale}
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
      b.append(RodaConstants.LOCALE).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(locale);
    }

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

  public static SafeUri createThemeResourceUri(String resourceId, String defaultResourceId, boolean inline) {
    // api/v1/theme/?resource_id={resourceId}&default_resource_od={defaultResourceId}
    StringBuilder b = new StringBuilder();

    b.append(RodaConstants.API_REST_V1_THEME).append(RodaConstants.API_QUERY_START)
      .append(RodaConstants.API_QUERY_PARAM_RESOURCE_ID).append(RodaConstants.API_QUERY_ASSIGN_SYMBOL)
      .append(resourceId);

    if (defaultResourceId != null) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(defaultResourceId);
    }

    if (inline) {
      b.append(RodaConstants.API_QUERY_SEP).append(RodaConstants.API_QUERY_PARAM_INLINE)
        .append(RodaConstants.API_QUERY_ASSIGN_SYMBOL).append(inline);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }
}
