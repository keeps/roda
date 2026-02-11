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
import org.roda.core.data.v2.jobs.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for List of Report objects.
 */
@Converter
public class ReportListConverter implements AttributeConverter<List<Report>, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportListConverter.class);

  @Override
  public String convertToDatabaseColumn(List<Report> attribute) {
    if (attribute == null) {
      return null;
    }
    return JsonUtils.getJsonFromObject(attribute);
  }

  @Override
  public List<Report> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new ArrayList<>();
    }
    try {
      return JsonUtils.getListFromJson(dbData, Report.class);
    } catch (GenericException e) {
      LOGGER.error("Error converting JSON to List<Report>: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }
}
