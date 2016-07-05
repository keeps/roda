package org.roda.core.common;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;

class RodaURIResolver implements URIResolver {

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      InputStream st = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CROSSWALKS_DISSEMINATION_OTHER_PATH + "/" + href);
      return new StreamSource(st);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}