/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLUtils {
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
    try {
      SAXSource xmlSource = getSafeSAXSource(new InputSource(new StringReader(xml)));
      JAXBContext jaxbContext = JAXBContext.newInstance(objectClass);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      return unmarshaller.unmarshal(xmlSource, objectClass).getValue();
    } catch (JAXBException | ParserConfigurationException | SAXException e) {
      throw new GenericException(e);
    }
  }

  public static SAXSource getSafeSAXSource(InputSource inputSource) throws SAXException, ParserConfigurationException {
    return new SAXSource(getSafeXMLReader(), inputSource);
  }

  public static XMLReader getSafeXMLReader() throws SAXException, ParserConfigurationException {
    // Disable XXE
    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
    xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    return xmlReader;
  }
}
