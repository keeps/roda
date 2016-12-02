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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexDIPPlugin extends ReindexRodaEntityPlugin<DIP> {

  @Override
  public String getName() {
    return "Rebuild DIP index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<DIP> cloneMe() {
    return new ReindexDIPPlugin();
  }

  @Override
  public List<Class<DIP>> getObjectClasses() {
    return Arrays.asList(DIP.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    index.delete(DIPFile.class, new Filter(new OneOfManyFilterParameter(RodaConstants.DIPFILE_DIP_ID, ids)));
  }

}
