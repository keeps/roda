/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.tuple.Triple;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class RodaUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaUtils.class);

  private static final Processor PROCESSOR = new Processor(false);

  private static final LoadingCache<Triple<String, String, String>, XsltExecutable> CACHE = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<Triple<String, String, String>, XsltExecutable>() {

      @Override
      public XsltExecutable load(Triple<String, String, String> key) throws Exception {
        String basePath = key.getLeft();
        String metadataType = key.getMiddle();
        String metadataVersion = key.getRight();
        return createMetadataTransformer(basePath, metadataType, metadataVersion);
      }

    });

  private static final LoadingCache<String, XsltExecutable> EVENT_CACHE = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, XsltExecutable>() {
      @Override
      public XsltExecutable load(String path) throws Exception {
        return createEventTransformer(path);
      }
    });

  /** Private empty constructor */
  private RodaUtils() {
    // do nothing
  }

  /**
   * Closes an input stream quietly (i.e. no exception is thrown) while inspecting
   * its class. The inspection is done to avoid closing streams associates with
   * jar files that may cause later errors like
   *
   * <pre>
   * Caused by: java.io.IOException: Stream closed
   * </pre>
   *
   * So, all streams whose class name does not start with
   * <code>sun.net.www.protocol.jar.JarURLConnection</code> will be closed
   *
   * @since 2016-09-20
   */
  public static void closeQuietly(InputStream inputstream) {
    try {
      close(inputstream);
    } catch (IOException e) {
      // do nothing as we should be quiet
    }
  }

  /**
   * Closes an input stream while inspecting its class. The inspection is done to
   * avoid closing streams associates with jar files that may cause later errors
   * like
   *
   * <pre>
   * Caused by: java.io.IOException: Stream closed
   * </pre>
   *
   * So, all streams whose class name does not start with
   * <code>sun.net.www.protocol.jar.JarURLConnection</code> will be closed
   *
   * @throws IOException
   *
   * @since 2016-09-20
   */
  public static void close(InputStream inputstream) throws IOException {
    if (inputstream != null) {
      String inputstreamClassName = inputstream.getClass().getName();
      if (!inputstreamClassName.startsWith("sun.net.www.protocol.jar.JarURLConnection")) {
        inputstream.close();
      }
    }
  }

  public static Map<String, Object> copyMap(Object object) {
    if (!(object instanceof Map)) {
      return null;
    }
    Map<?, ?> map = (Map<?, ?>) object;
    Map<String, Object> temp = new HashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() instanceof String) {
        temp.put((String) entry.getKey(), entry.getValue());
      } else {
        return null;
      }
    }
    return temp;
  }

  public static List<String> copyList(Object object) {
    if (!(object instanceof List)) {
      return new ArrayList<>();
    }
    List<?> list = (List<?>) object;
    List<String> temp = new ArrayList<>();
    for (Object ob : list) {
      if (ob instanceof String) {
        temp.add((String) ob);
      } else if (ob == null) {
        temp.add(null);
      } else {
        return new ArrayList<>();
      }
    }
    return temp;
  }

  /**
   * INFO 20160711 this method does not cache stylesheet related resources
   */
  public static void applyStylesheet(Reader xsltReader, Reader fileReader, Map<String, String> parameters,
    Writer result) throws IOException, TransformerException {

    TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
    factory.setURIResolver(new RodaURIFileResolver());
    Source xsltSource = new StreamSource(xsltReader);
    Transformer transformer = factory.newTransformer(xsltSource);
    for (Entry<String, String> parameter : parameters.entrySet()) {
      transformer.setParameter(parameter.getKey(), parameter.getValue());
    }
    try {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setEntityResolver(new RodaEntityResolver());
      InputSource source = new InputSource(fileReader);
      Source text = new SAXSource(xmlReader, source);
      transformer.transform(text, new StreamResult(result));
    } catch (SAXException se) {
      LOGGER.error(se.getMessage(), se);
    }
  }

  public static Reader applyMetadataStylesheet(Binary binary, String basePath, String metadataType,
    String metadataVersion, Map<String, String> parameters) throws GenericException {
    try (
      Reader descMetadataReader = new InputStreamReader(new BOMInputStream(binary.getContent().createInputStream()))) {

      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setEntityResolver(new RodaEntityResolver());
      InputSource source = new InputSource(descMetadataReader);
      Source text = new SAXSource(xmlReader, source);

      XsltExecutable xsltExecutable = CACHE.get(Triple.of(basePath, metadataType, metadataVersion));

      XsltTransformer transformer = xsltExecutable.load();
      CharArrayWriter transformerResult = new CharArrayWriter();

      transformer.setSource(text);
      transformer.setDestination(PROCESSOR.newSerializer(transformerResult));

      for (Entry<String, String> parameter : parameters.entrySet()) {
        QName qName = new QName(parameter.getKey());
        XdmValue xdmValue = new XdmAtomicValue(parameter.getValue());
        transformer.setParameter(qName, xdmValue);
      }

      QName qNameMap = new QName("i18n");
      XdmMap xdmMap = XdmMap.makeMap(parameters);
      transformer.setParameter(qNameMap, xdmMap);

      transformer.transform();

      return new CharArrayReader(transformerResult.toCharArray());

    } catch (IOException | SAXException | ExecutionException | SaxonApiException e) {
      throw new GenericException("Could not process descriptive metadata binary " + binary.getStoragePath()
        + " metadata type " + metadataType + " and version " + metadataVersion, e);
    }
  }

  public static Reader applyEventStylesheet(Binary binary, boolean onlyDetails, Map<String, String> translations,
    String path) throws GenericException {
    try (
      Reader descMetadataReader = new InputStreamReader(new BOMInputStream(binary.getContent().createInputStream()))) {

      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      xmlReader.setEntityResolver(new RodaEntityResolver());
      InputSource source = new InputSource(descMetadataReader);
      Source text = new SAXSource(xmlReader, source);

      XsltExecutable xsltExecutable = EVENT_CACHE.get(path);

      XsltTransformer transformer = xsltExecutable.load();
      CharArrayWriter transformerResult = new CharArrayWriter();

      transformer.setSource(text);
      transformer.setDestination(PROCESSOR.newSerializer(transformerResult));

      // send param to filter stylesheet work
      transformer.setParameter(new QName("onlyDetails"), new XdmAtomicValue(Boolean.toString(onlyDetails)));

      for (Entry<String, String> parameter : translations.entrySet()) {
        QName qName = new QName(parameter.getKey());
        XdmValue xdmValue = new XdmAtomicValue(parameter.getValue());
        transformer.setParameter(qName, xdmValue);
      }

      transformer.transform();
      return new CharArrayReader(transformerResult.toCharArray());
    } catch (IOException | SAXException | ExecutionException | SaxonApiException e) {
      LOGGER.error(e.getMessage(), e);
      throw new GenericException("Could not process event binary " + binary.getStoragePath(), e);
    }
  }

  protected static XsltExecutable createMetadataTransformer(String basePath, String metadataType,
    String metadataVersion) throws SaxonApiException, GenericException {
    InputStream transformerStream = null;

    try {
      // get xslt from metadata type and version if defined
      if (metadataType != null) {
        String lowerCaseMetadataType = metadataType.toLowerCase();
        if (metadataVersion != null) {
          String lowerCaseMetadataTypeWithVersion = lowerCaseMetadataType + RodaConstants.METADATA_VERSION_SEPARATOR
            + metadataVersion;
          transformerStream = RodaCoreFactory
            .getConfigurationFileAsStream(basePath + lowerCaseMetadataTypeWithVersion + ".xslt");
        }
        if (transformerStream == null) {
          transformerStream = RodaCoreFactory.getConfigurationFileAsStream(basePath + lowerCaseMetadataType + ".xslt");
        }
      }

      // fallback
      if (transformerStream == null) {
        // TODO change plain to default
        transformerStream = RodaCoreFactory.getConfigurationFileAsStream(basePath + "plain.xslt");
      }

      if (transformerStream == null) {
        throw new GenericException("Could not find stylesheet nor fallback at basePath=" + basePath + ", metadataType="
          + metadataType + ", metadataVersion=" + metadataVersion);
      }

      XsltCompiler compiler = PROCESSOR.newXsltCompiler();
      compiler.setURIResolver(new RodaURIFileResolver());
      // compiler.setSchemaAware(false);
      return compiler.compile(new StreamSource(transformerStream));

    } finally {
      IOUtils.closeQuietly(transformerStream);
    }
  }

  protected static XsltExecutable createEventTransformer(String path) throws SaxonApiException, GenericException {
    try (InputStream transformerStream = RodaCoreFactory.getConfigurationFileAsStream(path)) {
      if (transformerStream == null) {
        throw new GenericException("Could not find stylesheet nor fallback at path=" + path);
      }

      XsltCompiler compiler = PROCESSOR.newXsltCompiler();
      compiler.setURIResolver(new RodaURIFileResolver());
      return compiler.compile(new StreamSource(transformerStream));
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }
}
