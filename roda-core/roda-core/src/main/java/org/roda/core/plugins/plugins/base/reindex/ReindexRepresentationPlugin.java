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
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexRepresentationPlugin extends ReindexRodaEntityPlugin<Representation> {

  @Override
  public String getName() {
    return "Rebuild representation index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Representation> cloneMe() {
    return new ReindexRepresentationPlugin();
  }

  @Override
  public List<Class<Representation>> getObjectClasses() {
    return Arrays.asList(Representation.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    index.delete(IndexedFile.class,
      new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, ids)));
  }

}
