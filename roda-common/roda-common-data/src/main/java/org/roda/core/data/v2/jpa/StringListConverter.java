/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jpa;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for List&lt;String&gt; objects.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringListConverter.class);

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new ArrayList<>();
    }
    try {
      return JsonUtils.getListFromJson(dbData, String.class);
    } catch (GenericException e) {
      LOGGER.error("Error converting JSON to List<String>: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }
}
