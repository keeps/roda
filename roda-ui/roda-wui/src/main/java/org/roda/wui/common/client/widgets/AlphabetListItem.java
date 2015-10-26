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
package org.roda.wui.common.client.widgets;

import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public interface AlphabetListItem extends SourcesChangeEvents {

  /**
   * Set the item selected
   * 
   * @param selected
   */
  public void setSelected(boolean selected);

  /**
   * Is the item selected
   * 
   * @return true if it is selected, false otherwise
   */
  public boolean isSelected();

  /**
   * Get the item widget
   * 
   * @return
   */
  public Widget getWidget();

  /**
   * Get the keyword used to filter the panel
   * 
   * @return
   */
  public String getKeyword();

  /**
   * Check if the keyword used to filter/search the panel matches a regular
   * expression
   * 
   * @param regex
   *          the regular expression
   * @return true if it matches, false otherwise
   */
  public boolean matches(String regex);
}
