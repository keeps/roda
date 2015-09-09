/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 * 
 */
public interface EventManagementImageBundle extends ImageBundle {

  public AbstractImagePrototype taskScheduled();

  public AbstractImagePrototype taskPaused();

  public AbstractImagePrototype taskSuspended();

  public AbstractImagePrototype taskInstanceRunning();

  public AbstractImagePrototype taskInstancePaused();

  public AbstractImagePrototype taskInstanceStopped();

}
