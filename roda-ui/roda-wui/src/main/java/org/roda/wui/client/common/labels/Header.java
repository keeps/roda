package org.roda.wui.client.common.labels;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class Header extends HTML {
  private int level = 1;
  private String styleName = "";
  private String headerText = "";
  private String icon = "";

  public Header() {
    super();
  }

  public Header(String headerText, String styleName, String icon, int level) {
    super();
    this.headerText = headerText;
    this.styleName = styleName;
    this.icon = icon;
    this.level = level;
    rebuildHTML();
  }

  public void setLevel(int level) {
    this.level = level;
    rebuildHTML();
  }

  public void setHeaderStyleName(String styleName) {
    this.styleName = styleName;
    rebuildHTML();
  }

  public void setHeaderText(String text) {
    this.headerText = text;
    rebuildHTML();
  }

  public void setIcon(String icon) {
    this.icon = icon;
    rebuildHTML();
  }

  private void rebuildHTML() {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.append(SafeHtmlUtils.fromSafeConstant("<h" + level + " class=\"" + styleName + " rodaTitleHeader\">"));
    if (icon != null && !icon.isEmpty()) {
      builder.append(SafeHtmlUtils.fromSafeConstant("<i class=\"" + icon + "\"></i>"));
    }
    builder.append(SafeHtmlUtils.fromSafeConstant(headerText));
    builder.append(SafeHtmlUtils.fromSafeConstant("</h" + level + ">"));
    setHTML(builder.toSafeHtml());
  }
}
