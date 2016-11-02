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

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;

public class ReindexNotificationPlugin extends ReindexRodaEntityPlugin<Notification> {

  @Override
  public String getName() {
    return "Rebuild notification index";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Notification> cloneMe() {
    return new ReindexNotificationPlugin();
  }

  @Override
  public List<Class<Notification>> getObjectClasses() {
    return Arrays.asList(Notification.class);
  }

  @Override
  public void clearSpecificIndexes(IndexService index, List<String> ids)
    throws GenericException, RequestNotValidException {
    // do nothing
  }

}
