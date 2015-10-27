package org.roda.common;

public class CharacterizationUtils {
  /*
  public static SolrInputDocument getObjectCharacteristicsFields(String aipID, String representationID, String fileID, Binary binary, Path configBasePath)
    throws IndexServiceException {
    SolrInputDocument doc;
    InputStream inputStream;
    try {
      inputStream = binary.getContent().createInputStream();

      Reader descMetadataReader = new InputStreamReader(inputStream);

      InputStream transformerStream = RodaUtils.getResourceInputStream(configBasePath,
        "crosswalks/ingest/other/characterization.xslt", "Ingesting");

      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
      descMetadataReader.close();

      XMLLoader loader = new XMLLoader();
      LOGGER.trace("Transformed desc. metadata:\n" + transformerResult);
      CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

      boolean parsing = true;
      doc = null;
      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT) {
          String currTag = parser.getLocalName();
          if ("doc".equals(currTag)) {
            doc = loader.readDoc(parser);
          }
        }

      }
      transformationResult.close();

    } catch (IOException | TransformerException | XMLStreamException | FactoryConfigurationError e) {
      throw new IndexServiceException("Could not process descriptive metadata binary " + binary.getStoragePath()
        + " using xslt characterization.xslt", IndexServiceException.INTERNAL_SERVER_ERROR, e);
    }
    String id = SolrUtils.getId(aipID, representationID, fileID);
    doc.addField("id", id);
    return doc;
  }*/
}
