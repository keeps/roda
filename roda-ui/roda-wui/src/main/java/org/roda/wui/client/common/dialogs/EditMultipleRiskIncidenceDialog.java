/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;

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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

public class EditMultipleRiskIncidenceDialog<T extends IsIndexed, O> extends DialogBox implements SelectDialog<T> {
  private static final Binder binder = GWT.create(Binder.class);

  @SuppressWarnings("rawtypes")
  interface Binder extends UiBinder<Widget, EditMultipleRiskIncidenceDialog> {
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private SelectedItems incidences = SelectedItemsNone.create();

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

  public EditMultipleRiskIncidenceDialog(SelectedItems selectedIncidences) {
    incidences = selectedIncidences;
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

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(messages.editIncidencesTitle());

    center();
  }

  public void showAndCenter() {
    if (Window.getClientWidth() < 800) {
      this.setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  public void setSingleSelectionMode() {
    selectButton.setVisible(false);
  }

  @UiHandler("cancelButton")
  void buttonCancelHandler(ClickEvent e) {
    hide();
  }

  @UiHandler("selectButton")
  void buttonSelectHandler(ClickEvent e) {
    hide();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

}
