/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.event.client.images;

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
