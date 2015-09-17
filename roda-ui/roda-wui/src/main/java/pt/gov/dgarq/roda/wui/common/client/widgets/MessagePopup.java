/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

/**
 * @author Luis Faria
 * 
 */
public class MessagePopup extends PopupPanel {
  private static final int SLOTS_NUMBER = 7;

  private static final MessagePopup[] slots = new MessagePopup[SLOTS_NUMBER];

  private static int currentSlot = 0;

  private static int getNextSlot(MessagePopup next) {
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

  private Image icon;

  private static final CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final int slotNumber;

  private final MessagePopupType type;

  private final AccessibleFocusPanel focus;

  private final HorizontalPanel layout;

  private final VerticalPanel messageLayout;

  private final Label titleLabel;

  private final Label errorMessage;

  private final Timer hideTimer;

  /**
   * Create a new message popup
   * 
   * @param type
   * @param title
   * @param message
   */
  public MessagePopup(MessagePopupType type, String title, String message) {
    super(false);
    this.type = type;
    slotNumber = getNextSlot(this);
    layout = new HorizontalPanel();
    focus = new AccessibleFocusPanel(layout);
    messageLayout = new VerticalPanel();
    titleLabel = new Label(title);
    errorMessage = new Label(message);

    messageLayout.add(titleLabel);
    messageLayout.add(errorMessage);
    if (type.equals(MessagePopupType.ERROR_MESSAGE)) {
      icon = commonImageBundle.bigRedCross().createImage();
      layout.add(icon);
      icon.addStyleName("error-popup-icon");
    } else if (type.equals(MessagePopupType.INFO)) {
      // TODO add green check icon
    }
    layout.add(messageLayout);

    setWidget(focus);

    focus.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        hide();
      }

    });

    hideTimer = new Timer() {
      public void run() {
        hide();
      }
    };

    layout.setCellWidth(messageLayout, "100%");
    layout.addStyleName("wui-error-popup");
    messageLayout.addStyleName("error-popup-message");
    titleLabel.addStyleName("error-popup-message-title");
    errorMessage.addStyleName("error-popup-message-details");
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
            slotOffset += slots[i].getOffsetHeight() + 5;
          }
        }
        MessagePopup.this.setPopupPosition(Window.getClientWidth() - offsetWidth - 5,
          Window.getScrollTop() + 5 + slotOffset);
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
    MessagePopup errorPopup = new MessagePopup(MessagePopupType.ERROR_MESSAGE, constants.alertErrorTitle(), message);
    errorPopup.start();
  }

  /**
   * Show an error message
   * 
   * @param title
   * @param message
   */
  public static void showError(String title, String message) {
    MessagePopup errorPopup = new MessagePopup(MessagePopupType.ERROR_MESSAGE, title, message);
    errorPopup.start();
  }

  /**
   * Show a error message
   * 
   * @param message
   */
  public static void showInfo(String title, String message) {
    MessagePopup errorPopup = new MessagePopup(MessagePopupType.INFO, title, message);
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
}
