/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AccessKeyDataPanel extends Composite implements HasValueChangeHandlers<AccessKey> {
  public static final String IS_WRONG = "isWrong";

  interface MyUiBinder extends UiBinder<Widget, AccessKeyDataPanel> {
  }

  private static AccessKeyDataPanel.MyUiBinder uiBinder = GWT.create(AccessKeyDataPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox name;

  @UiField
  Label nameError;

  @UiField
  DateBox expirationDate;

  @UiField
  Label expirationDateError;

  @UiField
  HTML errors;

  private final boolean editMode;

  private boolean changed = false;
  private boolean checked = false;

  public AccessKeyDataPanel(AccessKey accessKey, boolean editMode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;

    setInitialState(accessKey);
    initHandlers();

    if (editMode) {
      setAccessKey(accessKey);
    }
  }

  private void initHandlers() {
    ChangeHandler changeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        AccessKeyDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        AccessKeyDataPanel.this.onChange();
      }
    };
  }

  private void setInitialState(AccessKey accessKey) {
    errors.setVisible(false);

    DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    expirationDate.setFormat(dateFormat);
    expirationDate.getDatePicker().setYearArrowsVisible(true);
    expirationDate.setFireNullValues(true);
    expirationDate.setValue(new Date());

    expirationDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
      @Override
      public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
        onChange();
      }
    });
  }

  public void setAccessKey(AccessKey accessKey) {
    this.name.setText(accessKey.getName());
    this.expirationDate.setValue(accessKey.getExpirationDate());
  }

  public AccessKey getAccessKey() {
    AccessKey accessKey = new AccessKey();
    accessKey.setName(name.getText());
    accessKey.setExpirationDate(expirationDate.getValue());
    return accessKey;
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (StringUtils.isBlank(name.getText())) {
      name.addStyleName(IS_WRONG);
      nameError.setText(messages.mandatoryField());
      nameError.setVisible(true);
      Window.scrollTo(name.getAbsoluteLeft(), name.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.distributedInstanceNameLabel()));
    } else {
      name.removeStyleName(IS_WRONG);
      nameError.setVisible(false);
    }

    if (expirationDate.getValue() == null || expirationDate.getValue().before(new Date())) {
      expirationDate.addStyleName("isWrong");
      expirationDateError.setVisible(true);
      expirationDateError.setText(messages.mandatoryField());
      errorList.add(messages.isAMandatoryField(messages.accessKeyExpirationDateLabel()));
    } else {
      expirationDate.removeStyleName("isWrong");
      expirationDateError.setVisible(false);
    }

    return errorList.isEmpty();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<AccessKey> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  // @UiHandler("buttonAddMember")
  // void buttonAddMemberHandler(ClickEvent e) {
  // Filter filter = new Filter();
  // filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
  //
  // MemberSelectDialog selectDialog = new
  // MemberSelectDialog(messages.selectUserOrGroupToAdd(), filter);
  // selectDialog.showAndCenter();
  // selectDialog.addValueChangeHandler(new ValueChangeHandler<RODAMember>() {
  //
  // @Override
  // public void onValueChange(ValueChangeEvent<RODAMember> event) {
  // RODAMember selected = event.getValue();
  // if (selected != null) {
  // user.setText(selected.getName());
  // }
  // }
  // });
  // }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public AccessKey getValue() {
    return getAccessKey();
  }
}
