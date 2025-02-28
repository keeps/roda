package org.roda.wui.client.common.cards;

import java.util.List;
import java.util.Map;

import org.roda.wui.client.common.labels.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ThumbnailCard extends Composite {
  public static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ThumbnailCard.MyUiBinder uiBinder = GWT.create(ThumbnailCard.MyUiBinder.class);

  @UiField
  Label title;
  @UiField
  FocusPanel thumbnail;
  @UiField
  FlowPanel tags;
  @UiField
  FlowPanel attributes;

  private boolean collapsed = false;

  public ThumbnailCard(String title, Widget iconThumbnail, List<Tag> tags, Map<String, String> attributes,
    ClickHandler thumbnailClickHandler) {
    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    expand();

    // Title
    this.title.setText(title);
    this.title.addClickHandler(event -> toggleCollapse());

    // Thumbnail
    iconThumbnail.addStyleName("thumbnailCardIconThumbnail");
    this.thumbnail.setWidget(iconThumbnail);
    this.thumbnail.addClickHandler(thumbnailClickHandler);

    // Tags
    for (Tag tag : tags) {
      this.tags.add(tag);
    }

    // Attributes
    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      FlowPanel attributePanel = new FlowPanel();
      attributePanel.add(new HTML(SafeHtmlUtils.fromSafeConstant(attribute.getKey())));
      attributePanel.add(new HTML(SafeHtmlUtils.fromSafeConstant(attribute.getValue())));
      this.attributes.add(attributePanel);
    }
  }

  public void toggleCollapse() {
    if (collapsed) {
      expand();
    } else {
      collapse();
    }
  }

  public void collapse() {
    collapsed = true;
    addStyleName("thumbnailCardExpandable");
    removeStyleName("thumbnailCardCollapsible");
  }

  public void expand() {
    collapsed = false;
    addStyleName("thumbnailCardCollapsible");
    removeStyleName("thumbnailCardExpandable");
  }

  interface MyUiBinder extends UiBinder<Widget, ThumbnailCard> {
  }
}
