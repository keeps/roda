package org.roda.core.common.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class ResourceResolver implements LSResourceResolver {

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    InputStream resourceAsStream = null;
    try {
      if (StringUtils.isNotBlank(systemId) && systemId.startsWith("http:")) {
        URL url = new URL(systemId);
        resourceAsStream = url.openStream();
      }
    } catch (IOException e) {
      // try to fallback to file
    }
    if (resourceAsStream == null) {
      resourceAsStream = RodaCoreFactory
        .getConfigurationFileAsStream(RodaConstants.CORE_SCHEMAS_FOLDER + "/" + systemId);
    }
    return new Input(publicId, systemId, resourceAsStream);
  }

}
