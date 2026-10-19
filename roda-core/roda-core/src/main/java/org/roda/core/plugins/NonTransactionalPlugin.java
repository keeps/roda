/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

/**
 * Marks plugins that never change the storage and only rebuild derived views
 * (index, catalog) from the main storage. They run outside a RODA transaction,
 * directly on the main model and index.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface NonTransactionalPlugin {
}
