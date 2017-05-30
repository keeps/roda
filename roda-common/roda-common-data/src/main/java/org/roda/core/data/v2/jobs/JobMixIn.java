/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({RodaConstants.JOB_ID, RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE,
  RodaConstants.JOB_STATE, RodaConstants.JOB_STATE_DETAILS, RodaConstants.JOB_STATS, RodaConstants.JOB_IN_FINAL_STATE,
  RodaConstants.JOB_STOPPING})
public class JobMixIn {
}
