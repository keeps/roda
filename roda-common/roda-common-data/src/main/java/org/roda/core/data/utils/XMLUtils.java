package org.roda.core.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

  private XMLUtils() {
    // do nothing
  }

  public static String getXMLFromObject(Object object) throws GenericException {
    String ret = null;
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = jaxbContext.createMarshaller();
      StringWriter writer = new StringWriter();
      marshaller.marshal(object, writer);
      ret = writer.toString();
    } catch (JAXBException e) {
      throw new GenericException(e);
    }

    return ret;
  }

  public static <T> T getObjectFromXML(InputStream xml, Class<T> objectClass) throws GenericException {
    T ret;
    try {
      String xmlString = IOUtils.toString(xml, RodaConstants.DEFAULT_ENCODING);
      ret = getObjectFromXML(xmlString, objectClass);
    } catch (IOException e) {
      throw new GenericException(e);
    } finally {
      IOUtils.closeQuietly(xml);
    }
    return ret;
  }

  public static <T> T getObjectFromXML(String xml, Class<T> objectClass) throws GenericException {
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance(objectClass);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      StringReader reader = new StringReader(xml);
      return (T) unmarshaller.unmarshal(reader);
    } catch (JAXBException e) {
      throw new GenericException(e);
    }
  }
}
