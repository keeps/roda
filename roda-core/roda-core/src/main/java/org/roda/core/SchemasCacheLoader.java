package org.roda.core;

import java.io.InputStream;
import java.util.Optional;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.validation.ResourceResolver;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;

import com.google.common.cache.CacheLoader;

public class SchemasCacheLoader extends CacheLoader<Pair<String, String>, Optional<Schema>> {
  @Override
  public Optional<Schema> load(Pair<String, String> pair) throws Exception {
    String metadataType = pair.getFirst();
    String metadataTypeLowerCase = metadataType.toLowerCase();
    String metadataVersion = pair.getSecond();
    String metadataVersionLowerCase = "";
    if (metadataVersion != null) {
      metadataVersionLowerCase = metadataVersion.toLowerCase();
    }

    String schemaFileName;
    if (StringUtils.isNotEmpty(metadataVersion)) {
      schemaFileName = metadataTypeLowerCase + RodaConstants.METADATA_VERSION_SEPARATOR + metadataVersionLowerCase
        + ".xsd";
    } else {
      schemaFileName = metadataTypeLowerCase + ".xsd";
    }

    String schemaPath = RodaConstants.CORE_SCHEMAS_FOLDER + "/" + schemaFileName;

    InputStream schemaStream = RodaCoreFactory.getConfigurationFileAsStream(schemaPath);

    Schema xmlSchema = null;
    try {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(RodaConstants.W3C_XML_SCHEMA_NS_URI);
      schemaFactory.setResourceResolver(new ResourceResolver());
      xmlSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
    } finally {
      RodaUtils.closeQuietly(schemaStream);
    }

    return Optional.ofNullable(xmlSchema);
  }
}
