/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import org.roda.core.data.common.RodaConstants;
import org.testng.annotations.Test;

/**
 * Runs {@link EARKSIPPluginsTest} with RODA transactions on: index updates are deferred until
 * each transaction commits, and structural lookups use the AIP catalog.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class TransactionalEARKSIPPluginsTest extends EARKSIPPluginsTest {
  @Override
  protected boolean useTransactions() {
    return true;
  }
}
