package org.roda.wui.client.common.lists.utils;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SimpleActionCell<T> extends AbstractCell<T> {

  private final Delegate<T> delegate;
  private final String buttonText;
  private final String cssClass;

  // Constructor allows you to pass custom text and CSS classes for the button
  public SimpleActionCell(String buttonText, String cssClass, Delegate<T> delegate) {
    super("click", "keydown"); // Listen for both click and Enter key
    this.buttonText = buttonText;
    this.cssClass = cssClass;
    this.delegate = delegate;
  }

  @Override
  public void render(Context context, T value, SafeHtmlBuilder sb) {
    // Render the button with your custom classes

    sb.appendHtmlConstant("<button type='button' class='" + cssClass + "'>");
    sb.appendEscaped(buttonText);
    sb.appendHtmlConstant("</button>");
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
    ValueUpdater<T> valueUpdater) {

    super.onBrowserEvent(context, parent, value, event, valueUpdater);

    String eventType = event.getType();
    if ("click".equals(eventType) || ("keydown".equals(eventType) && event.getKeyCode() == 13)) {

      // Stop the event from propagating (useful if the cell is in a selectable
      // DataGrid/CellTable)
      event.preventDefault();
      event.stopPropagation();

      if (delegate != null) {
        delegate.execute(value);
      }
    }
  }

  public interface Delegate<T> {
    void execute(T value);
  }
}
