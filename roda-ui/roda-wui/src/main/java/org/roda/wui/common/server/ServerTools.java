/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.MetadataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class ServerTools {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTools.class);

  private ServerTools() {
    // do nothing
  }

  /**
   * Parse a locale string into a Locale
   * 
   * @param localeString
   *          the locale string, e.g. en_US
   * @return
   */
  public static Locale parseLocale(String localeString) {
    Locale locale = null;
    if (StringUtils.isNotBlank(localeString)) {
      String[] localeArgs = localeString.split("_");

      if (localeArgs.length == 1) {
        locale = new Locale(localeArgs[0]);
      } else if (localeArgs.length == 2) {
        locale = new Locale(localeArgs[0], localeArgs[1]);
      } else if (localeArgs.length == 3) {
        locale = new Locale(localeArgs[0], localeArgs[1], localeArgs[2]);
      }
    } else {
      locale = Locale.ENGLISH;
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
    if (s != null) {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch == '<') {
          ret.append("&lt;");
        } else if (ch == '>') {
          ret.append("&gt;");
        } else if (ch == '"') {
          ret.append("&quot;");
        } else if (ch == '\'') {
          ret.append("&apos;");
        } else if (ch == '&') {
          ret.append("&amp;");
        } else if (ch < 0x20 && ch != 0x9 && ch != 0xD && ch != 0xA) {
          ret.append(ch <= 0xf ? "\\u000" : "\\u00").append(Integer.toHexString(ch).toUpperCase());
        } else {
          ret.append(ch);
        }
      }

      return ret.toString();
    } else {
      return null;
    }
  }

  /**
   * Encode a CSV value, always adding quotes to the value and duplicating
   * existing quotes
   * 
   * @param s
   * @return the encoded CSV value
   */
  public static String encodeCSV(String s) {
    return "\"" + s.replaceAll("\"", "\"\"") + "\"";
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

  public static Set<MetadataValue> transform(String content) {
    Set<MetadataValue> values = new TreeSet<>();

    if (content == null) {
      return values;
    }

    Set<String> addedTags = new HashSet<>();
    Handlebars handlebars = new Handlebars();

    Template tmpl;
    try {
      handlebars.registerHelper("field", (context, options) -> {
        if (options.hash.containsKey("name")) {
          String tagID = (String) options.hash.get("name");
          if (context != null && !addedTags.contains(tagID)) {
            HashMap<String, String> newHash = new HashMap<>();
            for (Entry<String, Object> hashEntry : options.hash.entrySet()) {
              newHash.put(hashEntry.getKey(), hashEntry.getValue().toString());
            }
            values.add(new MetadataValue(tagID, new HashMap<>(newHash)));
            addedTags.add(tagID);
          }
        }
        return options.fn();
      });
      // Prevent errors from unknown helpers
      handlebars.registerHelperMissing((o, options) -> options.fn());

      tmpl = handlebars.compileInline(content);
      tmpl.apply(new HashMap<>());
    } catch (IOException e) {
      LOGGER.error("Error getting the MetadataValue list from the template");
    }
    return values;
  }

  public static String autoGenerateAIPValue(IndexedAIP aip, User user, String generator) {
    String result = null;
    switch (generator) {
      case "now":
        result = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        break;
      case "id":
        result = aip.getId();
        break;
      case "title":
        result = aip.getTitle();
        break;
      case "language":
        result = Locale.getDefault().getDisplayLanguage();
        break;
      case "parentid":
        result = aip.getParentID();
        break;
      case "level":
        result = aip.getLevel();
        break;
      case "full-name":
        result = user.getFullName();
        break;
      case "email":
        result = user.getEmail();
        break;
      default:
        break;
    }
    return result;
  }

  public static String autoGenerateRepresentationValue(IndexedRepresentation representation, String generator) {
    String result = null;
    switch (generator) {
      case "now":
        result = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        break;
      case "id":
        result = representation.getId();
        break;
      default:
        break;
    }
    return result;
  }

  public static List<String> applyXpath(String xml, String xpathString) {
    List<String> result = new ArrayList<>();
    try {
      Processor proc = new Processor(false);
      XPathCompiler xpath = proc.newXPathCompiler();
      DocumentBuilder builder = proc.newDocumentBuilder();

      // Load the XML document.
      StringReader reader = new StringReader(xml);
      XdmNode doc = builder.build(new StreamSource(reader));

      // Compile the xpath
      XPathSelector selector = xpath.compile(xpathString).load();
      selector.setContextItem(doc);

      // Evaluate the expression.
      XdmValue nodes = selector.evaluate();

      for (XdmItem item : nodes) {
        result.add(item.toString());
      }

    } catch (Exception e) {
      LOGGER.error("Error applying XPath", e);
    }
    return result;
  }

}
