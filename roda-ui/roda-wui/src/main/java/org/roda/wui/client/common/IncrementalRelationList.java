/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.planning.RelationTypeTranslationsBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class IncrementalRelationList extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, IncrementalRelationList> {
  }

  @UiField
  FlowPanel contentPanel;

  @UiField
  Button addDynamicButton;

  private Map<String, List<RemovableRelation>> relations;
  boolean changed = false;
  private RepresentationInformation ri;

  public IncrementalRelationList(RepresentationInformation ri) {
    this.ri = ri;
    initWidget(uiBinder.createAndBindUi(this));
    relations = new HashMap<>();
  }

  public List<RepresentationInformationRelation> getValues() {
    ArrayList<RepresentationInformationRelation> listValues = new ArrayList<>();
    for (List<RemovableRelation> relationList : relations.values()) {
      for (RemovableRelation relation : relationList) {
        listValues.add(relation.getValue());
      }
    }
    return listValues;
  }

  public void setRelationList(final List<RepresentationInformationRelation> list) {
    BrowserService.Util.getInstance().retrieveRelationTypeTranslations(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RelationTypeTranslationsBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final RelationTypeTranslationsBundle bundle) {
          for (RepresentationInformationRelation element : list) {
            addRelation(element, false, bundle.getTranslations());
          }

          listRelations();
        }
      });
  }

  public void clear() {
    contentPanel.clear();
    relations = new HashMap<>();
  }

  private void addRelation(final RepresentationInformationRelation element, final boolean redesign,
    final Map<RelationObjectType, Map<String, String>> translations) {

    final RemovableRelation relation = new RemovableRelation(element);
    String relationType = translations.get(element.getObjectType()).get(element.getRelationType());

    if (relations.containsKey(relationType)) {
      relations.get(relationType).add(relation);
    } else {
      List<RemovableRelation> list = new ArrayList<>();
      list.add(relation);
      relations.put(relationType, list);
    }

    relation.addRemoveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        relations.get(translations.get(relation.getValue().getRelationType())).remove(relation);
        listRelations();
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalRelationList.this);
      }
    });

    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalRelationList.this);

    if (redesign) {
      listRelations();
    }
  }

  public void listRelations() {
    contentPanel.clear();

    for (Entry<String, List<RemovableRelation>> entry : relations.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        Label typeLabel = new Label(entry.getKey());
        typeLabel.addStyleName("label");
        contentPanel.add(typeLabel);

        for (RemovableRelation relation : entry.getValue()) {
          contentPanel.add(relation);
        }
      }
    }
  }

  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  @UiHandler("addDynamicButton")
  void addMore(ClickEvent event) {
    RepresentationInformationDialogs.showPromptDialogRepresentationInformationRelations(
      messages.representationInformationAddNewRelation(), messages.cancelButton(), messages.confirmButton(), ri,
      new AsyncCallback<RepresentationInformationRelation>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(final RepresentationInformationRelation newRelation) {
          DomEvent.fireNativeEvent(Document.get().createChangeEvent(), IncrementalRelationList.this);

          BrowserService.Util.getInstance().retrieveRelationTypeTranslations(
            LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<RelationTypeTranslationsBundle>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(final RelationTypeTranslationsBundle bundle) {
                addRelation(newRelation, true, bundle.getTranslations());
              }
            });
        }
      });
  }
}
