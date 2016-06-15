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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class IDTypePicker extends ListBox {

  /**
   * Identifier type
   * 
   */
  public static enum IDType {
    SIMPLE_ID, FULL_ID
  }

  private static String ATTRIBUTE_ALTRENDER_SIMPLE_ID = "id";
  private static String ATTRIBUTE_ALTRENDER_FULL_ID = "full_id";

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  /**
   * Create a new id picker
   */
  public IDTypePicker() {
    this.setVisibleItemCount(1);
    init();
    this.addStyleName("idpicker");
  }

  protected void init() {
    addItem(messages.simpleID());
    addItem(messages.fullID());
    setSelectedIndex(0);
  }

  public void setSelected(int id) {
    setSelectedIndex(id);
  }

  /**
   * Get selected id type
   * 
   * @return
   */
  public IDType getSelectedIDType() {
    int id = getSelectedIndex();
    return (id == 0) ? IDType.SIMPLE_ID : IDType.FULL_ID;
  }

  public static String getIDTypeLabel(String type) {
    String label = null;
    if (type.equals(ATTRIBUTE_ALTRENDER_SIMPLE_ID)) {
      label = messages.simpleID();
    } else if (type.equals(ATTRIBUTE_ALTRENDER_FULL_ID)) {
      label = messages.fullID();
    }
    return label;
  }
}
