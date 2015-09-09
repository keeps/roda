package pt.gov.dgarq.roda.core.metadata.v2.premis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * This is an utility class for metadata helpers.
 * 
 * @author Rui Castro
 */
public class MetadataHelperUtility {
  private static final Logger logger = Logger.getLogger(MetadataHelperUtility.class);

  /**
   * Saves the current XML object to a byte array.
   * 
   * @param xmlObject
   * 
   * @return a <code>byte[]</code> with the contents of the XML file.
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   */
  public static byte[] saveToByteArray(XmlObject xmlObject) throws MetadataException {
    return saveToByteArray(xmlObject, true);
  }

  /**
   * Saves the current XML object to a byte array.
   * 
   * @param xmlObject
   * @param writeXMLDeclaration
   * 
   * @return a <code>byte[]</code> with the contents of the XML file.
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   */
  public static byte[] saveToByteArray(XmlObject xmlObject, boolean writeXMLDeclaration) throws MetadataException {

    // Save the xml object to a byte array
    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

    saveToOutputStream(xmlObject, byteArrayStream, writeXMLDeclaration);

    return byteArrayStream.toByteArray();
  }

  /**
   * Saves the current XML object to a {@link File}.
   * 
   * @param xmlObject
   *          the XML object to save.
   * @param file
   *          the {@link File}.
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   * 
   * @throws FileNotFoundException
   *           if the specified {@link File} couldn't be opened.
   * @throws IOException
   *           if {@link FileOutputStream} associated with the {@link File}
   *           couldn't be closed.
   */
  public static void saveToFile(XmlObject xmlObject, File file) throws MetadataException, FileNotFoundException,
    IOException {

    FileOutputStream fileOutputStream = new FileOutputStream(file);
    saveToOutputStream(xmlObject, fileOutputStream, true);
    fileOutputStream.close();
  }

  /**
   * Saves the current XML object to an {@link OutputStream}.
   * 
   * @param xmlObject
   *          the XML object to save.
   * @param outputStream
   *          the {@link OutputStream}.
   * @param writeXMLDeclaration
   * 
   * @throws MetadataException
   *           if the XML object is not valid or if something goes wrong with
   *           the serialisation.
   */
  public static void saveToOutputStream(XmlObject xmlObject, OutputStream outputStream, boolean writeXMLDeclaration)
    throws MetadataException {

    logger.trace("Serializing XML Object " + xmlObject.toString());

    // Create an XmlOptions instance and set the error listener.
    XmlOptions validateOptions = new XmlOptions();
    List<XmlError> errorList = new ArrayList<XmlError>();
    validateOptions.setErrorListener(errorList);

    // Validate the XML.
    boolean isValid = xmlObject.validate(validateOptions);
    if (isValid) {

      try {

        XmlOptions xmlSaveOptions = new XmlOptions().setUseDefaultNamespace().setSavePrettyPrint()
          .setSaveAggressiveNamespaces();

        if (!writeXMLDeclaration) {
          xmlSaveOptions = xmlSaveOptions.setSaveNoXmlDecl();
        }

        xmlObject.save(outputStream, xmlSaveOptions);

      } catch (IOException e) {
        logger.debug("Error serializing XML object - " + e.getMessage(), e);
        throw new PremisMetadataException("Error serializing XML object - " + e.getMessage(), e);
      }

    } else {

      // If the XML isn't valid, loop through the listener's contents,
      // printing contained messages.
      for (XmlError xmlError : errorList) {
        logger.error("XmlError: " + xmlError);
      }

      throw new MetadataException("XML document is not valid: " + errorList);
    }
  }

}
