/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.index.IndexService;
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

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    index.delete(IndexedRepresentation.class,
      new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, ids)));
    index.delete(IndexedFile.class, new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_AIP_ID, ids)));
    index.delete(IndexedPreservationEvent.class,
      new Filter(new OneOfManyFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, ids)));
  }

}
