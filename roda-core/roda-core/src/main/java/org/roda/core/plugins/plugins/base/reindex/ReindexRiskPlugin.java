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

import org.roda.core.data.v2.risks.Risk;
import org.roda.core.plugins.Plugin;

public class ReindexRiskPlugin extends ReindexRodaEntityPlugin<Risk> {

  @Override
  public String getName() {
    return "Rebuild risk index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Risk> cloneMe() {
    return new ReindexRiskPlugin();
  }

  @Override
  public List<Class<Risk>> getObjectClasses() {
    return Arrays.asList(Risk.class);
  }

}
