package org.roda.wui.client.common.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.common.ValuedLabel;
import org.roda.wui.client.common.search.Dropdown;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RepresentationInformationDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RepresentationInformationDialogs() {
    // do nothing
  }

  public static void showPromptDialogRepresentationInformation(String title, String cancelButtonText,
    String confirmButtonText, final RepresentationInformation ri, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(true, true);
    dialogBox.addStyleName("wui-dialog-fixed");
    dialogBox.setText(title);

    final FlowPanel layout = new FlowPanel();

    HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html", new AsyncCallback<Void>() {
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

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(confirmButton);

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
          String className;

          if (!ri.getFilters().isEmpty()) {
            if (ri.getFilters().get(0).startsWith(Representation.class.getSimpleName())) {
              dropDown.setSelectedValue(RodaConstants.SEARCH_REPRESENTATIONS, true);
              className = Representation.class.getSimpleName();
              appropriateFields.addAll(result.getObjectClassFields().get(className));
            } else if (ri.getFilters().get(0).startsWith(File.class.getSimpleName())) {
              dropDown.setSelectedValue(RodaConstants.SEARCH_FILES, true);
              className = File.class.getSimpleName();
              appropriateFields.addAll(result.getObjectClassFields().get(className));
            } else {
              className = AIP.class.getSimpleName();
              appropriateFields.addAll(result.getObjectClassFields().get(className));
            }
          } else {
            className = AIP.class.getSimpleName();
            appropriateFields.addAll(result.getObjectClassFields().get(className));
          }

          for (String field : appropriateFields) {
            FlowPanel fieldPanel = new FlowPanel();
            fieldPanel.addStyleName("content ri-content");

            ValuedLabel fieldLabel = new ValuedLabel(result.getTranslations().get(
              className + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field), field);
            fieldLabel.addStyleName("form-label ri-content-label");
            fieldPanel.add(fieldLabel);

            TextBox fieldBox = new TextBox();

            for (String filter : ri.getFilters()) {
              if (filter.startsWith(
                className + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field)) {
                fieldBox.setText(RepresentationInformationUtils.getValueFromFilter(filter));
              }
            }

            fieldBox.addStyleName("form-textbox ri-content-textbox");
            fieldPanel.add(fieldBox);
            fieldsPanel.add(fieldPanel);
          }

          dropDown.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
              fieldsPanel.clear();
              String className = "";

              if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_ITEMS)) {
                className = AIP.class.getSimpleName();
                appropriateFields.clear();
                appropriateFields.addAll(result.getObjectClassFields().get(className));
              } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_REPRESENTATIONS)) {
                className = Representation.class.getSimpleName();
                appropriateFields.clear();
                appropriateFields.addAll(result.getObjectClassFields().get(className));
              } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_FILES)) {
                className = File.class.getSimpleName();
                appropriateFields.clear();
                appropriateFields.addAll(result.getObjectClassFields().get(className));
              }

              for (String field : appropriateFields) {
                FlowPanel fieldPanel = new FlowPanel();
                fieldPanel.addStyleName("content ri-content");

                ValuedLabel fieldLabel = new ValuedLabel(
                  result.getTranslations().get(
                    className + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field),
                  field);
                fieldLabel.addStyleName("form-label ri-content-label");
                fieldPanel.add(fieldLabel);

                TextBox fieldBox = new TextBox();

                for (String filter : ri.getFilters()) {
                  if (filter.startsWith(
                    className + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field)) {
                    fieldBox.setText(RepresentationInformationUtils.getValueFromFilter(filter));
                  }
                }

                fieldBox.addStyleName("form-textbox ri-content-textbox");
                fieldPanel.add(fieldBox);
                fieldsPanel.add(fieldPanel);
              }
            }
          });

          confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              ri.setFilters(new ArrayList<String>());

              for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
                Widget w = fieldsPanel.getWidget(i);

                if (w instanceof FlowPanel) {
                  FlowPanel panel = (FlowPanel) w;

                  for (int j = 0; j < panel.getWidgetCount(); j = j + 2) {
                    Widget w1 = panel.getWidget(j);
                    Widget w2 = panel.getWidget(j + 1);

                    if (w1 instanceof ValuedLabel && w2 instanceof TextBox) {
                      ValuedLabel label = (ValuedLabel) w1;
                      TextBox box = (TextBox) w2;

                      if (StringUtils.isNotBlank(box.getText())) {
                        if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_ITEMS)) {
                          ri.addFilter(RepresentationInformationUtils.createRepresentationInformationFilter(
                            AIP.class.getSimpleName(), label.getValue(), box.getValue()));
                        } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_REPRESENTATIONS)) {
                          ri.addFilter(RepresentationInformationUtils.createRepresentationInformationFilter(
                            Representation.class.getSimpleName(), label.getValue(), box.getValue()));
                        } else if (dropDown.getSelectedValue().equals(RodaConstants.SEARCH_FILES)) {
                          ri.addFilter(RepresentationInformationUtils.createRepresentationInformationFilter(
                            File.class.getSimpleName(), label.getValue(), box.getValue()));
                        }
                      }
                    }
                  }
                }
              }

              BrowserService.Util.getInstance().updateRepresentationInformation(ri, new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  AsyncCallbackUtils.defaultFailureTreatment(caught);
                }

                @Override
                public void onSuccess(Void result) {
                  ShowRepresentationInformation.getInstance().updateLists(ri);
                }
              });
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
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
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
      new AsyncCallback<Map<String, String>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Map<String, String> relationTypes) {
          final FlowPanel content = new FlowPanel();
          content.addStyleName("row skip_padding full_width");

          final FlowPanel leftSide = new FlowPanel();
          leftSide.addStyleName("dialog-left-side col3");

          final FlowPanel rightSide = new FlowPanel();
          rightSide.addStyleName("dialog-right-side col9");

          HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html",
            new AsyncCallback<Void>() {
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
          rightSide.add(description);

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

          final Label helpLabel = new Label();
          helpLabel.setText("Help");
          helpLabel.setTitle("Help");
          helpLabel.addStyleName("dialog-left-item-label dialog-left-item-selected");
          leftSide.add(helpLabel);

          content.add(leftSide);
          content.add(rightSide);
          layout.add(content);

          final FlowPanel buttonPanel = new FlowPanel();
          final Button cancelButton = new Button(cancelButtonText);
          final Button confirmButton = new Button(confirmButtonText);
          confirmButton.setEnabled(false);
          buttonPanel.add(cancelButton);
          buttonPanel.add(confirmButton);
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

          dialogBox.center();
          dialogBox.show();

          helpLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              aipLabel.removeStyleName("dialog-left-item-selected");
              riLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");
              helpLabel.addStyleName("dialog-left-item-selected");
              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html");
              description.addStyleName("page-description");
              rightSide.add(description);

              confirmButton.setEnabled(false);
            }
          });

          aipLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              aipLabel.addStyleName("dialog-left-item-selected");
              riLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");
              helpLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsWithIntellectualEntity.html");
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-selectbox");

              for (Entry<String, String> type : relationTypes.entrySet()) {
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
                      linkText.setText(aip.getTitle());
                      linkText.setValue(aip.getId());

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
                    callback.onSuccess(new RepresentationInformationRelation(select.getSelectedValue(),
                      RelationObjectType.AIP, linkText.getValue(), titleBox.getValue()));
                  } else {
                    Toast.showError(messages.representationInformationMissingFieldsTitle(),
                      messages.representationInformationMissingFields());
                  }
                }
              }));
            }
          });

          riLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              riLabel.addStyleName("dialog-left-item-selected");
              aipLabel.removeStyleName("dialog-left-item-selected");
              txtLabel.removeStyleName("dialog-left-item-selected");
              webLabel.removeStyleName("dialog-left-item-selected");
              helpLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html");
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-selectbox");
              for (Entry<String, String> type : relationTypes.entrySet()) {
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
                  selectDialog.setEmptyParentButtonVisible(true);
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
              helpLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html");
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-selectbox");
              for (Entry<String, String> type : relationTypes.entrySet()) {
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
              helpLabel.removeStyleName("dialog-left-item-selected");

              rightSide.clear();

              HTMLWidgetWrapper description = new HTMLWidgetWrapper("RIRelationsDescription.html");
              description.addStyleName("page-description");
              rightSide.add(description);

              Label selectLabel = new Label(messages.representationInformationRelationType());
              selectLabel.addStyleName("form-label");
              rightSide.add(selectLabel);

              final ListBox select = new ListBox();
              select.addStyleName("form-selectbox");
              for (Entry<String, String> type : relationTypes.entrySet()) {
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
}
