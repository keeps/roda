/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;

public class JobStats implements Serializable {
  private static final long serialVersionUID = 2048747729436732179L;

  private int completionPercentage = 0;
  private int sourceObjectsCount = 0;
  private int sourceObjectsBeingProcessed = 0;
  private int sourceObjectsWaitingToBeProcessed = 0;
  private int sourceObjectsProcessedWithSuccess = 0;
  private int sourceObjectsProcessedWithPartialSuccess = 0;
  private int sourceObjectsProcessedWithFailure = 0;
  private int sourceObjectsProcessedWithSkipped = 0;
  private int outcomeObjectsWithManualIntervention = 0;

  public JobStats() {
    // do nothing
  }

  public int getCompletionPercentage() {
    return completionPercentage;
  }

  public JobStats setCompletionPercentage(int completionPercentage) {
    this.completionPercentage = completionPercentage;
    return this;
  }

  public int getSourceObjectsCount() {
    return sourceObjectsCount;
  }

  public JobStats setSourceObjectsCount(int sourceObjectsCount) {
    this.sourceObjectsCount = sourceObjectsCount;
    return this;
  }

  public int getSourceObjectsBeingProcessed() {
    return sourceObjectsBeingProcessed;
  }

  public JobStats setSourceObjectsBeingProcessed(int sourceObjectsBeingProcessed) {
    this.sourceObjectsBeingProcessed = sourceObjectsBeingProcessed;
    return this;
  }

  public int getSourceObjectsWaitingToBeProcessed() {
    return sourceObjectsWaitingToBeProcessed;
  }

  public JobStats setSourceObjectsWaitingToBeProcessed(int sourceObjectsWaitingToBeProcessed) {
    this.sourceObjectsWaitingToBeProcessed = sourceObjectsWaitingToBeProcessed;
    return this;
  }

  public int getSourceObjectsProcessedWithSuccess() {
    return sourceObjectsProcessedWithSuccess;
  }

  public JobStats setSourceObjectsProcessedWithSuccess(int sourceObjectsProcessedWithSuccess) {
    this.sourceObjectsProcessedWithSuccess = sourceObjectsProcessedWithSuccess;
    return this;
  }

  public int getSourceObjectsProcessedWithPartialSuccess() {
    return sourceObjectsProcessedWithPartialSuccess;
  }

  public JobStats setSourceObjectsProcessedWithPartialSuccess(int sourceObjectsProcessedWithPartialSuccess) {
    this.sourceObjectsProcessedWithPartialSuccess = sourceObjectsProcessedWithPartialSuccess;
    return this;
  }

  public int getSourceObjectsProcessedWithSkipped() {
    return sourceObjectsProcessedWithSkipped;
  }

  public void setSourceObjectsProcessedWithSkipped(int sourceObjectsProcessedWithSkipped) {
    this.sourceObjectsProcessedWithSkipped = sourceObjectsProcessedWithSkipped;
  }

  public int getSourceObjectsProcessedWithFailure() {
    return sourceObjectsProcessedWithFailure;
  }

  public JobStats setSourceObjectsProcessedWithFailure(int sourceObjectsProcessedWithFailure) {
    this.sourceObjectsProcessedWithFailure = sourceObjectsProcessedWithFailure;
    return this;
  }

  public int getOutcomeObjectsWithManualIntervention() {
    return outcomeObjectsWithManualIntervention;
  }

  public JobStats setOutcomeObjectsWithManualIntervention(int outcomeObjectsWithManualIntervention) {
    this.outcomeObjectsWithManualIntervention = outcomeObjectsWithManualIntervention;
    return this;
  }

  public void incrementObjectsProcessed(PluginState state) {
    if (PluginState.SUCCESS.equals(state) || PluginState.SKIPPED.equals(state)) {
      incrementObjectsProcessedWithSuccess();
    } else {
      incrementObjectsProcessedWithFailure();
    }
  }

  /**
   * Increments by one the number of objects processed with failure & decrements
   * by the same amount the number of objects being processed
   */
  public void incrementObjectsProcessedWithFailure() {
    this.sourceObjectsProcessedWithFailure += 1;
    this.sourceObjectsBeingProcessed -= 1;
  }

  /**
   * Increments the number of objects processed with failure & decrements by the
   * same amount the number of objects being processed
   */
  public void incrementObjectsProcessedWithFailure(int count) {
    this.sourceObjectsProcessedWithFailure += count;
    this.sourceObjectsBeingProcessed -= count;
  }

  /**
   * Increments by one the number of objects processed with success & decrements
   * by the same amount the number of objects being processed
   */
  public void incrementObjectsProcessedWithSuccess() {
    this.sourceObjectsProcessedWithSuccess += 1;
    this.sourceObjectsBeingProcessed -= 1;
  }

  /**
   * Increments the number of objects processed with success & decrements by the
   * same amount the number of objects being processed
   */
  public void incrementObjectsProcessedWithSuccess(int count) {
    this.sourceObjectsProcessedWithSuccess += count;
    this.sourceObjectsBeingProcessed -= count;
  }

  /**
   * Increments the number of objects count
   */
  public void incrementObjectsCount() {
    this.sourceObjectsCount += 1;
  }

  /**
   * Increments the number of objects count
   */
  public void incrementObjectsCount(int count) {
    this.sourceObjectsCount += count;
  }

  /**
   * Increments by one the number of outcome objects with manual intervention
   */
  public void incrementOutcomeObjectsWithManualIntervention() {
    this.outcomeObjectsWithManualIntervention += 1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + completionPercentage;
    result = prime * result + outcomeObjectsWithManualIntervention;
    result = prime * result + sourceObjectsBeingProcessed;
    result = prime * result + sourceObjectsCount;
    result = prime * result + sourceObjectsProcessedWithFailure;
    result = prime * result + sourceObjectsProcessedWithSuccess;
    result = prime * result + sourceObjectsWaitingToBeProcessed;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JobStats)) {
      return false;
    }
    JobStats other = (JobStats) obj;
    if (completionPercentage != other.completionPercentage) {
      return false;
    }
    if (outcomeObjectsWithManualIntervention != other.outcomeObjectsWithManualIntervention) {
      return false;
    }
    if (sourceObjectsBeingProcessed != other.sourceObjectsBeingProcessed) {
      return false;
    }
    if (sourceObjectsCount != other.sourceObjectsCount) {
      return false;
    }
    if (sourceObjectsProcessedWithFailure != other.sourceObjectsProcessedWithFailure) {
      return false;
    }
    if (sourceObjectsProcessedWithSuccess != other.sourceObjectsProcessedWithSuccess) {
      return false;
    }
    if (sourceObjectsWaitingToBeProcessed != other.sourceObjectsWaitingToBeProcessed) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JobStats [completionPercentage=" + completionPercentage + ", sourceObjectsCount=" + sourceObjectsCount
      + ", sourceObjectsBeingProcessed=" + sourceObjectsBeingProcessed + ", sourceObjectsWaitingToBeProcessed="
      + sourceObjectsWaitingToBeProcessed + ", sourceObjectsProcessedWithSuccess=" + sourceObjectsProcessedWithSuccess
      + ", sourceObjectsProcessedWithPartialSuccess=" + sourceObjectsProcessedWithPartialSuccess
      + ", sourceObjectsProcessedWithFailure=" + sourceObjectsProcessedWithFailure
        + ", sourceObjectsProcessedWithSkipped=" + sourceObjectsProcessedWithSkipped
      + ", outcomeObjectsWithManualIntervention=" + outcomeObjectsWithManualIntervention + "]";
  }

}
