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
package org.roda.wui.management.user.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 *
 */
public interface UserManagementImageBundle extends ImageBundle {

  public AbstractImagePrototype user();

  @Resource("user_red.png")
  public AbstractImagePrototype inactiveUser();

  public AbstractImagePrototype group();

}
