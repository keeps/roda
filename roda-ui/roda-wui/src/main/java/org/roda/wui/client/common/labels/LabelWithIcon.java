package org.roda.wui.client.common.labels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class LabelWithIcon extends Composite {
  private static LabelWithIcon.MyUiBinder uiBinder = GWT.create(LabelWithIcon.MyUiBinder.class);

  @UiField
  Label text;
  @UiField
  HTML icon;

  boolean reversed = false;

  public LabelWithIcon() {
    super();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void reverseOrder() {
    reversed = !reversed;
    if (reversed) {
      addStyleName("labelWithIconReversed");
    } else {
      removeStyleName("labelWithIconReversed");
    }
  }

  public void setText(String text) {
    this.text.setText(text);
  }

  public void setIcon(String iconClasses) {
    if (iconClasses != null && !iconClasses.isEmpty()) {
      icon.setHTML(SafeHtmlUtils.fromSafeConstant("<i class=\"" + iconClasses + "\"></i>"));
    }
  }

  interface MyUiBinder extends UiBinder<Widget, LabelWithIcon> {
  }
}
