package org.roda.wui.client.common.lists.utils;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import config.i18n.client.ClientMessages;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActionMenuCell<T> extends AbstractCell<T> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final Delegate<T> delegate;

  public ActionMenuCell(Delegate<T> delegate) {
    super("click");
    this.delegate = delegate;
  }

  @Override
  public void render(Context context, T value, SafeHtmlBuilder sb) {
    // Render a button that looks like a dropdown trigger
    sb.appendHtmlConstant("<div class='groupedActionableMenu'>");
    sb.appendHtmlConstant("<div>");
    sb.appendHtmlConstant(
      "<button type='button' style='padding:0px 5px !important' class='btn-edit groupedActionableDropdownButton'>");
    sb.appendHtmlConstant(messages.manage());
    sb.appendHtmlConstant("</button>");
    sb.appendHtmlConstant("</div>");
    sb.appendHtmlConstant("</div>");
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
    ValueUpdater<T> valueUpdater) {
    if ("click".equals(event.getType())) {
      // Get position of the clicked element
      int left = parent.getAbsoluteLeft();
      int top = parent.getAbsoluteTop() + parent.getOffsetHeight();

      if (delegate != null) {
        delegate.onShowMenu(value, left, top);
      }
    }
  }

  public interface Delegate<T> {
    void onShowMenu(T value, int left, int top);
  }
}
