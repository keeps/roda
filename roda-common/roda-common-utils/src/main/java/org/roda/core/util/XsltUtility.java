/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Rui Castro
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public final class XsltUtility {

  /**
   * Apply the XSLT transformation (<code>xsltStream</code>) to the xml given (
   * <code>inputXmlStream</code>) and write the output to
   * <code>outputStream</code>
   * 
   * @param xsltStream
   * @param xsltParameters
   * @param inputXmlStream
   * @param outputStream
   * @return the {@link OutputStream} with the transformation output.
   * @throws TransformerException
   */
  public static OutputStream applyTransformation(InputStream xsltStream, Map<String, Object> xsltParameters,
    InputStream inputXmlStream, OutputStream outputStream) throws TransformerException {

    return applyTransformation(new StreamSource(xsltStream), xsltParameters, inputXmlStream, outputStream);
  }

  /**
   * Apply the XSLT transformation (<code>xslt</code>) to the XML given (
   * <code>inputXmlStream</code>) and write the output to
   * <code>outputStream</code>
   * 
   * @param xslt
   * @param xsltParameters
   * @param inputXmlStream
   * @param outputStream
   * 
   * @return the {@link OutputStream} with the transformation output.
   * 
   * @throws TransformerException
   */
  public static OutputStream applyTransformation(String xslt, Map<String, Object> xsltParameters,
    InputStream inputXmlStream, OutputStream outputStream) throws TransformerException {

    return applyTransformation(new StreamSource(new StringReader(xslt)), xsltParameters, inputXmlStream, outputStream);
  }

  /**
   * @param paramName
   * @param value
   * @return a {@link Map} of parameters with a single parameter.
   */
  public static Map<String, Object> createParameters(String paramName, Object value) {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(paramName, value);
    return parameters;
  }

  private static OutputStream applyTransformation(Source xsltSource, Map<String, Object> xsltParameters,
    InputStream inputXmlStream, OutputStream outputStream) throws TransformerException {

    // Create a transform factory instance.
    TransformerFactory tfactory = TransformerFactory.newInstance();

    // Create a transformer for the stylesheet.
    Transformer transformer = tfactory.newTransformer(xsltSource);

    if (xsltParameters != null) {
      for (String paramName : xsltParameters.keySet()) {
        transformer.setParameter(paramName, xsltParameters.get(paramName));
      }
    }

    // Transform the source XML to outputStream.
    transformer.transform(new StreamSource(inputXmlStream), new StreamResult(outputStream));

    return outputStream;
  }

}
