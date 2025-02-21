package org.roda.wui.client.common.labels;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class Tag extends HTML {
  public enum TagStyle {
    DEFAULT, SUCCESS, FAILURE, WARNING_LIGHT, DANGER_LIGHT, MONO;

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
        default:
          return "";
      }
    }
  }

  protected static final SafeHtml OPEN_TAG_SPAN = SafeHtmlUtils.fromSafeConstant("<span>");
  protected static final SafeHtml CLOSE_TAG_SPAN = SafeHtmlUtils.fromSafeConstant("</span>");

  public Tag(SafeHtml safeHtml) {
    super(safeHtml);
  }

  public static Tag fromText(String text) {
    SafeHtmlBuilder tagBuilder = new SafeHtmlBuilder();
    tagBuilder.append(OPEN_TAG_SPAN);
    tagBuilder.append(SafeHtmlUtils.fromString(text));
    tagBuilder.append(CLOSE_TAG_SPAN);
    Tag tag = new Tag(tagBuilder.toSafeHtml());
    tag.addStyleName("tag");
    tag.addStyleName(TagStyle.toStyleName(TagStyle.DEFAULT));
    return tag;
  }

  public static Tag fromText(String text, TagStyle style) {
    SafeHtmlBuilder tagBuilder = new SafeHtmlBuilder();
    tagBuilder.append(OPEN_TAG_SPAN);
    tagBuilder.append(SafeHtmlUtils.fromString(text));
    tagBuilder.append(CLOSE_TAG_SPAN);
    Tag tag = new Tag(tagBuilder.toSafeHtml());
    tag.addStyleName("tag");
    tag.addStyleName(TagStyle.toStyleName(style));
    return tag;
  }

  public static Tag fromText(String text, List<TagStyle> style) {
    SafeHtmlBuilder tagBuilder = new SafeHtmlBuilder();
    tagBuilder.append(OPEN_TAG_SPAN);
    tagBuilder.append(SafeHtmlUtils.fromString(text));
    tagBuilder.append(CLOSE_TAG_SPAN);
    Tag tag = new Tag(tagBuilder.toSafeHtml());
    tag.addStyleName("tag");
    for (TagStyle s : style) {
      tag.addStyleName(TagStyle.toStyleName(s));
    }
    return tag;
  }
}
