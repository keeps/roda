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
import org.roda.core.data.v2.index.select.SelectedItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for SelectedItems objects.
 */
@Converter
public class SelectedItemsConverter implements AttributeConverter<SelectedItems<?>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SelectedItemsConverter.class);

  @Override
  public String convertToDatabaseColumn(SelectedItems<?> attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  @SuppressWarnings("unchecked")
  public SelectedItems<?> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return null;
    }
    try {
      return JsonUtils.getObjectFromJson(dbData, SelectedItems.class);
    } catch (GenericException e) {
      LOGGER.error("Error converting JSON to SelectedItems: {}", e.getMessage(), e);
      return null;
    }
  }
}
