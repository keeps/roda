package org.roda.common;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.roda.index.utils.SolrUtils;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.StoragePath;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.SimplePreservationMetadata;

public class HTMLUtils {
	// TODO improve metadata to HTML stylesheets
	// TODO support localization (en/pt)
	public static String descriptiveMetadataToHtml(Binary binary, ModelService model, Locale locale)
			throws ModelServiceException {
		try {
			if (locale == null) {
				locale = new Locale("pt", "PT");
			}
			InputStream inputStream = binary.getContent().createInputStream();
			Reader descMetadataReader = new InputStreamReader(inputStream);
			String filename = binary.getStoragePath().getName();
			// TODO select transformers using file name extension
			InputStream transformerStream = getStylesheetInputStream("htmlXSLT", locale, filename);
			// TODO support the use of scripts for non-xml transformers
			Reader xsltReader = new InputStreamReader(transformerStream);
			CharArrayWriter transformerResult = new CharArrayWriter();
			Map<String, String> stylesheetOpt = new HashMap<String, String>();
			stylesheetOpt.put("title", binary.getStoragePath().getName());
			RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
			descMetadataReader.close();
			return transformerResult.toString();
		} catch (TransformerException | IOException e) {
			throw new ModelServiceException(e.getMessage(), ModelServiceException.INTERNAL_SERVER_ERROR);
		}
	}

	private static InputStream getStylesheetInputStream(String xsltFolder, Locale locale, String filename) {
		ClassLoader classLoader = SolrUtils.class.getClassLoader();
		InputStream transformerStream = classLoader.getResourceAsStream(
				xsltFolder + "/" + locale.getLanguage() + "/" + locale.getCountry() + "/" + filename + ".xslt");
		if (transformerStream == null) {
			transformerStream = classLoader
					.getResourceAsStream(xsltFolder + "/" + locale.getLanguage() + "/" + filename + ".xslt");
			if (transformerStream == null) {
				// FIXME not quite sure about this! perhaps it should fail and
				// an exception should be thrown
				transformerStream = classLoader
						.getResourceAsStream(xsltFolder + "/" + locale.getLanguage() + "/plain.xslt");
			}
		}
		return transformerStream;
	}

