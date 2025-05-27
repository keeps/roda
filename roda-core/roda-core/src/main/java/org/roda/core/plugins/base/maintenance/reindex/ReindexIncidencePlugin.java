/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.reindex;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

public class ReindexIncidencePlugin extends ReindexRodaEntityPlugin<RiskIncidence> {

  @Override
  public String getName() {
    return "Rebuild incidence index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<RiskIncidence> cloneMe() {
    return new ReindexIncidencePlugin();
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return Arrays.asList(RiskIncidence.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
