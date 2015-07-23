package org.roda.common;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.xml.sax.SAXException;

public class ValidationUtils {

	public static boolean validateAIPDescriptiveMetadata(ModelService model, AIP aip) throws ModelServiceException {
		boolean valid = true;
		Iterable<DescriptiveMetadata> descriptiveMetadataBinaries = model.listDescriptiveMetadataBinaries(aip.getId());
		for (DescriptiveMetadata descriptiveMetadata : descriptiveMetadataBinaries) {
			if (!validateDescriptiveMetadata(model, descriptiveMetadata)) {
				valid = false;
				break;
			}
		}
		return valid;
	}

	public static boolean validateDescriptiveMetadata(ModelService model, DescriptiveMetadata metadata)
			throws ModelServiceException {
		boolean valid = false;
		try {
			StoragePath storagePath = metadata.getStoragePath();
			Binary binary = model.getStorage().getBinary(storagePath);
			InputStream inputStream = binary.getContent().createInputStream();
			String filename = binary.getStoragePath().getName();
			ClassLoader classLoader = SolrUtils.class.getClassLoader();
			InputStream schemaStream = classLoader.getResourceAsStream("XSD/" + filename + ".xsd");
			if (schemaStream == null) {
				throw new ModelServiceException("Unable to validate " + filename,
						ModelServiceException.INTERNAL_SERVER_ERROR);
			}
			Source xmlFile = new StreamSource(inputStream);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
			Validator validator = schema.newValidator();
			try {
				validator.validate(xmlFile);
				valid = true;
			} catch (SAXException e) {
				// error validating... valid stays false
			}
		} catch (StorageActionException e) {
			throw new ModelServiceException("Error validating descriptive metadata",
					ModelServiceException.INTERNAL_SERVER_ERROR);
		} catch (SAXException | IOException e) {
			throw new ModelServiceException("Error validating descriptive metadata",
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}
		return valid;

	}
}
