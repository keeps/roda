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

  // RUNNING and next state is FAILURE; assert if plugin is mandatory, if
  // mandatory return FAILURE, if not return PARTIAL_SUCCESS;
  // RUNNING and next state is SUCCESS return SUCCESS;
  // RUNNING and next state is SKIPPED return SKIPPED;

  // SUCCESS and next state is FAILURE; assert if plugin is mandatory, if
  // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
  // SUCCESS and next state is SKIPPED return SUCCESS;
  // SUCCESS and next state is SUCCESS return SUCCESS;

  // PARTIAL_SUCCESS and next state is FAILURE; assert if plugin is mandatory, if
  // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
  // PARTIAL_SUCCESS and mext state is SKIPPED return PARTIAL_SUCCESS;
  // PARTIAL_SUCCESS and next state is SUCCESS return PARTIAL_SUCCESS;

  // SKIPPED and next state is FAILURE; assert if plugin is mandatory, if
  // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
  // SKIPPED and next state is SKIPPED return SKIPPED;
  // SKIPPED and next state is SUCCESS return SUCCESS;

  // FAILURE is an end tag, if reached always returns failure
  public static PluginState calculatePluginState(PluginState previousState, PluginState newState, boolean mandatory) {
    if (PluginState.RUNNING.equals(previousState)) {
      return calculatePluginStateOutcomeWhenStateIsRunning(newState, mandatory);
    }

    switch (previousState) {
      case SUCCESS:
        return calculatePluginStateOutcomeWhenStateIsSuccess(newState, mandatory);
      case PARTIAL_SUCCESS:
        return calculatePluginStateOutcomeWhenStateIsPartialSuccess(newState, mandatory);
      case SKIPPED:
        return calculatePluginStateOutcomeWhenStateIsSkipped(newState, mandatory);
      default:
      case FAILURE:
        return PluginState.FAILURE;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenStateIsRunning(PluginState newState, boolean mandatory) {
    // Running and next state is FAILURE; assert if plugin is mandatory, if
    // mandatory return FAILURE, if not return PARTIAL_SUCCESS;
    // Running and next state is SUCCESS return SUCCESS;
    // Running and next state is SKIPPED return SKIPPED;
    if (PluginState.FAILURE.equals(newState)) {
      if (mandatory) {
        return PluginState.FAILURE;
      } else {
        return PluginState.PARTIAL_SUCCESS;
      }
    } else {
      return newState;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenStateIsSuccess(PluginState newState, boolean mandatory) {
    // SUCCESS and next state is FAILURE; assert if plugin is mandatory, if
    // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
    // SUCCESS and next state is SKIPPED return SUCCESS;
    // SUCCESS and next state is SUCCESS return SUCCESS;
    if (PluginState.FAILURE.equals(newState)) {
      if (mandatory) {
        return PluginState.FAILURE;
      } else {
        return PluginState.PARTIAL_SUCCESS;
      }
    } else {
      return PluginState.SUCCESS;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenStateIsPartialSuccess(PluginState newState,
    boolean mandatory) {
    // PARTIAL_SUCCESS and next state is FAILURE; assert if plugin is mandatory, if
    // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
    // PARTIAL_SUCCESS and mext state is SKIPPED return PARTIAL_SUCCESS;
    // PARTIAL_SUCCESS and next state is SUCCESS return PARTIAL_SUCCESS;
    if (PluginState.FAILURE.equals(newState)) {
      if (mandatory) {
        return PluginState.FAILURE;
      } else {
        return PluginState.PARTIAL_SUCCESS;
      }
    } else {
      return PluginState.PARTIAL_SUCCESS;
    }
  }

  private static PluginState calculatePluginStateOutcomeWhenStateIsSkipped(PluginState newState, boolean mandatory) {
    // SKIPPED and next state is FAILURE; assert if plugin is mandatory, if
    // mandatory return FAILURE, if not return PARTIAl_SUCCESS;
    // SKIPPED and next state is SKIPPED return SKIPPED;
    // SKIPPED and next state is SUCCESS return SUCCESS;
    if (PluginState.FAILURE.equals(newState)) {
      if (mandatory) {
        return PluginState.FAILURE;
      } else {
        return PluginState.PARTIAL_SUCCESS;
      }
    } else {
      return newState;
    }
  }
}
