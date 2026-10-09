/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.db.jobs;

/**
 * How a job selects the objects it processes ({@code jobs.source_selection}).
 *
 * @author RODA Development Team
 */
public enum JobSourceSelection {
  /** No objects. */
  NONE,
  /** Every object of {@code jobs.source_objects_class}. */
  ALL,
  /** The objects listed in {@code job_source_objects}. */
  LIST,
  /** The objects matching {@code jobs.source_filter}. */
  FILTER
}
