/**
 * 
 */
package org.roda.wui.dissemination.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 * 
 *         Image icons for each level of the Description Object
 */
@Deprecated
public interface ElementIconBundle extends ImageBundle {

  @Resource("fonds.png")
  public AbstractImagePrototype fonds();

  @Resource("subfonds.png")
  public AbstractImagePrototype subfonds();

  @Resource("class.png")
  public AbstractImagePrototype class_();

  @Resource("subclass.png")
  public AbstractImagePrototype subclass();

  @Resource("series.png")
  public AbstractImagePrototype series();

  @Resource("subseries.png")
  public AbstractImagePrototype subseries();

  @Resource("file.png")
  public AbstractImagePrototype file();

  @Resource("item.png")
  public AbstractImagePrototype item();

}
