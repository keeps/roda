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

  public enum TagStyle {
    DEFAULT, SUCCESS, FAILURE, WARNING_LIGHT, DANGER_LIGHT, NEUTRAL, MONO, ICON_CALENDAR, ICON_LOCK, ICON_CLOCK,
    BORDER_BLACK, BORDER_DANGER;

    private static String toStyleName(TagStyle tagStyle) {
      switch (tagStyle) {
        case SUCCESS:
          return "tagGreen";
        case FAILURE:
          return "tagRed";
        case WARNING_LIGHT:
          return "tagLightYellow";
        case DANGER_LIGHT:
          return "tagLightRed";
        case MONO:
          return "tagMono";
        case NEUTRAL:
          return "tagGrey";
        case ICON_CALENDAR:
          return "tagIconCalendar";
        case ICON_LOCK:
          return "tagIconLock";
        case ICON_CLOCK:
          return "tagIconClock";
        case BORDER_BLACK:
          return "tagBorderBlack";
        case BORDER_DANGER:
          return "tagBorderRed";
        default:
          return "";
      }
    }
  }

  @UiField
  FocusPanel tagPanel;
  @UiField
  Label label;

  public Tag() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public static Tag fromText(String text) {
    Tag tag = new Tag();
    tag.label.setText(text);
    tag.addStyleName(TagStyle.toStyleName(TagStyle.DEFAULT));
    return tag;
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

  interface MyUiBinder extends UiBinder<Widget, Tag> {
  }
}
