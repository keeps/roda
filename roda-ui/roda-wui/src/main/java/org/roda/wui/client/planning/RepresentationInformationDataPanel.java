/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCustomForm;
import org.roda.core.data.v2.ri.RepresentationInformationFamily;
import org.roda.core.data.v2.ri.RepresentationInformationFamilyOptions;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.wui.client.common.IncrementalFilterList;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.IncrementalRelationList;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.Services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox name;

  @UiField
  TextArea description;

  @UiField
  ListBox family;

  @UiField(provided = true)
  IncrementalList tags;

  @UiField
  FlowPanel extras;

  @UiField
  ListBox support;

  @UiField(provided = true)
  IncrementalRelationList relations;

  @UiField
  IncrementalFilterList filters;

  private boolean editmode;
  private boolean changed = false;
  private boolean checked = false;
  private RepresentationInformationCustomForm customForm = null;

  /**
   * Create a new user data panel
   *
   * @param editMode
   *          if user name should be editable
   */
  public RepresentationInformationDataPanel(boolean editMode, RepresentationInformation ri) {
    this(true, editMode, ri);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editMode
   */
  public RepresentationInformationDataPanel(boolean visible, final boolean editMode,
    final RepresentationInformation ri) {
    relations = new IncrementalRelationList(ri);
    tags = new IncrementalList(true);
    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editMode;
    super.setVisible(visible);
    filters.setVisible(false);

    ChangeHandler changeHandler = event -> RepresentationInformationDataPanel.this.onChange();

    ValueChangeHandler valueChangeHandler = event -> RepresentationInformationDataPanel.this.onChange();

    KeyUpHandler keyUpHandler = event -> onChange();

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);
    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);

    ChangeHandler familyChangeHandler = event -> {
      RepresentationInformationDataPanel.this.onChange();
      extras.clear();
      Services services = new Services("Retrieve representation information family metadata", "get");
      if (editMode) {
        services
          .representationInformationResource(s -> s.retrieveRepresentationInformationFamily(ri.getId(),
            family.getSelectedValue(), LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((representationInformationFamily, throwable) -> {
            customForm = new RepresentationInformationCustomForm();
            customForm.setValues(representationInformationFamily.getFamilyValues());
            FormUtilities.create(extras,
            representationInformationFamily.getFamilyValues(), false, () -> {
              RepresentationInformationDataPanel.this.onChange();
              return null;
              });
          });
      } else {
        services
          .representationInformationResource(s -> s.retrieveRepresentationInformationFamilyConfigurations(
            family.getSelectedValue(), LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((representationInformationFamily, throwable) -> {
            customForm = new RepresentationInformationCustomForm();
            customForm.setValues(representationInformationFamily.getFamilyValues());
            if (throwable == null) {
              FormUtilities.create(extras, representationInformationFamily.getFamilyValues(), false, () -> {
                RepresentationInformationDataPanel.this.onChange();
                return null;
              });
            }
          });
      }
    };

    family.addChangeHandler(familyChangeHandler);
    family.addKeyUpHandler(keyUpHandler);
    tags.addValueChangeHandler(valueChangeHandler);

    support.addChangeHandler(changeHandler);
    relations.addChangeHandler(changeHandler);
    filters.addChangeHandler(changeHandler);

    tags.setRemovableTextBoxTitle(messages.representationInformationTags());

    for (RepresentationInformationSupport val : RepresentationInformationSupport.values()) {
      support.addItem(messages.representationInformationSupportValue(val.toString()), val.toString());
    }

    if (editMode) {
      Services services = new Services("Retrieve representation information family metadata", "get");
      CompletableFuture<RepresentationInformationFamily> riFamilyCompletableFuture = services
        .representationInformationResource(s -> s.retrieveRepresentationInformationFamily(ri.getId(), ri.getFamily(),
          LocaleInfo.getCurrentLocale().getLocaleName()))
        .toCompletableFuture();
      CompletableFuture<RepresentationInformationFamilyOptions> riFamilyOptionsCompletableFuture = services
        .representationInformationResource(
          s -> s.retrieveRepresentationInformationFamilyOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
        .toCompletableFuture();

      CompletableFuture.allOf(riFamilyCompletableFuture, riFamilyOptionsCompletableFuture).thenApplyAsync(unused -> {
        RepresentationInformationFamily representationInformationFamily = riFamilyCompletableFuture.join();
        RepresentationInformationFamilyOptions options = riFamilyOptionsCompletableFuture.join();

        extras.clear();
        customForm = new RepresentationInformationCustomForm();
        customForm.setValues(representationInformationFamily.getFamilyValues());
        FormUtilities.create(extras, representationInformationFamily.getFamilyValues(), false, () -> {
          RepresentationInformationDataPanel.this.onChange();
          return null;
        });

        for (Entry<String, String> item : options.getOptions().entrySet()) {
          family.addItem(item.getValue(), item.getKey());
        }

        setRepresentationInformation(ri);

        return null;
      });
    } else {
      Services services = new Services("Retrieve representation information family options", "get");
      CompletableFuture<RepresentationInformationFamilyOptions> riFamilyOptionsCompletableFuture = services
        .representationInformationResource(
          s -> s.retrieveRepresentationInformationFamilyOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
        .toCompletableFuture();

      CompletableFuture.allOf(riFamilyOptionsCompletableFuture).thenApplyAsync(unused -> {
        RepresentationInformationFamilyOptions options = riFamilyOptionsCompletableFuture.join();

        for (Entry<String, String> item : options.getOptions().entrySet()) {
          family.addItem(item.getValue(), item.getKey());
        }

        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), family);

        LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
        List<String> lastHistory = selectedItems.getLastHistory();

        if (lastHistory.size() > 4 && lastHistory.get(0).equals(Planning.RESOLVER.getHistoryToken())
          && lastHistory.get(1).equals(RepresentationInformationNetwork.RESOLVER.getHistoryToken())
          && lastHistory.get(2).equals(RepresentationInformationAssociations.RESOLVER.getHistoryToken())) {

          RepresentationInformationDataPanel.this.filters
            .setFilters(Collections.singletonList(lastHistory.get(lastHistory.size() - 1)));

          String[] filterParts = RepresentationInformationUtils
            .breakFilterIntoParts(lastHistory.get(lastHistory.size() - 1));
          RepresentationInformationDataPanel.this.name.setText(
            messages.representationInformationNameFromAssociation(filterParts[0], filterParts[1], filterParts[2]));
        }

        return null;

      });
    }
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
    this.tags.setTextBoxList(ri.getTags());

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
    ri.setTags(tags.getTextBoxesValue());

    ri.setSupport(RepresentationInformationSupport.valueOf(support.getSelectedValue()));
    ri.setRelations(relations.getValues());

    ri.setFilters(filters.getFiltersValue());
    return ri;
  }

  public void clear() {
    name.setText("");
    description.setText("");
    family.clear();
    tags.clearTextBoxes();
    extras.clear();
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

  public RepresentationInformationCustomForm getCustomForm() {
    return customForm;
  }
}
