package org.roda.core.entity.converts;

import java.util.UUID;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Converter
public class UUIDStringConverter implements AttributeConverter<String, UUID> {
  @Override
  public UUID convertToDatabaseColumn(String attribute) {
    return (attribute == null) ? null : UUID.fromString(attribute);
  }

  @Override
  public String convertToEntityAttribute(UUID uuid) {
    return (uuid == null) ? null : uuid.toString();
  }
}
