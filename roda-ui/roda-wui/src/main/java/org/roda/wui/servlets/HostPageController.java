/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Serves the GWT host pages, injecting the configured default locale
 * ({@value #DEFAULT_LOCALE_PROPERTY}) as a GWT meta property so it takes
 * precedence over the browser language. The {@code locale} query parameter
 * (used by the language menu) still overrides it.
 */
@Controller
public class HostPageController {
  private static final Logger LOGGER = LoggerFactory.getLogger(HostPageController.class);

  private static final String DEFAULT_LOCALE_PROPERTY = "ui.locale.default";
  private static final Pattern LOCALE_PATTERN = Pattern.compile("^[A-Za-z]{2,3}(_[A-Za-z]{2,4})?$");
  private static final Pattern HEAD_PATTERN = Pattern.compile("<head>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_LANG_PATTERN = Pattern.compile("(<html[^>]*\\slang=\")[^\"]*(\")",
    Pattern.CASE_INSENSITIVE);

  private final Map<String, String> hostPages = new ConcurrentHashMap<>();

  @GetMapping(value = {"/Main.html", "/Portal.html"}, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> hostPage(HttpServletRequest request) {
    String page = hostPages.computeIfAbsent(request.getServletPath(), HostPageController::loadHostPage);
    String locale = getDefaultLocale();
    if (locale != null) {
      String metaTag = "<meta name=\"gwt:property\" content=\"locale=" + locale + "\"/>";
      page = HEAD_PATTERN.matcher(page).replaceFirst(Matcher.quoteReplacement("<head>\n    " + metaTag));
      page = HTML_LANG_PATTERN.matcher(page)
        .replaceFirst("$1" + Matcher.quoteReplacement(locale.replace('_', '-')) + "$2");
    }

    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).contentType(MediaType.TEXT_HTML).body(page);
  }

  private static String loadHostPage(String path) {
    try (InputStream in = new ClassPathResource("static" + path).getInputStream()) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not load host page " + path, e);
    }
  }

  private static String getDefaultLocale() {
    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    if (rodaConfig == null) {
      return null;
    }

    String locale = StringUtils.trimToNull(rodaConfig.getString(DEFAULT_LOCALE_PROPERTY, null));
    if (locale != null && !LOCALE_PATTERN.matcher(locale).matches()) {
      LOGGER.warn("Ignoring invalid value for {}: '{}'", DEFAULT_LOCALE_PROPERTY, locale);
      return null;
    }
    return locale;
  }
}
