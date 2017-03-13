/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets;

import java.util.ArrayList;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/*
 * Copyright 2010 ArcBees Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * @author Christian Goudreau
 */
public class MaskedTextBox extends TextBox {
  private String baseContent = "";
  private String content = "";
  private ArrayList<String> expression = new ArrayList<>();
  private String mask = "";
  private String validContent = "";

  public MaskedTextBox() {
    super();
  }

  /**
   * @param mask
   * 
   *          # = [0-9] 9 = [\s0-9] A = [A-Z] a = [a-z] B = [A-Za-z] C =
   *          [A-Za-z0-9] /, * . - : ( ) { } = +
   */
  @UiConstructor
  public MaskedTextBox(String mask) {
    super();

    sinkEvents(Event.ONPASTE);

    this.mask = mask;
    transformMask();
    assignEvents();
  }

  public String getFormatedText() {
    return super.getText();
  }

  @Override
  public String getText() {
    return this.validContent;
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    if (event.getTypeInt() == Event.ONPASTE) {
      event.preventDefault();

      String pastedText = getPastedText(event);
      for (int i = 0; i < pastedText.length(); ++i) {
        addCharCode(pastedText.charAt(i));
      }
    }
  }

  @Override
  public void setText(String text) {
    if (text != null) {
      validContent = content = text;
    } else {
      validContent = content = "";
    }

    format();
  }

  private void addCharCode(Character ch) {
    String contentToValidate = validContent + ch;

    if (contentToValidate.length() <= expression.size()
      && String.valueOf(ch).matches(expression.get(contentToValidate.length() - 1))) {
      validContent = contentToValidate;
      format();
    }
  }

  private void assignEvents() {
    this.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
          if (validContent.length() >= 1) {
            validContent = validContent.substring(0, validContent.length() - 1);
            format();
          }

          cancelKey();
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_LEFT) {
          cancelKey();
        }
      }
    });

    this.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        Integer nativeCode = event.getNativeEvent().getKeyCode();
        if (nativeCode == KeyCodes.KEY_TAB) {
          return;
        }

        addCharCode(event.getCharCode());
        cancelKey();
      }
    });

    this.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setCaretPosition(getElement(), mask.length());
      }
    });
  }

  private void format() {
    this.content = this.baseContent;

    for (Integer i = 0; i < this.validContent.length(); i++) {
      this.content = this.content.replaceFirst("[_]", this.validContent.substring(i, i + 1));
    }

    super.setText(this.content);
    updateCursorPos();
  }

  private void updateCursorPos() {
    int cursorPos;
    int nextInputPos = super.getText().indexOf('_');

    if (nextInputPos < 0) {
      cursorPos = super.getText().length() - 1;
    } else {
      cursorPos = nextInputPos;
    }

    super.setCursorPos(cursorPos);

  }

  private native String getPastedText(Event event) /*-{
		var clipboard = event.clipboardData | $wnd.clipboardData;

		try {
			return clipboard.getData("Text");
		} catch (e) {
			return "";
		}
  }-*/;

  private native void setCaretPosition(Element elem, Integer pos) /*-{
		if (elem.setSelectionRange) {
			elem.setSelectionRange(pos, pos);
		} else if (elem.createTextRange) {
			var range = elem.createTextRange();
			range.collapse = true;
			range.moveStart('character', pos);
			range.moveEnd('character', pos);
			range.select();
		}
  }-*/;

  private void transformMask() {
    StringBuilder contentBuilder = new StringBuilder(this.content);

    for (Integer i = 0; i < this.mask.toCharArray().length; i++) {
      Character ch = this.mask.toCharArray()[i];
      switch (ch) {
        case '#':
          this.expression.add("[0-9]{1}");
          contentBuilder.append("_");
          break;
        case '9':
          this.expression.add("[\\s0-9]{1}");
          contentBuilder.append("_");
          break;
        case 'A':
          this.expression.add("[A-Z]{1}");
          contentBuilder.append("_");
          break;
        case 'a':
          this.expression.add("[a-z]{1}");
          contentBuilder.append("_");
          break;
        case 'B':
          this.expression.add("[A-Za-z]{1}");
          contentBuilder.append("_");
          break;
        case 'C':
          this.expression.add("[A-Za-z0-9]{1}");
          contentBuilder.append("_");
          break;
        case '/':
        case ',':
        case '*':
        case '.':
        case '-':
        case ':':
        case '(':
        case ')':
        case '{':
        case '}':
        case '=':
        case '+':
        case ' ':
          contentBuilder.append(ch);
          break;
        default:
          break;
      }
    }

    this.content = contentBuilder.toString();
    this.baseContent = this.content;
    super.setText(this.content);
  }
}
