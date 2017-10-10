/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.common.IncrementalFilterList;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.IncrementalRelationList;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RepresentationInformationDataPanel extends Composite
  implements HasValueChangeHandlers<RepresentationInformation> {

  interface MyUiBinder extends UiBinder<Widget, RepresentationInformationDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  ListBox family;

  @UiField
  IncrementalList categories;

  @UiField
  Label extrasLabel;

  @UiField
  TextBox extras;

  @UiField
  ListBox support;

  @UiField(provided = true)
  IncrementalRelationList relations;

  @UiField
  IncrementalFilterList filters;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean editmode;
  private boolean changed = false;
  private boolean checked = false;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   */
  public RepresentationInformationDataPanel(boolean editmode, RepresentationInformation ri) {
    this(true, editmode, ri);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editmode
   */
  public RepresentationInformationDataPanel(boolean visible, final boolean editmode,
    final RepresentationInformation ri) {
    relations = new IncrementalRelationList(ri);
    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);
    filters.setVisible(false);

    // TODO extras should not be hidden
    extrasLabel.setVisible(false);
    extras.setVisible(false);

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        RepresentationInformationDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);
    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);
    family.addChangeHandler(changeHandler);
    family.addKeyUpHandler(keyUpHandler);
    categories.addChangeHandler(changeHandler);
    extras.addChangeHandler(changeHandler);

    support.addChangeHandler(changeHandler);
    relations.addChangeHandler(changeHandler);
    filters.addChangeHandler(changeHandler);

    for (RepresentationInformationSupport val : RepresentationInformationSupport.values()) {
      support.addItem(messages.representationInformationSupportValue(val.toString()), val.toString());
    }

    BrowserService.Util.getInstance().retrieveRepresentationInformationFamilyOptions(new AsyncCallback<List<String>>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final List<String> familyList) {
        for (String item : familyList) {
          family.addItem(item);
        }
      }
    });

    BrowserService.Util.getInstance().retrieveObjectClassFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RepresentationInformationFilterBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(RepresentationInformationFilterBundle result) {
          RepresentationInformationDataPanel.this.filters.setFields(result.getObjectClassFields());

          if (editmode) {
            setRepresentationInformation(ri);
          } else {
            LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
            List<String> lastHistory = selectedItems.getLastHistory();
            List<String> newFilters = new ArrayList<>();

            if (lastHistory.size() > 4
              && lastHistory.get(1).equals(RepresentationInformationRegister.RESOLVER.getHistoryToken())
              && lastHistory.get(2).equals(Search.RESOLVER.getHistoryToken())) {
              boolean hasOperator = lastHistory.size() % 2 == 1 ? false : true;
              int initialIndex = hasOperator ? 4 : 3;

              for (int i = initialIndex; i < lastHistory.size(); i = i + 2) {
                newFilters.add(lastHistory.get(i + 1));
              }

              RepresentationInformationDataPanel.this.filters.setFilters(newFilters);
            }
          }
        }
      });
  }

  public boolean isValid() {
    boolean valid = true;

    if (name.getText().isEmpty()) {
      valid = false;
      name.addStyleName("isWrong");
    } else {
      name.removeStyleName("isWrong");
    }

    checked = true;
    return valid;
  }

  public void setRepresentationInformation(RepresentationInformation ri) {
    this.name.setText(ri.getName());
    this.description.setText(ri.getDescription());

    int index = 0;
    for (int i = 0; i < this.family.getItemCount(); i++) {
      if (this.family.getValue(i).equals(ri.getFamily())) {
        index = i;
      }
    }

    this.family.setSelectedIndex(index);
    this.categories.setTextBoxList(ri.getCategories());
    this.extras.setText(ri.getExtras());

    for (int i = 0; i < support.getItemCount(); i++) {
      if (support.getValue(i).equals(ri.getSupport().toString())) {
        support.setSelectedIndex(i);
        break;
      }
    }

    this.relations.setRelationList(ri.getRelations());
    this.filters.setFilters(ri.getFilters());
  }

  public RepresentationInformation getRepresentationInformation() {
    RepresentationInformation ri = new RepresentationInformation();
    ri.setName(name.getText());
    ri.setDescription(description.getText());
    ri.setFamily(family.getSelectedValue());
    ri.setCategories(categories.getTextBoxesValue());
    ri.setExtras(extras.getText());

    ri.setSupport(RepresentationInformationSupport.valueOf(support.getSelectedValue()));
    ri.setRelations(relations.getValues());

    ri.setFilters(filters.getFiltersValue());
    return ri;
  }

  public void clear() {
    name.setText("");
    description.setText("");
    family.clear();
    categories.clearTextBoxes();
    extras.setText("");
    support.clear();
    relations.clear();
    filters.clearFilters();
  }

  /**
   * Is user data panel editable, i.e. on create user mode
   *
   * @return true if editable
   */
  public boolean isEditmode() {
    return editmode;
  }

  /**
   * Is user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RepresentationInformation> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public RepresentationInformation getValue() {
    return getRepresentationInformation();
  }
}
