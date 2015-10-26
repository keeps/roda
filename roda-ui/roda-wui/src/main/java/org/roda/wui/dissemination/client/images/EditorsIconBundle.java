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
package org.roda.wui.dissemination.client.images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Luis Faria
 * 
 */
public interface EditorsIconBundle extends ImageBundle {

  @Resource("textfield.png")
  public AbstractImagePrototype editorText();

  @Resource("date.png")
  public AbstractImagePrototype editorDate();

  @Resource("time.png")
  public AbstractImagePrototype editorChronList();

  @Resource("flag_red.png")
  public AbstractImagePrototype editorLanguagesList();

  @Resource("table.png")
  public AbstractImagePrototype editorArrangementTable();

  @Resource("application_edit.png")
  public AbstractImagePrototype editorOther();

}
