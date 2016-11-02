package org.roda.wui.client.common.lists.utils;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class TooltipTextCell extends TextCell {

  interface Templates extends SafeHtmlTemplates {

    @Template("<span title=\"{0}\">")
    SafeHtml startToolTip(String toolTipText);

    @Template("</span>")
    SafeHtml endToolTip();

  }

  private static final Templates TEMPLATES = GWT.create(Templates.class);

  @Override
  public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {

    sb.append(TEMPLATES.startToolTip(value.asString()));
    super.render(context, value, sb);
    sb.append(TEMPLATES.endToolTip());

  }

}
