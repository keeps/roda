package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

public class SelectAipDialog extends DialogBox implements HasValueChangeHandlers<IndexedAIP> {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SelectAipDialog> {
  }

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

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

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    searchResultsPanel = new AIPList(DEFAULT_FILTER_AIP, facets, messages.selectAipSearchResults(), false);

    setWidget(binder.createAndBindUi(this));

    searchInputBox.getElement().setPropertyString("placeholder", messages.selectAipSearchPlaceHolder());

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(title);

    center();

    selectButton.setEnabled(false);
    emptyParentButton.setVisible(false);

    searchInputBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });

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

  public void doSearch() {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    String basicQuery = searchInputBox.getText();
    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, basicQuery));
    }

    Filter filter;
    if (parameters.size() == 0) {
      filter = DEFAULT_FILTER_AIP;
    } else {
      filter = new Filter(parameters);
    }

    searchResultsPanel.setFilter(filter);
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
