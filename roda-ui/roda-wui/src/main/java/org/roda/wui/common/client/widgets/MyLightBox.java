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

import org.gwtwidgets.client.ui.PNGImage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * @author Luis Faria
 * 
 */
public class MyLightBox implements PopupListener {
  private PNGImage png;

  private PopupPanel child;

  private PopupPanel background;

  private WindowResizeListener windowResizeListener;

  /**
   * Create a new lightbox
   * 
   * @param child
   */
  public MyLightBox(PopupPanel child) {
    background = new PopupPanel();

    windowResizeListener = new WindowResizeListener() {
      public void onWindowResized(int width, int height) {
        background.setWidth(Integer.toString(width));
        background.setHeight(Integer.toString(height));
        if (GWT.isScript()) {
          png.setPixelSize(width, height);
        }
        background.setPopupPosition(0, 0);
      }
    };
    Window.addWindowResizeListener(windowResizeListener);

    this.child = child;
    this.child.addPopupListener(this);
  }

  private native void backgroundFixup(Element e)
  /*-{
  	// fixes issue with GWT 1.1.10 by hiding the iframe
  	if (e.__frame) {
  		e.__frame.style.visibility = 'hidden';
  	}
  }-*/;

  public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
    if (GWT.isScript() && png != null) {
      this.hide();
    } else {
      this.hide();
    }
  }

  /**
   * Show lightbox
   */
  public void show() {
    int w = getWidth();
    int h = getHeight();

    background.setWidth(Integer.toString(w));
    background.setHeight(Integer.toString(h));
    // if (GWT.isScript()) {
    // background.setWidget(png = new
    // PNGImage("org.roda.wui.main.Main/images/lightbox.png", w,
    // h));
    // }
    background.setPopupPosition(0, 0);
    hideSelects();

    background.show();
    backgroundFixup(background.getElement());

    /**
     * Using setPopupPositionAndShow so child popup will not jump when centering
     * 
     * @author Luis Faria
     */
    child.setPopupPositionAndShow(new PositionCallback() {

      public void setPosition(int offsetWidth, int offsetHeight) {
        center(offsetWidth, offsetHeight);
      }

    });
  }

  /**
   * Center the inner popup in the window.
   * 
   */
  public void center() {
    int offsetWidth = child.getOffsetWidth();
    int offsetHeight = child.getOffsetHeight();
    center(offsetWidth, offsetHeight);
  }

  // protected void center(int offsetWidth, int offsetHeight) {
  // int left = (getWidth() - offsetWidth) / 2;
  // int top = (getHeight() - offsetHeight) / 2;
  // child.setPopupPosition(left, top);
  // }

  protected void center(int offsetWidth, int offsetHeight) {
    int width = Window.getClientWidth();
    int height = Window.getClientHeight();
    int scrollLeft = Window.getScrollLeft();
    int scrollRight = Window.getScrollTop();

    int left = scrollLeft + (width - offsetWidth) / 2;
    int top = scrollRight + (height - offsetHeight) / 2;

    child.setPopupPosition(left, top);
  }

  /**
   * Hide lightbox
   */
  public void hide() {
    /**
     * @author Luis Faria check if png is null before removing from parent
     */
    if (GWT.isScript()) {
      if (png != null) {
        png.removeFromParent();
      }
      png = null;
    }
    showSelects();
    child.hide();
    background.hide();
    Window.removeWindowResizeListener(windowResizeListener);
  }

  private native void hideSelects() /*-{
                                    var selects = $doc.getElementsByTagName("select");
                                    for (i = 0; i != selects.length; i++) {
                                    selects[i].style.visibility = "hidden";
                                    }
                                    }-*/;

  private native void showSelects() /*-{
                                    var selects = $doc.getElementsByTagName("select");
                                    for (i = 0; i != selects.length; i++) {
                                    selects[i].style.visibility = "visible";
                                    }
                                    }-*/;

  private native int getHeight() /*-{
                                 var yScroll;

                                 if ($wnd.innerHeight && $wnd.scrollMaxY) {
                                 yScroll = $wnd.innerHeight + $wnd.scrollMaxY;
                                 } else if ($doc.body.scrollHeight > $doc.body.offsetHeight) { // all but Explorer Mac
                                 yScroll = $doc.body.scrollHeight;
                                 } else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
                                 yScroll = $doc.body.offsetHeight;
                                 }

                                 var windowHeight;
                                 if (self.innerHeight) { // all except Explorer
                                 windowHeight = self.innerHeight;
                                 } else if ($doc.documentElement && $doc.documentElement.clientHeight) { // Explorer 6 Strict Mode
                                 windowHeight = $doc.documentElement.clientHeight;
                                 } else if ($doc.body) { // other Explorers
                                 windowHeight = $doc.body.clientHeight;
                                 }

                                 // for small pages with total height less then height of the viewport
                                 if (yScroll < windowHeight) {
                                 pageHeight = windowHeight;
                                 } else {
                                 pageHeight = yScroll;
                                 }
                                 return pageHeight;
                                 }-*/;

  private native int getWidth() /*-{
                                var xScroll;

                                if ($wnd.innerHeight && $wnd.scrollMaxY) {
                                xScroll = $doc.body.scrollWidth;
                                } else if ($doc.body.scrollHeight > $doc.body.offsetHeight) { // all but Explorer Mac
                                xScroll = $doc.body.scrollWidth;
                                } else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
                                xScroll = $doc.body.offsetWidth;
                                }

                                var windowHeight;
                                if (self.innerHeight) { // all except Explorer
                                windowWidth = self.innerWidth;
                                } else if ($doc.documentElement && $doc.documentElement.clientHeight) { // Explorer 6 Strict Mode
                                windowWidth = $doc.documentElement.clientWidth;
                                } else if ($doc.body) { // other Explorers
                                windowWidth = $doc.body.clientWidth;
                                }

                                // for small pages with total width less then width of the viewport
                                if (xScroll < windowWidth) {
                                pageWidth = windowWidth;
                                } else {
                                pageWidth = xScroll;
                                }
                                return pageWidth;
                                }-*/;

}
