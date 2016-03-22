/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.AIPList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

public class SelectAipDialog extends DialogBox implements HasValueChangeHandlers<IndexedAIP> {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SelectAipDialog> {
  }

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField
  Button cancelButton;

  @UiField
  Button selectButton;

  @UiField
  Button emptyParentButton;

  @UiField(provided = true)
  AIPList searchResultsPanel;

  private String aipId;

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));

  public SelectAipDialog(String title) {
    this(title, null);
  }

  public SelectAipDialog(String title, String aipId) {
    this.aipId = aipId;

    basicSearch = new BasicSearch(DEFAULT_FILTER_AIP, RodaConstants.AIP_SEARCH, messages.selectAipSearchPlaceHolder(),
      false, false);

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    searchResultsPanel = new AIPList(DEFAULT_FILTER_AIP, facets, messages.selectAipSearchResults(), false);

    basicSearch = new BasicSearch(DEFAULT_FILTER_AIP, RodaConstants.AIP_SEARCH, messages.selectAipSearchPlaceHolder(),
      false, false);
    basicSearch.setList(searchResultsPanel);

    setWidget(binder.createAndBindUi(this));

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);

    center();

    selectButton.setEnabled(false);
    emptyParentButton.setVisible(false);

    searchResultsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedAIP aip = searchResultsPanel.getSelectionModel().getSelectedObject();
        if (aip != null && !aip.getId().equals(SelectAipDialog.this.aipId)) {
          selectButton.setEnabled(true);
        } else {
          selectButton.setEnabled(false);
        }
      }
    });
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
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IndexedAIP> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public IndexedAIP getValue() {
    return searchResultsPanel.getSelectionModel().getSelectedObject();
  }

  public void setEmptyParentButtonVisible() {
    emptyParentButton.setVisible(true);
  }
}
