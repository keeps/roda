package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.client.common.ValuedLabel;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.search.Dropdown;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.planning.RelationTypeTranslationsBundle;
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
import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class RepresentationInformationDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RepresentationInformationDialogs() {
    // do nothing
  }

  public static void showPromptDialogRepresentationInformation(String title, String cancelButtonText,
    String confirmButtonText, String listButtonText, final RepresentationInformation ri,
    final AsyncCallback<RepresentationInformation> callback) {
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.addStyleName("wui-dialog-fixed");
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIAssociationsDescription.html", new AsyncCallback<Void>() {
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
    final Button listButton = new Button(listButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(confirmButton);
    buttonPanel.add(listButton);

    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    final Dropdown dropDown = new Dropdown();
    dropDown.setStyleName("searchInputListBox ri-dropdown-listbox");
    dropDown.addPopupStyleName("searchInputListBoxPopup");
    dropDown.setVisible(true);
    dropDown.setLabel(messages.searchListBoxItems());
    dropDown.addItem(messages.searchListBoxItems(), RodaConstants.SEARCH_ITEMS);
    dropDown.addItem(messages.searchListBoxRepresentations(), RodaConstants.SEARCH_REPRESENTATIONS);
    dropDown.addItem(messages.searchListBoxFiles(), RodaConstants.SEARCH_FILES);

    final FlowPanel fieldsPanel = new FlowPanel();
    fieldsPanel.setStyleName("ri-content-group");

    BrowserService.Util.getInstance().retrieveObjectClassFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RepresentationInformationFilterBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final RepresentationInformationFilterBundle result) {
          relationFormPanel.add(dropDown);
          relationFormPanel.add(fieldsPanel);

          final List<String> appropriateFields = new ArrayList<>();
          final Map<String, List<String>> values = new HashMap<>();

          dropDown.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
              updateAssociationFields(fieldsPanel, dropDown, appropriateFields, result, ri, values);
              dialogBox.center();
            }
          });

          if (!ri.getFilters().isEmpty()) {
            if (ri.getFilters().get(0).startsWith(Representation.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(Representation.class.getSimpleName()));
              dropDown.setSelectedValue(RodaConstants.SEARCH_REPRESENTATIONS, true);
            } else if (ri.getFilters().get(0).startsWith(File.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(File.class.getSimpleName()));
              dropDown.setSelectedValue(RodaConstants.SEARCH_FILES, true);
            } else if (ri.getFilters().get(0).startsWith(AIP.class.getSimpleName())) {
              appropriateFields.addAll(result.getObjectClassFields().get(AIP.class.getSimpleName()));
              dropDown.setSelectedValue(RodaConstants.SEARCH_ITEMS, true);
            }
          } else {
            appropriateFields.addAll(result.getObjectClassFields().get(AIP.class.getSimpleName()));
            dropDown.setSelectedValue(RodaConstants.SEARCH_ITEMS, true);
          }

          dialogBox.center();

          confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              ri.setFilters(new ArrayList<String>());

              String className;
              switch (dropDown.getSelectedValue()) {
                case RodaConstants.SEARCH_ITEMS:
                  className = AIP.class.getSimpleName();
                  break;
                case RodaConstants.SEARCH_REPRESENTATIONS:
                  className = Representation.class.getSimpleName();
                  break;
                case RodaConstants.SEARCH_FILES:
                  className = File.class.getSimpleName();
                  break;
                default:
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

          listButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              List<FilterParameter> filterList = new ArrayList<>();
              for (String field : values.keySet()) {
                for (String value : values.get(field)) {
                  if (StringUtils.isNotBlank(value)) {
                    filterList.add(new SimpleFilterParameter(field, value));
                  }
                }
              }

              listPanel.clear();

              if (!filterList.isEmpty()) {
                BasicAsyncTableCell<?> table = null;
                Filter tableFilter = new Filter(new OrFiltersParameters(filterList));
                switch (dropDown.getSelectedValue()) {
                  case RodaConstants.SEARCH_ITEMS:
                    table = new AIPList(tableFilter, true, Facets.NONE, "", false, 5, 5);
                    break;
                  case RodaConstants.SEARCH_REPRESENTATIONS:
                    table = new RepresentationList(tableFilter, true, Facets.NONE, "", false, 5, 5);
                    break;
                  case RodaConstants.SEARCH_FILES:
                    table = new SimpleFileList(tableFilter, true, Facets.NONE, "", false, 5, 5);
                    break;
                  default:
                    break;
                }

                if (table != null) {
                  table.addRedrawHandler(new AbstractHasData.RedrawEvent.Handler() {
                    @Override
                    public void onRedraw() {
                      dialogBox.center();
                    }
                  });

                  listPanel.add(section);
                  listPanel.add(table);
                }
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
    listButton.addStyleName("pull-right btn btn-search");

    dialogBox.center();
    dialogBox.show();
  }

  private static void updateAssociationFields(FlowPanel fieldsPanel, Dropdown dropDown, List<String> appropriateFields,
    RepresentationInformationFilterBundle result, RepresentationInformation ri,
    final Map<String, List<String>> values) {
    fieldsPanel.clear();
    String className = null;

    if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_ITEMS)) {
      className = AIP.class.getSimpleName();
    } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_REPRESENTATIONS)) {
      className = Representation.class.getSimpleName();
    } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_FILES)) {
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
      incrementalList.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
        @Override
        public void onValueChange(ValueChangeEvent<List<String>> event) {
          values.put(field, event.getValue());
        }
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

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    BrowserService.Util.getInstance().retrieveRelationTypeTranslations(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RelationTypeTranslationsBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final RelationTypeTranslationsBundle relationTypes) {
          final FlowPanel content = new FlowPanel();
          content.addStyleName("row skip_padding full_width content");

          final FlowPanel leftSide = new FlowPanel();
          leftSide.addStyleName("dialog-left-side col3");

          final FlowPanel rightSide = new FlowPanel();
          rightSide.addStyleName("dialog-right-side col9");

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
          buttonPanel.add(cancelButton);
          buttonPanel.add(confirmButton);
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

          cancelButton.addStyleName("btn btn-link");
          confirmButton.addStyleName("pull-right btn btn-play");
          helpLabel.addStyleName("pull-right btn btn-link");

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

          showAIPDescription(aipLabel, riLabel, txtLabel, webLabel, rightSide, relationTypes, ri, confirmButton,
            clickHandlers, dialogBox, callback, centerDialogBox);

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

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionHelp.html", centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              confirmButton.setEnabled(false);
            }
          });

          aipLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              showAIPDescription(aipLabel, riLabel, txtLabel, webLabel, rightSide, relationTypes, ri, confirmButton,
                clickHandlers, dialogBox, callback, centerDialogBox);
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

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithRI.html",
                centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : relationTypes.getTranslations()
                .get(RelationObjectType.REPRESENTATION_INFORMATION).entrySet()) {
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
                  List<String> riAlreadyLinked = new ArrayList<>();

                  if (ri != null) {
                    for (RepresentationInformationRelation r : ri.getRelations()) {
                      if (r.getObjectType().equals(RelationObjectType.REPRESENTATION_INFORMATION)) {
                        riAlreadyLinked.add(r.getLink());
                      }
                    }
                  }

                  Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, riAlreadyLinked));
                  SelectRepresentationInformationDialog selectDialog = new SelectRepresentationInformationDialog(
                    messages.moveItemTitle(), filter, false);
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

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithText.html",
                centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : relationTypes.getTranslations().get(RelationObjectType.TEXT)
                .entrySet()) {
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

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescriptionWithWeb.html",
                centerDialogBox);
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-listbox");
              for (Entry<String, String> type : relationTypes.getTranslations().get(RelationObjectType.WEB)
                .entrySet()) {
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
    FlowPanel rightSide, RelationTypeTranslationsBundle relationTypes, RepresentationInformation ri,
    Button confirmButton, List<HandlerRegistration> clickHandlers, DialogBox dialogBox,
    AsyncCallback<RepresentationInformationRelation> callback, AsyncCallback<Void> centerDialogBox) {
    aipLabel.addStyleName("dialog-left-item-selected");
    riLabel.removeStyleName("dialog-left-item-selected");
    txtLabel.removeStyleName("dialog-left-item-selected");
    webLabel.removeStyleName("dialog-left-item-selected");

    rightSide.clear();

    HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsWithIntellectualEntity.html", centerDialogBox);
    description.addStyleName("page-description");
    rightSide.add(description);

    Label selectLabel = new Label(messages.representationInformationRelationType());
    selectLabel.addStyleName("form-label");
    rightSide.add(selectLabel);

    final ListBox select = new ListBox();
    select.addStyleName("form-listbox");

    for (Entry<String, String> type : relationTypes.getTranslations().get(RelationObjectType.AIP).entrySet()) {
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
        SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, false, false);
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
    final AsyncCallback<SelectedItemsList<RepresentationInformation>> callback) {

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
    final Button addToSelectedRIButton, final AsyncCallback<SelectedItemsList<RepresentationInformation>> callback) {
    FlowPanel container = new FlowPanel();
    container.addStyleName("wui-dialog-message");

    // create search box and results list
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_TAGS),
      new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT));
    Filter defaultFilter = SearchFilters.defaultFilter(RepresentationInformation.class.getName());
    final RepresentationInformationList representationInformationList = new RepresentationInformationList(defaultFilter,
      facets, messages.representationInformationTitle(), true, 10, 10);
    representationInformationList.addRedrawHandler(new AbstractHasData.RedrawEvent.Handler() {
      @Override
      public void onRedraw() {
        dialogBox.center();
      }
    });

    SearchPanel representationInformationSearch = new SearchPanel(Filter.NULL,
      RodaConstants.REPRESENTATION_INFORMATION_SEARCH, true, messages.searchPlaceHolder(), false, false, true);
    representationInformationSearch.setList(representationInformationList);

    container.add(representationInformationSearch);

    ScrollPanel representationInformationListScrollPanel = new ScrollPanel(representationInformationList);
    representationInformationListScrollPanel.addStyleName("ri-dialog-list-scroll");
    container.add(representationInformationListScrollPanel);

    representationInformationList
      .addCheckboxSelectionListener(new AsyncTableCell.CheckboxSelectionListener<RepresentationInformation>() {
        @Override
        public void onSelectionChange(SelectedItems<RepresentationInformation> selected) {
          if (selected instanceof SelectedItemsList) {
            SelectedItemsList<RepresentationInformation> list = (SelectedItemsList<RepresentationInformation>) selected;
            addToSelectedRIButton.setEnabled(!list.getIds().isEmpty());
          } else {
            // TODO bferreira 2017-12-04: add support for SelectedItemsFilter
            // (is it needed?)
            throw new RuntimeException("Only SelectedItemsList is supported on RI, for now");
          }
        }
      });

    addToSelectedRIButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();

        SelectedItems<RepresentationInformation> selected = representationInformationList.getSelected();
        if (selected instanceof SelectedItemsList) {
          SelectedItemsList<RepresentationInformation> list = (SelectedItemsList<RepresentationInformation>) selected;
          callback.onSuccess(list);
        } else {
          // TODO bferreira 2017-12-04: add support for SelectedItemsFilter (is
          // it needed?)
          throw new RuntimeException("Only SelectedItemsList is supported on RI, for now");
        }
      }
    });

    return container;
  }
}
