/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.lists.AgentList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

public class SelectAgentDialog extends DialogBox implements SelectDialog {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SelectAgentDialog> {
  }

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  @UiField
  Button emptyParentButton;

  @UiField(provided = true)
  AgentList searchResultsPanel;

  private static final Filter DEFAULT_FILTER_AGENT = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AGENT_SEARCH, "*"));

  public SelectAgentDialog(String title) {
    this(title, DEFAULT_FILTER_AGENT);
  }

  public SelectAgentDialog(String title, Filter filter) {

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AGENT_NAME));
    searchResultsPanel = new AgentList(filter, facets, "Agents", false);

    searchPanel = new SearchPanel(filter, RodaConstants.AGENT_SEARCH, messages.selectAipSearchPlaceHolder(), false,
      false);
    searchPanel.setList(searchResultsPanel);
    searchPanel.setDefaultFilterIncremental(true);

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);

    center();

    emptyParentButton.setVisible(false);

  }

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
    onChange();
    hide();
  }

  @UiHandler("emptyParentButton")
  void buttonEmptyParentHandler(ClickEvent e) {
    searchResultsPanel.getSelectionModel().clear();
    onChange();
    hide();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IsIndexed> valueChangeHandler) {
    return addHandler(valueChangeHandler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public Agent getValue() {
    return searchResultsPanel.getSelectionModel().getSelectedObject();
  }

  public void setEmptyParentButtonVisible() {
    emptyParentButton.setVisible(true);
  }
}
