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
  private int sourceObjectsProcessedWithFailure = 0;
  private int outcomeObjectsWithManualIntervention = 0;

  public JobStats() {

  }

  public int getCompletionPercentage() {
    return completionPercentage;
  }

  public void setCompletionPercentage(int completionPercentage) {
    this.completionPercentage = completionPercentage;
  }

  public int getSourceObjectsCount() {
    return sourceObjectsCount;
  }

  public void setSourceObjectsCount(int sourceObjectsCount) {
    this.sourceObjectsCount = sourceObjectsCount;
  }

  public int getSourceObjectsBeingProcessed() {
    return sourceObjectsBeingProcessed;
  }

  public void setSourceObjectsBeingProcessed(int sourceObjectsBeingProcessed) {
    this.sourceObjectsBeingProcessed = sourceObjectsBeingProcessed;
  }

  public int getSourceObjectsWaitingToBeProcessed() {
    return sourceObjectsWaitingToBeProcessed;
  }

  public void setSourceObjectsWaitingToBeProcessed(int sourceObjectsWaitingToBeProcessed) {
    this.sourceObjectsWaitingToBeProcessed = sourceObjectsWaitingToBeProcessed;
  }

  public int getSourceObjectsProcessedWithSuccess() {
    return sourceObjectsProcessedWithSuccess;
  }

  public void setSourceObjectsProcessedWithSuccess(int sourceObjectsProcessedWithSuccess) {
    this.sourceObjectsProcessedWithSuccess = sourceObjectsProcessedWithSuccess;
  }

  public int getSourceObjectsProcessedWithFailure() {
    return sourceObjectsProcessedWithFailure;
  }

  public void setSourceObjectsProcessedWithFailure(int sourceObjectsProcessedWithFailure) {
    this.sourceObjectsProcessedWithFailure = sourceObjectsProcessedWithFailure;
  }

  public int getOutcomeObjectsWithManualIntervention() {
    return outcomeObjectsWithManualIntervention;
  }

  public void setOutcomeObjectsWithManualIntervention(int outcomeObjectsWithManualIntervention) {
    this.outcomeObjectsWithManualIntervention = outcomeObjectsWithManualIntervention;
  }

  public void incrementObjectsProcessedWithFailure() {
    this.sourceObjectsProcessedWithFailure += 1;
    this.sourceObjectsBeingProcessed -= 1;
  }

  public void incrementObjectsProcessedWithSuccess() {
    this.sourceObjectsProcessedWithSuccess += 1;
    this.sourceObjectsBeingProcessed -= 1;
  }

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
      + ", sourceObjectsProcessedWithFailure=" + sourceObjectsProcessedWithFailure
      + ", outcomeObjectsWithManualIntervention=" + outcomeObjectsWithManualIntervention + "]";
  }

}
