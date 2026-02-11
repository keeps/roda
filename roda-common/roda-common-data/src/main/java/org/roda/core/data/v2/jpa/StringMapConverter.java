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

import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for Map&lt;String, String&gt; objects.
 */
@Converter
public class StringMapConverter implements AttributeConverter<Map<String, String>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringMapConverter.class);

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new HashMap<>();
    }
    return JsonUtils.getMapFromJson(dbData);
  }
}
