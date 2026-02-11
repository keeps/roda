/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jpa;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;

/**
 * Abstract base class for JPA attribute converters that serialize/deserialize
 * objects to/from JSON strings.
 *
 * @param <T>
 *          The type of the attribute to convert
 */
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonAttributeConverter.class);

  /**
   * Returns the Class type for deserialization.
   *
   * @return the class type of the attribute
   */
  protected abstract Class<T> getAttributeClass();

  @Override
  public String convertToDatabaseColumn(T attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return null;
    }
    try {
      return JsonUtils.getObjectFromJson(dbData, getAttributeClass());
    } catch (GenericException e) {
      LOGGER.error("Error converting JSON to {}: {}", getAttributeClass().getSimpleName(), e.getMessage(), e);
      return null;
    }
  }
}