	// TODO: improve html.premis.xml.xslt
	public static Element preservationObjectFromStorageToHtml(SimplePreservationMetadata object, ModelService model,
			Locale locale) throws ModelServiceException {
		try {
			if (locale == null) {
				locale = new Locale("pt", "PT");
			}
			StoragePath storagePath = ModelUtils.getPreservationFilePath(object.getAipId(),
					object.getRepresentationId(), object.getFileId());
			Binary binary = model.getStorage().getBinary(storagePath);
			InputStream inputStream = binary.getContent().createInputStream();
			Reader descMetadataReader = new InputStreamReader(inputStream);
			String filename = binary.getStoragePath().getName();
			// TODO select transformers using file name extension
			ClassLoader classLoader = SolrUtils.class.getClassLoader();
			InputStream transformerStream = classLoader
					.getResourceAsStream("htmlXSLT/" + locale.getLanguage() + "/premis.xslt");
			// TODO support the use of scripts for non-xml transformers
			Reader xsltReader = new InputStreamReader(transformerStream);
			CharArrayWriter transformerResult = new CharArrayWriter();
			Map<String, String> stylesheetOpt = new HashMap<>();
			stylesheetOpt.put("prefix", RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX);
			RodaUtils.applyStylesheet(xsltReader, descMetadataReader, stylesheetOpt, transformerResult);
			descMetadataReader.close();
			CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new StAXSource(parser), new StreamResult(stringWriter));
			Document doc = Jsoup.parseBodyFragment(stringWriter.toString());
			transformationResult.close();
			return doc.getElementsByTag("body").get(0).child(0);
		} catch (Exception e) {
			throw new ModelServiceException(e.getMessage(), 100);
		}

	}

	/*
	 * public static <T> Element solrDocumentToHtml(SolrDocument doc, Class<T>
	 * inputClass) throws ModelServiceException{
	 * if(inputClass.equals(Representation.class)){ return
	 * representationToHtml(SolrUtils.solrDocumentToRepresentation(doc)); }else
	 * if(inputClass.equals(RepresentationPreservationObject.class)){ return
	 * representationPreservationObjectToHtml((RepresentationPreservationObject)
	 * SolrUtils.solrDocumentToSimpleRepresentationPreservationMetadata(doc));
	 * }else if(inputClass.equals(EventPreservationObject.class)){ return
	 * eventPreservationObjectToHtml((EventPreservationObject)
	 * SolrUtils.solrDocumentToSimpleEventPreservationMetadata(doc)); }else
	 * if(inputClass.equals(RepresentationFilePreservationObject.class)){ return
	 * representationFilePreservationObjectToHtml((
	 * RepresentationFilePreservationObject)
	 * SolrUtils.solrDocumentToSimpleRepresentationFileMetadata(doc)); }else{
	 * throw new ModelServiceException(
	 * "Error while converting Solr Document to HTML: not supported", 0); }
	 * 
	 * }
	 * 
	 * public static Element
	 * descriptiveMetadataFromSolrToHtml(DescriptiveMetadata metadata,
	 * ModelService model) throws StorageActionException, IndexActionException{
	 * Element wrapper = new Element(Tag.valueOf("span"), "").attr("type",
	 * "descriptiveMetadata"); StoragePath storagePath =
	 * metadata.getStoragePath(); Binary binary =
	 * model.getStorage().getBinary(storagePath); SolrInputDocument sd =
	 * SolrUtils.getDescriptiveMetataFields(binary); for (SolrInputField field :
	 * sd) { wrapper = appendElementGeneric(wrapper, field.getName(),
	 * field.getName(), field.getValue()); } return wrapper; }
	 * 
	 * public static Element representationFilePreservationObjectToHtml(
	 * RepresentationFilePreservationObject rfpo) { Element wrapper = new
	 * Element(Tag.valueOf("span"), "").attr("type",
	 * "representationFilePreservationObject"); wrapper = appendElement(wrapper,
	 * "aipID", "Identificador do AIP", rfpo.getAipId()); wrapper =
	 * appendElement(wrapper, "compositionLevel", "Nível de composição",
	 * rfpo.getCompositionLevel()); wrapper = appendElement(wrapper,
	 * "contentLocationType", "Localização do conteúdo - Tipo",
	 * rfpo.getContentLocationType()); wrapper = appendElement(wrapper,
	 * "contentLocationValue", "Localização do conteúdo - Valor",
	 * rfpo.getContentLocationValue()); wrapper = appendElement(wrapper,
	 * "dateCreated", "Data de criação", rfpo.getCreatedDate()); wrapper =
	 * appendElement(wrapper, "creatingApplicationName", "Nome da aplicação",
	 * rfpo.getCreatingApplicationName()); wrapper = appendElement(wrapper,
	 * "creatingApplicationVersion", "Versão da aplicação",
	 * rfpo.getCreatingApplicationVersion()); wrapper = appendElement(wrapper,
	 * "dateCreatedByApplication", "Data de criação pela aplicação",
	 * rfpo.getDateCreatedByApplication()); wrapper = appendElement(wrapper,
	 * "fileID", "Identificador do ficheiro", rfpo.getFileId()); // TODO
	 * FIXITIES wrapper = appendElement(wrapper, "formatDesignationName",
	 * "Designação do formato - Nome", rfpo.getFormatDesignationName()); wrapper
	 * = appendElement(wrapper, "formatDesignationVersion",
	 * "Designação do formato - Versão", rfpo.getFormatDesignationVersion());
	 * wrapper = appendElement(wrapper, "formatRegistryKey",
	 * "Registo de formato - Chave", rfpo.getFormatRegistryKey()); wrapper =
	 * appendElement(wrapper, "formatRegistryName", "Registo de formato - Nome",
	 * rfpo.getFormatRegistryName()); wrapper = appendElement(wrapper,
	 * "formatRegistryRole", "Registo de formato - Role",
	 * rfpo.getFormatRegistryRole()); wrapper = appendElement(wrapper, "hash",
	 * "Hash", rfpo.getHash()); wrapper = appendElement(wrapper, "id",
	 * "Identificador", rfpo.getId()); wrapper = appendElement(wrapper, "label",
	 * "Label", rfpo.getLabel()); wrapper = appendElement(wrapper,
	 * "lastModifiedDate", "Data de alteração", rfpo.getLastModifiedDate());
	 * wrapper = appendElement(wrapper, "mimetype", "Mimetype",
	 * rfpo.getMimetype()); wrapper = appendElement(wrapper, "model", "Modelo",
	 * rfpo.getModel()); wrapper = appendElement(wrapper,
	 * "objectCharacteristicsExtension", "Características do objeto - Extensão",
	 * rfpo.getObjectCharacteristicsExtension()); wrapper =
	 * appendElement(wrapper, "originalName", "Nome original",
	 * rfpo.getOriginalName()); wrapper = appendElement(wrapper,
	 * "preservationLevel", "Nível de preservação",
	 * rfpo.getPreservationLevel()); wrapper = appendElement(wrapper,
	 * "pronomId", "ID Pronom", rfpo.getPronomId()); wrapper =
	 * appendElement(wrapper, "representationId",
	 * "Identificador da representação", rfpo.getRepresentationId()); wrapper =
	 * appendElement(wrapper, "representationObjectId",
	 * "Identificador do objeto da representação",
	 * rfpo.getRepresentationObjectId()); wrapper = appendElement(wrapper,
	 * "size", "Tamanho", rfpo.getSize()); wrapper = appendElement(wrapper,
	 * "state", "Estado", rfpo.getState()); wrapper = appendElement(wrapper,
	 * "type", "Tipo", rfpo.getType()); return wrapper; }
	 * 
	 * public static Element
	 * eventPreservationObjectToHtml(EventPreservationObject epo) { Element
	 * wrapper = new Element(Tag.valueOf("span"), "").attr("type",
	 * "eventPreservationObject"); wrapper = appendElement(wrapper, "agentID",
	 * "Identificador do agente", epo.getAgentID()); wrapper =
	 * appendElement(wrapper, "agentRole", "Role do agente",
	 * epo.getAgentRole()); wrapper = appendElement(wrapper, "aipID",
	 * "Identificador do AIP", epo.getAipId()); wrapper = appendElement(wrapper,
	 * "dateCreated", "Data de criação", epo.getCreatedDate()); wrapper =
	 * appendElement(wrapper, "date", "Data", epo.getDate()); wrapper =
	 * appendElement(wrapper, "datetime", "Data e hora", epo.getDatetime());
	 * wrapper = appendElement(wrapper, "description", "Descrição",
	 * epo.getDescription()); wrapper = appendElement(wrapper, "eventDetail",
	 * "Detalhes do evento", epo.getEventDetail()); wrapper =
	 * appendElement(wrapper, "eventType", "Tipo de evento",
	 * epo.getEventType()); wrapper = appendElement(wrapper, "fileID",
	 * "Identificador do ficheiro", epo.getFileId()); wrapper =
	 * appendElement(wrapper, "id", "Identificador", epo.getId()); wrapper =
	 * appendElement(wrapper, "label", "Label", epo.getLabel()); wrapper =
	 * appendElement(wrapper, "lastModifiedDate", "Data de alteração",
	 * epo.getLastModifiedDate()); wrapper = appendElement(wrapper, "model",
	 * "Modelo", epo.getModel()); wrapper = appendElement(wrapper, "name",
	 * "Nome", epo.getName()); wrapper = appendElement(wrapper, "objectIDs",
	 * "Identificadores de objetos", epo.getObjectIDs()); wrapper =
	 * appendElement(wrapper, "outcome", "Resultado", epo.getOutcome()); wrapper
	 * = appendElement(wrapper, "outcome", "Resultado - Extensão",
	 * epo.getOutcomeDetailExtension()); wrapper = appendElement(wrapper,
	 * "outcome", "Resultado - Notas", epo.getOutcomeDetailNote()); wrapper =
	 * appendElement(wrapper, "outcome", "Resultado - Detalhes",
	 * epo.getOutcomeDetails()); wrapper = appendElement(wrapper, "outcome",
	 * "Resultado - Resultado", epo.getOutcomeResult()); wrapper =
	 * appendElement(wrapper, "representationId",
	 * "Identificador da representação", epo.getRepresentationId()); wrapper =
	 * appendElement(wrapper, "state", "Estado", epo.getState()); wrapper =
	 * appendElement(wrapper, "targetID", "Identificador do alvo",
	 * epo.getTargetID()); wrapper = appendElement(wrapper, "type", "Tipo",
	 * epo.getType()); return wrapper; }
	 * 
	 * public static Element
	 * representationPreservationObjectToHtml(RepresentationPreservationObject
	 * rpo) { Element wrapper = new Element(Tag.valueOf("span"),
	 * "").attr("type", "representationPreservationObject"); wrapper =
	 * appendElement(wrapper, "aipID", "Identificador do AIP", rpo.getAipId());
	 * wrapper = appendElement(wrapper, "dateCreated", "Data de criação",
	 * rpo.getCreatedDate()); wrapper = appendElement(wrapper,
	 * "derivationEventID", "Identificador do evento de derivação",
	 * rpo.getDerivationEventID()); wrapper = appendElement(wrapper,
	 * "derivedFromRepresentationObjectID", "Derivado da representação",
	 * rpo.getDerivedFromRepresentationObjectID()); wrapper =
	 * appendElement(wrapper, "fileID", "Identificador do ficheiro",
	 * rpo.getFileId()); wrapper = appendElement(wrapper, "id", "Identificador",
	 * rpo.getId()); wrapper = appendElement(wrapper, "label", "Label",
	 * rpo.getLabel()); wrapper = appendElement(wrapper, "lastModifiedDate",
	 * "Data de alteração", rpo.getLastModifiedDate()); wrapper =
	 * appendElement(wrapper, "model", "Modelo", rpo.getModel()); wrapper =
	 * appendElement(wrapper, "preservationEventIDs", "Modelo",
	 * rpo.getPreservationEventIDs()); wrapper = appendElement(wrapper,
	 * "preservationLevel", "Nível de preservação", rpo.getPreservationLevel());
	 * wrapper = appendElement(wrapper, "representationId",
	 * "Identificador da representação", rpo.getRepresentationId()); wrapper =
	 * appendElement(wrapper, "representationObjectId",
	 * "Identificador do objeto da representação",
	 * rpo.getRepresentationObjectID()); wrapper = appendElement(wrapper,
	 * "state", "Estado", rpo.getState()); wrapper = appendElement(wrapper,
	 * "type", "Tipo", rpo.getType()); return wrapper; }
	 * 
	 * 
	 * public static Element representationToHtml(Representation representation)
	 * { Element wrapper = new Element(Tag.valueOf("span"), "").attr("type",
	 * "representation"); wrapper = appendElement(wrapper, "aipID",
	 * "Identificador do AIP", representation.getAipId()); wrapper =
	 * appendElement(wrapper, "id", "Identificador", representation.getId());
	 * wrapper = appendElement(wrapper, "type", "Tipo",
	 * representation.getType()); wrapper = appendElement(wrapper,
	 * "dateCreated", "Data de criação", representation.getDateCreated());
	 * wrapper = appendElement(wrapper, "dateModified", "Data de alteração",
	 * representation.getDateModified()); wrapper = appendElement(wrapper,
	 * "fileIds", "Identificadores de ficheiros", representation.getFileIds());
	 * wrapper = appendElement(wrapper, "statuses", "Estados",
	 * representation.getStatuses()); return wrapper; }
	 * 
	 * private static Element appendElementGeneric(Element wrapper, String
	 * field, String label, Object value) { if (value instanceof List) {
	 * List<String> valueList = (List) value; return appendElement(wrapper,
	 * field, label, valueList); } else if (value instanceof Boolean) { Boolean
	 * valueBoolean = (Boolean) value; return appendElement(wrapper, field,
	 * label, valueBoolean); } else if (value instanceof Date) { Date valueDate
	 * = (Date) value; return appendElement(wrapper, field, label, valueDate); }
	 * else if (value instanceof String) { String valueString = (String) value;
	 * return appendElement(wrapper, field, label, valueString); } else if
	 * (value instanceof Double) { Double valueDouble = (Double)value; return
	 * appendElement(wrapper, field, label, valueDouble); }else{ String
	 * valueString = (String) value; return appendElement(wrapper, field, label,
	 * valueString);
	 * 
	 * } }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, String value) { if(value!=null){ Element el =
	 * wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label);
	 * el.appendElement("span").attr("type", "value").text(value); } return
	 * wrapper; } private static Element appendElement(Element wrapper, String
	 * field, String label, Double value) { if(value!=null){ Element el =
	 * wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label);
	 * el.appendElement("span").attr("type",
	 * "value").text(Double.toString(value)); } return wrapper; } private static
	 * Element appendElement(Element wrapper, String field, String label,
	 * Boolean value) { if(value!=null){ Element el =
	 * wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label);
	 * el.appendElement("span").attr("type", "value").text(""+value); } return
	 * wrapper; }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, long value) { if(value!=-1){ //TODO: any better way???
	 * Element el = wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label);
	 * el.appendElement("span").attr("type",
	 * "value").text(Long.toString(value)); } return wrapper; }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, Date value) { if(value!=null){ Element el =
	 * wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label);
	 * el.appendElement("span").attr("type",
	 * "value").text(dateFormat.format(value)); } return wrapper; }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, List<String> values) { if(values!=null && values.size()>0){
	 * Element el = wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label); Element
	 * valuesWrapper = el.appendElement("span").attr("type", "values"); for
	 * (String string : values) {
	 * valuesWrapper.appendElement("span").attr("type", "value").text(string); }
	 * } return wrapper; }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, String[] values) { if(values!=null && values.length>0){
	 * Element el = wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label); Element
	 * valuesWrapper = el.appendElement("span").attr("type", "values"); for
	 * (String string : values) {
	 * valuesWrapper.appendElement("span").attr("type", "value").text(string); }
	 * } return wrapper; }
	 * 
	 * private static Element appendElement(Element wrapper, String field,
	 * String label, Set<RepresentationState> values) { if(values!=null &&
	 * values.size()>0){ Element el =
	 * wrapper.appendElement("span").attr("field", field);
	 * el.appendElement("span").attr("type", "label").text(label); Element
	 * valuesWrapper = el.appendElement("span").attr("type", "values"); for
	 * (RepresentationState status : values) {
	 * valuesWrapper.appendElement("span").attr("type",
	 * "value").text(status.name()); } } return wrapper; }
	 * 
	 * public static void main(String args[]) { Representation r = new
	 * Representation("ID", "AIPID", true, new Date(), new Date(), null, "TYPE",
	 * Arrays.asList("V1", "V2", "V3"));
	 * System.out.println(representationToHtml(r)); }
	 */
}
