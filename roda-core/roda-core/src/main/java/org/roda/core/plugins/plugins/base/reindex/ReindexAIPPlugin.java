/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base.reindex;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;

public class ReindexAIPPlugin extends ReindexRodaEntityPlugin<AIP> {

  @Override
  public String getName() {
    return "Rebuild AIP index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ReindexAIPPlugin();
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
