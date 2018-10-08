/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import java.util.Date;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

public class EditMultipleRiskIncidenceDialog<T extends IsIndexed> extends DialogBox implements SelectDialog<T> {
  private static final Binder binder = GWT.create(Binder.class);

  @SuppressWarnings("rawtypes")
  interface Binder extends UiBinder<Widget, EditMultipleRiskIncidenceDialog> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel titlePanel;

  @UiField
  ListBox status, severity;

  @UiField
  DateBox mitigatedOn;

  @UiField
  TextBox mitigatedBy;

  @UiField
  TextArea mitigatedDescription;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  public EditMultipleRiskIncidenceDialog() {
    setWidget(binder.createAndBindUi(this));

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    mitigatedOn.setFormat(dateFormat);
    mitigatedOn.getDatePicker().setYearArrowsVisible(true);
    mitigatedOn.setFireNullValues(true);

    for (INCIDENCE_STATUS istatus : INCIDENCE_STATUS.values()) {
      status.addItem(messages.riskIncidenceStatusValue(istatus), istatus.toString());
    }

    for (SEVERITY_LEVEL iseverity : SEVERITY_LEVEL.values()) {
      severity.addItem(messages.severityLevel(iseverity), iseverity.toString());
    }

    titlePanel.add(new HTMLWidgetWrapper("EditRiskIncidenceDescription.html", new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(Void result) {
        center();
      }
    }));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);
    addStyleDependentName("wui-dialog-confirm");

    setText(messages.editIncidencesTitle());
  }

  @Override
  public void showAndCenter() {
    if (Window.getClientWidth() < 800) {
      this.setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  @UiHandler("cancelButton")
  void buttonCancelHandler(ClickEvent e) {
    hide();
  }

  @UiHandler("selectButton")
  void buttonSelectHandler(ClickEvent e) {
    ValueChangeEvent.fire(this, null);
    hide();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public String getStatus() {
    return status.getSelectedValue();
  }

  public String getSeverity() {
    return severity.getSelectedValue();
  }

  public Date getMitigatedOn() {
    return mitigatedOn.getValue();
  }

  public String getMitigatedBy() {
    return mitigatedBy.getValue();
  }

  public String getMitigatedDescription() {
    return mitigatedDescription.getValue();
  }
}
