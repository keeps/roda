/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.images;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public interface EditorsIconBundle  extends ClientBundle {


  @Source("textfield.png")
  public ImageResource editorText();

  @Source("date.png")
  public ImageResource editorDate();

  @Source("time.png")
  public ImageResource editorChronList();

  @Source("flag_red.png")
  public ImageResource editorLanguagesList();

  @Source("table.png")
  public ImageResource editorArrangementTable();

  @Source("application_edit.png")
  public ImageResource editorOther();

}
