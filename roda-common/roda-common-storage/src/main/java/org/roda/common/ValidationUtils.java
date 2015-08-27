package org.roda.common;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.roda.index.utils.SolrUtils;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StoragePath;
import org.xml.sax.SAXException;

public class ValidationUtils {
	private static final Logger LOGGER = Logger.getLogger(ValidationUtils.class);

	private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Validates all descriptive metadata files contained in the AIP
	 */
	public static boolean isAIPDescriptiveMetadataValid(ModelService model, String aipId, boolean failIfNoSchema)
			throws ModelServiceException {
		boolean valid = true;
		ClosableIterable<DescriptiveMetadata> descriptiveMetadataBinaries = model
				.listDescriptiveMetadataBinaries(aipId);
		try {
			for (DescriptiveMetadata descriptiveMetadata : descriptiveMetadataBinaries) {
				if (!isDescriptiveMetadataValid(model, descriptiveMetadata, failIfNoSchema)) {
					valid = false;
					break;
				}
			}
		} finally {
			try {
				descriptiveMetadataBinaries.close();
			} catch (IOException e) {
				LOGGER.error("Error while while freeing up resources", e);
			}
		}
		return valid;
	}

	/**
	 * Validates descriptive medatada (e.g. against its schema, but other
	 * strategies may be used)
	 * 
	 * @param failIfNoSchema
	 */
	public static boolean isDescriptiveMetadataValid(ModelService model, DescriptiveMetadata metadata,
			boolean failIfNoSchema) throws ModelServiceException {
		boolean valid;
		try {
			StoragePath storagePath = metadata.getStoragePath();
			Binary binary = model.getStorage().getBinary(storagePath);
			InputStream inputStream = binary.getContent().createInputStream();
			String filename = binary.getStoragePath().getName();
			// FIXME this should be loaded from config folder (to be dynamic)
			ClassLoader classLoader = SolrUtils.class.getClassLoader();
			InputStream schemaStream = classLoader.getResourceAsStream("XSD/" + filename + ".xsd");
			if (schemaStream != null) {
				Source xmlFile = new StreamSource(inputStream);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
				Validator validator = schema.newValidator();
				try {
					validator.validate(xmlFile);
					valid = true;
				} catch (SAXException e) {
					LOGGER.debug("Error validating descriptive metadata " + metadata.getStoragePath().asString(), e);
					valid = false;
				}
			} else {
				if (failIfNoSchema) {
					throw new ModelServiceException("Unable to validate " + filename,
							ModelServiceException.INTERNAL_SERVER_ERROR);
				} else {
					valid = true;
				}
			}
		} catch (StorageServiceException | SAXException | IOException e) {
			throw new ModelServiceException(
					"Error validating descriptive metadata " + metadata.getStoragePath().asString(),
					ModelServiceException.INTERNAL_SERVER_ERROR, e);
		}
		return valid;
	}
}
