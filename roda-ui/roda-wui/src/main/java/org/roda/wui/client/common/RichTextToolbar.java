/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.wui.client.common.dialogs.Dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * A sample toolbar for use with {@link RichTextArea}. It provides a simple UI
 * for all rich text formatting, dynamically displayed only for the available
 * functionality.
 */
public class RichTextToolbar extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  /**
   * We use an inner EventHandler class to avoid exposing event methods on the
   * RichTextToolbar itself.
   */
  private class EventHandler implements ClickHandler, ChangeHandler, KeyUpHandler {
    @Override
    public void onChange(ChangeEvent event) {
      // do nothing
    }

    @Override
    public void onClick(ClickEvent event) {
      Widget sender = (Widget) event.getSource();

      if (sender == bold) {
        formatter.toggleBold();
      } else if (sender == italic) {
        formatter.toggleItalic();
      } else if (sender == underline) {
        formatter.toggleUnderline();
      } else if (sender == subscript) {
        formatter.toggleSubscript();
      } else if (sender == superscript) {
        formatter.toggleSuperscript();
      } else if (sender == strikethrough) {
        formatter.toggleStrikethrough();
      } else if (sender == indent) {
        formatter.rightIndent();
      } else if (sender == outdent) {
        formatter.leftIndent();
      } else if (sender == justifyLeft) {
        formatter.setJustification(RichTextArea.Justification.LEFT);
      } else if (sender == justifyCenter) {
        formatter.setJustification(RichTextArea.Justification.CENTER);
      } else if (sender == justifyRight) {
        formatter.setJustification(RichTextArea.Justification.RIGHT);
      } else if (sender == insertImage) {
        Dialogs.showPromptDialog(messages.insertImageUrl(), null, null, "http://", RegExp.compile(".*"),
          messages.cancelButton(), messages.confirmButton(), true, false, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String url) {
              if (url != null) {
                formatter.insertImage(url);
              }
            }
          });
      } else if (sender == createLink) {
        Dialogs.showPromptDialog(messages.insertLinkUrl(), null, null, "http://", RegExp.compile(".*"),
          messages.cancelButton(), messages.confirmButton(), true, false, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String url) {
              if (url != null) {
                formatter.createLink(url);
              }
            }
          });
      } else if (sender == removeLink) {
        formatter.removeLink();
      } else if (sender == ol) {
        formatter.insertOrderedList();
      } else if (sender == ul) {
        formatter.insertUnorderedList();
      } else if (sender == removeFormat) {
        formatter.removeFormat();
      } else if (sender == code) {
        Dialogs.showPromptDialog(messages.editHTMLContent(), null, richText.getHTML(), null, RegExp.compile(".*"),
          messages.cancelButton(), messages.confirmButton(), false, true, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String codeHtml) {
              if (codeHtml != null) {
                richText.setHTML(codeHtml);
              }
            }
          });
      } else if (sender == richText) {
        updateStatus();
      }
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
      Widget sender = (Widget) event.getSource();
      if (sender == richText) {
        updateStatus();
      }
    }
  }

  private EventHandler handler = new EventHandler();

  private RichTextArea richText;
  private RichTextArea.Formatter formatter;

  private VerticalPanel outer = new VerticalPanel();
  private HorizontalPanel topPanel = new HorizontalPanel();
  private HorizontalPanel bottomPanel = new HorizontalPanel();
  private ToggleButton bold;
  private ToggleButton italic;
  private ToggleButton underline;
  private ToggleButton subscript;
  private ToggleButton superscript;
  private ToggleButton strikethrough;
  private PushButton indent;
  private PushButton outdent;
  private PushButton justifyLeft;
  private PushButton justifyCenter;
  private PushButton justifyRight;
  private PushButton ol;
  private PushButton ul;
  private PushButton insertImage;
  private PushButton createLink;
  private PushButton removeLink;
  private PushButton removeFormat;
  private PushButton code;

  /**
   * Creates a new toolbar that drives the given rich text area.
   * 
   * @param richText
   *          the rich text area to be controlled
   */
  public RichTextToolbar(RichTextArea richText) {
    this.richText = richText;
    this.formatter = richText.getFormatter();

    outer.add(topPanel);
    outer.add(bottomPanel);
    topPanel.setWidth("100%");
    bottomPanel.setWidth("100%");

    initWidget(outer);
    setStyleName("gwt-RichTextToolbar");
    richText.addStyleName("hasRichTextToolbar");

    if (formatter != null) {
      topPanel.add(bold = createToggleButton("fa-bold"));
      topPanel.add(italic = createToggleButton("fa-italic"));
      topPanel.add(underline = createToggleButton("fa-underline"));
      topPanel.add(subscript = createToggleButton("fa-subscript"));
      topPanel.add(superscript = createToggleButton("fa-superscript"));
      topPanel.add(justifyLeft = createPushButton("fa-align-left"));
      topPanel.add(justifyCenter = createPushButton("fa-align-center"));
      topPanel.add(justifyRight = createPushButton("fa-align-right"));
      topPanel.add(strikethrough = createToggleButton("fa-strikethrough"));
      topPanel.add(indent = createPushButton("fa-indent"));
      topPanel.add(outdent = createPushButton("fa-outdent"));
      topPanel.add(ol = createPushButton("fa-list-ol"));
      topPanel.add(ul = createPushButton("fa-list-ul"));
      topPanel.add(insertImage = createPushButton("fa-picture-o"));
      topPanel.add(createLink = createPushButton("fa-link"));
      topPanel.add(removeLink = createPushButton("fa-unlink"));
      topPanel.add(removeFormat = createPushButton("fa-eraser"));
      topPanel.add(code = createPushButton("fa-code"));
      richText.addKeyUpHandler(handler);
      richText.addClickHandler(handler);
    }
  }

  private PushButton createPushButton(String icon) {
    PushButton pb = new PushButton();
    pb.addStyleName("fa " + icon);
    pb.addClickHandler(handler);
    return pb;
  }

  private ToggleButton createToggleButton(String icon) {
    ToggleButton tb = new ToggleButton();
    tb.addStyleName("fa " + icon);
    tb.addClickHandler(handler);
    return tb;
  }

  /**
   * Updates the status of all the stateful buttons.
   */
  private void updateStatus() {
    if (formatter != null) {
      bold.setDown(formatter.isBold());
      italic.setDown(formatter.isItalic());
      underline.setDown(formatter.isUnderlined());
      subscript.setDown(formatter.isSubscript());
      superscript.setDown(formatter.isSuperscript());
      strikethrough.setDown(formatter.isStrikethrough());
    }
  }
}
