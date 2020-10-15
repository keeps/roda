package org.roda.wui.client.disposal;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.disposal.DisposalHold;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalHoldDataPanel extends Composite implements HasValueChangeHandlers<DisposalHold> {

  interface MyUiBinder extends UiBinder<Widget, DisposalHoldDataPanel> {
  }

  private static DisposalHoldDataPanel.MyUiBinder uiBinder = GWT.create(DisposalHoldDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox title;

  @UiField
  Label titleError;

  @UiField
  TextBox description;

  @UiField
  TextBox mandate;

  @UiField
  TextArea notes;

  private boolean editmode;

  // has to be true to detected new field changes
  private boolean changed = true;
  private boolean checked = false;

  @UiField
  HTML errors;

  public DisposalHoldDataPanel(boolean visible, boolean editmode) {

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);

    errors.setVisible(false);

    ValueChangeHandler<String> valueChangedHandler = new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onChange();
      }
    };

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        DisposalHoldDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };
  }

  public void setDiposalHold(DisposalHold disposalHold) {
    this.title.setText(disposalHold.getTitle());
  }

  public DisposalHold getDisposalHold() {
    DisposalHold disposalHold = new DisposalHold();
    disposalHold.setTitle(title.getText());
    disposalHold.setDescription(description.getText());
    disposalHold.setMandate(mandate.getText());
    disposalHold.setScopeNotes(notes.getText());
    return disposalHold;
  }

  /**
   * Is disposal hold data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (title.getText().length() == 0) {
      title.addStyleName("isWrong");
      titleError.setText(messages.mandatoryField());
      titleError.setVisible(true);
      Window.scrollTo(title.getAbsoluteLeft(), title.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      title.removeStyleName("isWrong");
      titleError.setVisible(false);
    }

    checked = true;

    return errorList.isEmpty() ? true : false;
  }

  public boolean isTitleReadOnly() {
    return title.isReadOnly();
  }

  public void setTitleReadOnly(boolean readonly) {
    title.setReadOnly(readonly);
  }

  public boolean isMandateReadOnly() {
    return mandate.isReadOnly();
  }

  public void setMandateReadOnly(boolean readonly) {
    mandate.setReadOnly(readonly);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
  }

  public void clear() {
    title.setText("");
    description.setText("");
    mandate.setText("");
  }

  public boolean isEditmode() {
    return editmode;
  }

  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DisposalHold> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public DisposalHold getValue() {
    return getDisposalHold();
  }

}
