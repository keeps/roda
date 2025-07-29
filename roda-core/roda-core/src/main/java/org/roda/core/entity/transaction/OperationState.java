/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.entity.transaction;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public enum OperationState {
    PENDING, RUNNING, FAILURE, SUCCESS, ROLLED_BACK, ROLLING_BACK, ROLL_BACK_FAILURE
}
