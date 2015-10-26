/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets;

import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ImageLink extends Composite {

  private final AccessibleFocusPanel layout;
  private final HorizontalPanel panel;

  private final Image image;
  private final Label label;

  public ImageLink(Image linkImage, String text) {
    layout = new AccessibleFocusPanel();
    panel = new HorizontalPanel();

    image = linkImage;
    label = new Label(text);

    panel.add(image);
    panel.add(label);

    layout.add(panel);

    initWidget(layout);

    layout.addStyleName("image-link");
    layout.addStyleName("image-link-panel");
    image.addStyleName("image-link-image");
    label.addStyleName("image-link-label");
  }

  public void addClickHandler(ClickHandler handler) {
    layout.addClickHandler(handler);
  }

  public void setText(String text) {
    label.setText(text);
  }

}
