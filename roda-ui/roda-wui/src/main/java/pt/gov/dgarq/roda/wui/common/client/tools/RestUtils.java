package pt.gov.dgarq.roda.wui.common.client.tools;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class RestUtils {

  private static final String SEP = "/";
  private static final String REST_V1_AIPS = "api/v1/aips/";
  private static final String DATA = "data";
  private static final String DESCRIPTIVE_METADATA = "descriptive_metadata";
  private static final String PRESERVATION_METADATA = "preservation_metadata";
  private static final String QUERY_START = "?";
  private static final String ATTR_ASSIGN_SYMBOL = "=";
  private static final String QUERY_SEP = "&";

  private static final String ATTR_ACCEPT_FORMAT = "acceptFormat";
  private static final String ATTR_ACCEPT_FORMAT_BIN = "bin";
  private static final String ATTR_ACCEPT_FORMAT_XML = "xml";
  private static final String ATTR_ACCEPT_FORMAT_HTML = "html";

  private static final String ATTR_LANG = "lang";

  public static SafeUri createRepresentationDownloadUri(String aipId, String repId) {

    // api/v1/aips/{aip_id}/data/{rep_id}/?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(REST_V1_AIPS).append(UriUtils.encode(aipId)).append(SEP).append(DATA).append(SEP)
      .append(UriUtils.encode(repId)).append(SEP);
    // accept format attribute
    b.append(QUERY_START).append(ATTR_ACCEPT_FORMAT).append(ATTR_ASSIGN_SYMBOL).append(ATTR_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/descriptive_metadata/?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(REST_V1_AIPS).append(UriUtils.encode(aipId)).append(SEP).append(DESCRIPTIVE_METADATA).append(SEP);
    // accept format attribute
    b.append(QUERY_START).append(ATTR_ACCEPT_FORMAT).append(ATTR_ASSIGN_SYMBOL).append(ATTR_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createDescriptiveMetadataDownloadUri(String aipId, String descId) {

    // api/v1/aips/{aip_id}/descriptive_metadata/{descId}?acceptFormat=xml
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(REST_V1_AIPS).append(UriUtils.encode(aipId)).append(SEP).append(DESCRIPTIVE_METADATA).append(SEP)
      .append(descId);
    // accept format attribute
    b.append(QUERY_START).append(ATTR_ACCEPT_FORMAT).append(ATTR_ASSIGN_SYMBOL).append(ATTR_ACCEPT_FORMAT_XML);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static String createDescriptiveMetadataHTMLUri(String aipId, String descId) {
    // api/v1/aips/{aip_id}/descriptive_metadata/{descId}?acceptFormat=xml
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(REST_V1_AIPS).append(UriUtils.encode(aipId)).append(SEP).append(DESCRIPTIVE_METADATA).append(SEP)
      .append(descId);
    // accept format attribute
    b.append(QUERY_START).append(ATTR_ACCEPT_FORMAT).append(ATTR_ASSIGN_SYMBOL).append(ATTR_ACCEPT_FORMAT_HTML);

    // locale
    b.append(QUERY_SEP).append(ATTR_LANG).append(ATTR_ASSIGN_SYMBOL).append(LocaleInfo.getCurrentLocale().getLocaleName());

    return b.toString();
  }

  public static SafeUri createPreservationMetadataDownloadUri(String aipId) {

    // api/v1/aips/{aip_id}/preservation_metadata/?acceptFormat=bin
    StringBuilder b = new StringBuilder();
    // base uri
    b.append(REST_V1_AIPS).append(UriUtils.encode(aipId)).append(SEP).append(PRESERVATION_METADATA).append(SEP);
    // accept format attribute
    b.append(QUERY_START).append(ATTR_ACCEPT_FORMAT).append(ATTR_ASSIGN_SYMBOL).append(ATTR_ACCEPT_FORMAT_BIN);

    return UriUtils.fromSafeConstant(b.toString());
  }

}
