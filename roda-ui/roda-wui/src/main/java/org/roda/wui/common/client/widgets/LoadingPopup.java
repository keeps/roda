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

import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class LoadingPopup extends PopupPanel {

  private static final int UPDATE_TIME_MILISEC = 1000;
  private static final int MAX_TIME_MILISEC = 30000;
  private final HTML loading;
  private Widget widgetCenter;
  private boolean show;
  private Timer updateTimer;
  private Timer maxTimeout;

  /**
   * Show loading popup
   * 
   * @param widgetCenter
   *          widget in which the popup will center
   */
  public LoadingPopup(Widget widgetCenter) {
    loading = new HTML(HtmlSnippetUtils.LOADING);
    this.setWidget(loading);
    this.widgetCenter = widgetCenter;
    this.show = false;

    this.updateTimer = new Timer() {
      @Override
      public void run() {
        update();
      }
    };

    this.maxTimeout = new Timer() {
      @Override
      public void run() {
        hide();
      }
    };

    loading.addStyleName("loadingImage");
  }

  @Override
  public void show() {
    centerAndShow();
    updateTimer.cancel();
    updateTimer.scheduleRepeating(UPDATE_TIME_MILISEC);
    maxTimeout.cancel();
    maxTimeout.schedule(MAX_TIME_MILISEC);
  }

  @Override
  public void hide() {
    updateTimer.cancel();
    maxTimeout.cancel();
    show = false;
    super.hide();
  }

  protected void centerAndShow() {
    show = true;
    if (widgetCenter != null && widgetCenter.isAttached() && widgetCenter.isVisible()
      && widgetCenter.getOffsetWidth() > 0) {
      center(this.getOffsetWidth(), this.getOffsetHeight());
      super.show();
    }

  }

  protected void center(int offsetWidth, int offsetHeight) {
    int left = Math
      .round(widgetCenter.getAbsoluteLeft() + (float) widgetCenter.getOffsetWidth() / 2 - (float) offsetWidth / 2);

    int top = Math
      .round(widgetCenter.getAbsoluteTop() + (float) widgetCenter.getOffsetHeight() / 2 - (float) offsetHeight / 2);

    this.setPopupPosition(left, top);
  }

  /**
   * Center the loading popup on a widget
   * 
   * @param w
   */
  public void centerOn(Widget w) {
    widgetCenter = w;

  }

  /**
   * Update loading popup position
   */
  public void update() {
    if (show) {
      centerAndShow();
    }
  }

}
