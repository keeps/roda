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
