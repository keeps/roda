/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import org.roda.wui.client.common.utils.JavascriptUtils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SliderPanel extends Composite implements HasChangeHandlers {

  private static final String CSS_CLASS_ACTIVE = "active";
  private static SliderPanel OPEN_SLIDER = null;

  private final SimplePanel wrapper;
  private final ScrollPanel layout;
  private FlowPanel contentLayout;

  private FocusPanel toggleButton = null;

  public SliderPanel() {
    super();
    this.contentLayout = new FlowPanel();
    this.layout = new ScrollPanel();
    this.layout.add(contentLayout);

    this.wrapper = new SimplePanel(this.layout);
    initWidget(wrapper);

    layout.addStyleName("slider-layout");
    this.addStyleName("slider");
  }

  public void setToggleButton(FocusPanel toggleButton) {
    this.toggleButton = toggleButton;

    // bind toggle button events
    toggleButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (isOpen(SliderPanel.this)) {
          closeAll();
        } else {
          open();
        }
      }
    });
  }

  public static void closeAll() {
    if (OPEN_SLIDER != null) {
      // remove CSS classes
      OPEN_SLIDER.setActive(false);

      // hide animation
      JavascriptUtils.toggle(OPEN_SLIDER.getElement());

      // update state
      OPEN_SLIDER = null;
    }
  }

  private static boolean isOpen(SliderPanel slider) {
    return slider == OPEN_SLIDER;
  }

  public static void open(SliderPanel slider) {
    closeAll();

    // update state
    OPEN_SLIDER = slider;

    // Show animation
    JavascriptUtils.toggle(slider.getElement());

    // Add CSS classes
    OPEN_SLIDER.setActive(true);
  }

  @UiChild(tagname = "title")
  public void addTitle(Label title) {
    contentLayout.add(title);
    title.addStyleName("slider-title");
  }

  @UiChild(tagname = "content")
  public void addContent(Widget widget) {
    contentLayout.add(widget);
  }

  public void clear() {
    contentLayout.clear();
  }

  private void setActive(boolean active) {
    if (active) {
      if (toggleButton != null) {
        toggleButton.addStyleName(CSS_CLASS_ACTIVE);
      }
      this.addStyleDependentName(CSS_CLASS_ACTIVE);
    } else {
      if (toggleButton != null) {
        toggleButton.removeStyleName(CSS_CLASS_ACTIVE);
      }
      this.removeStyleDependentName(CSS_CLASS_ACTIVE);
    }

    ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
  }

  public void open() {
    open(this);
  }

  public boolean isOpen() {
    return isOpen(this);
  }

  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addHandler(handler, ChangeEvent.getType());
  }

}
