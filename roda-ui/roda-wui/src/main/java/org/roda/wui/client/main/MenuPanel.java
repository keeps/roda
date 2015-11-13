/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MenuPanel extends Composite {

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, MenuPanel> {
  }

  interface MenuStyle extends CssResource {
    String menu_lightblue();

    String menu_darkblue();

    String menu_yellow();

    String menu_green();
  }

  public enum MenuColor {
    LIGHT_BLUE, DARK_BLUE, YELLOW, GREEN
  }

  @UiField(provided = true)
  Label title;

  @UiField
  FlowPanel itemsPanel;

  @UiField
  MenuStyle style;

  /**
   * Create a new menu panel
   * 
   * @param title
   *          the menu title
   * @param items
   *          the menu items
   * @param color
   *          the menu hover color
   */
  public MenuPanel(Label title, Label[] items, final MenuColor color) {
    this.title = title;
    initWidget(uiBinder.createAndBindUi(this));

    for (int i = 0; i < items.length; i++) {
      items[i].addStyleName("menu_item");
      itemsPanel.add(items[i]);
      if (i < items.length - 1) {
        HTML separator = new HTML("&nbsp;·&nbsp;");
        separator.addStyleName("menu_separator");
        itemsPanel.add(separator);
      }
    }

    switch (color) {
      case DARK_BLUE:
        getElement().addClassName(style.menu_darkblue());
        break;
      case LIGHT_BLUE:
        getElement().addClassName(style.menu_lightblue());
        break;
      case YELLOW:
        getElement().addClassName(style.menu_yellow());
        break;
      case GREEN:
        getElement().addClassName(style.menu_green());
        break;

      default:
        break;
    }
  }

  /**
   * Set the visibility of a determined menu item
   * 
   * @param index
   *          the item index
   * @param visible
   *          true to turn visible, false otherwise
   */
  public void setItemVisible(int index, boolean visible) {
    // visibility of (index)th item
    itemsPanel.getWidget(2 * index).setVisible(visible);

    // check separators
    int count = itemsPanel.getWidgetCount();
    if (count > 1) {
      boolean lastItemVisible = itemsPanel.getWidget(0).isVisible();
      for (int i = 2; i <= count; i += 2) {
        boolean indexVisible = itemsPanel.getWidget(i).isVisible();

        // separator index-1
        itemsPanel.getWidget(i - 1).setVisible(lastItemVisible && indexVisible);

        lastItemVisible |= indexVisible;
      }
    }

  }

}
