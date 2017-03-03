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

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexJobPlugin extends ReindexRodaEntityPlugin<Job> {

  @Override
  public String getName() {
    return "Rebuild job index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Job> cloneMe() {
    return new ReindexJobPlugin();
  }

  @Override
  public List<Class<Job>> getObjectClasses() {
    return Arrays.asList(Job.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
