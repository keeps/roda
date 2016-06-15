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
public class RedactionTypePicker extends ListBox {

  /**
   * The type of redaction
   * 
   */
  public static enum RedactionType {
    INPUT, OUTPUT
  }

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  /**
   * Create a new RedactionType picker
   */
  public RedactionTypePicker() {
    this.setVisibleItemCount(1);
    init();
    this.addStyleName("redactionpicker");
  }

  protected void init() {
    addItem(messages.input());
    addItem(messages.output());
    setSelectedIndex(0);
  }

  public void setSelected(int id) {
    setSelectedIndex(id);
  }

  /**
   * Get selected redaction type
   * 
   * @return
   */
  public RedactionType getSelectedIDType() {
    int id = getSelectedIndex();
    return (id == 0) ? RedactionType.INPUT : RedactionType.OUTPUT;
  }

}
