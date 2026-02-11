/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jpa;

import org.roda.core.data.v2.jobs.JobStats;

import jakarta.persistence.Converter;

/**
 * JPA converter for JobStats objects.
 */
@Converter
public class JobStatsConverter extends JsonAttributeConverter<JobStats> {

  @Override
  protected Class<JobStats> getAttributeClass() {
    return JobStats.class;
  }
}
