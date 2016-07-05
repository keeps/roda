package org.roda.core.common;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RodaEntityResolver implements EntityResolver {
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
          throws SAXException, IOException {
      if (systemId.endsWith(".dtd")) {
          return new InputSource(new StringReader(""));
      } else {
          return null;
      }
  }
}
