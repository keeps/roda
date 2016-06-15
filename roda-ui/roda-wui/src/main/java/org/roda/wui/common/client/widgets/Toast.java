/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client.widgets;

import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class Toast extends PopupPanel {
  private static final int PADDING = 10;

  private static final int SLOTS_NUMBER = 7;

  private static final Toast[] slots = new Toast[SLOTS_NUMBER];

  private static int currentSlot = 0;

  private static int getNextSlot(Toast next) {
    if (slots[currentSlot] != null) {
      currentSlot = (currentSlot + 1) % SLOTS_NUMBER;
      if (slots[currentSlot] != null) {
        slots[currentSlot].hide();
      }
    }
    slots[currentSlot] = next;
    return currentSlot;
  }

  private static final int HIDE_DELAY_MS = 7000;

  /**
   * The type of message
   */
  public static enum MessagePopupType {
    /**
     * An error message
     */
    ERROR_MESSAGE, INFO
  }

  private static final ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private final int slotNumber;

  private final MessagePopupType type;

  private final AccessibleFocusPanel focus;

  private final FlowPanel layout;

  private final Label titleLabel;

  private final Label messageLabel;

  private final Timer hideTimer;

  /**
   * Create a new message popup
   * 
   * @param type
   * @param title
   * @param message
   */
  public Toast(MessagePopupType type, String title, String message) {
    super(false);
    this.type = type;
    slotNumber = getNextSlot(this);
    layout = new FlowPanel();
    focus = new AccessibleFocusPanel(layout);
    titleLabel = new Label(title);
    messageLabel = new Label(message);

    if (type.equals(MessagePopupType.ERROR_MESSAGE)) {
      layout.addStyleName("toast-error");
    } else if (type.equals(MessagePopupType.INFO)) {
      layout.addStyleName("toast-info");
    }

    layout.add(titleLabel);
    layout.add(messageLabel);

    setWidget(focus);

    focus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });

    hideTimer = new Timer() {
      public void run() {
        hide();
      }
    };

    layout.addStyleName("wui-toast");
    titleLabel.addStyleName("toast-title");
    messageLabel.addStyleName("toast-message");
  }

  public void hide() {
    super.hide();
    slots[slotNumber] = null;
  }

  /**
   * Start showing popup
   */
  public void start() {
    setPopupPositionAndShow(new PositionCallback() {

      public void setPosition(int offsetWidth, int offsetHeight) {
        int slotOffset = 0;
        for (int i = 0; i < slotNumber; i++) {
          if (slots[i] != null) {
            slotOffset += slots[i].getOffsetHeight() + PADDING;
          }
        }
        Toast.this.setPopupPosition(Window.getClientWidth() - offsetWidth - PADDING,
          Window.getScrollTop() + PADDING + slotOffset);
      }

    });

    hideTimer.schedule(HIDE_DELAY_MS);

  }

  /**
   * Show a error message
   * 
   * @param message
   */
  public static void showError(String message) {
    Toast errorPopup = new Toast(MessagePopupType.ERROR_MESSAGE, messages.alertErrorTitle(), message);
    errorPopup.start();
  }

  /**
   * Show an error message
   * 
   * @param title
   * @param message
   */
  public static void showError(String title, String message) {
    Toast errorPopup = new Toast(MessagePopupType.ERROR_MESSAGE, title, message);
    errorPopup.start();
  }

  /**
   * Show a error message
   * 
   * @param message
   */
  public static void showInfo(String title, String message) {
    Toast errorPopup = new Toast(MessagePopupType.INFO, title, message);
    errorPopup.start();
  }

  /**
   * Get the message popup type
   * 
   * @return
   */
  public MessagePopupType getType() {
    return type;
  }

  public static void showError(Throwable caught) {
    showError(caught.getClass().getName(), caught.getMessage());

  }
}
