/**
 * 
 */
package org.roda.wui.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import config.i18n.client.CommonConstants;

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

  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  /**
   * Create a new RedactionType picker
   */
  public RedactionTypePicker() {
    this.setVisibleItemCount(1);
    init();
    this.addStyleName("redactionpicker");
  }

  protected void init() {
    addItem(constants.input());
    addItem(constants.output());
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
