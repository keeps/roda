/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jpa;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for Map&lt;String, Object&gt; objects.
 */
@Converter
public class ObjectMapConverter implements AttributeConverter<Map<String, Object>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapConverter.class);

  @Override
  public String convertToDatabaseColumn(Map<String, Object> attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new HashMap<>();
    }
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      return mapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      LOGGER.error("Error converting JSON to Map<String, Object>: {}", e.getMessage(), e);
      return new HashMap<>();
    }
  }
}
