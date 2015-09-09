package pt.gov.dgarq.roda.wui.common.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class ServerTools {
  /**
   * Parse a locale string into a Locale
   * 
   * @param localeString
   *          the locale string, e.g. en_US
   * @return
   */
  public static Locale parseLocale(String localeString) {
    String[] localeArgs = localeString.split("_");
    Locale locale = null;
    if (localeArgs.length == 1) {
      locale = new Locale(localeArgs[0]);
    } else if (localeArgs.length == 2) {
      locale = new Locale(localeArgs[0], localeArgs[1]);
    } else if (localeArgs.length == 3) {
      locale = new Locale(localeArgs[0], localeArgs[1], localeArgs[2]);
    }

    return locale;
  }

  /**
   * Encode XML entities and unicode control chars
   * 
   * @param s
   *          the string to encode
   * @return the encoded string
   */
  public static String encodeXML(String s) {
    String ret;
    if (s != null) {
      ret = "";
      for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch == '<') {
          ret += "&lt;";
        } else if (ch == '>') {
          ret += "&gt;";
        } else if (ch == '"') {
          ret += "&quot;";
        } else if (ch == '\'') {
          ret += "&apos;";
        } else if (ch == '&') {
          ret += "&amp;";
        } else if (ch < 0x20 && ch != 0x9 && ch != 0xD && ch != 0xA) {
          ret += (ch <= 0xf ? "\\u000" : "\\u00") + Integer.toHexString(ch).toUpperCase();
        } else {
          ret += ch;
        }
      }
    } else {
      ret = null;
    }
    return ret;
  }

  /**
   * Encode a CSV value, always adding quotes to the value and duplicating
   * existing quotes
   * 
   * @param s
   * @return the encoded CSV value
   */
  public static String encodeCSV(String s) {
    String ret = new String(s);
    ret.replaceAll("\"", "\"\"");
    return "\"" + ret + "\"";
  }

  /**
   * Check if a string matches an URL
   * 
   * @param s
   * @return true if it matches
   */
  public static boolean isURL(String s) {
    boolean isURL;
    try {
      new URL(s);
      isURL = true;
    } catch (MalformedURLException e) {
      isURL = false;
    }
    return isURL;
  }

}
