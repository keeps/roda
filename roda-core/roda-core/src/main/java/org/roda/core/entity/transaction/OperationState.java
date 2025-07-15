package org.roda.core.entity.transaction;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public enum OperationState {
    PENDING, RUNNING, FAILURE, SUCCESS, ROLLED_BACK, ROLLING_BACK, ROLL_BACK_FAILURE
}
