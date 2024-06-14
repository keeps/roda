package org.roda.core.plugins.base.security;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SecurityPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPluginUtils.class);

  public static URIBuilder getServiceURI(HttpServletRequest httpRequest, List<String> paramsToRemove) {
    final String url = httpRequest.getRequestURL().toString();
    final String requestURI = httpRequest.getRequestURI();
    final String path = httpRequest.getParameter("path");
    final String hash = httpRequest.getParameter("hash");

    Map<String, String[]> parameterMap = new HashMap<>(httpRequest.getParameterMap());
    parameterMap.remove("path");
    parameterMap.remove("hash");
    for (String param : paramsToRemove) {
      parameterMap.remove(param);
    }

    URIBuilder uri = new URIBuilder();

    // Setting RODA host via config
    String rodaServerName = RodaCoreFactory.getRodaConfigurationAsString("core.plugins.external.security.roda.serviceUrl");
    if (StringUtils.isNotBlank(rodaServerName)) {
      URI rodaServerNameUri = URI.create(rodaServerName);
      uri.setScheme(rodaServerNameUri.getScheme());
      uri.setHost(rodaServerNameUri.getHost());
      uri.setPort(rodaServerNameUri.getPort());
      uri.setFragment(hash);
    }

    uri.setPath(path);

    // adding all other parameters
    parameterMap.forEach((param, values) -> {
      for (String value : values) {
        uri.addParameter(param, value);
      }
    });

    LOGGER.info("URL: {} ; Request URI: {} ; Path: {} ; Hash: {}; Parameters: {}", url, requestURI, path, hash,
      parameterMap);

    return uri;
  }
}
