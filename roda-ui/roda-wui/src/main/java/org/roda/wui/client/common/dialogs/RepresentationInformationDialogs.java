/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.properties.ObjectClassFields;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.core.data.v2.ri.RepresentationInformationRelationOptions;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.ValuedLabel;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.Dropdown;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.search.SelectedPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class RepresentationInformationDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final String SEARCH_ITEMS = IndexedAIP.class.getSimpleName();
  private static final String SEARCH_REPRESENTATIONS = IndexedRepresentation.class.getSimpleName();
  private static final String SEARCH_FILES = IndexedFile.class.getSimpleName();

  private RepresentationInformationDialogs() {
    // do nothing
  }

  public static void showPromptDialogRepresentationInformation(String title, String cancelButtonText,
    String confirmButtonText, String listButtonText, final RepresentationInformation ri,
    final AsyncCallback<RepresentationInformation> callback) {
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.addStyleName("ri-dialog");
    dialogBox.addStyleName("wui-dialog-prompt");
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    layout.addStyleName("wui-dialog-layout");

    HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIAssociationsDescription.html", null,
      RodaConstants.ResourcesTypes.INTERNAL, new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Void result) {
          dialogBox.center();
        }
      });

    description.addStyleName("page-description");
    layout.add(description);

    final FlowPanel relationFormPanel = new FlowPanel();
    relationFormPanel.addStyleName("relation-form-panel");
    layout.add(relationFormPanel);
    final FlowPanel listPanel = new FlowPanel();
    layout.add(listPanel);

    final Label section = new Label(messages.currentRelationResults());
    section.addStyleName("ri-form-separator");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);
    final Button searchButton = new Button(listButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(confirmButton);
    buttonPanel.add(searchButton);

    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    final String aipListId = "RepresentationInformationDialogs_AIPs";
    final String representationsListId = "RepresentationInformationDialogs_representations";
    final String filesListId = "RepresentationInformationDialogs_files";

    final Dropdown dropDown = new Dropdown();
    dropDown.setStyleName("searchInputListBox ri-dropdown-listbox");
    dropDown.addPopupStyleName("searchInputListBoxPopup");
    dropDown.setVisible(true);
    dropDown.setLabel(messages.searchListBoxItems());
    dropDown.addItem(messages.searchListBoxItems(), SEARCH_ITEMS,
      SelectedPanel.getIconForList(aipListId, IndexedAIP.class.getSimpleName()));
    dropDown.addItem(messages.searchListBoxRepresentations(), SEARCH_REPRESENTATIONS,
      SelectedPanel.getIconForList(representationsListId, IndexedRepresentation.class.getSimpleName()));
    dropDown.addItem(messages.searchListBoxFiles(), SEARCH_FILES,
      SelectedPanel.getIconForList(filesListId, IndexedFile.class.getSimpleName()));

    final FlowPanel fieldsPanel = new FlowPanel();
    fieldsPanel.setStyleName("ri-content-group");

    Services services = new Services("Retrieves object class fields from configurations", "get");
    services.configurationsResource(s -> s.retrieveObjectClassFields(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((result, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable.getCause());
        } else {
          relationFormPanel.add(dropDown);
          relationFormPanel.add(fieldsPanel);

          final List<String> appropriateFields = new ArrayList<>();
          final Map<String, List<String>> values = new HashMap<>();

          dropDown.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
              updateAssociationFields(fieldsPanel, dropDown, appropriateFields, result, ri, values, searchButton);

              // set initial enabled status for searchButton
              boolean searchButtonEnabled = false;
              for (String field : appropriateFields) {
                if (!searchButtonEnabled && values.containsKey(field)) {
                  for (String value : values.get(field)) {
                    if (StringUtils.isNotBlank(value)) {
                      searchButtonEnabled = true;
                      break;
                    }
                  }
                }
              }
              searchButton.setEnabled(searchButtonEnabled);

              dialogBox.center();
            }
          });

          if (!ri.getFilters().isEmpty()) {
            if (ri.getFilters().get(0).startsWith(Representation.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(Representation.class.getSimpleName()));
              dropDown.setSelectedValue(SEARCH_REPRESENTATIONS, true);
            } else if (ri.getFilters().get(0).startsWith(File.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(File.class.getSimpleName()));
              dropDown.setSelectedValue(SEARCH_FILES, true);
            } else if (ri.getFilters().get(0).startsWith(AIP.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(AIP.class.getSimpleName()));
              dropDown.setSelectedValue(SEARCH_ITEMS, true);
            }
          } else {
            appropriateFields.addAll(result.getObjectClassFields().get(AIP.class.getSimpleName()));
            dropDown.setSelectedValue(SEARCH_ITEMS, true);
          }

          dialogBox.center();

          confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              ri.setFilters(new ArrayList<String>());

              String className;
              if (SEARCH_ITEMS.equals(dropDown.getSelectedValue())) {
                className = AIP.class.getSimpleName();
              } else if (SEARCH_REPRESENTATIONS.equals(dropDown.getSelectedValue())) {
                className = Representation.class.getSimpleName();
              } else if (SEARCH_FILES.equals(dropDown.getSelectedValue())) {
                className = File.class.getSimpleName();
              } else {
                return;
              }

              for (String field : values.keySet()) {
                for (String value : values.get(field)) {
                  if (StringUtils.isNotBlank(value)) {
                    ri.addFilter(
                      RepresentationInformationUtils.createRepresentationInformationFilter(className, field, value));
                  }
                }
              }

              callback.onSuccess(ri);
            }
          });

          searchButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              List<FilterParameter> filterList = new ArrayList<>();
              for (String field : appropriateFields) {
                if (values.containsKey(field)) {
                  for (String value : values.get(field)) {
                    if (StringUtils.isNotBlank(value)) {
                      filterList.add(new SimpleFilterParameter(field, value));
                    }
                  }
                }
              }

              listPanel.clear();

              if (!filterList.isEmpty()) {
                ListBuilder<?> listBuilder = null;
                Filter tableFilter = new Filter(new OrFiltersParameters(filterList));

                if (SEARCH_ITEMS.equals(dropDown.getSelectedValue())) {
                  listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
                    new AsyncTableCellOptions<>(IndexedAIP.class, aipListId).withFilter(tableFilter)
                      .withJustActive(true).withCsvDownloadButtonVisibility(false)
                      .withRecenteringOfParentDialog(dialogBox));

                } else if (SEARCH_REPRESENTATIONS.equals(dropDown.getSelectedValue())) {
                  listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
                    new AsyncTableCellOptions<>(IndexedRepresentation.class, representationsListId)
                      .withFilter(tableFilter).withJustActive(true).withCsvDownloadButtonVisibility(false)
                      .withRecenteringOfParentDialog(dialogBox));

                } else if (SEARCH_FILES.equals(dropDown.getSelectedValue())) {
                  listBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
                    new AsyncTableCellOptions<>(IndexedFile.class, filesListId).withFilter(tableFilter)
                      .withJustActive(true).withCsvDownloadButtonVisibility(false)
                      .withRecenteringOfParentDialog(dialogBox));

                }

                if (listBuilder != null) {
                  listPanel.add(section);
                  listPanel.add(listBuilder.build());
                }
              } else {
                dialogBox.center();
              }
            }
          });
        }
      });

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right left-spaced btn btn-play");
    searchButton.addStyleName("pull-right btn btn-search");

    dialogBox.center();
    dialogBox.show();
  }

  private static void updateAssociationFields(FlowPanel fieldsPanel, Dropdown dropDown, List<String> appropriateFields,
    ObjectClassFields result, RepresentationInformation ri, final Map<String, List<String>> values,
    Button searchButton) {
    fieldsPanel.clear();
    String className = null;

    if (dropDown.getSelectedValue().equals(SEARCH_ITEMS)) {
      className = AIP.class.getSimpleName();
    } else if (dropDown.getSelectedValue().equals(SEARCH_REPRESENTATIONS)) {
      className = Representation.class.getSimpleName();
    } else if (dropDown.getSelectedValue().equals(SEARCH_FILES)) {
      className = File.class.getSimpleName();
    }

    appropriateFields.clear();
    appropriateFields.addAll(result.getObjectClassFields().get(className));

    for (final String field : appropriateFields) {
      String filterClassField = className + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR
        + field;

      FlowPanel fieldPanel = new FlowPanel();
      fieldPanel.addStyleName("content ri-content");

      ValuedLabel fieldLabel = new ValuedLabel(result.getTranslations().get(filterClassField), field);
      fieldLabel.addStyleName("form-label ri-content-label");
      fieldPanel.add(fieldLabel);

      List<String> valuesForThisField = new ArrayList<>();

      for (String filter : ri.getFilters()) {
        if (filter.startsWith(filterClassField)) {
          valuesForThisField.add(RepresentationInformationUtils.getValueFromFilter(filter));
        }
      }

      values.put(field, valuesForThisField);

      IncrementalList incrementalList = new IncrementalList(true, valuesForThisField);
      incrementalList.addValueChangeHandler(event -> {
        values.put(field, event.getValue());

        // update enabled status for searchButton
        boolean enabled = false;
        for (String appropriateField : appropriateFields) {
          if (!enabled && values.containsKey(appropriateField)) {
            for (String value : values.get(appropriateField)) {
              if (StringUtils.isNotBlank(value)) {
                enabled = true;
                break;
              }
            }
          }
        }
        searchButton.setEnabled(enabled);
      });

      fieldPanel.add(incrementalList);
      fieldsPanel.add(fieldPanel);
    }
  }

  public static void showPromptDialogRepresentationInformationRelations(String title, final String cancelButtonText,
    final String confirmButtonText, final RepresentationInformation ri,
    final AsyncCallback<RepresentationInformationRelation> callback) {
    final List<HandlerRegistration> clickHandlers = new ArrayList<>();
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.addStyleName("ri-dialog");
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();
    layout.addStyleName("wui-dialog-layout");

    Services services = new Services("Retreive relation type options", "get");
    services
      .representationInformationResource(
        s -> s.retrieveRepresentationInformationRelationOptions(LocaleInfo.getCurrentLocale().toString()))
      .whenComplete((representationInformationRelationOptions, throwable) -> {
        if (throwable == null) {
          final FlowPanel content = new FlowPanel();
          content.addStyleName("row skip_padding full_width content");

          final FlowPanel leftSide = new FlowPanel();
          leftSide.addStyleName("dialog-left-side col_3");

          final FlowPanel rightSide = new FlowPanel();
          rightSide.addStyleName("dialog-right-side col_9");

          final Label aipLabel = new Label();
          aipLabel.setText(messages.representationInformationRelationObjectType(RelationObjectType.AIP.toString()));
          aipLabel.setTitle(messages.representationInformationRelationObjectType(RelationObjectType.AIP.toString()));
          aipLabel.addStyleName("dialog-left-item-label");
          leftSide.add(aipLabel);

          final Label riLabel = new Label();
          riLabel.setText(messages
            .representationInformationRelationObjectType(RelationObjectType.REPRESENTATION_INFORMATION.toString()));
          riLabel.setTitle(messages
            .representationInformationRelationObjectType(RelationObjectType.REPRESENTATION_INFORMATION.toString()));
          riLabel.addStyleName("dialog-left-item-label");
          leftSide.add(riLabel);

          final Label webLabel = new Label();
          webLabel.setText(messages.representationInformationRelationObjectType(RelationObjectType.WEB.toString()));
          webLabel.setTitle(messages.representationInformationRelationObjectType(RelationObjectType.WEB.toString()));
          webLabel.addStyleName("dialog-left-item-label");
          leftSide.add(webLabel);

          final Label txtLabel = new Label();
          txtLabel.setText(messages.representationInformationRelationObjectType(RelationObjectType.TEXT.toString()));
          txtLabel.setTitle(messages.representationInformationRelationObjectType(RelationObjectType.TEXT.toString()));
          txtLabel.addStyleName("dialog-left-item-label");
          leftSide.add(txtLabel);

          content.add(leftSide);
          content.add(rightSide);
          layout.add(content);

          final FlowPanel buttonPanel = new FlowPanel();
          final Button cancelButton = new Button(cancelButtonText);
          final Button confirmButton = new Button(confirmButtonText);
          confirmButton.setEnabled(false);
          final Label helpLabel = new Label(messages.title("help"));
          buttonPanel.add(confirmButton);
          buttonPanel.add(cancelButton);
          buttonPanel.add(helpLabel);
          layout.add(buttonPanel);
          dialogBox.setWidget(layout);

          dialogBox.setGlassEnabled(true);
          dialogBox.setAnimationEnabled(false);

          cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              callback.onFailure(null);
            }
          });

          cancelButton.addStyleName("pull-right btn btn-link");
          confirmButton.addStyleName("pull-right btn btn-play");
          helpLabel.addStyleName("btn btn-link");

          AsyncCallback<Void> centerDialogBox = new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              dialogBox.center();
            }

            @Override
            public void onSuccess(Void result) {
              dialogBox.center();
            }
          };

          showAIPDescription(aipLabel, riLabel, txtLabel, webLabel, rightSide, representationInformationRelationOptions,
            ri, confirmButton, clickHandlers, dialogBox, callback, centerDialogBox);

          dialogBox.center();
          dialogBox.show();

          helpLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              aipLabel.removeStyleName("dialog-left-item-selected");
              riLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");
              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionHelp.html", null,
                RodaConstants.ResourcesTypes.INTERNAL, centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              confirmButton.setEnabled(false);
            }
          });

          aipLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              showAIPDescription(aipLabel, riLabel, txtLabel, webLabel, rightSide,
                representationInformationRelationOptions, ri, confirmButton, clickHandlers, dialogBox, callback,
                centerDialogBox);
            }
          });

          riLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              riLabel.addStyleName("dialog-left-item-selected");
              aipLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithRI.html", null,
                RodaConstants.ResourcesTypes.INTERNAL, centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : representationInformationRelationOptions.getRelationsTranslations()
                .get(RelationObjectType.REPRESENTATION_INFORMATION.toString()).entrySet()) {
                select.addItem(type.getValue(), type.getKey());
              }
              rightSide.add(select);

              Label linkLabel = new Label(messages.representationInformationRelationLink());
              linkLabel.addStyleName("form-label");
              rightSide.add(linkLabel);

              final Button button = new Button(messages.selectButton());
              button.addStyleName("btn btn-search");
              rightSide.add(button);

              final ValuedLabel linkText = new ValuedLabel();
              linkText.setStyleName("label");
              linkText.setVisible(false);
              rightSide.add(linkText);

              Label titleLabel = new Label(messages.representationInformationRelationTitle());
              titleLabel.addStyleName("form-label");
              rightSide.add(titleLabel);

              final TextBox titleBox = new TextBox();
              titleBox.addStyleName("form-textbox");
              rightSide.add(titleBox);

              button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, ri.getId()));
                  SelectRepresentationInformationDialog selectDialog = new SelectRepresentationInformationDialog(
                    messages.chooseEntityTitle(), filter, false);
                  selectDialog.setSingleSelectionMode();
                  selectDialog.showAndCenter();
                  selectDialog.addValueChangeHandler(new ValueChangeHandler<RepresentationInformation>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<RepresentationInformation> event) {
                      final RepresentationInformation ri = event.getValue();
                      button.setVisible(false);
                      linkText.setVisible(true);
                      linkText.setText(ri.getName());
                      linkText.setValue(ri.getId());

                      if (titleBox.getText().isEmpty()) {
                        titleBox.setText(ri.getName());
                      }

                      confirmButton.setEnabled(true);
                    }
                  });
                }
              });

              for (HandlerRegistration handler : clickHandlers) {
                handler.removeHandler();
              }

              clickHandlers.add(confirmButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  if (!titleBox.getText().isEmpty() && !linkText.getValue().isEmpty()) {
                    dialogBox.hide();
                    callback.onSuccess(new RepresentationInformationRelation(select.getSelectedValue(),
                      RelationObjectType.REPRESENTATION_INFORMATION, linkText.getValue(), titleBox.getValue()));
                  } else {
                    Toast.showError(messages.representationInformationMissingFieldsTitle(),
                      messages.representationInformationMissingFields());
                  }
                }
              }));
            }
          });

          txtLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              txtLabel.addStyleName("dialog-left-item-selected");
              aipLabel.removeStyleName("dialog-left-item-selected");
              riLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithText.html", null,
                RodaConstants.ResourcesTypes.INTERNAL, centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : representationInformationRelationOptions.getRelationsTranslations()
                .get(RelationObjectType.TEXT.toString()).entrySet()) {
                select.addItem(type.getValue(), type.getKey());
              }
              rightSide.add(select);

              Label titleLabel = new Label(messages.representationInformationRelationTitle());
              titleLabel.addStyleName("form-label");
              rightSide.add(titleLabel);

              final TextBox titleBox = new TextBox();
              titleBox.setStyleName("form-textbox");
              rightSide.add(titleBox);

              confirmButton.setEnabled(true);

              for (HandlerRegistration handler : clickHandlers) {
                handler.removeHandler();
              }

              clickHandlers.add(confirmButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  if (!titleBox.getText().isEmpty()) {
                    dialogBox.hide();
                    callback.onSuccess(new RepresentationInformationRelation(select.getSelectedValue(),
                      RelationObjectType.TEXT, titleBox.getValue(), titleBox.getValue()));
                  } else {
                    Toast.showError(messages.representationInformationMissingFieldsTitle(),
                      messages.representationInformationMissingFields());
                  }
                }
              }));
            }
          });

          webLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              webLabel.addStyleName("dialog-left-item-selected");
              aipLabel.removeStyleName("dialog-left-item-selected");
              riLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithWeb.html", null,
                RodaConstants.ResourcesTypes.INTERNAL, centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : representationInformationRelationOptions.getRelationsTranslations()
                .get(RelationObjectType.WEB.toString()).entrySet()) {
                select.addItem(type.getValue(), type.getKey());
              }
              rightSide.add(select);

              Label linkLabel = new Label(messages.representationInformationRelationLink());
              linkLabel.addStyleName("form-label");
              rightSide.add(linkLabel);

              final TextBox linkText = new TextBox();
              linkText.setStyleName("form-textbox");
              rightSide.add(linkText);

              Label titleLabel = new Label(messages.representationInformationRelationTitle());
              titleLabel.addStyleName("form-label");
              rightSide.add(titleLabel);

              final TextBox titleBox = new TextBox();
              titleBox.addStyleName("form-textbox");
              rightSide.add(titleBox);

              confirmButton.setEnabled(true);

              for (HandlerRegistration handler : clickHandlers) {
                handler.removeHandler();
              }

              clickHandlers.add(confirmButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  if (!titleBox.getText().isEmpty() && !linkText.getText().isEmpty()) {
                    dialogBox.hide();
                    callback.onSuccess(new RepresentationInformationRelation(select.getSelectedValue(),
                      RelationObjectType.WEB, linkText.getValue(), titleBox.getValue()));
                  } else {
                    Toast.showError(messages.representationInformationMissingFieldsTitle(),
                      messages.representationInformationMissingFields());
                  }
                }
              }));
            }
          });
        }
      });
  }

  private static void showAIPDescription(Label aipLabel, Label riLabel, Label txtLabel, Label webLabel,
    FlowPanel rightSide, RepresentationInformationRelationOptions relationTypes, RepresentationInformation ri,
    Button confirmButton, List<HandlerRegistration> clickHandlers, DialogBox dialogBox,
    AsyncCallback<RepresentationInformationRelation> callback, AsyncCallback<Void> centerDialogBox) {
    aipLabel.addStyleName("dialog-left-item-selected");
    riLabel.removeStyleName("dialog-left-item-selected");
    txtLabel.removeStyleName("dialog-left-item-selected");
    webLabel.removeStyleName("dialog-left-item-selected");

    rightSide.clear();

    HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsWithIntellectualEntity.html", null,
      RodaConstants.ResourcesTypes.INTERNAL, centerDialogBox);
    description.addStyleName("page-description");
    rightSide.add(description);

    Label selectLabel = new Label(messages.representationInformationRelationType());
    selectLabel.addStyleName("form-label");
    rightSide.add(selectLabel);

    final ListBox select = new ListBox();
    select.addStyleName("form-listbox");

    for (Entry<String, String> type : relationTypes.getRelationsTranslations().get(RelationObjectType.AIP.toString())
      .entrySet()) {
      select.addItem(type.getValue(), type.getKey());
    }
    rightSide.add(select);

    Label linkLabel = new Label(messages.representationInformationRelationLink());
    linkLabel.addStyleName("form-label");
    rightSide.add(linkLabel);

    final Button button = new Button(messages.selectButton());
    button.addStyleName("btn btn-search");
    rightSide.add(button);

    final ValuedLabel linkText = new ValuedLabel();
    linkText.setStyleName("label");
    linkText.setVisible(false);
    rightSide.add(linkText);

    Label titleLabel = new Label(messages.representationInformationRelationTitle());
    titleLabel.addStyleName("form-label");
    rightSide.add(titleLabel);

    final TextBox titleBox = new TextBox();
    titleBox.addStyleName("form-textbox");
    rightSide.add(titleBox);

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<String> aipsAlreadyLinked = new ArrayList<>();

        if (ri != null) {
          for (RepresentationInformationRelation r : ri.getRelations()) {
            if (r.getObjectType().equals(RelationObjectType.AIP)) {
              aipsAlreadyLinked.add(r.getLink());
            }
          }
        }

        Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, aipsAlreadyLinked));
        SelectAipDialog selectAipDialog = new SelectAipDialog(messages.chooseEntityTitle(), filter, false, false);
        selectAipDialog.setSingleSelectionMode();
        selectAipDialog.showAndCenter();
        selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

          @Override
          public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
            final IndexedAIP aip = event.getValue();
            button.setVisible(false);
            linkText.setVisible(true);
            linkText.setValue(aip.getId());

            if (StringUtils.isNotBlank(aip.getTitle())) {
              linkText.setText(aip.getTitle());
            } else {
              linkText.setText(messages.noTitleMessage());
            }

            if (titleBox.getText().isEmpty()) {
              titleBox.setText(aip.getTitle());
            }

            confirmButton.setEnabled(true);
          }
        });
      }
    });

    for (HandlerRegistration handler : clickHandlers) {
      handler.removeHandler();
    }

    clickHandlers.add(confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!titleBox.getText().isEmpty() && !linkText.getValue().isEmpty()) {
          dialogBox.hide();
          callback.onSuccess(new RepresentationInformationRelation(select.getSelectedValue(), RelationObjectType.AIP,
            linkText.getValue(), titleBox.getValue()));
        } else {
          Toast.showError(messages.representationInformationMissingFieldsTitle(),
            messages.representationInformationMissingFields());
        }
      }
    }));
  }

  public static void showPromptAddRepresentationInformationWithAssociation(SafeHtml title,
    final String cancelButtonText, final String addToSelectedRIButtonText, final String addToNewRIButtonText,
    final AsyncCallback<SelectedItems<RepresentationInformation>> callback) {

    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.addStyleName("ri-dialog");
    dialogBox.setHTML(title);
    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button addToSelectedRIButton = new Button(addToSelectedRIButtonText);
    final Button addToNewRIButton = new Button(addToNewRIButtonText);
    addToSelectedRIButton.setEnabled(false);
    buttonPanel.add(cancelButton);
    buttonPanel.add(addToSelectedRIButton);
    buttonPanel.add(addToNewRIButton);

    final FlowPanel content = new FlowPanel();
    content.addStyleName("row skip_padding full_width content");
    content.add(createInnerAddRepresentationInformationwithAssociation(dialogBox, addToSelectedRIButton, callback));
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    addToNewRIButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(null);
      }
    });

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    cancelButton.addStyleName("btn btn-link");
    addToSelectedRIButton.addStyleName("pull-right btn btn-edit");
    addToNewRIButton.addStyleName("pull-right btn btn-plus");

    dialogBox.center();
    dialogBox.show();
  }

  public static FlowPanel createInnerAddRepresentationInformationwithAssociation(final DialogBox dialogBox,
    final Button addToSelectedRIButton, final AsyncCallback<SelectedItems<RepresentationInformation>> callback) {
    FlowPanel container = new FlowPanel();
    container.addStyleName("wui-dialog-message");

    // create search box and results list

    ListBuilder<RepresentationInformation> representationInformationListBuilder = new ListBuilder<>(
      () -> new RepresentationInformationList(),
      new AsyncTableCellOptions<>(RepresentationInformation.class, "RepresentationInformationDialogs_RI")
        .withSummary(messages.representationInformationTitle()).withInitialPageSize(10).withPageSizeIncrement(10)
        .withCsvDownloadButtonVisibility(false).withRecenteringOfParentDialog(dialogBox).withForceSelectable(true)
        .addCheckboxSelectionListener(new AsyncTableCell.CheckboxSelectionListener<RepresentationInformation>() {
          @Override
          public void onSelectionChange(SelectedItems<RepresentationInformation> selected) {
            if (selected instanceof SelectedItemsNone
              || (selected instanceof SelectedItemsList && ((SelectedItemsList) selected).getIds().isEmpty())) {
              addToSelectedRIButton.setEnabled(false);
            } else {
              addToSelectedRIButton.setEnabled(true);
            }
          }
        }));

    SearchWrapper searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("ri-dialog-list-scroll")
      .createListAndSearchPanel(representationInformationListBuilder);

    container.add(searchWrapper);

    addToSelectedRIButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(searchWrapper.getSelectedItems(RepresentationInformation.class));
    });

    return container;
  }
}
