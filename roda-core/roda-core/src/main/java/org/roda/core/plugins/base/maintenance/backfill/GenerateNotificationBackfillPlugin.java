package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateNotificationBackfillPlugin extends GenerateRODAEntityBackfillPlugin<Notification> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) Notification.class;
  }


  @Override
  public Plugin<Notification> cloneMe() {
    return new GenerateNotificationBackfillPlugin();
  }

  @Override
  public List<Class<Notification>> getObjectClasses() {
    return List.of(Notification.class);
  }
}
