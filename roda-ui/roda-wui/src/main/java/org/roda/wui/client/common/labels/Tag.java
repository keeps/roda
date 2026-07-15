/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.labels;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class Tag extends Composite {
  private static final Tag.MyUiBinder uiBinder = GWT.create(Tag.MyUiBinder.class);
  @UiField
  FocusPanel tagPanel;
  @UiField
  Label label;

  public Tag() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public static Tag fromText(String text, TagStyle style) {
    Tag tag;
    tag = new Tag();
    tag.label.setText(text);
    tag.addStyleName(TagStyle.toStyleName(style));
    return tag;
  }

  public static Tag fromText(String text, List<TagStyle> style) {
    Tag tag;
    tag = new Tag();
    tag.label.setText(text);
    for (TagStyle s : style) {
      tag.addStyleName(TagStyle.toStyleName(s));
    }
    return tag;
  }

  public void addClickHandler(ClickHandler clickHandler) {
    tagPanel.addClickHandler(clickHandler);
    tagPanel.addStyleName("clickable");
  }

  public enum TagStyle {
    INFO, SUCCESS, WARNING, DANGER, NEUTRAL, INGESTED, PRESERVATION, DISSEMINATION, ORIGINAL, CUSTOM;

    private static String toStyleName(TagStyle tagStyle) {
      switch (tagStyle) {
        case INFO:
          return "tag-info";
        case SUCCESS:
          return "tag-success";
        case WARNING:
          return "tag-warning";
        case DANGER:
          return "tag-danger";
        case NEUTRAL:
          return "tag-default";
        case ORIGINAL:
          return "tag-original";
        case PRESERVATION:
          return "tag-preservation";
        case DISSEMINATION:
          return "tag-dissemination";
        case INGESTED:
          return "tag-ingested";
        case CUSTOM:
          return "tag-custom";
        default:
          return "";
      }
    }
  }

  interface MyUiBinder extends UiBinder<Widget, Tag> {
  }
}
