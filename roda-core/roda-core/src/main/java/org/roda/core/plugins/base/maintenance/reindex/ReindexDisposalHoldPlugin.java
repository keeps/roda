/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.reindex;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexDisposalHoldPlugin extends ReindexRodaEntityPlugin<DisposalHold> {

  @Override
  public String getName() {
    return "Rebuild disposal hold index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<DisposalHold> cloneMe() {
    return new ReindexDisposalHoldPlugin();
  }

  @Override
  public List<Class<DisposalHold>> getObjectClasses() {
    return Collections.singletonList(DisposalHold.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
