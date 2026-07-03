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
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.forms.GenericDataPanel;
import org.roda.wui.client.common.forms.TagInputWidget;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.Services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RepresentationInformationDataPanel extends Composite
  implements GenericDataPanel<RepresentationInformation>, HasValueChangeHandlers<RepresentationInformation> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final GenericDataForm<RepresentationInformation> form;
  private final boolean editMode;
  // Manually managed widgets for form integration
  private final ListBox family;
  private final FlowPanel extras;
  private final IncrementalRelationList relations;
  private final IncrementalFilterList filters;
  private final Button saveButton;
  private final Button cancelButton;
  private final String originalFamily;
  private RepresentationInformationCustomForm customForm = null;

  public RepresentationInformationDataPanel(boolean editMode, RepresentationInformation ri) {
    this.editMode = editMode;
    this.originalFamily = ri.getFamily();
    this.form = new GenericDataForm<>();

    // 1. Initialize custom lists/panels
    this.relations = new IncrementalRelationList(ri);
    this.filters = new IncrementalFilterList();
    this.filters.setVisible(false);
    this.extras = new FlowPanel();

    this.family = new ListBox();
    ListBox support = new ListBox();
    TagInputWidget tags = new TagInputWidget();

    for (RepresentationInformationSupport val : RepresentationInformationSupport.values()) {
      support.addItem(messages.representationInformationSupportValue(val.toString()), val.toString());
    }

    // 2. Build generic form fields using method references
    form.addTextField(messages.representationInformationName(), RepresentationInformation::getName,
      RepresentationInformation::setName, true);

    form.addTextArea(messages.representationInformationDescription(), RepresentationInformation::getDescription,
      RepresentationInformation::setDescription, false);

    form.addTagField(messages.representationInformationTags(), tags, RepresentationInformation::getTags,
      RepresentationInformation::setTags, false);

    form.addListBox(messages.representationInformationSupport(), support,
      s -> s.getSupport() != null ? s.getSupport().name() : "",
      (s, val) -> s.setSupport(val != null && !val.isEmpty() ? RepresentationInformationSupport.valueOf(val) : null),
      false);

    form.addListBox(messages.representationInformationFamily(), family, RepresentationInformation::getFamily,
      RepresentationInformation::setFamily, false);

    // 3. Inject the custom metadata form container sequentially after tags
    extras.getElement().getStyle().setProperty("display", "contents");
    form.addCustomWidget(extras);

    // Inject complex lists that aren't natively supported by GenericDataForm but
    // wrap them to match style
    form.addCustomWidget(createCustomFieldContainer(messages.representationInformationRelations(), relations));

    // 4. Initialize and inject Buttons at the bottom
    saveButton = new Button(messages.saveButton());
    saveButton.addStyleName("btn btn-primary btn-play");

    cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");

    FlowPanel actionsPanel = new FlowPanel();
    actionsPanel.addStyleName("alignButtonsPanel");
    actionsPanel.add(saveButton);
    actionsPanel.add(cancelButton);

    form.addCustomWidget(actionsPanel);

    // 5. Add Handlers bridging external widgets to trigger generic form changes
    family.addChangeHandler(event -> {
      ValueChangeEvent.fire(this, getValue());
      handleFamilyChange(ri);
    });

    relations.addChangeHandler(event -> ValueChangeEvent.fire(this, getValue()));
    filters.addChangeHandler(event -> ValueChangeEvent.fire(this, getValue()));
    form.addValueChangeHandler(event -> ValueChangeEvent.fire(this, getValue()));

    // 6. Initialize layout
    initWidget(form);

    // 7. Fetch async dependencies
    loadAsyncData(ri);
  }

  private void handleFamilyChange(RepresentationInformation ri) {
    extras.clear();
    Services services = new Services("Retrieve representation information family metadata", "get");

    // Check if we are in edit mode and the user reverted to the originally saved
    // family
    boolean isRevertedToOriginal = editMode && family.getSelectedValue().equals(originalFamily);

    if (isRevertedToOriginal) {
      // Restore the originally saved form and its custom values from the database
      services
        .representationInformationResource(
          s -> s.retrieveRepresentationInformationFamily(ri.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((representationInformationFamily, throwable) -> {
          if (throwable == null) {
            customForm = new RepresentationInformationCustomForm();
            customForm.setValues(representationInformationFamily.getFamilyValues());
            FormUtilities.create(extras, representationInformationFamily.getFamilyValues(), false, () -> {
              ValueChangeEvent.fire(RepresentationInformationDataPanel.this, getValue());
              return null;
            });
          }
        });
    } else {
      // Fetch the blank configuration template for the newly selected family type
      services
        .representationInformationResource(s -> s.retrieveRepresentationInformationFamilyConfigurations(
          family.getSelectedValue(), LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((representationInformationFamily, throwable) -> {
          if (throwable == null) {
            customForm = new RepresentationInformationCustomForm();
            customForm.setValues(representationInformationFamily.getFamilyValues());
            FormUtilities.create(extras, representationInformationFamily.getFamilyValues(), false, () -> {
              ValueChangeEvent.fire(RepresentationInformationDataPanel.this, getValue());
              return null;
            });
          }
        });
    }
  }

  private void loadAsyncData(RepresentationInformation ri) {
    if (editMode) {
      Services services = new Services("Retrieve representation information family metadata", "get");
      CompletableFuture<RepresentationInformationFamily> riFamilyCompletableFuture = services
        .representationInformationResource(
          s -> s.retrieveRepresentationInformationFamily(ri.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
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
          ValueChangeEvent.fire(RepresentationInformationDataPanel.this, getValue());
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

        setRepresentationInformation(ri);
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

          // Update model with the preset name based on association
          ri.setName(
            messages.representationInformationNameFromAssociation(filterParts[0], filterParts[1], filterParts[2]));
          setRepresentationInformation(ri); // Refresh
        }

        return null;
      });
    }
  }

  private FlowPanel createCustomFieldContainer(String labelText, Widget widget) {
    FlowPanel searchField = new FlowPanel();
    searchField.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(labelText);
    label.addStyleName("form-label");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    leftPanel.add(label);
    leftPanel.add(inputPanel);
    searchField.add(leftPanel);
    inputPanel.add(widget);

    return searchField;
  }

  public void setSaveHandler(Runnable onSave) {
    saveButton.addClickHandler(event -> {
      if (isValid()) {
        onSave.run();
      }
    });
  }

  // --- BUTTON HANDLERS ---

  public void setCancelHandler(Runnable onCancel) {
    cancelButton.addClickHandler(event -> onCancel.run());
  }

  @Override
  public RepresentationInformation getValue() {
    RepresentationInformation ri = form.getValue();

    ri.setRelations(relations.getValues());
    ri.setFilters(filters.getFiltersValue());

    return ri;
  }

  public RepresentationInformation getRepresentationInformation() {
    return getValue();
  }

  public void setRepresentationInformation(RepresentationInformation ri) {
    form.setModel(ri);
    this.relations.setRelationList(ri.getRelations());
    this.filters.setFilters(ri.getFilters());
  }

  public void clear() {
    setRepresentationInformation(new RepresentationInformation());
    extras.clear();
    customForm = null;
  }

  public boolean isEditMode() {
    return editMode;
  }

  public boolean isChanged() {
    return form.isChanged();
  }

  @Override
  public boolean isValid() {
    return form.isValid();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RepresentationInformation> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public RepresentationInformationCustomForm getCustomForm() {
    return customForm;
  }
}