/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.entity.transaction;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 *         <p>
 *         All operation types will cause transactions to attempt to acquire
 *         locks for the affected resources except for {@link READ} and
 *         {@link OPTIMISTIC_CREATE_IF_NOT_EXISTS}
 *         </p>
 */
public enum OperationType {
  CREATE, UPDATE, DELETE, READ, CREATE_OR_UPDATE, OPTIMISTIC_CREATE_IF_NOT_EXISTS;
}
