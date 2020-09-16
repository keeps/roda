/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ReportUtils {
  public static PluginState calculatePluginState(PluginState previousState, PluginState newState, boolean mandatory) {
    if (PluginState.RUNNING.equals(previousState)) {
      return newState;
    }

    if (mandatory) {
      if (PluginState.SUCCESS.equals(previousState)) {
        return calculatePluginStateOutcomeWhenMandatoryAndSuccess(previousState, newState);
      } else if (PluginState.PARTIAL_SUCCESS.equals(previousState)) {
        return calculatePluginStateOutcomeWhenMandatoryAndPartialSuccess(previousState, newState);
      } else {
        return calculatePluginStateOutcomeWhenFailure();
      }
    } else {
      if (PluginState.SUCCESS.equals(previousState)) {
        return calculatePluginStateOutcomeWhenOptionalAndSuccess(previousState, newState);
      } else if (PluginState.PARTIAL_SUCCESS.equals(previousState)) {
        return calculatePluginStateOutcomeWhenOptionalAndPartialSuccess();
      } else {
        return calculatePluginStateOutcomeWhenFailure();
      }
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenOptionalAndPartialSuccess() {
    return PluginState.PARTIAL_SUCCESS;
  }

  private static PluginState calculatePluginStateOutcomeWhenOptionalAndSuccess(PluginState previousState,
    PluginState newState) {
    switch (newState) {
      case SKIPPED:
      case SUCCESS:
        return PluginState.SUCCESS;
      case PARTIAL_SUCCESS:
      case FAILURE:
        return PluginState.PARTIAL_SUCCESS;
      default:
        return previousState;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenMandatoryAndSuccess(PluginState previousState,
    PluginState newState) {
    switch (newState) {
      case SUCCESS:
      case SKIPPED:
        return PluginState.SUCCESS;
      case PARTIAL_SUCCESS:
        return PluginState.PARTIAL_SUCCESS;
      case FAILURE:
        return PluginState.FAILURE;
      default:
        return previousState;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenMandatoryAndPartialSuccess(PluginState previousState,
    PluginState newState) {
    switch (newState) {
      case FAILURE:
        return PluginState.FAILURE;
      case SUCCESS:
      case SKIPPED:
      case PARTIAL_SUCCESS:
        return PluginState.PARTIAL_SUCCESS;
      default:
        return previousState;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenFailure() {
    return PluginState.FAILURE;
  }
}
