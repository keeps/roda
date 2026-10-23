/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.reindex;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

public class ReindexAIPPlugin extends ReindexRodaEntityPlugin<AIP> {

  @Override
  public String getName() {
    return "plugin.reindexAipPlugin.name";
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
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    // nothing to clear: the reindex overwrites the documents in place and deletes only the leftovers (#3783)
  }

}
