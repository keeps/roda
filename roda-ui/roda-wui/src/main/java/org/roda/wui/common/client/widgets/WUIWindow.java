/**
 * 
 */
package org.roda.wui.common.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.SourcesSuccessEvents;
import org.roda.wui.common.client.SuccessListener;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class WUIWindow implements SourcesSuccessEvents {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DialogBox dialog;

  private final DockPanel dock;

  private final Label title;

  private boolean tabmode;

  private final TabPanel tabPanel;

  private Widget widget;

  private final FlowPanel bottom;

  private final List<SuccessListener> successListeners;

  private int maxWidth;

  private int maxHeight;

  /**
   * Create a new WUI Window
   * 
   * @param maxWidth
   * @param maxHeight
   */
  public WUIWindow(int maxWidth, int maxHeight) {
    this.dock = new DockPanel();
    this.dialog = new DialogBox(false, true);
    this.dialog.setGlassEnabled(true);
    this.dialog.setWidget(dock);

    this.title = new Label();
    this.tabmode = true;
    this.tabPanel = new TabPanel();
    this.widget = null;
    this.bottom = new FlowPanel();

    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;

    this.dock.add(title, DockPanel.NORTH);
    this.dock.add(tabPanel, DockPanel.CENTER);
    this.dock.add(bottom, DockPanel.SOUTH);

    this.dock.addStyleName("office-window-dock");
    this.title.addStyleName("office-window-title");
    this.bottom.addStyleName("office-window-bottom");
    this.dock.setCellHorizontalAlignment(bottom, DockPanel.ALIGN_RIGHT);

    this.successListeners = new ArrayList<SuccessListener>();

  }

  /**
   * Create a new WUI window
   * 
   * @param text
   * @param maxWidth
   * @param maxHeigth
   */
  public WUIWindow(String text, int maxWidth, int maxHeigth) {
    this(maxWidth, maxHeigth);
    this.setTitle(text);
  }

  /**
   * Set window title
   * 
   * @param text
   */
  public void setTitle(String text) {
    this.title.setText(text);
  }

  /**
   * Add widget to window bottom
   * 
   * @param widget
   */
  public void addToBottom(Widget widget) {
    this.bottom.add(widget);
  }

  /**
   * Remove widget to window bottom
   * 
   * @param widget
   * @return
   */
  public boolean removeFromBottom(Widget widget) {
    return this.bottom.remove(widget);
  }

  /**
   * Add tab to window
   * 
   * @param widget
   * @param tabText
   */
  public void addTab(Widget widget, String tabText) {
    ScrollPanel scroll = new ScrollPanel(widget);
    scroll.addStyleName("tabScroll");
    tabPanel.add(scroll, tabText);
    if (!tabmode && this.widget != null) {
      dock.remove(this.widget);
      dock.add(tabPanel, DockPanel.CENTER);
      tabmode = true;
    }
  }

  /**
   * Set window widget, removing tab panel
   * 
   * @param widget
   */
  public void setWidget(Widget widget) {
    if (tabmode) {
      dock.remove(tabPanel);
      tabmode = false;
    } else if (this.widget != null) {
      dock.remove(this.widget);
    }

    this.widget = widget;
    dock.add(widget, DockPanel.CENTER);
    widget.addStyleName("office-window-center");
  }

  /**
   * Remove a tab
   * 
   * @param index
   */
  public void removeTab(int index) {
    tabPanel.remove(index);
  }

  /**
   * Select a tab
   * 
   * @param index
   */
  public void selectTab(int index) {
    tabPanel.selectTab(index);
  }

  /**
   * Show window
   */
  public void show() {
    // updatePopupSize();
    // lightbox.show();
    dialog.center();
    dialog.show();
  }

  /**
   * Hide window
   */
  public void hide() {
    // lightbox.hide();
    dialog.hide();
  }

  public void addSuccessListener(SuccessListener listener) {
    this.successListeners.add(listener);
  }

  public void removeSuccessListener(SuccessListener listener) {
    this.successListeners.remove(listener);
  }

  protected void onSuccess() {
    for (SuccessListener listener : successListeners) {
      listener.onSuccess();
    }
  }

  protected void onCancel() {
    for (SuccessListener listener : successListeners) {
      listener.onCancel();
    }
  }

  /**
   * Set window popup position
   * 
   * @param left
   * @param top
   */
  public void setPopupPosition(int left, int top) {
    dialog.setPopupPosition(left, top);
  }

  /**
   * Get window tab panel
   * 
   * @return
   */
  public TabPanel getTabPanel() {
    return tabPanel;
  }

  public void addStyleName(String style) {
    this.dialog.addStyleName(style);
  }

}
